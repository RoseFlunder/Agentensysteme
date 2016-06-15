package de.hsb.smaevers.agent.model;

import de.hsb.smaevers.agent.model.json.CellObject;

public interface IWorld {
	
	public void put(CellObject tile);

	public CellObject get(Integer col, Integer row);
	
	public Iterable<CellObject> getAllTiles();
	
}
