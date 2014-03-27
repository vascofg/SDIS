//TODO: TESTAR MAIS!

package Backup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
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

	private static final String MCport = "50001";
	public static final String MCgroup = "239.254.254.252";
	private static final String MDBport = "50002";
	public static final String MDBgroup = "239.254.254.253";
	private static final String MDRport = "50003";
	public static final String MDRgroup = "239.254.254.254";
	public static final String version = "1.0";
	public static long maxSpace = 256000;
	// TODO: ler maxSpace de config file
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
		java.io.File folder = new java.io.File("files/");
		FileInputStream fis;
		ObjectInputStream ois;
		for (final java.io.File fileEntry : folder.listFiles()) {
			try {
				fis = new FileInputStream(fileEntry);
				ois = new ObjectInputStream(fis);
				File file = (File) ois.readObject();
				files.add(file);
				usedSpace += addFileChunksToChunkArray(file);
				ois.close();
				fis.close();
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void loadChunks() throws NullPointerException {
		java.io.File folder = new java.io.File("chunks/");
		FileInputStream fis;
		ObjectInputStream ois;
		for (final java.io.File fileEntry : folder.listFiles()) { // pastas para
																	// cada
																	// ficheiro
			try {
				// filtro para apenas ler ficheiros de serialize
				FilenameFilter filter = new FilenameFilter() {

					@Override
					public boolean accept(java.io.File dir, String name) {
						if (name.contains(".ser"))
							return true;
						return false;
					}
				};
				// TODO: no chunks to load if directory empty
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
																	// não
																	// existe
																	// (chunk
																	// remoto)
						{
							chunks.add(chunk);
							usedSpace += chunk.getSize();
						}
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			if (file.getFileID().equals(fileID))
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
		String data[];

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

		while (true) {
			System.out.print('>');
			cmd = sc.nextLine();
			data = cmd.split(" ");

			switch (data[0]) // backup <filename> <repdeg>, restore
			{
			case "backup":
				if (data.length < 3)
					System.out.println("usage: backup <filename> <repdeg>");
				else {
					File file = new File(data[1], Integer.parseInt(data[2]));
					if (getFileByID(file.getFileID()) == null) {
						file.chunker();
						if (usedSpace > maxSpace)
							System.out
									.println("Max space reached! Allocate more space!");
						files.add(file);
						addFileChunksToChunkArray(file);
						sendBackup(file);
					}
				}
				break;
			case "restore":
				int i;
				System.out.println("Choose which file to restore");
				try {
					File file = selectFile(sc);
					getMisingChunks(file);
					//TODO: restaurar chunks automagically
				} catch (FileNotFoundException e) {
					System.out.println("File not found!");
				}
				break;
			case "delete":
				System.out.println("Choose which file to delete");
				File file = selectFile(sc);
				deleteFile(file);
				break;
			case "send":
				Message msg = new Message(new Header("PUTCHUNK", version,
						"Teste", 0, 1), null);
				MC.send(msg);
				break;
			case "reclaim":// TODO: mudar nome
				System.out.println("Espaço actual: " + usedSpace);
				System.out.println("Espaço máximo: " + maxSpace);
				System.out.print("Novo espaço máximo: ");
				maxSpace = sc.nextLong();
				reclaimChoice();
				break;
			case "teste":
				file = new File("bolha.png", 1);
				for (i = 0; i < chunks.size(); i++) {
					file.addChunk(chunks.get(i));
				}
				file.setId(chunks.get(0).getFileID());
				file.dechunker();
				break;
			case "exit":
				sc.close();
				MC.interrupt();
				MDB.interrupt();
				MDR.interrupt();
				saveFiles();
				saveChunks();
				return;
			}
		}
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
		int i;
		for (i = 0; i < files.size(); i++) {
			System.out.println(i + ": " + files.get(i).getName());
		}
		int fileNo = sc.nextInt();
		if (fileNo >= i || fileNo < 0)
			throw new FileNotFoundException();
		else
			return files.get(fileNo);
	}

	public static void deleteFile(File file) // peer local
	{
		Header header = new Header("DELETE", null, file.getFileID(), null, null);
		Message message = new Message(header, null);
		MC.send(message);
		// TODO: mandar várias vezes para confirmar que é apagado (maybe)
		for (final Chunk chunk : file.getChunks()) {
			chunk.delete(); // apaga ficheiros
			chunks.remove(chunk);
		}
		file.delete(); // apaga ficheiro
		files.remove(file); // apaga o ficheiro
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
					if (MDB.ignoreChunk == false) // ñ recebeu entretanto
													// PUTCHUNK com o mm fileID
													// / chunkNo
						new Thread() { // esperar por tempo máximo de putchunk

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
			// não tem o ficheiro (ignora)
		}
	}

	public static void putChunk(Chunk chunk) {
		try {
			int waitTime = putchunkDelay;
			Header header = new Header("PUTCHUNK", version, chunk.getFileID(),
					chunk.getChunkNo(), chunk.getReplicationDeg());
			Message message = new Message(header, chunk);
			for (int i = 0; i < 5; i++) // máx 5 retries
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
																			// não
																			// existe
		{
			if (usedSpace + chunk.getSize() <= maxSpace) { // se tiver espaço
															// para guardar
				chunks.add(chunk);
				chunk.write(chunkData, chunkData.length);
				Header header = new Header("STORED", version,
						chunk.getFileID(), chunk.getChunkNo(), null);
				Message message = new Message(header, null);
				try {
					Thread.sleep(Math.round(Math.random() * 400));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MC.send(message);
				chunk.incrementCurrentReplicationDeg();
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
				if (MDR.ignoreChunk == false) // ñ recebeu entretanto
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
		Iterator<Chunk> iterator = file.getChunks().iterator();
		Chunk chunk = null;
		while (iterator.hasNext()) {
			chunk = iterator.next();
			if (chunk.getFile() == null) {
				askChunk(chunk);
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
		Chunk chunkTemp = null;
		for (int i = 0; i < file.getChunks().size(); i++) {
			chunkTemp = file.getChunks().get(i);
			if (chunkTemp.getFileID().equals(chunk.getFileID())
					&& chunkTemp.getChunkNo() == chunk.getChunkNo()) {
				chunkTemp.write(chunkData, chunkData.length);
				chunks.add(chunkTemp);
			}
		}
		//set tiver os chunks todos, reconstroi ficheiro
		if(file.gotAllChunks())
			file.dechunker();
	}
}
