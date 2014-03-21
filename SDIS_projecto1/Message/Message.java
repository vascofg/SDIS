package Message;

import Data.File;

public class Message {

	Header header=null;
	File file=null;
	
	public Message(Header header, File file) {
		this.header=header;
		this.file=file;
	}
	
	public void send(/*ip port or somth*/){
		//TODO corpo desta merda
	}
	
	public void receive(){
		//TODO mesma merda
	}
	
	public void construct(String type){
		
	}
}
