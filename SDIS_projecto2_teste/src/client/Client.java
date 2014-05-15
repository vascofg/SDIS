package client;

import client.EdgeDetect;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import message.Message;

public class Client {
	static DatagramSocket socket;
	static DatagramPacket packet;
	static InetAddress initiatorAddress;
	static Robot r;
	static final int port = 44444;
	
	static Point screenCenter;

	static EdgeDetect edgeDetect;
	static EventHandler eventHandler;
	static MessageListener messageListener;
	static MessageSender messageSender;

	static short messageDelay = 25; // delay to send messages (in milliseconds)

	public static void main(String[] args) {
		try {
			socket = new DatagramSocket(port);
			r = new Robot();
			
			Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
			screenCenter = new Point(screenRes.width/2, screenRes.height/2);

			eventHandler = new EventHandler();
			eventHandler.start();
			
			edgeDetect = new EdgeDetect();
			edgeDetect.pause();
			edgeDetect.start(); //start paused

			messageListener = new MessageListener();
			messageListener.start();

			messageSender = new MessageSender();
			messageSender.start();
		} catch (SocketException | AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	static void onEdge(byte edge) {
		edgeDetect.pause();
		messageSender.addMessage(new Message(Message.EDGE, edge));
	}
}
