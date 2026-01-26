package src.chemin.actions;

import java.util.ArrayList;

import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;

public interface ComputeNeighborhood {
	/** return the neighborhood of a node n, can go inside an obstacle*/
	ArrayList<Cell> getNeighbors(Cell n, Entrepot ent, int velocity, Direction d);
}
