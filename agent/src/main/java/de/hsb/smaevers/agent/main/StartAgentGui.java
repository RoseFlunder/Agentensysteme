package de.hsb.smaevers.agent.main;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import de.hsb.smaevers.agent.agents.MyAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class StartAgentGui {
	
	private static final Logger LOG = LoggerFactory.getLogger(StartAgentGui.class);
	
	private static Properties getProperties(){
		Properties props = new Properties();
		try {
			props.load(StartAgentGui.class.getClassLoader().getResourceAsStream("connection.properties"));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return props;
	}

	public static void main(String[] args){
		LOG.debug("Application starting..");
		
		Properties props = getProperties();
		
		String host = props.getProperty("host", "localhost");
		LOG.debug("IP: {}", host);
		int port = -1;
		
		try {
			port = Integer.parseInt(props.getProperty("port", "-1"));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		LOG.debug("Port: {}", port);
		
		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl(host, port, null, false);
		profile.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService");
		
		AgentContainer container = runtime.createAgentContainer(profile);
		
		try {
			AgentController agentController = container.createNewAgent("RoseFlunder" + UUID.randomUUID().toString(), MyAgent.class.getName(), args);
			agentController.start();
			
			AgentController guiAgentController = container.createNewAgent("Gui Agent", AntUiAgent.class.getName(), args);
			guiAgentController.start();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
