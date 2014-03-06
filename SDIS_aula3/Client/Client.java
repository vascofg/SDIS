package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	public static Socket socket;

	public static void main(String[] args) throws Exception {
		// java Client <host_name> <port_number> <oper> <opnd>*
		// <plate number> <owner name>, for register
		// <plate number>, for lookup
		if (args.length < 4)
			throw (new Exception());

		InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);

		socket = new Socket(address, port);

		String msg, replyMsg;

		if (args[2].toLowerCase().equals("register")) {
			if (args.length != 5)
				throw (new Exception());
			msg = "register " + args[3] + " " + args[4];
			socket.getOutputStream().write(msg.getBytes(), 0, msg.length());
		} else if (args[2].toLowerCase().equals("lookup")) {
			msg = "lookup " + args[3];
			socket.getOutputStream().write(msg.getBytes(), 0, msg.length());
		} else
			throw (new Exception());

		socket.shutdownOutput(); // terminou envio (flush)

		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		replyMsg = in.readLine();

		socket.close();

		System.out.println(replyMsg);
	}
}