package Message;

import Data.Chunk;

public class Message {

	public Header getHeader() {
		return header;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public byte[] getChunkData() {
		return chunkData;
	}

	Header header;
	Chunk chunk;
	byte[] chunkData;

	public Message(Header header, Chunk chunk) {
		this.header = header;
		this.chunk = chunk;
	}
	
	public Message(byte[] messageBytes, int length) throws Exception {
		int i;
		String string = new String(messageBytes, 0, length, "UTF-8");
		for(i=0;i<=length-4;i++)
		{
			if(messageBytes[i]=='\r' && messageBytes[i+1]=='\n')
			{
				if(messageBytes[i+2]=='\r' && messageBytes[i+3]=='\n')
				{
					byte[] headerBytes = new byte[i];
					System.arraycopy(messageBytes, 0, headerBytes, 0, i);
					this.header = new Header(headerBytes);
					i+=4; //primeiro byte do chunk
					if(i != length) //mensagem com body
					{
						this.chunkData = new byte[length-i];
						System.arraycopy(messageBytes, i, this.chunkData, 0, length-i);
						this.chunk = new Chunk(header.fileId, header.ChunkNo, header.RepDeg);
					}
					return;
				}
			}
		}
		throw new Exception("Control flags not found");
	}

	public byte[] getBytes() { //concatenar header e body para envio
		byte[] headerBytes = header.getBytes();
		byte[] bodyBytes;
		try {
			bodyBytes = chunk.read();
		} catch (NullPointerException e) { //mensagem de controlo
			bodyBytes = new byte[0];
		}
		byte[] messageBytes = new byte[headerBytes.length + bodyBytes.length];

		System.arraycopy(header.getBytes(), 0, messageBytes, 0,
				header.getBytes().length);
		System.arraycopy(bodyBytes, 0, messageBytes, headerBytes.length,
				bodyBytes.length);

		return messageBytes;
	}
}
