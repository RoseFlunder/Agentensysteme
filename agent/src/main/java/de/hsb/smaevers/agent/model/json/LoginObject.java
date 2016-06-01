package de.hsb.smaevers.agent.model.json;

import de.aim.antworld.agent.AntWorldConsts;

public class LoginObject {
	
	private final String type = AntWorldConsts.ANT_ACTION_LOGIN;
	private String color;
	
	public LoginObject(){
	}
	
	public LoginObject(String color) {
		this.color = color;
	}
	
	public String getType() {
		return type;
	}

	public String getColor() {
		return color;
	}
}
