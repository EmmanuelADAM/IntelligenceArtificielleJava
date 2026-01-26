package parcours;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * classe codant la recherche du plus cours chemin entre des villes <br>
 * recherche par algo génétique sur nombres entiers <br>
 * cas particulier : une sequence ne peut comporter 2 x le mm nombre <br>
 * ceci implique qu'une mutation du gene i = une permutation du nombre i avec le nombre i+1 dans la sequence <br>
 * et ceci implique que le croisement n'est possible que s'il ne genere pas de doublon
 * @author Emmanuel Adam
 * @since 2013.03.22*/
public class ParcoursGenetique extends JPanel implements ActionListener{
 /**noms des villes*/
//	String []noms = {"Bordeaux","Le Havre","Lille","Lyon","Marseille","Montpellier","Nantes","Nice","Paris","Reims","Rennes","Saint-�tienne","Strasbourg","Toulon","Toulouse"};
	static final String []NOMS = {"Valenciennes", "St Saulve", "Beuvrage", "Raisme", "Petite-Foret", "Marly", "Aulnoy-lez-Valenciennes", "Onnaing"};
	/**tableau de distances entre les villes*/
	static final double [][] TAB_CORRESPONDANCES = {
			{0,2.5,3,4.75,3.7,2.5,3,6.5}, 
			{2.5,0,3.2,3.3,5.4,3.3,4.6,3.9}, 
			{3,3.2,0,2,2.8,5.5,6,6.5}, 
			{4.75,3.3,2,0,2.6,7.3,7.5,8.3}, 
			{3.7,5.4,2.8,2.6,0,6,5.7,9}, 
			{2.5,3.3,5.5,7.3,6,0,1.75,6.2}, 
			{3,4.6,6,7.5,5.7,1.75,0,8}, 
			{6.5,3.9,6.5,8.3,9,6.2,8,0}};

	/**tableau de temps de trajet entre les villes*/
	static final double [][] TAB_CORRESPONDANCES_TEMPS = {
			{0,     5,   3,  10,  7,     5,   6,   10}, 
			{5,     0,   6,    6, 11, 6.6,   9,     4}, 
			{3,     6,   0,    2,   5,  11, 12,   12}, 
			{10,   6,   2,    0,    5, 15,  15,  16}, 
			{7,   11,   5,    5,    0,  12,  11, 13}, 
			{5,  6.6, 11,  15,  12,    0, 3.5,   5}, 
			{6,     9, 12,  15,  11, 3.5,    0, 10}, 
			{10,   4, 12,  16,  13,   5,  10,   0}};

	/**importance du temps de trajet dans le calcul du score final... on doit avoir COEF_TEMPS+COEF_DIST = 1*/
	final static double COEF_TEMPS = 0.8;
	/**importance de la distance  dans le calcul du score final... on doit avoir COEF_TEMPS+COEF_DIST = 1*/
	final static double COEF_DIST = 0.2;
	
	/** longueur d'une sequence */
	 int longSequence = 8;;//= 15; //= 8;
	/** nb de sequences a faire "vivre"*/
	final int nbSequences = 40;
	/** tableau des sequences a utiliser*/
	List<Sequence> population;
	/** pourcentage de sequences touches par la mutation */
	final double mutationDesSequences = 0.3;
	/** pourcentage de mutation dans une sequence*/
	final double mutationDansSequence = 0.1;
	/** nb cycles de reproduction*/
	int nbRepro;
	/**max score*/
	double max;
	/**max score*/
	double min;
	/**hasard*/
	private Random r;

	/** Text area : pour affichage*/
	JTextArea jTextArea;


	/**constructeur par d�faut */
	public ParcoursGenetique()
	{
		init();

		setLayout(new BorderLayout());
		jTextArea =  new JTextArea();
		jTextArea.setRows(5);
		JScrollPane jScrollPane  = new JScrollPane(jTextArea); 
		add(BorderLayout.CENTER, jScrollPane);

		JPanel jpanel = new JPanel(new GridLayout(0, 2));
		JButton jbOK = new JButton("Croiser");
		jbOK.setActionCommand("croiser");
		jbOK.addActionListener(this);
		jpanel.add(jbOK);
		jbOK = new JButton("Croiser "+nbRepro+" fois");
		jbOK.setActionCommand("croiserEnBoucle");
		jbOK.addActionListener(this);
		jpanel.add(jbOK);
		add(BorderLayout.NORTH, jpanel);
		jbOK = new JButton("Relancer");
		jbOK.setActionCommand("relancer");
		jbOK.addActionListener(this);
		add(BorderLayout.SOUTH, jbOK);

		afficheCodes(population);
	}
	
	/**initialisation de l'algo*/
	void init()
	{
		r = new Random();
		max = -1.0;
		min = 10000.0;
		nbRepro = 1000;
		population = new ArrayList<>();
		construirePopulation();
		scorer();

	}

	/**affiche les sequences
	 * @param pop liste des sequences*/
	void afficheCodes(List<Sequence> pop)
	{
		jTextArea.setText("");
		println("max = "+max );
		println("min = "+min );
		
		for(Sequence s : pop) println(s.toString());

		StringBuilder sb = new StringBuilder();
		var meilleur = pop.getFirst();
		int []meilleurParcours = meilleur.getSequence();
		sb.append("liste des villes = ");
		for(int noVille:meilleurParcours)
			sb.append(NOMS[noVille]).append(", ");
		println(sb.toString());
		double valDist = meilleur.getScoreDistance();
		println("distance de la meilleure solution :" + valDist);
		double valTemps = meilleur.getScoreTemps();
		println("temps de trajet de la meilleure solution :" + valTemps);
	}

	/**ajoute une ligne sur la fenetre
	 * @param s chaine a ajouter dans la zone de texte*/
	void println(String s)
	{
		jTextArea.setText(jTextArea.getText()+s+"\n");
	}


	/**construit le tableau de sequences : tableauSequences*/
	void construirePopulation()
	{
		Sequence s = null;
		for(int i = 0; i<nbSequences; i++)
		{
			s = new Sequence();
			s.construireSequence();
			population.add(s);
		}
	}

	/**donner un score a chaque sequence<br>
	 * lance le calul de la distance pour chaque sequence, et determine la distance max <br>
	 * lance le calul du temps pour chaque sequence, et determine le temps max <br>
	 * determine et affecte le score a chaque sequence : score = COEF_DIST x (scoreDist / maxDist) + COEF_TEMPS x (scoreTemps / maxTemps) <br>
	 * tri le tableau <br>
	 * pour le suivi, max = score le plus haut (le pire), min = score le plus bas (le meilleur)<br>
	 * (N.B. dans la tradition ou l'on selectionne le max le plus eleve; ici on fait l'inverse)
	 * */
	void scorer()
	{
		var maxDist = 0.0;
		var maxTemps = 0.0;
		var valD = 0.0;
		var valT = 0.0;
		for(Sequence s:population)
		{
			s.scorerParametres();
			valD = s.getScoreDistance();
			if (valD>maxDist) maxDist = valD;
			valT = s.getScoreTemps();
			if (valT>maxTemps) maxTemps = valT;
		}
		for(Sequence s:population)
			s.setScore(Math.sqrt((1 - s.getScoreDistance()/maxDist) * (1- s.getScoreDistance()/maxDist)));
		Collections.sort(population);
		max = population.getFirst().getScore();
		min = population.getLast().getScore();
	}


	/** le croisement des nbElites meilleurs remplace les nbElites pires, le reste ne change pas
	 * @param nbElites nb d'elites a considerer, et donc nb de pires a remplacer*/
	void croiserMeilleurs(int nbElites)
	{
		Sequence p1=null, p2=null, e=null;
		for(int i=0; i<nbElites; i++)
			for(int j=0; j<nbElites; j++) {
				if(i==j) continue;
				p1 = population.get(i);
				p2 = population.get(j);
				e = p1.croiserSequence(p2);
				if(!population.contains(e))  population.add(e);
			}
	}

	/** fonction effectuant des mutations dans le tableau des sequences : <br>
	 * ici, on choisit des sequences au hasard, sauf les 2 premieres, et demande la modification des genes */
	void muter()
	{
		int nbSequenceAMuter = (int)Math.round(nbSequences  * mutationDesSequences);		
		// ensemble d'elements uniques
		for(int i=0; i<nbSequenceAMuter; i++)
		{	
			//  ne pas muter les 2 premieres sequences
			int num =r.nextInt(nbSequences-2)+2;
			population.get(num).muterSequence();
		}
	}

	/** fonction lancee par la demande de croisement : recherche les meilleurs, les croise, lance la mutation et propage les modifs*/
	public void actionPerformed(ActionEvent event) {
		String verif = event.getActionCommand();
		if(verif.equalsIgnoreCase("croiser"))
		{			
			croiserMeilleurs(4);
			scorer();
			muter();
			scorer();
			afficheCodes(population);
			repaint();
		}
		if(verif.equalsIgnoreCase("croiserEnBoucle"))
		{			
			for(int i=0; i<nbRepro; i++)
			{
				croiserMeilleurs(4);
				scorer();
				muter();
			}
			scorer();
			afficheCodes(population);
			repaint();
		}
		if(verif.equalsIgnoreCase("relancer"))
		{			
			init();
			scorer();
			afficheCodes(population);
			repaint();
		}
	}


	/**fonction principale*/
	public static void main(String[] args)
	{
		JFrame jf = new JFrame("ParcoursGen");
		jf.setBounds(100, 100, 700, 500);
		Container ct = jf.getContentPane();
		ct.add(new ParcoursGenetique());
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}

}
