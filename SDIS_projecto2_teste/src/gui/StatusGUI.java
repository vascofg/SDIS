package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import clipboard.ClipboardListener;

public class StatusGUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private static StatusGUI instance = null;

	private JPanel activityPanel;
	private JLabel activityLabel;
	public JButton getClipboard;

	protected StatusGUI() {
		// singleton
	}

	private void init() {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		activityLabel = new JLabel();
		activityLabel.setHorizontalAlignment(SwingConstants.CENTER);
		activityLabel.setAlignmentY(CENTER_ALIGNMENT);
		activityLabel.setOpaque(true);
		activityPanel = new JPanel(new BorderLayout());
		activityPanel.setPreferredSize(new Dimension(150, 50));
		activityLabel.setBackground(Color.red);
		JLabel close = new JLabel("X ", SwingConstants.RIGHT);
		close.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (JOptionPane.showOptionDialog(null, "Exit application?",
						"Exit", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null) == JOptionPane.YES_OPTION) {
					WindowEvent ev = new WindowEvent(instance,
							WindowEvent.WINDOW_CLOSING);
					Toolkit.getDefaultToolkit().getSystemEventQueue()
							.postEvent(ev);
				}
			}
		});
		activityPanel.add(close, BorderLayout.NORTH);
		activityPanel.add(activityLabel, BorderLayout.CENTER);
		getClipboard = new JButton("No contents");
		getClipboard.setEnabled(false);
		getClipboard.setAlignmentX(CENTER_ALIGNMENT);
		c.gridy++;
		c.insets = new Insets(5, 5, 5, 5);
		this.getContentPane().add(activityPanel, c);
		c.gridy++;
		this.getContentPane().add(getClipboard, c);
		this.setUndecorated(true);
		this.setResizable(false);
		this.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(
				getGraphicsConfiguration());
		this.setLocation((screenSize.width - scnMax.right - frameSize.width),
				(screenSize.height - scnMax.bottom - frameSize.height));
	}

	public static StatusGUI getInstance() {
		if (instance == null) {
			instance = new StatusGUI();
			instance.init();
		}
		return instance;
	}

	public void setActivity(boolean activity) {
		this.activityLabel.setText(activity ? "ACTIVE" : "INACTIVE");
		Color bg = activity ? Color.GREEN : Color.RED;
		this.activityPanel.setBackground(bg);
		this.activityLabel.setBackground(bg);
	}

	public void setClipboardContent(byte contentType, InetAddress addr) {
		this.getClipboard.setText(ClipboardListener.typeText[contentType] + "@"
				+ addr.getHostAddress());
	}
}
