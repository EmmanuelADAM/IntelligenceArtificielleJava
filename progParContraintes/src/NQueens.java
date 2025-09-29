import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
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
     * ecriture "facile"*/
    static int[] nQueens_1(int n)
    {
        Model model = new Model("probleme des "+ n + " reines");
        IntVar[] colonnes = model.intVarArray("reine_", n, 0,n-1, false);

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
     * ecriture des contraintes plus "classique" */
    static int[] nQueens_2(int n)
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

        Solution solution = model.getSolver().findSolution();

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
     * plus de variables, moins de  contraintes
     * ici des variables contiennent les indices des diagonales montantes et descendantes pour chaque reine
     * donc toutes les colonnes, toutes les diagonales doivent être differentes pour tout i,j avec i <> j </>*/
    static int[] nQueens_3(int n)
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
     * ecriture "facile"*/
    static int[] nQueens_4(int n)
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
        System.out.println("nb contraintes = "+ model.getCstrs().length);

        Solver s = model.getSolver();
        s.setSearch(Search.intVarSearch(
        // selects the variable of smallest domain size
                new FirstFail(model),
        // selects the smallest domain value (lower bound)
                new IntDomainMin(),
        // variables to branch on
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


    /**résolution du problème des n reines par recherche locale
     * configuration du mode de sélection de variables et du mode de recherche*/
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

        // Configuration de la recherche locale
        Random random = new Random(0);

        // 1. Générer une solution initiale (aléatoire)
        int[] initialSolution = new int[n];
        for(int i = 0; i < n; i++) {
            initialSolution[i] = random.nextInt(n);
        }

        // Créer la solution initiale dans Choco
        Solution startSolution = new Solution(model);
        for(int i = 0; i < n; i++) {
            startSolution.setIntVal(colonnes[i], initialSolution[i]);
        }

        // 2. Configurer le solver pour la recherche locale
        // Utiliser une stratégie de recherche basée sur la variable avec le plus petit domaine (First Fail)
        // et la valeur minimale dans le domaine
        model.getSolver().setSearch(
                org.chocosolver.solver.search.strategy.Search.intVarSearch(
                        new org.chocosolver.solver.search.strategy.selectors.variables.FirstFail(model),
                        new org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin(),
                        colonnes
                )
        );

        // 3. Utiliser Large Neighborhood Search (LNS)
        // Ici, on utilise une stratégie de voisinage guidée par la propagation
        // qui sélectionne environ n/3 variables à re-assigner à chaque itération de LNS
        model.getSolver().setLNS(
                new org.chocosolver.solver.search.loop.lns.neighbors.PropagationGuidedNeighborhood(
                        colonnes, n/3, n, 0
                )
        );
        // Limiter le nombre d'échecs pour relancer la recherche
        model.getSolver().setRestarts(
                new FailCounter(model, 100),
                new org.chocosolver.solver.search.restart.LubyCutoff(100),
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


    /**
     * draw the board with the queens
     */
    private static void drawBoard(int[] queens) {
        int n = queens.length;
        for (int queen : queens) {
            for (int j = 0; j < n; j++) {
                if (queen == j) {
                    System.out.print(" Q ");
                } else {
                    System.out.print(" . ");
                }
            }
            System.out.println();
        }
    }

    // Programme principal
    public static void main(String[] args) {
        int n = 200;
        int[] positions = nQueens_4(n);// nQueens_1(n);
        drawBoard(positions);


    }


}