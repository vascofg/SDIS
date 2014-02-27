package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {

	public static DatagramSocket socket;
	public static MulticastSocket multicast_socket;

	public static void main(String[] args) throws Exception {
		// java Client <multicast_addr> <multicast_port> <oper> <opnd>*
		// <plate number> <owner name>, for register
		// <plate number>, for lookup
		if (args.length < 4)
			throw (new Exception());

		int multicast_port = Integer.parseInt(args[1]);
		InetAddress group = InetAddress.getByName(args[0]);
		
		multicast_socket = new MulticastSocket(multicast_port);
		socket = new DatagramSocket(); //unicast
		
		multicast_socket.joinGroup(group);
		
		byte[] buf = new byte[256];
		DatagramPacket serverInfo = new DatagramPacket(buf, buf.length);
		
		//IP PORT
		multicast_socket.receive(serverInfo);
		
		String msg = new String(serverInfo.getData(), 0, serverInfo.getLength());

		String data[] = msg.split(" ");	
		
		InetAddress address = InetAddress.getByName(data[0]);
		int port = Integer.parseInt(data[1]);
		
		if (args[2].toLowerCase().equals("register")) {
			if (args.length != 5)
				throw (new Exception());
			msg = "register " + args[3] + " " + args[4];
			DatagramPacket packet = new DatagramPacket(msg.getBytes(),
					msg.length(), address, port);
			socket.send(packet);
		} else if (args[2].toLowerCase().equals("lookup")) {
			msg = "lookup " + args[3];
			DatagramPacket packet = new DatagramPacket(msg.getBytes(),
					msg.length(), address, port);
			socket.send(packet);
		} else
			throw (new Exception());

		buf = new byte[256];
		DatagramPacket reply = new DatagramPacket(buf, buf.length);

		socket.receive(reply);
		String replyMsg = new String(reply.getData());
		System.out.println(replyMsg);
	}
}