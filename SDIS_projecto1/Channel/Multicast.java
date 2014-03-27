package Channel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import Message.Header;
import Message.Message;
import Backup.Backup;
import Data.Chunk;

public class Multicast extends Thread {
	public int port;
	public int multicast_port;
	public InetAddress group;
	public InetAddress address;
	public MulticastSocket multicast_socket;
	public String ignoreFileID; // ignores this file ID
	public Integer ignoreChunkNo;
	public Boolean ignoreChunk;

	public Multicast(String group, String multicast_port) {
		try {
			this.multicast_port = Integer.parseInt(multicast_port);
			this.group = InetAddress.getByName(group);
			this.address = InetAddress.getLocalHost();

			this.ignoreFileID = null;
			this.ignoreChunkNo = null;
			this.ignoreChunk = null;

			multicast_socket = new MulticastSocket(this.multicast_port);
			multicast_socket.setTimeToLive(1);
			multicast_socket.joinGroup(this.group);
			multicast_socket.setLoopbackMode(true); // disable loopback
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
				multicast_socket.receive(messagePacket);
				msg = new Message(messagePacket.getData(),
						messagePacket.getLength());

				Header header = msg.getHeader();
				Chunk chunk = msg.getChunk();

				String messageType = header.getMessageType();

				System.out.println("received   " + header.toString());

				switch (messageType) {
				case "PUTCHUNK":
					if (ignoreChunk != null
							&& ignoreChunkNo == header.getChunkNo()
							&& ignoreFileID.equals(header.getFileId()))
						ignoreChunk = true; // recebeu o chunk que queremos
											// ignorar (não reenvia putchunk)
					else
						Backup.stored(chunk, msg.getChunkData());
					break;
				case "GETCHUNK":
					if (ignoreChunk != null
							&& ignoreChunkNo == header.getChunkNo()
							&& ignoreFileID.equals(header.getFileId()))
						ignoreChunk = true; // recebeu o chunk que queremos
											// ignorar (não reenvia CHUNK)
					else
						Backup.sendChunk(header.getFileId(),
								header.getChunkNo());
					break;
				case "STORED":
					// incrementa rep deg do chunk armazenado no peer remoto
					try {
						Backup.getChunkByID(header.getFileId(),
								header.getChunkNo())
								.incrementCurrentReplicationDeg();
					} catch (NullPointerException e) {
						// n tem chunk (ignora)
					}
					break;
				case "DELETE":
					Backup.deleteFile(header.getFileId());
					break;
				case "REMOVED":
					Backup.removed(header.getFileId(), header.getChunkNo());
					break;
				case "CHUNK":
					Backup.gotChunk(chunk, msg.getChunkData());
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void send(Message message) {
		byte[] messageBytes = message.getBytes();
		DatagramPacket controlMessagePacket = new DatagramPacket(messageBytes,
				messageBytes.length, this.group, this.multicast_port);

		try {
			multicast_socket.send(controlMessagePacket);
			System.out.println("sent   " + message.getHeader().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
