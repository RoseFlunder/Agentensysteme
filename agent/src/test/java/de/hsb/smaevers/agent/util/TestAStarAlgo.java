package de.hsb.smaevers.agent.util;

import java.util.Queue;

import org.junit.Test;

import de.hsb.smaevers.agent.model.IWorld;
import de.hsb.smaevers.agent.model.World;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;

public class TestAStarAlgo {

	@Test
	public void testAStar(){
		IWorld world = new World();
		
		world.put(new CellObject(0, 0, CellType.FREE));
		world.put(new CellObject(1, 0, CellType.FREE));
		world.put(new CellObject(2, 0, CellType.FREE));
		world.put(new CellObject(0, 1, CellType.FREE));
		world.put(new CellObject(1, 1, CellType.OBSTACLE));
		world.put(new CellObject(2, 1, CellType.FREE));
		world.put(new CellObject(0, 2, CellType.FREE));
		world.put(new CellObject(1, 2, CellType.OBSTACLE));
		world.put(new CellObject(2, 2, CellType.FREE));
		
		Queue<CellObject> shortestPath = AStarAlgo.getShortestPath(world.get(0, 2), world.get(2, 2), world);
		
		for (CellObject cellObject : shortestPath) {
			System.out.println(cellObject);
		}
		
		
	}
	
}
