//TODO: TESTAR MAIS!

package Backup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import Data.Chunk;
import Data.File;
import Message.Header;
import Message.Message;
import Channel.Multicast;

public final class Backup {

	private static String MCport = "50001";
	public static String MCgroup = "239.254.254.252";
	private static String MDBport = "50002";
	public static String MDBgroup = "239.254.254.253";
	private static String MDRport = "50003";
	public static String MDRgroup = "239.254.254.254";
	public static String version = "1.0";
	public static long maxSpace = 256000;
	public static long usedSpace = 0;
	public static Multicast MC = new Multicast(MCgroup, MCport);
	public static Multicast MDB = new Multicast(MDBgroup, MDBport);
	public static Multicast MDR = new Multicast(MDRgroup, MDRport);
	public static final int putchunkDelay = 500;

	public static List<File> files = new ArrayList<File>(); // ficheiros que
															// pertencem a este
															// peer

	public static List<Chunk> chunks = new ArrayList<Chunk>(); // todos os
																// chunks

	public static void loadFiles() throws NullPointerException {
		try {
			java.io.File folder = new java.io.File("files/");
			FileInputStream fis;
			ObjectInputStream ois;
			java.io.File[] filesList = folder.listFiles();
			if (filesList.length == 0) // pasta vazia
				throw new NullPointerException(); // mostrar mensagem de aviso
			for (final java.io.File fileEntry : filesList) {
				fis = new FileInputStream(fileEntry);
				ois = new ObjectInputStream(fis);
				File file = (File) ois.readObject();
				files.add(file);
				usedSpace += addFileChunksToChunkArray(file);
				ois.close();
				fis.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void loadChunks() throws NullPointerException {
		try {
			java.io.File folder = new java.io.File("chunks/");
			FileInputStream fis;
			ObjectInputStream ois;
			java.io.File[] filesList = folder.listFiles();
			if (filesList.length == 0) // pasta vazia
				throw new NullPointerException(); // mostrar mensagem de aviso
			for (final java.io.File fileEntry : filesList) { // pastas
																// para
																// cada
																// ficheiro
				// filtro para apenas ler ficheiros de serialize
				FilenameFilter filter = new FilenameFilter() {

					@Override
					public boolean accept(java.io.File dir, String name) {
						if (name.contains(".ser"))
							return true;
						return false;
					}
				};
				if (fileEntry.isDirectory()) {
					for (final java.io.File fileEntry2 : fileEntry
							.listFiles(filter)) { // chunks
						// de
						// cada
						// ficheiro
						fis = new FileInputStream(fileEntry2);
						ois = new ObjectInputStream(fis);
						Chunk chunk = (Chunk) ois.readObject();
						ois.close();
						fis.close();
						if (getFileByID(chunk.getFileID()) == null) // ficheiro
																	// n�o
																	// existe
																	// (chunk
																	// remoto)
						{
							chunks.add(chunk);
							usedSpace += chunk.getSize();
						}
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// delete enchancement (reenvia delete aquando da abertura do programa, para
	// garantir remo��o)
	public static void resendDelete() {
		try {
			java.io.File folder = new java.io.File("deletedFiles/");
			FileInputStream fis;
			ObjectInputStream ois;
			for (final java.io.File fileEntry : folder.listFiles()) {
				fis = new FileInputStream(fileEntry);
				ois = new ObjectInputStream(fis);
				File file = (File) ois.readObject();
				file.setDeleted(true); // marca para remo��o
				deleteFile(file);
				ois.close();
				fis.close();
				fileEntry.delete();
			}
		} catch (NullPointerException e) {
			// do nothing
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void saveFiles() {
		for (int i = 0; i < files.size(); i++)
			files.get(i).serialize();
	}

	public static void saveChunks() {
		for (int i = 0; i < chunks.size(); i++)
			chunks.get(i).serialize();
	}

	public static File getFileByID(String fileID) {
		for (int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			if (file.getId().equals(fileID))
				return file;
		}
		return null;
	}

	public static Chunk getChunkByID(String fileID, int chunkNo) {
		for (int i = 0; i < chunks.size(); i++) {
			Chunk chunk = chunks.get(i);
			if (chunk.getChunkNo() == chunkNo
					&& chunk.getFileID().equals(fileID))
				return chunk;
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		String cmd;
		Scanner sc = new Scanner(System.in);
		String data[] = null;

		MC.start();
		MDB.start();
		MDR.start();

		try {
			loadFiles();
		} catch (NullPointerException n) {
			System.out.println("No files to load");
		}

		try {
			loadChunks();
		} catch (NullPointerException n) {
			System.out.println("No chunks to load");
		}

		try {
			loadConfs();
		} catch (Exception n) {
			System.out.println("No config files to load");
		}

		resendDelete();

		while (true) {
			System.out.println("Choose one option\n");
			System.out.println("1 ------------- Backup File");
			System.out.println("2 ------------- Restore File");
			System.out.println("3 ------------- Delete file");
			System.out.println("4 ------------- Change Settings");
			System.out.println("5 ------------- Exit");

			System.out.print('>');
			cmd = sc.nextLine();

			switch (cmd) {
			//TODO: corrigir bugs nos inputs
			case "1":
				System.out
						.println("Write the file name and replication number <Filename> <RepNumber>");
				cmd = sc.nextLine();
				data = cmd.split(" ");
				File file = new File(data[0], Integer.parseInt(data[1]));
				if (getFileByID(file.getFileID()) == null) {
					file.chunker();
					if (usedSpace > maxSpace)
						System.out
								.println("Max space reached! Allocate more space!");
					files.add(file);
					addFileChunksToChunkArray(file);
					sendBackup(file);

				} else
					System.out.println("File already in the system!");
				break;
			case "2":
				System.out.println("Choose which file to restore");
				try {
					file = selectFile(sc);
					getMisingChunks(file);

				} catch (FileNotFoundException e) {
					System.out.println("File not found!");
				}
				break;
			case "3":
				System.out.println("Choose which file to delete");
				try {
					file = selectFile(sc);
					deleteFile(file);
				} catch (FileNotFoundException e) {
					System.out.println("File not found!");
				}
				break;
			case "send":
				Message msg = new Message(new Header("PUTCHUNK", version,
						"Teste", 0, 1), null);
				MC.send(msg);
				break;
			case "4":
				System.out.println("Current Settings");
				System.out.print("MC group :  " + MCgroup);
				System.out.println("  MC port  : " + MCport);
				System.out.print("MDB group : " + MDBgroup);
				System.out.println("  MDB port : " + MDBport);
				System.out.print("MDR group : " + MDRgroup);
				System.out.println("  MDR port : " + MDRport);
				System.out.println("\nUsed Space  :  " + usedSpace);
				System.out.println("Max Space  : " + maxSpace);
				System.out.println("1 ---------------- Control Channel");
				System.out.println("2 ---------------- Data Backup Channel");
				System.out.println("3 ---------------- Data Recovery Channel");
				System.out.println("4 ---------------- Max storage space");

				cmd = sc.nextLine();

				if (cmd.equals("1") || cmd.equals("2") || cmd.equals("3")) {
					System.out
							.println("Insert Group IP and Port <group> <Port>");
					String cenas = sc.nextLine();
					data = cenas.split(" ");
				}
				switch (cmd) {
				case "1":
					MCgroup = data[0];
					MCport = data[1];
					break;
				case "2":
					MDBgroup = data[0];
					MDBport = data[1];
					break;
				case "3":
					MDRgroup = data[0];
					MDRport = data[1];
					break;
				case "4":
					System.out.println("Used storage: " + usedSpace);
					System.out.println("Max storage: " + maxSpace);
					System.out.print("New max storage: ");
					maxSpace = Long.parseLong(sc.nextLine());
					reclaimChoice();
					break;

				default:
					System.out.println("choose the right number please");
					break;

				}
				saveConfs();
				break;
			case "5":
				sc.close();
				MC.interrupt();
				MDB.interrupt();
				MDR.interrupt();
				saveFiles();
				saveChunks();
				saveConfs();
				return;
			default:
				System.out.println("choose the right number please");
				break;
			}
		}
	}

	public static void saveConfs() {

		PrintWriter out;
		try {
			out = new PrintWriter("confs.rv");
			out.println(MCgroup + " " + MCport);
			out.println(MDBgroup + " " + MDBport);
			out.println(MDRgroup + " " + MDRport);
			out.println(maxSpace);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void loadConfs() throws Exception {
		java.io.File fi = new java.io.File("confs.rv");
		BufferedReader br;
		br = new BufferedReader(new FileReader(fi));
		String line;
		String[] temp;
		line = br.readLine();
		temp = line.split(" ");
		MCgroup = temp[0];
		MCport = temp[1];
		line = br.readLine();
		temp = line.split(" ");
		MDBgroup = temp[0];
		MDBport = temp[1];
		line = br.readLine();
		temp = line.split(" ");
		MDRgroup = temp[0];
		MDRport = temp[1];
		line = br.readLine();
		maxSpace = Integer.parseInt(line);

		br.close();

	}

	public static long addFileChunksToChunkArray(File file) {
		long totalSpace = 0;
		for (int i = 0; i < file.getChunks().size(); i++) {
			if (file.getChunks().get(i).getFile() != null) { // chunk foi
																// reclaimed
				chunks.add(file.getChunks().get(i));
				totalSpace += file.getChunks().get(i).getSize();
			}
		}
		return totalSpace;
	}

	public static File selectFile(Scanner sc) throws FileNotFoundException {
		// TODO: mostrar modification time
		try {
			int i;
			for (i = 0; i < files.size(); i++) {
				System.out.println(i + ": " + files.get(i).getName());
			}
			int fileNo = Integer.parseInt(sc.nextLine());

			if (fileNo >= i || fileNo < 0)
				throw new FileNotFoundException();
			else
				return files.get(fileNo);
		} catch (NumberFormatException e) {
			throw new FileNotFoundException(); // se n�mero inv�lido, atira
												// filenotfound
		}
	}

	public static void deleteFile(File file) // peer local
	{
		Header header = new Header("DELETE", null, file.getId(), null, null);
		Message message = new Message(header, null);
		MC.send(message);
		if (!file.isDeleted()) {
			for (final Chunk chunk : file.getChunks()) {
				chunk.delete(); // apaga ficheiros
				chunks.remove(chunk);
			}
			file.delete(); // apaga ficheiro
			file.serializeDeletedFile(); // guarda para posterior DELETE
			files.remove(file); // apaga o ficheiro
		} else {
			file.eraseDeletedFile();
		}
	}

	public static void deleteFile(String fileID) // peer remoto
	{
		Iterator<Chunk> iterator = chunks.iterator();
		Chunk chunk = null;
		while (iterator.hasNext()) {
			chunk = iterator.next();
			if (chunk.getFileID().equals(fileID)) {
				chunk.delete();
				iterator.remove();
			}
		}
		java.io.File folder = new java.io.File("chunks/" + fileID + '/');
		folder.delete();

		System.out.println("Deleted " + fileID);
	}

	public static void sendBackup(File file) {
		for (int i = 0; i < file.getChunks().size(); i++)
			putChunk(file.getChunks().get(i));
	}

	public static void reclaimChoice() {
		while (usedSpace > maxSpace) {
			reclaim(chunks.get(0)); // apaga primeiro chunk
		}
	}

	public static void reclaim(Chunk chunk) {
		chunk.delete();
		Header header = new Header("REMOVED", version, chunk.getFileID(),
				chunk.getChunkNo(), null);
		Message message = new Message(header, null);
		MC.send(message);
		chunks.remove(chunk); // se for o peer local, vai ficar na lista files
	}

	public static void removed(String fileID, int chunkNo) {
		try {
			final Chunk removedChunk = getChunkByID(fileID, chunkNo);
			if (removedChunk != null) // temos o chunk
			{
				removedChunk.decrementCurrentReplicationDeg();
				if (removedChunk.getCurrentReplicationDeg() < removedChunk
						.getReplicationDeg()) {
					MDB.ignoreChunk = false;
					MDB.ignoreChunkNo = removedChunk.getChunkNo();
					MDB.ignoreFileID = removedChunk.getFileID();
					Thread.sleep(Math.round(Math.random() * 400));
					if (MDB.ignoreChunk == false) // � recebeu entretanto
													// PUTCHUNK com o mm fileID
													// / chunkNo
						new Thread() { // esperar por tempo m�ximo de putchunk

							@Override
							public void run() {
								putChunk(removedChunk);
							}
						}.start();
					;
					MDB.ignoreChunk = null;
					MDB.ignoreChunkNo = null;
					MDB.ignoreFileID = null;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// n�o tem o ficheiro (ignora)
		}
	}

	public static void putChunk(Chunk chunk) {
		// TODO: permitir aumentar repdeg
		try {
			int waitTime = putchunkDelay;
			Header header = new Header("PUTCHUNK", version, chunk.getFileID(),
					chunk.getChunkNo(), chunk.getReplicationDeg());
			Message message = new Message(header, chunk);
			for (int i = 0; i < 5; i++) // m�x 5 retries
			{
				MDB.send(message);
				System.out.println("Waiting for stored messages...");
				Thread.sleep(waitTime);
				// TODO: interromper sleep se receber entretanto
				waitTime *= 2; // duplica tempo de espera
				if (chunk.getCurrentReplicationDeg() >= chunk
						.getReplicationDeg())
					return; // done
			}
			// para de tentar
			System.out.println("Replication degree not met. Giving up...");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void stored(Chunk chunk, byte[] chunkData) { // recebido
																// putchunk
		if (getChunkByID(chunk.getFileID(), chunk.getChunkNo()) == null) // ainda
																			// n�o
																			// existe
		{
			if (usedSpace + chunk.getSize() <= maxSpace) { // se tiver espa�o
															// para guardar
				chunks.add(chunk);
				try {
					Thread.sleep(Math.round(Math.random() * 400));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (chunk.getCurrentReplicationDeg() < chunk
						.getReplicationDeg()) // se n�o tiver atingido rep deg
												// desejado, guarda e envia
												// stored
				{
					chunk.write(chunkData, chunkData.length);
					Header header = new Header("STORED", version,
							chunk.getFileID(), chunk.getChunkNo(), null);
					Message message = new Message(header, null);
					MC.send(message);
					chunk.incrementCurrentReplicationDeg();
				} else
					chunks.remove(chunk); // descarta (ENHANCEMENT)
			} else
				System.out.println("Not enough space to store the chunk...");
		} else
			System.out.println("Chunk already stored!");
	}

	public static void sendChunk(String fileId, Integer chunkNo) {

		try {
			Chunk chunk = getChunkByID(fileId, chunkNo);

			if (chunk != null) {
				Header header = new Header("CHUNK", version, chunk.getFileID(),
						chunk.getChunkNo(), null);
				Message message = new Message(header, chunk);
				MDR.ignoreChunk = false;
				MDR.ignoreChunkNo = chunk.getChunkNo();
				MDR.ignoreFileID = chunk.getFileID();
				Thread.sleep(Math.round(Math.random() * 400));
				if (MDR.ignoreChunk == false) // � recebeu entretanto
					// CHUNK com o mm fileID
					// / chunkNo
					MDR.send(message);

				MDR.ignoreChunk = null;
				MDR.ignoreChunkNo = null;
				MDR.ignoreFileID = null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getMisingChunks(File file) {
		if (file.gotAllChunks()) {
			System.out.println("Got all chunks. Restoring now...");
			file.dechunker();
		} else {
			Iterator<Chunk> iterator = file.getChunks().iterator();
			Chunk chunk = null;
			while (iterator.hasNext()) {
				chunk = iterator.next();
				if (chunk.getFile() == null) {
					askChunk(chunk);
				}
			}
		}
	}

	public static void askChunk(Chunk chunk) {
		Header header = new Header("GETCHUNK", version, chunk.getFileID(),
				chunk.getChunkNo(), null);
		Message message = new Message(header, null);
		MC.send(message);
	}

	public static void gotChunk(Chunk chunk, byte[] chunkData) {
		File file = getFileByID(chunk.getFileID());
		if (file == null) // n�o foi pedido por este peer
			return;
		Chunk chunkTemp = null;
		for (int i = 0; i < file.getChunks().size(); i++) {
			chunkTemp = file.getChunks().get(i);
			if (chunkTemp.getFileID().equals(chunk.getFileID())
					&& chunkTemp.getChunkNo() == chunk.getChunkNo()) {
				if (chunkTemp.getFile() != null) // j� tem o ficheiro, descarta
				{
					System.out.println("Chunk already received, ignoring...");
					return;
				}
				chunkTemp.write(chunkData, chunkData.length);
				chunks.add(chunkTemp);
			}
		}

		// se tiver os chunks todos, reconstroi ficheiro
		if (file.gotAllChunks()) {
			System.out.println("Got all chunks. Restoring now...");
			file.dechunker();
		}
	}
}
