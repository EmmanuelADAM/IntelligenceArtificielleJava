package testKMeans;


import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.System.out;

import com.opencsv.CSVReader;


/**
 * class that contains the KMeans algorithm in Java > 8
 * @author emmanueladam
 * */
public class AlgoKMeans
{

	/**samples of values*/
	private static double[][] tabNotes;
   /**titles of columns*/
   private static String[] titles;
   /**nb of columns*/
   private static  int nbColumns;

	/**list of the data*/
	private static final ArrayList<Data> dataSet = new ArrayList<>();
	/**list of the clusters*/
	private static final ArrayList<Cluster> clusters = new ArrayList<>();
	
	private static Random hasard;


	/**create some randomize data
	 * @param _nb nb of data to generate
	 * @param _dim dimension of the data */
	private static void createSamples(int _nb, int _dim)
	{
		tabNotes =new double[_nb][_dim];
		for(int i=0; i<_nb; i++)
			Arrays.setAll(tabNotes[i], s->hasard.nextDouble()*20);
	}

	
	/**initialise de Data list, normalize them, and create the clusters
	 * @param _nbClusters nb of clusters
	 * @param _dim size of a data
	 *  */
	private static void initialize(int _nbClusters, int _dim)
	{
	   hasard = new Random();
		double[]maxs = new double[_dim];
		Arrays.fill(maxs, Double.NEGATIVE_INFINITY);
		for(double[] data: tabNotes)
		{
			dataSet.add(new Data(data));
			Arrays.setAll(maxs, i->(Math.max(maxs[i], data[i])));
		}
		dataSet.forEach(d->d.normalize(maxs));

		createClusters(_nbClusters);
	}

	/** create some clusters with the KMeans++ method : 
	 * a cluster is choosen randomly, then each new cluster is located at the most distant position of the others
	 * @param _nbClusters the nb of cluster to create from the dataSet
	 * */
	private static void createClusters(int _nbClusters) {
		Data centroid = dataSet.get(hasard.nextInt(dataSet.size()));
		Cluster firstCluster = new Cluster(centroid);
		clusters.add(firstCluster);
		int nbClusters = _nbClusters;
		for(int c = 1; c<nbClusters; c++) {
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
			
			Cluster cluster = new Cluster(farData.clone());
			clusters.add(cluster);
		}

		out.println("Initially, centroids are:");
		clusters.forEach(c->out.println(c.centroid.toString()));
	}
	

	
	/**algorithm of Kmeans :<br>
	 * set each data in th best cluster and do a loop: <br>
	 * -compute the new clusters centers; -moving  of data in best clusters if necessary
	 * @param maxMoves nb max of adaptations
	 * */
	private static void kMeanCluster(int maxMoves)
	{
		dataSet.forEach(data -> searchCluster(data).add(data));

		boolean moving = true;
		int i=0;
		while(moving && i<maxMoves)
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
         i++;
		}
		System.err.println("end after " + i + " moves...");
	}



	/**search the best cluster for a data
	 * @param data the data to place in a cluster
	 * @return the cluster whose the centroid is the closest from the data*/
	private static Cluster searchCluster(Data data) {
		Cluster bestCluster = null;
		double minimum = Double.POSITIVE_INFINITY;
		double distance=0;
		bestCluster = clusters.stream().min(Comparator.comparingDouble(c-> data.gap(c.getCentroid()))).get();
//		for(Cluster cluster:clusters)
//		{
//			distance = data.gap(cluster.getCentroid());
//			if(distance < minimum){ minimum = distance; bestCluster = cluster; }
//		}
		return bestCluster.clone();
	}

	
	/**read a cvs file that contains data to clusterise. 
	 * @param file the name of the datafile
	 * */
	static int getSamplesFromCSV(final String file, final String[]selectedColumns) {
	   int nbElts=0;
	   Arrays.sort(selectedColumns);
		List<String[]>lines = null;
		try (CSVReader csvReader = new CSVReader(new FileReader(file))){
		   lines =  csvReader.readAll();
		} 
		catch (NumberFormatException | IOException e) {
			System.err.println(e.getMessage());
		}
		if(lines !=null && !lines.isEmpty())
		{
	      nbElts=lines.size()-1;
		   nbColumns = lines.get(0).length;
		   int nbSelectedColumns = selectedColumns.length;
		   tabNotes = new double[nbElts][nbSelectedColumns];
		   titles = Arrays.copyOf(lines.get(0), nbColumns);
	      int nbLines = lines.size();	
	      int c=0;
         for(int j=0; j<nbColumns;j++)
         {
            if(Arrays.binarySearch(selectedColumns, titles[j])>=0)
            {
               for(int i=1; i<nbLines;i++)
               {
                  String[] nextLine = lines.get(i);
                  tabNotes[i-1][c] = Double.parseDouble(nextLine[j].trim());
               }
               c++;
            }
	      }
		}
		return nbElts;
	}

	/**main program*/
	public static void main(String[] args)
	{
	   String[] cols= {"age", "studies", "trips", "wakeEarly"};
      int nbSamples = getSamplesFromCSV("/Users/emmanueladam/IdeaProjects/Clustering/src/testKMeans/Data-Partens-EA-cl1.csv", cols);
		int dim=nbColumns;
		int nbClusters = 20;
//		createSamples(nbSamples, dim);
		out.println("classification of " +nbSamples+ " samples of dimension " + dim + " into " + nbClusters + " clusters.");
		long start = System.currentTimeMillis();
		initialize(nbClusters, cols.length);
		int maxMoves = 1000;
		kMeanCluster(maxMoves);
		long end = System.currentTimeMillis();
		out.println("after " + (end-start) + " ms ");
		clusters.forEach(Cluster::computeStats);
		// Print out clustering results.
		double error = 0;
		int nb=0;
		for(Cluster cluster:clusters)
		{
			if (!cluster.dataSet.isEmpty())
			{
				out.println(cluster);
				nb++;
				error += cluster.moyDist;
			}
		}
		error = error/(nb*dim);
		out.println("gobal error= " + String.format(Locale.ENGLISH, "%.2f", (error*100)) + " % ");
		
	}
}