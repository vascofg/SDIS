package Channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import Backup.Backup;
import Data.Chunk;
import Message.Header;
import Message.Message;

public class Unicast extends Thread {
	public int port;
	public DatagramSocket unicast_socket;
	InetAddress address;
	public boolean wasStored;

	public void setAddress(InetAddress unicast_address) {
		this.address = unicast_address;
	}

	public Unicast(String name, String port) {
		super(name);
		try {
			this.port = Integer.parseInt(port);

			this.wasStored = false;

			unicast_socket = new DatagramSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() { // receive
		super.run();
		byte[] buf = new byte[Chunk.ChunkSize + 256];
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);

		Message msg;

		while (true) {
			try {
				unicast_socket.receive(messagePacket);
				msg = new Message(messagePacket.getData(),
						messagePacket.getLength());

				Header header = msg.getHeader();
				Chunk chunk = msg.getChunk();

				String messageType = header.getMessageType();

				System.out.println("received   " + header.toString() + " from unicast");

				switch (messageType) {
				case "STORED":
					wasStored = true;
					break;
				case "CHUNK":
					Backup.gotChunk(chunk, msg.getChunkData(), messagePacket.getAddress());
					break;
				default:
					System.out.println("Message type not recognized!");
					break;
				}
			} catch (SocketException e) {
				// thread interrompida, sair do loop
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void send(Message message) {
		byte[] messageBytes = message.getBytes();
		DatagramPacket controlMessagePacket = new DatagramPacket(messageBytes,
				messageBytes.length, this.address, this.port);

		try {
			unicast_socket.send(controlMessagePacket);
			System.out.println("sent   " + message.getHeader().toString()  + " by unicast");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		unicast_socket.close();
	}
}
