package testKMeans.TP.soluce;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**KMeans algorithm
 * create set of data composed of values and group them into a set of clusters
 * */
public class AlgoKMeans
{
	/**initial values*/
	private static double[][] values;
	/**list of the data*/
	private static final ArrayList<Data> dataSet = new ArrayList<>();
	/**list of the clusters*/
	private static final ArrayList<Cluster> clusters = new ArrayList<>();


	/**create some randomize data
	 * @param _nb nb of data to generate
	 * @param _dim dimension of the data (nb values / data) */
	private static void createValues(int _nb, int _dim)
	{
		values =new double[_nb][_dim];
		double[] noteMax = {1};
		for(int i=0; i<_nb; i++)
		{
			noteMax[0] = (int)((Math.random())*100+10);
			Arrays.setAll(values[i], j-> Math.random()*noteMax[0]);
		}
	}


	/**create  well separated clusters : <br>
	 * take a data randomly as the centroid of the first cluster<br>
	 * and next take the most distant data to create each other cluster
	 * @param _nbClusters nb of cluster to create*/
	private static void createClusters(int _nbClusters) {
		Data centroid = dataSet.get((int)(dataSet.size()*Math.random()));
		Cluster firstCluster = new Cluster(centroid);
		clusters.add(firstCluster);
		for(int c = 1; c<_nbClusters; c++) {
			Data farData = null;
			double maxDist = Double.NEGATIVE_INFINITY;
			for(Data data:dataSet) {
				double minDist = Double.POSITIVE_INFINITY;
				for(Cluster cluster:clusters) {
					double dist = data.distNorm(cluster.getCentroid());
					if(minDist>dist) minDist = dist;
				}
				if (maxDist<minDist) {
					maxDist  = minDist;
					farData = data;
				}
			}

			if(Objects.nonNull(farData))  clusters.add(new Cluster(new Data(farData)));
		}

		System.out.println("Initially, centroids are:");
		clusters.forEach(c->System.out.println(c.centroid.toString()));
	}

	/**create the data list from the values,
	 * compute the max for each indice i of the values,
	 * ask to each data to normalised itself with the tab of max
	 * lanch the creation of clusters
	 * @param _nbClusters nb of clusters
	 * @param _dim size of a data
	 *  */
	private static void initialize(int _nbClusters, int _dim)
	{
		double[]maxs = new double[_dim];
		Arrays.fill(maxs, Double.NEGATIVE_INFINITY);
		for(double[] data: values)
		{
			dataSet.add(new Data(data));
			Arrays.setAll(maxs, i->(Math.max(maxs[i], data[i])));
		}
		dataSet.forEach(d->d.normalize(maxs));

		createClusters(_nbClusters);
	}

	/**algorithm of Kmeans :<br>
	 * set each data in th best cluster and do a loop: <br>
	 *     - compute the new clusters centers; <br>
	 *     - move the data in best clusters if necessary
	 * stop when no more data move
	 * */
	private static void kMeanCluster()
	{
		dataSet.forEach(data -> searchCluster(data).add(data));
		boolean moving = true;
		while(moving)
		{
			moving = false;
			clusters.forEach(Cluster::centralize);
			for(Data data:dataSet)
			{
				Cluster bestCluster = searchCluster(data);
				if(data.getCluster() != bestCluster){
					moving = true;
					data.getCluster().remove(data);
					bestCluster.add(data);
				}
			}
		}
	}



	/**search the best cluster for a data
	 * @param data the data to place in a cluster
	 * @return the cluster whose the centroid is the closest from the data*/
	private static Cluster searchCluster(Data data) {
		Cluster bestCluster = null;
		double minimum = Double.POSITIVE_INFINITY;
		double distance=0;
		for(Cluster cluster:clusters)
		{
			distance = data.distNorm(cluster.getCentroid());
			if(distance < minimum){ minimum = distance; bestCluster = cluster; }
		}
		return bestCluster;
	}


	/**main program*/
	public static void main(String[] args)
	{
		//Exemple with 100 000 data composed of 5 values to group into 10 clusters
		// it takes approximately only 4 seconds !
		int dim=5;
		int nbSamples = 100000;
		int nbClusters = 10;
		createValues(nbSamples, dim);
		System.out.println("classification of " +nbSamples+ " samples of dimension " + dim + " into " + nbClusters + " clusters.");
		long start = System.currentTimeMillis();
		initialize(nbClusters, dim);
		kMeanCluster();
		long end = System.currentTimeMillis();
		System.out.println("after " + (end-start) + " ms ");
		clusters.forEach(Cluster::computeStats);
		// Print out clustering results.
		double error = 0;
		int nb=0;
		for(Cluster cluster:clusters)
		{
			if (cluster.dataSet.size()>0)
			{
				System.out.println(cluster);
				nb++;
				error += cluster.moyDist;
			}
		}
		error = error/(nb*dim);
		System.out.println("gobal error= " + String.format(Locale.ENGLISH, "%.2f", (error*100)) + " % ");
	}
}