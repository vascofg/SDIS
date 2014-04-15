package client;

import java.util.concurrent.LinkedBlockingQueue;

public class EventHandler extends Thread {
	private boolean go = true;
	LinkedBlockingQueue<Message> messageQueue;

	public EventHandler() {
		messageQueue = new LinkedBlockingQueue<>();
	}

	void addMessage(Message msg) {
		messageQueue.offer(msg); // não espera para inserir
	}

	@Override
	public void run() {
		Message msg;
		while (go) {
			try {
				msg = messageQueue.take();
				System.out.println("EVENT HANDLER: RECEBI MENSAGEM "); //por o id
				// TODO: ler msg (switch case?) e processar comandos
			} catch (InterruptedException e) {
				System.out.println("interrompido catch");
			}
		}
	}

	@Override
	public void interrupt() {
		this.go = false;
		// TODO: Interromper queue.take()
		// eventQueue.add(null); // queue bloqueante iria causar deadlock NÃO
		// FUNCIONA
	}
}