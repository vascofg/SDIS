import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import Data.File;

public final class Backup {

	public static List<File> files = new ArrayList<File>();

	public static void loadFiles() throws NullPointerException{
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

	public static void main(String[] args) throws IOException {
		try {
			loadFiles();
		} catch (NullPointerException n) {
			System.out.println("No files to load");
		}
		//File file = new File("pinguim.jpg", 1);
		//file.chunker();
		//System.in.read();
		files.get(0).dechunker();
	}
}
