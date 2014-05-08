package initiator;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.LinkedBlockingQueue;

import message.Message;

public class EventHandler extends Thread {
	private boolean go = true;
	private LinkedBlockingQueue<InputEvent> eventQueue;
	private int deltaX, deltaY;
	private boolean mouseUpdated;

	public EventHandler() {
		eventQueue = new LinkedBlockingQueue<>();
		deltaX = deltaY = 0;
	}

	void addEvent(InputEvent event) {
		eventQueue.offer(event); // não espera para inserir
	}

	@Override
	public void run() {
		InputEvent event;
		while (go) {
			try {
				event = eventQueue.take();
				switch (event.getID()) {
				case MouseEvent.MOUSE_MOVED:
				case MouseEvent.MOUSE_DRAGGED:
					updateMouseDelta((MouseEvent) event);
					break;
				case KeyEvent.KEY_PRESSED:
				case KeyEvent.KEY_RELEASED:
					if(((KeyEvent)event).getKeyCode()==0) //TODO: keycodes de Ç etc.
						break;
				default:
					Teste.messageSender.addMessage(new Message(event));
				}
				// TODO: criar MENSAIGE e adicionar ao sender!
			} catch (InterruptedException e) { // eventQueue interrompida
			}
		}
	}

	private synchronized void updateMouseDelta(MouseEvent event) {
		// discard center moves
		if (event.getX() != Teste.relativeCenterX
				|| event.getY() != Teste.relativeCenterY) { // discard
			deltaX += (event.getX() - Teste.relativeCenterX);
			deltaY += (event.getY() - Teste.relativeCenterY);
			if (!mouseUpdated) { //se rato ainda não tinha sido movido, adiciona mensagem indicativa
				Teste.messageSender.addMessage(new Message());
				mouseUpdated = true;
			}
		}
	}

	public synchronized Point getMouseDelta() throws Exception {
		Point p = new Point(deltaX, deltaY);
		if (deltaX == 0 && deltaY == 0)
			throw new Exception();
		deltaX = deltaY = 0;
		mouseUpdated = false;
		return p;
	}

	@Override
	public void interrupt() {
		this.go = false;
		super.interrupt();
	}
}
