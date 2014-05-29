package client;

import java.awt.MouseInfo;
import java.awt.Point;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import message.Message;

public class EventHandler extends Thread {
	private boolean go = true;
	private LinkedBlockingQueue<Message> messageQueue;

	public EventHandler() {
		messageQueue = new LinkedBlockingQueue<>();
	}

	void addMessage(Message msg) {
		messageQueue.offer(msg); // não espera para inserir
	}

	void addMessages(Collection<Message> msgs) {
		messageQueue.addAll(msgs);
	}

	@Override
	public void run() {
		Message msg;
		Point mouseDelta, currentPos;
		int argument;
		InetAddress remoteAddr;
		while (go) {
			try {
				msg = messageQueue.take();
				switch (msg.getType()) {
				case Message.MOUSE_MOVE:
					mouseDelta = msg.getMouseDelta();
					currentPos = MouseInfo.getPointerInfo().getLocation();
					Client.r.mouseMove(currentPos.x + mouseDelta.x,
							currentPos.y + mouseDelta.y);
					break;
				case Message.MOUSE_PRESS:
					argument = msg.getMouseButtons();
					Client.r.mousePress(argument);
					break;
				case Message.MOUSE_RELEASE:
					argument = msg.getMouseButtons();
					Client.r.mouseRelease(argument);
					break;
				case Message.MOUSE_SCROLL:
					argument = msg.getMouseScroll();
					Client.r.mouseWheel(argument);
					break;
				case Message.KEY_PRESS:
					argument = msg.getKeyCode();
					Client.r.keyPress(argument);
					break;
				case Message.KEY_RELEASE:
					argument = msg.getKeyCode();
					Client.r.keyRelease(argument);
					break;
				// TODO: quando ligar, passar ponto do ecrã para dar ideia de
				// continuidade (ponto/res*100)
				case Message.CONNECT:
					Client.initiatorAddress = msg.getRemoteAddress();
					Client.messageSender.addMessage(Message.resolution());
					Client.joinScreen();
					break;
				case Message.DISCONNECT:
					Client.exit();
					break;
				case Message.ALIVE:
					Client.messageSender.addMessage(new Message(Message.ALIVE));
					break;
				case Message.CLIPBOARD_ANNOUNCE:
					remoteAddr = msg.getAddress();
					Client.fileListener.hostAddress = remoteAddr;
					Client.statusGUI.setClipboardContent(msg.getContentType(),
							remoteAddr);
					Client.fileListener.availableContentType = msg
							.getContentType();
					Client.statusGUI.getClipboard.setEnabled(true);
					break;
				case Message.CLIPBOARD_HAVE:
					remoteAddr = msg.getRemoteAddress();
					Client.fileListener.hostAddress = remoteAddr;
					Client.statusGUI.setClipboardContent(msg.getContentType(),
							remoteAddr);
					Client.fileListener.availableContentType = msg
							.getContentType();
					Client.statusGUI.getClipboard.setEnabled(true);
					break;
				case Message.LEAVE:
					Client.leaveScreen();
					break;
				default:
					System.out.println("Got unexpected message: "
							+ msg.getType());
				}
			} catch (InterruptedException e) {
			} catch (UnknownHostException e) {
				System.out.println("Unknown host getting clipboard announcer");
			}
		}
	}

	@Override
	public void interrupt() {
		this.go = false;
		super.interrupt();
	}
}