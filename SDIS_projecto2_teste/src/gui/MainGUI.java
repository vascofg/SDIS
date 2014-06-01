package gui;

import httpServer.HttpConnection;
import httpServer.User;
import initiator.Initiator;
import initiator.MessageSender;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import message.Message;
import monitor.Monitor;
import client.Client;

public class MainGUI {
	static JFrame frame;
	static JPanel panelS;
	static JPanel panelC;
	static JPanel panelI;
	static DefaultListModel<User> model = new DefaultListModel<User>();
	static JList<User> list = new JList<User>(model);
	public static Monitor initiatorMonitor;
	public static ArrayList<Monitor> ls = new ArrayList<Monitor>();
	static int tamanho = 5;
	static short vel = 25;
	static ArrayList<JPanel> panels = new ArrayList<JPanel>();
	static HttpConnection hC = new HttpConnection();
	public static ArrayList<User> users = new ArrayList<User>();
	private static String roomCode;

	private static class Handler implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("serv")) {
				try {
					Initiator.init();
					hC.init();
					frame.setVisible(false);
					frame.dispose();
					roomCode = hC.rsp(1);
					initServ();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (e.getActionCommand().equals("cli")) {
				try {
					Client.init();
					hC.init();
					hC.setCode(JOptionPane.showInputDialog(frame,
							"Insert Room code", "Enter room"));
					frame.setVisible(false);
					frame.dispose();
					hC.rsp(2);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// TODO por cenas
			} else if (e.getActionCommand().equals("exit")) {
				try {
					frame.setVisible(false);
					Initiator.exit();
					frame.dispose();
					hC.rsp(4);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (e.getActionCommand().equals("con")) {
				if (getNumMonitors() > 0) {
					Initiator.monitorsReady(vel);
					frame.setVisible(false);
					frame.dispose();
				}
			} else if (e.getActionCommand().equals("opt")) {
				frame.setVisible(false);
				createopt();
				frame.setVisible(true);

			} else if (e.getActionCommand().equals("gethosts")) {

				try {
					getIps(hC.rsp(3));

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		}
	}

	public static void getIps(String txt) throws UnknownHostException,
			InterruptedException {
		if (!txt.equals("")) {
			String[] uses = txt.split("~");
			String[] preps = null;
			model.clear();
			for (String c : uses) {
				preps = c.split(" ");
				User temp = new User(preps[0], preps[1]);
				users.add(temp);
				model.addElement(temp);
			}
			defineMonitors(users);
		}
	}

	public static void defineMonitors(ArrayList<User> users)
			throws InterruptedException, UnknownHostException {
		byte[] msg = new Message(Message.ALIVE).getBytes();
		Initiator.messageSender.checkingReachables = true;

		for (User user : users) {
			for (String ip : user.getIps()) {
				Initiator.messageSender.sendMessage(msg,
						InetAddress.getByName(ip));
			}
		}
		Thread.sleep(500);
		Initiator.messageSender.checkingReachables = false;
		for (User user : users) {
			for (Message temp : MessageSender.msgRec) {
				if (user.getIps().contains(
						temp.getRemoteAddress().getHostAddress())) {
					user.setIp(temp.getRemoteAddress().getHostAddress());
					break;
				}
			}
		}
		MessageSender.msgRec.clear();
	}

	public static void init() {
		frame = new JFrame();
		panelS = new JPanel();
		panelC = new JPanel();

		frame.getContentPane().setLayout(new GridLayout(2, 1));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panelS);
		frame.getContentPane().add(panelC);
		frame.setLocationRelativeTo(null);
		JButton serv = new JButton("Server");
		JButton cli = new JButton("Client");
		frame.setResizable(false);
		cli.setPreferredSize(serv.getPreferredSize());
		serv.setVerticalAlignment(SwingConstants.TOP);
		cli.setVerticalAlignment(SwingConstants.BOTTOM);

		panelS.add(serv);
		panelC.add(cli);
		// serv.setLocation(null);
		// cli.setLocation(null);
		serv.addActionListener(new Handler());
		cli.addActionListener(new Handler());
		serv.setActionCommand("serv");
		cli.setActionCommand("cli");

		frame.pack();
		frame.setVisible(true);

	}

	private static void initServ() {

		frame = new JFrame("Servidor");
		frame.setResizable(false);
		panelS = new JPanel();
		panelC = new JPanel();
		panelI = new JPanel();
		frame.setPreferredSize(new Dimension(600, 200));
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(new GridLayout(1, 2));
		panelS.setLayout(new GridLayout(tamanho, tamanho));
		panelS.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panelI);
		frame.getContentPane().add(panelS);
		frame.getContentPane().add(panelC);
		panelI.setBorder(BorderFactory
				.createTitledBorder("Available Connections"));
		panelI.setLayout(new BorderLayout());
		/*********************** panel i *******************************/

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panelI.add(new JScrollPane(list), BorderLayout.CENTER);
		JPanel temp = new JPanel();
		temp.add(new JLabel("Room code:"));
		JTextArea roomCodeTArea = new JTextArea(roomCode);
		roomCodeTArea.setEditable(false);
		roomCodeTArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				((JTextArea) e.getComponent()).selectAll();
			}
		});
		temp.add(roomCodeTArea);
		panelI.add(temp, BorderLayout.SOUTH);

		/*********************** panel i *******************************/

		int count = 0;
		for (int i = 1; i <= tamanho * tamanho; i++) {
			JPanel pan = new JPanel();
			ls.add(new Monitor(count));
			pan.setEnabled(true);
			if (tamanho * tamanho / 2 + 1 == i) {
				pan.setBackground(Color.GREEN);
				initiatorMonitor = ls.get(i - 1);
			} else {
				pan.setBackground(Color.white);
				pan.addMouseListener(new BoxListener(pan));
			}
			pan.setPreferredSize(new Dimension(3, 3));
			pan.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			// add a mouse listener
			// to make the panels
			// clickable
			pan.setName(count + "");
			++count;
			panels.add(pan);
			panelS.add(pan);
		}

		JButton con = new JButton("Connect");
		JButton opt = new JButton("Options");
		JButton ts = new JButton("Get hosts");
		JButton ex = new JButton("Exit");

		panelC.setLayout(new GridLayout(4, 1));
		panelC.add(con);
		panelC.add(opt);
		panelC.add(ts);
		panelC.add(ex);

		ex.addActionListener(new Handler());
		opt.addActionListener(new Handler());
		con.addActionListener(new Handler());
		ts.addActionListener(new Handler());
		ex.setActionCommand("exit");
		con.setActionCommand("con");
		opt.setActionCommand("opt");
		ts.setActionCommand("gethosts");

		frame.pack();
		frame.setVisible(true);

	}

	private static void createopt() {
		String speeds[] = { "High", "Medium", "Slow" };
		JComboBox<String> speed = new JComboBox<String>(speeds);
		switch (vel) {
		case 10:
			speed.setSelectedIndex(0);
			break;
		case 25:
			speed.setSelectedIndex(1);
			break;
		case 50:
			speed.setSelectedIndex(2);
			break;
		}

		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel("Refresh Rate:"));
		myPanel.add(speed);
		myPanel.add(Box.createHorizontalStrut(15)); // a spacer

		int result = JOptionPane.showConfirmDialog(null, myPanel, "Options",
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			switch (speed.getSelectedIndex()) {
			case 0:
				vel = 10;
				break;
			case 1:
				vel = 25;
				break;
			case 2:
				vel = 50;
				break;
			}
		}

	}

	private static int getNumMonitors() {
		int count = 0;
		for (Monitor mon : ls)
			if (mon.getIp() != null)
				count++;
		return count;
	}

	public static void main(String[] args) {
		MainGUI.init();
	}

}
