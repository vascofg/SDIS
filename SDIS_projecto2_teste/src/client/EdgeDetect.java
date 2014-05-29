package client;

import java.awt.MouseInfo;
import java.awt.Point;

public class EdgeDetect extends Thread {

	private boolean go = true, threadSuspended = false;
	private Point currentPos;
	private Point maxPos;

	public static final byte EDGE_LEFT = 1;
	public static final byte EDGE_RIGHT = 2;
	public static final byte EDGE_TOP = 3;
	public static final byte EDGE_BOTTOM = 4;

	public EdgeDetect() {
		maxPos = new Point(Client.screenRes.width - 1, Client.screenRes.height - 1); // position
																			// is
																			// 0
																			// based,
																			// size
																			// is
																			// 1
																			// based
	}

	@Override
	public void run() {
		super.run();
		while (go) {
			try {
				synchronized (this) {
					while (threadSuspended)
						wait();
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			currentPos = MouseInfo.getPointerInfo().getLocation();
			if (currentPos.x <= 0) {
				int percentage = currentPos.y * 100 / maxPos.y;
				Client.onEdge(EDGE_LEFT, percentage);
			} else if (currentPos.x >= maxPos.x) {
				int percentage = currentPos.y * 100 / maxPos.y;
				Client.onEdge(EDGE_RIGHT, percentage);
			} else if (currentPos.y <= 0) {
				int percentage = currentPos.x * 100 / maxPos.x;
				Client.onEdge(EDGE_TOP, percentage);
			} else if (currentPos.y >= maxPos.y) {
				int percentage = currentPos.x * 100 / maxPos.x;
				Client.onEdge(EDGE_BOTTOM, percentage);
			}

		}
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
		unpause();
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
