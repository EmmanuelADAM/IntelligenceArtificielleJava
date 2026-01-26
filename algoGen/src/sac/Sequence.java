package sac;

import java.util.Random;

/**sequence genetique composee de 'longSequence' entiers 
 * @author Emmanuel Adam
 * @since 2013.03.22*/
public class Sequence implements Comparable<Sequence>{
	/** sequence = tableau d'entiers*/
	byte [] genes;
	/** longueur d'une sequence */
	 int longueurGene ;
	/** nb de genes devant muter*/
	int nbGenesAMuter;
	/**score*/
	private double tauxGenesMutants;
	/**score*/
	private double utilite;
	/**somme des poids normalises*/
	private double sommePoids=0d;
	/**somme des interets normalises*/
	private double  sommeInterets = 0d;

	
	/**hasard*/
	static Random hasard = new Random();


	public Sequence(int _longueurGene, double _tauxGenesMutants)
	{
		longueurGene = _longueurGene;
		tauxGenesMutants = _tauxGenesMutants;
		nbGenesAMuter = (int)Math.round(longueurGene * _tauxGenesMutants);
		genes = new byte [longueurGene];
	}

	/**construit et retourne une sequence initialisee au hasard <br>
	 * modifie le tableau 'sequence'*/
	void construireSequence()
	{
		for(int i=0; i<longueurGene; i++)
			genes[i]  =  (byte)CriteresSac.hasard.nextInt(2);
		calculUtilite();
	}

	/** retourne la sequence resultant du croisement de A et de B<br>
	 * si A = a1b1c1d1 e1f1g1h1  et
	 * si B = a2b2c2d2 e2f2g2h2  <br>
	 * alors C = a1b1c1d1 e2f2g2h2 avec a1,b1,c1,d1,e2,f2,g2,h2 
	 *  @param autre Sequence avec laquelle la sequence courante doit se reproduire
	 *  @return la sequence issue du croisement*/
	Sequence croiserSequence(Sequence autre)
	{
		Sequence result = new Sequence(longueurGene, tauxGenesMutants);
		int demiLongSequence = longueurGene/2;
		System.arraycopy(genes, 0, result.genes, 0, demiLongSequence);
		System.arraycopy(autre.genes, demiLongSequence, result.genes, demiLongSequence, demiLongSequence);
		result.calculUtilite();
		return result;
	}
	

	/** fonction effectuant des mutations dans les genes de la sequence.<br>
	 * choix de nbGenesAMuter genes 
	 * une mutation est un changement de 0 en 1 ou inversement */
	void muterSequence( )
	{
		for (int i=0; i<nbGenesAMuter; i++)
		{
			int num = Sequence.hasard.nextInt(longueurGene);
			genes[num] = (byte)(1- genes[num]);
		}
		calculUtilite();
	}

	/**calcul de l'utilité du sac codé par la séquence.
	 * c-a-d la somme des interets unifies - la somme des poids
	 * @return l'utilité calculee ou Double.NEGATIVE_INFINITY si le poids dépasse le poids maximal autorise*/
	double calculUtilite( )
	{
		sommePoids = 0;
		sommeInterets = 0;
		for(int i=0; i<longueurGene; i++)
		{
			if(genes[i]==1) 
				{ 
				sommePoids +=  CriteresSac.PoidsUniformes.get(i) ;
				sommeInterets +=  CriteresSac.InteretsUniformes.get(i) ;
				}
		}
		if(sommePoids>CriteresSac.MaxPoidsUnifie) 
			utilite = Double.NEGATIVE_INFINITY;
		else
			utilite =  sommeInterets -  sommePoids;
		return utilite;
	}
	
	
	
	/**retourne une chaine de caractere representant la sequence + son score*/
	public String toString()
	{
		StringBuilder sb = new StringBuilder("[ ");
		for(int j=0; j<longueurGene; j++)
			sb.append(genes[j]).append(" ");
		sb.append("] : utilité = ");
		sb.append(utilite);
		return sb.toString();
	}
	
	
	/**comparaison de sequence par utilite décroissante <br>
	 * deux scores sont identiques si egaux à +- 0.00000001<br>
	 * une séquence d'utilite  Double.NEGATIVE_INFINITY est apres l'autre 
	 * sauf si l'autre a aussi son utilité à  Double.NEGATIVE_INFINITY, auquel cas les deux séquences sont considérée égales
	 * */
	@Override
	public int compareTo(Sequence autre) {
		  int retour = 0;
		  double precision = 10000000;
		  if(utilite==Double.NEGATIVE_INFINITY || autre.utilite == Double.NEGATIVE_INFINITY)
		  {
			  if(utilite==Double.NEGATIVE_INFINITY && autre.utilite == Double.NEGATIVE_INFINITY) retour=0;
			  else if(utilite==Double.NEGATIVE_INFINITY ) retour = 1;
			  else retour=-1;
		  }
		  else retour = (int)Math.round(precision*(autre.utilite-utilite));
		  return retour;
		}


	/**
	 * @return the sequence
	 */
	public byte[] getSequence() { return genes; }

	/**
	 * @return the score
	 */
	public double getUtilite() { return utilite; }

	/**des tests
	public static void main(String ...args)
	{
		Sequence s1 = new Sequence(5, .2);
		Sequence s2 = new Sequence(5, .2);
		ArrayList<Sequence> listeSequences = new ArrayList<>();
		listeSequences.add(s1);
		listeSequences.add(s2);
		TreeSet<Sequence> ensemble = new TreeSet<>();
		listeSequences.forEach(s->ensemble.add(s));
		listeSequences.clear();
		listeSequences.addAll(ensemble);
		ensemble.forEach(System.out::println);
		System.out.println("XXXXXX");
		TreeSet<String> set = new TreeSet<>();
		set.add("az");
		set.add("er");
		set.add("az");
		set.forEach(System.out::println);
		
	}*/

}
