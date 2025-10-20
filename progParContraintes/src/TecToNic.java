import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;

/** Résolution du puzzle TecToNic avec ChocoSolver .
 * Le puzzle consiste à remplir une grille avec des chiffres en respectant des contraintes spécifiques liées aux régions colorées.
 * Les contraintes sont les suivantes :
 * 1. Certaines cellules ont des valeurs données qui doivent être respectées.
 * 2. Chaque région colorée doit contenir tous les chiffres de 1 à N (N = taille de la région) exactement une fois.
 * 3. Deux cellules adjacentes (horizontalement ou verticalement) ne peuvent pas contenir la même valeur.
 */
public class TecToNic {
    /** Représentation de la grille du puzzle */
    int[][] regions;
    /** Valeurs données dans certaines cellules */
    int[][] clues;
    /** nb de lignes de la grille */
    int rows;
    /** nb de colonnes de la grille */
    int cols;
    /* Taille des régions */
    Map<Integer, Integer> regionSizes;
    /** Modèle ChocoSolver */
    Model model;

    TecToNic(){
        model = new Model("TecToNik Solver");
    }

    /** Initialisation du puzzle avec les régions et les valeurs données */
    void init(int[][] regions, int[][] given){
        this.regions = regions;
        this.clues = given;
        this.rows = regions.length;
        this.cols = regions[0].length;
    }

    /** Définition des variables pour chaque cellule du puzzle */
    private IntVar[][] defineVariables(int[][] regions) {
        // Calculer la taille de chaque région
        regionSizes = new HashMap<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int region = regions[i][j];
                regionSizes.compute(region, (k, v) -> (v == null) ? 1 : v + 1);
            }
        }

        // Créer les variables (valeurs de 1 à la taille de la région)
        IntVar[][] grid = new IntVar[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int region = regions[i][j];
                int maxVal = regionSizes.get(region);
                grid[i][j] = model.intVar("cell_" + i + "_" + j, 1, maxVal);
            }
        }
        return grid;
    }


    /** Définition des contraintes du puzzle */
    private void defineConstraints(IntVar[][] grid ) {
        // Contrainte 1 : Valeurs indices données dans l'énoncé
        for (int[] hint : clues) {
            model.arithm(grid[hint[0]][hint[1]], "=", hint[2]).post();
        }

        // Contrainte 2 : Dans chaque région, tous les chiffres de 1 à N doivent apparaître exactement une fois
        // N = dimension de la région
        for (int region = 0; region < 5; region++) {
            IntVar[] regionCells = new IntVar[regionSizes.get(region)];
            int k=0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (regions[i][j] == region) {
                        regionCells[k++] = grid[i][j];
                    }
                }
            }
            // AllDifferent dans la région
            model.allDifferent(regionCells).post();
        }

        // Contrainte 3: Deux cellules adjacentes (horizontalement ou verticalement)
        // ne peuvent pas contenir la même valeur
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Vérifier à droite (pas besoin de vérifier à gauche, car ce sera fait par la cellule de gauche)
                if (j + 1 < cols) {
                    model.arithm(grid[i][j], "!=", grid[i][j + 1]).post();
                    if (i + 1 < rows)  model.arithm(grid[i][j], "!=", grid[i + 1][j+1]).post();
                    if (i > 0)  model.arithm(grid[i][j], "!=", grid[i - 1][j+1]).post();
                }
                // Vérifier en bas (pas besoin de vérifier en haut, car ce sera fait par la cellule du dessus)
                if (i + 1 < rows) {
                    model.arithm(grid[i][j], "!=", grid[i + 1][j]).post();
                }
            }
        }

    }

    /** Résolution du puzzle et affichage de la solution */
    private  void solve(IntVar[][] grid) {
        // Résoudre
        if (model.getSolver().solve()) {
            System.out.println("Solution trouvée:\n");

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    System.out.print(grid[i][j].getValue() + " ");
                }
                System.out.println();
            }

            System.out.println("\nAvec les couleurs:");
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    //affiche la valeur avec la police de la couleur de la région
                    // en utilisant les caractères spéciaux rose = \033[35m, violet = \033[34m, jaune = \033[33m, noir = \033[30m
                    // réinitialiser la couleur après chaque impression avec "\033[0m"
                    String color = switch(regions[i][j]%7) {
                        case 0 -> "\033[30m";
                        case 1 -> "\033[31m";
                        case 2 -> "\033[32m";
                        case 3 -> "\033[33m";
                        case 4 -> "\033[34m";
                        case 5 -> "\033[35m";
                        case 6 -> "\033[36m";
                        default -> "?";
                    };
                    System.out.print(color + grid[i][j].getValue() + "|" + "\033[0m");
                }
                System.out.println();
            }

            System.out.println("\nStatistiques:");
            System.out.println("Noeuds: " + model.getSolver().getNodeCount());
            System.out.println("Temps: " + model.getSolver().getTimeCount() + "s");
        } else {
            System.out.println("Aucune solution trouvée!");
        }
    }


    /** Fonction principale : définit la grille et lance la résolution*/
    public static void main(String[] args) {
        TecToNic puzzle = new TecToNic();
        // Définition des régions
        int[][] regions = {
                {0, 0, 0, 1, 1},
                {2, 0, 3, 1, 1},
                {2, 2, 3, 3, 1},
                {2, 2, 3, 3, 4},
                {5, 5, 4, 4, 4},
                {6, 5, 5, 5, 4},
                {6, 7, 8, 8, 8},
                {7, 7, 8, 8, 9},
                {7, 7, 9, 9, 9},
                {10, 10, 11, 11, 9},
                {10, 11, 11, 11, 12},
        };
        // Contraintes des chiffres donnés (ligne, colonne, valeur)
        int[][] given = {
                {0, 2, 3},
                {0, 4, 5},
                {2, 2, 1},
                {3, 0, 4},
                {4, 2, 1},
                {5, 1, 4},
                {7, 0, 3},
                {8, 2, 1},
                {8, 4, 2}
        };

        puzzle.init(regions, given);

        IntVar[][] grid = puzzle.defineVariables(regions);

        puzzle.defineConstraints(grid);

        puzzle.solve(grid);
    }


}