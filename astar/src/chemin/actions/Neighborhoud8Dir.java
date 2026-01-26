package src.chemin.actions;

import java.util.ArrayList;

import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;

/** return the neighborhood of a node n, can hit an obstacle*/
public class Neighborhoud8Dir implements ComputeNeighborhood {

	public ArrayList<Cell> getNeighbors(Cell cell, Entrepot ent,  int velocity, Direction d) {
		ArrayList<Cell> neighbors = new ArrayList<>();
		int x = cell.getX();
		int y = cell.getY();
		for(int i=-1; i<2; i++)
			for(int j=-1; j<2; j++)
			{
				if (i==0 && j==0) continue;
				int xi = x+i; 
				int yj = y+j;
				if ( (xi>=0 && xi<ent.getWidth()) && (yj>=0 && yj<ent.getHeight()) )
				{
					Cell v = ent.getCell(xi,yj);
					if(!v.isContainer())
						neighbors.add(v);
				}
			}
		return neighbors;
		}

}
