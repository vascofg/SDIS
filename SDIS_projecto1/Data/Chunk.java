package Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Chunk {

	public static final int ChunkSize = 64000; // 64Kbytes

	public Chunk(String fileID, Integer chunkNo, Integer replicationDeg) {
		super();
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
	}

	String fileID;
	Integer chunkNo, replicationDeg, size;
	java.io.File file;

	public String getFileID() {
		return fileID;
	}

	public Integer getChunkNo() {
		return chunkNo;
	}

	public Integer getReplicationDeg() {
		return replicationDeg;
	}

	public long getSize() {
		return size;
	}

	public void write(byte[] data, int len) {
		file = new java.io.File(fileID+'/'+chunkNo);
		file.getParentFile().mkdir();
		try {
		
			FileOutputStream os = new FileOutputStream(file);
			os.write(data, 0, len);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public byte[] read() {
		byte[] data = null;
		try {
			FileInputStream is = new FileInputStream(file);
			data = new byte[size];
			is.read(data);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
}
