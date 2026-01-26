package src.chemin;

import src.chemin.actions.Direction;
import src.chemin.actions.TypeNeighborhood;
import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;
import src.chemin.gui.EnvironmentGui;

import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/** this class propose an implementation of the a star algorithm */
public class AlgoAStar extends AlgoStar{


	/**initialize the environment
	 * @param width width of the environment
	 * @param height height of the environment
	 * @param density percent of bloc in the environment
	 * */
	private AlgoAStar(int width, int height, double density)
	{
		ent = new Entrepot(width, height, (density/100.0), this);
		gui = new EnvironmentGui(ent);
		typeDistance = TypeDistance.Manhattan;
		typeNeighborhood = TypeNeighborhood.avoidHurtObstacle;
		reCompute();
	}

	/** a* algorithm to find the best path between two states 
	 * @param _start initial state
	 * @param _goal final state*/
	ArrayList<Cell> algoSTAR(Cell _start, Cell _goal)
	{
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
		// while there is still a node to evaluate
		while(!freeNodes.isEmpty())
		{
			// choose the node having a F minimal
//			Cell n = chooseBestNode();
//			Collections.sort(freeNodes,Comparator.comparingInt(Cell::getF));
			freeNodes.sort((c1, c2) -> {
				int comp = c1.getF() - c2.getF();
				if (comp == 0) comp = c2.getNo() - c1.getNo();
				return comp;
			});
			Cell n = freeNodes.get(0);

			Direction d = Direction.getDirection(currentCell, n);
			nbSameDir = ((d == currentDirection)? nbSameDir +1:0);
			velocity = ((nbSameDir>2)?2:1);
			currentCell = n;
			// stop if the node is the goal
			if (isGoal(n)) return rebuildPath(n);
			freeNodes.remove(n);
			closedNodes.add(n);
			// construct the list of neighbourg
			ArrayList<Cell> nextDoorNeighbours  = typeNeighborhood.search.getNeighbors(n, ent, velocity, currentDirection);
			for(Cell ndn:nextDoorNeighbours)
			{
				// cost to reach the neighbour is the cost to reach n + cost from n to the neighbourg
				int cost = n.getG() + costBetween(n, ndn);
				boolean bestCost = false;
				// if the neighbour has been visited, do not reevaluate it
				if (closedNodes.contains(ndn))
				{
					// if the neighbour has been evaluated to a more important cost, change its evaluation
					if (cost < ndn.getG()) {
						bestCost = true;
						closedNodes.remove(ndn);
						freeNodes.add(ndn);
//						System.out.println("changement de parent pour " + ndn + " et reapparition en noeud libre...");
					}
				}
				else {
					// if the neighbour has not been evaluated
					if (freeNodes.contains(ndn)) {
						// if the neighbour has been evaluated to a more important cost, change its evaluation
						if (cost < ndn.getG()) {
							bestCost = true;
//							System.out.println("changement de parent pour " + ndn);
						}
					} else {
						freeNodes.add(ndn);
						ndn.setH(evaluation(ndn));
						bestCost = true;
					}
				}
				if(bestCost)
				{
					ndn.setParent(n);
					ndn.setG(cost);
					ndn.setF(ndn.getG()+ndn.getH());
				}
			}
		}
		return null;
	}


	public static void main(String []args)
	{
		logger.setLevel(Level.OFF);
		Handler fh ;
		try { fh = new FileHandler("./AstarLog.xml", false);
			logger.addHandler(fh); }
		catch (Exception e) { e.printStackTrace(); }

		new AlgoAStar(200, 100, 25);
	}

}
