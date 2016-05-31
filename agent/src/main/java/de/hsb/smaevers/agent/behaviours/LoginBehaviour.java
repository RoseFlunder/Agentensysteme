package de.hsb.smaevers.agent.behaviours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.aim.antworld.agent.AntWorldConsts;
import de.hsb.smaevers.agent.messages.LoginMessage;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class LoginBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 1L;
	
	private Logger LOG;
	
	@Override
	public void action() {
		ServiceDescription filter = new ServiceDescription();
		filter.setName(AntWorldConsts.SEVICE_NAME);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(filter);
		
		try {
			DFAgentDescription[] results = DFService.search(myAgent, dfd);
			
			for (DFAgentDescription other : results) {
				LOG.debug("Agent {} found antword agent agent: {}", myAgent.getLocalName(), other.getName().getLocalName());
				
				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.setSender(myAgent.getAID());
				message.addReceiver(other.getName());
				message.setLanguage("JSON");
				
				Gson gson = new Gson();
				LoginMessage loginbody = new LoginMessage(AntWorldConsts.ANT_COLOR_BLUE);
				message.setContent(gson.toJson(loginbody));
				
				LOG.debug("Try to send a login request with content {}", message.getContent());
				
				myAgent.send(message);
			}
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		LOG = LoggerFactory.getLogger(myAgent.getLocalName());
	}

}
