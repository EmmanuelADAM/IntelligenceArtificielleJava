package src.chemin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import src.chemin.actions.Direction;
import src.chemin.actions.TypeNeighborhood;
import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;
import src.chemin.gui.EnvironmentGui;

/** this class propose an implementation of the a star algorithm */
public abstract class AlgoStar {
	public static Logger logger = Logger.getLogger("monLog");

	/** nodes to be evaluated*/
	ArrayList<Cell> freeNodes;
	/** evaluated nodes*/
	ArrayList<Cell> closedNodes;

	/** Goal Cell*/
	Cell goal;

	/**velocity of the robot*/
	int velocity = 1;
	/**directionof the robot*/
	Direction currentDirection = Direction.N;

	/**distance method*/
	TypeDistance typeDistance;

	/**type of method to build neighborhood of a node*/
	TypeNeighborhood typeNeighborhood;

	/** graphe / map of the nodes*/
	Entrepot ent;
	/** gui*/
	EnvironmentGui gui;
	

	/**nb of step in same direction*/
	int nbSameDir;



	/** a* algorithm to find the best path between two states
	 * @param _start initial state
	 * @param _goal final state*/
	 abstract ArrayList<Cell> algoSTAR(Cell _start, Cell _goal);


	/** return the path from start to the node n*/
	ArrayList<Cell> rebuildPath(Cell n)
	{
		if (n.getParent()!=null)
		{
			ArrayList<Cell> p = rebuildPath(n.getParent());
			p.add(n);
			return p;
		}
		else
			return (new ArrayList<>());
	}

	/** algo called to (re)launch a star algo*/
	public void reCompute()
	{
		System.gc();
		ent.removeVisit();
		if (goal!=null) goal.reset();
		long dateBefore = System.nanoTime();
		ArrayList<Cell>  solution = algoSTAR(ent.getStart(), ent.getGoal());
		long elapsedTime = (System.nanoTime() - dateBefore)/1000;
		double arr = (double)elapsedTime / 1000.0;
		//donner une valeur de arr arrondi à 2 chiffres apres la virgule
		arr  = Math.round(arr * 100.0) / 100.0;
		gui.setFieldComputeTime(""+arr);
		ent.setSolution(solution);
		if (solution==null)
			System.out.println("solution IMPOSSIBLE");
		else
		{
			int nbNoeudsClos = closedNodes.size();
			int nbNoeudsLibres = freeNodes.size();
			int distanceWithNuisance = goal.getF();
			gui.setFieldClosedNodes(""+nbNoeudsClos);
			gui.setFieldFreeNodes(""+nbNoeudsLibres);
			gui.setFieldDistanceWithNuisance(""+distanceWithNuisance);
//			System.err.println("solution found, goal.f=" + goal.getF() + ", goal.g=" + goal.getG() + ", goal.h=" + goal.getH()  );
			gui.setFieldDistance(""+	computeDistance(solution));
		}
		gui.repaint();
//		gui.setSize(gui.getWidth(), gui.getHeight());
//		gui.pack();
	}


	/**
	 * compute the distance recorded in the solution
	 * @param solution list of points from start to goal
	 * @return the distance from start to goal
	 */
	int computeDistance(ArrayList<Cell>  solution)
	{
		int distance=0;
		if(solution != null)
		{
			Cell end = solution.getLast();
			if(solution.size()>1)
				for(int i=solution.size()-2; i>0; i--)
				{
					Cell parent = solution.get(i);
					distance += costBetweenWithoutNuisance(parent, end);
					end = parent;
				}
		}
		return distance;
	}



	/** algo called to (re)launch a star algo with all the heuristic to determine the best one*/
	public void reComputeAll()
	{
		TypeDistance[] tabMethods = TypeDistance.values();
//		, TypeDistance.Adam};
		int distanceMin = Integer.MAX_VALUE;
		int nbNoeudsClosMin = 0;
		int nbNoeudsLibresMin = 0;
		TypeDistance meilleureMethode = TypeDistance.Manhattan;
		ArrayList<Cell>  meilleureSolution = null;
		for(TypeDistance method:tabMethods)
		{
			ent.removeVisit();
			if (goal!=null) goal.reset();
			typeDistance = method;
			ArrayList<Cell>  solution = algoSTAR(ent.getStart(), ent.getGoal());
			ent.setSolution(solution);
			if (solution==null)
				System.out.println("solution IMPOSSIBLE");
			else
			{
				int nbNoeudsClos = closedNodes.size();
				int nbNoeudsLibres = freeNodes.size();
				int distance = goal.getF();
				if (distance<distanceMin)
				{
					nbNoeudsClosMin = nbNoeudsClos;
					nbNoeudsLibresMin = nbNoeudsLibres;
					meilleureMethode = method;
					distanceMin = distance;
					meilleureSolution = solution;
				}
				String chaine = " methode " + method +  "; nb de noeuds clos =  " + nbNoeudsClos +
				"; nb de noeuds libres =  " + nbNoeudsLibres +  "; distance =  " + distance;
				System.out.println(chaine);
				gui.repaint();
			}
		}
		String chaine = "meilleure methode " + meilleureMethode +  "; nb de noeuds clos =  " + nbNoeudsClosMin +
		"; nb de noeuds libres =  " + nbNoeudsLibresMin +  "; distance =  " + distanceMin;
		System.out.println(chaine);
		System.out.println();
		typeDistance = meilleureMethode;
		ent.removeVisit();
		ent.setSolution(meilleureSolution);
		gui.repaint();

	}

	/** return the estimation of the distance from c to the goal*/
	int evaluation(Cell c)
	{
		int retour =  10* (int)distance(c, goal);
		retour = (int)((double) retour * c.getValue());
		return retour;
	}

	/**return the  distance between the cells*/
	double distance(Cell a, Cell b)
	{
		return typeDistance.distance.apply(a, b);
	}


	/** return the node having the minimal f*/
	Cell chooseBestNode()
	{
		Collections.sort(freeNodes);
		return freeNodes.getFirst();
	}

	/** return if n is a goal or not */
	boolean isGoal(Cell n)
	{
		return (n.getX() == goal.getX() && n.getY() == goal.getY());
	}



	/** return the cost from n to c : 10 for a lateral move, 14 (squareroot(2)*10) for a diagonal move
	 * @param n a node/cell
	 * @param c a node/cel close to n
	 * @return distance between the two adjacent nodes n and c
	 * */
	int costBetween(Cell n, Cell c)
	{
		double dist = TypeDistance.Euclidean.distance.apply(n, c);
		dist = dist * c.getValue() * 10d;
		if (typeNeighborhood == TypeNeighborhood.Neighborhoud8DirBB8) dist  =dist / (double)velocity;
		return (int)dist;
	}

	/** return the cost from n to c : 10 for a longitudinal move, 14 (squareroot(2)*10) for a diagonal move
	 * @param n a node/cell
	 * @param c a node/cel close to n
	 * @return distance between the two adjacent nodes n and c
	 * */
	int costBetweenWithoutNuisance(Cell n, Cell c)
	{
		double dist = TypeDistance.Euclidean.distance.apply(n, c);
		dist = dist *c.getType().getValue() * 10d;
		if (typeNeighborhood == TypeNeighborhood.Neighborhoud8DirBB8) dist  =dist / (double)velocity;
		return (int)dist;
	}


	/**
	 * @param typeDistance the typeDistance to set
	 */
	public void setTypeDistance(TypeDistance typeDistance) {
		this.typeDistance = typeDistance;
	}

	/**
	 * @param _typeneighborhood the typeneighborhood to set
	 */
	public void setTypeneighborhood(TypeNeighborhood _typeneighborhood) {
		//		System.err.println(""+this.getClass().getName() + "::" +_typeneighborhood );
		typeNeighborhood = _typeneighborhood;
	}




}
