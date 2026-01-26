package colordbscan;

import java.util.TreeSet;

import javafx.scene.shape.Circle;

class Cluster2D
{
	/**data associated to the cluster*/
	private final TreeSet<Data2D> dataSet;
	/**nb of clusters*/
	private static int nb;
	/**no of the cluster*/
	int no;
	/**minimal distance between one of the data and the centroid
	 * */
	private double minDist;
	/**maximal distance between one of the data and the centroid
	 * */
	private double maxDist;
	/**average of the distances between the data and the centroid
	 * */
	private double moyDist;
	/**graphical representation*/
	Circle circle;

	public Cluster2D()
	{
		dataSet = new TreeSet<>();
		no = nb++;
		System.out.println("new cluster " + no);
	}


	/**add a data to the cluster (and tell to the data that it is associated to this cluster)*/
	public void add(Data2D data)
	{
		dataSet.add(data);
		data.setCluster(this);
	}

	/**remove a data to from the cluster 
	 * (and tell to the data that it is no more associated to this cluster)*/
	public void remove(Data2D data)
	{
		dataSet.remove(data);
		data.setCluster(null);
	}
	

	/**compute the stats (minimal distance from a data to the centroid, maximal distance, average of the distances)*/
	public void computeStats()
	{

	}
	
	/**@return the no of the clusters, its nb of data, the stats and the data*/
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("cluster " + no + ", nb elts =  " + dataSet.size() + "\n");
		return sb.toString();
	}


	int getNo() {return no;}

}