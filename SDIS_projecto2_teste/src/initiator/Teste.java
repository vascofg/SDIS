package initiator;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

	static MouseDeltaThread mouseThread;
	static EdgeDetectThread edgeThread;

	static InetAddress address;
	static String addressName;
	static final int port = 44444;

	public static void main(String[] args) throws AWTException {
		addressName = JOptionPane
				.showInputDialog("Input IP address:");
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
		frame.setAlwaysOnTop(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		// frame.setVisible(true);

		edgeThread = new EdgeDetectThread();
		edgeThread.start();

		mouseThread = new MouseDeltaThread(socket, address, port);
		mouseThread.pause();
		mouseThread.start();

		frame.addMouseListener(new MouseListener() {

			String msg;

			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (captured) {
					System.out.println("Release mouse " + arg0.getButton()
							+ " " + arg0.getClickCount() + " times");
					msg = "release "
							+ InputEvent.getMaskForButton(arg0.getButton());
					packet = new DatagramPacket(msg.getBytes(), msg.length(),
							address, port);
					try {
						socket.send(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				if (captured) {
					System.out.println("Press mouse " + arg0.getButton() + " "
							+ arg0.getClickCount() + " times");
					msg = "press "
							+ InputEvent.getMaskForButton(arg0.getButton());
					packet = new DatagramPacket(msg.getBytes(), msg.length(),
							address, port);
					try {
						socket.send(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				if (captured) {
					r.mouseMove(absoluteCenterX, absoluteCenterY);
					// frame.toFront();
					System.out.println("Mouse escaped. Recapturing...");
				}

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				// frame.toFront();

			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!captured) {
					System.out.println("Captured");
					frame.getContentPane().setCursor(blankCursor);
					captured = true;
				} else {
					System.out.println("Click mouse " + arg0.getButton() + " "
							+ arg0.getClickCount() + " times");
				}

			}
		});

		frame.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				absoluteCenterX = relativeCenterX + frame.getLocation().x;
				absoluteCenterY = relativeCenterY + frame.getLocation().y;
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});

		frame.addMouseMotionListener(new MouseMotionListener() {
			int deltaX, deltaY;

			@Override
			public void mouseMoved(MouseEvent e) {
				if (captured
						&& (e.getX() != relativeCenterX && e.getY() != relativeCenterY)) { // discard
																							// center
																							// moves
					deltaX = e.getX() - relativeCenterX;
					deltaY = e.getY() - relativeCenterY;
					r.mouseMove(absoluteCenterX, absoluteCenterY);
					// System.out.println("MOVE: " + deltaX + "  " + deltaY);
					mouseThread.setPos(deltaX, deltaY);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) { // discard center moves
				if (captured
						&& (e.getX() != relativeCenterX && e.getY() != relativeCenterY)) {
					deltaX = e.getX() - relativeCenterX;
					deltaY = e.getY() - relativeCenterY;
					r.mouseMove(absoluteCenterX, absoluteCenterY);
					// System.out.println("DRAG: " + deltaX + "  " + deltaY);
					mouseThread.setPos(deltaX, deltaY);
				}
			}
		});

		frame.addKeyListener(new KeyListener() {

			String msg;

			@Override
			public void keyTyped(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (captured && (arg0.getKeyCode() != KeyEvent.VK_ESCAPE)) {
					System.out.println("Released key "
							+ KeyEvent.getKeyText(arg0.getKeyCode()));
					msg = "releasekey " + arg0.getKeyCode();
					packet = new DatagramPacket(msg.getBytes(), msg.length(),
							address, port);
					try {
						socket.send(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (captured) {
					if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
						captured = false;
						frame.getContentPane().setCursor(
								Cursor.getDefaultCursor());
						mouseThread.pause();
						frame.setVisible(false);
						edgeThread.unpause();
						System.out.println("Released capture");
					} else {
						System.out.println("Pressed key "
								+ KeyEvent.getKeyText(arg0.getKeyCode()));
						msg = "presskey " + arg0.getKeyCode();
						packet = new DatagramPacket(msg.getBytes(), msg
								.length(), address, port);
						try {
							socket.send(packet);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		});
		frame.addMouseWheelListener(new MouseWheelListener() {
			String msg;

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				if (captured) {
					System.out.println("ROTATE: " + arg0.getWheelRotation()
							+ " notches");
					msg = "rotate " + arg0.getWheelRotation();
					packet = new DatagramPacket(msg.getBytes(), msg.length(),
							address, port);
					try {
						socket.send(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	static void onEdge() {
		edgeThread.pause();
		enableWindow();
		mouseThread.unpause();
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
		r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
}
