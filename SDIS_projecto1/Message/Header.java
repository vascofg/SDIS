package Message;

public class Header {
	String messageType;
	String version;
	String fileId;
	Integer ChunkNo;
	Integer RepDeg;

	public Header(String messageType, String version, String fileId, Integer chunkNo, Integer repDeg) {
		this.messageType = messageType;
		this.version = version;
		this.fileId = fileId;
		ChunkNo = chunkNo;
		RepDeg = repDeg;
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
		return messageType + ' ' + version + ' ' + fileId + ' ' + ChunkNo + ' ' + RepDeg + '\r' + '\n';
	}
}
