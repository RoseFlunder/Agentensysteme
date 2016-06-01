package de.hsb.smaevers.agent.agents;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.aim.antworld.agent.AntWorldConsts;
import de.hsb.smaevers.agent.behaviours.ReceiveMessageBehaviour;
import de.hsb.smaevers.agent.model.json.LoginObject;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MyAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	
	private Logger log;

	@Override
	protected void setup() {
		super.setup();
		log = LoggerFactory.getLogger(getLocalName());
		
		log.debug("Test agent with name: {} starting", getLocalName());
		
		addBehaviour(new LoginBehaviour());
		addBehaviour(new ReceiveMessageBehaviour());
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
