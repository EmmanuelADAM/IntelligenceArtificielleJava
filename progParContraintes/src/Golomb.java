import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

public class Golomb {

    static void golombChoco(int n){

// A new model instance
        Model model = new Model("Golomb ruler");
// VARIABLES
// set of marks that should be put on the ruler
        IntVar[] ticks = model.intVarArray("a", n, 0, (int)Math.pow(n,2), false);
// set of distances between two distinct marks
        IntVar[] diffs = model.intVarArray("d", (n * (n - 1)) / 2, 0, (int)Math.pow(n,2), false);

// CONSTRAINTS
// the first mark is set to 0
        model.arithm(ticks[0], "=", 0).post();

        for (int i = 0, k = 0 ; i < n - 1; i++) {
            // // the mark variables are ordered
            model.arithm(ticks[i + 1], ">", ticks[i]).post();
            for (int j = i + 1; j < n; j++, k++) {
                // declare the distance constraint between two distinct marks
                model.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, "=", diffs[k]).post();
                // redundant constraints on bounds of diffs[k]
                model.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2).post();
                model.arithm(diffs[k], "<=", ticks[n - 1], "-", ((n - 1 - j + i) * (n - j + i)) / 2).post();
            }
        }
// all distances must be distinct
        model.allDifferent(diffs, "BC").post();
//symmetry-breaking constraints
        model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();
        model.setObjective(Model.MINIMIZE, ticks[n-1]);

        Solution solution = model.getSolver().findSolution();
        if (solution != null) {
            System.out.println(solution);
        }

    }
    static void golombChocoPerso(int n){

// A new model instance
        Model model = new Model("BenVoyonMonGolomb");
// VARIABLES
// set of marks that should be put on the ruler
        IntVar[] ticks = model.intVarArray("a", n, 0, (int)Math.pow(n,2), true);
// set of distances between two distinct marks
        IntVar[] diffs = model.intVarArray("d", (n * (n - 1)) / 2, 0, (int)Math.pow(n,2), false);

// CONSTRAINTS
// the first mark is set to 0
        model.arithm(ticks[0], "=", 0).post();

        for (int i = 0, k = 0 ; i < n - 1; i++) {
            // // the mark variables are ordered
            model.arithm(ticks[i + 1], ">", ticks[i]).post();
            for (int j = i + 1; j < n; j++, k++) {
                // declare the distance constraint between two distinct marks
                model.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, "=", diffs[k]).post();
            }
        }
// all distances must be distinct
        model.allDifferent(diffs, "BC").post();
//symmetry-breaking constraints
        model.setObjective(Model.MINIMIZE, ticks[n-1]);

        var solutions = model.getSolver().findAllSolutions();
        if (solutions != null) {
            for(Solution s : solutions){
                System.out.println(s);
            }
        }
        System.out.println("-".repeat(30));

        var solution = model.getSolver().findOptimalSolution(ticks[n-1],false );
        if (solution != null) {
                System.out.println(solution);
        }


    }

    public static void main(String[] args) {
        golombChocoPerso(4);
    }
}
