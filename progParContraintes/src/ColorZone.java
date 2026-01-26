import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.Solution;

public class ColorZone {
    static void colorZones() {
        enum Couleur {ROUGE, JAUNE, VERT, BLEU}
        ;
        Model model = new Model("Network Security Zones");

        // Création des variables (une pour chaque zone)
        IntVar zoneAdmin = model.intVar("Zone_Admin", new int[]{Couleur.ROUGE.ordinal(), Couleur.JAUNE.ordinal()});
        IntVar zoneData = model.intVar("Zone_Data", Couleur.ROUGE.ordinal(), Couleur.BLEU.ordinal());
        IntVar zoneWeb = model.intVar("Zone_Web", new int[]{Couleur.VERT.ordinal(), Couleur.BLEU.ordinal()});
        IntVar zoneDev = model.intVar("Zone_Dev", Couleur.ROUGE.ordinal(), Couleur.BLEU.ordinal());
        IntVar zoneTest = model.intVar("Zone_Test", Couleur.JAUNE.ordinal(), Couleur.BLEU.ordinal()); // pas de rouge

        IntVar[] zones = new IntVar[]{ zoneAdmin, zoneData, zoneWeb, zoneDev, zoneTest };
        // Contrainte 1: zones connectées = niveaux différents
        // Zone_Admin connexions
        model.arithm(zoneAdmin, "!=", zoneData).post();
        model.arithm(zoneAdmin, "!=", zoneDev).post();

        // Zone_Data connexions
        model.arithm(zoneData, "!=", zoneWeb).post();
        model.arithm(zoneData, "!=", zoneDev).post();

        // Zone_Web connexions
        model.arithm(zoneWeb, "!=", zoneTest).post();

        // Zone_Dev connexions
        model.arithm(zoneDev, "!=", zoneTest).post();


        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) { System.err.println("Modèle inconsistant ! ");
            System.exit(0);
        }
        System.out.println("=== Domaines après consistance ===");
        for (IntVar z: zones)
            System.out.println(z);

        // Résolution et affichage
        Solution solution = null;

        while ((solution = model.getSolver().findSolution()) != null) {
            System.out.println("Solution trouvée :");
            System.out.println("Zone_Admin  : " + Couleur.values()[solution.getIntVal(zoneAdmin)]);
            System.out.println("Zone_Data   : " + Couleur.values()[solution.getIntVal(zoneData)]);
            System.out.println("Zone_Web    : " + Couleur.values()[solution.getIntVal(zoneWeb)]);
            System.out.println("Zone_Dev    : " + Couleur.values()[solution.getIntVal(zoneDev)]);
            System.out.println("Zone_Test   : " + Couleur.values()[solution.getIntVal(zoneTest)]);
        }
    }


    static void colorZonesPetit() {
        Model model = new Model("Network Zones");
        // Création des variables (une pour chaque zone)
        IntVar A = model.intVar("A", 1, 3);
        IntVar B = model.intVar("B", 1, 3);
        IntVar C = model.intVar("C", 1, 3);
        IntVar D = model.intVar("D", 1, 3);
        IntVar[] tab = {A, B, C, D};
        // Contraintes
        model.arithm(A, "!=", B).post();
        model.arithm(A, "!=", C).post();
        model.arithm(B, "!=", C).post();
        model.arithm(C, "!=", D).post();
        // Résolution et affichage
        Solution solution = model.getSolver().findSolution();

        if (solution != null) {
            System.out.println("Solution trouvée :");
            for (IntVar var : tab) {
                System.out.println(var.getName() + " : " + solution.getIntVal(var));
            }
        }
    }


    public static void main(String[] args) {
        colorZones();
//        colorZonesPetit();
    }
}
