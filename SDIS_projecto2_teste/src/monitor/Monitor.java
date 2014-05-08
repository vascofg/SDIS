package monitor;

public class Monitor {
	int id;
	String ip;
	String port;
	Monitor up;
	Monitor down;
	Monitor left;
	Monitor right;
	Boolean main= false;

	public Monitor(int id) {
	this.id=id;
	}
	public void init(){
		ip= "127.0.0.1";
		port= "nao sei";
	}
	
	
	public int getId() {
		return id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public Monitor getUp() {
		return up;
	}
	public void setUp(Monitor up) {
		this.up = up;
	}
	public Monitor getDown() {
		return down;
	}
	public void setDown(Monitor down) {
		this.down = down;
	}
	public Monitor getLeft() {
		return left;
	}
	public void setLeft(Monitor left) {
		this.left = left;
	}
	public Monitor getRight() {
		return right;
	}
	public void setRight(Monitor right) {
		this.right = right;
	}
	
	

	/*
	public void configure(String cenas){
		//dependente da string mudar pc decente
		//realconf(String ip, String port);
	}
	
	public void realconf(String ip, String port){
		
	}*/
}
