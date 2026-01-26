package P4;

import java.util.Scanner;

public class P4MCTS {

    static P4MCTSNode myNode = null;
    static boolean fin = false;

    static int[][]getMatriceJeu()
    {
        return (myNode==null?null:myNode.getGrille());
    }

    static void init()
    {
        var startNode = new P4MCTSNode();
        long starTime = System.currentTimeMillis();
        int n = 10000;
        for (int i=0; i<n; i++) {
            startNode.selectAction();
        }
        System.out.println(n + " played game in " + (System.currentTimeMillis() - starTime) + " ms");
        System.out.println("Je commence..");
        var fin = false;
        myNode = startNode;
        System.out.println("----------");
        System.out.println(myNode);
    }

    public static void main(String[] args) {
        init();
        Scanner sc = new Scanner(System.in);
        var iaGagne = false;
        var humainGagne = false;
        while (!fin)
        {
            var child = myNode.bestChild();
            while(child==null)
            {
                System.out.println("suite non calculee.... je m'y mets");
                for (int i=0; i<10000; i++) myNode.selectAction();
                child = myNode.bestChild();
            }
            myNode = child;
            System.out.println("je choisis l'action " + myNode.getAction());
            System.out.println(myNode);
            System.out.println("----------");
            fin = iaGagne = myNode.isWinner(1);
            if(!fin) {
                System.out.println("Votre choix ?");
                var choix = sc.nextInt();
                var yournode = myNode.findChild(choix);
                if (yournode == null) {
//                    System.out.println("choix non calcule.... je m'y mets");
                    myNode.expand();
                    yournode = myNode.findChild(choix);
                }
                fin = humainGagne = yournode.isWinner(2);
                if (yournode != null) {
                    System.out.println("votre jeu ");
                    myNode = yournode;
                    System.out.println(myNode);
                    System.out.println("----------");
                }
            }
            if(fin) {
                System.out.println("fin du jeu");
                if(iaGagne)
                    System.out.println("j'ai gagne");
                if(humainGagne)
                    System.out.println("vous avez gagne");
            }
            else
                for (int i=0; i<100000; i++) myNode.selectAction();
        }
    }
}

