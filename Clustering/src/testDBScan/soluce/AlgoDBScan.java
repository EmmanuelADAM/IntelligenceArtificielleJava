package testDBScan.soluce;

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

	static double eps = 0.02;//0.067;
	private static final int nbMin = 2;




	/**create some randomize data
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

	/**create some randomize data with concentric shapes 
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

	/**create some randomize data with concentric shapes 
	 * @param nb nb of data to generate
	 * @param radiusMax max dimension for radius
	 * @param nbSets nb of shape
	 */
	static void createSamplesConcentricWithNoise(int nb, int radiusMax, int nbSets, double percentNoise)
	{
		Random hasard = new Random();
		samples =new double[nb][2];
		double discWidth = ((double)radiusMax)/nbSets/2;	
		int nbData = (int)(nb * (1. - percentNoise));
		int nbBySet = nbData/nbSets;
		int k=0;
		for(int i=0; i<nbData; i++)
		{
			if (i%nbBySet==0) k++;
			double radius = 2*k*discWidth + (hasard.nextDouble()-0.5)*discWidth;
			double angle = 2*hasard.nextDouble()*Math.PI;
			samples[i][0] = radiusMax + radius*Math.cos(angle);
			samples[i][1] = radiusMax + radius*Math.sin(angle);
		}
		for(int i=nbData; i<nb; i++)
		{
			samples[i][0] = hasard.nextDouble() * 2 * radiusMax;
			samples[i][1] = hasard.nextDouble() * 2 * radiusMax;
		}
	}


	/**initialise de Data list, normalize them, and create the clusters
	 *  */
	static void initialize()
	{
		double[]maxs = new double[2];
		Arrays.fill(maxs, Double.NEGATIVE_INFINITY);
		for(double[] data:samples) 
		{
			dataSet.add(new Data(data));
			Arrays.setAll(maxs, i->(Math.max(maxs[i], data[i])));
		}
		dataSet.forEach(d->d.normalize(maxs));

	}


	/**algorithm of Kmeans :<br>
	 * set each data in th best cluster and do a loop: <br>
	 * -compute the new clusters centers; -moving  of data in best clusters if necessary
	 * */
	static void dbScan()
	{
		boolean isMoving = true;
		while(isMoving)
		{
			Optional<Data> dataNotVisitedOpt = dataSet.stream().filter(d-> !d.isVisited()).findAny();
			if(dataNotVisitedOpt.isPresent()) 
			{
				final Data data = dataNotVisitedOpt.get();
				data.setVisited(true);
				Set<Data> voisinage = dataSet.stream().filter(d-> (!d.isVisited()  && d.distNorm(data)<eps)).collect(Collectors.toSet());
				if (voisinage.size()<nbMin)
				{
					data.setNoise(true); 
				}
				else
				{
					Cluster cluster = new Cluster();
					clusters.add(cluster);
					cluster.add(data);
//					dataSet.remove(data);
					extendCluster(voisinage, cluster);
				}				
			}
			else System.err.println("no data !!!");
			isMoving = dataSet.stream().anyMatch(d -> !d.isVisited());
		}
	}

	private static void extendCluster(Set<Data> voisins, Cluster cluster)
	{
		ArrayList<Data> groupe = new ArrayList<>(voisins);
		while(!groupe.isEmpty())
		{
			Data voisin = groupe.get(0);
			groupe.remove(voisin);
			if (!voisin.isVisited() || voisin.isNoise())
			{
				voisin.setVisited(true);
				voisin.setNoise(false);
				cluster.add(voisin);
				final Data v = voisin;
				Set<Data> voisinage = dataSet.stream().filter(d-> (!d.isVisited() && !d.isNoise()) && d.distNorm(v)<eps).collect(Collectors.toSet());
				if (voisinage.size()>nbMin) 
				{
					voisins.addAll(voisinage);
					groupe.clear();
					groupe.addAll(voisins);
				}
			}
		}		 
	}

	public static void showClusters()
	{
		clusters.forEach(System.out::println);
	}

	public static void recolorize()
	{
		dataSet.forEach(Data::updateColor);
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