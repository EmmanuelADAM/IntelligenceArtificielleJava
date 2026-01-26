package src.chemin.actions;

import java.util.ArrayList;

import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;

/** return the neighborhood of a node n, only in 4 directions N, S, W, E, avoiding obstacle*/
public class Neighborhoud4Dir implements ComputeNeighborhood {

	public ArrayList<Cell> getNeighbors(Cell cell, Entrepot ent,  int velocity, Direction d) {
		ArrayList<Cell> neighbors = new ArrayList<>();
		int x = cell.getX();
		int y = cell.getY();
		Cell c;
		if(x>1) {
			c = ent.getCell(x-1,y);
			if(c!=null && !c.isContainer()) neighbors.add(c);
		}
		if(x<ent.getWidth()-1) {
			c = ent.getCell(x+1,y);
			if(c!=null && !c.isContainer()) neighbors.add(c);
		}
		if(y>1) {
			c = ent.getCell(x, y-1);
			if(c!=null && !c.isContainer()) neighbors.add(c);
		}
		if(y<ent.getHeight()-1) {
			c = ent.getCell(x, y+1);
			if(c!=null && !c.isContainer()) neighbors.add(c);
		}
		return neighbors;
		}

}
