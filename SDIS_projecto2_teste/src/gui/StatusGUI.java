package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.net.InetAddress;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import clipboard.ClipboardListener;

public class StatusGUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private static StatusGUI instance = null;

	private JPanel activity;
	private JLabel clipboardContent;
	public JButton getClipboard;

	protected StatusGUI() {
		// singleton
	}

	private void init() {
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		activity = new JPanel();
		activity.setPreferredSize(new Dimension(150, 150));
		this.setResizable(false);
		activity.setBackground(Color.red);
		clipboardContent = new JLabel("No contents");
		clipboardContent.setAlignmentX(CENTER_ALIGNMENT);
		getClipboard = new JButton("Get Clipboard");
		getClipboard.setEnabled(false);
		getClipboard.setAlignmentX(CENTER_ALIGNMENT);
		this.getContentPane().add(activity);
		this.getContentPane().add(clipboardContent);
		this.getContentPane().add(getClipboard);
		this.setAlwaysOnTop(true);
		this.setType(Type.UTILITY);
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
		this.activity.setBackground(activity ? Color.GREEN : Color.RED);
	}

	public void setClipboardContent(byte contentType, InetAddress addr) {
		this.clipboardContent.setText(ClipboardListener.typeText[contentType]
				+ "@" + addr.getHostAddress());
	}
}
