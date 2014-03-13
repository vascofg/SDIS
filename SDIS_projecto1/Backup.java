import java.io.IOException;

import Data.File;


public class Backup {
	public static void main(String[] args) throws IOException
	{
		File file = new File("2048_score.png", 1);
		file.chunker();
		System.in.read();
		file.dechunker();
	}
}
