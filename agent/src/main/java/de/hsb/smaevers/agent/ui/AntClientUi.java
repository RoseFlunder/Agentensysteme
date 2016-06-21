package de.hsb.smaevers.agent.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;
import jade.core.AID;
import jade.gui.GuiEvent;

public class AntClientUi {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private final JFrame frame;
	
	private Map<String, Ant> ants = new HashMap<>();

	private WorldPanel worldPanel;
	
	public AntClientUi(final AntUiAgent agent){		
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
		
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public void updateCell(CellObject cell){
		worldPanel.putTile(cell);
	}
	
	public void updateAntPosition(CellObject cell, AID ant){
		if (!ants.containsKey(ant.getLocalName())){
			Ant antComp = new Ant();
			worldPanel.add(antComp, new Integer(1));
			ants.put(ant.getLocalName(), antComp);
		}
		
		Ant antComp = ants.get(ant.getLocalName());
		antComp.setLocation(cell.getCol(), cell.getRow());
		antComp.repaint();
	}
	
	/**
	 * Panel where all cells and ants are added
	 * @author Stephan
	 */
	class WorldPanel extends JLayeredPane {

		private static final long serialVersionUID = 1L;
		
		private Map<Point, UITile> cells = new HashMap<>();
		
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
			
			Point point = new Point(xPos, yPos);
			UITile comp = cells.get(point);
			
			if (comp != null){
				CellObject stored = comp.getCell();
				//check if update is useful
				if (stored == null || (stored.getType() == CellType.UNKOWN && cell.getType() != CellType.UNKOWN)
						|| cell.getFood() < stored.getFood())
					comp.setCell(cell);
			} else {
				UITile uiTile = new UITile(cell);
				uiTile.setLocation(xPos, yPos);
				add(uiTile, new Integer(0), 0);
				
				cells.put(new Point(xPos, yPos), uiTile);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(maxX, maxY);
		}
	}

}
