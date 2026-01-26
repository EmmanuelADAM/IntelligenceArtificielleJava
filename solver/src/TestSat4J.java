import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

import java.util.Arrays;

public class TestSat4J {

    static void test01(){
         int MAXVAR = 1000000;
         int NBCLAUSES = 500000;

         try {
             ISolver solver = SolverFactory.newDefault();
// prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
             solver.newVar(MAXVAR);
             solver.setExpectedNumberOfClauses(NBCLAUSES);
             var clauses = new int[][]{{-1, 2}, {-2, 3}, {1}, {3}};
// Feed the solver using Dimacs format, using arrays of int
// (best option to avoid dependencies on SAT4J IVecInt)
             for (var clause : clauses) {
                 solver.addClause(new VecInt(clause)); // adapt Array to IVecInt
             }

// we are done. Working now on the IProblem interface
             IProblem problem = solver;
             if (problem.isSatisfiable()) {
                 System.out.println("Satisfiable !" + Arrays.toString(problem.model()));
             } else {
                 System.out.println("Unsatisfiable !");
             }
         } catch (Exception e) {
             System.err.println(e);
         }
    }

    public static void main(String[] args) {
        System.out.println("Hello, Sat4J!");
        test01();
    }
}
