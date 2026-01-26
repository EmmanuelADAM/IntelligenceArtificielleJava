package src.taquin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/** this class propose an implementation of the a star algorithm 
 * @author emmanueladam */
public class AlgoAStar {

	/** a* algorithm to find the best path between two states 
	 * @param start initial state*/
	static LinkedList<State> algoASTAR(State start)
	{
		ArrayList<State> freeNodes;
		ArrayList<State> closedNodes;

		int index;

		// list of visited nodes
		closedNodes = new ArrayList<>();
		// list of nodes to evaluate
		freeNodes = new ArrayList<>();
		freeNodes.add(start);
		LinkedList<State> solution = null;
		boolean found = false;
		// no cost to go from start to start
		start.setG(0);
		start.evaluate();
		start.updateF();
		// while there is still a node to evaluate
		while(!freeNodes.isEmpty() && !found)
		{


			// choose the node having a F minimal
			State s = Collections.min(freeNodes);
			freeNodes.remove(s);
			closedNodes.add(s);


//			forget();

			// construct the list of neighbourgs
			LinkedList<State> nextDoorNeighbours  = s.nextStates();
			for(State ndn:nextDoorNeighbours)
			{
				ndn.setParent(s);
				// cost to reach the neighbour is the cost to reach n + cost from n to the neighbourg
				int cost = s.getG() + ndn.costBetween(s);
				ndn.setG(cost);
				ndn.evaluate();
				ndn.updateF();
				// stop if the node is the goal
				if (ndn.checkState())
				{
					found = true;
					solution = ndn.rebuildPath();
				}
				else {

					// if the neighbour has been visited, do not reevaluate it
					index = closedNodes.indexOf(ndn);
					if (index != -1)
						if (closedNodes.get(index).getF() > ndn.getF())
							closedNodes.remove(index);
						else continue;
					else {
						index = freeNodes.indexOf(ndn);
						if (index != -1)
							if (freeNodes.get(index).getF() > ndn.getF())
								freeNodes.remove(index);
							else continue;
					}
					freeNodes.add(ndn);
				}
			}
		}
		System.out.println("nb of free nodes = " + freeNodes.size());
		System.out.println("nb of closed nodes = " + closedNodes.size());
		return solution;
	}


}
