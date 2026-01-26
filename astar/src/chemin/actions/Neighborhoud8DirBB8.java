package src.chemin.actions;

import java.util.ArrayList;

import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;

/** return the neighborhood of a node n, can hit an obstacle */
public class Neighborhoud8DirBB8 implements ComputeNeighborhood {

	public ArrayList<Cell> getNeighbors(Cell cell, Entrepot ent, int velocity, Direction d) {
		ArrayList<Cell> neighbors = new ArrayList<>();
		int x = cell.getX(), y = cell.getY();
		switch (velocity) {
		case 1:
			for (int i = -1; i < 2; i++)
				for (int j = -1; j < 2; j++) {
					if (i == 0 && j == 0)
						continue;
					int xi = (x + i), yj = (y + j);
					if (xi >= 0 && xi < ent.getWidth() && yj >= 0 && yj < ent.getHeight()) {
						Cell c = ent.getCell(xi, yj);
						if (!c.isContainer())
							if (!ent.getCell(xi, y).isContainer() && !ent.getCell(x, yj).isContainer())
								neighbors.add(c);
					}
				}
			break;
		case 2:
			int xi = (x + d.x), yi = (y + d.y);
			if (xi >= 0 && xi < ent.getWidth() && yi >= 0 && yi < ent.getHeight()) {
				Cell c = ent.getCell(xi, yi);
				if (!c.isContainer())
					{						
						neighbors.add(c);
						for (int i = -1; i < 2; i++) {
							xi = x;  yi = y;
							switch (d) {
							case N: xi += i; yi -= 2; break;
							case S: xi += i; yi += 2; break;
							case E: yi += i; xi += 2; break;
							case W: yi += i; xi -= 2; break;
							case NE: if (i<=0) { xi += i; yi -= 2;} else { xi += 2; yi -= 1;}
							case NW: if (i>0) { xi += i; yi -= 2;} else { xi -= 2; yi -= 1;}
							case SE: if (i<=0) { xi += i; yi += 2;} else { xi += 2; yi += 1;}
							case SW: if (i>0) { xi += i; yi += 2;} else { xi -= 2; yi += 1;}
							}
							if (xi >= 0 && xi < ent.getWidth() && yi >= 0 && yi < ent.getHeight()) {
								 c = ent.getCell(xi, yi);
								if (!c.isContainer()) neighbors.add(c);
							}
						}
					}
			}

			break;
		}

		return neighbors;
	}

}
