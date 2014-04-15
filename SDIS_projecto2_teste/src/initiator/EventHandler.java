package initiator;

import java.awt.event.InputEvent;
import java.util.concurrent.LinkedBlockingQueue;

public class EventHandler extends Thread {
	private boolean go = true;
	LinkedBlockingQueue<InputEvent> eventQueue;

	public EventHandler() {
		eventQueue = new LinkedBlockingQueue<>();
	}

	void addEvent(InputEvent event) {
		eventQueue.offer(event); //não espera para inserir
	}

	@Override
	public void run() {
		InputEvent event;
		while (go) {
			try {
				event = eventQueue.take();
				if(event == null)
					System.out.println("interrompido");
				else
					System.out.println("EVENT HANDLER: RECEBI EVENTO " + event.getID());
				//TODO: criar MENSAIGE e adicionar ao sender!
			} catch (InterruptedException e) {
				System.out.println("interrompido catch");
			}
		}
	}

	@Override
	public void interrupt() {
		this.go = false;
		//TODO: Interromper queue.take()
		//eventQueue.add(null); // queue bloqueante iria causar deadlock NÃO FUNCIONA
	}
}
