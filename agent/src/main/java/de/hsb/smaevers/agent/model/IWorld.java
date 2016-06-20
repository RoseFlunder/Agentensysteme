package de.hsb.smaevers.agent.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import de.hsb.smaevers.agent.model.json.CellObject;

public interface IWorld {
	
	public void put(CellObject cell);

	public CellObject get(Integer col, Integer row);
	
	public List<CellObject> getAccessibleSuccessors(CellObject cell);
	
	public Collection<CellObject> getAllCells();
	
	public List<CellObject> getUnvisitedCells(Predicate<CellObject> p);
	
	public List<CellObject> getCellsWithFood();
	
}
