package de.hsb.smaevers.agent.agents;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.hsb.smaevers.agent.model.json.CellObject;
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

			topicUpdate = hlp.createTopic(getArguments()[1].toString());
			hlp.register(topicUpdate);

			topicPosition = hlp.createTopic(getArguments()[2].toString());
			hlp.register(topicPosition);

			addBehaviour(new ReceivePosititionMessages(this));
			addBehaviour(new ReceiveCellMessages(this));

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
	
	class ReceiveCellMessages extends CyclicBehaviour {
		public ReceiveCellMessages(AntUiAgent agent) {
			super(agent);
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			MessageTemplate updateTemplate = MessageTemplate.MatchTopic(topicUpdate);
			ACLMessage msg = null;
			while ((msg = receive(updateTemplate)) != null) {
				LOG.trace("update message received");
				CellObject cell = gson.fromJson(msg.getContent(), CellObject.class);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						antClientUi.updateCell(cell);
					}
				});
				LOG.trace(msg.toString());
			}
			block();
		}
	}

	class ReceivePosititionMessages extends CyclicBehaviour {

		public ReceivePosititionMessages(AntUiAgent agent) {
			super(agent);
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			MessageTemplate updatePositoin = MessageTemplate.MatchTopic(topicPosition);
			ACLMessage msgPosUpdate = receive(updatePositoin);
			if (msgPosUpdate != null) {
				LOG.trace("position update received");
				CellObject cell = gson.fromJson(msgPosUpdate.getContent(), CellObject.class);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						antClientUi.updateAntPosition(cell, msgPosUpdate.getSender());
					}
				});
				LOG.trace(msgPosUpdate.toString());
			} else {
				block();
			}
		}

	}

	@Override
	protected void onGuiEvent(GuiEvent ev) {
		switch (ev.getType()) {
		case EVENT_CLOSE:
			// send message to all ants to log off
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
