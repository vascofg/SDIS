package client;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Receiver {
	static DatagramSocket socket;
	static DatagramPacket packet;
	static Robot r;
	static final int port = 44444;

	public static void main(String[] args) {
		try {
			socket = new DatagramSocket(port);
			r = new Robot();
		} catch (SocketException | AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] buf = new byte[256];
		packet = new DatagramPacket(buf, buf.length);
		int arg1, arg2;
		Point currentPos;
		String msg;
		String data[];
		while (true) {
			try {
				socket.receive(packet);
				msg = new String(packet.getData(), 0, packet.getLength());

				data = msg.split(" ");

				arg1 = Integer.parseInt(data[1]);

				switch (data[0]) {
				case "press":
					System.out.println("Got press mouse");
					r.mousePress(arg1);
					break;
				case "release":
					System.out.println("Got release mouse");
					r.mouseRelease(arg1);
					break;
				case "move":
					arg2 = Integer.parseInt(data[2]);
					currentPos = MouseInfo.getPointerInfo().getLocation();
					// System.out.println("Got move mouse");
					r.mouseMove(currentPos.x + arg1, currentPos.y + arg2);
					break;
				case "presskey":
					System.out.println("Got press key "
							+ KeyEvent.getKeyText(arg1));
					r.keyPress(arg1);
					break;
				case "releasekey":
					System.out.println("Got release key "
							+ KeyEvent.getKeyText(arg1));
					r.keyRelease(arg1);
					break;
				case "rotate":
					System.out.println("Got rotate wheel " + arg1 + " notches");
					r.mouseWheel(arg1);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void onEdge(byte edge) {
		// TODO Auto-generated method stub
		
	}
}
