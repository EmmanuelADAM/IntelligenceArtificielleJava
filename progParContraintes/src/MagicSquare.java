import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

public class MagicSquare {

    static void printArray(IntVar[] array, int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(array[i * n + j].getValue() + "\t");
            }
            System.out.println();
        }
    }

    public static void  resolveSquare(int n)
    {
        int n2 = n * n;
        Model model = new Model("MAGIC SQUARE");
        IntVar[] nb = new IntVar[n2];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                nb[i * n + j] = model.intVar("c" + i + "" + j, 1, n2, false);

        model.allDifferent(nb).post();

        IntVar but = model.intVar(n *(n*n + 1)/2);
        System.out.println("but = " + but.getLB());

//contraintes de lignes
        for(int i=0; i<n; i++) {
            IntVar[] ligne = Arrays.copyOfRange(nb, i*n, (i+1)*n);
            model.post(model.sum(ligne, "=", but));
        }
//contraintes de colonnes
        for(int j=0; j<n; j++) {
            IntVar[] colonne = new IntVar[n];
            for(int i=0; i<n; i++) colonne[i] = nb[i*n+j];
            model.post(model.sum(colonne, "=", but));
        }
//contraintes de diagonales
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for(int i=0; i<n; i++) {
            diag1[i] = nb[i*n+i];
            diag2[i] = nb[i*n+(n-i-1)];
        }
        model.post(model.sum(diag1, "=", but));
        model.post(model.sum(diag2, "=", but));

        //improve solving time by symmetry breaking
//        model.arithm(nb[0], "<", nb[n2-1]).post();
        model.getSolver().showShortStatistics();
        Solution solution = model.getSolver().findSolution();

        if (solution != null){
            printArray(nb, n);
        }
    }

    public static void main(String[] args){

        resolveSquare(5);
    }
}
