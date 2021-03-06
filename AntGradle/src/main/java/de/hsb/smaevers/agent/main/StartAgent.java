package de.hsb.smaevers.agent.main;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import de.hsb.smaevers.agent.agents.MyAgent;
import de.hsb.smaevers.agent.model.json.AntColor;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

/**
 * Main Class to start the application.
 * Configuration can be done via connection.properties file
 * 
 * @author Stephan
 *
 */
public class StartAgent {
	
	private static final Logger LOG = LoggerFactory.getLogger(StartAgent.class);
	
	private static Properties getProperties(){
		Properties props = new Properties();
		try {
			props.load(StartAgent.class.getClassLoader().getResourceAsStream("connection.properties"));
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return props;
	}
	
	private static AntColor getColor(Properties props){
		String colorProp = props.getProperty("color");
		try {
			return AntColor.valueOf(colorProp);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return AntColor.ANT_COLOR_BLUE;
		}
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
		
		Object[] agentArgs = new Object[3];
		agentArgs[0] = getColor(props);
		//topic cell update
		agentArgs[1] = props.getProperty("topic_cell_update", "topic_cell_update");
		//topic ant position update
		agentArgs[2] = props.getProperty("topic_position_update", "topic_position_update");
		
		LOG.debug("Topic for cell update: {}", agentArgs[1]);
		LOG.debug("Topic for position update: {}", agentArgs[2]);
		
		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl(host, port, null, false);
		profile.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService");
		
		AgentContainer container = runtime.createAgentContainer(profile);
		
		try {
			int numberOfAnts = 1;
			try {
				numberOfAnts = Integer.parseInt(props.getProperty("number_ants", "1"));
			} catch (NumberFormatException e) {
				LOG.error("Cannot parse the number of ants from the properties file", e);
			}
			
			for (int i = 0; i < numberOfAnts; ++i){
				AgentController agentController = container.createNewAgent("RoseFlunder" + UUID.randomUUID().toString(), MyAgent.class.getName(), agentArgs);
				agentController.start();
			}
			
			if (Boolean.valueOf(props.getProperty("start_gui", "false"))){
				AgentController guiAgentController = container.createNewAgent("Gui Agent", AntUiAgent.class.getName(), agentArgs);
				guiAgentController.start();
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
