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
import Channel.MulticastControl;
import Channel.MulticastDataBackup;
import Channel.MulticastDataRecover;

public final class Backup {

	private static final String MCport = "50001";
	public static final String MCgroup = "239.254.254.252";
	private static final String MDBport = "50001";
	public static final String MDBgroup = "239.254.254.253";
	private static final String MDRport = "50001";
	public static final String MDRgroup = "239.254.254.254";
	public static final String version = "1.0";
	public static MulticastControl MC = new MulticastControl(MCgroup, MCport);
	public static MulticastDataBackup MDB = new MulticastDataBackup(MDBgroup, MDBport);
	public static MulticastDataRecover MDR = new MulticastDataRecover(MDRgroup, MDRport);
	public static List<File> files = new ArrayList<File>();

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

	public static Boolean fileAlreadyExists(String fileID) {
		for (int i = 0; i < files.size(); i++)
			if (files.get(i).getId().equals(fileID))
				return true;
		return false;
	}

	public static void main(String[] args) throws IOException {
		String cmd;
		Scanner sc = new Scanner(System.in);
		String data[];
		
		MC.start();

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
					if (!fileAlreadyExists(file.getFileID())) {
						file.chunker();
						files.add(file);
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
			case "exit":
				sc.close();
				return;
			}
		}
	}
	
	public void putChunk(Chunk chunk)
	{
		Header header = new Header("PUTCHUNK", version, chunk.getFileID(), chunk.getChunkNo(), chunk.getReplicationDeg());
		Message message = new Message(header, chunk);
		MDB.send(message);
	}
}
