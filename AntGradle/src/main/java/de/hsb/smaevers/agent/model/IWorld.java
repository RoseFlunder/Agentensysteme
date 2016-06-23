package de.hsb.smaevers.agent.model;

import java.util.List;
import java.util.function.Predicate;

import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;

/**
 * Data structure to store the Cells from antworld
 * @author Stephan
 */
public interface IWorld {
	
	/**
	 * Adds a new Cell to the datastructure
	 * @param cell
	 */
	public void put(CellObject cell);

	/**
	 * Retrieves a cell for the given coordinates
	 * @param col Column of the cell
	 * @param row Row of the cell
	 * @return the {@link CellObject} or <code>null</code> if its not in the structure
	 */
	public CellObject get(Integer col, Integer row);
	
	/**
	 * Gets all neighbours which are accessible, so no traps and no obstacles
	 * @param cell
	 * @return {@link List} of neighbour cells
	 */
	public List<CellObject> getAccessibleSuccessors(CellObject cell);
	
	/**
	 * Returns all neighbours of the given cell
	 * @param cell
	 * @return
	 */
	public List<CellObject> getAllSuccessors(CellObject cell);
	
	/**
	 * Returns all unvisited cells, {@link CellType} UNKOWN, that match the given predicate
	 * @param p
	 * @return
	 */
	public List<CellObject> getUnvisitedCells(Predicate<CellObject> p);
	
	/**
	 * Returns als known cells with food > 0
	 * @return
	 */
	public List<CellObject> getCellsWithFood();
	
}
