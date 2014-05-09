package gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import monitor.Monitor;

public class BoxListener implements MouseListener {

	private Boolean clied = false;
	private JPanel pp;

	public BoxListener(JPanel pan) {
		pp = pan;

		// TODO por imagens icones
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		JPanel btnPanel = (JPanel) arg0.getSource();
		System.out.println(btnPanel.getName());
		System.out.println(Gui.list.getSelectedValue());

		if (clied) { // disconnects the Monitor
			pp.setBackground(Color.WHITE);
			disconnect(btnPanel.getName());
			clied = false;
		} else { // checks if Monitor can be created, and creates it if possible
			checkSurrounds(btnPanel.getName());
			clied = true;
		}

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Checks surroundings of clicked cell, creates monitor and connects it to
	 * adjacent ones.
	 */
	private void checkSurrounds(String name) {
		int id = Integer.parseInt(name);
		int tamanho = Gui.tamanho;
		boolean inst = false;
		Monitor mon = null;

		if (((id + 1) % tamanho) != 0
				&& (Gui.panels.get(id + 1).getBackground().equals(Color.BLACK) || Gui.panels
						.get(id + 1).getBackground().equals(Color.GREEN))) { // check
																				// right

			// create monitor
			if (!inst) {
				mon = instamon(id, "192", "5000");
				inst = true;
			}

			// connect to adjacent monitor
			connect(mon, id + 1, 0);
			Gui.panels.get(id).setBackground(Color.BLACK);

		} else if (((id + 1) % tamanho) != 1
				&& (Gui.panels.get(id - 1).getBackground().equals(Color.BLACK) || Gui.panels
						.get(id - 1).getBackground().equals(Color.GREEN))) { // check
																				// left

			// create monitor
			if (!inst) {
				mon = instamon(id, "192", "5000");
				inst = true;
			}

			// connect to adjacent monitor
			connect(mon, id - 1, 1);
			Gui.panels.get(id).setBackground(Color.BLACK);

		} else if ((id / tamanho) != 0
				&& (Gui.panels.get(id - tamanho).getBackground()
						.equals(Color.BLACK) || Gui.panels.get(id - tamanho)
						.getBackground().equals(Color.GREEN))) { // check top

			// create monitor
			if (!inst) {
				mon = instamon(id, "192", "5000");
				inst = true;
			}

			// connect to adjacent monitor
			connect(mon, id - tamanho, 2);
			Gui.panels.get(id).setBackground(Color.BLACK);

		} else if (((id / tamanho) != tamanho - 1)
				&& (Gui.panels.get(id + tamanho).getBackground()
						.equals(Color.BLACK) || Gui.panels.get(id + tamanho)
						.getBackground().equals(Color.GREEN))) { // check bottom

			// create monitor
			if (!inst) {
				mon = instamon(id, "192", "5000");
				inst = true;
			}

			// connect to adjacent monitor
			connect(mon, id + tamanho, 3);
			Gui.panels.get(id).setBackground(Color.BLACK);

		}
	}

	/**
	 * Instantiates a Monitor.
	 */
	private Monitor instamon(int id, String ip, String port) {
		Monitor temp = Gui.ls.get(id);
		ip = JOptionPane.showInputDialog("Input IP address:");
		temp.setIp(ip);
		temp.setPort(port);
		return temp;
	}

	/**
	 * Connects Monitor mon to the Monitor identified by id according to the
	 * direction dir.
	 */
	private void connect(Monitor mon, int id, int dir) {
		Monitor temp = Gui.ls.get(id);
		if (dir == 0) {
			mon.setRight(temp);
			temp.setLeft(mon);
		} else if (dir == 1) {
			mon.setLeft(temp);
			temp.setRight(mon);
		} else if (dir == 2) {
			mon.setUp(temp);
			temp.setDown(mon);
		} else if (dir == 3) {
			mon.setDown(temp);
			temp.setUp(mon);
		}
	}

	/**
	 * Disconnects Monitor with id name.
	 */
	private void disconnect(String name) {
		int id = Integer.parseInt(name);
		Monitor temp = Gui.ls.get(id);

		// disconnect from up
		if (temp.getUp() != null) {
			temp.getUp().setDown(null);
			temp.setDown(null);
		}

		// disconnect from right
		if (temp.getRight() != null) {
			temp.getRight().setLeft(null);
			temp.setRight(null);
		}

		// disconnect from bottom
		if (temp.getDown() != null) {
			temp.getDown().setUp(null);
			temp.setDown(null);
		}

		// disconnect from left
		if (temp.getLeft() != null) {
			temp.getLeft().setRight(null);
			temp.setLeft(null);
		}

		// disconnect self
		temp.setIp(null);
		temp.setPort(null);
	}
}
