package initiator;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class EventListener {
	public MouseListener mouseListener;
	public MouseWheelListener wheelListener;
	public MouseMotionListener motionListener;
	public KeyListener keyListener;

	public EventListener() {
		// MOUSE LISTENER
		mouseListener = new MouseListener() {

			String msg;

			@Override
			public void mouseReleased(MouseEvent arg0) {
				System.out.println("Release mouse " + arg0.getButton() + " "
						+ arg0.getClickCount() + " times");
				msg = "release "
						+ InputEvent.getMaskForButton(arg0.getButton());
				Teste.eventHandler.addEvent(arg0);
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				System.out.println("Press mouse " + arg0.getButton() + " "
						+ arg0.getClickCount() + " times");
				msg = "press " + InputEvent.getMaskForButton(arg0.getButton());
				Teste.eventHandler.addEvent(arg0);
			}

			@Override
			public void mouseExited(MouseEvent arg0) {// NOT IMPLEMENTED
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {// NOT IMPLEMENTED
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {// NOT IMPLEMENTED
			}
		};
		// ---------------------
		// WHEEL LISTENER
		wheelListener = new MouseWheelListener() {
			String msg;

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				System.out.println("ROTATE: " + arg0.getWheelRotation()
						+ " notches");
				msg = "rotate " + arg0.getWheelRotation();
				Teste.eventHandler.addEvent(arg0);
			}
		};
		// ---------------------
		// MOTION LISTENER
		motionListener = new MouseMotionListener() {

			int deltaX, deltaY;

			@Override
			public void mouseMoved(MouseEvent e) {
				Teste.r.mouseMove(Teste.absoluteCenterX, Teste.absoluteCenterY);
				Teste.eventHandler.addEvent(e);
				/*if ((e.getX() != relativeCenterX && e.getY() != relativeCenterY)) { // discard
																					// center
																					// moves
					deltaX = e.getX() - relativeCenterX;
					deltaY = e.getY() - relativeCenterY;
					r.mouseMove(absoluteCenterX, absoluteCenterY);
					// System.out.println("MOVE: " + deltaX + "  " + deltaY);
					mouseThread.setPos(deltaX, deltaY);
				}*/
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Teste.r.mouseMove(Teste.absoluteCenterX, Teste.absoluteCenterY);
				Teste.eventHandler.addEvent(e);
				/*if (captured // discard center moves
						&& (e.getX() != relativeCenterX && e.getY() != relativeCenterY)) {
					deltaX = e.getX() - relativeCenterX;
					deltaY = e.getY() - relativeCenterY;
					r.mouseMove(absoluteCenterX, absoluteCenterY);
					// System.out.println("DRAG: " + deltaX + "  " + deltaY);
					mouseThread.setPos(deltaX, deltaY);
				}*/
			}
		};
		// ---------------------
		// KEY LISTENER
		keyListener = new KeyListener() {
			
			String msg;
			
			@Override
			public void keyTyped(KeyEvent arg0) { //NOT IMPLEMENTED
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				System.out.println("Released " + KeyEvent.getKeyText(arg0.getKeyCode()));
				Teste.eventHandler.addEvent(arg0);
				/*if (captured && (arg0.getKeyCode() != KeyEvent.VK_ESCAPE)) {
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
				}*/
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				System.out.println("Pressed " + KeyEvent.getKeyText(arg0.getKeyCode()));
				Teste.eventHandler.addEvent(arg0);
				/*if (captured) {
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
				}*/
			}
		};
	}
}
