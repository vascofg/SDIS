package httpServer;


public class User {
	String name, ips;
	
	public User(String name, String ips) {
		this.name=name;
		this.ips=ips;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "            " +name + " "+ ips;
	}
	
	public String[] getIps() {
		String delims = "/";
		return ips.split(delims); 
	}
	
	
}
