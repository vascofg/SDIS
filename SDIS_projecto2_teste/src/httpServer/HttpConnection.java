package httpServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import client.Client;

public class HttpConnection {

	private final String USER_AGENT = "Mozilla/5.0";

	String rsp;
	String code;
	HttpConnection http;
	String pcName;

	public void init() {
		http = new HttpConnection();
		try {
			pcName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			pcName = "unknown";
		}

	}

	public void setCode(String code) {
		this.code = code;
	}

	public String rsp(int nr) throws Exception {

		if (nr == 1) {
			code = http.sendPost("action=1");

			return code;

		} else if (nr == 2) {
			String st = "";

			ArrayList<Inet4Address> ipcs = getIps();
			for (Inet4Address addr : ipcs) {
				st += addr.toString();
			}
			System.out.println(st);
			st = http.sendPost("action=" + code + "&name=" + pcName + "&ip="
					+ st + "~");
			if (st.equals("HASH errada"))
				Client.exit();
			System.out.println(st);
		} else if (nr == 3) {
			String smth = http.sendPost("action=" + 69 + "&file=" + code);
			System.out.println(smth);
			return smth;
		} else if (nr == 4) {
			String smth = http.sendGet("action=" + 99 + "&file=" + code);
			System.out.println(smth);
		} else {
			System.out.println("error soz");
			return "";
		}

		return "";

	}

	// HTTP GET request
	private String sendGet(String msg) throws Exception {

		String url = "http://paginas.fe.up.pt/~ei06013/sdis2/frontpage.php?"
				+ msg;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		return response.toString();

	}

	// HTTP POST request
	private String sendPost(String msg) throws Exception {

		String url = "http://paginas.fe.up.pt/~ei06013/sdis2/frontpage.php";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = msg;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		return response.toString();

	}

	public static ArrayList<Inet4Address> getIps() throws SocketException,
			UnknownHostException {
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
				.getNetworkInterfaces();
		ArrayList<Inet4Address> ips = new ArrayList<Inet4Address>();
		while (networkInterfaces.hasMoreElements()) {

			NetworkInterface networkInterface = (NetworkInterface) networkInterfaces
					.nextElement();

			if (networkInterface.isUp() && !networkInterface.isLoopback()) {
				Enumeration<InetAddress> addrs = networkInterface
						.getInetAddresses();
				while (addrs.hasMoreElements())
					try {
						ips.add((Inet4Address) addrs.nextElement());
					} catch (ClassCastException e) {
						// ipv6 - ignore (not implemented)
					}
			}

		}
		return ips;
	}

}
