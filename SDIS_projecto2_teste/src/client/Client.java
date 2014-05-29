package client;

import client.EdgeDetect;
import clipboard.ClipboardListener;
import clipboard.FileHandler;
import clipboard.FileListener;
import gui.StatusGUI;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	static StatusGUI statusGUI;
	static final int port = 44444;
	static final int clipboardPort = 44445;

	static Point screenCenter;

	static EdgeDetect edgeDetect;
	static EventHandler eventHandler;
	static MessageListener messageListener;
	static MessageSender messageSender;
	static FileListener fileListener;
	static FileHandler fileHandler;

	static short messageDelay = 25; // delay to send messages (in milliseconds)

	public static void main(String[] args) {
		try {
			socket = new DatagramSocket(port);
			r = new Robot();

			Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
			screenCenter = new Point(screenRes.width / 2, screenRes.height / 2);

			eventHandler = new EventHandler();
			eventHandler.start();

			edgeDetect = new EdgeDetect();
			edgeDetect.pause();
			edgeDetect.start(); // start paused

			messageListener = new MessageListener();
			messageListener.start();

			messageSender = new MessageSender();
			messageSender.start();

			statusGUI = StatusGUI.getInstance();
			statusGUI.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					exit();
					super.windowClosed(e);
				}
			});
			statusGUI.getClipboard.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fileListener.requestFile();
				}
			});

			fileHandler = new FileHandler(clipboardPort);
			fileHandler.start();

			fileListener = new FileListener(clipboardPort);
			fileListener.start();

			Toolkit.getDefaultToolkit().getSystemClipboard()
					.addFlavorListener(new ClipboardListener(messageSender));

			statusGUI.setVisible(true);
		} catch (SocketException | AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void exit() {
		//TODO: enviar mensagem disconnect
		statusGUI.dispose();
		eventHandler.interrupt();
		edgeDetect.interrupt();
		messageListener.interrupt();
		messageSender.interrupt();
		fileHandler.interrupt();
		fileListener.interrupt();
	}
	
	public static void leaveScreen() {
		Client.statusGUI.setActivity(false);
		edgeDetect.pause();
	}
	
	public static void joinScreen() {
		Client.statusGUI.setActivity(true);
		edgeDetect.unpause();
	}

	static void onEdge(byte edge) {
		messageSender.addMessage(Message.edge(edge));
	}
}
