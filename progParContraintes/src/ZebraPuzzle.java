import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class ZebraPuzzle {
    
    
    
    public static void main(String[] args) {
        // Création du modèle
        zebraPuzzle();
    }

    private static int v(Enum<?> e){return e.ordinal();}

    private static void zebraPuzzle() {
        Model model = new Model("Zebra Puzzle");

        // Constants pour les maisons
        enum Couleur  {Bleu, Blanc, Rouge, Verte, Jaune}
        enum Nationalite {Britanique, Suedoise,  Francaise, Espagnole, Italienne}
        enum Animal {Chien, Chat, Gerbille, Perruche, Zebre}
        enum Boisson { Cafe, The, Sirop, Soda, Eau}
        enum Series {PeakyBlinders, Fargo, Crown, HouseOfCards, LeParlement}

        // Variables: position (0-4) pour chaque attribut
        IntVar[] maisonsCouleurs = model.intVarArray("couleurs", 5, 0, 4);
        IntVar[] maisonsNationalites = model.intVarArray("nationalites", 5, 0, 4);
        IntVar[] maisonsAnimaux = model.intVarArray("animaux", 5, 0, 4);
        IntVar[] maisonsBoissons = model.intVarArray("boissons", 5, 0, 4);
        IntVar[] maisonsSeries = model.intVarArray("series", 5, 0, 4);

        // Contrainte: chaque position ne peut être utilisée qu'une fois par catégorie
        model.allDifferent(maisonsCouleurs).post();
        model.allDifferent(maisonsNationalites).post();
        model.allDifferent(maisonsAnimaux).post();
        model.allDifferent(maisonsBoissons).post();
        model.allDifferent(maisonsSeries).post();

        // 1. Le Britannique vit dans la maison rouge
        model.arithm(maisonsNationalites[v(Nationalite.Britanique)], "=", maisonsCouleurs[v(Couleur.Rouge)]).post();

        // 2. La Suédoise a des chiens
        model.arithm(maisonsNationalites[v(Nationalite.Suedoise)], "=", maisonsAnimaux[v(Animal.Chien)]).post();

        // 3. La personne qui boit du café est dans la maison verte
        model.arithm(maisonsBoissons[v(Boisson.Cafe)], "=", maisonsCouleurs[v(Couleur.Verte)]).post();

        // 4. La Française boit du thé
        model.arithm(maisonsNationalites[v(Nationalite.Francaise)], "=", maisonsBoissons[v(Boisson.The)]).post();

        // 5. La maison verte est juste à droite de la maison blanche
        model.arithm(maisonsCouleurs[v(Couleur.Verte)], "=", maisonsCouleurs[v(Couleur.Blanc)], "+", 1).post();

        // 6. La personne qui regarde "peaky blinders" a un chat
        model.arithm(maisonsSeries[v(Series.PeakyBlinders)], "=", maisonsAnimaux[v(Animal.Chat)]).post();

        // 7. Dans la maison jaune, on regarde "Fargo
        model.arithm(maisonsSeries[v(Series.Fargo)], "=", maisonsCouleurs[v(Couleur.Jaune)]).post();

        // 8. La personne qui vit dans la maison du centre boit du sirop.
        model.arithm(maisonsBoissons[v(Boisson.Sirop)], "=", 2).post();

        // 9. L'Espagnol habite dans la première maison en partant de la gauche.
        model.arithm(maisonsNationalites[v(Nationalite.Espagnole)], "=", 0).post();

        // 10. La personne qui regarde "the Crown" vit à côté de celle qui a une Gerbille.
        model.distance(maisonsSeries[v(Series.Crown)], maisonsAnimaux[v(Animal.Gerbille)], "=", 1).post();

        // 11. Il y a une perruche dans la maison voisine de celle où on regarde "Fargo"'.
        model.distance(maisonsSeries[v(Series.Fargo)], maisonsAnimaux[v(Animal.Perruche)], "=", 1).post();

        // 12. La personne qui regarde "House of cards" boit du soda.
        model.arithm(maisonsSeries[v(Series.HouseOfCards)], "=", maisonsBoissons[v(Boisson.Soda)]).post();

        // 13. L'Italienne regarde "Le Parlement".
        model.arithm(maisonsNationalites[v(Nationalite.Italienne)], "=", maisonsSeries[v(Series.LeParlement)]).post();

        // 14. L'Espagnole vit à côté de la maison bleue
        model.distance(maisonsNationalites[v(Nationalite.Espagnole)], maisonsCouleurs[v(Couleur.Bleu)], "=", 1).post();

        // Résolution
        if (model.getSolver().solve()) {
            System.out.println("\nSolution trouvée !");
            // Affichage de la solution
            var couleurs = Couleur.values();
            var nationalites = Nationalite.values();
            var animaux  = Animal.values();
            var boissons = Boisson.values();
            var series = Series.values();
            for (int maison = 0; maison < 5; maison++) {
                System.out.println("\nMaison " + (maison + 1) + ":");
                for (int i = 0; i < 5; i++) {
                    if (maisonsNationalites[i].getValue() == maison) System.out.println("Personne de nationalité : " + nationalites[i]);
                    if (maisonsCouleurs[i].getValue() == maison) System.out.println("Couleur de maison  : " + couleurs[i]);
                    if (maisonsAnimaux[i].getValue() == maison) System.out.println("Animal présent : " + animaux[i]);
                    if (maisonsBoissons[i].getValue() == maison) System.out.println("Boisson trouvée : " + boissons[i]);
                    if (maisonsSeries[i].getValue() == maison) System.out.println("Série regardée  : " + series[i]);
                }
            }

            System.out.println("-".repeat(30));
            // Réponse aux questions du puzzle
            for (int i = 0; i < 5; i++) {
                if (maisonsNationalites[i].getValue() == maisonsAnimaux[v(Animal.Zebre)].getValue()) {
                    System.out.println("\nLe zèbre appartient à la personne de nationalité " + nationalites[i]);
                }
                if (maisonsNationalites[i].getValue() == maisonsBoissons[v(Boisson.Eau)].getValue()) {
                    System.out.println("L'eau est bue par la personne de nationalité " + nationalites[i]);
                }
            }
        }
    }
}