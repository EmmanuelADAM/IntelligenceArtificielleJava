import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.objective.ParetoMaximizer;
import org.chocosolver.solver.variables.IntVar;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Optimisation MULTI-OBJECTIF avec Choco : le front de Pareto.
 *
 * <p>Problème : on remplit un sac (un "panier repas") avec des aliments.
 * On veut <b>en même temps</b> :
 * <ul>
 *   <li>MAXIMISER la quantité de glucose (de l'énergie !),</li>
 *   <li>MINIMISER le poids du sac.</li>
 * </ul>
 * Ces deux objectifs sont <b>contradictoires</b> : plus on ajoute d'aliments,
 * plus on gagne du glucose... mais plus le sac est lourd.
 * Il n'existe donc pas UNE meilleure solution, mais un ensemble de compromis.</p>
 *
 * <p><b>Notion de dominance</b> : une solution A <i>domine</i> une solution B si
 * A est au moins aussi bonne que B sur tous les objectifs, et strictement
 * meilleure sur au moins un. Exemple : (glucose=50, poids=20) domine
 * (glucose=45, poids=25).</p>
 *
 * <p><b>Front de Pareto</b> : c'est l'ensemble des solutions qui ne sont dominées
 * par aucune autre. Chacune représente un compromis "raisonnable" ; c'est ensuite
 * à l'humain (le décideur) de choisir celui qui lui convient.</p>
 */
public class Pareto {

    public static void main(String[] args) {
        // ------------------------------------------------------------------
        // 1. LES DONNÉES DU PROBLÈME
        // ------------------------------------------------------------------
        // 4 types d'aliments ; l'indice i de chaque tableau ci-dessous
        // correspond à l'aliment Fruits.values()[i]
        // (0 = Strawberry, 1 = Bananas, 2 = Apples, 3 = Honey)
        enum Fruits {Strawberry, Bananas, Apples, Honey;}
        int[] nbItems = new int[]{5, 2, 6, 7};  // stock disponible : nb max d'unités de chaque aliment
        int[] weights = new int[]{5, 8, 7, 8};  // poids d'une unité de chaque aliment
        int[] lipids = new int[]{5, 9, 8, 1};   // lipides apportés par une unité
        int[] glucose = new int[]{9, 5, 7, 32}; // glucose apporté par une unité
        // Remarque : le miel (Honey) est très intéressant (32 de glucose pour un poids de 8) !

        // Le modèle contient les variables et les contraintes du problème
        Model model = new Model("ParetoKnapsack");

        // ------------------------------------------------------------------
        // 2. LES VARIABLES DE DÉCISION
        // ------------------------------------------------------------------
        // occurrences[i] = combien d'unités de l'aliment i on met dans le sac.
        // Son domaine va de 0 (on n'en prend pas) au stock disponible nbItems[i].
        IntVar[] occurrences = new IntVar[nbItems.length];
        for (int i = 0; i < nbItems.length; i++) {
            occurrences[i] = model.intVar(0, nbItems[i]);
        }

        // ------------------------------------------------------------------
        // 3. LES VARIABLES "CALCULÉES" (totaux)
        // ------------------------------------------------------------------
        // Ces variables ne sont pas choisies librement : leur valeur sera
        // imposée par les contraintes à partir des occurrences.
        // Les bornes des domaines jouent aussi le rôle de contraintes :
        // ici, le poids total ne peut PAS dépasser 80 (capacité du sac).
        int poidsMax = 80;
        int lipidMax = 0;
        int glucoseMax = 0;
        for (int i = 0; i < nbItems.length; i++) {
            lipidMax += nbItems[i] * lipids[i];
            glucoseMax += nbItems[i] * glucose[i];
        }

        IntVar weight = model.intVar(0, poidsMax);         // poids total du sac
        IntVar totalLipids = model.intVar(0, lipidMax);   // total des lipides
        IntVar totalGlucose = model.intVar(0, glucoseMax);  // total du glucose

        // ------------------------------------------------------------------
        // 4. LES CONTRAINTES
        // ------------------------------------------------------------------
        // La contrainte knapsack (sac à dos) impose simultanément :
        //   weight      = somme( occurrences[i] * weights[i] )
        //   totalXxx    = somme( occurrences[i] * xxx[i] )
        // On en pose deux : une pour les lipides, une pour le glucose
        // (elles partagent les mêmes occurrences et le même poids).
        // ATTENTION : une contrainte n'est prise en compte qu'après l'appel à post() !
        model.knapsack(occurrences, weight, totalLipids, weights, lipids).post();
        model.knapsack(occurrences, weight, totalGlucose, weights, glucose).post();

        // ------------------------------------------------------------------
        // 5. LES OBJECTIFS
        // ------------------------------------------------------------------
        // Le solveur Pareto de Choco cherche à optimiser TOUS les objectifs
        // dans le MÊME sens (ici : tout maximiser).
        // Astuce classique : minimiser le poids <=> maximiser (-poids).
        // intView(-1, weight, 0) crée une "vue" égale à -1*weight + 0.
        // Une vue n'est pas une nouvelle variable : c'est juste une autre façon
        // de regarder weight (aucun coût supplémentaire pour le solveur).
        IntVar minusWeight = model.intView(-1, weight, 0);

        // ------------------------------------------------------------------
        // 6. RÉSOLUTION : calcul du front de Pareto
        // ------------------------------------------------------------------
        // findParetoFront explore l'espace de recherche et renvoie la liste
        // de toutes les solutions non dominées pour les objectifs
        // (totalGlucose, -weight), tous deux à MAXIMISER.
        // Les lipides ne sont PAS un objectif ici : ils sont seulement calculés et affichés.
        List<Solution> solutions = model.getSolver().findParetoFront(new IntVar[]{ totalGlucose, minusWeight}, Model.MAXIMIZE);
        System.out.println("There are " + solutions.size() + " Pareto-optimal solutions");

        //tri par poids croissant
        solutions.sort(java.util.Comparator.comparingInt(s -> s.getIntVal(weight)));

        // ------------------------------------------------------------------
        // 7. AFFICHAGE DES COMPROMIS TROUVÉS
        // ------------------------------------------------------------------
        // Chaque solution est un compromis : on ne peut pas augmenter son glucose
        // sans augmenter son poids (sinon elle serait dominée).
        // On remarque d'ailleurs que la solution "sac vide" (W=0, G=0) fait partie du front :
        // aucune autre solution n'est plus légère !
        for (Solution solution : solutions) {
            System.out.println("-----------------------------------");
            // Un objet Solution est une "photo" des valeurs des variables à un instant donné
            System.out.println("W: " + solution.getIntVal(weight) + "\t G:" + solution.getIntVal(totalGlucose) + "\t L: " + solution.getIntVal(totalLipids));
            for (int i = 0; i < Fruits.values().length; i++) {
                System.out.println(Fruits.values()[i].toString() + ": " + solution.getIntVal(occurrences[i]));
            }

            // AUTRE APPROCHE (version "manuelle", plus ancienne) : le ParetoMaximizer.
            // C'est un moniteur que l'on branche sur le solveur AVANT de lancer la recherche
            // (via model.getSolver().solve() dans une boucle) ; à chaque solution trouvée,
            // il ajoute une contrainte interdisant les solutions dominées,
            // puis pareto.getParetoFront() donne le front.
            // ParetoMaximizer pareto = new ParetoMaximizer(new IntVar[]{totalLipids, totalGlucose,});
            // model.getSolver().plugMonitor(pareto);
        }

        // ------------------------------------------------------------------
        // 8. DESSIN DU FRONT DE PARETO (diagramme poids / glucose)
        // ------------------------------------------------------------------
        // On extrait des solutions les deux valeurs objectifs, déjà triées par poids croissant.
        int[] poidsFront = new int[solutions.size()];
        int[] glucoseFront = new int[solutions.size()];
        for (int k = 0; k < solutions.size(); k++) {
            poidsFront[k] = solutions.get(k).getIntVal(weight);
            glucoseFront[k] = solutions.get(k).getIntVal(totalGlucose);
        }
        dessinerFront(poidsFront, glucoseFront, poidsMax, glucoseMax);
    }

    /**
     * Affiche dans une fenêtre Swing le diagramme poids (axe x) / glucose (axe y)
     * des solutions du front de Pareto.
     *
     * <p>Lecture du diagramme : le point "idéal" serait en haut à gauche
     * (poids nul ET glucose maximal), mais il est inaccessible. Les points du front
     * forment la "frontière" la plus proche possible de cet idéal :
     * pour monter (plus de glucose), il faut forcément aller à droite (plus de poids).</p>
     *
     * @param poids      poids de chaque solution du front (triés par ordre croissant)
     * @param gluc       glucose de chaque solution du front
     * @param poidsMax   borne de l'axe des x
     * @param glucoseMax borne de l'axe des y
     */
    static void dessinerFront(int[] poids, int[] gluc, int poidsMax, int glucoseMax) {
        // Swing n'est pas "thread-safe" : on crée la fenêtre dans le thread graphique dédié
        SwingUtilities.invokeLater(() -> {
            JFrame fenetre = new JFrame("Front de Pareto : poids / glucose");
            fenetre.add(new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int marge = 70;
                    int largeur = getWidth() - 2 * marge;
                    int hauteur = getHeight() - 2 * marge;
                    int origineX = marge;
                    int origineY = getHeight() - marge;

                    // Conversion des valeurs "métier" en pixels.
                    // Attention : en Swing, l'axe y est orienté vers le BAS, d'où la soustraction.
                    IntUnaryOperator px = w -> origineX + w * largeur / poidsMax;
                    IntUnaryOperator py = gl -> origineY - gl * hauteur / glucoseMax;

                    // --- Axes et graduations ---
                    g2.setColor(Color.BLACK);
                    g2.drawLine(origineX, origineY, origineX + largeur, origineY); // axe des poids
                    g2.drawLine(origineX, origineY, origineX, origineY - hauteur); // axe du glucose
                    g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                    for (int w = 0; w <= poidsMax; w += 10) {
                        int x = px.applyAsInt(w);
                        g2.drawLine(x, origineY, x, origineY + 5);
                        g2.drawString(String.valueOf(w), x - 6, origineY + 18);
                    }
                    for (int gl = 0; gl <= glucoseMax; gl += 50) {
                        int y = py.applyAsInt(gl);
                        g2.drawLine(origineX - 5, y, origineX, y);
                        g2.drawString(String.valueOf(gl), origineX - 35, y + 4);
                    }
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                    g2.drawString("Poids (à minimiser) →", origineX + largeur / 2 - 70, origineY + 45);
                    g2.drawString("↑ Glucose (à maximiser)", origineX - 50, origineY - hauteur - 20);

                    // --- Le point idéal (inaccessible) : poids 0 et glucose maximal ---
                    g2.setColor(Color.GRAY);
                    int xIdeal = px.applyAsInt(0);
                    int yIdeal = py.applyAsInt(glucoseMax);
                    g2.drawString("★ idéal (inaccessible)", xIdeal + 8, yIdeal + 5);

                    // --- La frontière : on relie les points successifs (triés par poids) ---
                    g2.setColor(new Color(70, 130, 180));
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            10f, new float[]{6f, 4f}, 0f)); // trait en pointillés
                    for (int k = 1; k < poids.length; k++) {
                        g2.drawLine(px.applyAsInt(poids[k - 1]), py.applyAsInt(gluc[k - 1]),
                                px.applyAsInt(poids[k]), py.applyAsInt(gluc[k]));
                    }

                    // --- Les points de Pareto, avec leurs coordonnées (poids, glucose) ---
                    g2.setStroke(new BasicStroke(1f));
                    g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                    int rayon = 5;
                    for (int k = 0; k < poids.length; k++) {
                        int x = px.applyAsInt(poids[k]);
                        int y = py.applyAsInt(gluc[k]);
                        g2.setColor(new Color(200, 40, 40));
                        g2.fillOval(x - rayon, y - rayon, 2 * rayon, 2 * rayon);
                        g2.setColor(Color.DARK_GRAY);
                        g2.drawString("(" + poids[k] + ", " + gluc[k] + ")", x + 7, y + 12);
                    }
                }
            });
            fenetre.setSize(900, 650);
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fenetre.setLocationRelativeTo(null); // fenêtre centrée à l'écran
            fenetre.setVisible(true);
        });
    }

}
