package client;

import java.awt.MouseInfo;
import java.awt.Point;
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
				case Message.CONNECT:
					Client.initiatorAddress = msg.getAddress();
					Client.messageSender.addMessage(new Message(
							Message.RESOLUTION, null));
					break;
				case Message.DISCONNECT:
					// TODO: mandar saír
					break;
				case Message.ALIVE:
					Client.messageSender
							.addMessage(new Message(Message.ALIVE, null));
					break;
				default:
					System.out.println("Got unexpected message: " + msg.getType());
				}
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void interrupt() {
		this.go = false;
		super.interrupt();
	}
}