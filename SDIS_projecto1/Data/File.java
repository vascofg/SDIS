package Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class File {
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

	public void chunker() {
		java.io.File file = new java.io.File(name);
		int i;
		Chunk chunk;
		try {
			FileInputStream is = new FileInputStream(file);
			long fileSize = file.length();
			long numChunks = (long) Math.ceil(fileSize / (float)Chunk.ChunkSize);
			byte[] data = new byte[Chunk.ChunkSize];
			int readBytes = 0;
			// gerar fileID
			for (i = 0; i < numChunks; i++) {
				readBytes = is.read(data);
				chunk = new Chunk(name + "DIR", i, replicationDeg);
				addChunk(chunk);
				chunk.write(data, readBytes);
			}
			if (readBytes == Chunk.ChunkSize) // save 0 byte chunk
			{
			}
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
