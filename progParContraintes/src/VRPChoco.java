import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**exemple classique de résolution du problème du voyageur de commerce (VRP) par chocosolver
 * Très largement inspiré de la documentation choco https://choco-solver.org/
 * */
public class VRPChoco {
    enum Villes {A, B, C, D, E;
        // Distances entre villes
        int[][] dist = {
                {0, 10, 15, 20, 25},
                {10, 0, 35, 25, 30},
                {15, 35, 0, 30, 20},
                {20, 25, 30, 0, 15},
                {25, 30, 20, 15, 0}
        };
        /** Obtient les distances depuis cette ville vers toutes les autres
         * @return tableau des distances
         * */
        int[] getDistances() {
            return dist[this.ordinal()];
        }
    }

    public static void main(String[] args) {
        // Création du modèle
        Model model = new Model("VRP");

        int n = Villes.values().length;



        // next[i] <- prochaine ville après i
        IntVar[] next = model.intVarArray("next", n, 0, n-1);

        // Contrainte de circuit (tour fermé)
        model.circuit(next).post();

        // Calculer la distance totale
        IntVar[] distArcs = new IntVar[n];
        for (Villes v:Villes.values()) {
            int i = v.ordinal();
            distArcs[i] = model.intVar(0, 50);
            // distArcs[i] <- dist[i][next[i]]
            model.element(distArcs[i], v.getDistances(), next[i]).post();
        }
        IntVar distTotale = model.intVar("total", 0, 200);
        model.sum(distArcs, "=", distTotale).post();

        // Minimiser la distance
        model.setObjective(Model.MINIMIZE, distTotale);

        // Résoudre
        if (model.getSolver().solve()) {
            System.out.println("Distance: " + distTotale.getValue());
            System.out.print("Tournée: " + Villes.values()[0]);
            int v = next[0].getValue();
            while (v != 0) {
                //TODO: afficher la ville v
                System.out.print(" -> " + Villes.values()[v]);
                v = next[v].getValue();
            }
            System.out.println(" -> "+ Villes.values()[0]);
        }
    }
}