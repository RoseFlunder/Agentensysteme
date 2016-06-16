package de.hsb.smaevers.agent.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.aim.antworld.agent.AntWorldConsts;
import de.hsb.smaevers.agent.model.IWorld;
import de.hsb.smaevers.agent.model.World;
import de.hsb.smaevers.agent.model.json.ActionObject;
import de.hsb.smaevers.agent.model.json.ActionType;
import de.hsb.smaevers.agent.model.json.CellObject;
import de.hsb.smaevers.agent.model.json.CellType;
import de.hsb.smaevers.agent.model.json.PerceptionObject;
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
	private IWorld world = new World();

	private AID antworldAgent;
	private AID updateWorldTopic;
	
	private Set<CellObject> potentialFoodCells = new HashSet<>();
	private Set<CellObject> potentialTrapsCells = new HashSet<>();
	private Set<CellObject> unvisitedUndangerousCells = new HashSet<>();

	private PerceptionObject lastPerception;
	private ActionObject lastAction;
	private ActionType lastDirection;

	private Random random = new Random();
	private Gson gson = new Gson();

	@Override
	protected void setup() {
		super.setup();
		log = LoggerFactory.getLogger(getLocalName());

		log.debug("Test agent with name: {} starting", getLocalName());

		try {
			TopicManagementHelper hlp = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			updateWorldTopic = hlp.createTopic(AntUiAgent.TILE_UPDATE);

			addBehaviour(new ReceiveMessageBehaviour());
			addBehaviour(new LoginBehaviour());

		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			doDelete();
		}
	}

	private void updateWorld(CellObject cell) {
		world.put(cell);
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setLanguage("JSON");
		msg.addReceiver(updateWorldTopic);
		msg.setContent(gson.toJson(cell));
		send(msg);
	}

	class ReceiveMessageBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				log.debug(msg.toString());
				if (antworldAgent.equals(msg.getSender())) {
					try {
						PerceptionObject perception = gson.fromJson(msg.getContent(), PerceptionObject.class);
						if (perception != null) {
							updateWorld(perception.getCell());

							doNextTurn(perception, msg.getReplyWith());
						}

						lastPerception = perception;
					} catch (JsonSyntaxException e) {
						log.error(e.getMessage(), e);
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

			} else {
				block();
			}

		}

	}

	private void doNextTurn(PerceptionObject perception, String replyTo) {
		// TODO: nice logic

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage("JSON");
		msg.setInReplyTo(replyTo);

		List<CellObject> neighbours = getNeighbours(perception.getCell());
		if (lastPerception != null)
			neighbours.remove(lastPerception.getCell());
		
		// move
		if (perception.getCell().getSmell() > 0){
			potentialFoodCells.addAll(neighbours);
		}
		if (perception.getCell().getStench() > 0){
			potentialTrapsCells.addAll(neighbours);
		}
		if (perception.getCell().getStench() == 0 && perception.getCell().getSmell() == 0){
			unvisitedUndangerousCells.addAll(neighbours);
		}
		
		ActionType direction = getNextDirectionForFoodSearch(perception);
		ActionObject action = new ActionObject(direction, AntWorldConsts.ANT_COLOR_BLUE);

		msg.setContent(gson.toJson(action));
		msg.addReceiver(antworldAgent);

		send(msg);

		lastDirection = direction;
		lastAction = action;
	}
	
	private List<CellObject> getNeighbours(CellObject cell){
		List<CellObject> list = new ArrayList<>();
		int row = cell.getRow();
		int col = cell.getCol();
		
		CellType type = CellType.UNKOWN;
		if (cell.getStench() == 3)
			type = CellType.PIT;
		
		CellObject leftCell = world.get(col - 1, row);
		if (leftCell == null)
			leftCell = createCellAndUpdateWorld(col - 1, row, type);
		list.add(leftCell);
		
		CellObject rightCell = world.get(col + 1, row);
		if (rightCell == null)
			rightCell = createCellAndUpdateWorld(col + 1, row, type);
		list.add(rightCell);
		
		CellObject topCell = world.get(col, row - 1);
		if (topCell == null)
			topCell = createCellAndUpdateWorld(col, row - 1, type);
		list.add(topCell);
		
		CellObject botCell = world.get(col, row + 1);
		if (botCell == null)
			botCell = createCellAndUpdateWorld(col, row + 1, type);
		list.add(botCell);
		
		return list;
	}
	
	private CellObject createCellAndUpdateWorld(int col, int row, CellType type){
		CellObject cell = new CellObject(col, row, type);
		updateWorld(cell);
		return cell;
	}

	private ActionType getNextDirectionForFoodSearch(PerceptionObject perception) {
		List<ActionType> possibleDirections = new ArrayList<>(ActionType.directions);
		int row = perception.getCell().getRow();
		int col = perception.getCell().getCol();
		
		// found rock
		if (!hasMoved(perception)) {
			switch (lastDirection) {
			case ANT_ACTION_UP:
				--row;
				break;
			case ANT_ACTION_DOWN:
				++row;
				break;
			case ANT_ACTION_LEFT:
				--col;
				break;
			case ANT_ACTION_RIGHT:
				++col;
				break;

			default:
				break;
			}
			CellObject rock = new CellObject(col, row, CellType.OBSTACLE);
			updateWorld(rock);
		}
		
		//remove directions of known fields
//		if (world.get(col + 1, row) != null)
//			possibleDirections.remove(ActionType.ANT_ACTION_RIGHT);
//		if (world.get(col - 1, row) != null)
//			possibleDirections.remove(ActionType.ANT_ACTION_LEFT);
//		if (world.get(col, row + 1) != null)
//			possibleDirections.remove(ActionType.ANT_ACTION_DOWN);
//		if (world.get(col, row - 1) != null)
//			possibleDirections.remove(ActionType.ANT_ACTION_UP);
		
		if (!possibleDirections.isEmpty())
			return possibleDirections.get(random.nextInt(possibleDirections.size()));
		
		//go back, this area is already explored
		return ActionType.getReverseDirection(lastDirection);
	}

	private boolean hasMoved(PerceptionObject currentPerception) {
		if (lastPerception != null && lastPerception.getCell() != null && currentPerception != null
				&& currentPerception.getCell() != null) {
			return lastPerception.getCell().getRow() != currentPerception.getCell().getRow()
					|| lastPerception.getCell().getCol() != currentPerception.getCell().getCol();
		}
		return true;
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
				DFAgentDescription[] results = DFService.searchUntilFound(myAgent, getDefaultDF(), dfd, null,
						TimeUnit.MINUTES.toMillis(2));

				for (DFAgentDescription other : results) {
					log.debug("Found antword agent agent: {}", other.getName().getLocalName());
					antworldAgent = other.getName();

					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
					message.setSender(myAgent.getAID());
					message.addReceiver(other.getName());
					message.setLanguage("JSON");

					Gson gson = new Gson();
					ActionObject loginbody = new ActionObject(ActionType.ANT_ACTION_LOGIN,
							AntWorldConsts.ANT_COLOR_BLUE);
					message.setContent(gson.toJson(loginbody));
					myAgent.send(message);
				}
			} catch (FIPAException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
