package de.hsb.smaevers.agent.behaviours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ReceiveMessageBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;
	
	private Logger LOG;
	
	@Override
	public void action() {
		
		ACLMessage msg = myAgent.receive();
		
		if (msg != null){
			LOG.debug("message from {}", msg.getSender().getLocalName());
			LOG.debug(msg.toString());
		} else {
			block();
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		LOG = LoggerFactory.getLogger(myAgent.getLocalName());
	}

}
