import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * classe permettant la resolution du compte est bon
 * 
 * @author emmanuel adam
 */
class Solution
{
   /** solution à trouver */
   int but;
   /** nombres a utiliser */
   Integer[] nombres;
   /** noeuds libres (valeurs pouvant être utilisée pour le calcul) */
   List<Integer> valeursLibres;
   /** operateurs à utiliser pour le calcul */
   List<OperateurFunc> operateurs = Arrays.asList(OperateurFunc.values());
   /** hasard */
   Random hasard;

   /** enchainement des etapes sous forme de chaine de caracteres */
   StringBuilder etapes;

   // constantes pour optimiser la création de la chaine des étapes
   static final String space = " ";
   static final String equal = " = ";
   static final String coma = " ; ";

   /** constructeur */
   Solution() {
      valeursLibres = new ArrayList<>();
      etapes = new StringBuilder();
      hasard = new Random();
   }

   /**
    * constructeur
    * 
    * @param nombres
    *                   nombres à utiliser pour le calcul
    * @param but
    *                   nombre à atteindre
    */
   Solution(Integer[] nombres, int but) {
      this();
      this.but = but;
      this.nombres = Arrays.copyOf(nombres, nombres.length);
      valeursLibres = new ArrayList<>(Arrays.asList(nombres));
   }

   /**
    * reinitialise les variables globales
    * replace les 6 nombres de base dans la pile valeursLibres<br>
    * efface la liste des étapes
    */
   void reinit()
   {
      valeursLibres.clear();
      valeursLibres.addAll(Arrays.asList(nombres));
      etapes.delete(0, etapes.length());
   }

   /**
    * fonction de calcul d'une solution par méthode aléatoire<br>
    * modifie l'objet etapes en indiquant les etapes de calcul suivies
    * 
    * @return la meilleure solution trouvée
    */
   public int calcule()
   {
      int dernierCalcul = 0;
      int bestSolution = 0;
      int bestEcart = Integer.MAX_VALUE;
      OperateurFunc operateur = null;
      int nbValLibres = valeursLibres.size();
      // tant que le dernier resultat est différent du but et qu'il reste au moins 2
      // nombres à choisir
      while ((dernierCalcul != but) && (nbValLibres > 1))
      {
         // prendre 2 nombres au hasard
         int indice = hasard.nextInt(nbValLibres);
         int nb1 = valeursLibres.remove(indice);
         indice = hasard.nextInt(nbValLibres - 1);
         int nb2 = valeursLibres.remove(indice);

         // tirer un opérateur applicable
         operateur = choixOperateurApplicable(nb1, nb2);

         // appliquer l'operateur et ajouter le resultat à la pile des valeurs libres
         dernierCalcul = operateur.calcul.apply(nb1, nb2);
         valeursLibres.add(dernierCalcul);

         // on ajoute aux étapes la chaine " nb1 (+,-,* ou /) nb2 = dernierCalcul;"
         etapes.append(space).append(nb1).append(space).append(operateur).append(space);
         etapes.append(nb2).append(equal).append(dernierCalcul).append(coma);

         // si valeur intéressante, la mémoriser
         int ecart = Math.abs(dernierCalcul - but);
         if (ecart < bestEcart)
         {
            bestEcart = ecart;
            bestSolution = dernierCalcul;
         }
         nbValLibres--;
      }
      return bestSolution;
   }

   /**
    * choix au hasard d'un operateur applicable sur nb1 et nb2
    * 
    * @param nb1
    *               premier argument de l'operation
    * @param nb2
    *               second argument de l'operation
    * @return operateur dans (+, -, *, /)
    */
   OperateurFunc choixOperateurApplicable(int nb1, int nb2)
   {
      int i = hasard.nextInt(4);
      boolean ok = false;
      do
      {
         i = (i + 1) % 4;
         ok = operateurs.get(i).test.apply(nb1, nb2);
      } while (!ok);
      return operateurs.get(i);
   }

   /**
    * fonction statique qui lance la recherche d'une solution
    * jusqu'a ce que la solution exacte soit trouvee
    * ou que le nombre de tests max a été atteint
    * 
    * @param nombres
    *                     nombres à utiliser pour le calcul
    * @param but
    *                     solution à trouver
    * @param nbCalculsMax
    *                     nb max de tests de solution autorisé
    * @return l'enchainement des etapes menant à la solution ou à la valeur la
    *         plus proche trouvée, séparées par des ';' le dernier élément de la
    *         chaine est la valeur trouvée
    */
   public static String testDesSols(Integer[] nombres, int but, int nbCalculsMax)
   {
      int nbCalculs = 0;
      Solution sol = new Solution(nombres, but);
      int solutionTrouvee = 0;
      String bestSol = null;
      int meilleurEcart = Integer.MAX_VALUE;
      while (solutionTrouvee != but && nbCalculs < nbCalculsMax && !Thread.currentThread().isInterrupted())
      {
         solutionTrouvee = sol.calcule();
         int ecart = Math.abs(solutionTrouvee - but);
         if (ecart < meilleurEcart)
         {
            meilleurEcart = ecart;
            sol.etapes.append(solutionTrouvee);
            bestSol = sol.etapes.toString();
         }
         nbCalculs++;
         sol.reinit();
         Thread.yield();
      }
      if (Thread.currentThread().isInterrupted()) bestSol = null;
      return bestSol;
   }

   /** fonction principale, lance la resolution */
   public static void main(String[] args)
   {
      int but = 826;
//      Integer[] tab = new Integer[] { 1, 2, 6, 7, 25, 5 };
      Integer[] tab = new Integer[]{100, 2, 75, 3, 1, 10};

      System.out.print("trouver " + but + ", avec : ");
      for (int v : tab) System.out.print(v + " ; ");
      System.out.println();

      int nbThreads = 4;
      System.out.printf("tente de trouver des solutions à partir de %d tests en parallèle : \n", nbThreads);
      System.out.println("---------------------------------");

      long debut = System.currentTimeMillis();
      ThreadGroup groupe = new ThreadGroup("calcul");
      for (int i = 0; i < nbThreads; i++)
      {
         new Thread(groupe, () -> {
            String strSol = testDesSols(tab, but, 10000);
            if (strSol != null)
            {
               int pos = strSol.lastIndexOf(";");
               String res = strSol.substring(pos + 1);
               System.out.println("resultat = " + res + " : " + strSol.substring(0, pos - 1));
               System.out.println("---------------------------------");
            }
         }).start();
      }
      while (groupe.activeCount() == nbThreads)
         Thread.yield();
      groupe.interrupt();
      long fin = System.currentTimeMillis();
      double duree = (fin - debut) ;
      System.out.println("duree pour la 1ere solution = " + duree + " ms");
   }

}
