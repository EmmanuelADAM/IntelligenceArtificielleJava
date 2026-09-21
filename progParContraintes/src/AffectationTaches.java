import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

public class AffectationTaches {
    public static void main(String[] args) {
        // Création du modèle
        Model model = new Model("Affectation de tâches");
        // Nombre de tâches et d'ouvriers
        int nbTaches = 4;
        int nbOuvriers = 3;
        // Coût d'affectation de chaque tâche à chaque ouvrier
        int[][] couts = {
                {1, 2, 7},  // Tâche 0
                {9, 4, 5},  // Tâche 1
                {4, 8, 3},  // Tâche 2
                {5, 6, 1}   // Tâche 3
        };

        // Variables : affectation[i] <- ouvrier assigné à la tâche i
        IntVar[] affectation = model.intVarArray("tache", nbTaches, 0, nbOuvriers - 1);
        // Variable de coût total
        IntVar coutTotal = model.intVar("cout_total", 0, 100);

        // Contrainte : calculer le coût total
        IntVar[] coutsAffectes = new IntVar[nbTaches];
        for (int i = 0; i < nbTaches; i++) {
            coutsAffectes[i] = model.intVar("cout_" + i, 0, 10);
            // coutsAffectes[i] <- couts[i][affectation[i]]
            model.element(coutsAffectes[i], couts[i], affectation[i]).post();
        }
        model.sum(coutsAffectes, "=", coutTotal).post();
        // Objectif : minimiser le coût total
        model.setObjective(Model.MINIMIZE, coutTotal);

        // Résolution
        if (model.getSolver().solve()) {
            System.out.println("=== Solution optimale ===");
            for (int i = 0; i < nbTaches; i++) {
                int ouvrier = affectation[i].getValue();
                System.out.printf("Tâche %d -> Ouvrier %d (coût: %d)\n", i, ouvrier, couts[i][ouvrier]);
            }
            System.out.println("Coût total minimal: " + coutTotal.getValue());
        } else {
            System.out.println("Aucune solution trouvée");
        }
    }
}