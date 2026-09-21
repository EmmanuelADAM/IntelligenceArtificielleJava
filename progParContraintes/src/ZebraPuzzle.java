import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

public class ZebraPuzzle {
    
    
    
    public static void main(String[] args) {
        // Création du modèle
//        zebraPuzzle();
        zebra2();
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
            model.getSolver().printStatistics();
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

    /**zebre2

     Le donneur A+ est à côté du donneur B+.
     Brooke est à une extrémité.
     La femme en chemise noire est à gauche de la femme de 68 kg.
     L'actrice est à côté du chef cuisinier.
     Kathleen a 40 ans.
     La fleuriste est à droite de la femme en chemise violette.
     Le donneur le plus âgé pèse 59 kg.
     Brooke est à côté de Nichole.
     La femme de 35 ans est exactement à gauche de celle de 30 ans.
     Le donneur de 54 kg se situe entre le donneur O- et celui de 68 kg.
     Kathleen est à une extrémité.
     La femme en chemise violette est à droite de celle en chemise verte.
     Le donneur B+ pèse 63 kg.
     La femme la plus jeune est à côté de celle de 30 ans.
     La femme considérée comme receveuse universelle se trouve exactement à gauche de la donneuse A+.
     Meghan est à droite de la femme en chemise violette.
     La femme en chemise verte se situe entre l'actrice et celle en chemise rouge, dans cet ordre.
     À l'une des extrémités se trouve la femme de 59 kg.
     La donneuse universelle a 35 ans.
     La fleuriste se situe entre l'actrice et l'ingénieur, dans cet ordre.
     La femme en chemise bleue est à gauche de celle en chemise rouge.
     La donneuse AB+ est à côté de la plus jeune femme.*/
    private static void zebra2(){
        Model model = new Model("Puzzle des donneuses");

        enum Prenom {Brooke, Nichole, Kathleen, Meghan, AutrePrenom}
        enum Groupe {APlus, BPlus, OMoins, ABPlus, AutreGroupe}
        enum Poids {Kg68, Kg59, Kg63, Kg54, AutrePoids}
        // PlusJeune / PlusAgee : la plus jeune et la plus âgée, distinctes des 3 femmes d'âge cité
        enum Age {Age30, Age35, Age40, PlusJeune, PlusAgee}
        enum Chemise {Noire, Violette, Verte, Rouge, Bleue}
        enum Metier {Actrice, ChefCuisinier, Fleuriste, Ingenieur, AutreMetier}

        // Variables : position (0-4) pour chaque valeur de chaque catégorie
        IntVar[] prenom = model.intVarArray("prenom", 5, 0, 4);
        IntVar[] groupe = model.intVarArray("groupe", 5, 0, 4);
        IntVar[] poids = model.intVarArray("poids", 5, 0, 4);
        IntVar[] age = model.intVarArray("age", 5, 0, 4);
        IntVar[] chemise = model.intVarArray("chemise", 5, 0, 4);
        IntVar[] metier = model.intVarArray("metier", 5, 0, 4);

        // Contrainte : chaque position ne peut être utilisée qu'une fois par catégorie
        model.allDifferent(prenom).post();
        model.allDifferent(groupe).post();
        model.allDifferent(poids).post();
        model.allDifferent(age).post();
        model.allDifferent(chemise).post();
        model.allDifferent(metier).post();

        // 1. Le donneur A+ est à côté du donneur B+.
        model.distance(groupe[v(Groupe.APlus)], groupe[v(Groupe.BPlus)], "=", 1).post();

        // 2. Brooke est à une extrémité.
        model.or(model.arithm(prenom[v(Prenom.Brooke)], "=", 0), model.arithm(prenom[v(Prenom.Brooke)], "=", 4)).post();

        // 3. La femme en chemise noire est à gauche de la femme de 68 kg.
        model.arithm(chemise[v(Chemise.Noire)], "<", poids[v(Poids.Kg68)]).post();

        // 4. L'actrice est à côté du chef cuisinier.
        model.distance(metier[v(Metier.Actrice)], metier[v(Metier.ChefCuisinier)], "=", 1).post();

        // 5. Kathleen a 40 ans.
        model.arithm(prenom[v(Prenom.Kathleen)], "=", age[v(Age.Age40)]).post();

        // 6. La fleuriste est à droite de la femme en chemise violette.
        model.arithm(metier[v(Metier.Fleuriste)], ">", chemise[v(Chemise.Violette)]).post();

        // 7. Le donneur le plus âgé pèse 59 kg.
        model.arithm(age[v(Age.PlusAgee)], "=", poids[v(Poids.Kg59)]).post();

        // 8. Brooke est à côté de Nichole.
        model.distance(prenom[v(Prenom.Brooke)], prenom[v(Prenom.Nichole)], "=", 1).post();

        // 9. La femme de 35 ans est exactement à gauche de celle de 30 ans.
        model.arithm(age[v(Age.Age30)], "=", age[v(Age.Age35)], "+", 1).post();

        // 10. Le donneur de 54 kg se situe entre le donneur O- et celui de 68 kg (peu importe l'ordre).
        Constraint ordreA = model.and(
                model.arithm(poids[v(Poids.Kg54)], ">", groupe[v(Groupe.OMoins)]),
                model.arithm(poids[v(Poids.Kg54)], "<", poids[v(Poids.Kg68)]));
        Constraint ordreB = model.and(
                model.arithm(poids[v(Poids.Kg54)], "<", groupe[v(Groupe.OMoins)]),
                model.arithm(poids[v(Poids.Kg54)], ">", poids[v(Poids.Kg68)]));
        model.or(ordreA, ordreB).post();

        // 11. Kathleen est à une extrémité.
        model.or(model.arithm(prenom[v(Prenom.Kathleen)], "=", 0), model.arithm(prenom[v(Prenom.Kathleen)], "=", 4)).post();

        // 12. La femme en chemise violette est à droite de celle en chemise verte.
        model.arithm(chemise[v(Chemise.Violette)], ">", chemise[v(Chemise.Verte)]).post();

        // 13. Le donneur B+ pèse 63 kg.
        model.arithm(groupe[v(Groupe.BPlus)], "=", poids[v(Poids.Kg63)]).post();

        // 14. La femme la plus jeune est à côté de celle de 30 ans.
        model.distance(age[v(Age.PlusJeune)], age[v(Age.Age30)], "=", 1).post();

        // 15. La receveuse universelle (AB+) est exactement à gauche de la donneuse A+.
        model.arithm(groupe[v(Groupe.APlus)], "=", groupe[v(Groupe.ABPlus)], "+", 1).post();

        // 16. Meghan est à droite de la femme en chemise violette.
        model.arithm(prenom[v(Prenom.Meghan)], ">", chemise[v(Chemise.Violette)]).post();

        // 17. La chemise verte se situe entre l'actrice et la chemise rouge, dans cet ordre.
        model.arithm(metier[v(Metier.Actrice)], "<", chemise[v(Chemise.Verte)]).post();
        model.arithm(chemise[v(Chemise.Verte)], "<", chemise[v(Chemise.Rouge)]).post();

        // 18. La femme de 59 kg est à une extrémité.
        model.or(model.arithm(poids[v(Poids.Kg59)], "=", 0), model.arithm(poids[v(Poids.Kg59)], "=", 4)).post();

        // 19. La donneuse universelle (O-) a 35 ans.
        model.arithm(groupe[v(Groupe.OMoins)], "=", age[v(Age.Age35)]).post();

        // 20. La fleuriste se situe entre l'actrice et l'ingénieur, dans cet ordre.
        model.arithm(metier[v(Metier.Actrice)], "<", metier[v(Metier.Fleuriste)]).post();
        model.arithm(metier[v(Metier.Fleuriste)], "<", metier[v(Metier.Ingenieur)]).post();

        // 21. La chemise bleue est à gauche de la chemise rouge.
        model.arithm(chemise[v(Chemise.Bleue)], "<", chemise[v(Chemise.Rouge)]).post();

        // 22. La donneuse AB+ est à côté de la plus jeune femme.
        model.distance(groupe[v(Groupe.ABPlus)], age[v(Age.PlusJeune)], "=", 1).post();

        //affiche les domaines après propagation
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            model.getSolver().getEngine().flush();
            System.out.println("Domaines vides : CSP infaisable dès la propagation initiale " + e.getMessage());
        }
        //affiche les domaines après propagation
        for (int i = 0; i < 5; i++) {
            System.out.println(prenom[i]);
            System.out.println(groupe[i]);
            System.out.println(poids[i]);
            System.out.println(age[i]);
            System.out.println(chemise[i]);
            System.out.println(metier[i]);
        }

        // Résolution
        if (model.getSolver().solve()) {
            model.getSolver().printStatistics();
            System.out.println("\nSolution trouvée !");

            var prenoms = Prenom.values();
            var groupes = Groupe.values();
            var poidsVals = Poids.values();
            var ages = Age.values();
            var chemises = Chemise.values();
            var metiers = Metier.values();

            for (int position = 0; position < 5; position++) {
                System.out.println("\nPersonne " + (position + 1) + ":");
                for (int i = 0; i < 5; i++) {
                    if (prenom[i].getValue() == position) System.out.println("Prénom : " + prenoms[i]);
                    if (groupe[i].getValue() == position) System.out.println("Groupe sanguin : " + groupes[i]);
                    if (poids[i].getValue() == position) System.out.println("Poids : " + poidsVals[i]);
                    if (age[i].getValue() == position) System.out.println("Âge : " + ages[i]);
                    if (chemise[i].getValue() == position) System.out.println("Chemise : " + chemises[i]);
                    if (metier[i].getValue() == position) System.out.println("Métier : " + metiers[i]);
                }
            }
        } else {
            System.out.println("Aucune solution trouvée !");
        }
    }



}