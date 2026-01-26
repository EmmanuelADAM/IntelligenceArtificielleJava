package testKMeans.TP.soluce;

import java.util.ArrayList;
import java.util.Locale;

/**A cluster is localised at the center of the set of data linked to it*/
class Cluster
{
	/**nb of clusters*/
	private static int nb;
	/**no of this cluster*/
	int no;

	/**data linked to the cluster*/
	 ArrayList<Data> dataSet;
	/**a false data that represents the center of the cluster*/
	Data centroid;

	/**minimal distance between one of the data and the centroid */
	double minDist;
	/**maximal distance between one of the data and the centroid*/
	double maxDist;
	/**average of the distances between the data and the centroid*/
	double moyDist;

	/**create the dataSet, the centroid, increment the nb of clusters and set the no*/
	public Cluster()
	{
		dataSet = new ArrayList<>();
		centroid = new Data();
		no = nb++;
	}

	/**same as Cluster() + set the data with a centroid*/
	public Cluster(Data _centroid)
	{
		this();
		centroid = _centroid;
	}

	/**add a data to the cluster (and link the data to this cluster)*/
	public void add(Data data)
	{
		dataSet.add(data);
		data.setCluster(this);
	}

	/**remove a data to from the cluster (and remove the link to this cluster)*/
	public void remove(Data data)
	{
		dataSet.remove(data);
		data.setCluster(null);
	}
	
	/**recompute the center of the cluster according to tha values of the data linked to it<br>
	 * each value i of the centroid is the average of the value i of the data linked to the cluster<br>
	 * same thing for the normalized values (each normalized value i of the centroid.....)
	 * */
	public void centralize()
	{
		int nbElt = dataSet.size();
		if(nbElt>0)
		{
			int dim = dataSet.get(0).length;
			int[] tabI = {0};
			for(int i=0; i<dim;i++)
			{
				tabI[0] = i;
				double sum = dataSet.stream().parallel().mapToDouble(d->d.getNormValue(tabI[0])).sum();
				double average = sum/(double)nbElt;
				if(centroid.getNormValue(i)!=average)
				{
					centroid.setNormValue(i, sum/(double)nbElt);
					sum = dataSet.stream().parallel().mapToDouble(d->d.getValue(tabI[0])).sum();
					centroid.setValue(i, sum/(double)nbElt);
				}
			}
		}    	
	}
	
	/**compute the stats (minimal distance from a data to the centroid, maximal distance, average of the distances)*/
	public void computeStats()
	{
		var min = dataSet.stream().parallel().mapToDouble(d->d.distNorm(centroid)).min();
		min.ifPresent(m-> minDist = m);
		var max = dataSet.stream().parallel().mapToDouble(d->d.distNorm(centroid)).max();
		max.ifPresent(m-> maxDist = m);
		var avg = dataSet.stream().parallel().mapToDouble(d->d.distNorm(centroid)).average();
		avg.ifPresent(a-> moyDist = a);
	}
	
	/**@return the no of the cluster, its nb of data, the stats and the data if the nb<50*/
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("cluster " + no + ", nb elts =  " + dataSet.size() + "\n");
		sb.append("--> centroid = ").append(centroid).append("\n");
		if (dataSet.size()<50)
		{
			sb.append(" data =");
			for(Data data:dataSet) sb.append(data.toString());
			sb.append(" \n");
		}
		sb.append("--> dist min=").append(String.format(Locale.ENGLISH,"%.2f", minDist));
		sb.append("; dist max=").append(String.format(Locale.ENGLISH,"%.2f", maxDist));
		sb.append("; average dist=").append(String.format(Locale.ENGLISH,"%.2f", moyDist));
		sb.append("	\n---- ");
		return sb.toString();
	}


	//------------ GETTERS AND SETTERS
	
	void setCentroid(Data _centroid) { centroid = _centroid; }
	Data getCentroid() { return centroid; }
	int getNo() {return no;}

	///----- TESTS
	public static void main(String[] args)
	{
		//create data
		Data d1 = new Data(12.4, 100, 3.5);
		Data d2 = new Data(8.4, 95, 4.5);
		Data d3 = new Data(15.4, 55, 5);
		double[] maxs = new double[]{15.4, 100, 5};
		d1.normalize(maxs);
		d2.normalize(maxs);
		d3.normalize(maxs);

		//create a cluster centered on d1
		Cluster c = new Cluster(d1);
		c.add(d2);
		c.add(d3);
		System.out.println("initially center of cluster  = " + c.centroid );
		c.centralize();
		System.out.println("next centralization, center of cluster  = " + c.centroid );

		//EXPECTED OUPUT
		// initially center of cluster  = (12.40, 100.00, 3.50, )
		// next centralization, center of cluster  = (11.90, 75.00, 4.75, )

	}
}