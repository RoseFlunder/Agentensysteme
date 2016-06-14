package de.hsb.smaevers.agent.model.json;

public class ActionObject {
	
	private final String type;
	private final String color;
	
	public ActionObject(String type, String color) {
		this.type = type;
		this.color = color;
	}
	
	public String getType() {
		return type;
	}

	public String getColor() {
		return color;
	}
}
