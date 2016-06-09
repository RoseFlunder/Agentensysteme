package de.hsb.smaevers.agent.agents;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.ui.AgentUi;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
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
	
	private AgentUi agentUi;
	
	@Override
	protected void setup() {
		super.setup();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				LOG.debug("create agent UI");
				agentUi = new AgentUi(AntUiAgent.this);				
			}
		});
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
