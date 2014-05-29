package initiator;

import gui.MainGUI;

import java.awt.Dimension;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeoutException;

import message.Message;

public class Control extends Thread {

	private boolean go = true, threadSuspended = false;
	static boolean connected = false;
	static final int numRetries = 3;
	static int retries;
	static final int retryDelay = 500;

	@Override
	public void run() {
		while (go) {
			try {
				synchronized (this) {
					while (threadSuspended)
						wait();
				}

				if (retries-- == 0)
					throw new TimeoutException();

				if (!connected)
					Initiator.messageSender.addMessage(new Message(
							Message.CONNECT));
				else
					Initiator.messageSender.addMessage(new Message(
							Message.ALIVE));

				Thread.sleep(retryDelay);
			} catch (InterruptedException e) {
			} catch (TimeoutException e) {
				Initiator.timeout();
			}

		}
	}

	public synchronized void newConnection() {
		connected = false;
		retries = numRetries; // reset tries
		unpause(); // continue thread
	}

	public void handleMessages(List<Message> messages) {
		for (Message message : messages) {
			byte msgType = message.getType();
			// reject messages which do not come from the currentMonitor (except
			// CLIPBOARD_HAVE)
			if (msgType != Message.CLIPBOARD_HAVE
					&& !message.getRemoteAddress().equals(
							Initiator.currentMonitor))
				continue;
			switch (msgType) {
			case Message.EDGE:
				Initiator.onEdge(message.getEdge());
				break;
			case Message.RESOLUTION:
				if (!threadSuspended) {
					connected = true;
					Initiator.connected();
				}
				Dimension dim = message.getResolution();
				System.out.println("Width: " + dim.width);
				System.out.println("Height: " + dim.height);
				break;
			case Message.DISCONNECT:
				break;
			case Message.ALIVE:
				retries = numRetries; // reset retries
				break;
			case Message.CLIPBOARD_HAVE:
				InetAddress remoteAddr = message.getRemoteAddress();
				byte contentType = message.getContentType();
				Initiator.messageSender.sendMessageToAllOthers(
						Message.announceClipboard(contentType, remoteAddr),
						MainGUI.ls, remoteAddr);
				Initiator.fileListener.hostAddress = remoteAddr;
				Initiator.statusGUI
						.setClipboardContent(contentType, remoteAddr);
				Initiator.fileListener.availableContentType = contentType;
				Initiator.statusGUI.getClipboard.setEnabled(true);
				break;
			}
		}
	}

	public void disconnectAll() {
		Message msg = new Message(Message.DISCONNECT);
		Initiator.messageSender.sendMessageToAll(msg, MainGUI.ls);
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		super.interrupt();
	}

	public synchronized void pause() {
		this.threadSuspended = true;
	}

	public synchronized void unpause() {
		this.threadSuspended = false;
		notify();
	}
}
