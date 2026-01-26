package colordbscan3D;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TreeSet;

class Data2D implements  Cloneable, Comparable<Data2D>
{
	/**nb of values in a data*/
	int length;
	/**the values*/
	double[]values;
	/**the values, normalized*/
	 double[]normValues;
	/**cluster associated to the data*/
	Cluster2D cluster;
	/**has been visited*/
	boolean visited;
	/**is a noise*/
	boolean noise;
	/**id*/
	int id;
	/**graphical representation*/
	Circle circle;
	/**graphical of the custler*/
	Circle circleCluster;
	/**nb of data*/
	static int nb=0;

	
	/**
	 * initialize the values, the length and the sum
	 * @param _length the nb of values*/
	public Data2D(int _length)
	{
		id = Data2D.nb++;
		length = _length;
		values = new double[length];
		normValues= new double[length];
	}
	
	/**
	 * initialize the values, the length and the sum
	 * @param tab the list of values*/
	public Data2D(double[]tab)
	{
		this(tab.length);
		values = Arrays.copyOf(tab, tab.length);
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
	public double diste(Data2D o)
	{
		double dist=0;
		double sum = 0;
		for(int i=0; i<length; i++)
			sum += Math.pow(values[i] - o.values[i], 2);
		dist = Math.sqrt(sum);
		return  dist;
	}

	/**compute the euclidian distance between the current value and another one
	 * @param o the Other value to be compared with
	 * @return the distance between the current data and the other*/
	public double distNorm(Data2D o)
	{
		double dist, sum = 0;
		for(int i=0; i<length; i++)
			sum += Math.pow(normValues[i] - o.normValues[i], 2);
		dist = Math.sqrt(sum);
		return  dist;
	}
	/**compute the gap between the current value and another one. 
	 * The distance  between the two data is amplified with a 1/x formula.
	 * @param o the Other value to be compared with
	 * @return the gap between the current data and the other*/
	public double gap(Data2D o)
	{
		double dist=0;
		double sum = 0;
		for(int i=0; i<length; i++)
		{
			dist = Math.abs(normValues[i] - o.normValues[i]);
			dist = (dist<0.1?dist:1.1/(1.1-dist)-1);
//			dist = (dist<5?dist:Math.log(dist)*dist*1.5);			
			sum += dist;
		}
		return  sum;
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
		normValues = new double[length];
		Arrays.setAll(normValues, i->values[i]/maxs[i]);		
	}
	
	public void updateColor()
	{
		int noColor = 0;
		if(isNoise()) noColor = 0;
		else if(cluster!=null) noColor = (cluster.no+1)%(AppliDBScaneCouleurs.colors.length-1)+1;
		if(isVisited())
		{
			Color col = AppliDBScaneCouleurs.colors[noColor];
			circle.setFill(col);
			circleCluster.setFill(col.interpolate(Color.WHITE, 0.8));
		}			
	}
	
	public void setCluster(Cluster2D cluster) { 
		this.cluster = cluster; 
		if(cluster!=null && circle!=null) 
		{
			int noColor = (cluster.no+1)%(AppliDBScaneCouleurs.colors.length-1)+1;
			Color col = AppliDBScaneCouleurs.colors[noColor];
			circle.setFill(col);
			circleCluster.setFill(col.interpolate(Color.WHITE, 0.8));
		}
		}

	public Cluster2D getCluster() { return cluster; }

	@Override
	public int compareTo(Data2D o) {
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
		Color col = AppliDBScaneCouleurs.colors[0];
		if(circle!=null)
		{
			circle.setFill(col);
			circleCluster.setFill(col.interpolate(Color.WHITE, 0.8));
		}
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}

	public void setCircleCluster(Circle circleCluster) {
		this.circleCluster = circleCluster;
	}

	public static void main(String ... args)
	{
		var tab = new Data2D[10];
		Arrays.setAll(tab, i -> new Data2D(2));
		var liste1 = new ArrayList<Data2D>(Arrays.asList(tab));
		TreeSet<Data2D> set = new TreeSet<>();
		for(int i=0;i<5; i++) set.add(tab[i]);
		set.forEach(System.out::println);
		System.out.println("---");
		set.addAll(liste1);
		set.forEach(System.out::println);
	}


}
