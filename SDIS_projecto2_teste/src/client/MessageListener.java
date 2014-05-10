package client;

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
		byte[] buf = new byte[256]; // TODO: definir tamanho m�ximo da mensagem
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		LinkedList<Message> messages = new LinkedList<>();
		while (go) {
			try {
				Client.socket.receive(packet);
				Message.decodePacket(packet.getData(), packet.getLength(),
						messages, messages, packet.getAddress()); // n�o h� separa��o (as duas
												// tratadas pelo event handler)
				Client.eventHandler.addMessages(messages);
				messages.clear();
				//TODO: solu��o um bocado m�?
				Client.edgeDetect.unpause();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
