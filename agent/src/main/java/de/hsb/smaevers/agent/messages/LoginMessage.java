package de.hsb.smaevers.agent.messages;

import de.aim.antworld.agent.AntWorldConsts;

public class LoginMessage {
	
	private final String type = AntWorldConsts.ANT_ACTION_LOGIN;
	private String color;
	
	public LoginMessage(){
	}
	
	public LoginMessage(String color) {
		this.color = color;
	}
	
	public String getType() {
		return type;
	}

	public String getColor() {
		return color;
	}
}
