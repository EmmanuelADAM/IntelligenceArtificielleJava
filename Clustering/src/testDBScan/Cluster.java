package testDBScan;

import javafx.scene.shape.Circle;

import java.util.TreeSet;

class Cluster
{
	/**data associated to the cluster*/
	private final TreeSet<Data> dataSet=null;
	/**nb of clusters*/
	private static int nb;
	/**no of the cluster*/
	int no;
	/**minimal distance between one of the data and the centroid
	 * */

	public Cluster()
	{
		//TODO: A FAIRE
	}


	/**add a data to the cluster (and tell to the data that it is associated to this cluster)*/
	public void add(Data data)
	{
		//TODO: A FAIRE
	}

	/**remove a data to from the cluster 
	 * (and tell to the data that it is no more associated to this cluster)*/
	public void remove(Data data)
	{
		//TODO: A FAIRE
	}


	
	/**@return the no of the clusters, its nb of data, the stats and the data*/
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("cluster " + no + ", nb elts =  " + dataSet.size() + "\n");
		//TODO: A FAIRE
		return sb.toString();
	}


	int getNo() {return no;}

}