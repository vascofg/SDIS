package initiator;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import message.Message;
import monitor.Monitor;

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
							Message.CONNECT, null));
				else
					Initiator.messageSender.addMessage(new Message(
							Message.ALIVE, null));

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
			switch (message.getType()) {
			// TODO: FAZER
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
				retries = numRetries; //reset retries
				break;
			}
		}
	}
	
	public void disconnectAll(Collection<Monitor> monitors) {
		Message msg = new Message(Message.DISCONNECT, null);
		for (Monitor mon : monitors)
			if (mon.getIp() != null)
				Initiator.messageSender.sendMessage(msg, mon);
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
