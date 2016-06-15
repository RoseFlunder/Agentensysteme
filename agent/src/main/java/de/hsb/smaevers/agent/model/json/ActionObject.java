package de.hsb.smaevers.agent.model.json;

public class ActionObject {
	
	private final ActionType type;
	private final String color;
	
	public ActionObject(ActionType type, String color) {
		this.type = type;
		this.color = color;
	}
	
	public ActionType getType() {
		return type;
	}

	public String getColor() {
		return color;
	}
}
