package de.hsb.smaevers.agent.util;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.model.IWorld;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;

/**
 * A star algo to find the shortest path from a starting cell to a destination
 * cell
 * 
 * @author Stephan
 */
public class AStarAlgo {
	
	private static final Logger LOG = LoggerFactory.getLogger(AStarAlgo.class);

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
		LOG.debug("Start AStar from {} to {} and avoid traps {}", start, dest, avoidTraps);
		Deque<CellObject> path = new LinkedList<>();
		Set<AStarNode<CellObject>> closedList = new HashSet<>();
		Queue<AStarNode<CellObject>> openList = new PriorityQueue<>();
		openList.add(new AStarNode<CellObject>(start, 0));

		AStarNode<CellObject> currentNode = null;
		boolean pathFound = false;
		do {
			// get the next node with the lowest estimated distance to our
			// destination
			currentNode = openList.remove();
			// check if destination is reached
			if (currentNode.getData().equals(dest)) {
				pathFound = true;
				break;
			}

			// add node to closed list so its not expanded twice
			closedList.add(currentNode);

			// expand node
			for (CellObject cell : world.getAccessibleSuccessors(currentNode.getData())) {
				// skip dangerous cells if requested
				if (avoidTraps && (cell.isPotentialTrap() || cell.getType() == CellType.PIT)){
					LOG.debug("Skipping cell {} because its a potential trap", cell);
					continue;
				}

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

		if (pathFound){
			// construct path from the destination nodes predecessors
			do {
				path.addFirst(currentNode.getData());
				currentNode = currentNode.getPredecessor();
			} while (currentNode != null);

			// remove the first entry because its the starting cell
			path.removeFirst();
		} else {
			LOG.warn("No path found with AStar from {} to {}, something may be wrong..", start, dest);
		}
		
		return path;
	}

}
