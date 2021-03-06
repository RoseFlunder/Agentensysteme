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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.hsb.smaevers.agent.model.IWorld;
import de.hsb.smaevers.agent.model.World;
import de.hsb.smaevers.agent.model.json.ActionObject;
import de.hsb.smaevers.agent.model.json.ActionType;
import de.hsb.smaevers.agent.model.json.AntColor;
import de.hsb.smaevers.agent.model.json.AntState;
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
import jade.lang.acl.MessageTemplate;

/**
 * Class for an ant agent which explores the map and collects food.
 * 
 * @author Stephan
 */
public class MyAgent extends Agent {

	private static final String JSON = "JSON";

	private static final long serialVersionUID = 1L;

	private static final int INCREMENT_RADIUS = 5;
	private int searchRadius = 5;

	private Logger log;
	private IWorld world = new World();
	private CellObject start;

	private MessageTemplate updateTemplate;
	private MessageTemplate otherMessages;

	private AID antworldAgent;
	private AID updateWorldTopic;
	private AID updatePositionTopic;

	private PerceptionObject lastPerception;

	private Random random = new Random();
	private Gson gson = new Gson();
	private AntColor color;

	@Override
	protected void setup() {
		super.setup();
		log = LoggerFactory.getLogger(getLocalName());

		log.debug("Ant agent with name: {} starting", getLocalName());

		Object arg0 = getArguments()[0];
		if (arg0 != null && arg0 instanceof AntColor)
			this.color = (AntColor) arg0;

		try {
			// create topics
			TopicManagementHelper hlp = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			updateWorldTopic = hlp.createTopic(getArguments()[1].toString());
			updatePositionTopic = hlp.createTopic(getArguments()[2].toString());
			hlp.register(updateWorldTopic);

			updateTemplate = MessageTemplate.MatchTopic(updateWorldTopic);

			// add behaviours
			addBehaviour(new ReceiveUpdateMessageBehaviour());
			addBehaviour(new ReceiveMessageBehaviour());
			addBehaviour(new LoginBehaviour());
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			doDelete();
		}
	}

	/**
	 * Updates the local world map with the given cell and sends the cell to all
	 * other agents
	 * 
	 * @param cell Cell which will be put into the map and sent to the others
	 */
	private void updateWorldAndPropagteToOthers(CellObject cell) {
		world.put(cell);

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setLanguage(JSON);
		msg.addReceiver(updateWorldTopic);
		msg.setContent(gson.toJson(cell));
		send(msg);

		log.trace("Updated world and sent this cell to the other ants {}, cell");
	}

	/**
	 * Sends the current position of this ant to all other ants
	 * 
	 * @param perception current Perception
	 */
	private void updateAntPosition(PerceptionObject perception) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setLanguage(JSON);
		msg.addReceiver(updatePositionTopic);
		msg.setContent(gson.toJson(perception));
		send(msg);
		log.trace("Sent current perception to gui");
	}

	/**
	 * Behaviour the receive update messages from other ants
	 * 
	 * @author Stephan
	 */
	class ReceiveUpdateMessageBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ACLMessage msgUpdate = null;
			Queue<ACLMessage> messageQueue = new LinkedList<>();
			while ((msgUpdate = receive(updateTemplate)) != null) {
				if (!msgUpdate.getSender().equals(getAID())) {
					log.trace("update message received: {}", msgUpdate);
					messageQueue.add(msgUpdate);
				}
			}

			// iterate over messages and update the GUI
			for (ACLMessage message : messageQueue) {
				CellObject cell = gson.fromJson(message.getContent(), CellObject.class);

				// check if update is useful
				CellObject stored = world.get(cell.getCol(), cell.getRow());
				if (stored == null || (stored.getType() == CellType.UNKOWN && cell.getType() != CellType.UNKOWN)
						|| (cell.getType() == CellType.FREE && cell.getFood() != stored.getFood())) {
					log.trace("update world with cell: {}", cell);
					world.put(cell);
				}
			}
			block();
		}
	}

	/**
	 * Behaviour which checks for messages from antworld, updates the world with
	 * the perception and determines the next action
	 * 
	 * @author Stephan
	 */
	class ReceiveMessageBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ACLMessage msg = receive(otherMessages);
			if (msg != null) {
				log.trace("Received this message from antworld: {}", msg);
				try {
					PerceptionObject perception = gson.fromJson(msg.getContent(), PerceptionObject.class);
					if (perception != null) {
						if (start == null && perception.getCell().getType() == CellType.START) {
							start = perception.getCell();
						}

						updateAntPosition(perception);
						gainKnowledgeFromPerception(msg.getPerformative(), perception);

						// delete the agent when its dead
						if (perception.getState() == AntState.DEAD) {
							log.debug("Agent is dead and will be deleted");
							MyAgent.this.doDelete();
						}
						// otherwise do the next turn
						else {
							ActionType action = getNextTurn(perception);
							ACLMessage answer = new ACLMessage(ACLMessage.REQUEST);
							answer.setLanguage(JSON);
							answer.setInReplyTo(msg.getReplyWith());
							answer.setContent(gson.toJson(new ActionObject(action, color)));
							answer.addReceiver(antworldAgent);
							send(answer);
							log.trace("Sent message to antworld with content: {}", answer);

							if (action == ActionType.ANT_ACTION_VOID)
								doSuspend();
						}
					}
					lastPerception = perception;
				} catch (JsonSyntaxException e) {
					log.error(e.getMessage(), e);
				}
			} else {
				block();
			}
		}
	}

	/**
	 * Gets the current perception and uses its information to update the world
	 * map.
	 * 
	 * @param performative performative of the received message
	 * @param perception current perception
	 */
	private void gainKnowledgeFromPerception(int performative, PerceptionObject perception) {
		// updates the world with the information of the current cell
		updateWorldAndPropagteToOthers(perception.getCell());
		int row = perception.getCell().getRow();
		int col = perception.getCell().getCol();

		// check for obstacle
		if (performative == ACLMessage.REFUSE && !hasMoved(perception)
				&& perception.getAction() != ActionType.ANT_ACTION_COLLECT) {
			int colRock = col;
			int rowRock = row;
			switch (perception.getAction()) {
			case ANT_ACTION_UP:
				--rowRock;
				break;
			case ANT_ACTION_DOWN:
				++rowRock;
				break;
			case ANT_ACTION_LEFT:
				--colRock;
				break;
			case ANT_ACTION_RIGHT:
				++colRock;
				break;

			default:
				break;
			}
			log.debug("Found rock at {}|{}", colRock, rowRock);
			CellObject rock = new CellObject(colRock, rowRock, CellType.OBSTACLE);
			updateWorldAndPropagteToOthers(rock);
		} else {
			// check all neighbour cells and create them if they are unknown
			createNeighbourIfNotPresentAndUpdateWorld(col - 1, row);
			createNeighbourIfNotPresentAndUpdateWorld(col + 1, row);
			createNeighbourIfNotPresentAndUpdateWorld(col, row - 1);
			createNeighbourIfNotPresentAndUpdateWorld(col, row + 1);
		}
	}

	/**
	 * Checks if the ant has moved since the last turn
	 * 
	 * @param currentPerception perception
	 * @return <code>true</code> if the ant has moved, <code>false</code> otherweise
	 */
	private boolean hasMoved(PerceptionObject currentPerception) {
		if (lastPerception != null && lastPerception.getCell() != null && currentPerception != null
				&& currentPerception.getCell() != null) {
			return lastPerception.getCell().getRow() != currentPerception.getCell().getRow()
					|| lastPerception.getCell().getCol() != currentPerception.getCell().getCol();
		}
		return true;
	}

	/**
	 * Checks if a cell if the given coordinates exists and creates it if not
	 * and updates the world map.
	 * 
	 * @param col column of the neighbour
	 * @param row row of the neighbour
	 */
	private void createNeighbourIfNotPresentAndUpdateWorld(int col, int row) {
		CellObject cell = world.get(col, row);
		if (cell == null) {
			cell = new CellObject(col, row, CellType.UNKOWN);
			log.trace("Created new unknown cell at {}", cell);
		}
		updateCellWithNeighbourInfos(cell);
		updateWorldAndPropagteToOthers(cell);
	}

	/**
	 * Gathers knowledge for a unknown cell from its known neighbours using
	 * their stench and smell
	 * 
	 * @param cell cell which should be investigated with its neighbours
	 */
	private void updateCellWithNeighbourInfos(CellObject cell) {
		if (cell.getType() == CellType.UNKOWN) {
			log.trace("Updating unknown cell {} with its neighbour informations", cell);
			List<CellObject> neighbours = world.getAllSuccessors(cell);
			boolean potentialTrap = true;
			boolean potentialFood = false;
			for (CellObject n : neighbours) {
				if (n.getType() == CellType.FREE || n.getType() == CellType.START) {
					// this eleminates a chance that this cell could be trap
					if (n.getStench() == 0) {
						log.trace("Neighbour {} has no stench, {} must not be a trap", n, cell);
						potentialTrap = false;
					} else if (potentialTrap && cell.getType() != CellType.PIT) {
						boolean somethingChanged = false;

						do {
							// we know that there must be (4 - stench)
							// undangerous
							// cells around it
							int nFree = 4 - n.getStench();
							int nTraps = n.getStench();
							// so check its other neighbours
							for (CellObject n2 : world.getAllSuccessors(n)) {
								// if they are free, decrease the left over
								// number
								// of free cells
								if (!n2.equals(cell) && !n2.isPotentialTrap() && n2.getType() != CellType.PIT) {
									--nFree;
								} else if (!n2.equals(cell) && n2.getType() == CellType.PIT) {
									--nTraps;
								}
							}
							// if all free cells are already known, our cell
							// must be
							// a trap
							if (nFree == 0 && cell.getType() != CellType.PIT) {
								log.trace("{} must be a trap because all free cells around {} are already known", cell,
										n);
								cell.setType(CellType.PIT);
								somethingChanged = true;
							} else if (nTraps == 0 && potentialTrap) {
								log.trace("{} cannot be trap because all traps around {} are already known", cell, n);
								potentialTrap = false;
								somethingChanged = true;
							} else {
								somethingChanged = false;
							}
						} while (somethingChanged);

					}
					// here could be food
					if (n.getSmell() - n.getFood() > 0) {
						log.trace("{} could contain food because {} has smell", cell, n);
						potentialFood = true;
					}
				}
			}
			cell.setPotentialFood(potentialFood);
			cell.setPotentialTrap(potentialTrap);
		}
	}

	/**
	 * After knowledge is gained this method is called to determine what to do
	 * next and sends the next turn to antworld.
	 * @param perception current perception
	 * @return action what to do next
	 */
	private ActionType getNextTurn(PerceptionObject perception) {
		ActionType action = null;

		// if the current cell contains food and the ant is not carrying food
		// yet, pick it up
		if (perception.getCell().getFood() > 0 && perception.getCurrentFood() == 0) {
			log.debug("Found food, pick it up");
			action = ActionType.ANT_ACTION_COLLECT;
		}
		// if the ant is carrying food and has reached the start cell, drop it
		else if (perception.getCurrentFood() > 0 && perception.getCell().getType() == CellType.START) {
			log.debug("Dropping food at start");
			action = ActionType.ANT_ACTION_DROP;
		}
		// otherwise determine which cell should get visited next
		else {
			action = getNextDirectionToMove(perception);
			log.debug("Next direction: {}", action);
		}

		return action;
	}

	/**
	 * Determines the next direction for the current turn
	 * 
	 * @param perception current perception
	 * @return Action describing the direction
	 */
	private ActionType getNextDirectionToMove(PerceptionObject perception) {
		CellObject currentCell = perception.getCell();

		// if the ant is carriying food, the highest priority is to find the
		// shortest path to the start
		if (perception.getCurrentFood() > 0) {
			log.debug("Search shortest path to start");
			Queue<CellObject> path = AStarAlgo.getShortestPath(currentCell, start, world, true);
			return getDirection(currentCell, path.peek());
		}
		// Otherwise search for food
		else {
			// 1. priority: check if the world map knows cell with food
			List<CellObject> options = world.getCellsWithFood();
			if (!options.isEmpty()) {
				log.debug("Search for path to a food cell");
				return getDirectionToFirstCellFromShortestPath(currentCell, options, true);
			}

			ActionType action = null;
			// 2. priority: check if the world map knows undangerous cell with a
			// potential for food
			List<CellObject> potentialFood = world.getUnvisitedCells(c -> c.isPotentialFood() && !c.isPotentialTrap());

			// 3. priority: check if the world mal knows undangerous cells to
			// explore
			List<CellObject> unvisitedCells = world.getUnvisitedCells(c -> !c.isPotentialTrap());

			if (potentialFood.isEmpty() && unvisitedCells.isEmpty()) {
				for (CellObject c : world.getUnvisitedCells(c -> c.isPotentialTrap())) {
					updateCellWithNeighbourInfos(c);
				}
				potentialFood = world.getUnvisitedCells(c -> c.isPotentialFood() && !c.isPotentialTrap());
				unvisitedCells = world.getUnvisitedCells(c -> !c.isPotentialTrap());
			}

			if (!potentialFood.isEmpty() || !unvisitedCells.isEmpty()) {
				// undangerous should be explored in growing radius from the
				// start
				// to find closer food earlier
				while (action == null) {
					options = potentialFood.parallelStream()
							.filter(c -> CellUtils.getHeuristicDistance(c, start) <= searchRadius)
							.collect(Collectors.toList());
					if (!options.isEmpty()) {
						log.debug("Search for path to a potential food cell");
						action = getDirectionToFirstCellFromShortestPath(currentCell, options, true);
					}

					options = unvisitedCells.parallelStream()
							.filter(c -> CellUtils.getHeuristicDistance(c, start) <= searchRadius)
							.collect(Collectors.toList());
					if (options.isEmpty()) {
						log.debug("Increment search radius for cells to {}", searchRadius);
						searchRadius += INCREMENT_RADIUS;
					} else {
						log.debug("Search for path to an undangerous cell");
						action = getDirectionToFirstCellFromShortestPath(currentCell, options, true);
					}
				}
			}

			if (action == null || action == ActionType.ANT_ACTION_VOID) {
				// 4. priority: try an unknown dangerous cell with potential
				// food
				options = world.getUnvisitedCells(c -> c.isPotentialFood());
				if (!options.isEmpty()) {
					log.debug("Search for path to a potential trap cell with potential food");
					action = getDirectionToFirstCellFromShortestPath(currentCell, options, false);
				}
			}

			if (action == null || action == ActionType.ANT_ACTION_VOID) {
				// 5. priority: just try an unknown dangerous cell
				options = world.getUnvisitedCells(c -> true);
				if (!options.isEmpty()) {
					log.debug("Search for path to a potential trap cell");
					action = getDirectionToFirstCellFromShortestPath(currentCell, options, false);
				}
			}

			if (action == null) {
				action = ActionType.ANT_ACTION_VOID;
				log.debug("No more cells to investigate");
			}

			return action;
		}
	}

	/**
	 * Gets the first direction as an action to move the first cell of a shortest path for the given options
	 * 
	 * @param currentCell
	 *            current ant position
	 * @param potentialCells
	 *            possible destinations
	 * @param avoidTraps
	 *            flag if dangerous paths should be avoided
	 * @return Action describing the direction for the next turn
	 */
	private ActionType getDirectionToFirstCellFromShortestPath(CellObject currentCell,
			Collection<CellObject> potentialCells, boolean avoidTraps) {
		List<CellObject> cells = new ArrayList<>(potentialCells);

		// sort all possible destinations by manhatten distance to the current
		// cell
		Collections.sort(cells, new DistanceComparatorToRefCell(currentCell));

		Iterator<CellObject> iterator = cells.iterator();
		int shortestPathLength = 0;
		Set<CellObject> options = new HashSet<>();
		CellObject dest = iterator.next();

		// iterate over all destinations
		do {
			log.trace("Search path from {} to {}", currentCell, dest);

			// search the shortest path with A* to the destionation
			Queue<CellObject> path = AStarAlgo.getShortestPath(currentCell, dest, world, avoidTraps);
			if (path.isEmpty()) {
				log.debug("Cannot find a path from {} to {}", currentCell, dest);
				continue;
			}

			// if there is a path and its length is shorter to previous shortest
			// path. clear all previous options and this path is the new
			// reference. Moreover add the first cell from this path as an
			// option visit
			if (options.isEmpty() || path.size() < shortestPathLength) {
				shortestPathLength = path.size();
				options.clear();
				options.add(path.peek());
			}
			// if the path has the same length like the previous shortest path,
			// take the first cell of the this path as an option too
			else if (path.size() == shortestPathLength)
				options.add(path.peek());

			// try next possible destination until the manhatten distance makes
			// it unpossible to find a shorther path
		} while (iterator.hasNext() && (options.isEmpty()
				| CellUtils.getHeuristicDistance(currentCell, (dest = iterator.next())) <= shortestPathLength));

		log.debug("stopped path finding and found {} options which led to a shortest path to a preferred cell",
				options.size());
		// choose an option and determine the direction to move to it
		if (!options.isEmpty()) {
			CellObject[] optionsArray = options.toArray(new CellObject[options.size()]);
			return getDirection(currentCell, optionsArray[random.nextInt(optionsArray.length)]);
		}
		return ActionType.ANT_ACTION_VOID;
	}

	/**
	 * Gets a direction to move furter from the first, the second cell. Usually
	 * this method should be only called for cells which connected directly.
	 * 
	 * @param from start cell
	 * @param to destination cell
	 * @return Action describing the direction
	 */
	private ActionType getDirection(CellObject from, CellObject to) {
		log.debug("Get direction from {} to {}", from, to);
		if (from.getCol() == to.getCol())
			return from.getRow() < to.getRow() ? ActionType.ANT_ACTION_DOWN : ActionType.ANT_ACTION_UP;

		return from.getCol() < to.getCol() ? ActionType.ANT_ACTION_RIGHT : ActionType.ANT_ACTION_LEFT;
	}

	/**
	 * Behaviour which is responsible for logging in to antworld
	 */
	class LoginBehaviour extends OneShotBehaviour {
		private static final String ANTWORLD_SERVICE_NAME = "antworld2016";
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ServiceDescription filter = new ServiceDescription();
			filter.setName(ANTWORLD_SERVICE_NAME);
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.addServices(filter);

			try {
				DFAgentDescription[] results = DFService.searchUntilFound(myAgent, getDefaultDF(), dfd, null,
						TimeUnit.MINUTES.toMillis(2));

				for (DFAgentDescription other : results) {
					log.debug("Found antword agent agent: {}", other.getName().getLocalName());
					antworldAgent = other.getName();
					otherMessages = MessageTemplate.MatchSender(antworldAgent);

					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
					message.setSender(myAgent.getAID());
					message.addReceiver(other.getName());
					message.setLanguage(JSON);

					Gson gson = new Gson();
					ActionObject loginbody = new ActionObject(ActionType.ANT_ACTION_LOGIN, color);
					message.setContent(gson.toJson(loginbody));
					myAgent.send(message);
				}
			} catch (FIPAException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
