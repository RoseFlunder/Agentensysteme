package de.hsb.smaevers.agent.model.json;

public enum CellType {
	
	FREE(true),
	OBSTACLE(false),
	PIT(false),
	START(true),
	UNKOWN(true);

	
	private final boolean accessible;
	
	private CellType(boolean accessible){
		this.accessible = accessible;
	}

	public boolean isAccessible() {
		return accessible;
	}
}
