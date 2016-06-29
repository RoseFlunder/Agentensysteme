package de.hsb.smaevers.agent.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;
import de.hsb.smaevers.agent.model.json.PerceptionObject;
import jade.core.AID;
import jade.gui.GuiEvent;

public class AntClientUi {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private final JFrame frame;
	
	private Map<String, Ant> ants = new LinkedHashMap<>();

	private JTable table;
	private AntTableModel antTableModel;
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
		
		table = new JTable(antTableModel = new AntTableModel());
		frame.add(new JScrollPane(table), BorderLayout.WEST);

		table.getColumnModel().getColumn(1).setMaxWidth(100);
		table.getColumnModel().getColumn(1).setMaxWidth(40);
		table.getColumnModel().getColumn(2).setMaxWidth(40);
		table.getColumnModel().getColumn(3).setMaxWidth(70);
		
		frame.setSize(1024, 768);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public void updateCell(CellObject cell){
		worldPanel.putTile(cell);
	}
	
	public void updateAntPosition(PerceptionObject p, AID ant){
		if (!ants.containsKey(ant.getLocalName())){
			Ant antComp = new Ant();
			worldPanel.add(antComp, new Integer(1));
			ants.put(ant.getLocalName(), antComp);
			antTableModel.addAnt(antComp);
		}
		
		Ant antComp = ants.get(ant.getLocalName());
		antComp.setLocation(p.getCell().getCol(), p.getCell().getRow());
		antComp.setPerception(p);
		antComp.repaint();
		antTableModel.fireTableDataChanged();
	}
	
	class AntTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		
		private List<Ant> antList = new ArrayList<>();
		
		public void addAnt(Ant ant){
			antList.add(ant);
		}

		@Override
		public int getRowCount() {
			return antList.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			case 1:
				return "Col";
			case 2:
				return "Row";
			case 3:
				return "Total food";

			default:
				break;
			}
			
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Ant ant = antList.get(row);
			
			switch (column) {
			case 0:
				return ant.getPerception().getName();
			case 1:
				return ant.getPerception().getCell().getCol();
			case 2:
				return ant.getPerception().getCell().getRow();
			case 3:
				return ant.getPerception().getTotalFood();

			default:
				break;
			}
			
			return null;
		}
		
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
				comp.repaint();
			} else {
				UITile uiTile = new UITile(cell);
				uiTile.setLocation(xPos, yPos);
				add(uiTile, new Integer(0));
				
				cells.put(new Point(xPos, yPos), uiTile);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(maxX, maxY);
		}
	}

}
