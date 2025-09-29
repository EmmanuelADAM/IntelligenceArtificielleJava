import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

/**exemple classique de résolution de sudoku par chocosolver*/
public class SudokuChoco {
    /**Taille du problème*/
    static int nbvar = 9;

    //Declaration de la grille
    static int[][] grille;

    static void initGame(){
        //Declaration de la grille
        grille = new int[][]{
                {0, 0, 0, 0, 4, 0, 0, 0, 0},
                {0, 0, 8, 0, 0, 1, 0, 0, 0},
                {9, 0, 0, 7, 2, 0, 0, 0, 3},
                {3, 5, 0, 0, 0, 0, 0, 2, 0},
                {0, 0, 7, 3, 1, 5, 6, 4, 0},
                {4, 9, 0, 0, 0, 7, 0, 0, 0},
                {0, 8, 0, 0, 0, 0, 9, 0, 0},
                {0, 3, 5, 6, 9, 0, 0, 8, 2},
                {0, 0, 0, 0, 0, 0, 1, 0, 0}
        };
    }

    /**affiche la grille de sudoku initiale*/
    static void printGame(){
        for(int i = 0; i < nbvar; i++){
            for(int j = 0; j < nbvar; j++){
                System.out.print(grille[i][j] + " ");
                if ((j + 1) % 3 == 0) System.out.print("| ");
            }
            if ((i + 1) % 3 == 0) System.out.println("\n- - - + - - - + - - -");
            else
                System.out.println();
        }
    }

    /**affiche la solution du sudoku*/
    static void printSolution(Model model){
        var vars = model.getVars();
        for(int i = 0; i < nbvar; i++){
            for(int j = 0; j < nbvar; j++){
                System.out.print(vars[i * nbvar + j].asIntVar().getLB() + " ");
                if ((j + 1) % 3 == 0) System.out.print("| ");
            }
            if ((i + 1) % 3 == 0) System.out.println("\n- - - + - - - + - - -");
             else
            System.out.println();
        }
    }

    /**ajoute les contraintes spécifiques au sudoku*/
    static void createConstraints(Model model){
        //une ligne ne doit pas comporter 2x la même valeur
        IntVar[][] vars = new IntVar[nbvar][nbvar];
        for(int i = 0; i < nbvar; i++){
            for(int j = 0; j < nbvar; j++){
                if(grille[i][j] == 0){
                    //si la variable est libre, son domaine va de à 9
                    vars[i][j] = model.intVar("x_"+i+"_"+j, 1, 9, false);
                } else {
                    //si la variable correspond à une case déjà valuée, son domaine va de cette valeur à cette valeur
                    vars[i][j] = model.intVar("x_"+i+"_"+j, grille[i][j], grille[i][j], true);
                }
            }
            model.allDifferent(vars[i]).post();
        }
        //on ne crée plus les variables contraintes, elles sont maintenant toutes créées
        //une colonne ne doit pas comporter 2x la même valeur
        for(int j = 0; j < nbvar; j++){
            IntVar[] col = new IntVar[nbvar];
            for(int i = 0; i < nbvar; i++){
                col[i] = vars[i][j];
            }
            model.allDifferent(col).post();
        }
        //chaque bloc de 9 cases ne doit comporter qu'une occurrence des valeurs de 1 à 9
        for(int boxRow = 0; boxRow < 3; boxRow++){
            for(int boxCol = 0; boxCol < 3; boxCol++){
                IntVar[] box = new IntVar[nbvar];
                int idx = 0;
                for(int i = 0; i < 3; i++){
                    for(int j = 0; j < 3; j++){
                        box[idx++] = vars[boxRow * 3 + i][boxCol * 3 + j];
                    }
                }
                model.allDifferent(box).post();
            }
        }
    }


    /**cree le model specifique au sudoku*/
    static Model createModel(){
        Model model = new Model("sudoku");
        createConstraints(model);
        return model;
    }


    public static void main(String[] args) {
        initGame();
        var model = createModel();

        Solver solver = model.getSolver();
        // afficher le nb de variables, de contraintes
        solver.showShortStatistics();
        // demander la résolution
        solver.solve();
        //afficher la grille de départ
        printGame();
        System.out.println("-".repeat(20));
        //afficher la solution
        System.out.println("Solution : ");
        printSolution(model);
    }
}
