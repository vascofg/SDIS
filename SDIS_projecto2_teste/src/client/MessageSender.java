package client;

import interfaces.SendClipboardMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import message.Message;

public class MessageSender extends Thread implements SendClipboardMessage {
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
				messageList.add(messageQueue.take()); // espera até ter algum
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
					// ainda não ligado a nenhum peer (não faz nada)
				}
				messageList.clear();

			} catch (InterruptedException e) {
			}
		}
	}

	public void addMessage(Message message) {
		messageQueue.offer(message); // não espera para inserir
	}

	// sends message to specific monitor immediately
	public void sendMessage(byte[] msgBytes, InetAddress addr) {
		DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length,
				addr, Client.port);
		try {
			Client.socket.send(packet);
		} catch (IOException e) {
		}
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		super.interrupt();
	}

	@Override
	public void sendClipboardMessage(Message message) {
		addMessage(message); // send to initiator
	}
}