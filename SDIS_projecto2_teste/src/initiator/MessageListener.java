package initiator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedList;

import message.Message;

public class MessageListener extends Thread {

	private boolean go = true;

	public MessageListener() {

	}

	@Override
	public void run() {
		byte[] buf = new byte[256]; // TODO: definir tamanho máximo da mensagem
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		LinkedList<Message> eventMessages = new LinkedList<>();
		LinkedList<Message> controlMessages = new LinkedList<>();
		while (go) {
			try {
				Initiator.socket.receive(packet);
				Message.decodePacket(packet.getData(), packet.getLength(),
						eventMessages, controlMessages, packet.getAddress()); // não há separação (as duas
												// tratadas pelo event handler)
				if(eventMessages.size()>0)
				{
					System.out.println("Got event message! WTH?");
					eventMessages.clear();
				}
				Initiator.control.handleMessages(controlMessages);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
