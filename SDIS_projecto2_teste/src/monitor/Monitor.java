package monitor;

import java.net.InetAddress;

public class Monitor {
	int id;
	InetAddress ip;
	int port;
	Monitor up;
	Monitor down;
	Monitor left;
	Monitor right;
	Boolean main = false;

	public Monitor(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
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
}
