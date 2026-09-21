import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.restart.LubyCutoff;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
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
            model.getSolver().printStatistics();
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

        System.out.println("nb contraintes = "+ model.getCstrs().length);

        Solver s = model.getSolver();
        s.setSearch(Search.intVarSearch(
                //selectionner la variable avec le plus petit domaine
                new FirstFail(model),
                //selectionner la valeur à assigner parmi la plus petite du domaine
                new IntDomainMiddle(false),
                //les  variables
                colonnes));

        Solution solution = model.getSolver().findSolution();
        model.getSolver().printStatistics();

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


    /**résolution du problème des n reines pour de grandes valeurs de n (500, 1000, ...)
     *
     * une recherche COMPLÈTE (backtracking + propagation) mais RANDOMISÉE,
     *  tant sur le choix des variables que de leurs valeurs,
     *  et couplée avec des redémarrages selon la loi de Luby :
     *  chaque restart explore alors une branche différente de
     *  l'arbre de recherche, ce qui permet d'éviter de rester bloqué dans un sous-arbre sans
     *    solution.
     *    La relance du backtrack est de plus en plus patiente.. ce qui permttra d'explorer des sous-arbres plus profonds,
     *    et au pire de balayer tout l'arbre de recherche
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
        //la faillilimit de 100 signifie que si 100 backtracks sont effectués sans trouver de solution, on redémarre
        // LubyCutoff(100) signifie que la séquence de redémarrage suit la loi de Luby, qui est une séquence de nombres entiers,
        // alternant des nombres courts avec des nombres de plus en plus grands.
        // ceci  permet d'explorer différentes parties de l'arbre de recherche, et d'alterner entre exploration et exploitation.
        //par exemple, la séquence de Luby pour un cutoff de 100 est : 100, 100, 200, 100, 100, 200, 400, 100, 600,...
        //ce qui signifie que le solveur va d'abord essayer de trouver une solution en explorant 100 backtracks,
        // puis redémarrer et explorer à nouveau 100 backtracks, puis redémarrer et explorer 200 backtracks, etc.
        solver.setRestarts(
                new FailCounter(model, 100),
                new LubyCutoff(100),
                1_000_000
        );

        Solution solution = solver.findSolution();
        solver.printStatistics();

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
        int n = 32;
        int[] positions = nQueens_2(n);
        drawBoard(positions);

        positions = nQueens_3(n);
        drawBoard(positions);
        // passage à l'échelle : 500 puis 1000 reines
        for (int grandN : new int[]{500, 1000}) {
            long debut = System.currentTimeMillis();
            int[] grandesPositions = nQueens_grandeTaille(grandN, 0);
            if (grandesPositions!=null) {
                long duree = System.currentTimeMillis() - debut;
                System.out.println(grandN + " reines : solution   en " + duree + " ms");
                System.out.println(Arrays.toString(grandesPositions));
            }
            else {
                System.out.println(grandN + " reines : PAS de solution trouvée");
            }
        }
    }


}