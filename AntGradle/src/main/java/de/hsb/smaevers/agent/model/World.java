package de.hsb.smaevers.agent.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;

public class World implements IWorld {

	private Map<Integer, Map<Integer, CellObject>> data = new HashMap<>();

	/* (non-Javadoc)
	 * @see de.hsb.smaevers.agent.model.IWorld#put(de.hsb.smaevers.agent.model.json.CellObject)
	 */
	@Override
	public void put(CellObject newCell) {
		int col = newCell.getCol();
		int row = newCell.getRow();

		if (!data.containsKey(col))
			data.put(col, new HashMap<>());

		data.get(col).put(row, newCell);
	}

	/* (non-Javadoc)
	 * @see de.hsb.smaevers.agent.model.IWorld#get(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public CellObject get(Integer col, Integer row) {
		if (data.containsKey(col))
			return data.get(col).get(row);
		return null;
	}

	/**
	 * Returns all cells that match the given predicate
	 * @param p
	 * @return
	 */
	private List<CellObject> getAllCells(Predicate<CellObject> p) {
		List<CellObject> all = new ArrayList<>();
		for (Iterator<Map<Integer, CellObject>> iterator = data.values().iterator(); iterator.hasNext();) {
			Map<Integer, CellObject> map = iterator.next();
			all.addAll(map.values());
		}

		return all.parallelStream().filter(p).collect(Collectors.toList());
	}
	
	/**
	 * Returns all neighbours which match the given predicate
	 * @param cell
	 * @param p
	 * @return
	 */
	private List<CellObject> getSuccessors(CellObject cell, Predicate<CellObject> p){
		List<CellObject> successors = new ArrayList<>();
		int row = cell.getRow();
		int col = cell.getCol();

		CellObject s = get(col - 1, row);
		if (s != null && p.test(s))
			successors.add(s);
		s = get(col + 1, row);
		if (s != null && p.test(s))
			successors.add(s);
		s = get(col, row - 1);
		if (s != null && p.test(s))
			successors.add(s);
		s = get(col, row + 1);
		if (s != null && p.test(s))
			successors.add(s);

		return successors;
	}

	/* (non-Javadoc)
	 * @see de.hsb.smaevers.agent.model.IWorld#getAccessibleSuccessors(de.hsb.smaevers.agent.model.json.CellObject)
	 */
	@Override
	public List<CellObject> getAccessibleSuccessors(CellObject cell) {
		return getSuccessors(cell, c -> c.getType().isAccessible());

	}
	
	/* (non-Javadoc)
	 * @see de.hsb.smaevers.agent.model.IWorld#getAllSuccessors(de.hsb.smaevers.agent.model.json.CellObject)
	 */
	@Override
	public List<CellObject> getAllSuccessors(CellObject cell) {
		return getSuccessors(cell, c -> true);
	}

	/* (non-Javadoc)
	 * @see de.hsb.smaevers.agent.model.IWorld#getUnvisitedCells(java.util.function.Predicate)
	 */
	@Override
	public List<CellObject> getUnvisitedCells(Predicate<CellObject> p) {
		return getAllCells(c -> p.test(c) && c.getType() == CellType.UNKOWN);
	}

	/* (non-Javadoc)
	 * @see de.hsb.smaevers.agent.model.IWorld#getCellsWithFood()
	 */
	@Override
	public List<CellObject> getCellsWithFood() {
		return getAllCells(c -> c.getFood() > 0);
	}

}
