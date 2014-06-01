package gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FileTransferProgress extends JFrame {
	private static final long serialVersionUID = 1L;

	public JProgressBar progressBar;
	private JButton cancel;
	private boolean canceled;
	public JTextArea output;

	public FileTransferProgress() {
		this.setTitle("File transfer");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		this.progressBar = new JProgressBar();
		this.progressBar.setStringPainted(true);
		this.canceled = false;
		this.cancel = new JButton("Cancel");
		this.cancel.setAlignmentX(CENTER_ALIGNMENT);
		this.cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				canceled = true;
			}
		});
		this.output = new JTextArea(8, 25);
		this.output.setMargin(new Insets(5, 5, 5, 5));
		this.output.setEditable(false);
		this.output.setLineWrap(true);
		this.getContentPane().add(this.progressBar);
		this.getContentPane().add(new JScrollPane(this.output));
		this.getContentPane().add(this.cancel);
		this.pack();
		this.setVisible(true);
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void close() {
		this.dispose();
	}
}
