package Message;

import java.io.UnsupportedEncodingException;

public class Header {
	String messageType;
	String version;
	String fileId;
	Integer ChunkNo;
	Integer RepDeg;

	public Header(String messageType, String version, String fileId,
			Integer chunkNo, Integer repDeg) {
		this.messageType = messageType;
		this.version = version;
		this.fileId = fileId;
		this.ChunkNo = chunkNo;
		this.RepDeg = repDeg;
	}
	
	public Header(byte[] headerBytes)
	{
		try {
			String header = new String(headerBytes, "UTF-8");
			String data[] = header.split(" ");
			this.messageType = data[0];
			this.version = data[1];
			this.fileId = data[2];
			this.ChunkNo = Integer.parseInt(data[3]);
			this.RepDeg = Integer.parseInt(data[4]);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public Integer getChunkNo() {
		return ChunkNo;
	}

	public void setChunkNo(Integer chunkNo) {
		ChunkNo = chunkNo;
	}

	public Integer getRepDeg() {
		return RepDeg;
	}

	public void setRepDeg(Integer repDeg) {
		RepDeg = repDeg;
	}

	@Override
	public String toString() {
		return messageType + ' ' + version + ' ' + fileId + ' ' + ChunkNo + ' '
				+ RepDeg + '\r' + '\n' + '\r' + '\n';
	}

	public byte[] getBytes() {
		try {
			return toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
