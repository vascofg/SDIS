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
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		LinkedList<Message> messages = new LinkedList<>();
		while (go) {
			try {
				Client.socket.receive(packet);
				Message.decodePacket(packet.getData(), packet.getLength(),
						messages, messages, packet.getAddress()); // não há
																	// separação
																	// (as duas
				// tratadas pelo event handler)
				Client.eventHandler.addMessages(messages);
				messages.clear();
			} catch (IOException e) {
				// socket closed (do nothing)
			}
		}
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		super.interrupt();
		Client.socket.close();
	}
}
