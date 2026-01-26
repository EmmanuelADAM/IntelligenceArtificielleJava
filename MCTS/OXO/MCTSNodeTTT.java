package OXO;

import java.util.Random;

/**
 * noeud d'un arbre pour algo MCTS pour jeu du TicTacToe (Morpion)
 * joueur IA : pion 1
 * joueur Personne : pion 2
 * 1 | 1 | 2
 * 2 | 1 | 0
 * 0 | 2 | 1
 * se note [1,2,2, 2,1,0, 0,2,1]
 * @author emmanueladam
 */
public class MCTSNodeTTT {
    /**
     * dimension de la grille
     */
    private static final int dim = 3;
    /**
     * objet pour selection au hasard
     */
    private static final Random r = new Random();
    /**
     * valeur d'une feuille si gain de match
     */
    private static final int GAIN = 1;
    /**
     * valeur d'une feuille si match nul
     */
    private static final int NUL = 0;
    /**
     * valeur d'une feuille si perte de match
     */
    private static final int PERTE = -1;
    /**
     * valeur du pion IA dans la grille
     */
    static int valeurIA = 1;
    /**
     * valeur ligne IA (on choisit de multiplier les valeurs des cases des lignes, une ligne IA = valeurIA^dim)
     */
    static int valeurLigneIA = 1;
    /**
     * valeur du pion Personne dans la grille
     */
    static int valeurPersonne = 2;
    /**
     * valeur ligne personne (on choisit de multiplier les valeurs des cases des lignes, une ligne Personne = valeurPersonne^dim)
     */
    static int valeurLignePersonne = 8;
    // donnees typique à l'application, au jeu, ici le tictactoe ou aligne-3
    /**
     * grille de jeu sous forme de tableau d'entiers (valeurIA, valeurPersonne, 0 pour case vide)
     */
    private final int[] grille = new int[9];

    /**
     * fils du noeud
     */
    MCTSNodeTTT[] children = null;
    /**
     * parent du noeud
     */
    MCTSNodeTTT parent;

    /**
     * nb de fois que le noeud a ete visite
     */
    double nbVisites = 0;
    /**
     * somme des valeurs de la descendance du noeud
     */
    double sommeGains = 0;
    /**
     * valeur du noeud si final
     */
    int valeur = -1;
    /**
     * ia=true si noeud joué par IA
     */
    private boolean ia = false;
    /**
     * noeud terminal = partie gagnee, perdue ou nulle
     */
    private boolean terminal = false;
    /**
     * feuille -> noeud genere, sans fils, mais ne correspondant pas necessairement a une fin de jeu
     */
    private boolean leaf = true;
    /**
     * noeud win = le joueur ia a gagne
     */
    private boolean win = false;
    /**
     * nb d'actions maxi faisables a partir de ce noeud  (nb de cases vides (=0)) initialement = 9 pour les 9 cases
     */
    private int nbActions = 9;
    /**
     * action  = no de case joué dans le noeud(0->case en (0,0), 3->case en (0,1), 8->case en (2,2)
     */
    private int action = 0;


    /**constructeur par defaut*/
    MCTSNodeTTT() {
    }

    /** constructor that defines the parameter ia*/
    MCTSNodeTTT(boolean _ia) {
        ia = _ia;
    }


    /**constructeur
     * @param _nbActions nb d'actions realisables a partir de ce noeud
     * @param _ia  indique si le noeud est ia ou non
     * @param _grille grille de jeu qui est recopie dans ce noeud*/
    MCTSNodeTTT(int _nbActions, boolean _ia, int[] _grille) {
        nbActions = _nbActions;
        ia = _ia;
        System.arraycopy(_grille, 0, grille, 0, grille.length);
    }

    /**
     * à partir du noeud courant, <br>
     * - descendre jusqu'à un noeud feuille en choisissant le ''meilleur'' chemin <br>
     * - en creer des fils et jouer une partie complete
     */
    public void selectAction() {
        terminal = false;
        var node = this;
        //on descend jusqu'a une feuille
        while (!node.isLeaf()) {
            node = node.select();
        }
        var newNode = node;
        if (!newNode.terminal) {
            //si c'est un noeud feuille non terminal.. creer ses fils directs
            newNode.expand();
            //en choisir un au hasard
            newNode = newNode.select();
        }


        // jouer une partie jusqu'au bout s'il reste encore une action possible
        if (newNode!=null) rollOut(newNode);
    }


    /**
     * ajouter des noeuds fils avec chacun une action différente
     */
    public void expand() {
        leaf = false;
        if (children == null) children = new MCTSNodeTTT[nbActions];
        MCTSNodeTTT child;
        int choice = 0;
        for (int i = 0; i < nbActions; i++) {
            //creer un fils pour le joeur oppose, recopiant le pere
            child = new MCTSNodeTTT(nbActions - 1, !ia, grille);
            child.parent = this;
            leaf = false;
            // chercher une case vide où placer une marque
            while (grille[choice] != 0) choice++;
            //dire au fils que le pere a jouer en position choice
            putAToken(child, choice);
            //on verifie si s'est un noeud terminal en tentant de le valuer
            child.value();
            children[i] = child;
            choice++;
        }
    }

    /**
     * choisir un noeud parmi les fils en utilisant les valeurs
     * d'exploitation (somme des gains du fils/ nb de ses visites)
     * et d'exploration racine carree du (log(nb de visite du noeud)/ nb visite du fils)
     */
    private MCTSNodeTTT select() {
        MCTSNodeTTT selectedNode = null;
        var bestValue = Double.NEGATIVE_INFINITY;
        var c = 1.5;
        var value = 0.;
        for (MCTSNodeTTT child : children) {
            if (child.isLeaf() && child.win) {
                selectedNode = child;
                break;
            }
            if (child.nbVisites == 0) value = r.nextDouble() + Math.sqrt(Math.log(nbVisites + 1)) * c;
            else value =
                    child.sommeGains / child.nbVisites +
                            Math.sqrt(Math.log(nbVisites + 1) / child.nbVisites) * c;
                            //(Math.pow(nbVisites+1, 1/nbVisites) / child.nbVisites);
            if (value >= bestValue) {
                selectedNode = child;
                bestValue = value;
            }
        }
        if(selectedNode!=null) selectedNode.parent = this;

        return selectedNode;
    }


    /**
     * determine si le joeur ia ou humain a gagne dans ce noeud
     */
    public boolean isWinner(int val) {
        boolean win = false;
        //verifie le produit des lignes et des colonnes
        for(int i=0; i<dim && !win; i++) {
            win = (grille[i*dim] * grille[i*dim+1] * grille[i*dim+2] == val);
            win = win || (grille[i] * grille[i+dim] * grille[i+2*dim] == val);
        }
        //verifie le produit des diagonales
        if(!win) win = (grille[0] * grille[4] * grille[8] == val);
        if(!win) win = (grille[2] * grille[4] * grille[6] == val);
        return win;
    }

    /**
     * retourne la valeur du noeud et determine s'il est terminal ou non
     */
    private double value() {
        int val = 0;
        terminal = false;
        //si c'est un noeud gagnant pour IA
        if (isWinner(MCTSNodeTTT.valeurLigneIA)) {
            terminal = true;
            val = GAIN;
            if (ia) win = true;
        } else if (isWinner(MCTSNodeTTT.valeurLignePersonne)) {
            //si c'est un noeud gagnant pour la personne
            terminal = true;
            val = PERTE;
        } else if (nbActions == 0) {
            //si c'est un noeud pour match nul (aucune place libre et aucun gagnant)
            terminal = true;
            val = NUL;
        }
        //si c'est un noeud terminal; c'est une feuille et on incremente le nb de visite
        //(sinon ce sera fait par un descendant)
        valeur = val;
        if (terminal) {leaf = true; nbVisites++;sommeGains = valeur;}
        return val;
    }

    /**
     * continuer une partie jusqu'a une victoire ou un match nul :
     * etend le noeud, choisit un fils au hasard, l'étend, etc...
     */
    public void rollOut(MCTSNodeTTT tn) {
        MCTSNodeTTT child = null;
        //si le noeud n'est pas terminal, on joue a partir de lui
        if (!tn.terminal) {
            tn.expand();
            int i = 0;
            //si l'ia joue, on verifie si dans les fils direct il y a un gagnant pour l'IA
            var children = tn.children;
            while (i < children.length && child == null) {
                if (children[i].terminal) {
                    if (tn.ia && children[i].valeur == MCTSNodeTTT.valeurIA) child = children[i];
                    if (!tn.ia && children[i].valeur == MCTSNodeTTT.valeurPersonne) child = children[i];
                }
                i++;
            }
            //sinon on en prend un au hasard
            if (child == null) child = tn.children[r.nextInt(tn.arity())];
            // on poursuit le jeu avec ce noeud
            rollOut(child);
        }
        else tn.parent.updateValue(tn.valeur);
        //sinon, on repercute sa valeur le long de son ascendance
    }


    /**
     * placer une marque en position 'action' dans la grille du noeud node
     * @param node noeud dans lequel on joue un pion d'une valeur IA ou Personne
     * @param action position ou le pion doit etre joue
     */
    private void putAToken(MCTSNodeTTT node, int action) {
        var token = (node.ia ? valeurIA: valeurPersonne);
        node.grille[action] = token;
        node.action = action;
    }


    /**
     * incremente le nb de visites et cumule la valeur value au total. demande au parent de faire de même par recursivite
     * @param value valeur a ajouter a la somme des gains obtenus en passant par ce noeud
     */
    public void updateValue(double value) {
        //pour eviter les debordements dus aux reels, fixe la valeur au plus à 1 au pire à PERTE
        //value = Math.max(PERTE, Math.min(GAIN, value));
        nbVisites++;
        sommeGains += value;
        //normalement, la valeur est propagee telle quelle, ici j'applique un petit coef reducteur
        if (parent != null) parent.updateValue(value *0.9);//* 0.95);
    }


    /**
     * @return  le noeud fils vainqueur pour l'IA ou celui dont la valeur moyenne est maximale
     */
    public MCTSNodeTTT bestChild() {
        var max = -1d;
        int arity = arity();
        MCTSNodeTTT bestChild = null;
        for (int i = 0; i <arity ; i++) {
            var child = children[i];
            if (child.terminal && child.ia && child.win) {
                bestChild = child;
                break;
            } else if (child.nbVisites > 0) {
                var interest = child.sommeGains / child.nbVisites;
                if (interest > max) {
                    max = interest;
                    bestChild = child;
                }
            }
        }
        if (bestChild == null && arity>0) bestChild = children[r.nextInt(arity())];
        return bestChild;
    }

    /**
     * retourne le fils correspondant à l'action (0 pour un jeu en (0,0), 1 pour un jeu en (1,0), 8 pour un jeun en (2,2)
     */
    public MCTSNodeTTT findChild(int action) {
        MCTSNodeTTT child = null;
        if (children != null) {
            int i = 0;
            while (child == null && i < children.length) {
                if (children[i].action == action) child = children[i];
                i++;
            }
        }
        return child;
    }

    /**
     * @return  la dimension du noeud (nb de fils, 0 si aucun fils)
     */
    private int arity() {
        return (children == null ? 0 : children.length);
    }

    /**
     * @return vrai si la case "action" est libre dans la grille
     * */
    boolean isFree(int action){
        return grille[action]==0;
    }

    /**
     * retourne leaf
     */
    public boolean isLeaf() {
        return leaf;
    }

    public int getAction() {
        return action;
    }

    public int getNbActions() {
        return nbActions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("niveau ").append(this.nbActions).append("\n");
        for (int i = 0; i < 3; i++) {
            sb.append("\t+");
            for (int j = 0; j < 3; j++)
                sb.append(grille[i * 3 + j]).append("+");
            sb.append("\n");
        }
        sb.append("value=%.2f".formatted(sommeGains)).append(", nb visites=").append(nbVisites);
        return sb.toString();
    }

}