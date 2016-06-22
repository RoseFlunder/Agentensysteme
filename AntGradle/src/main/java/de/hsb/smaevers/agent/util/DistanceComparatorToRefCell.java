package de.hsb.smaevers.agent.util;

import java.util.Comparator;

import de.hsb.smaevers.agent.model.json.CellObject;

/**
 * Comparator that compares two cell objects usings its manhatten distance to a
 * given reference cell
 * 
 * @author Stephan
 */
public class DistanceComparatorToRefCell implements Comparator<CellObject> {

	private CellObject referenceCell;

	public DistanceComparatorToRefCell(CellObject referenceCell) {
		this.referenceCell = referenceCell;
	}

	private int getHeuristicDistance(CellObject cell) {
		return CellUtils.getHeuristicDistance(referenceCell, cell);
	}

	@Override
	public int compare(CellObject o1, CellObject o2) {
		if (o1 != null && o2 != null) {
			return Integer.compare(getHeuristicDistance(o1), getHeuristicDistance(o2));
		}
		if (o1 == null)
			return 1;
		return -1;
	}

}
