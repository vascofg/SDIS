package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {

	public static DatagramSocket socket;

	public static void main(String[] args) throws Exception {
		// java Client <host_name> <port_number> <oper> <opnd>*
		// <plate number> <owner name>, for register
		// <plate number>, for lookup
		if (args.length < 4)
			throw (new Exception());

		InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);

		socket = new DatagramSocket(); // client doesn't need port

		String msg;

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

		byte[] buf = new byte[256];
		DatagramPacket reply = new DatagramPacket(buf, buf.length);

		socket.receive(reply);
		String replyMsg = new String(reply.getData());
		System.out.println(replyMsg);
	}
}