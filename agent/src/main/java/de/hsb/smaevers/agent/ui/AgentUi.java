package de.hsb.smaevers.agent.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsb.smaevers.agent.agents.AntUiAgent;
import jade.gui.GuiEvent;

public class AgentUi {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private final JFrame frame;
	private final AntUiAgent agent;
	
	public AgentUi(final AntUiAgent agent){
		this.agent = agent;
		
		LOG.debug("Constructor of UI called");
		
		frame = new JFrame("Antworld Client UI");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				LOG.debug("Closing GUI and sending close event to gui agent");
				GuiEvent event = new GuiEvent(frame, AntUiAgent.EVENT_CLOSE);
				agent.postGuiEvent(event);
				frame.dispose();
			}
		});
		
		frame.setLayout(new BorderLayout());
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}

}
