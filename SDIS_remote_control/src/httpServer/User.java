package httpServer;

import gui.MainGUI;

import java.util.ArrayList;

public class User {
	String name, ip;
	ArrayList<String> ips;
	boolean placed = false;
	String id;

	public User(String name, String ips) {
		this.name = name;
		fillIps(ips);
	}

	public boolean isPlaced() {
		return placed;
	}

	public void unPlace(String id) {
		for (User user : MainGUI.users) {
			if (user.id.equals(id)) {
				this.placed = false;
			}
		}
	}

	public void Place(String id) {
		this.placed = true;
		this.id = id;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public ArrayList<String> getIps() {
		return ips;
	}

	public void fillIps(String ip) {
		ips = new ArrayList<String>();
		String delims = "/";
		String[] parsedIps = ip.split(delims);
		for (String temp : parsedIps) {
			ips.add(temp);
		}
	}

	public void defineValidMonitors() {

	}

}
