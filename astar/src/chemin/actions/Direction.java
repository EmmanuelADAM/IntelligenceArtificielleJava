package src.chemin.actions;

import src.chemin.environment.Cell;

public enum Direction {
N(1,0),NE(1,1), E(0,1), SE(-1,1),S(-1,0), SW(-1,-1), W(-1,0), NW(1,-1);
	
	int x, y;

	Direction(int _x, int _y){x = _x; y = _y;}
	
	public static Direction getDirection(Cell a, Cell b)
	{
		Direction d  = N;
		int difX = a.x - b.x;
		if(difX!=0)difX /=difX;
		int difY = a.y - b.y;
		if(difY!=0)difY /=difY;
		Direction[] dirs = Direction.values();
		boolean found = false;
		for(int i=0; i<8 && !found; i++)
		{
			d = dirs[i];
			found = (difX==d.x && difY==d.y);
		}
		return d;
	}
}
