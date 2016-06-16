package de.hsb.smaevers.agent.util;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import de.hsb.smaevers.agent.model.IWorld;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;

public class AStarAlgo {
	
	public static Queue<CellObject> getShortestPath(CellObject start, CellObject dest, IWorld world){
		Deque<CellObject> path = new LinkedList<>();
		Set<AStarNode<CellObject>> closedList = new HashSet<>();
		Queue<AStarNode<CellObject>> openList = new PriorityQueue<>();
		openList.add(new AStarNode<CellObject>(start, 0));
		
		AStarNode<CellObject> currentNode = null;
		do {
			currentNode = openList.remove();
			if (currentNode.getData().equals(dest)){
				break;
			}
				
			closedList.add(currentNode);
			
			//expand node
			for (CellObject cell : world.getKnownSuccessors(currentNode.getData())) {
				if (cell.getType() != CellType.FREE && cell.getType() != CellType.START)
					continue;
				int hSuc = CellUtils.getHeuristicDistance(start, cell);
				AStarNode<CellObject> suc = new AStarNode<CellObject>(cell, hSuc);
				if (closedList.contains(suc))
					continue;
				int tentativeG = currentNode.getG() + 1;
				
				if (openList.contains(suc) && tentativeG > suc.getG())
					continue;
				
				suc.setPredecessor(currentNode);
				suc.setG(tentativeG);
				
				if (openList.contains(suc)){
					openList.remove(suc);
				}
				openList.add(suc);
			}
		} while (!openList.isEmpty());
		
		do {
			path.addFirst(currentNode.getData());
			currentNode = currentNode.getPredecessor();
		} while (currentNode != null);
		
		return path;
	}

}
