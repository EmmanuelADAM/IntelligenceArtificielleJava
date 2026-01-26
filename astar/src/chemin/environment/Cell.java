package src.chemin.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/** a Cell has a position and is either a container, or a robot or is empty, can be a start or a goal or being visited <br>
 * a Cell owns its f, g and h values and a link to its parent */
public class Cell implements Serializable, Comparable<Cell>{
	/** the Cell contain a container or not*/
	private boolean container;
	/** start cell*/
	private boolean start = false;
	/** goal cell*/
	private boolean goal = false;
	/** evaluation of the distance from start to goal, through this cell*/
	private int f;
	/** evaluation of the distance from start to this cell*/
	private int g;
	/** evaluation of the distance from this cell to the goal */
	private int h;
	/** coordinates*/
	public int x,y;
	/** type cell*/
	private TypeCell type;
	/**value of the cell -> difficulty to go on the cell*/
	private double value;
	/**no (unique) of the cell*/
	private int no;
	/**nb (unique) of cells*/
	private static int nb;

	/**parent of the node*/
	private Cell parent = null;

	private List<Cell> children = null;


	private Cell()
	{
		type = TypeCell.HERBE;
		value = TypeCell.HERBE.getValue();
		children = new ArrayList<>();
		no = nb++;
	}

	Cell(int i, int j)
	{
		this();
		x = i; y = j;
		no = nb++;
	}

	public String toString()
	{
		return "("+x+", "+y+")";
	}

	//////// getters and setters
	public boolean isContainer() {
		return container;
	}

	void setContainer(boolean container) {
		this.container = container;
		if(container)
		{
			this.type = TypeCell.OBSTACLE;
			this.value = TypeCell.OBSTACLE.getValue();
		}
	}



	public boolean isStart() {
		return start;
	}

	void setStart(boolean start) {
		this.start = start;
	}

	public boolean isGoal() {
		return goal;
	}

	void setGoal(boolean goal) {
		this.goal = goal;
	}

	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}


	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public TypeCell getType() {
		return type;
	}

	void setType(TypeCell type) {
		if(!container)
		{
			this.type = type;
			this.value = type.getValue();
		}
	}


	public Cell getParent() {
		return parent;
	}

	public void setParent(Cell parent) {
		this.parent = parent;
	}
	//////// end of getters and setters

	/**reset f, g, and h to 0*/
	public void reset()
	{
		f = g = h = 0;
	}

	/**compare the Cell, on base of F*/
	public int compareTo(Cell o) {
		return (f - o.getF());
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return if the cell c can be accessed from the current cell*/
	public boolean isChild(Cell c){
		return children.contains(c);
	}

	/**
	 * add the cell c to the current s if the cell c can be accessed from the current cell*/
	public void addChild(Cell c){
		children.add(c);
	}

	/**
	 * add the cells in list to the children of the current cell*/
	public void addChildren(List<Cell> list){
		if(Objects.isNull(children)) children = new ArrayList<>();
		children.addAll(list);
	}

	/**
	 * set the list of children cells*/
	public void setChildren(List<Cell> list){
		children = list;
	}

	public int getNo() { return no; }

	public List<Cell> getChildren() { return children;}

}
