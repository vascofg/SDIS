package initiator;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Teste {

	static int absoluteCenterX, absoluteCenterY, relativeCenterX,
			relativeCenterY;

	static JFrame frame;

	static Robot r;

	static boolean captured = false;

	static DatagramSocket socket;
	static DatagramPacket packet;

	static EdgeDetect edgeThread;

	static EventHandler eventHandler;

	static MessageSender messageSender;

	static InetAddress address;
	static String addressName;
	static final int port = 44444;

	static short messageDelay = 25; // delay to send messages (in milliseconds)

	public static void main(String[] args) throws AWTException {
		addressName = JOptionPane.showInputDialog("Input IP address:");
		r = new Robot();

		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName(addressName);
		} catch (SocketException | UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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

		edgeThread = new EdgeDetect();
		edgeThread.start();

		eventHandler = new EventHandler();
		eventHandler.start();

		messageSender = new MessageSender();
		messageSender.start();

		frame.addMouseListener(EventListener.mouseAdapter);
		frame.addMouseMotionListener(EventListener.mouseAdapter);
		frame.addMouseWheelListener(EventListener.mouseAdapter);
		frame.addKeyListener(EventListener.keyAdapter);
	}

	static void onEdge(byte edge) {
		// EdgeDetect.EDGE_RIGHT
		edgeThread.pause();
		enableWindow();
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
		//r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		//r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
}
