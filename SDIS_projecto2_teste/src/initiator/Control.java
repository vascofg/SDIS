package initiator;

import java.awt.Dimension;
import java.util.List;

import message.Message;

public class Control extends Thread {

	public void handleMessages(List<Message> messages) {
		for (Message message : messages) {
			switch (message.getType()) {
			// TODO: FAZER
			case Message.EDGE:
				System.out.println("GOT REMOTE EDGE: " + message.getEdge());
				Initiator.onEdge(message.getEdge());
				break;
			case Message.RESOLUTION:
				Dimension dim = message.getResolution();
				System.out.println("Width: " + dim.width);
				System.out.println("Height: " + dim.height);
				break;
			case Message.DISCONNECT:
				break;
			case Message.ALIVE:
				break;
			}
		}
	}
}
