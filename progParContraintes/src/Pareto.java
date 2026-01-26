import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.objective.ParetoMaximizer;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;

public class Pareto {

    public static void main(String[] args) {
        enum Fruits {Strawberry, Bananas, Apples, Honey;}
        // We handle 4 types of food, described below
        int[] nbItems = new int[]{5, 2, 6, 7}; // number of items for each type
        int[] weights = new int[]{5, 8, 7, 8}; // weight of an item for each type
        int[] lipids = new int[]{5, 9, 8, 1}; // quantity of lipids of the item
        int[] glucose = new int[]{9, 5, 7, 32}; // quantity of glucose of the item

        Model model = new Model("ParetoKnapsack");

        // For each type, we create a variable for the number of occurrences
        IntVar[] occurrences = new IntVar[nbItems.length];
        for (int i = 0; i < Fruits.values().length; i++) {
            occurrences[i] = model.intVar(0, nbItems[i]);
        }

        // Total weight of the solution.
        IntVar weight = model.intVar(0, 80);
        // Total of lipids
        IntVar totalLipids = model.intVar(0, 200);
        // Total of glucose
        IntVar totalGlucose = model.intVar(0, 200);

        // We add two knapsack constraints to the solver
        // Beware : call the post() method to save it
        model.knapsack(occurrences, weight, totalLipids, weights, lipids).post();
        model.knapsack(occurrences, weight, totalGlucose, weights, glucose).post();
        IntVar minusWeight = model.intView(-1, weight, 0);

        // Optimise independently two variables using the Pareto optimizer
        List<Solution> solutions = model.getSolver().findParetoFront(new IntVar[]{ totalGlucose, minusWeight}, Model.MAXIMIZE);
        for (Solution solution : solutions) {
            System.out.println("-----------------------------------");
            System.out.println("W: " + solution.getIntVal(weight) + "\t G:" + solution.getIntVal(totalGlucose) + "\t L: " + solution.getIntVal(totalLipids));
            for (int i = 0; i < Fruits.values().length; i++) {
                System.out.println(Fruits.values()[i].toString() + ": " + solution.getIntVal(occurrences[i]));
            }
            System.out.println("There are " + solutions.size() + " Pareto-optimal solutions");
            // To minimize the weight in the same time, we take its opposite by using the choco's minus view
            ParetoMaximizer pareto = new ParetoMaximizer(new IntVar[]{totalLipids, totalGlucose,});
            model.getSolver().plugMonitor(pareto);
        }
    }

}