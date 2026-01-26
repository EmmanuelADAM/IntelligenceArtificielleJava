package colorkmeans;


import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.System.out;


public class AlgoKMeans2D {

    /**
     * list of the data
     */
    static ArrayList<Data2D> dataSet = new ArrayList<>();
    /**
     * list of the clusters
     */
    static ArrayList<Cluster2D> clusters = new ArrayList<>();
    /**
     * cluster moves
     */
    static boolean isMoving = true;
    /**
     * samples of values
     */
    private static double[][] samples;

    /**
     * create some randomize data
     *
     * @param nb   nb of data to generate
     * @param xDim max dimension for x abscice
     * @param yDim max dimension for y abscice
     */
    static void createSamples(int nb, int xDim, int yDim) {
        Random hasard = new Random();
        samples = new double[nb][2];
        for (int i = 0; i < nb; i++) {
            samples[i][0] = hasard.nextDouble() * xDim;// * Math.abs(Math.cos(((i*1d)/(nb*1d))*Math.PI));
            samples[i][1] = hasard.nextDouble() * yDim;// * Math.abs(Math.sin(((i*1d)/(nb*1d))*Math.PI));
        }
    }

    /**
     * create some randomize data with concentric shapes
     *
     * @param nb        nb of data to generate
     * @param radiusMax max dimension for radius
     * @param nbSets    nb of shape
     */
    static void createSamplesConcentric(int nb, int radiusMax, int nbSets) {
        Random hasard = new Random();
        samples = new double[nb][2];
        double discWidth = ((double) radiusMax) / nbSets / 2;
        int nbBySet = nb / nbSets;
        for (int k = 1; k <= nbSets; k++) {
            for (int i = 0; i < nbBySet; i++) {
                double radius = 2 * k * discWidth + (hasard.nextDouble() - 0.5) * discWidth;
                double angle = 2 * hasard.nextDouble() * Math.PI;
                samples[i + (k - 1) * nbBySet][0] = radiusMax + radius * Math.cos(angle);
                samples[i + (k - 1) * nbBySet][1] = radiusMax + radius * Math.sin(angle);
            }
        }
    }

    /**
     * initialise de Data list, normalize them, and create the clusters
     *
     * @param _nbClusters nb of clusters
     */
    static void initialize(int _nbClusters) {
        double[] maxs = new double[2];
        Arrays.fill(maxs, Double.NEGATIVE_INFINITY);
        for (double[] data : samples) {
            dataSet.add(new Data2D(data));
            Arrays.setAll(maxs, i -> (Math.max(maxs[i], data[i])));
        }
        dataSet.forEach(d -> d.normalize(maxs));

        createClusters(_nbClusters);
        dataSet.forEach(data -> searchCluster(data).add(data));
    }


    private static void createClusters(int _nbClusters) {
        Data2D centroid = dataSet.get((int) (dataSet.size() * Math.random()));
        Cluster2D firstCluster = new Cluster2D(centroid);
        clusters.add(firstCluster);
        for (int c = 1; c < _nbClusters; c++) {
            Data2D farData = null;
            double maxDist = Double.NEGATIVE_INFINITY;
            for (Data2D data : dataSet) {
                double minDist = Double.POSITIVE_INFINITY;
                for (Cluster2D cluster : clusters) {
                    double dist = data.distNorm(cluster.getCentroid());
                    if (minDist > dist) minDist = dist;
                }
                if (maxDist < minDist) {
                    maxDist = minDist;
                    farData = data;
                }
            }

            Cluster2D cluster = new Cluster2D(farData.clone());
            clusters.add(cluster);
        }

        out.println("Initially, centroids are:");
        clusters.forEach(c -> out.println(c.centroid.toString()));
    }


    /**
     * algorithm of Kmeans :<br>
     * set each data in th best cluster and do a loop: <br>
     * -compute the new clusters centers; -moving  of data in best clusters if necessary
     */
    static void kMeanCluster() {

        isMoving = false;

        clusters.forEach(Cluster2D::centralize);

        for (Data2D data : dataSet) {
            Cluster2D bestCluster = searchCluster(data);
            if (data.getCluster() != bestCluster) {
                isMoving = true;
                data.getCluster().remove(data);
                bestCluster.add(data);
            }
        }
        if (!isMoving) {
            out.println("fin ! ");
            AlgoKMeans2D.showStats();
            AppliKMeansCouleurs.littleCycle.stop();
        }
    }


    /**
     * search the best cluster for a data
     *
     * @param data the data to place in a cluster
     * @return the cluster whose the centroid is the closest from the data
     */
    private static Cluster2D searchCluster(Data2D data) {
        Cluster2D bestCluster = null;
        double minimum = Double.POSITIVE_INFINITY;
        double distance;
        bestCluster = clusters.stream().min(Comparator.comparingDouble(c -> data.gap(c.getCentroid()))).get();
//		for(Cluster2D cluster:clusters)
//		{
//			distance = data.gap(cluster.getCentroid());
//			if(distance < minimum){ minimum = distance; bestCluster = cluster; }
//		}
        return bestCluster;
    }


    static int getSamplesFromCSV(final String file) {
        int nbElt = 0;
        try (CSVReader cvsReader = new CSVReader(new FileReader(file), ',', '\'', 1)) {

            String[] nextLine;
            while ((nextLine = cvsReader.readNext()) != null) {
                samples[nbElt][0] = Double.parseDouble(nextLine[0].trim());
                samples[nbElt][1] = Double.parseDouble(nextLine[1].trim());
                nbElt++;
            }
        } catch (NumberFormatException | IOException e) {
            System.err.println(e.getMessage());
        }
        return nbElt;
    }

    private static void showStats() {
        int dim = dataSet.get(0).length;
        clusters.forEach(Cluster2D::computeStats);
        // Print out clustering results.
        double error = 0;
        int nb = 0;
        for (Cluster2D cluster : clusters) {
            if (!cluster.dataSet.isEmpty()) {
                out.println(cluster);
                nb++;
                error += cluster.moyDist;
            }
        }
        error = error / (nb * dim);
        out.println("gobal error= " + String.format(Locale.ENGLISH, "%.2f", (error * 100)) + " % ");
    }

    /**
     * main program
     */
    public static void main(String[] args) {
        int dim = 5;
        int nbSamples = 1000000;
//		samples = new double[nbSamples][2];
//		nbSamples = getSamplesFromCSV("./samples.data");
//		samples = Arrays.copyOf(samples, nbSamples);
        createSamples(nbSamples, 600, 600);
//		nbSamples = samples.length;
//		dim = samples[0].length;
        int nbClusters = 5;
        out.println("classification of " + nbSamples + " samples of dimension " + dim + " into " + nbClusters + " clusters.");
        long start = System.currentTimeMillis();
        initialize(nbClusters);
        kMeanCluster();
        long end = System.currentTimeMillis();
        out.println("after " + (end - start) + " ms ");
        showStats();

        //clusters.forEach(c -> out.println(c));
    }
}