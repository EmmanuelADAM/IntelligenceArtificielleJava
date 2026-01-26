package parcours;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**sequence genetique composee de 'longSequence' entiers 
 * @author Emmanuel Adam
 * @since 2013.03.22*/
public class Sequence implements Comparable<Sequence>, Cloneable{
	/** longueur d'une sequence */
	static final int longSequence = 8;;//= 15; 
	/** sequence = tableau d'entiers*/
	int [] sequence;
	/** pourcentage de mutation dans une sequence*/
	static final double mutationDansSequence = 0.4;
	/**score*/
	private double score;
	/**score*/
	private double scoreTemps;
	/**score*/
	private double scoreDistance;
	/**hasard*/
	private Random r;
	
	public Sequence()
	{
		sequence = new int [longSequence];
		r = new Random();
	}

	public Sequence(Sequence original)
	{
		sequence = new int [longSequence];
		r = new Random();
		score = original.score;
		scoreTemps = original.scoreTemps;
		scoreDistance = original.scoreDistance;
		System.arraycopy(original.sequence, 0, sequence, 0, longSequence);
	}

	/**construit et retourne une sequence initialisee au hasard <br>
	 * modifie le tableau 'sequence'*/
	void construireSequence()
	{
		ArrayList<Integer> liste = new ArrayList<Integer>();
		for(int i=0; i<longSequence; i++) liste.add(i);
		Collections.shuffle(liste);
		for(int i=0; i<longSequence; i++)
			sequence[i]  = liste.get(i);
	}

	/** retourne la sequence resultant du croisement de A et de B<br>
	 * si A = a1b1c1d1 e1f1g1h1  et
	 * si B = a2b2c2d2 e2f2g2h2  <br>
	 * alors C = a1b1c1d1 e2f2g2h2 avec a1,b1,c1,d1,e2,f2,g2,h2 tous diff�erents <br>
	 *  si croisement impossible (presence de doublon dans C), retourne A
	 *  @param autre Sequence avec laquelle la sequence courante doit se reproduire
	 *  @return la sequence issue du croisement*/
	Sequence croiserSequence(Sequence autre)
	{
		Sequence result = new Sequence(this);
		int demiLongSequence = longSequence/2;
		System.arraycopy(sequence, 0, result.sequence, 0, demiLongSequence);
		System.arraycopy(sequence, demiLongSequence, result.sequence, 0, demiLongSequence);
		try { verif(result.sequence); }
		catch (Exception e) { System.arraycopy(sequence, 0, result.sequence, 0, longSequence) ;}
		return result;
		
	}
	
	/**verification non doublon dans la sequence
	 * @param tabL tableau d'entier dans lequel il ne doit pas avoir de valeurs identiques
	 * @throws Exception  si il existe au moins un doublon*/
	public void verif(int []tabL) throws Exception
	{
		for(int i=0; i<longSequence-1;i++)
		{
			int val = tabL[i];
			for(int j=i+1; j<longSequence; j++)
				if(tabL[j]==val) throw new Exception();
		}
	}
	
	/** fonction effetuant des mutations dans les chromosomes de la sequence.<br>
	 * effectue 'mutationDansSequence' % de mutations.<br>
	 * une mutation est une permutation de position entre une ville tir�e au sort et sa voisine.*/
	void muterSequence( )
	{		
		int nbGenesAMuter = (int)Math.round(longSequence * mutationDansSequence);

		for (int i=0; i<nbGenesAMuter; i++)
		{
			int num = r.nextInt(longSequence);
			int num2 =(num+1)%longSequence;
			int a = sequence[num];
			sequence[num] = sequence[num2];
			sequence[num2] = a;
		}
	}

	
	
	/**calcul de la distance parcourue  et du temps utilise par le chemin code par la séquence */
	void scorerParametres( )
	{
		double sd = 0.0;
		double st = 0.0;
		for(int j=0; j<longSequence-1; j++) {
			sd = sd + ParcoursGenetique.TAB_CORRESPONDANCES[sequence[j]][sequence[j + 1]];
			st = st + ParcoursGenetique.TAB_CORRESPONDANCES_TEMPS[sequence[j]][sequence[j+1]];
		}
		sd = sd + ParcoursGenetique.TAB_CORRESPONDANCES[sequence[longSequence-1]][sequence[0]] ;
		st = st + ParcoursGenetique.TAB_CORRESPONDANCES_TEMPS[sequence[longSequence-1]][sequence[0]] ;
		//boucle complete, on revient au debut:
		scoreDistance = sd + ParcoursGenetique.TAB_CORRESPONDANCES[sequence[longSequence-1]][0];
		scoreTemps = st + ParcoursGenetique.TAB_CORRESPONDANCES_TEMPS[sequence[longSequence-1]][0];
	}


	
	/**retourne une chaine de caractere representant la sequence + son score*/
	public String toString()
	{
		StringBuffer sb = new StringBuffer("[ ");
		for(int j=0; j<longSequence; j++)
			sb.append(sequence[j]).append(" ");
		sb.append("] : score = ");
		sb.append(score);
		return sb.toString();
	}
	

	public boolean equals(Object o)
	{
		var eq = false;
		if(o.getClass().equals(this.getClass()))
		{
			var autre = (Sequence)o;
			eq = Arrays.equals(this.sequence, autre.sequence);
		}
		return eq;

	}
	/**comparaison de sequence par score d�croissant <br>
	 * deux scores sont identiques si egaux � +- 0.0001*/
	public int compareTo(Sequence autre) {
		int retour = 0;
		double precision = 10000000;
		if(score==Double.NEGATIVE_INFINITY || autre.score == Double.NEGATIVE_INFINITY)
		{
			if(score==Double.NEGATIVE_INFINITY && autre.score == Double.NEGATIVE_INFINITY) retour=0;
			else if(score==Double.NEGATIVE_INFINITY ) retour = 1;
			else retour=-1;
		}
		else retour = (int)Math.round(precision*(autre.score-score));
		return retour;
	}
	
	/**fonction retournant un clone (au sens Java) de la sequence*/
  	public Object clone() {
  		Sequence seq = null;
  		try { seq = (Sequence)super.clone();	} 
  		catch(CloneNotSupportedException cnse) { cnse.printStackTrace(System.err);  }
  		return seq;
  	}
  	
	
	

	/**
	 * @return the sequence
	 */
	public int[] getSequence() {
		return sequence;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return the scoreTemps
	 */
	public double getScoreTemps() {
		return scoreTemps;
	}

	/**
	 * @return the scoreDistance
	 */
	public double getScoreDistance() {
		return scoreDistance;
	}

}
