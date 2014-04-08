package initiator;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MouseDeltaThread extends Thread {
	private boolean go = true, update = false, threadSuspended = false;
	private int deltaX, deltaY, port;
	private DatagramSocket socket;
	private InetAddress address;

	public MouseDeltaThread(DatagramSocket socket, InetAddress address, int port) {
		this.socket = socket;
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		super.run();
		String msg;
		DatagramPacket packet;
		while (go) {
			try {
				Thread.sleep(50);
				
				synchronized(this) {
                    while (threadSuspended || !update)
                        wait();
				}
			} catch (InterruptedException e) {
			}
			update = false;
			msg = "move " + deltaX + " " + deltaY;
			packet = new DatagramPacket(msg.getBytes(), msg.length(),
					address, port);
			try {
				socket.send(packet);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			deltaX = deltaY = 0;
		}
		System.out.println("Mouse thread ending...");
	}

	public synchronized void setPos(int x, int y) {
		this.deltaX += x;
		this.deltaY += y;
		update = true;
		notify();
	}

	public void setAddrPort(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public synchronized void interrupt() {
		this.go = false;
	}
	
	public synchronized void pause(){
		this.threadSuspended = true;
	}
	
	public synchronized void unpause(){
		this.threadSuspended = false;
		notify();
	}
}
