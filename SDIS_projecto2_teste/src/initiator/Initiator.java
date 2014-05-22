package initiator;

import gui.Gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JFrame;

import monitor.Monitor;

public class Initiator {

	static int absoluteCenterX, absoluteCenterY, relativeCenterX,
			relativeCenterY;

	static JFrame frame;
	static JFrame closeFrame;

	static Robot r;

	static boolean captured = false;

	static Monitor previousMonitor;
	static Monitor currentMonitor;

	static DatagramSocket socket;
	static DatagramPacket packet;

	static EdgeDetect edgeThread;

	static EventHandler eventHandler;

	static MessageSender messageSender;

	static MessageListener messageListener;

	static Control control;

	static final int port = 44444;

	static short messageDelay = 25; // delay to send messages (in milliseconds)

	public static void main(String[] args) throws AWTException {
		Gui.init();

		r = new Robot();

		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		final Cursor blankCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

		frame = new JFrame();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.5f));
		frame.setType(Type.UTILITY);
		frame.getContentPane().setCursor(blankCursor);
		frame.setAlwaysOnTop(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setFocusTraversalKeysEnabled(false); // allow capture of tab key
		frame.addMouseListener(EventListener.mouseAdapter);
		frame.addMouseMotionListener(EventListener.mouseAdapter);
		frame.addMouseWheelListener(EventListener.mouseAdapter);
		frame.addKeyListener(EventListener.keyAdapter);

		closeFrame = new JFrame("CLOSE");
		closeFrame.setResizable(false);
		closeFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
				super.windowClosed(e);
			}
		});

		edgeThread = new EdgeDetect();
		// edgeThread.start();

		eventHandler = new EventHandler();
		eventHandler.start();

		messageSender = new MessageSender();

		messageListener = new MessageListener();

		control = new Control();
		control.pause();
		control.start();

		messageSender.start();
		messageListener.start();
	}

	public static void monitorsReady() {
		currentMonitor = Gui.initiatorMonitor;
		edgeThread.start();
		closeFrame.setVisible(true);
	}

	static void onEdge(byte edge) {
		Monitor tmp = null;
		switch (edge) {
		case EdgeDetect.EDGE_LEFT:
			tmp = currentMonitor.getLeft();
			break;
		case EdgeDetect.EDGE_RIGHT:
			tmp = currentMonitor.getRight();
			break;
		case EdgeDetect.EDGE_TOP:
			tmp = currentMonitor.getUp();
			break;
		case EdgeDetect.EDGE_BOTTOM:
			tmp = currentMonitor.getDown();
			break;
		}
		if (tmp != null) {
			previousMonitor = currentMonitor; // save previous
			currentMonitor = tmp;
			if (currentMonitor == Gui.initiatorMonitor) {
				control.pause();
				edgeThread.unpause();
				disableWindow();
			} else {
				edgeThread.pause();
				control.newConnection();
			}
		}
	}

	static void enableWindow() {
		frame.setVisible(true);
		// recalculate center points
		relativeCenterX = frame.getSize().width / 2;
		relativeCenterY = frame.getSize().height / 2;
		absoluteCenterX = relativeCenterX + frame.getLocation().x;
		absoluteCenterY = relativeCenterY + frame.getLocation().y;
		// move to center
		r.mouseMove(absoluteCenterX, absoluteCenterY);
		// click to grab focus
		// r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		// r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	static void disableWindow() {
		frame.setVisible(false);
	}

	public static void connected() {
		enableWindow();
	}

	public static void timeout() {
		// TODO: voltar para monitor anterior: pode não ter adjacencias
		// válidas...
		System.out.println("TIMEOUT");
		// currentMonitor = previousMonitor;
		// if (currentMonitor == Gui.initiatorMonitor) {
		//TODO: set current monitor as null
		currentMonitor = Gui.initiatorMonitor;
		control.pause();
		edgeThread.unpause();
		disableWindow();
		// }
	}

	public static void exit() {
		control.disconnectAll(Gui.ls);
		frame.dispose();
		closeFrame.dispose();
		edgeThread.interrupt();
		eventHandler.interrupt();
		messageSender.interrupt();
		messageListener.interrupt();
		control.interrupt();
	}
}
