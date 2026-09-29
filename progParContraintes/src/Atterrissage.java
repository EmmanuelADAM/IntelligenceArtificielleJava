import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

/**
 * probleme classique de l'atterrissage d'avions
 * Airplane Landing Problem
 * Chaque avion peut atterrir dans une marge date au plus tot, au plus tard et possède une date ideale.
 * Ne pas respecter la date ideale entraine des pénalités.
 * En plus les avions doivent respecter des contraintes de temps entre eux.
 * Les fonctions abordent le probleme progressivement
 * */
public class Atterrissage
{
    /**
     * calculer la date d'atterrissage d'avion.
     * Chaque avion peut atterrir dans une marge date au plus tot, au plus tard et possède une date ideale.
     * Ne pas respecter la date ideale entraine des pénalités.
     * En plus les avions doivent respecter des contraintes de temps entre eux;
     * contraintes variables selon les couples d'avions, par exemple, ceux occupant la meme piste
     * @version 1 : on ne respecte juste que les contraintes de créneaux d'atterrissage
     * */
    static void atterrissagesV1()
    {
        // nb d'avions
        int N = 10;
        // fourchettes d'atterissage : {au plus tôt, ciblé, au plus tard}
        int[][] LT = { {129, 155, 559}, {195, 258, 744}, {89, 98, 510}, {96, 106, 521}, {110, 123, 555}, {120, 135, 576},
                {124, 138, 577}, {126, 140, 573}, {135, 150, 591}, {160, 180, 657}};

        Model model = new Model("atterrissages tranquilles");
        IntVar[] atterris = new IntVar[N];
        for(int i=0; i<N; i++){
            // domaine borné directement par la fourchette [au plus tôt, au plus tard] de l'avion
            atterris[i] = model.intVar("atterrissage-avion-"+(i+1), LT[i][0], LT[i][2], false);
        }

        var solution = model.getSolver().findSolution();

        if (solution != null) {
            for (int i = 0; i < N; i++) {
                System.out.println("avion " + (i + 1) + " atterrit à " + solution.getIntVal(atterris[i]));
            }
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    /**
     * calculer la date d'atterrissage d'avion.
     * Chaque avion peut atterrir dans une marge date au plus tot, au plus tard et possède une date ideale.
     * Ne pas respecter la date ideale entraine des pénalités.
     * En plus les avions doivent respecter des contraintes de temps entre eux;
     * contraintes variables selon les couples d'avions, par exemple, ceux occupant la meme piste
     * @version 2 : on ne respecte juste que les contraintes de créneaux d'atterrissage
     * */
    static void atterrissagesV2()
    {
        // nb d'avions
        int N = 10;
        // fourchettes d'atterissage : {au plus tôt, ciblé, au plus tard}
        int[][] LT = { {129, 155, 559}, {195, 258, 744}, {89, 98, 510}, {96, 106, 521}, {110, 123, 555}, {120, 135, 576},
                {124, 138, 577}, {126, 140, 573}, {135, 150, 591}, {160, 180, 657}};
        // coût de pénalité par minute par avion : { coût / mn atterrissage avancé, coût / mn atterrissage retardé}
        int[][] PC = { {10, 10}, {10, 10}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}};

        Model model = new Model("atterrissages tranquilles");
        IntVar[] atterris = new IntVar[N];
        for(int i=0; i<N; i++){
            // domaine borné directement par la fourchette [au plus tôt, au plus tard] de l'avion
            atterris[i] = model.intVar("atterrissage-avion-"+(i+1), LT[i][0], LT[i][2], false);
        }

        // coût de chaque avion selon son écart par rapport à son heure ciblée (LT[i][1])
        // couts[i] = avance[i]*PC[i][0] (cout de l'avance) + retard[i]*PC[i][1] (cout du retard)
        // remarque, si on est en avance, on n'est pas en retard; mais ainsi la définition est générique
        IntVar[] couts = new IntVar[N];
        IntVar[] avances = new IntVar[N];
        IntVar[] retards = new IntVar[N];
        for(int i=0; i<N; i++){
            // écart = heure d'atterrissage - (substract) heure ciblée (négatif si en avance, positif si en retard)
            IntVar ecart = atterris[i].sub(LT[i][1]).intVar();
            //l'avance est comprise entre 0mn et le délai entre l'arrivée max et l'arrivée idéale
            avances[i] = model.intVar("avance"+(i+1), 0, LT[i][1]-LT[i][0]);
            //le retard est compris entre 0mn et le délai entre l'arrivée au plus tôx et l'arrivée idéale
            retards[i] = model.intVar("retard"+(i+1), 0, LT[i][2]-LT[i][1]);
            // retard est dans [0, ecart]
            model.max(retards[i], ecart, model.intVar(0)).post();
            // avance est dans [0, -ecart] (car ecart alors négatif)
            model.max(avances[i], ecart.neg().intVar(), model.intVar(0)).post();

            //le cout du décalage est dans [0, 1000000] (grosse fourchette à mieux définir par la suite)
            couts[i] = model.intVar("cout"+(i+1), 0, 100000);
            //produit scalaire entre les tableaux de délai et de cout
            model.scalar(new IntVar[]{avances[i], retards[i]}, new int[]{PC[i][0], PC[i][1]}, "=", couts[i]).post();
        }

        //le cout du décalage  total pour tous les avions est dans [0, 1000000] (on voit que cela n'a pas été trop réfléchi)
        IntVar coutTotal = model.intVar("coutTotal", 0, 1000000);
        //une simple somme des couts individuels
        model.sum(couts, "=", coutTotal).post();
        //c'est ce cout à optimiser
        //maintenant on ne demande plus de résoudre, mais de trouver une solution optimale
        Solution solution = model.getSolver().findOptimalSolution(coutTotal, Model.MINIMIZE);

        if (solution != null) {
            System.out.println("coût total = " + solution.getIntVal(coutTotal));
            for (int i = 0; i < N; i++) {
                System.out.println("avion " + (i + 1) + " atterrit à " + solution.getIntVal(atterris[i])
                        + " (ciblé " + LT[i][1] + ", coût = " + solution.getIntVal(couts[i]) + ")");
            }
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    /**
     * calculer la date d'atterrissage d'avion.
     * Chaque avion peut atterrir dans une marge date au plus tot, au plus tard et possède une date ideale.
     * Ne pas respecter la date ideale entraine des pénalités.
     * En plus les avions doivent respecter des contraintes de temps entre eux;
     * contraintes variables selon les couples d'avions, par exemple, ceux occupant la meme piste
     * @version F : on ne respecte toutes les contraintes
     * */
    static void atterrissages()
    {
        // nb d'avions
        int N = 10;
        // Landing Time : fourchettes d'atterrissage : {au plus tôt, ciblé, au plus tard}
        int[][] LT = { {129, 155, 559}, {195, 258, 744}, {89, 98, 510}, {96, 106, 521}, {110, 123, 555}, {120, 135, 576},
                {124, 138, 577}, {126, 140, 573}, {135, 150, 591}, {160, 180, 657}};
        // Penalty Cost : coût de pénalité par minute par avion : { coût / mn atterrissage avancé, coût / mn atterrissage retardé}
        int[][] PC = { {10, 10}, {10, 10}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}, {30, 30}};
        // Separation Time : marge de temps requise entre atterrissages des avions (3 mn entre avion 1 et avion 0, 15 mn entre avion 2 et avion 0, ....
        int[][] ST = {
                {99999, 3, 15, 15, 15, 15, 15, 15, 15, 15},
                {3, 99999, 15, 15, 15, 15, 15, 15, 15, 15},
                {15, 15, 99999, 8, 8, 8, 8, 8, 8, 8},
                {15, 15, 8, 99999, 8, 8, 8, 8, 8, 8},
                {15, 15, 8, 8, 99999, 8, 8, 8, 8, 8},
                {15, 15, 8, 8, 8, 99999, 8, 8, 8, 8},
                {15, 15, 8, 8, 8, 8, 99999, 8, 8, 8},
                {15, 15, 8, 8, 8, 8, 8, 99999, 8, 8},
                {15, 15, 8, 8, 8, 8, 8, 8,  99999, 8},
                {15, 15, 8, 8, 8, 8, 8, 8,  8, 99999}};

        Model model = new Model("atterrissages tranquilles");
        IntVar[] atterris = new IntVar[N];
        for(int i=0; i<N; i++){
            // domaine borné directement par la fourchette [au plus tôt, au plus tard] de l'avion
            atterris[i] = model.intVar("atterrissage-avion-"+(i+1), LT[i][0], LT[i][2], false);
        }

        // contraintes de séparation : entre deux avions, l'un doit atterrir avant l'autre
        // avec au moins la marge de temps requise (matrice ST) entre les deux
        for(int i=0; i<N; i++){
            for(int j=i+1; j<N; j++){
                //soit atterrissage-avion-j - atterrissage-avion-i >= separation nécessaire entre i et j
                //soit atterrissage-avion-i - atterrissage-avion-j >= separation nécessaire entre j et i
                model.or(
                        model.arithm(atterris[j], "-", atterris[i], ">=", ST[i][j]),
                        model.arithm(atterris[i], "-", atterris[j], ">=", ST[j][i])
                ).post();
            }
        }


        // coût de chaque avion selon son écart par rapport à son heure ciblée (LT[i][1])
        // couts[i] = avance[i]*PC[i][0] (cout de l'avance) + retard[i]*PC[i][1] (cout du retard)
        // remarque, si on est en avance, on n'est pas en retard; mais ainsi la définition est générique
        IntVar[] couts = new IntVar[N];
        IntVar[] avances = new IntVar[N];
        IntVar[] retards = new IntVar[N];
        for(int i=0; i<N; i++){
            // écart = heure d'atterrissage - (substract) heure ciblée (négatif si en avance, positif si en retard)
            IntVar ecart = atterris[i].sub(LT[i][1]).intVar();
            //l'avance est comprise entre 0mn et le délai entre l'arrivée max et l'arrivée idéale
            avances[i] = model.intVar("avance"+(i+1), 0, LT[i][1]-LT[i][0]);
            //le retard est compris entre 0mn et le délai entre l'arrivée au plus tôx et l'arrivée idéale
            retards[i] = model.intVar("retard"+(i+1), 0, LT[i][2]-LT[i][1]);
            // retard est dans [0, ecart]
            model.max(retards[i], ecart, model.intVar(0)).post();
            // avance est dans [0, -ecart] (car ecart alors négatif)
            model.max(avances[i], ecart.neg().intVar(), model.intVar(0)).post();

            //le cout du décalage est dans [0, 1000000] (grosse fourchette à mieux définir par la suite)
            couts[i] = model.intVar("cout"+(i+1), 0, 200);
            //produit scalaire entre les tableaux de délai et de cout
            model.scalar(new IntVar[]{avances[i], retards[i]}, new int[]{PC[i][0], PC[i][1]}, "=", couts[i]).post();
        }

        //le cout du décalage  total pour tous les avions est dans [0, 1000000] (on voit que cela n'a pas été trop réfléchi)
        IntVar coutTotal = model.intVar("coutTotal", 0, 10000);
        //une simple somme des couts individuels
        model.sum(couts, "=", coutTotal).post();
        //c'est ce cout à optimiser
        //maintenant on ne demande plus de résoudre, mais de trouver une solution optimale
        Solution solution = model.getSolver().findOptimalSolution(coutTotal, Model.MINIMIZE);

        if (solution != null) {
            System.out.println("coût total = " + solution.getIntVal(coutTotal));
            for (int i = 0; i < N; i++) {
                System.out.println("avion " + (i + 1) + " atterrit à " + solution.getIntVal(atterris[i])
                        + " (ciblé " + LT[i][1] + ", coût = " + solution.getIntVal(couts[i]) + ")");
            }
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

}
