package de.hsb.smaevers.agent.util;

import de.hsb.smaevers.agent.model.json.CellObject;

public final class CellUtils {

	public static int getHeuristicDistance(CellObject c1, CellObject c2) throws IllegalArgumentException {
		if (c1 == null || c2 == null)
			throw new IllegalArgumentException("cell objects must not be null");
		
		return Math.abs(c1.getCol() - c2.getCol()) + Math.abs(c1.getRow() - c2.getRow());
	}
	
}
