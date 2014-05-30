package httpServer;

import java.util.ArrayList;


public class User {
	String name, ip;
	ArrayList<String> ips;
	
	public User(String name, String ips) {
		this.name=name;
		fillIps(ips);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "            " +name + " "+ ips;
	}
	
	public String getIp() {
		return ip;
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
		for(String temp : parsedIps) {
			ips.add(temp);
		}
	}
	
	public void defineValidMonitors() {
		
	}
	
}
