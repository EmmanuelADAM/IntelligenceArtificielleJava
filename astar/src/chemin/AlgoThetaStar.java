package src.chemin;

import src.chemin.actions.Direction;
import src.chemin.actions.TypeNeighborhood;
import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;
import src.chemin.gui.EnvironmentGui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/** this class propose an implementation of the a star algorithm */
public class AlgoThetaStar extends AlgoStar {


	/**
	 * initialize the environment
	 *
	 * @param width   width of the environment
	 * @param height  height of the environment
	 * @param density percent of bloc in the environment
	 */
	private AlgoThetaStar(int width, int height, double density) {
		ent = new Entrepot(width, height, (density / 100.0), this);
		gui = new EnvironmentGui(ent);
		typeDistance = TypeDistance.Manhattan;
		typeNeighborhood = TypeNeighborhood.avoidHurtObstacle;
		reCompute();
	}

	/**
	 * a* algorithm to find the best path between two states
	 *
	 * @param _start initial state
	 * @param _goal  final state
	 */
	ArrayList<Cell> algoSTAR(Cell _start, Cell _goal) {
		Cell currentCell = _start;
		goal = _goal;
		// list of visited nodes
		closedNodes = new ArrayList<>();
		// list of nodes to evaluate
		freeNodes = new ArrayList<>();
		freeNodes.add(_start);
		// no cost to go from start to start
		_start.setG(0);
		_start.setH(evaluation(_start));
		_start.setF(_start.getH());
		_start.setChildren(null);
		// while there is still a node to evaluate
		goal.setG(Integer.MAX_VALUE);
		goal.setH(0);
		goal.setF(Integer.MAX_VALUE);
		goal.setChildren(null);
		goal.setParent(null);
		while (!freeNodes.isEmpty()) {
			// choose the node having a F minimal
//			Cell n = chooseBestNode();
			freeNodes.sort(Comparator.comparingInt(Cell::getF).thenComparingInt(Cell::getNo));
			Cell n = freeNodes.getFirst();
			Direction d = Direction.getDirection(currentCell, n);
			nbSameDir = ((d == currentDirection) ? nbSameDir + 1 : 0);
			velocity = ((nbSameDir > 2) ? 2 : 1);
			currentCell = n;
			// stop if the node is the goal
			if (isGoal(n)) return rebuildPath(n);
			freeNodes.remove(n);
			closedNodes.add(n);
			// construct the list of neighbourgs
			ArrayList<Cell> nextDoorNeighbours = typeNeighborhood.search.getNeighbors(n, ent, velocity, currentDirection);
			n.addChildren(nextDoorNeighbours);
			for (Cell ndn : nextDoorNeighbours) {
				if (!closedNodes.contains(ndn) && (!freeNodes.contains(ndn))) {
						ndn.setG(Integer.MAX_VALUE);
						ndn.setG(n.getG() + costBetween(n, ndn));
						ndn.setH(evaluation(ndn));
						ndn.setF(ndn.getG()+ndn.getH());
						ndn.setParent(n);
						freeNodes.add(ndn);
				}
				else update_vertex(n, ndn);
			}
		}

		return null;
	}

	private void update_vertex(Cell c, Cell neighbor) {
		Cell parent = c.getParent();

		if (Objects.nonNull(parent) && parent.isChild(neighbor))
		{
			int costParentNeighbor = costBetween(parent, neighbor);
			if (parent.getG() + costParentNeighbor < neighbor.getG()){
				neighbor.setG(parent.getG() + costParentNeighbor);
				neighbor.setParent(parent);
//				neighbor.setH(evaluation(neighbor));
				neighbor.setF(neighbor.getG()+neighbor.getH());
			}
		}
		else {
			int costCNeighbor = costBetween(c, neighbor);
			if (c.getG() + costCNeighbor < neighbor.getG()){
				neighbor.setG(c.getG() + costCNeighbor);
				neighbor.setParent(c);
//				neighbor.setH(evaluation(neighbor));
				neighbor.setF(neighbor.getG()+neighbor.getH());
			}

		}
	}

	public static void main(String []args)
	{
		logger.setLevel(Level.OFF);
		Handler fh ;
		try { fh = new FileHandler("./TethaStarLog.xml", false);
			logger.addHandler(fh); }
		catch (Exception e) { e.printStackTrace(); }

		new AlgoThetaStar(200, 150, 25);
	}

}
