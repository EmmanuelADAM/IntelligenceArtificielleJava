import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.variables.IntVar;
import java.util.Arrays;


public class Main {
static void testNReines()
{
    int n = 10;
    Model model = new Model(n + "-queens problem");
    IntVar[] vars = new IntVar[n];
    for(int q = 0; q < n; q++){
        vars[q] = model.intVar("Q_"+q, 0, n-1);
    }
    for(int i  = 0; i < n-1; i++){
        for(int j = i + 1; j < n; j++){
            model.arithm(vars[i], "!=",vars[j]).post();
            model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
            model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
        }
    }
    System.out.println("nb contraintes = "+ model.getCstrs().length);
    Solution solution = model.getSolver().findSolution();
    if(solution != null){
        System.out.println(solution.toString());
    }
}

    static void testNReines2()
    {
        int n = 10;
        Model model = new Model(n + "-queens problem");
        IntVar[] vars = model.intVarArray("Q", n, 0, n-1, false);

        for(int i  = 0; i < n-1; i++){
            for(int j = i + 1; j < n; j++){
                vars[i].ne(vars[j]).post();
                vars[i].ne(vars[j].sub(j - i)).post();
                vars[i].ne(vars[j].add(j - i)).post();            }
        }
        Solution solution = model.getSolver().findSolution();
        System.out.println("nb contraintes = "+ model.getCstrs().length);
        if(solution != null){
            System.out.println(solution.toString());
        }
    }

    static void testNReines3()
    {
        int n = 500;
        Model model = new Model(n + "-queens problem");
        IntVar[] lignes = model.intVarArray("Q", n, 0, n-1, false);
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for(int i = 0 ; i < n; i++){
            diag1[i] = lignes[i].sub(i).intVar();
            diag2[i] = lignes[i].add(i).intVar();
        }
        model.post(
                model.allDifferent(lignes),
                model.allDifferent(diag1),
                model.allDifferent(diag2)
        );
        //afficher le nb de contraintes
        System.out.println("nb contraintes = "+ model.getCstrs().length);
        //afficher le nb de variables
        System.out.println("nb variables = "+ model.getVars().length);

        Solver s = model.getSolver();
        s.setLNS(INeighborFactory.random(lignes), new FailCounter(s, 100));
        Solution solution = s.findSolution();
        if(solution != null){
            System.out.println(solution.toString());
        }
    }

    static void testCarreMagique() {
        int n = 8;
        int n2 = n * n;
        Model model = new Model("MAGIC SQUARE");
        IntVar[] nb = new IntVar[n2];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                nb[i * n + j] = model.intVar("c" + i + "" + j, 1, n2, false);
            }
        }

        IntVar but = model.intVar(n *(n*n + 1)/2);
        model.allDifferent(nb).post();

        System.out.println("but = " + but);
        //contraintes de lignes
        for(int i=0; i<n; i++) {
            IntVar[] ligne = Arrays.copyOfRange(nb, i*n, (i+1)*n);
            model.post(model.sum(ligne, "=", but));
        }
        //contraintes de colonnes
        for(int j=0; j<n; j++) {
            IntVar[] colonne = new IntVar[n];
            for(int i=0; i<n; i++) {
                colonne[i] = nb[i*n+j];
            }
            model.post(model.sum(colonne, "=", but));
            }

        Solution solution = model.getSolver().findSolution();
        if (solution != null) {
            System.out.println(solution);
            //affiche la solution sous forme d'un carre
            for(int i=0; i<n; i++) {
                for (int j = 0; j < n; j++) {
                    System.out.print(solution.getIntVal(nb[i * n + j]) + "\t");
                }
                System.out.println();
            }
        }
    }

    static void testOptimisation(){
    //les taches 1,2 et 3 doivent etre realisées dans l'ordre 1,2,3
        // 1 a une durée de 4 jours, 2 dure 3j, 3 dure 2j
        //il faut minimise le temps de realisation du projet
        Model model = new Model("Projet");
        IntVar[] dates = new IntVar[7];
        for(int i=0; i<3; i++){
            dates[i*2] = model.intVar("d"+(i+1), 0, 20, false);
            dates[i*2+1] = model.intVar("f"+(i+1), 0, 20, false);
        }
        model.arithm(dates[1], "=", dates[0], "+", 4).post();
        model.arithm(dates[2], ">=", dates[1]).post();
        model.arithm(dates[3], "=", dates[2], "+", 3).post();
        model.arithm(dates[4], ">=", dates[3]).post();
        model.arithm(dates[5], "=", dates[4], "+", 2).post();
        //minimise dates[5]
        model.setObjective(Model.MINIMIZE, dates[5]);
        Solution solution = model.getSolver().findSolution();
        if (solution != null) {
            System.out.println(solution);
        }


    }

    public static void main(String[] args) {
        testOptimisation();
    }
}