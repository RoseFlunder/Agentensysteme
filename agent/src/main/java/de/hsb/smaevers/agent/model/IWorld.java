package de.hsb.smaevers.agent.model;

import java.util.List;

import de.hsb.smaevers.agent.model.json.CellObject;

public interface IWorld {
	
	public void put(CellObject tile);

	public CellObject get(Integer col, Integer row);
	
	public List<CellObject> getKnownSuccessors(CellObject cell);
	
	public Iterable<CellObject> getAllTiles();
	
}
