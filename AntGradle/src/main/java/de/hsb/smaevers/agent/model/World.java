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

	@Override
	public void put(CellObject newCell) {
		int col = newCell.getCol();
		int row = newCell.getRow();

		if (!data.containsKey(col))
			data.put(col, new HashMap<>());

		data.get(col).put(row, newCell);
	}

	@Override
	public CellObject get(Integer col, Integer row) {
		if (data.containsKey(col))
			return data.get(col).get(row);
		return null;
	}

	private List<CellObject> getAllCells(Predicate<CellObject> p) {
		List<CellObject> all = new ArrayList<>();
		for (Iterator<Map<Integer, CellObject>> iterator = data.values().iterator(); iterator.hasNext();) {
			Map<Integer, CellObject> map = iterator.next();
			all.addAll(map.values().parallelStream().filter(p).collect(Collectors.toList()));
		}

		return all;
	}
	
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

	@Override
	public List<CellObject> getAccessibleSuccessors(CellObject cell) {
		return getSuccessors(cell, c -> c.getType().isAccessible());

	}
	
	@Override
	public List<CellObject> getAllSuccessors(CellObject cell) {
		return getSuccessors(cell, c -> true);
	}

	@Override
	public List<CellObject> getUnvisitedCells(Predicate<CellObject> p) {
		return getAllCells(c -> p.test(c) && c.getType() == CellType.UNKOWN);
	}

	@Override
	public List<CellObject> getCellsWithFood() {
		return getAllCells(c -> c.getFood() > 0);
	}

}
