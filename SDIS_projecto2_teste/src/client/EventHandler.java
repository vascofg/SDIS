package client;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import initiator.Message;

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
					Receiver.r.mouseMove(currentPos.x + mouseDelta.x,
							currentPos.y + mouseDelta.y);
					break;
				case Message.MOUSE_PRESS:
					argument = msg.getMouseButtons();
					Receiver.r.mousePress(argument);
					break;
				case Message.MOUSE_RELEASE:
					argument = msg.getMouseButtons();
					Receiver.r.mouseRelease(argument);
					break;
				case Message.MOUSE_SCROLL:
					argument = msg.getMouseScroll();
					Receiver.r.mouseWheel(argument);
					break;
				case Message.KEY_PRESS:
					argument = msg.getKeyCode();
					Receiver.r.keyPress(argument);
					break;
				case Message.KEY_RELEASE:
					argument = msg.getKeyCode();
					Receiver.r.keyRelease(argument);
					break;
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