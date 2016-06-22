package de.hsb.smaevers.agent.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * Represents an ant in the UI
 * @author Stephan
 */
public class Ant extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH = 50;
	public static final int HEIGHT = 50;
	
	private static Image img = null;
	
	static {
		try {
			img = ImageIO.read(Ant.class.getClassLoader().getResource("images/ant.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Ant() {
		setSize(WIDTH, HEIGHT);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g2.drawImage(img, 0, 0, null);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x * WIDTH, y * HEIGHT);
	}
	
	

}
