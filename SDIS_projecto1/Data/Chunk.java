package Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import Backup.Backup;

public class Chunk implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int ChunkSize = 64000; // 64Kbytes

	public Chunk(String fileID, Integer chunkNo, Integer replicationDeg,
			int size) {
		super();
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
		this.size = size;
		this.currentReplicationDeg = 0;
	}

	String fileID;
	Integer chunkNo, replicationDeg, size, currentReplicationDeg;
	java.io.File file;

	public java.io.File getFile() {
		return file;
	}

	public void setFile(java.io.File file) {
		this.file = file;
	}

	public Integer getCurrentReplicationDeg() {
		return currentReplicationDeg;
	}

	public void setCurrentReplicationDeg(Integer currentReplicationDeg) {
		this.currentReplicationDeg = currentReplicationDeg;
	}

	public void incrementCurrentReplicationDeg() {
		this.currentReplicationDeg++;
	}

	public void decrementCurrentReplicationDeg() {
		this.currentReplicationDeg--;
	}

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

	public void delete() {
		if(file==null)
			return; //chunk já apagado
		file.delete();
		file = null;
		Backup.usedSpace-=size;
		java.io.File serializeFile = new java.io.File("chunks/" + fileID + '/' + chunkNo + ".ser");
		serializeFile.delete();
	}

	public void write(byte[] data, int len) {
		file = new java.io.File("chunks/" + fileID + '/' + chunkNo);
		Backup.usedSpace+=size;
		file.getParentFile().mkdirs();
		try {

			FileOutputStream os = new FileOutputStream(file);
			os.write(data, 0, len);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.serialize(); // serializa chunk
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

	public void serialize() {
		try {
			java.io.File file = new java.io.File("chunks/" + fileID + '/'
					+ chunkNo + ".ser"); // file id
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
