package Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Multicast extends Thread {
	public int port;
	public int multicast_port;
	public InetAddress group;
	public InetAddress address;
	public MulticastSocket multicast_socket;
	public DatagramPacket serverInfo;
	

	public Multicast(String group,
			String multicast_port) throws UnknownHostException {
		
		this.multicast_port = Integer.parseInt(multicast_port);
		this.group = InetAddress.getByName(group);
		this.address = InetAddress.getLocalHost();
System.out.println("write msg");
		Scanner sc = new Scanner(System.in);
		String msg = sc.nextLine();
		//sc.close();
		serverInfo = new DatagramPacket(msg.getBytes(), msg.length(),
				this.group, this.multicast_port);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			multicast_socket = new MulticastSocket(multicast_port);
			multicast_socket.setTimeToLive(1);
			multicast_socket.joinGroup(group);
			
		
				multicast_socket.send(serverInfo);
				String msg = new String(serverInfo.getData(), 0, serverInfo.getLength());
				System.out.println("sent: " + msg);
				Thread.sleep(1000);
	
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}