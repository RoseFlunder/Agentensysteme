package de.hsb.smaevers.agent.model;

import java.util.Objects;

public class Tile {
	
	private TileType type;
	private int x;
	private int y;
	
	public Tile(int x, int y){
		this(x, y, TileType.UNKOWN);
	}
	
	public Tile(int x, int y, TileType type){
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public TileType getType() {
		return type;
	}

	public void setType(TileType type) {
		this.type = type;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Tile){
			Tile t = (Tile) obj;
			return this.x == t.x && this.y == t.y;
		}
		
		return false;
	}
}
