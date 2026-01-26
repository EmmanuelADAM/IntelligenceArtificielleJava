import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

public class OptimisationsDemos {
    public static void main(String[] args) {
        violatedConstraints();
//        bestSolution();
    }

/*    private static void bestSolution() {
        // Création du modèle
        Model model = new Model("Simple Optimization Example");

        // Variables
        IntVar x = model.intVar("x", 0, 10);
        IntVar y = model.intVar("y", 0, 10);

        // Contrainte : x + y <= 10
        model.arithm(x, "+", y, "<=", 10).post();

        // Fonction objectif : f = (x - y)^2 + (x + y)^2
        // On crée d'abord (x - y)
        IntVar diff = model.intVar("diff", -10, 10);
        model.arithm(x, "-", y, "=", diff).post();
        // Puis (x - y)²
        IntVar diffSq = model.intVar("diffSq", 0, 100);
        model.times(diff, diff, diffSq).post();

        // On crée  (x + y)
        IntVar plus = model.intVar("plus", 0, 20);
        model.arithm(x, "+", y, "=", plus).post();
        // Puis (x - y)²
        IntVar plusSq = model.intVar("plusSq", 0, 100);
        model.times(plus, plus, plusSq).post();

        // f = diffSq + plusSq
        IntVar f = model.intVar("f", 0, 200);
        model.sum(new IntVar[]{diffSq, plusSq}, "=", f).post();

        model.setObjective(Model.MAXIMIZE, f);
        model.setObjective(Model.MINIMIZE, y);
        // On cherche à minimiser f
        Solution optimal = model.getSolver().findSolution();

        // Affichage du résultat
        if (optimal != null) {
            System.out.println("Solution optimale trouvée :");
            System.out.println("x = " + optimal.getIntVal(x));
            System.out.println("y = " + optimal.getIntVal(y));
            System.out.println("f(x,y) = " + optimal.getIntVal(f));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }
*/
    private static void violatedConstraints() {
        Model model = new Model("Soft Constraints Example");

        // Variables principales
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        IntVar z = model.intVar("z", 0, 5);

        // Poids des contraintes souples : 0 = respectée, w = pénalité
        IntVar[] coutViolation = model.intVarArray("cout_c", 3, 0,5);

        // Contrainte 0: x + y + z = 14
        IntVar sum = model.intVar("sum", 0, 15);
        model.sum(new IntVar[]{x, y, z}, "=", sum).post();
        // cout_violation[0] = 3 si la somme <> 14
        var c0 = model.arithm(sum, "=", 14);
        model.ifThenElse(c0,
                model.arithm(coutViolation[0], "=", 0),
                model.arithm(coutViolation[0], "=", 3));

        // Contrainte 1: x<y,   cout_violation[1] = 1 si x>= y+1
        var c1 = model.arithm(x, "<", y);
        model.ifThenElse(c1,
                model.arithm(coutViolation[1], "=", 0),
                model.arithm(coutViolation[1], "=", 1));

        // Contrainte 2: y<z,   cout_violation[2] = 2 si y>= z+1
        var c2 = model.arithm(y, "<", z);
        model.ifThenElse(c2,
                model.arithm(coutViolation[2], "=", 0),
                model.arithm(coutViolation[2], "=", 2));
        Constraint[] contraintesSouples = {c0,c1,c2};

        //   Fonction objectif : minimiser le nombre total de violations
        // domaine de totalViolations = [0, somme des couts max]
        IntVar totalViolations = model.intVar("coutTotalViolations", 0, coutViolation[0].getUB()*(coutViolation.length+1));
        // totalViolations <- sum(coutViolation)
        model.sum(coutViolation, "=", totalViolations).post();

        // 🔍 Recherche de la meilleure solution, en minimisant totalViolations
        Solution best = model.getSolver().findOptimalSolution(totalViolations, Model.MINIMIZE);

        if (best != null) {
            System.out.println("Meilleure solution trouvée :");
            System.out.printf("x = %d, y = %d, z = %d.\n",best.getIntVal(x),best.getIntVal(y),best.getIntVal(z));
            var nonRespect = false;
            for(int i=0; i<contraintesSouples.length; i++)
            {
                var cout = best.getIntVal(coutViolation[i]);
                if (cout!=0) {
                    nonRespect = true;
                    System.out.printf("contrainte %d non restectée : %s. \n", i,contraintesSouples[i]);
                }
            }
            if(!nonRespect) System.out.println("Toutes les contraintes ont été respectée !!");
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }
}
