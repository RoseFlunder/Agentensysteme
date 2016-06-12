package de.hsb.smaevers.agent.agents;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.aim.antworld.agent.AntWorldConsts;
import de.hsb.smaevers.agent.model.json.LoginObject;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MyAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	
	private Logger log;

	private AID updateTileTopic;

	@Override
	protected void setup() {
		super.setup();
		log = LoggerFactory.getLogger(getLocalName());
		
		log.debug("Test agent with name: {} starting", getLocalName());
		
		try {
			TopicManagementHelper hlp = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			updateTileTopic = hlp.createTopic(AntUiAgent.TILE_UPDATE);
			
			addBehaviour(new ReceiveMessageBehaviour());
			addBehaviour(new LoginBehaviour());
			
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			doDelete();			
		}
	}
	
	private void sendTileUpdateMessage(){
		ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
		msg.setSender(getAID());
		msg.addReceiver(updateTileTopic);
		msg.setContent("test");
		send(msg);
	}
	
	class ReceiveMessageBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null){
				log.debug(msg.toString());
				sendTileUpdateMessage();
			} else {
				block();
			}		
		}
		
	}
	
	class LoginBehaviour extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ServiceDescription filter = new ServiceDescription();
			filter.setName(AntWorldConsts.SEVICE_NAME);
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.addServices(filter);
			
			try {
				DFAgentDescription[] results = DFService.searchUntilFound(myAgent, getDefaultDF(), dfd, null, TimeUnit.MINUTES.toMillis(2));
				
				for (DFAgentDescription other : results) {
					log.debug("Found antword agent agent: {}", other.getName().getLocalName());
					
					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
					message.setSender(myAgent.getAID());
					message.addReceiver(other.getName());
					message.setLanguage("JSON");
					
					Gson gson = new Gson();
					LoginObject loginbody = new LoginObject(AntWorldConsts.ANT_COLOR_BLUE);
					message.setContent(gson.toJson(loginbody));
					myAgent.send(message);
				}
			} catch (FIPAException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
