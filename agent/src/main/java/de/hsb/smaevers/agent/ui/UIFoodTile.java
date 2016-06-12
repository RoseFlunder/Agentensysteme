package de.hsb.smaevers.agent.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.hsb.smaevers.agent.model.Tile;

public class UIFoodTile extends UITile {

	private static final long serialVersionUID = 1L;
	
	private static Image img = null;
	
	static {
		try {
			img = ImageIO.read(UIFoodTile.class.getClassLoader().getResource("food.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public UIFoodTile(Tile tile) {
		super(tile);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g2.drawImage(img, 0, 0, null);
	}

}
