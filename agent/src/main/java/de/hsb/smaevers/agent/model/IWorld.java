package de.hsb.smaevers.agent.model;

public interface IWorld {
	
	public void put(Tile tile);

	public Tile get(Integer x, Integer y);
	
	public Iterable<Tile> getAllTiles();
	
}
