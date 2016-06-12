package de.hsb.smaevers.agent.agents;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.ui.AntClientUi;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;

/**
 * Agent that is only responsible for the GUI.
 * 
 * @author Stephan
 */
public class AntUiAgent extends jade.gui.GuiAgent {

	private static final long serialVersionUID = 1L;
	
	public static final String TILE_UPDATE = "TILE_UPDATE";
	
	public static final int EVENT_CLOSE = 0;
	
	
	private final Logger LOG = LoggerFactory.getLogger(GuiAgent.class);
	
	private AntClientUi antClientUi;
	
	@Override
	protected void setup() {
		super.setup();
		
		try {
			TopicManagementHelper hlp = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			
			AID topic = hlp.createTopic(TILE_UPDATE);
			hlp.register(topic);
			
			addBehaviour(new ReceiveMessages(this));			
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					LOG.debug("create agent UI");
					antClientUi = new AntClientUi(AntUiAgent.this);				
				}
			});
		} catch (ServiceException e) {
			LOG.error(e.getMessage(), e);
		}
		
		
	}
	
	class ReceiveMessages extends CyclicBehaviour {
		
		public ReceiveMessages(AntUiAgent agent) {
			super(agent);
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {			
			ACLMessage msg = receive();
			if (msg != null){
				LOG.debug(msg.toString());
			} else {
				block();
			}
		}
		
	}


	@Override
	protected void onGuiEvent(GuiEvent ev) {
		switch (ev.getType()) {
		case EVENT_CLOSE:
			//send message to all ants to log off
			doDelete();
			LOG.debug("Called GUI agent delete");
			
			try {
				getContainerController().kill();
			} catch (StaleProxyException e) {
				LOG.error(e.getMessage(), e);
			}
			break;

		default:
			break;
		}
	}
}
