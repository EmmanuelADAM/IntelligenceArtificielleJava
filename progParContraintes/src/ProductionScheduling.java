import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.BoolVar;

import static org.chocosolver.solver.search.strategy.Search.*;

public class ProductionScheduling {

    static void planifTacheTD()
    {
        // Create a new model
        Model model = new Model("planif taches");

        // Parameters
        int numJobs = 6;          // Number of production jobs

        int tempsMax = 12;
        // Decision Variables
        IntVar[] startTime = model.intVarArray("taches", numJobs, 0, tempsMax);
        int[] duree = {1,1,2,1,2,1};

        model.arithm(startTime[5], ">=", startTime[0], "+", duree[0]).post();
        model.arithm(startTime[3], ">=", startTime[1], "+", duree[1]).post();
        model.arithm(startTime[3], ">=", startTime[2], "+", duree[2]).post();
        model.arithm(startTime[4], ">=", startTime[3], "+", duree[3]).post();
        model.arithm(startTime[5], ">=", startTime[4], "+", duree[4]).post();
        // Find and print  solution
        Solver s = model.getSolver();
        s.setSearch(intVarSearch(
                // selects the variable of smallest domain size
                new FirstFail(model),
                // selects the smallest domain value (lower bound)
                new IntDomainMax(),
                // variables to branch on
                startTime
        ));
        System.out.println(model.toString());
        Solution solution = s.findSolution();
        if(solution != null){
            System.out.println(solution.toString());
        }
    }

    static void production()
    {
        // Create a new model
        Model model = new Model("Production Scheduling");

        // Parameters
        int numJobs = 5;          // Number of production jobs
        int numMachines = 3;      // Number of machines
        int horizon = 10;         // Time horizon
        int[] jobDurations = {2, 6, 5, 4, 3};  // Duration of each job
        int[] dueDate = {4, 5, 8, 5, 4};       // Due dates for jobs
        int[] priority = {3, 1, 4, 2, 2};      // Priority of each job (higher = more important)

        // Decision Variables
        IntVar[] startTimes = model.intVarArray("start", numJobs, 0, horizon);
        IntVar[] machines = model.intVarArray("machine", numJobs, 0, numMachines - 1);
        IntVar[] endTimes = model.intVarArray("end", numJobs, 0, horizon);
        BoolVar[][] overlaps = new BoolVar[numJobs][numJobs];

        // Variables for constraint violations
        BoolVar[] deadlineMissed = new BoolVar[numJobs];
        IntVar[] tardiness = new IntVar[numJobs];

        // Calculate end times
        for (int i = 0; i < numJobs; i++) {
            model.arithm(endTimes[i], "=", startTimes[i], "+", jobDurations[i]).post();
            deadlineMissed[i] = model.boolVar("deadline_missed_" + i);
            tardiness[i] = model.intVar("tardiness_" + i, 0, horizon);

            // Calculate tardiness
            model.max(tardiness[i], model.intVar(0), model.intView(1, endTimes[i], -dueDate[i])).post();

            // Link deadline missed with tardiness
            model.arithm(tardiness[i], ">", 0).reifyWith(deadlineMissed[i]);
        }

        // Hard Constraints

        // 1. No overlap between jobs on the same machine
        for (int i = 0; i < numJobs; i++) {
            for (int j = i + 1; j < numJobs; j++) {
                overlaps[i][j] = model.boolVar("overlap_" + i + "_" + j);

                // Jobs on different machines can overlap
                model.ifThen(
                        model.arithm(machines[i], "=", machines[j]),
                        model.or(
                                model.arithm(endTimes[i], "<=", startTimes[j]),
                                model.arithm(endTimes[j], "<=", startTimes[i])
                        )
                );
            }
        }

        // 2. All jobs must complete within the horizon
        for (int i = 0; i < numJobs; i++) {
            model.arithm(endTimes[i], "<=", horizon).post();
        }

        // Optimization Objective
        // Minimize weighted sum of tardiness (priority * tardiness)
        IntVar[] weightedTardiness = new IntVar[numJobs];
        for (int i = 0; i < numJobs; i++) {
            weightedTardiness[i] = model.intVar("weighted_tardiness_" + i, 0, horizon * priority[i]);
            model.times(tardiness[i], priority[i], weightedTardiness[i]).post();
        }

        IntVar totalPenalty = model.intVar("total_penalty", 0, horizon * numJobs * 5);
        model.sum(weightedTardiness, "=", totalPenalty).post();

        // Set optimization objective
        model.setObjective(Model.MINIMIZE, totalPenalty);

        // Find and print optimal solution
        var solver = model.getSolver();
//        solver.setSearch(Search.intVarSearch(new FirstFail(model), new IntDomainMin(), machines));

        Solution bestSolution = solver.findOptimalSolution(totalPenalty, Model.MINIMIZE);

        if (bestSolution != null) {
            System.out.println("Optimal solution found!");
            System.out.println("Total penalty: " + bestSolution.getIntVal(totalPenalty));

            for (int i = 0; i < numJobs; i++) {
                System.out.printf("Job %d: Machine %d, Start %d, End %d, Tardiness %d%n",
                        i,
                        bestSolution.getIntVal(machines[i]),
                        bestSolution.getIntVal(startTimes[i]),
                        bestSolution.getIntVal(endTimes[i]),
                        bestSolution.getIntVal(tardiness[i])
                );
            }
        } else {
            System.out.println("No solution found!");
        }

    }
    public static void main(String[] args) {
        planifTacheTD();
    }
}