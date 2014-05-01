package initiator;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class EventListener {
	public static final MouseAdapter mouseAdapter = new MouseAdapter() {

		// MOUSE LISTENER
		@Override
		public void mouseReleased(MouseEvent arg0) {
			System.out.println("Release mouse " + arg0.getButton() + " "
					+ arg0.getClickCount() + " times");
			Teste.eventHandler.addEvent(arg0);
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			System.out.println("Press mouse " + arg0.getButton() + " "
					+ arg0.getClickCount() + " times");
			Teste.eventHandler.addEvent(arg0);
		}

		// ---------------------
		// WHEEL LISTENER
		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {
			System.out.println("ROTATE: " + arg0.getWheelRotation()
					+ " notches");
			Teste.eventHandler.addEvent(arg0);
		}

		// ---------------------
		// MOTION LISTENER
		@Override
		public void mouseMoved(MouseEvent e) {
			Teste.r.mouseMove(Teste.absoluteCenterX, Teste.absoluteCenterY);
			Teste.eventHandler.addEvent(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Teste.r.mouseMove(Teste.absoluteCenterX, Teste.absoluteCenterY);
			Teste.eventHandler.addEvent(e);
		}
	};
	// ---------------------
	// KEY LISTENER
	public static final KeyAdapter keyAdapter = new KeyAdapter() {

		@Override
		public void keyReleased(KeyEvent arg0) {
			System.out.println("Released "
					+ KeyEvent.getKeyText(arg0.getKeyCode()));
			Teste.eventHandler.addEvent(arg0);
		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			System.out.println("Pressed "
					+ KeyEvent.getKeyText(arg0.getKeyCode()));
			Teste.eventHandler.addEvent(arg0);
		}
	};
}