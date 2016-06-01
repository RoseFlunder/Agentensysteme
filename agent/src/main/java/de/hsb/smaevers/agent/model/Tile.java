package de.hsb.smaevers.agent.model;

public class Tile {
	
	private TileType type;
	
	public Tile(){
		this(TileType.UNKOWN);
	}
	
	public Tile(TileType type){
		this.type = type;
	}

	public TileType getType() {
		return type;
	}

	public void setType(TileType type) {
		this.type = type;
	}

}
