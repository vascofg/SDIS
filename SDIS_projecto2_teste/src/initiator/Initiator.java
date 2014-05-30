package initiator;

import gui.MainGUI;
import gui.StatusGUI;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JFrame;

import initiator.EdgeDetect;
import clipboard.ClipboardListener;
import clipboard.FileHandler;
import clipboard.FileListener;
import message.Message;
import monitor.Monitor;

public class Initiator {

	static int absoluteCenterX, absoluteCenterY, relativeCenterX,
			relativeCenterY;

	static JFrame eventCaptureFrame;
	static StatusGUI statusGUI;

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

	static FileHandler fileHandler;

	static FileListener fileListener;

	static final int port = 44444;
	static final int clipboardPort = 44445;

	static Dimension screenRes;

	static short messageDelay = 25; // delay to send messages (in milliseconds)

	public static void main(String[] args) throws AWTException {
		screenRes = Toolkit.getDefaultToolkit().getScreenSize();
		MainGUI.init();

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

		eventCaptureFrame = new JFrame();
		eventCaptureFrame.setResizable(false);
		eventCaptureFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		eventCaptureFrame.setUndecorated(true);
		eventCaptureFrame.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.01f));
		eventCaptureFrame.setType(Type.UTILITY);
		eventCaptureFrame.getContentPane().setCursor(blankCursor);
		eventCaptureFrame.setAlwaysOnTop(true);
		eventCaptureFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		eventCaptureFrame.setFocusTraversalKeysEnabled(false); // allow capture
																// of tab key
		eventCaptureFrame.addMouseListener(EventListener.mouseAdapter);
		eventCaptureFrame.addMouseMotionListener(EventListener.mouseAdapter);
		eventCaptureFrame.addMouseWheelListener(EventListener.mouseAdapter);
		eventCaptureFrame.addKeyListener(EventListener.keyAdapter);

		edgeThread = new EdgeDetect();
		// edgeThread.start();

		eventHandler = new EventHandler();
		eventHandler.start();

		messageSender = new MessageSender();

		messageListener = new MessageListener();

		control = new Control();
		control.pause();
		control.start();

		statusGUI = StatusGUI.getInstance();
		statusGUI.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
				super.windowClosed(e);
			}
		});
		statusGUI.setActivity(true);
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

		messageSender.start();
		messageListener.start();

		Toolkit.getDefaultToolkit().getSystemClipboard()
				.addFlavorListener(new ClipboardListener(messageSender));
	}

	public static void monitorsReady() {
		currentMonitor = MainGUI.initiatorMonitor;
		edgeThread.start();
		statusGUI.setVisible(true);
	}

	static void onEdge(byte edge, int percentage) {
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
			if (previousMonitor != MainGUI.initiatorMonitor)
				messageSender.sendMessage(
						new Message(Message.LEAVE).getBytes(), previousMonitor.getIp());
			if (currentMonitor == MainGUI.initiatorMonitor) {
				control.pause();
				edgeThread.unpause();
				disableWindow();
				switch (edge) {
				case EdgeDetect.EDGE_RIGHT:
					Initiator.r.mouseMove(1, Initiator.screenRes.height
							* percentage / 100);
					break;
				case EdgeDetect.EDGE_LEFT:
					Initiator.r.mouseMove(Initiator.screenRes.width - 1,
							Initiator.screenRes.height * percentage / 100);
					break;
				case EdgeDetect.EDGE_BOTTOM:
					Initiator.r.mouseMove(Initiator.screenRes.width
							* percentage / 100, 1);
					break;
				case EdgeDetect.EDGE_TOP:
					Initiator.r.mouseMove(Initiator.screenRes.width
							* percentage / 100, Initiator.screenRes.height - 1);
					break;
				}
			} else {
				edgeThread.pause();
				control.newConnection(edge, percentage);
			}
		}
	}

	static void enableWindow() {
		eventCaptureFrame.setVisible(true);
		// recalculate center points
		relativeCenterX = eventCaptureFrame.getSize().width / 2;
		relativeCenterY = eventCaptureFrame.getSize().height / 2;
		absoluteCenterX = relativeCenterX + eventCaptureFrame.getLocation().x;
		absoluteCenterY = relativeCenterY + eventCaptureFrame.getLocation().y;
		// move to center
		r.mouseMove(absoluteCenterX, absoluteCenterY);
		// click to grab focus
		// r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		// r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		statusGUI.setActivity(false);
	}

	static void disableWindow() {
		eventCaptureFrame.setVisible(false);
		statusGUI.setActivity(true);
	}

	public static void connected(byte edge, int percentage) {
		Initiator.messageSender.addMessage(Message.edge(edge, percentage));
		enableWindow();
	}

	public static void timeout() {
		// TODO: voltar para monitor anterior: pode não ter adjacencias
		// válidas...
		System.out.println("TIMEOUT");
		// currentMonitor = previousMonitor;
		// if (currentMonitor == Gui.initiatorMonitor) {
		currentMonitor = MainGUI.initiatorMonitor;
		control.pause();
		edgeThread.unpause();
		disableWindow();
		// }
	}

	public static void exit() {
		control.disconnectAll();
		eventCaptureFrame.dispose();
		statusGUI.dispose();
		edgeThread.interrupt();
		eventHandler.interrupt();
		messageSender.interrupt();
		messageListener.interrupt();
		control.interrupt();
		fileHandler.interrupt();
		fileListener.interrupt();
	}
}
