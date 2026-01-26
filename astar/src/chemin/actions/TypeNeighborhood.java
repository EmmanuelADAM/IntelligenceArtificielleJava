package src.chemin.actions;

public enum TypeNeighborhood {
	avoidHurtObstacle((n,ent, v,d)->(new NeighborhoudDiagNoObstacle().getNeighbors(n, ent, v,d))),
	breakObstacle((n,ent, v,d)->(new NeighborhoudDiagBreakObstacle().getNeighbors(n, ent, v,d))),
	heightDirections((n,ent, v,d)->(new Neighborhoud8Dir().getNeighbors(n, ent, v,d))),
	fourDirectionsNSEW((n,ent, v,d)->(new Neighborhoud4Dir().getNeighbors(n, ent, v,d))),
	Neighborhoud8DirBB8((n, ent, v, d)->(new Neighborhoud8DirBB8().getNeighbors(n, ent, v,d)));

	public ComputeNeighborhood search;
	
	TypeNeighborhood(ComputeNeighborhood _search)
	{search = _search;}
}
