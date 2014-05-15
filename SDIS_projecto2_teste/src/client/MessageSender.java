package client;

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
				Thread.sleep(Client.messageDelay);
				messageList.add(messageQueue.take()); // espera at� ter algum
														// elemento
				messageQueue.drainTo(messageList); // retira os restantes
													// elementos

				bytes = Message.getPacket(messageList);
				packet = new DatagramPacket(bytes, bytes.length,
						Client.initiatorAddress, Client.port);
				try {
					Client.socket.send(packet);
				} catch (IOException e1) {
					// socket closed (do nothing)
				} catch (NullPointerException e) {
					// ainda n�o ligado a nenhum peer (n�o faz nada)
				}
				messageList.clear();

			} catch (InterruptedException e) {
			}
		}
	}

	public void addMessage(Message message) {
		messageQueue.offer(message); // n�o espera para inserir
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		super.interrupt();
	}
}