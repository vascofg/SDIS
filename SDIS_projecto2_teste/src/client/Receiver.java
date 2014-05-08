package client;

import java.awt.AWTException;
import java.awt.Robot;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver {
	static DatagramSocket socket;
	static DatagramPacket packet;
	static InetAddress initiatorAddress;
	static Robot r;
	static final int port = 44444;
	
	static EdgeDetect edgeDetect;
	static EventHandler eventHandler;
	static MessageListener messageListener;
	static MessageSender messageSender;

	public static void main(String[] args) {
		try {
			socket = new DatagramSocket(port);
			r = new Robot();
			
			eventHandler = new EventHandler();
			eventHandler.start();
			
			messageListener = new MessageListener();
			messageListener.start();
			
			messageSender = new MessageSender();
			messageSender.start();
		} catch (SocketException | AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void onEdge(byte edge) {
		// TODO Auto-generated method stub
	}
}
