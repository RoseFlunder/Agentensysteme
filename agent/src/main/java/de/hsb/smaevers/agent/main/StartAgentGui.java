package de.hsb.smaevers.agent.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.TestAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class StartAgentGui {
	
	private static final Logger LOG = LoggerFactory.getLogger(StartAgentGui.class);

	public static void main(String[] args) throws StaleProxyException {
		LOG.debug("Application starting..");
		
		
		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl("localhost", -1, null, false);
		
		AgentContainer container = runtime.createAgentContainer(profile);
		
		AgentController agentController = container.createNewAgent("testAgent", TestAgent.class.getName(), args);
		agentController.start();
	}

}
