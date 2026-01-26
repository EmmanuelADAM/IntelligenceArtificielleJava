package testDBScan.soluce;

import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

class Cluster
{
	/**data associated to the cluster*/
	private final TreeSet<Data> dataSet;
	/**nb of clusters*/
	private static int nb;
	/**no of the cluster*/
	int no;

	public Cluster()
	{
		dataSet = new TreeSet<>();
		no = nb++;
		System.out.println("new cluster " + no);
	}


	/**add a data to the cluster (and tell to the data that it is associated to this cluster)*/
	public void add(Data data)
	{
		dataSet.add(data);
		data.setCluster(this);
	}

	/**remove a data to from the cluster 
	 * (and tell to the data that it is no more associated to this cluster)*/
	public void remove(Data data)
	{
		dataSet.remove(data);
		data.setCluster(null);
	}



	private void computeStat()
	{
		int n = dataSet.size();
		double mini, maxi;
		double actualMini = Double.MAX_VALUE;
		double actualMaxi = Double.MIN_VALUE;
		for(Data d:dataSet)
		{
			var flux = dataSet.stream().filter(o->(o.distNorm(d)<AlgoDBScan.eps && !o.equals(d))).mapToDouble(v->v.distNorm(d));
			var tab = flux.toArray();
			mini = Arrays.stream(tab).min().getAsDouble();
			assert mini>0: "pb avec mini, d="+d+"\n les autres = \n" + Arrays.toString(dataSet.toArray());
			maxi = Arrays.stream(tab).max().getAsDouble();
			if (mini<actualMini) actualMini = mini;
			if (maxi>actualMaxi) actualMaxi = maxi;
		}
		System.err.printf("dans cluster %d, dist min = %.5f, dist max = %.5f %n", this.no, actualMini, actualMaxi);

	}
	
	/**@return the no of the clusters, its nb of data, the stats and the data*/
	@Override
	public String toString()
	{
		computeStat();
		StringBuilder sb = new StringBuilder("cluster " + no + ", nb elts =  " + dataSet.size() + "\n");
		int nb = Math.min(10, dataSet.size());
		Data[] tab = dataSet.toArray(Data[]::new);
		for(int i=0; i<nb; i++)
			sb.append(tab[i].toString()).append(" :: ");
		return sb.toString();
	}


	int getNo() {return no;}

}