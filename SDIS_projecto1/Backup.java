import Data.File;


public class Backup {
	public static void main(String[] args)
	{
		File file = new File("lol.txt", 1);
		file.chunker();
	}
}
