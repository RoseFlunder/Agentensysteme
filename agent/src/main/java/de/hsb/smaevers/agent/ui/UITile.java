package de.hsb.smaevers.agent.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import de.hsb.smaevers.agent.model.json.CellObject;

public class UITile extends JComponent {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH = 50;
	public static final int HEIGHT = 50;
	
	private static Image imgFood = null;
	private static Image imgStandard = null;
	private static Image imgRock = null;
	private static Image imgTrap = null;
	private static Image imgUnknown = null;
	private static Image imgStart = null;
	
	static {
		try {
			imgFood = ImageIO.read(UITile.class.getClassLoader().getResource("images/food.png"));
			imgStandard = ImageIO.read(UITile.class.getClassLoader().getResource("images/standard.png"));
			imgRock = ImageIO.read(UITile.class.getClassLoader().getResource("images/rock.png"));
			imgTrap = ImageIO.read(UITile.class.getClassLoader().getResource("images/trap.png"));
			imgUnknown = ImageIO.read(UITile.class.getClassLoader().getResource("images/unknown.png"));
			imgStart = ImageIO.read(UITile.class.getClassLoader().getResource("images/start2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private CellObject cell;
	
	public UITile(CellObject cell) {
		this.cell = cell;
		setSize(WIDTH, HEIGHT);
	}
	
	public void setCell(CellObject cell) {
		this.cell = cell;
	}
	
	public CellObject getCell(){
		return cell;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		
		switch (cell.getType()) {
		case FREE:
			if (cell.getFood() > 0){
				g2.drawImage(imgFood, 0, 0, null);
				g2.setColor(Color.BLACK);
				g2.drawString(Integer.toString(cell.getFood()), WIDTH / 2, HEIGHT - 5);
			} else {
				g2.drawImage(imgStandard, 0, 0, null);
			}
			
			break;
			
		case OBSTACLE:
			g2.drawImage(imgRock, 0, 0, null);
			break;
			
		case PIT:
			g2.drawImage(imgTrap, 0, 0, null);
			break;
			
		case START:
			g2.drawImage(imgStart, 0, 0, null);
			break;
			
		case UNKOWN:
			g2.drawImage(imgUnknown, 0, 0, null);
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

	
}
