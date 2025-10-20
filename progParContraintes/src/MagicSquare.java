import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

public class MagicSquare {


    /**
     * * * Résout le carré magique de taille n x n
     * @param n taille du carré
     * @param assisted si true, utilise des contraintes assistées pour faciliter la résolution
     * */
    public static void resolveSquare(int n, boolean assisted) {
        Model model = new Model("MAGIC SQUARE");

        IntVar[] numbers = generateVariables(n, model);

        generateConstraints(n, model, numbers);

        var solver = model.getSolver();

        if (assisted) {
            model.arithm(numbers[1], "<", numbers[n]).post();
            if (n % 2 == 1) {
                //exemple de contraintes assistées pour n impair
                //on fixe la première cellule à 1
                numbers[0].eq(1).extension().post();
            } else {
                //exemple de contraintes assistées pour n pair
                //ici on fixe la première cellule à n*n
                numbers[0].eq(n * n).extension().post();
            }
            //choix de la variable qui cumule le plus de conflits
            solver.setSearch(Search.activityBasedSearch(numbers));
        }

        solver.showShortStatistics();
        Solution solution = solver.findSolution();

        if (solution != null) {
            printArray(numbers, n);

        }
    }

    /**
     * * Génère les variables pour le carré magique
     * @param n nb de lignes
     * @param model le modèle ChocoSolver
     * @return le tableau des variables
     */
    private static IntVar[] generateVariables(int n, Model model) {
        int n2 = n * n;
        IntVar[] nb = new IntVar[n2];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                nb[i * n + j] = model.intVar("c" + i + "." + j, 1, n2, false);
        return nb;
    }

    /**
     * * Génère les contraintes pour le carré magique
     * @param n nb de lignes0
     * @param model le modèle ChocoSolver
     * @param numbers le tableau des variables
     */
    private static void generateConstraints(int n, Model model, IntVar[] numbers) {
        //contrainte d'unicité
        model.allDifferent(numbers).post();

        //contrainte de somme magique
        IntVar but = model.intVar(n * (n * n + 1) / 2);
        System.out.println("but = " + but.getLB());

        //contraintes de lignes
        for (int i = 0; i < n; i++) {
            IntVar[] ligne = Arrays.copyOfRange(numbers, i * n, (i + 1) * n);
            model.post(model.sum(ligne, "=", but));
        }
        //contraintes de colonnes
        for (int j = 0; j < n; j++) {
            IntVar[] colonne = new IntVar[n];
            for (int i = 0; i < n; i++) colonne[i] = numbers[i * n + j];
            model.post(model.sum(colonne, "=", but));
        }
        //contraintes de diagonales
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            diag1[i] = numbers[i * n + i];
            diag2[i] = numbers[i * n + (n - i - 1)];
        }
        model.post(model.sum(diag1, "=", but));
        model.post(model.sum(diag2, "=", but));
    }

    /**
     * * Affiche le carré magique
     * @param array le tableau des variables
     * @param n nb de lignes
     */
    static void printArray(IntVar[] array, int n) {
        System.out.println("-".repeat(4*2*n));
        for (int i = 0; i < n; i++) {
            System.out.print("|\t");
            for (int j = 0; j < n; j++) {
                System.out.print(array[i * n + j].getValue() + "\t|\t");
            }
            System.out.println();
            System.out.println("-".repeat(4*2*n));
        }
    }

    /**
     *  Point d'entrée du programme
     * */
    public static void main(String[] args) {

        resolveSquare(6, true);
    }
}
