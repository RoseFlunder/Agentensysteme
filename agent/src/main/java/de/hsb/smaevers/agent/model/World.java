package de.hsb.smaevers.agent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hsb.smaevers.agent.model.json.CellObject;

public class World implements IWorld {
	
	private Map<Integer, Map<Integer, CellObject>> data = new HashMap<>();

	@Override
	public void put(CellObject tile) {
		if (!data.containsKey(tile.getCol()))
			data.put(tile.getCol(), new HashMap<>());
		
		data.get(tile.getCol()).put(tile.getRow(), tile);
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

}
