package de.hsb.smaevers.agent.model.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum ActionType {

	ANT_ACTION_LOGIN, ANT_ACTION_VOID, ANT_ACTION_COLLECT, ANT_ACTION_DROP,

	ANT_ACTION_UP, ANT_ACTION_DOWN, ANT_ACTION_LEFT, ANT_ACTION_RIGHT;

	public static final List<ActionType> directions = Collections
			.unmodifiableList(Arrays.asList(ANT_ACTION_UP, ANT_ACTION_DOWN, ANT_ACTION_LEFT, ANT_ACTION_RIGHT));

	public static ActionType getReverseDirection(ActionType direction){
		if (direction != null){
			switch (direction) {
			case ANT_ACTION_UP:
				return ANT_ACTION_DOWN;
				
			case ANT_ACTION_DOWN:
				return ANT_ACTION_UP;
				
			case ANT_ACTION_LEFT:
				return ANT_ACTION_RIGHT;
				
			case ANT_ACTION_RIGHT:
				return ANT_ACTION_LEFT;

			default:
				break;
			}
		}
		
		return null;
	}
}
