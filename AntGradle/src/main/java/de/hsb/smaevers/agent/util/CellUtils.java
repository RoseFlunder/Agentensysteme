package de.hsb.smaevers.agent.util;

import de.hsb.smaevers.agent.model.json.CellObject;

/**
 * Just a class containing some static utility methods for cell objects
 * @author Stephan
 */
public final class CellUtils {

	/**
	 * Calculates the manhatten distance between the two given cells
	 * @param c1 first cell
	 * @param c2 second cell
	 * @return manhatten distance
	 * @throws IllegalArgumentException when one of the arguments is null
	 */
	public static int getHeuristicDistance(CellObject c1, CellObject c2) throws IllegalArgumentException {
		if (c1 == null || c2 == null)
			throw new IllegalArgumentException("cell objects must not be null");
		
		return Math.abs(c1.getCol() - c2.getCol()) + Math.abs(c1.getRow() - c2.getRow());
	}
	
}
