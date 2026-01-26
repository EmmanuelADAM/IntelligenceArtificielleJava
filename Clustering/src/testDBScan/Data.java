package testDBScan;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import testDBScan.TPAppliDBScanCouleurs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TreeSet;

class Data implements  Cloneable, Comparable<Data>
{
	/**nb of values in a data*/
	int length;
	/**the values*/
	double[]values;
	/**the values, normalized*/
	 double[]normValues;
	/**cluster associated to the data*/
	Cluster cluster;
	/**has been visited*/
	boolean visited;
	/**is a noise*/
	boolean noise;
	/**id*/
	int id;
	/**nb of data*/
	static int nb=0;
	/**graphic representation*/
	Circle c;


	/**
	 * initialize the values, the length and the sum
	 * @param _length the nb of values*/
	public Data(int _length)
	{
		//TODO: A FAIRE
	}

	/**
	 * initialize the values, the length and the sum
	 * @param tab the list of values*/
	public Data(double[]tab)
	{
		//TODO: A FAIRE
	}

	/**
	 * change a normalized value 
	 * @param i indice of the value to be changed
	 * @param x new value
	 * */
	public void setNormValue(int i, double x) { normValues[i] = x; }

	/**
	 * change a value, 
	 * @param i indice of the value to be changed
	 * @param x new value
	 * */
	public void setValue(int i, double x) { values[i] = x; }

	/**
	 * @return the ith normalized value of he data
	 * */
	public double getNormValue(int i) { return normValues[i]; }

	/**
	 * @return the ith value of he data
	 * */
	public double getValue(int i) { return values[i]; }

	/**compute the euclidian distance between the current value and another one
	 * @param o the Other value to be compared with
	 * @return the distance between the current data and the other*/
	public double diste(Data o)
	{
		double dist=0;
		//TODO: A FAIRE
		return  dist;
	}

	/**compute the euclidian distance between the current value and another one
	 * @param o the Other value to be compared with
	 * @return the distance between the current data and the other*/
	public double distNorm(Data o)
	{
		double dist=0, sum = 0;
		//TODO: A FAIRE
		return  dist;
	}

	
	/**
	 * @return the list of the values
	 * */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(""+id+"-(");
		String sep=", ";
		for(double v:values) sb.append(String.format(Locale.ENGLISH,"%.5f", v)).append(sep);
		sb.append(")");
		return sb.toString();
	}

	
	/**create the array of normalized values
	 * @param maxs arrays of maximal values*/
	void normalize(double[]maxs)
	{
		//TODO: A FAIRE
	}
	

	public void setCluster(Cluster cluster) {
		this.cluster = cluster; 
	}

	public Cluster getCluster() { return cluster; }

	@Override
	public int compareTo(Data o) {
		return id -o.id;
	}

	public double[] getValues() {
		return values;
	}

	public double[] getNormValues() {
		return normValues;
	}

	public boolean isVisited() {
		return visited;
	}

	public boolean isNoise() {
		return noise;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public void setNoise(boolean noise) {
		this.noise = noise;
	}

	public void setC(Circle c) {
		this.c = c;
	}

	public void updateColor()
	{
		int noColor = 0;
		if(isNoise()) noColor = 0;
		else if(cluster!=null) noColor = (cluster.no+1)%(TPAppliDBScanCouleurs.colors.length-1)+1;
		if(isVisited())
		{
			Color col = TPAppliDBScanCouleurs.colors[noColor];
			if(c==null)System.err.println(this);
			if(c!=null) c.setFill(col);
		}
	}

}
