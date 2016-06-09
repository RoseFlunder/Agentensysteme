package de.hsb.smaevers.agent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class World implements IWorld {
	
	private Map<Integer, Map<Integer, Tile>> data = new HashMap<>();

	@Override
	public void put(Tile tile) {
		if (!data.containsKey(tile.getX()))
			data.put(tile.getX(), new HashMap<>());
		
		data.get(tile.getX()).put(tile.getY(), tile);
	}

	@Override
	public Tile get(Integer x, Integer y) {
		return data.get(x).get(y);
	}

	@Override
	public Iterable<Tile> getAllTiles() {
		List<Tile> allTiles = new ArrayList<>();
		for (Iterator<Map<Integer, Tile>> iterator = data.values().iterator(); iterator.hasNext();) {
			Map<Integer, Tile> map = iterator.next();
			allTiles.addAll(map.values());
		}
		
		return allTiles;
	}

}
