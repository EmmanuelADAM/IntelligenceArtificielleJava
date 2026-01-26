package colorkmeansSwing;

import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Locale;

class Cluster2D
{
	/**data associated to the cluster*/
	 ArrayList<Data2D> dataSet;
	/**computed data that represent the center of the cluster*/
	Data2D centroid;
	/**nb of clusters*/
	private static int nb;
	/**no of the cluster*/
	private final int no;
	/**minimal distance between one of the data and the centroid
	 * */
	private double minDist;
	/**maximal distance between one of the data and the centroid
	 * */
	private double maxDist;
	/**average of the distances between the data and the centroid
	 * */
	double moyDist;
	/**graphical representation*/
	Circle circle;

	private  Cluster2D()
	{
		dataSet = new ArrayList<>();
		centroid = new Data2D();
		no = nb++;
	}

	/**initialise the data with a centroid*/
	Cluster2D(Data2D _centroid)
	{
		this();
		centroid = _centroid;
	}

	/**add a data to the cluster (and tell to the data that it is associated to this cluster)*/
	void add(Data2D data)
	{
		dataSet.add(data);
		data.setCluster(this);
	}

	/**remove a data to from the cluster 
	 * (and tell to the data that it is no more associated to this cluster)*/
	void remove(Data2D data)
	{
		dataSet.remove(data);
		data.setCluster(null);
	}
	
	/**recompute the center of the cluster*/
	void centralize()
	{
		int nbElt = dataSet.size();
		if(nbElt>0)
		{
			int dim = dataSet.get(0).length;
			int[] tabI = {0};
//			boolean change=false;
			for(int i=0; i<dim;i++)
			{
				tabI[0] = i;
				double sum = dataSet.stream().parallel().mapToDouble(d->d.getNormValue(tabI[0])).sum();
				double average = sum/(double)nbElt;
				if(centroid.getNormValue(i)!=average)
				{
//					change=true;
					centroid.setNormValue(i, sum/(double)nbElt);
					sum = dataSet.stream().parallel().mapToDouble(d->d.getValue(tabI[0])).sum();
					centroid.setValue(i, sum/(double)nbElt);
				}				
			}
			circle.setCenterX(centroid.values[0]);
			circle.setCenterY(centroid.values[1]);
			computeStats();
			circle.setRadius(maxDist* AppliKMeansCouleurs.taille);
//			if(change)System.err.println("cluster-"+no+" change");
		}    	
	}
	
	/**compute the stats (minimal distance from a data to the centroid, maximal distance, average of the distances)*/
	void computeStats()
	{
		double somDist=0;
		minDist=Double.POSITIVE_INFINITY;
		maxDist=Double.NEGATIVE_INFINITY;
		for(Data2D data:dataSet)
		{
			double dist = data.distNorm(centroid);
			if(dist<minDist) minDist=dist;
			if(dist>maxDist) maxDist=dist;
			somDist+=dist;
		}
		if(dataSet.size()>0) moyDist = somDist/dataSet.size();
	}
	
	/**@return the no of the clusters, its nb of data, the stats and the data*/
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("cluster " + no + ", nb elts =  " + dataSet.size() + "\n");
		sb.append("--> centroid = ").append(centroid).append("\n data = \n");
		if (dataSet.size()<50) 
			for(Data2D data:dataSet) sb.append(data.toString()).append("\n");
		sb.append("--> dist min=").append(String.format(Locale.ENGLISH,"%.5f", minDist));
		sb.append("; dist max=").append(String.format(Locale.ENGLISH,"%.5f", maxDist));
		sb.append("; average dist=").append(String.format(Locale.ENGLISH,"%.5f", moyDist));
		sb.append("	\n---- ");
		return sb.toString();
	}


	Data2D getCentroid() { return centroid; }

}