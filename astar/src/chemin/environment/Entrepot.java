package src.chemin.environment;

import java.util.ArrayList;

import src.chemin.AlgoStar;
import src.chemin.TypeDistance;
import src.chemin.actions.TypeNeighborhood;

/** entrepot : rectangle of cells; some cells contains container*/
public class Entrepot {

	/** width of the environment*/
	private final int width;
	/** height of the environment*/
	private final int height;
	/** table of cells*/
	private final Cell[][] content;
	/** density of containers*/
	private double density = 0;
	/** initial Cell*/
	private Cell start;
	/** goal Cell*/
	private Cell goal;
	
	/**solution found by atar*/
	private ArrayList<Cell>  solution;
	

	/** A* algo linked to this environment*/
	public AlgoStar algo;
	
	
	/**true if the neighbouring of a container should be avoided*/
	private boolean poisonNeighbourg;
	
	/**coef of repulsion near a container*/
	private int coefPoisonNeightbourg;

	
	/**create empty environment*/
	public Entrepot(int _width, int _height)
	{
		width = _width;
		height = _height;
		poisonNeighbourg = false;
		content = new Cell[width][height];
		coefPoisonNeightbourg  = 0;
		init();
	}
	
	/**create an environment filled with container <br>
	 * set up the initial state and the goal state
	 * @param _density density of cells that own a container*/
	public Entrepot(int _width, int _height, double _density)
	{
		this(_width, _height);
		density = _density;
		fill();
		start = content[width/10][height/10];
		start.setStart(true);
		start.setContainer(false);
		goal = content[width-width/10][height-height/10];
		goal.setGoal(true);
		goal.setContainer(false);
		System.err.println("goal is in "+goal.getX() + "," + goal.getY());
	}

	
	/**create an environment filled with container <br>
	 * set up the initial state and the goal state
	 * @param _density density of cells that own a container
	 * @param _algo A* algo linked to this environment*/
	public Entrepot(int _width, int _height, double _density, AlgoStar _algo)
	{
		this(_width, _height, _density);
		algo = _algo;
	}

	/** initialise the cells*/
	private void init()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				content[i][j] = new Cell(i,j);
		unifyLandscape();
	}
	
	/**define 1 zone for all the landscape*/
	public void unifyLandscape()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				content[i][j].setType(TypeCell.HERBE);
	}
	
	/**define 3 zones in the landscape*/
	public void colorLandscape()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				if(i<width/3) content[i][j].setType(TypeCell.HERBE);
				else
				if(i>=(width/3) && (i<(2*width)/3)) content[i][j].setType(TypeCell.SABLE);
				else
				if (i>=(2*width)/3) content[i][j].setType(TypeCell.EAU);
	}
	
	
	/** set some cells as container<br>
	 * the neighbouring of a container is not attractive --> */
	private void fill()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				if (Math.random() <= density) 
					content[i][j].setContainer(true);
		if(poisonNeighbourg) diffusePoison();
	}
	
	/**a container has a negative impact on its neighbouring : cells arrouding the container increase their values*/
	private void poisonNeighbouring(int x, int y)
	{
		for(int i=-1; i<=1; i++)
			for(int j=-1; j<=1; j++)
			{
				if(x+i<0 || x+i>=width || y+j<0 || y+j >= height || (x+y == 0)) continue;
				if(!content[x+i][y+j].isContainer())
					content[x+i][y+j].setValue(content[x+i][y+j].getValue() +TypeCell.OBSTACLE.getValue()/(double)coefPoisonNeightbourg );
			}
	}

	/** diffuse the repulsive aspect for all containers*/
	public void diffusePoison()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				if (content[i][j].isContainer())  poisonNeighbouring(i,j);		
	}
	
	/**restore the value of the cells, without containers repulsion*/
	public 	void restoreArea()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				content[i][j].setValue(content[i][j].getType().getValue());
	}
	
	/** return the cell int x,y*/
	public Cell getCell(int x, int y) {
		return content[x][y];
	}

	/** change the goal*/
	public void setGoal(int i, int j) {
		if(i<content.length && j<content[0].length)
		{
			goal.setGoal(false);
			goal = content[i][j];
			goal.setGoal(true);
		}
	}

	/** clear the previous visits */
	public void removeVisit()
	{
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
			{
				content[i][j].setF(0);
				content[i][j].setG(0);
				content[i][j].setH(0);
			}
	}
	
	/**launch the recomputation of the best path*/
	public void recompute()
	{
		algo.reCompute();		
	}

	
	public void setMethod(TypeDistance method)
	{
		algo.setTypeDistance(method);
	}
	
	public void setMethodNeighbouring(TypeNeighborhood _typeNeighbouring)
	{
		algo.setTypeneighborhood(_typeNeighbouring);
	}
	
	////// getters and setters
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Cell[][] getContent() {
		return content;
	}

	public Cell getStart() {
		return start;
	}

	public Cell getGoal() {
		return goal;
	}

	public void setSolution(ArrayList<Cell> solution) {
		this.solution = solution;
	}

	public ArrayList<Cell> getSolution() {
		return solution;
	}

	/**
	 * @param coefPoisonNeightbourg the coefPoisonNeightbourg to set
	 */
	public void setCoefPoisonNeightbourg(int coefPoisonNeightbourg) {
		this.coefPoisonNeightbourg = coefPoisonNeightbourg;
	}

	////// end of getters and setters

}
