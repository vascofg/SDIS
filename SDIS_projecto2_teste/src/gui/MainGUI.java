package gui;

import initiator.Initiator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import monitor.Monitor;

public class MainGUI {
	static JFrame frame;
	static JPanel panelS;
	static JPanel panelC;
	static JPanel panelI;
	static DefaultListModel<String> model = new DefaultListModel<String>();
	static JList<String> list = new JList<String>(model);
	public static Monitor initiatorMonitor;
	public static ArrayList<Monitor> ls = new ArrayList<Monitor>();
	static int tamanho = 5;
	static int vel = 25;
	static ArrayList<JPanel> panels = new ArrayList<JPanel>();

	private static class handelador implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("serv")) {
				frame.setVisible(false);
				frame.dispose();
				initServ();
			} else if (e.getActionCommand().equals("cli")) {
				frame.setVisible(false);
				frame.dispose();
				JOptionPane.showMessageDialog(null, "CLI been clicked");
			} else if (e.getActionCommand().equals("exit")) {
				frame.setVisible(false);
				Initiator.exit();
				frame.dispose();
			} else if (e.getActionCommand().equals("con")) {
				if (getNumMonitors() > 0) {
					Initiator.monitorsReady();
					frame.setVisible(false);
					frame.dispose();
				}
			} else if (e.getActionCommand().equals("opt")) {
				frame.setVisible(false);
				frame.dispose();
				createopt();
				initServ();
			}

		}
	}

	public static void init() {
		frame = new JFrame("init");
		panelS = new JPanel();
		panelC = new JPanel();

		frame.getContentPane().setLayout(new GridLayout(2, 1));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panelS);
		frame.getContentPane().add(panelC);
		frame.setLocationRelativeTo(null);
		JButton serv = new JButton("Servidor");
		JButton cli = new JButton("client");
		frame.setResizable(false);
		cli.setPreferredSize(serv.getPreferredSize());
		serv.setVerticalAlignment(SwingConstants.TOP);
		cli.setVerticalAlignment(SwingConstants.BOTTOM);

		panelS.add(serv);
		panelC.add(cli);
		// serv.setLocation(null);
		// cli.setLocation(null);
		serv.addActionListener(new handelador());
		cli.addActionListener(new handelador());
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
		/*********************** panel i *******************************/

		model.addElement("192.168.26.32   |    jose-pc");
		model.addElement("192.168.26.33   |    armando-pc");
		model.addElement("192.168.26.34   |    juvenaldo-pc");
		model.addElement("192.168.26.35   |    doce-pc");
		model.addElement("192.168.26.36   |    maninho-pc");

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBorder(BorderFactory.createEmptyBorder(0, 10, -10, 10));
		panelI.add(new JScrollPane(list));

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
		JButton ex = new JButton("Exit");

		panelC.setLayout(new GridLayout(3, 1));
		panelC.add(con);
		panelC.add(opt);
		panelC.add(ex);
		ex.addActionListener(new handelador());
		opt.addActionListener(new handelador());
		con.addActionListener(new handelador());
		ex.setActionCommand("exit");
		con.setActionCommand("con");
		opt.setActionCommand("opt");

		frame.pack();
		frame.setVisible(true);

	}

	private static void createopt() {
		String speeds[] = { "High", "Medium", "Slow" };
		JComboBox<String> speed = new JComboBox<String>(speeds);
		String widths[] = { "3x3", "5x5", "7x7" };
		JComboBox<String> width = new JComboBox<String>(widths);

		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel("Refresh Rate:"));
		myPanel.add(speed);
		myPanel.add(Box.createHorizontalStrut(15)); // a spacer
		myPanel.add(new JLabel("Grid Size:"));
		myPanel.add(width);

		int result = JOptionPane.showConfirmDialog(null, myPanel,
				"Please enter refresh rate and grid width values",
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			if (speed.getSelectedItem().toString().equals("High")) {
				vel = 10;
			} else if (speed.getSelectedItem().toString().equals("Medium")) {
				vel = 25;
			} else if (speed.getSelectedItem().toString().equals("Low")) {
				vel = 50;
			}

			if (width.getSelectedItem().toString().equals("3x3")) {
				tamanho = 3;
			} else if (width.getSelectedItem().toString().equals("5x5")) {
				tamanho = 5;
			} else if (width.getSelectedItem().toString().equals("7x7")) {
				tamanho = 7;
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

}
