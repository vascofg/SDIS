package Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class File implements Serializable {
	
	//for tests only
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 */
	private static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	private static final long serialVersionUID = 1L;

	public File(String name, Integer replicationDeg) {
		super();
		this.name = name;
		this.replicationDeg = replicationDeg;
		chunks = new ArrayList<Chunk>();
	}

	String name, id;
	List<Chunk> chunks;
	Integer replicationDeg;

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public List<Chunk> getChunks() {
		return chunks;
	}

	public void addChunk(Chunk chunk) {
		chunks.add(chunk);
	}

	public Integer getReplicationDeg() {
		return replicationDeg;
	}

	public String getFileID() {
		try {
			java.io.File file = new java.io.File(name);
			FileInputStream is = new FileInputStream(file);
			byte[] data = new byte[Chunk.ChunkSize];
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			String toHash = name + file.lastModified();
			sha.update(toHash.getBytes());
			is.read(data); // data to hash
			sha.update(data);
			is.close();
			return byteArrayToHexString(sha.digest());
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void chunker() {
		java.io.File file = new java.io.File(name);
		int i;
		Chunk chunk;
		try {
			FileInputStream is = new FileInputStream(file);
			long fileSize = file.length();
			long numChunks = (long) Math.ceil(fileSize
					/ (float) Chunk.ChunkSize);
			byte[] data = new byte[Chunk.ChunkSize];
			int readBytes = 0;

			this.id = getFileID();
			System.out.println(this.id);
			is.close();
			is = new FileInputStream(file); // rewind

			for (i = 0; i < numChunks; i++) {
				readBytes = is.read(data);
				chunk = new Chunk(id, i, replicationDeg, readBytes);
				addChunk(chunk);
				chunk.write(data, readBytes);
			}
			if (readBytes == Chunk.ChunkSize) // save 0 byte chunk
			{
				chunk = new Chunk(id, i, replicationDeg, 0);
				addChunk(chunk);
				chunk.write(data, 0);
			}
			is.close();
			serialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dechunker() {
		// TODO: Verificar se temos todos os chunks e obter chunks em falta
		java.io.File file = new java.io.File(name);
		try {
			if (file.createNewFile()) // ficheiro n�o existe
			{
				java.io.File chunk;
				FileInputStream is = null;
				FileOutputStream os = new FileOutputStream(file);
				byte[] chunkData = new byte[Chunk.ChunkSize];
				for (int i = 0; i < this.chunks.size(); i++) {
					chunk = new java.io.File("chunks/" + id + '/' + i);
					is = new FileInputStream(chunk);
					int bytesRead = is.read(chunkData);
					if (bytesRead == -1) // último chunk
						break;
					os.write(chunkData, 0, bytesRead);
				}
				is.close();
				os.close();
			} else
				System.out.println("FILE ALREADY EXISTS!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void serialize() {
		try {
			java.io.File file = new java.io.File("files/" + id); // file id
			file.getParentFile().mkdir();
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(this);
			oos.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
