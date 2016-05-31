package de.hsb.smaevers.agent.agents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.behaviours.LoginBehaviour;
import de.hsb.smaevers.agent.behaviours.ReceiveMessageBehaviour;
import jade.core.Agent;

public class TestAgent extends Agent {
	
	private Logger LOG;

	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {
		super.setup();
		LOG = LoggerFactory.getLogger(getLocalName());
		
		LOG.debug("Test agent with name: {} starting", getLocalName());
		
		addBehaviour(new LoginBehaviour());
		addBehaviour(new ReceiveMessageBehaviour());
	}
}
