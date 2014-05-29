package initiator;

import gui.MainGUI;
import interfaces.SendClipboardMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import message.Message;
import monitor.Monitor;

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
				Thread.sleep(Initiator.messageDelay);
				messageList.add(messageQueue.take()); // espera até ter algum
														// elemento
				messageQueue.drainTo(messageList); // retira os restantes
													// elementos
				try {
					messageList.add(Message.mouseDelta(Initiator.eventHandler
							.getMouseDelta()));
				} catch (InputMismatchException e) {
				} // adiciona movimento do rato (se for 0, atira excepção e não
					// faz nada)

				bytes = Message.getPacket(messageList);
				packet = new DatagramPacket(bytes, bytes.length,
						Initiator.currentMonitor.getIp(), Initiator.port);
				try {
					Initiator.socket.send(packet);
				} catch (IOException e1) {
					// socket closed do nothing
				} catch (NullPointerException e) // entretanto desligou:
													// descarta
				{
					// TODO: é preciso fazer alguma coisa aqui?
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
	public void sendMessage(byte[] msgBytes, Monitor monitor) {
		DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length,
				monitor.getIp(), Initiator.port);
		try {
			Initiator.socket.send(packet);
		} catch (IOException e) {
		}
	}

	// send a message to all peers
	public void sendMessageToAll(Message message, Collection<Monitor> monitors) {
		byte[] msgBytes = message.getBytes();
		for (Monitor mon : monitors)
			if (mon.getIp() != null)
				sendMessage(msgBytes, mon);
	}

	// send message to all except one peer
	public void sendMessageToAllOthers(Message message,
			Collection<Monitor> monitors, InetAddress except) {
		byte[] msgBytes = message.getBytes();
		for (Monitor mon : monitors)
			if (mon.getIp() != null && !mon.getIp().equals(except))
				sendMessage(msgBytes, mon);
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		super.interrupt();
	}

	// send to all peers
	@Override
	public void sendClipboardMessage(Message message) {
		sendMessageToAll(message, MainGUI.ls);
	}
}
