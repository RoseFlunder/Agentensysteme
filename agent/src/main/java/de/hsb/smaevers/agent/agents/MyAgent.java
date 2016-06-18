package de.hsb.smaevers.agent.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import de.hsb.smaevers.agent.util.AStarAlgo;
import de.hsb.smaevers.agent.util.CellUtils;
import de.hsb.smaevers.agent.util.DistanceComparatorToRefCell;
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
	private AID updatePosition;

	private Set<CellObject> foundFoodCells = new HashSet<>();
	private Set<CellObject> potentialFoodCells = new HashSet<>();
	private Set<CellObject> potentialTrapsCells = new HashSet<>();
	private Set<CellObject> unvisitedUndangerousCells = new HashSet<>();

	private PerceptionObject lastPerception;
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
			updatePosition = hlp.createTopic(AntUiAgent.ANT_POSITION_UPDATE);

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
				log.trace(msg.toString());
				if (antworldAgent.equals(msg.getSender())) {
					try {
						PerceptionObject perception = gson.fromJson(msg.getContent(), PerceptionObject.class);
						if (perception != null) {
							gainKnowledgeFromPerception(perception);
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

	private void gainKnowledgeFromPerception(PerceptionObject perception) {
		updateWorld(perception.getCell());
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
			log.debug("Found rock at {}|{}", col, row);
			CellObject rock = new CellObject(col, row, CellType.OBSTACLE);
			potentialFoodCells.remove(rock);
			unvisitedUndangerousCells.remove(rock);
			potentialTrapsCells.remove(rock);
			updateWorld(rock);
		}
		
		//TODO: pit & food chance
		CellObject currentCell = perception.getCell();
		double pitChance = currentCell.getStench();
		
		CellObject leftCell = world.get(col - 1, row);
		if (leftCell == null){
			leftCell = new CellObject(col - 1, row, CellType.UNKOWN);
			leftCell.setPitChance(pitChance);
			updateWorld(leftCell);
			//TODO: too restrictive
			if (currentCell.getSmell() - currentCell.getFood() > 0 && currentCell.getStench() == 0){
				potentialFoodCells.add(leftCell);
			}
			if (currentCell.getStench() > 0){
				potentialTrapsCells.add(leftCell);
			}
			if (currentCell.getSmell() - currentCell.getFood() == 0 && currentCell.getStench() == 0){
				unvisitedUndangerousCells.add(leftCell);
			}
		}

		CellObject rightCell = world.get(col + 1, row);
		if (rightCell == null){
			rightCell = new CellObject(col + 1, row, CellType.UNKOWN);
			rightCell.setPitChance(pitChance);
			updateWorld(rightCell);
			
			//TODO: too restrictive
			if (currentCell.getSmell() - currentCell.getFood() > 0 && currentCell.getStench() == 0){
				potentialFoodCells.add(rightCell);
			}
			if (currentCell.getStench() > 0){
				potentialTrapsCells.add(rightCell);
			}
			if (currentCell.getSmell() - currentCell.getFood() == 0 && currentCell.getStench() == 0){
				unvisitedUndangerousCells.add(rightCell);
			}
		}

		CellObject topCell = world.get(col, row - 1);
		if (topCell == null){
			topCell = new CellObject(col, row - 1, CellType.UNKOWN);
			topCell.setPitChance(pitChance);
			updateWorld(topCell);
			
			//TODO: too restrictive
			if (currentCell.getSmell() - currentCell.getFood() > 0 && currentCell.getStench() == 0){
				potentialFoodCells.add(topCell);
			}
			if (currentCell.getStench() > 0){
				potentialTrapsCells.add(topCell);
			}
			if (currentCell.getSmell() - currentCell.getFood() == 0 && currentCell.getStench() == 0){
				unvisitedUndangerousCells.add(topCell);
			}
		}

		CellObject botCell = world.get(col, row + 1);
		if (botCell == null){
			botCell = new CellObject(col, row + 1, CellType.UNKOWN);
			botCell.setPitChance(pitChance);
			updateWorld(botCell);
			
			//TODO: too restrictive
			if (currentCell.getSmell() - currentCell.getFood() > 0 && currentCell.getStench() == 0){
				potentialFoodCells.add(botCell);
			}
			if (currentCell.getStench() > 0){
				potentialTrapsCells.add(botCell);
			}
			if (currentCell.getSmell() - currentCell.getFood() == 0 && currentCell.getStench() == 0){
				unvisitedUndangerousCells.add(botCell);
			}
		}
		
		if (currentCell.getFood() > 0){
			foundFoodCells.add(currentCell);
		} else {
			foundFoodCells.remove(currentCell);
		}
		
		potentialFoodCells.remove(currentCell);
		unvisitedUndangerousCells.remove(currentCell);
		potentialTrapsCells.remove(currentCell);
	}

	private void doNextTurn(PerceptionObject perception, String replyTo) {
		// TODO: nice logic

		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage("JSON");
		msg.setInReplyTo(replyTo);

		ActionType direction = getNextDirectionForFoodSearch(perception);
		log.debug("Next direction: {}", direction);
		ActionObject action = new ActionObject(direction, AntWorldConsts.ANT_COLOR_BLUE);

		msg.setContent(gson.toJson(action));
		msg.addReceiver(antworldAgent);

		send(msg);

		lastDirection = direction;
	}

	private ActionType getNextDirectionForFoodSearch(PerceptionObject perception) {
		CellObject currentCell = perception.getCell();

		if (!potentialFoodCells.isEmpty()) {
			log.debug("Search for path to a potential food cell");
			return getDirectionToFirstCellFromShortestPath(currentCell, potentialFoodCells);			
		}
		
		if (!unvisitedUndangerousCells.isEmpty()){
			log.debug("Search for path to an undangerous cell");
			return getDirectionToFirstCellFromShortestPath(currentCell, unvisitedUndangerousCells);	
		}
		
		if (!potentialTrapsCells.isEmpty()){
			log.debug("Search for path to a potential trap cell");
			//TODO: cannot work yet because a star ignores potential trap cells
			return getDirectionToFirstCellFromShortestPath(currentCell, potentialTrapsCells);
		}
		
		return null;
	}
	
	private ActionType getDirectionToFirstCellFromShortestPath(CellObject currentCell, Collection<CellObject> potentialCells){
		List<CellObject> cells = new ArrayList<>(potentialCells);
		Collections.sort(cells, new DistanceComparatorToRefCell(currentCell));

		Iterator<CellObject> iterator = cells.iterator();

		int shortestPathLength = 0;
		List<CellObject> options = new ArrayList<>();
		CellObject dest = null;
		do {
			dest = iterator.next();
			log.debug("Search path from {} to {}", currentCell, dest);
			
			Queue<CellObject> path = AStarAlgo.getShortestPath(currentCell, dest, world);
			if (shortestPathLength == 0 || path.size() < shortestPathLength) {
				shortestPathLength = path.size();
				options.clear();
				options.add(path.peek());
			} else if (path.size() == shortestPathLength)
				options.add(path.peek());

		} while (iterator.hasNext() && CellUtils.getHeuristicDistance(currentCell, dest) <= shortestPathLength);

		log.debug("stopped search path finding and found {} path options", options.size());
		return getDirection(currentCell, options.get(random.nextInt(options.size())));
	}

	private ActionType getDirection(CellObject from, CellObject to) {
		log.debug("Get direction from {} to {}", from, to);
		if (from.getCol() == to.getCol())
			return from.getRow() < to.getRow() ? ActionType.ANT_ACTION_DOWN : ActionType.ANT_ACTION_UP;

		return from.getCol() < to.getCol() ? ActionType.ANT_ACTION_RIGHT : ActionType.ANT_ACTION_LEFT;
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
