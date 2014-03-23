package Channel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
			multicast_socket.setLoopbackMode(true); //disable loopback
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

		Message msg;
		
		while (true) {
			try {
				multicast_socket.receive(controlMessagePacket);
				msg  = new Message(controlMessagePacket.getData(), 
						controlMessagePacket.getLength());
				
				System.out.println("received   " + msg.getHeader().getMessageType());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void send(Message message) throws UnsupportedEncodingException{
		byte[] messageBytes = message.getBytes();
		DatagramPacket controlMessagePacket = new DatagramPacket(
				messageBytes, messageBytes.length, this.group, this.multicast_port);
		try {
			multicast_socket.send(controlMessagePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
