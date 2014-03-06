package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Vehicle.Vehicle;

public class Server {

	public static ServerSocket socket;
	public static Socket echoSocket;

	public static List<Vehicle> vehicles = new ArrayList<Vehicle>();

	public static Vehicle getVehicleByPlate(String plate) {
		for (int i = 0; i < vehicles.size(); i++)
			if (vehicles.get(i).getPlate().equals(plate))
				return vehicles.get(i);

		return null;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1)
			throw (new Exception());

		int port = Integer.parseInt(args[0]);

		socket = new ServerSocket(port);

		BufferedReader in;
		OutputStream out;

		while (true) {
			echoSocket = socket.accept(); // espera por ligação
			in = new BufferedReader(new InputStreamReader(
					echoSocket.getInputStream()));

			String msg = in.readLine();

			String data[] = msg.split(" ");

			String replyMsg;

			if (data[0].equals("register")) {
				if (getVehicleByPlate(data[1]) != null)
					replyMsg = "-1";
				else {
					Vehicle newVehicle = new Vehicle(data[1], data[2]);
					vehicles.add(newVehicle);
					replyMsg = Integer.toString(vehicles.size());
				}

			}

			else {
				try {
					replyMsg = getVehicleByPlate(data[1]).getOwner();
				} catch (NullPointerException e) {
					replyMsg = "NOT_FOUND";
				}
			}

			replyMsg += '\n';

			out = echoSocket.getOutputStream();

			echoSocket.getOutputStream().write(replyMsg.getBytes(), 0,
					replyMsg.length());

			System.out.println(msg);

			echoSocket.close();
		}

	}
}
