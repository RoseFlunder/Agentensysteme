package de.hsb.smaevers.agent.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import de.hsb.smaevers.agent.model.Tile;

public class UITile extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private static final int WIDTH = 50;
	private static final int HEIGHT = 50;
	
	private Tile tile;
	
	public UITile(Tile tile) {
		this.tile = tile;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		
		switch (tile.getType()) {
		case STANDARD:
			g2.setBackground(Color.GREEN);
			g2.fillRect(0, 0, getWidth(), getHeight());
			break;
			
		case ROCK:
			g2.setBackground(Color.GRAY);
			g2.fillRect(0, 0, getWidth(), getHeight());
			break;
			
		case TRAP:
			g2.setBackground(Color.BLACK);
			g2.fillRect(0, 0, getWidth(), getHeight());
			break;

		default:
			break;
		}
		
		//frame
		g2.setColor(Color.BLACK);
		g2.drawRect(0, 0, getWidth(), getHeight());
	}



	@Override
	public int getWidth() {
		return WIDTH;
	}
	
	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}
	
}
