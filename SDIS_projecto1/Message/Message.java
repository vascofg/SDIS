package Message;

import Data.Chunk;

public class Message {

	Header header=null;
	Chunk chunk=null;
	
	public Message(Header header, Chunk chunk) {
		this.header=header;
		this.chunk=chunk;
	}
}
