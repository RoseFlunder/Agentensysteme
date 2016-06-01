package de.hsb.smaevers.agent.model;

import java.util.HashMap;
import java.util.Map;

public class World implements IWorld {
	
	private Map<Integer, Map<Integer, Tile>> data = new HashMap<>();

	@Override
	public void put(Integer x, Integer y, Tile tile) {
		if (!data.containsKey(x))
			data.put(x, new HashMap<>());
		
		data.get(x).put(y, tile);
	}

	@Override
	public Tile get(Integer x, Integer y) {
		return data.get(x).get(y);
	}

}
