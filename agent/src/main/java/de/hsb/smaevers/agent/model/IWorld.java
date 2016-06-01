package de.hsb.smaevers.agent.model;

public interface IWorld {
	
	public void put(Integer x, Integer y, Tile tile);

	public Tile get(Integer x, Integer y);
	
}
