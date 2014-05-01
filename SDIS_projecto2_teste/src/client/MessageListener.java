package client;

import initiator.Message;

import java.io.IOException;
import java.net.DatagramPacket;

public class MessageListener extends Thread {

	private boolean go = true;

	public MessageListener() {

	}

	@Override
	public void run() {
		byte[] buf = new byte[256]; //TODO: definir tamanho máximo da mensagem
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		while (go) {
			try {
				Receiver.socket.receive(packet);
				Receiver.eventHandler.addMessages(Message.decodePacket(
						packet.getData(), packet.getLength()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
