package de.hsb.smaevers.agent.model.json;

public class CellObject {
	
	private int row;
	private int col;
	private CellType type;
	private int food;
	private int smell;
	private int stench;
	private String[] ants;
	
	public CellObject(){
		
	}
	
	public CellObject(int col, int row, CellType type){
		this.col = col;
		this.row = row;
		this.type = type;
	}
	
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public CellType getType() {
		return type;
	}
	public void setType(CellType type) {
		this.type = type;
	}
	public int getFood() {
		return food;
	}
	public void setFood(int food) {
		this.food = food;
	}
	public int getSmell() {
		return smell;
	}
	public void setSmell(int smell) {
		this.smell = smell;
	}
	public int getStench() {
		return stench;
	}
	public void setStench(int stench) {
		this.stench = stench;
	}
	public String[] getAnts() {
		return ants;
	}
	public void setAnts(String[] ants) {
		this.ants = ants;
	}

}
