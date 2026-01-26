package testDBScan;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;


public class AlgoDBScan
{

	/**samples of values*/
	private static  double[][] samples;

	/**list of the data*/
	static TreeSet<Data> dataSet = new TreeSet<>();
	/**list of the clusters*/
	private static final ArrayList<Cluster> clusters = new ArrayList<>();

	static double eps = 0.067;
	private static final int nbMin = 2;




	/**create some randomize data
	 * UTILISER CETTE FONCTION POUR TESTER L ALGO
	 * @param nb nb of data to generate
	 * @param xDim max dimension for x abscice
	 * @param yDim max dimension for y abscice
	 */
	static void createSamples(int nb, int xDim, int yDim)
	{
		Random hasard = new Random();
		samples =new double[nb][2];
		for(int i=0; i<nb; i++)
		{
			samples[i][0] = hasard.nextDouble()*xDim;
			samples[i][1] = hasard.nextDouble()*yDim;
		}
	}

	/**create some randomize data of 2 values in concentric shapes
	 * DEFINIR ET UTILISER CETTE FONCTION  POUR UN  FONCTIONNEMENT PLUS CORRECT QU'AVEC LA PRECEDENTE
	 * @param nb nb of data to generate
	 * @param radiusMax max dimension for radius
	 * @param nbSets nb of shape
	 */
	static void createSamplesConcentric(int nb, int radiusMax, int nbSets)
	{
		Random hasard = new Random();
		samples =new double[nb][2];
		double discWidth = ((double)radiusMax)/nbSets/2;
		int nbBySet = nb/nbSets;
		int k=0;
		for(int i=0; i<nb; i++)
		{
			if (i%nbBySet==0) k++;
			double radius = 2*k*discWidth + (hasard.nextDouble()-0.5)*discWidth;
			double angle = 2*hasard.nextDouble()*Math.PI;
			samples[i][0] = radiusMax + radius*Math.cos(angle);
			samples[i][1] = radiusMax + radius*Math.sin(angle);
		}
	}



	/**initialise de Data list, normalize them, and create the clusters
	 *  */
	static void initialize()
	{
		//TODO: A FAIRE
	}


	/**algorithm of Kmeans :<br>
	 * set each data in th best cluster and do a loop: <br>
	 * -compute the new clusters centers; -moving  of data in best clusters if necessary
	 * */
	static void dbScan()
	{
		//TODO: A FAIRE

	}

	private static void extendCluster(Set<Data> voisins, Cluster cluster)
	{
		//TODO: A FAIRE

	}

	public static void showClusters()
	{
		clusters.forEach(System.out::println);
	}

	/**update the color of the data*/
	public static void recolorize()
	{
		dataSet.forEach(testDBScan.Data::updateColor);
	}


	/**main program*/
	public static void main(String[] args)
	{
		int nbSamples = 1000;
		AlgoDBScan.createSamplesConcentric(nbSamples, 600/2, 4);

		out.println("classification of " +nbSamples);
		long start = System.currentTimeMillis();
		AlgoDBScan.initialize();
		AlgoDBScan.dbScan();
		long end = System.currentTimeMillis();
		out.println("after " + (end-start) + " ms ");
		AlgoDBScan.showClusters();

	}
}