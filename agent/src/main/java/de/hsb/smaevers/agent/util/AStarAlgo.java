package de.hsb.smaevers.agent.util;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import de.hsb.smaevers.agent.model.IWorld;
import de.hsb.smaevers.agent.model.json.CellObject;

/**
 * A star algo to find the shortest path from a starting cell to a destination
 * cell
 * 
 * @author Stephan
 */
public class AStarAlgo {

	/**
	 * Retrieves the shortest path
	 * 
	 * @param start
	 *            Start cell
	 * @param dest
	 *            Destination cell
	 * @param world
	 *            World map to get eachs cells successors
	 * @param avoidTraps
	 *            Flag if potential traps should be avoided
	 * @return
	 */
	public static Queue<CellObject> getShortestPath(CellObject start, CellObject dest, IWorld world,
			boolean avoidTraps) {
		Deque<CellObject> path = new LinkedList<>();
		Set<AStarNode<CellObject>> closedList = new HashSet<>();
		Queue<AStarNode<CellObject>> openList = new PriorityQueue<>();
		openList.add(new AStarNode<CellObject>(start, 0));

		AStarNode<CellObject> currentNode = null;
		do {
			// get the next node with the lowest estimated distance to our
			// destination
			currentNode = openList.remove();
			// check if destination is reached
			if (currentNode.getData().equals(dest)) {
				break;
			}

			// add node to closed list so its not expanded twice
			closedList.add(currentNode);

			// expand node
			for (CellObject cell : world.getAccessibleSuccessors(currentNode.getData())) {
				// skip dangerous cells if requested
				if (cell.isPotentialTrap() && avoidTraps)
					continue;

				int hSuc = CellUtils.getHeuristicDistance(start, cell);
				AStarNode<CellObject> suc = new AStarNode<CellObject>(cell, hSuc);
				if (closedList.contains(suc))
					continue;
				int tentativeG = currentNode.getG() + 1;

				// this is not shorter
				if (openList.contains(suc) && tentativeG > suc.getG())
					continue;

				suc.setPredecessor(currentNode);
				suc.setG(tentativeG);

				if (openList.contains(suc)) {
					openList.remove(suc);
				}
				openList.add(suc);
			}
		} while (!openList.isEmpty());

		// construct path from the destination nodes predecessors
		do {
			path.addFirst(currentNode.getData());
			currentNode = currentNode.getPredecessor();
		} while (currentNode != null);

		// remove the first entry because its the starting cell
		path.removeFirst();
		return path;
	}

}
