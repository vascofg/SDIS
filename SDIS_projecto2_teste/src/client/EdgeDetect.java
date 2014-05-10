package client;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

public class EdgeDetect extends Thread {

	private boolean go = true, threadSuspended = false;
	private Point currentPos;
	private Point maxPos;

	public static final byte EDGE_LEFT = 1;
	public static final byte EDGE_RIGHT = 2;
	public static final byte EDGE_TOP = 3;
	public static final byte EDGE_BOTTOM = 4;

	public EdgeDetect() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		maxPos = new Point(screenSize.width - 1, screenSize.height - 1); // position
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
			if (currentPos.x == 0) {
				System.out.println("EDGE DETECT: LEFT");
				//move one pixel off the edge (avoid leaving screen instantly when connected)
				Client.r.mouseMove(1, currentPos.y);
				Client.onEdge(EDGE_LEFT);
			} else if (currentPos.x == maxPos.x) {
				System.out.println("EDGE DETECT: RIGHT");
				Client.r.mouseMove(currentPos.x-1, currentPos.y);
				Client.onEdge(EDGE_RIGHT);
			}
			if (currentPos.y == 0) {
				System.out.println("EDGE DETECT: TOP");
				Client.r.mouseMove(currentPos.x, 1);
				Client.onEdge(EDGE_TOP);
			} else if (currentPos.y == maxPos.y) {
				System.out.println("EDGE DETECT: BOTTOM");
				Client.r.mouseMove(currentPos.x, currentPos.y-1);
				Client.onEdge(EDGE_BOTTOM);
			}

		}
		System.out.println("Edge thread ending...");
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
	}

	public synchronized void pause() {
		this.threadSuspended = true;
	}

	public synchronized void unpause() {
		this.threadSuspended = false;
		notify();
	}
}
