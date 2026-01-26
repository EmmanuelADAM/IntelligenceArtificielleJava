package src.chemin;


import src.chemin.environment.Cell;

import java.util.function.BiFunction;

public enum TypeDistance {
	Manhattan((a,b) -> ((double) (Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY())))),
	Euclidean((a,b) -> (Math.sqrt( Math.pow((a.getX() - b.getX()), 2) + (Math.pow((a.getY()- b.getY()), 2) )))) ,
	Chebyshev((a,b) -> ((double) Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY())))),
	Adam((a,b) -> {
		double difx = Math.abs(a.getX() - b.getX());
		double dify = Math.abs(a.getY() - b.getY());
		double da =  difx + dify; 
		double db = Math.max(difx, dify);
		return (.7*db + .3*da);});
	
	BiFunction<Cell, Cell, Double> distance;
	
	TypeDistance(BiFunction<Cell, Cell, Double> _distance)
	{
		distance = _distance;
	}
	
}
