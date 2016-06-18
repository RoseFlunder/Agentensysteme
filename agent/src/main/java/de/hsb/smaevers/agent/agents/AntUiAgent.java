package de.hsb.smaevers.agent.agents;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.PerceptionObject;
import de.hsb.smaevers.agent.ui.AntClientUi;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;

/**
 * Agent that is only responsible for the GUI.
 * 
 * @author Stephan
 */
public class AntUiAgent extends jade.gui.GuiAgent {

	private static final long serialVersionUID = 1L;
	
	public static final String TILE_UPDATE = "TILE_UPDATE";
	public static final String ANT_POSITION_UPDATE = "ANT_POSITION_UPDATE";
	
	public static final int EVENT_CLOSE = 0;
	
	
	private final Logger LOG = LoggerFactory.getLogger(GuiAgent.class);
	
	private final Gson gson = new Gson();
	private AntClientUi antClientUi;

	private AID topicUpdate;
	private AID topicPosition;
	
	@Override
	protected void setup() {
		super.setup();
		
		try {
			TopicManagementHelper hlp = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			
			topicUpdate = hlp.createTopic(TILE_UPDATE);
			hlp.register(topicUpdate);
			
			topicPosition = hlp.createTopic(ANT_POSITION_UPDATE);
			hlp.register(topicPosition);
			
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
			MessageTemplate updateTemplate = MessageTemplate.MatchTopic(topicUpdate);
			ACLMessage msg = receive(updateTemplate);
			if (msg != null){
				LOG.debug("update message received");
				CellObject cell = gson.fromJson(msg.getContent(), CellObject.class);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						antClientUi.updateCell(cell);
					}
				});
				LOG.trace(msg.toString());
			}
			
			MessageTemplate updatePositoin = MessageTemplate.MatchTopic(topicPosition);
			msg = receive(updatePositoin);
			if (msg != null){
				LOG.debug("position update received");
				
			}
			
			if (msg == null)
				block();
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
