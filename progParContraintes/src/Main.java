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

    static void rendreMonaie(int montant)
    {
        Model model = new Model("rendre monnaie");
        IntVar[] nbPieces = new IntVar[4];
        int[] valeurs = {1,2,5,10};
        for(int i=0; i<4; i++){
            nbPieces[i] = model.intVar("nbPieces"+valeurs[i], 0, 10, false);
        }
        IntVar somme = model.intVar("somme", 0, 100, false);
        model.scalar(nbPieces, valeurs, "=", montant).post();
        model . sum( nbPieces , "=" , somme ).post( ) ;
        Solution solution = model.getSolver().findOptimalSolution(somme, Model.MINIMIZE);
        if (solution != null) {
            System.out.println(solution);
        }
    }


    static void atterrissages()
    {
// nb d'avions
        int N = 10;
// fourchettes d'atterissage : {au plus tôt, ciblé, au plus tard}
        int[][] LT = {
                {129, 155, 559},
                {195, 258, 744},
                {89, 98, 510},
                {96, 106, 521},
                {110, 123, 555},
                {120, 135, 576},
                {124, 138, 577},
                {126, 140, 573},
                {135, 150, 591},
                {160, 180, 657}};
// coût de pénalité par minute par avion : { coût / mn atterrissage avancé, coût / mn atterrissage retardé}
        int[][] PC = {
                {10, 10},
                {10, 10},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30},
                {30, 30}};

// marge de temps requise entre atterrissages des avions (3 mn entre avion 1 et avion 0, 15 mn entre avion 2 et avion 0, ....
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

        Model model = new Model("atterrissages");
        IntVar[] atterris = new IntVar[N];
        for(int i=0; i<N; i++){
            // domaine borné directement par la fourchette [au plus tôt, au plus tard] de l'avion
            atterris[i] = model.intVar("avion"+(i+1), LT[i][0], LT[i][2], false);
        }

        // contraintes de séparation : entre deux avions, l'un doit atterrir avant l'autre
        // avec au moins la marge de temps requise (matrice ST) entre les deux
        for(int i=0; i<N; i++){
            for(int j=i+1; j<N; j++){
                model.or(
                        model.arithm(atterris[j], "-", atterris[i], ">=", ST[i][j]),
                        model.arithm(atterris[i], "-", atterris[j], ">=", ST[j][i])
                ).post();
            }
        }

        // coût de chaque avion selon son écart par rapport à son heure ciblée (LT[i][1])
        IntVar[] couts = new IntVar[N];
        IntVar[] avances = new IntVar[N];
        IntVar[] retards = new IntVar[N];
        for(int i=0; i<N; i++){
            // écart = heure d'atterrissage - heure ciblée (négatif si en avance, positif si en retard)
            IntVar ecart = atterris[i].sub(LT[i][1]).intVar();
            avances[i] = model.intVar("avance"+(i+1), 0, LT[i][1]-LT[i][0]);
            retards[i] = model.intVar("retard"+(i+1), 0, LT[i][2]-LT[i][1]);
            model.max(retards[i], ecart, model.intVar(0)).post();
            model.max(avances[i], ecart.neg().intVar(), model.intVar(0)).post();

            couts[i] = model.intVar("cout"+(i+1), 0, 100000);
            model.scalar(new IntVar[]{avances[i], retards[i]}, new int[]{PC[i][0], PC[i][1]}, "=", couts[i]).post();
        }

        IntVar coutTotal = model.intVar("coutTotal", 0, 1000000);
        model.sum(couts, "=", coutTotal).post();
        model.setObjective(Model.MINIMIZE, coutTotal);

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

    static void keyGen()
    {

        Model model = new Model("keyGen");
        //longueur de la clé
        int L = 15;
        int nbMaj = 4;
        int nbMin = 4;
        int nbNum = 2;
        int nbSpec = 2;
        // deux majuscules sont dans les positions entre 0 et 15
        IntVar[] keyMaj = model.intVarArray("maj", nbMaj, 0, L-1, false);
        // deux minuscules sont dans les positions entre 0 et 15
        IntVar[] keyMin = model.intVarArray("min", nbMin, 0, L-1, false);
        // deux chiffres sont dans les positions entre 0 et 15
        IntVar[] keyNum = model.intVarArray("num", nbNum, 0, L-1, false);
        // deux caractères spéciaux sont dans les positions entre 0 et 15
        IntVar[] keySpec = model.intVarArray("spec", nbSpec, 0, L-1, false);
        //ensemble de toutes les positions
        IntVar[] keyAll = new IntVar[nbMaj + nbMin + nbNum + nbSpec];
        System.arraycopy(keyMaj, 0, keyAll, 0, nbMaj);
        System.arraycopy(keyMin, 0, keyAll, nbMaj, nbMin);
        System.arraycopy(keyNum, 0, keyAll, nbMaj + nbMin, nbNum);
        System.arraycopy(keySpec, 0, keyAll, nbMaj + nbMin + nbNum, nbSpec);
        // les positions des majuscules, minuscules, chiffres et caractères spéciaux sont toutes différentes
        model.allDifferent(keyAll).post();
        // pour ne pas avoir toujours les mêmes positions, on demande un tirage aléatoire des variables à valuer
        // randomSearch prend en paramètre un tableau de variables et une graine pour le générateur aléatoire
        model.getSolver().setSearch(
                org.chocosolver.solver.search.strategy.Search.randomSearch(keyAll, System.currentTimeMillis())
        );
        Solution solution = model.getSolver().findSolution();
        if (solution != null) {
            System.out.println(solution);
            //tableau des majuscules, minuscules, chiffres et caractères spéciaux
            //generer une chaine aléatoire en java
            String Majuscule = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String Minuscule = "abcdefghijklmnopqrstuvwxyz";
            String Chiffre = "0123456789";
            String Special = "!@#$%^&*()_+-=[]{}|;':\",.<>/?";
            String total = Majuscule + Minuscule + Chiffre + Special;
            //creaion d'un tableau de 15 caractères pour la clé
            char[] key = new char[L];
            for (int i = 0; i < L; i++) {
                key[i] = total.charAt((int) (Math.random() * total.length()));
            }

            //remplissage du tableau avec des majusules, minuscules, chiffres et caractères aléatoirement
            for (int i = 0; i < nbMaj; i++)
                key[solution.getIntVal(keyMaj[i])] = (char) ('A' + (int) (Math.random() * 26));
            for (int i = 0; i < nbMin; i++)
                key[solution.getIntVal(keyMin[i])] = (char) ('a' + (int) (Math.random() * 26));
            for (int i = 0; i < nbNum; i++)
                key[solution.getIntVal(keyNum[i])] = (char) ('0' + (int) (Math.random() * 10));
            for (int i = 0; i < nbSpec; i++)
                key[solution.getIntVal(keySpec[i])] = Special.charAt((int) (Math.random() * Special.length()));
            System.out.println("clé générée : " + new String(key));
        }



    }


    public static void main(String[] args) {
//        keyGen();
        atterrissages();
//        rendreMonaie(13);
    }
}