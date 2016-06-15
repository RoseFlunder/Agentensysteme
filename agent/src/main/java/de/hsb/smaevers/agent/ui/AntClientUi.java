package de.hsb.smaevers.agent.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;
import jade.gui.GuiEvent;

public class AntClientUi {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private final JFrame frame;
	private final AntUiAgent agent;

	private WorldPanel worldPanel;
	
	public AntClientUi(final AntUiAgent agent){
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
		
		
//		worldPanel.putTile(new CellObject(0, 0, CellType.OBSTACLE));
//		worldPanel.putTile(new CellObject(1, 1, CellType.PIT));
//		CellObject cell = new CellObject(2, 2, CellType.FREE);
//		cell.setFood(2);
//		worldPanel.putTile(cell);
//		worldPanel.putTile(new CellObject(3, 3, CellType.START));
//		worldPanel.putTile(new CellObject(4, 4, CellType.FREE));
//		
//		Ant ant = new Ant();
//		ant.setLocation(2, 2);
//		worldPanel.add(ant, 1);
		
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public void updateCell(CellObject cell){
		worldPanel.putTile(cell);
	}
	
	class WorldPanel extends JLayeredPane {

		private static final long serialVersionUID = 1L;
		
		private int maxX = 0;
		private int maxY = 0;

		public WorldPanel(){
			setLayout(null);
		}
		
		public void putTile(CellObject cell){
			int xPos = cell.getCol() * UITile.WIDTH;
			int yPos = cell.getRow() * UITile.HEIGHT;
			
			if (xPos + UITile.WIDTH > maxX)
				maxX = xPos + UITile.WIDTH;
			if (yPos + UITile.HEIGHT > maxY)
				maxY = yPos + UITile.HEIGHT;
			
			WorldPanel.this.setSize(maxX, maxY);
			
			Component comp = WorldPanel.this.getComponentAt(xPos, yPos);
			if (comp != null && comp instanceof UITile){
				remove(comp);
			}
				
			UITile uiTile = new UITile(cell);
			uiTile.setLocation(xPos, yPos);
			add(uiTile, 0);
			
			validate();
			repaint();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(maxX, maxY);
		}
	}

}
