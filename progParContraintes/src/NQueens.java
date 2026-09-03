import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.restart.LubyCutoff;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Random;

/**
 * exemple classique de résolution du problème des n reines par chocosolver
 * plusieurs variantes sont proposées
 * Très largement inspiré de la documentation choco https://choco-solver.org/
 */

public class NQueens {

    /**résolution du problème des n reines
     * ecriture "facile"
     * @param boundedDomain si true, les variables ont un domaine borné (plus efficace), ex : x in [0,7]
     *                      sinon le domaine est non borné (plus général), ex : x in {0,1,2,3,4,5,6,7}
     * */
    static int[] nQueens_1(int n, boolean boundedDomain)
    {
        Model model = new Model("probleme des "+ n + " reines");
        IntVar[] colonnes = model.intVarArray("reine_", n, 0,n-1, boundedDomain);

        for(int i  = 0; i < n-1; i++){
            for(int j = i + 1; j < n; j++){
                colonnes[i].ne(colonnes[j]).post();
                colonnes[i].ne(colonnes[j].sub(j - i)).post();
                colonnes[i].ne(colonnes[j].add(j - i)).post();
            }
        }
        System.out.println("nb contraintes = "+ model.getCstrs().length);

        Solution solution = model.getSolver().findSolution();

        if(solution != null){
            int[] res = new int[n];
            for(int i=0; i<n; i++){
                res[i] = solution.getIntVal(colonnes[i]);
            }
            System.out.println(Arrays.toString(res));
            return res;
        }
        System.out.println(solution);

        return null;

    }


    /**résolution du problème des n reines
     * plus de variables, moins de  contraintes
     * ici des variables contiennent les indices des diagonales montantes et descendantes pour chaque reine
     * donc toutes les colonnes, toutes les diagonales doivent être differentes pour tout i,j avec i <> j */
    static int[] nQueens_2(int n)
    {
        Model model = new Model("probleme des "+ n + " reines");
        IntVar[] colonnes = model.intVarArray("reine_", n, 1, n, false);
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for(int i = 0 ; i < n; i++){
            diag1[i] = colonnes[i].sub(i).intVar();
            diag2[i] = colonnes[i].add(i).intVar();
        }
        model.post(
                model.allDifferent(colonnes),
                model.allDifferent(diag1),
                model.allDifferent(diag2)
        );

        Solution solution = model.getSolver().findSolution();
        System.out.println("nb contraintes = "+ model.getCstrs().length);

        if(solution != null){
            System.out.println(solution);
            int[] res = new int[n];
            for(int i=0; i<n; i++){
                res[i] = solution.getIntVal(colonnes[i]);
            }
            return res;
        }
        return null;
    }

    /**résolution du problème des n reines
     * ecriture classique mais avec une stratégie de recherche personnalisée
     * */
    static int[] nQueens_4(int n)
    {
        Model model = new Model("probleme des "+ n + " reines");
        IntVar[] colonnes = model.intVarArray("reine_", n, 1, n, true);

        for(int i  = 0; i < n-1; i++){
            for(int j = i + 1; j < n; j++){
                colonnes[i].ne(colonnes[j]).post();
                colonnes[i].ne(colonnes[j].sub(j - i)).post();
                colonnes[i].ne(colonnes[j].add(j - i)).post();
            }
        }
        System.out.println("nb contraintes = "+ model.getCstrs().length);

        Solver s = model.getSolver();
        s.setSearch(Search.intVarSearch(
        //selectionner la variable avec le plus petit domaine
                new FirstFail(model),
        //selectionner la valeur à assigner parmi la plus petite du domaine
                new IntDomainMin(),
        //les  variables
                colonnes));

        Solution solution = model.getSolver().findSolution();

        if(solution != null){
            int[] res = new int[n];
            for(int i=0; i<n; i++){
                res[i] = solution.getIntVal(colonnes[i]);
            }
            System.out.println(Arrays.toString(res));
            return res;
        }
        System.out.println(solution);

        return null;

    }


    /**résolution du problème des n reines par recherche locale aléatoire
     * configuration du mode de sélection de variables et du mode de recherche
     *
     * ATTENTION : cette version reste bloquée vers n=100 pour deux raisons indépendantes
     * du "voisinage" choisi :
     * 1) le LNS (Large Neighborhood Search, ci-dessous PropagationGuidedNeighborhood) ne sert
     *    qu'à AMÉLIORER une solution déjà trouvée (il "relâche" quelques variables d'une solution
     *    existante et cherche mieux) : il est fait pour l'optimisation (findOptimalSolution(), ou
     *    plusieurs solve() successifs). Ici on ne demande qu'une seule solution via findSolution(),
     *    qui s'arrête dès la 1ere solution trouvée : le LNS n'a donc jamais l'occasion de s'activer,
     *    il n'a aucun effet sur le temps nécessaire pour trouver CETTE première solution.
     * 2) les restarts (setRestarts) ne servent à rien non plus ici car la stratégie de recherche
     *    (FirstFail + IntDomainMin) est déterministe : à chaque restart, le solveur rejoue EXACTEMENT
     *    le même arbre de recherche et échoue de la même façon.
     * => voir nQueens_grandeTaille(n) pour une version qui passe effectivement à l'échelle (500-1000 reines).
     * */
    static int[] nQueens_LocalSearch(int n) {
        Model model = new Model("probleme des " + n + " reines - Local Search");

        // Variables : position de la reine sur chaque ligne
        IntVar[] colonnes = model.intVarArray("reine_", n, 0, n-1, false);

        // Contraintes
        for(int i = 0; i < n-1; i++){
            for(int j = i + 1; j < n; j++){
                // Pas sur la même colonne
                colonnes[i].ne(colonnes[j]).post();
                // Pas sur la même diagonale descendante
                colonnes[i].ne(colonnes[j].sub(j - i)).post();
                // Pas sur la même diagonale montante
                colonnes[i].ne(colonnes[j].add(j - i)).post();
            }
        }

        System.out.println("nb contraintes = " + model.getCstrs().length);

        // Stratégie de recherche déterministe : variable au plus petit domaine, plus petite valeur
        model.getSolver().setSearch(
                Search.intVarSearch(
                        new FirstFail(model),
                        new IntDomainMin(),
                        colonnes
                )
        );

        // Large Neighborhood Search : n'a d'effet qu'entre deux solutions successives
        // (ici il n'y en a qu'une, donc ce bloc est sans effet sur findSolution())
        model.getSolver().setLNS(
                new org.chocosolver.solver.search.loop.lns.neighbors.PropagationGuidedNeighborhood(
                        colonnes, n/3, n, 0
                )
        );
        // Restarts déterministes : sans randomisation de la recherche, chaque restart est identique
        model.getSolver().setRestarts(
                new FailCounter(model, 100),
                new LubyCutoff(100),
                1000
        );

        // Trouver une solution
        Solution solution = model.getSolver().findSolution();

        if(solution != null){
            int[] res = new int[n];
            for(int i = 0; i < n; i++){
                res[i] = solution.getIntVal(colonnes[i]);
            }
            System.out.println("Solution trouvée : " + Arrays.toString(res));
            System.out.println("Nb de backtracks : " + model.getSolver().getBackTrackCount());
            System.out.println("Temps : " + model.getSolver().getTimeCount() + "s");
            return res;
        }

        System.out.println("Pas de solution trouvée");
        return null;
    }

    /**résolution du problème des n reines pour de grandes valeurs de n (500, 1000, ...)
     * Deux changements, par rapport à nQueens_LocalSearch, expliquent le passage à l'échelle :
     *
     * 1) contraintes globales allDifferent (au lieu de O(n²) contraintes binaires "!=") :
     *    la propagation par cohérence de bornes (consistance "BC") d'un allDifferent est bien plus
     *    rapide, pour de grands n, que la décomposition en de très nombreuses contraintes binaires.
     *
     * 2) une recherche COMPLÈTE (backtracking + propagation) mais RANDOMISÉE couplée à des
     *    redémarrages (loi de Luby) : chaque restart explore alors une branche différente de
     *    l'arbre de recherche, ce qui permet d'éviter de rester bloqué dans un sous-arbre sans
     *    solution (phénomène classique des distributions "à queue lourde" décrit par Gomes &
     *    Selman, dont les n-reines sont l'exemple académique de référence).
     *    Le LNS, lui, n'a pas sa place ici : il sert à améliorer une solution déjà trouvée, alors
     *    qu'ici le problème est de trouver LA PREMIÈRE solution le plus vite possible.
     *
     *    Expérimentalement (voir mesures ci-dessous), randomiser uniquement la VALEUR en gardant
     *    une variable "first-fail" (plus petit domaine) ne suffit pas à passer l'échelle : pour
     *    n=1000 la recherche ne termine pas en moins d'une minute. Il faut aussi randomiser le
     *    choix de la VARIABLE (Search.randomSearch) : les domaines sont quasiment tous de même
     *    taille avant d'être réduits, donc "first-fail" départage ses ex-aequo de façon déterministe
     *    et ramène malgré tout le solveur, restart après restart, dans les mêmes premières branches.
     *    Avec une sélection aléatoire des deux, n=500 se résout en ~1s et n=1000 en quelques secondes.
     *
     * @param n nombre de reines (testé jusqu'à 1000, ok en quelques secondes)
     * @param seed graine du générateur aléatoire utilisé pour le choix des variables/valeurs
     */
    static int[] nQueens_grandeTaille(int n, long seed) {
        Model model = new Model("probleme des " + n + " reines - grande taille");

        // domaine borné (intervalle [0, n-1]) : indispensable en mémoire/temps pour n grand
        IntVar[] colonnes = model.intVarArray("reine_", n, 0, n - 1, true);
        IntVar[] diagMontantes = new IntVar[n];
        IntVar[] diagDescendantes = new IntVar[n];
        for (int i = 0; i < n; i++) {
            diagMontantes[i] = colonnes[i].add(i).intVar();
            diagDescendantes[i] = colonnes[i].sub(i).intVar();
        }
        model.post(
                model.allDifferent(colonnes, "BC"),
                model.allDifferent(diagMontantes, "BC"),
                model.allDifferent(diagDescendantes, "BC")
        );
        System.out.println("nb contraintes = " + model.getCstrs().length);

        Solver solver = model.getSolver();
        // variable ET valeur choisies au hasard : c'est ce qui rend les restarts réellement utiles
        solver.setSearch(Search.randomSearch(colonnes, seed));

        // redémarrages en loi de Luby : grâce au hasard ci-dessus, chaque restart tente une branche différente
        solver.setRestarts(
                new FailCounter(model, 100),
                new LubyCutoff(100),
                1_000_000
        );

        Solution solution = solver.findSolution();

        if (solution != null) {
            int[] res = new int[n];
            for (int i = 0; i < n; i++) {
                res[i] = solution.getIntVal(colonnes[i]);
            }
            System.out.println("Nb de backtracks : " + solver.getBackTrackCount());
            System.out.println("Nb de restarts : " + solver.getRestartCount());
            System.out.println("Temps : " + solver.getTimeCount() + "s");
            return res;
        }
        System.out.println("Pas de solution trouvée");
        return null;
    }


    /**
     * draw the board with the queens
     */
    private static void drawBoard(int[] queens) {
        int n = queens.length;
        var black = true;
        for (int queen : queens) {
            for (int j = 0; j < n; j++) {
                if (queen == j) {
                    //draw a red queen
                    System.out.print("\033[31m♛\033[0m");
                } else {
                    // draw an empty square by alternating colors
                    if (black) System.out.print("⬛");
                    else System.out.print("⬜");
                }
                black = !black;
            }
            System.out.println();
            if(n%2==0) black = !black;
        }
    }

    // Programme principal
    public static void main(String[] args) {
        int n = 17;
        int[] positions = nQueens_LocalSearch(n);
//        int[] positions = nQueens_3(n);// nQueens_1(n);
        drawBoard(positions);

        // passage à l'échelle : 500 puis 1000 reines
        for (int grandN : new int[]{500, 1000}) {
            long debut = System.currentTimeMillis();
            int[] grandesPositions = nQueens_grandeTaille(grandN, 0);
            long duree = System.currentTimeMillis() - debut;
            System.out.println(grandN + " reines : solution " + (grandesPositions != null ? "trouvée" : "NON trouvée")
                    + " en " + duree + " ms");
        }
    }


}