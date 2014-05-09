package initiator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import message.Message;

public class MessageSender extends Thread {
	private boolean go = true;
	private LinkedBlockingQueue<Message> messageQueue;

	public MessageSender() {
		messageQueue = new LinkedBlockingQueue<>();
	}

	@Override
	public void run() {
		List<Message> messageList = new LinkedList<>();
		byte[] bytes;
		DatagramPacket packet;
		while (go) {
			try {
				Thread.sleep(Initiator.messageDelay);
				messageList.add(messageQueue.take()); // espera até ter algum
														// elemento
				messageQueue.drainTo(messageList); // retira os restantes
													// elementos
				try {
					messageList.add(new Message(Initiator.eventHandler.getMouseDelta()));
				} catch (Exception e) {
				} // adiciona movimento do rato (se for 0, atira excepção e não
					// faz nada)

				bytes = Message.getPacket(messageList);
				packet = new DatagramPacket(bytes, bytes.length, Initiator.address,
						Initiator.port);
				try {
					Initiator.socket.send(packet);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				messageList.clear();

			} catch (InterruptedException e) {
			}
		}
	}

	public void addMessage(Message message) {
		messageQueue.offer(message); // não espera para inserir
	}
}
