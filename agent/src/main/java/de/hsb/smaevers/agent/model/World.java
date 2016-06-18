package de.hsb.smaevers.agent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;

public class World implements IWorld {
	
	private Map<Integer, Map<Integer, CellObject>> data = new HashMap<>();

	@Override
	public void put(CellObject newCell) {
		int col = newCell.getCol();
		int row = newCell.getRow();
		
		if (!data.containsKey(col))
			data.put(col, new HashMap<>());
		
		if (!data.get(col).containsKey(row))
			data.get(col).put(row, newCell);
		else {
			CellObject cell = data.get(col).get(row);
			cell.setAnts(newCell.getAnts());
			cell.setFood(newCell.getFood());
			cell.setSmell(newCell.getSmell());
			cell.setStench(newCell.getStench());
			cell.setType(newCell.getType());
		}
	}

	@Override
	public CellObject get(Integer col, Integer row) {
		if (data.containsKey(col))
			return data.get(col).get(row);
		return null;
	}

	@Override
	public Iterable<CellObject> getAllTiles() {
		List<CellObject> allTiles = new ArrayList<>();
		for (Iterator<Map<Integer, CellObject>> iterator = data.values().iterator(); iterator.hasNext();) {
			Map<Integer, CellObject> map = iterator.next();
			allTiles.addAll(map.values());
		}
		
		return allTiles;
	}

	@Override
	public List<CellObject> getSuccessorsWithoutObstacles(CellObject cell) {
		List<CellObject> successors = new ArrayList<>();
		int row = cell.getRow();
		int col = cell.getCol();
		
		if (get(col - 1, row) != null && get(col - 1, row).getType() != CellType.OBSTACLE)
			successors.add(get(col - 1, row));
		if (get(col + 1, row) != null && get(col + 1, row).getType() != CellType.OBSTACLE)
			successors.add(get(col + 1, row));
		if (get(col, row - 1) != null && get(col, row - 1).getType() != CellType.OBSTACLE)
			successors.add(get(col, row - 1));
		if (get(col, row + 1) != null && get(col, row + 1).getType() != CellType.OBSTACLE)
			successors.add(get(col, row + 1));
		
		return successors;
		
	}

}
