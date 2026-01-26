package P4;

import java.util.*;

import static  java.lang.Math.min;
import static  java.lang.Math.max;


/**noeud d'un arbre pour algo MCTS*/
public class P4MCTSNode {
    /**objet pour selection au hasard*/
    private static Random r = new Random();
    /**ia=true si noeud joué par IA*/
    private boolean ia = true;
    /**fils du noeud*/
    private List<P4MCTSNode> children = null;
    /**noeud terminal = partie gagnee, perdue ou nulle*/
    private boolean terminal = false;
    /**feuille -> noeud genere ne correspondant pas à une fin de jeu*/
    private boolean leaf = true;
    /**nb de fois que le noeud a été visite*/
    private  double nbVisites = 0;
    /**somme des valeurs du noeud et de ses fils*/
    private double sommeGains = 0;
    /**small value to slect when exaequo*/
    private static double epsilon = 1e-6;
    /**parent du noeud*/
    private P4MCTSNode parent;
    /**niveau du noeud*/
    private int level;
    /**no unique*/
    private int no;
    /**nb de noeuds*/
    private static int nb=0;
    // donnees typique à l'application, au jeu, ici le tictactoe ou aligne-3


    /**grille sous forme de tableau d'entiers (1 pour jeu IA, 2 pour jeu humain, 0 pour case vide)*/
    private int[][] grille = new int[6][7];
    /**action  = no de colonne jouée dans le noeud */
    private  int action = 0;
    /**ligne  = no de ligne jouée dans le noeud */
    private  int ligne= 0;
    /**ligne faite par IA*/
    private static final int[] ligneAI = {1,1,1,1};
    /**ligne faite par Humain*/
    private static final int[] ligneHumain = {2,2,2,2};
    /**valeur pour perte de match*/
    private static int PERTE = -10;


    P4MCTSNode(){no=nb++;}
    private P4MCTSNode(boolean _ia, int[][] _grille){
        no=nb++;
        ia =_ia;
        for(int i=0; i<6; i++)
            System.arraycopy(_grille[i], 0, grille[i], 0, 7);
    }

    /**à partir du noeud courant, <br>
     * - descendre jusqu'à un noeud feuille en choisissant le ''meilleur'' chemin <br>
     * - en creer des fils et jouer une partie complete
     * */
    void selectAction() {
        terminal = false;
        var node = this;
        while (!node.isLeaf()) {
            node = node.select();
        }
/*        if(!node.terminal ) {
            //si c'est un noeud feuille non terminal.. creer ses fils directs
            node.expand();
            //en choisir un au hasard
            node = node.select();
        }
 */       // jouer jusqu'au bout pour recuperer la valeur de la branche
        // (si le noeud est terminal,
        // alors rollOut fera une mise à jour des parents du noeud avec sa valeur
        rollOut(node);
    }


    /**ajouter des noeuds fils avec chacun une action différente*/
    void expand() {
        leaf = false;
        if(children ==null) children = new LinkedList<>();
        P4MCTSNode child;
        int token=(ia ?1:2);
        for(int i = 0; i< 7; i++)
        {
            int ligne = getPossibleLigne(i);
            if(ligne!=-1)
            {
                child = new P4MCTSNode( !ia, grille);
                child.grille[ligne][i] = token;
                child.action = i;
                child.parent = this;
                child.level = level + 1;
                child.value();
                children.add(child);
            }
        }
    }

    /**retourne la ligne possible en cas de jeu en colonne i (retourne -1 si jeu impossible) */
    private int getPossibleLigne(int i)
    {
        int l=5;
        while (l>=0 && grille[l][i]==0) l--;
        l++;
        if(l==6) l= -1;
        return  l;
    }
    /**choisir un noeud parmi les fils en utilisant les valeurs
     * d'exploitation (somme des gains du fils/ nb de ses visites)
     * et d'exploration racine carree du (log(nb de visite du noeud)/ nb visite du fils)
     * */
    private P4MCTSNode select() {
        P4MCTSNode selectedNode = null;
        var bestValue = Double.NEGATIVE_INFINITY;
        var c = 1.5;
        var value = 0.;
        var arity = arity();
        for (P4MCTSNode child : children) {
            if(child.nbVisites==0) value = r.nextDouble() + Math.sqrt(Math.log(nbVisites+1)) * c;
            else value =
                    child.sommeGains / child.nbVisites +
                            (Math.pow(nbVisites+1, (double) 1 /arity) / child.nbVisites);
            if (value >= bestValue) {
                selectedNode = child;
                bestValue = value;
            }
        }
        selectedNode.parent = this;

        return selectedNode;
    }



    /**determine si le joeur de coef 1 ou 2 a gagne dans ce noeud*/
    boolean isWinner(int coef)
    {
        var ligne = (coef==1?ligneAI:ligneHumain);
        boolean gain = false;
        //test lignes
        for(int i=5; i>=0 && !gain; i--)
            for(int j=0; j<=3; j++)
                gain = gain || Arrays.equals(ligne, 0, 4, grille[i], j, j+4);
        //test colonnes
        if(!gain)
        {
            for(int i=0; i<=2 && !gain; i++)
                for(int j=0; j<7; j++)
                    gain = gain || (grille[i][j]==coef && grille[i+1][j]==coef && grille[i+2][j]==coef && grille[i+3][j]==coef);
        }
        //TODO:test diagonales

        return gain;
    }

    private boolean testDiag(int from, int to, int token)
    {
        var diag = false;
        for (int i=from; i<=to && !diag; i++)
        {
            var k=0;
        }

        return false;
    }

    private boolean isFull()
    {
        var full = true;
        var i=0;
        while (i<7 && full) full = grille[5][i++]!=0;
        return full;
    }

    /**retourne la valeur du noeud et determine s'il est terminal ou non*/
    private double value()
    {
        var val = 0d;
        terminal = true;
        if(isWinner(1)) val = 1;
        else if(isWinner(2)) val = PERTE;// -TicTacToeMCTS.nbGames;
        else if(isFull()) val = 0;
        else terminal = false;
        if(terminal){leaf=true; sommeGains = val;}
        return val;
    }

    /**continuer une partie jusqu'a une victoire ou un match nul :
     * etend le noeud, choisit un fils au hasard, l'étend, etc...*/
    private void rollOut(P4MCTSNode tn) {
//        double val = tn.value();
        P4MCTSNode child = null;
        int token = (!ia?1:2);
        if(!tn.terminal)
        {
            tn.expand();
            int i=0;
            var grandChildren = tn.children;
            while (i<grandChildren.size() && child==null)
            {
                if(grandChildren.get(i).terminal && grandChildren.get(i).isWinner(token))
                    child = grandChildren.get(i);
                i++;
            }
            if (child==null) child = tn.children.get(r.nextInt(tn.arity()));
            if (!child.terminal) rollOut(child);
            else  child.updateValue(child.sommeGains);
            tn = child;
        }
        //arrivé à un noeud terminal, mettre à jour les valeurs des ancetres
    }

    /**incremente le nb de visites et cumule la valeur value au total. demande aux ancetres de faire de même*/
    private void updateValue(double value) {
        //pour eviter les debordements dus aux reels, fixe la valeur au plus à 1 au pire à PERTE
        value = Math.max(PERTE, Math.min(1,value));
        nbVisites++;
        sommeGains += value;
//        out.println("1 visite de plus pour " + "noeud " + no + ", level = "+ level+ ", value = "+ sommeGains);
        //normalement, la valeur est propagee telle quelle, ici j'applique un petit coef reducteur
        if(parent!=null) parent.updateValue(value*0.9);//*0.95);
    }

    /**retourne la dimension du noeud*/
    private int arity() {
        return children == null ? 0 : children.size();
    }

    /**retourne le noeud fils dont la valeur moyenne est maximale */
    P4MCTSNode bestChild()
    {
        P4MCTSNode bestChild = null;
        if(children!=null) {
            var possibleChild = children.stream().max(Comparator.comparingDouble(c -> (c.sommeGains / c.nbVisites) + epsilon * r.nextDouble()));
            if (possibleChild.isPresent()) bestChild = possibleChild.get();
        }
        return bestChild;
    }

    /**retourne le fils correspondant à l'action (0 pour un jeu en (0,0), 1 pour un jeu en (1,0), 8 pour un jeun en (2,2)*/
    P4MCTSNode findChild(int action)
    {
        P4MCTSNode child = null;
        if(children!=null)
        {
            var possibleChild = children.stream().filter(c->c.action == action).findFirst();
            if (possibleChild.isPresent()) child = possibleChild.get();
        }
        return child;
    }

    /**retourne leaf*/
    private  boolean isLeaf() {
        return leaf;
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("noeud ").append(no).append(", level = ").append(level);
        sb.append(", action ").append(this.action).append("\n");
        for(int i=5; i>=0; i--) {
            sb.append("\t+");
            for (int e : grille[i])
                if(e>0)sb.append(e);else sb.append('_');
            sb.append("+");
            sb.append("\n");
        }
        sb.append("valeur=").append(sommeGains).append(" / ").append(nbVisites).append(" visites");
        return sb.toString();
    }

    public int getAction() {  return action;  }
    public int[][] getGrille() {  return grille; }


}
