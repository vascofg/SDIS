package initiator;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

public class EdgeDetectThread extends Thread {

	private boolean go = true, threadSuspended = false;
	Point currentPos;
	Point maxPos;

	public EdgeDetectThread() {
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
				Thread.sleep(100);
				synchronized(this) {
                    while (threadSuspended)
                        wait();
                }
			} catch (InterruptedException e) {
			}
			currentPos = MouseInfo.getPointerInfo().getLocation();
			if (currentPos.x == 0)
				System.out.println("EDGE DETECT: LEFT");
			else if (currentPos.x == maxPos.x) {
				System.out.println("EDGE DETECT: RIGHT");
				Teste.onEdge();
			}
			if (currentPos.y == 0)
				System.out.println("EDGE DETECT: TOP");
			else if (currentPos.y == maxPos.y)
				System.out.println("EDGE DETECT: BOTTOM");

		}
		System.out.println("Edge thread ending...");
	}
	
	@Override
	public void interrupt() {
		this.go = false;
	}
	
	public void pause(){
		this.threadSuspended = true;
	}
	
	public void unpause(){
		this.threadSuspended = false;
		notify();
	}
}
