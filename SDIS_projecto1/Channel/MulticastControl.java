package Channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import Message.Message;

public class MulticastControl extends Thread {
	public int port;
	public int multicast_port;
	public InetAddress group;
	public InetAddress address;
	public MulticastSocket multicast_socket;

	public MulticastControl(String group, String multicast_port) {
		try {
			this.multicast_port = Integer.parseInt(multicast_port);
			this.group = InetAddress.getByName(group);
			this.address = InetAddress.getLocalHost();

			multicast_socket = new MulticastSocket(this.multicast_port);
			multicast_socket.setTimeToLive(1);
			multicast_socket.joinGroup(this.group);
			multicast_socket.setLoopbackMode(false); //disable loopback
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		super.run();
		byte[] buf = new byte[256];
		DatagramPacket controlMessagePacket = new DatagramPacket(buf,
				buf.length);

		String msg;
		while (true) {
			try {
				multicast_socket.receive(controlMessagePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			msg = new String(controlMessagePacket.getData(), 0,
					controlMessagePacket.getLength());
			System.out.println("received   " + msg);
		}
	}

	public void send(String msg) {
		//String msg = message.toString();
		DatagramPacket controlMessagePacket = new DatagramPacket(
				msg.getBytes(), msg.length(), this.group, this.multicast_port);
		try {
			multicast_socket.send(controlMessagePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("sent: " + msg);
	}
}
