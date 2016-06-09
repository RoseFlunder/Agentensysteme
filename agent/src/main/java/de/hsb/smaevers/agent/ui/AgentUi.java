package de.hsb.smaevers.agent.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import de.hsb.smaevers.agent.model.Tile;
import de.hsb.smaevers.agent.model.TileType;
import jade.gui.GuiEvent;

public class AgentUi {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private final JFrame frame;
	private final AntUiAgent agent;

	private WorldPanel worldPanel;
	
	public AgentUi(final AntUiAgent agent){
		this.agent = agent;
		
		LOG.debug("Constructor of UI called");
		
		frame = new JFrame("Antworld Client UI");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				LOG.debug("Closing GUI and sending close event to gui agent");
				GuiEvent event = new GuiEvent(frame, AntUiAgent.EVENT_CLOSE);
				agent.postGuiEvent(event);
				frame.dispose();
			}
		});
		
		frame.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(worldPanel = new WorldPanel());
		frame.add(scrollPane, BorderLayout.CENTER);
		
		
		worldPanel.putTile(new Tile(0, 0, TileType.ROCK));
		worldPanel.putTile(new Tile(1, 0, TileType.TRAP));
		
		worldPanel.putTile(new Tile(2, 2, TileType.UNKOWN));
		worldPanel.putTile(new Tile(3, 2, TileType.STANDARD));
		
		worldPanel.putTile(new Tile(10, 10, TileType.STANDARD));
		worldPanel.putTile(new Tile(10, 10, TileType.ROCK));
		
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	class WorldPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		private int maxX = 0;
		private int maxY = 0;

		public WorldPanel(){
			setLayout(null);
		}
		
		public void putTile(Tile t){
			int xPos = t.getX() * UITile.WIDTH;
			int yPos = t.getY() * UITile.HEIGHT;
			
			if (xPos + UITile.WIDTH > maxX)
				maxX = xPos + UITile.WIDTH;
			if (yPos + UITile.HEIGHT > maxY)
				maxY = yPos + UITile.HEIGHT;
			
			WorldPanel.this.setSize(maxX, maxY);
			
			Component comp = WorldPanel.this.getComponentAt(xPos, yPos);
			if (comp != null && comp instanceof UITile){
				LOG.debug("Remove old tile at {}|{}", t.getX(), t.getY());
				remove(comp);
			}
				
			LOG.debug("Add new tile at {}|{}", t.getX(), t.getY());
			UITile uiTile = new UITile(t);
			uiTile.setLocation(xPos, yPos);
			add(uiTile);
			
			validate();
			repaint();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(maxX, maxY);
		}
	}

}
