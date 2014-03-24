package Backup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
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

	public static List<File> files = new ArrayList<File>(); // ficheiros que
															// pertencem a este
															// peer

	public static List<Chunk> chunks = new ArrayList<Chunk>(); // chunks de
																// ficheiros de
																// outro peer

	public static void loadFiles() throws NullPointerException {
		java.io.File folder = new java.io.File("files/");
		FileInputStream fis;
		ObjectInputStream ois;
		for (final java.io.File fileEntry : folder.listFiles()) {
			try {
				fis = new FileInputStream(fileEntry);
				ois = new ObjectInputStream(fis);
				files.add((File) ois.readObject());
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static File getFileByID(String fileID) {
		for (int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			if (file.getFileID().equals(fileID))
				return file;
		}
		return null;
	}

	public static Chunk getChunkByID(List<Chunk> chunks, String fileID,
			int chunkNo) {
		for (int i = 0; i < chunks.size(); i++) {
			Chunk chunk = chunks.get(i);
			if (chunk.getChunkNo() == chunkNo
					&& chunk.getFileID().equals(fileID))
				return chunk;
		}
		return null;
	}

	public static Chunk getFileChunk(String fileID, int chunkNo) {
		File file = getFileByID(fileID);
		return getChunkByID(file.getChunks(), file.getFileID(), chunkNo);
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
						sendBackup(file);
					}
				}
				break;
			case "restore":
				int i;
				System.out.println("Choose what file to restore");
				for (i = 0; i < files.size(); i++) {
					System.out.println(i + ": " + files.get(i).getName());
				}
				int fileNo = sc.nextInt();
				if (fileNo >= i || fileNo < 0)
					throw new FileNotFoundException();
				else
					files.get(fileNo).dechunker();
				break;
			case "send":
				Message msg = new Message(new Header("PUTCHUNK", version,
						"Teste", 0, 1), null);
				MC.send(msg);
				break;
			case "teste":
				File file = new File("bolha.png", 1);
				for (i = 0; i < chunks.size(); i++) {
					file.addChunk(chunks.get(i));
				}
				file.setId(chunks.get(0).getFileID());
				file.dechunker();
				break;
			case "exit":
				sc.close();
				return;
			}
		}
	}

	public static void sendBackup(File file) {
		for (int i = 0; i < file.getChunks().size(); i++) {
			putChunk(file.getChunks().get(i));
			// TODO: esperar por stored e cenas
		}
	}

	public static void putChunk(Chunk chunk) {
		try {
			int waitTime = 500;
			Header header = new Header("PUTCHUNK", version, chunk.getFileID(),
					chunk.getChunkNo(), chunk.getReplicationDeg());
			Message message = new Message(header, chunk);
			for (int i = 0; i < 5; i++) // máx 5 retries
			{
				MDB.send(message);
				System.out.println("Waiting for stored messages...");
				Thread.sleep(waitTime);
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
		// TODO: verificar replication degree
		if (getChunkByID(chunks, chunk.getFileID(), chunk.getChunkNo()) == null) // ainda
																					// não
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
		}
	}
}
