package de.hsb.smaevers.agent.model.json;

public class ActionObject {
	
	private final ActionType type;
	private final AntColor color;
	
	public ActionObject(ActionType type, AntColor color) {
		this.type = type;
		this.color = color;
	}
	
	public ActionType getType() {
		return type;
	}

	public AntColor getColor() {
		return color;
	}
}
