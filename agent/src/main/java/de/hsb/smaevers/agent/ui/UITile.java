package de.hsb.smaevers.agent.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Objects;

import javax.swing.JComponent;

import de.hsb.smaevers.agent.model.Tile;

public class UITile extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH = 50;
	public static final int HEIGHT = 50;
	
	private Tile tile;
	
	public UITile(Tile tile) {
		this.tile = tile;
		setSize(WIDTH, HEIGHT);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		
		switch (tile.getType()) {
		case STANDARD:
			g2.setColor(Color.GREEN);
			g2.fillRect(0, 0, getWidth(), getHeight());
			break;
			
		case ROCK:
			g2.setColor(Color.GRAY);
			g2.fillRect(0, 0, getWidth(), getHeight());
			break;
			
		case TRAP:
			g2.setColor(Color.BLACK);
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
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	@Override
	public int hashCode() {
		return tile.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof UITile){
			UITile t = (UITile) obj;
			
			return Objects.equals(tile, t.tile);
		}
		
		return false;
	}
	
}
