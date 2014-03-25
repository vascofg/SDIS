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
	private static final String MDBport = "50001";
	public static final String MDBgroup = "239.254.254.253";
	private static final String MDRport = "50001";
	public static final String MDRgroup = "239.254.254.254";
	public static final String version = "1.0";
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
				addFileChunksToChunkArray(file);
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
																	// n�o
																	// existe
																	// (chunk
																	// remoto)
							chunks.add(chunk);
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
					selectFile(sc).dechunker();
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
			case "reclaim":
				// TODO: reclaim autom�tico
				// TODO: decis�o do chunk a apagar
				Chunk chunk = chunks.get(0);
				reclaim(chunk);
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

	public static void addFileChunksToChunkArray(File file) {
		for (int i = 0; i < file.getChunks().size(); i++)
			chunks.add(file.getChunks().get(i));
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
		// TODO: mandar v�rias vezes para confirmar que � apagado (maybe)
		chunks.removeAll(file.getChunks()); // apaga todos os chunks do ficheiro
											// da lista de chunks
		file.getChunks().get(0).deleteFileChunks(); // apaga chunks
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
				iterator.remove();
			}
		}
		chunk.deleteFileChunks();

		System.out.println("Deleted " + fileID);
	}

	public static void sendBackup(File file) {
		for (int i = 0; i < file.getChunks().size(); i++)
			putChunk(file.getChunks().get(i));
	}

	public static void reclaim(Chunk chunk) {
		chunk.delete();
		Header header = new Header("REMOVED", version, chunk.getFileID(),
				chunk.getChunkNo(), null);
		Message message = new Message(header, null);
		MDB.ignoreFileID = chunk.getFileID();
		MDB.ignoreChunkNo = chunk.getChunkNo();
		//TODO: SE MANDAR SEGUNDO RECLAIM, FODEU!
		MC.send(message);
		new Thread() { // esperar por tempo m�ximo de putchunk
			@Override
			public void run() {
				try {
					Thread.sleep(2 ^ 5 * putchunkDelay - putchunkDelay);
					MDB.ignoreFileID = null;
					MDB.ignoreChunkNo = null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
		chunks.remove(chunk);
	}

	public static void removed(Chunk chunk) {

		try {
			if ((chunk = getChunkByID(chunk.getFileID(), chunk.getChunkNo())) != null) // se
																						// tem
																						// o
																						// chunk
			{
				chunk.decrementCurrentReplicationDeg();
				if (chunk.getCurrentReplicationDeg() < chunk
						.getReplicationDeg()) {
					MDB.ignoreChunk = false;
					MDB.ignoreChunkNo = chunk.getChunkNo();
					MDB.ignoreFileID = chunk.getFileID();
					Thread.sleep(Math.round(Math.random() * 400));
					if (MDB.ignoreChunk == false) // � recebeu entretanto
													// PUTCHUNK com o mm fileID
													// / chunkNo
						putChunk(chunk);
					MDB.ignoreChunk = null;
					MDB.ignoreChunkNo = null;
					MDB.ignoreFileID = null;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void putChunk(Chunk chunk) {
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
			chunks.add(chunk);
			chunk.write(chunkData, chunkData.length);
			Header header = new Header("STORED", version, chunk.getFileID(),
					chunk.getChunkNo(), null);
			Message message = new Message(header, null);
			try {
				Thread.sleep(Math.round(Math.random() * 400));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MC.send(message);
			chunk.incrementCurrentReplicationDeg();
		}
	}
}
