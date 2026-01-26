package testKMeans.TP.soluce;

import java.util.Arrays;
import java.util.Locale;

/**
 * A data is composed of a set of double values and is linked to a cluster
 * */
class Data
{
	/**nb of values in a data*/
	int length;
	/**the values*/
	private double[]values;
	/**the values, normalized*/
	double[]normValues;
	/**cluster associated to the data*/
	Cluster cluster;

	
	/**
	 * initialize the length, the values, the normalized values
	 * @param _length the nb of values*/
	public Data(int _length)
	{
		length = _length;
		values = new double[length];
		normValues= new double[length];
	}

	/**
	 * initialize  the length, the normalized values, set the values,
	 * @param tab the list of values*/
	public Data(double ...tab)
	{
		this(tab.length);
		values = Arrays.copyOf(tab, tab.length);
	}

	/**copy the other data
	 *@param other the data to "clone" */
	public Data(Data other)
	{
		this(other.values);
		normValues = Arrays.copyOf(other.normValues, length);
		cluster = other.cluster;
	}


	/**create the array of normalized values
	 * @param maxs arrays of maximal values found among the other data*/
	void normalize(double[]maxs)
	{
		normValues = new double[length];
		Arrays.setAll(normValues, i->values[i]/maxs[i]);
	}


	/**compute the euclidian distance between the current data and another one, by using the normalized values</br>
	 * so the root square of the sum of the squares of the difference between a value i of this data that the value i of the other one
	 * @param other the other data to be compared with
	 * @return the distance between the current data and the other*/
	public double distNorm(Data other)
	{
		double dist, sum = 0;
		for(int i=0; i<length; i++)
			sum += Math.pow(normValues[i] - other.normValues[i], 2);
		dist = Math.sqrt(sum);
		return  dist;
	}


	/**
	 * @return a string that contains the list of the values
	 * */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(");
		String sep=", ";
		for(double v:values) sb.append(String.format(Locale.ENGLISH,"%.2f", v)).append(sep);
		sb.append(")");
		return sb.toString();
	}

	

	///----- GETTERS AND SETTERS

	/**link a cluster to this data
	 * @param cluster the cluster to link with
	 * */
	public void setCluster(Cluster cluster) { this.cluster = cluster; }

	/**@return the cluster linke to this data*/
	public Cluster getCluster() { return cluster; }

	/**
	 * set a normalized value
	 * @param i indice of the normalized value to be changed
	 * @param x new value
	 * */
	public void setNormValue(int i, double x)
	{ normValues[i] = x; }

	/**
	 * set a value
	 * @param i indice of the value to be changed
	 * @param x new value
	 * */
	public void setValue(int i, double x)
	{ values[i] = x; }

	/**
	 * @return the ith normalized value of he data
	 * */
	public double getNormValue(int i)
	{ return normValues[i]; }

	/**
	 * @return the ith value of he data
	 * */
	public double getValue(int i)
	{ return values[i]; }

	///----- TESTS
	public static void main(String[] args)
	{
		Data d1 = new Data(12.4, 100, 3.5);
		Data d2 = new Data(8.4, 95, 4.5);
		Data d3 = new Data(15.4, 55, 5);
		double[] maxs = new double[]{15.4, 100, 5};
		d1.normalize(maxs);
		System.out.println("d1 : values = " + Arrays.toString(d1.values) + ", normalized values = " + Arrays.toString(d1.normValues) );
		d2.normalize(maxs);
		System.out.println("d2 : values = " + Arrays.toString(d2.values) + ", normalized values = " + Arrays.toString(d2.normValues) );
		//EXPECTED OUPUT :
		// d1 : values = [12.4, 100.0, 3.5], normalized values = [0.8051948051948052, 1.0, 0.7]
		// d2 : values = [8.4, 95.0, 4.5], normalized values = [0.5454545454545454, 0.95, 0.9]
		System.out.println("distance between d1 and  d2 = " + d1.distNorm(d2));
		//EXPECTED OUTPUT
		// distance between d1 and  d2 = 0.3316097141670275
	}


}
