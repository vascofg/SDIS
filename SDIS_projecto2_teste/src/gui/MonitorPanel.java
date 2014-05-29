package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MonitorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage buffas;

	public MonitorPanel() {
		try {
			buffas = ImageIO.read(new File("Icon_pc.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(buffas, 0, 0, null);
	}
}
