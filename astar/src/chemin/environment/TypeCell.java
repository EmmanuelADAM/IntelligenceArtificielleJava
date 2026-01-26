package src.chemin.environment;

public enum TypeCell {
	HERBE(1.0), 
	SABLE(1.5),
	EAU(3),
	OBSTACLE(20);
	
	/** cost to walk on the environment*/
	double value;
	
	TypeCell(double _value){value = _value;}
	
	/** return the cost to walk on the environment*/
	public double getValue() {
		return value;
	}
}

