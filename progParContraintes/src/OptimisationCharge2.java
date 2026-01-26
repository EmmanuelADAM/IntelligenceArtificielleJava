import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.search.strategy.Search;

import java.util.*;

public class OptimisationCharge2 {

    // Structures de données
    /** Station de recharge
     * @param id identifiant
     * @param noeud noeud du graphe où se situe la station
     * @param nbBornes nombre de bornes disponibles
     * @param puissance puissance de charge (unités par minute)
     * */
    record Station(int id, int noeud, int nbBornes, int puissance) {}

    /** Véhicule électrique
     * @param id identifiant
     * @param noeud noeud du graphe où se situe le véhicule
     * @param chargeInitiale charge initiale du véhicule
     * @param capaciteActuelle capacité actuelle du véhicule
     * @param capaciteMax capacité maximale du véhicule
     * */
    record Vehicule(int id, int noeud, int chargeInitiale, int capaciteActuelle, int capaciteMax) {
        // Méthode supplémentaire pour calculer la charge nécessaire
        public int chargeNecessaire() {
            return capaciteMax - chargeInitiale;
        }
    }

    /** Informations de planification pour un véhicule
     * @param vehicule véhicule concerné
     * @param station station assignée
     * @param distance distance au trajet
     * @param debut heure de début de charge
     * @param fin heure de fin de charge
     * @param duree durée de la charge
     * */
    static record  InfoVehicule ( Vehicule vehicule, Station station, int distance, int debut, int fin, int duree){};

    static class Graphe {
        int nbNoeuds;
        int[][] distances; // matrice d'adjacence avec distances

        public Graphe(int nbNoeuds) {
            this.nbNoeuds = nbNoeuds;
            this.distances = new int[nbNoeuds][nbNoeuds];
            // Initialiser avec des grandes valeurs (infini)
            for (int i = 0; i < nbNoeuds; i++) {
                for (int j = 0; j < nbNoeuds; j++) {
                    distances[i][j] = (i == j) ? 0 : 99999;
                }
            }
        }

        public void ajouterArc(int u, int v, int distance) {
            distances[u][v] = distance;
            distances[v][u] = distance; // graphe non orienté
        }

        // Floyd-Warshall pour calculer les plus courts chemins
        public void calculerPlusCourtsChemin() {
            for (int k = 0; k < nbNoeuds; k++) {
                for (int i = 0; i < nbNoeuds; i++) {
                    for (int j = 0; j < nbNoeuds; j++) {
                        if (distances[i][k] + distances[k][j] < distances[i][j]) {
                            distances[i][j] = distances[i][k] + distances[k][j];
                        }
                    }
                }
            }
        }
    }


    // Critère 1 : Minimiser le temps total (makespan) avec planification temporelle
    public static void resoudreMinimiserTemps(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        System.out.println("=== OPTIMISATION : MINIMISER LE TEMPS TOTAL (avec planification) ===\n");

        Model model = new Model("EV Charging - Min Temps");
        int nbVehicules = vehicules.size();
        int nbStations = stations.size();

        // Calculer l'horizon temporel maximum
        int horizonMax = calculerHorizonMax(graphe, stations, vehicules);

        // Variables de décision : assignation[v][s] = 1 si véhicule v va à station s
        BoolVar[][] assignation = new BoolVar[nbVehicules][nbStations];
        for (int v = 0; v < nbVehicules; v++) {
            for (int s = 0; s < nbStations; s++) {
                assignation[v][s] = model.boolVar("assign_" + v + "_" + s);
            }
        }

        // Variables temporelles : début et fin de charge pour chaque véhicule
        IntVar[] debutCharge = new IntVar[nbVehicules];
        IntVar[] finCharge = new IntVar[nbVehicules];
        IntVar[] dureeCharge = new IntVar[nbVehicules];

        for (int v = 0; v < nbVehicules; v++) {
            debutCharge[v] = model.intVar("debut_" + v, 0, horizonMax);
            finCharge[v] = model.intVar("fin_" + v, 0, horizonMax);
            dureeCharge[v] = model.intVar("duree_" + v, 0, horizonMax);
        }

        // Contrainte : chaque véhicule va à exactement une station
        for (int v = 0; v < nbVehicules; v++) {
            model.sum(assignation[v], "=", 1).post();
        }

        // Calculer les durées de charge et début de charge pour chaque véhicule
        for (int v = 0; v < nbVehicules; v++) {
            Vehicule veh = vehicules.get(v);
            int chargeNecessaire = veh.chargeNecessaire();

            for (int s = 0; s < nbStations; s++) {
                Station st = stations.get(s);
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int duree = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);

                // Si assigné à cette station
                model.ifThen(
                        model.arithm(assignation[v][s], "=", 1),
                        model.and(
                                model.arithm(debutCharge[v], ">=", trajet),
                                model.arithm(dureeCharge[v], "=", duree)
                        )
                );
            }

            // fin = debut + duree
            model.arithm(finCharge[v], "=", debutCharge[v], "+", dureeCharge[v]).post();
        }

        // Contrainte de non-chevauchement : à chaque station, max nbBornes véhicules en charge simultanément
        for (int s = 0; s < nbStations; s++) {
            Station st = stations.get(s);

            // Créer des tâches pour les véhicules assignés à cette station
            List<Task> taches = new ArrayList<>();
            List<IntVar> hauteursTaches = new ArrayList<>();

            for (int v = 0; v < nbVehicules; v++) {
                // Utiliser une tâche conditionnelle
                IntVar debut = model.intVar("debut_cond_" + v + "_" + s, 0, horizonMax);
                IntVar duree = model.intVar("duree_cond_" + v + "_" + s, 0, horizonMax);
                IntVar fin = model.intVar("fin_cond_" + v + "_" + s, 0, horizonMax);
                IntVar hauteur = model.intVar("hauteur_" + v + "_" + s, 0, 1);

                // Si assigné à cette station, hauteur = 1, sinon hauteur = 0
                model.ifOnlyIf(
                        model.arithm(assignation[v][s], "=", 1),
                        model.and(
                                model.arithm(debut, "=", debutCharge[v]),
                                model.arithm(duree, "=", dureeCharge[v]),
                                model.arithm(fin, "=", finCharge[v]),
                                model.arithm(hauteur, "=", 1)
                        )
                );

                model.ifOnlyIf(
                        model.arithm(assignation[v][s], "=", 0),
                        model.and(
                                model.arithm(hauteur, "=", 0),
                                model.arithm(duree, "=", 0)
                        )
                );

                // Créer une Task pour Choco Solver 5
                Task task = new Task(debut, duree, fin);
                taches.add(task);
                hauteursTaches.add(hauteur);
            }

            // Contrainte cumulative : capacité de la station
            if (!taches.isEmpty()) {
                model.cumulative(
                        taches.toArray(new Task[0]),
                        hauteursTaches.toArray(new IntVar[0]),
                        model.intVar(st.nbBornes)
                ).post();
            }
        }

        // Objectif : minimiser le makespan (fin max)
        IntVar makespan = model.intVar("makespan", 0, horizonMax);
        model.max(makespan, finCharge).post();
        model.setObjective(Model.MINIMIZE, makespan);

        // Résolution
        Solver solver = model.getSolver();
        solver.setSearch(Search.domOverWDegSearch(model.retrieveIntVars(true)));
        //utiliser une recherche par voisinage pour améliorer la solution
//        model.getSolver().setLNS(
//                new org.chocosolver.solver.search.loop.lns.neighbors.PropagationGuidedNeighborhood(
//                        dureeCharge, dureeCharge.length/3, dureeCharge.length, 0
//                )
//        );

        Solution solution = new Solution(model);
        while (solver.solve()) {
            solution.record();
        }

        if (solution.exists()) {
            afficherSolutionTemporelle(solution, assignation, debutCharge, finCharge,
                    vehicules, stations, graphe, "TEMPS");
            System.out.printf("\nMakespan optimal : %d min\n", solution.getIntVal(makespan));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    // Critère 2 : Minimiser le coût (distance totale parcourue) avec planification
    public static void resoudreMinimiserCout(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        System.out.println("=== OPTIMISATION : MINIMISER LE COÛT (DISTANCE) avec planification ===\n");

        Model model = new Model("EV Charging - Min Coût");
        int nbVehicules = vehicules.size();
        int nbStations = stations.size();
        int horizonMax = calculerHorizonMax(graphe, stations, vehicules);

        BoolVar[][] assignation = new BoolVar[nbVehicules][nbStations];
        for (int v = 0; v < nbVehicules; v++) {
            for (int s = 0; s < nbStations; s++) {
                assignation[v][s] = model.boolVar("assign_" + v + "_" + s);
            }
        }

        IntVar[] debutCharge = new IntVar[nbVehicules];
        IntVar[] finCharge = new IntVar[nbVehicules];
        IntVar[] dureeCharge = new IntVar[nbVehicules];

        for (int v = 0; v < nbVehicules; v++) {
            debutCharge[v] = model.intVar("debut_" + v, 0, horizonMax);
            finCharge[v] = model.intVar("fin_" + v, 0, horizonMax);
            dureeCharge[v] = model.intVar("duree_" + v, 0, horizonMax);
        }

        for (int v = 0; v < nbVehicules; v++) {
            model.sum(assignation[v], "=", 1).post();
        }

        // Calculer les durées et distances
        IntVar[] distanceVehicule = new IntVar[nbVehicules];
        for (int v = 0; v < nbVehicules; v++) {
            Vehicule veh = vehicules.get(v);
            int chargeNecessaire = veh.chargeNecessaire();
            int maxDist = 0;

            for (Station st : stations) {
                maxDist = Math.max(maxDist, graphe.distances[veh.noeud][st.noeud]);
            }

            distanceVehicule[v] = model.intVar("dist_" + v, 0, maxDist);

            for (int s = 0; s < nbStations; s++) {
                Station st = stations.get(s);
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int duree = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);

                model.ifThen(
                        model.arithm(assignation[v][s], "=", 1),
                        model.and(
                                model.arithm(distanceVehicule[v], "=", trajet),
                                model.arithm(debutCharge[v], ">=", trajet),
                                model.arithm(dureeCharge[v], "=", duree)
                        )
                );
            }

            model.arithm(finCharge[v], "=", debutCharge[v], "+", dureeCharge[v]).post();
        }

        // Contraintes cumulatives pour chaque station
        for (int s = 0; s < nbStations; s++) {
            Station st = stations.get(s);
            ajouterContrainteCumulative(model, assignation, debutCharge, dureeCharge,
                    finCharge, nbVehicules, s, st.nbBornes, horizonMax);
        }

        // Objectif : minimiser la distance totale
        IntVar distanceTotale = model.intVar("distance_totale", 0, 100000);
        model.sum(distanceVehicule, "=", distanceTotale).post();
        model.setObjective(Model.MINIMIZE, distanceTotale);

        Solver solver = model.getSolver();
        Solution solution = new Solution(model);
        while (solver.solve()) {
            solution.record();
        }

        if (solution.exists()) {
            afficherSolutionTemporelle(solution, assignation, debutCharge, finCharge,
                    vehicules, stations, graphe, "COÛT");
            System.out.printf("\nDistance totale optimale : %d km\n", solution.getIntVal(distanceTotale));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    // Critère 3 : Répartition équitable
    public static void resoudreRepartitionEquitable(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        System.out.println("=== OPTIMISATION : RÉPARTITION ÉQUITABLE avec planification ===\n");

        Model model = new Model("EV Charging - Équitable");
        int nbVehicules = vehicules.size();
        int nbStations = stations.size();
        int horizonMax = calculerHorizonMax(graphe, stations, vehicules);

        BoolVar[][] assignation = new BoolVar[nbVehicules][nbStations];
        for (int v = 0; v < nbVehicules; v++) {
            for (int s = 0; s < nbStations; s++) {
                assignation[v][s] = model.boolVar("assign_" + v + "_" + s);
            }
        }

        IntVar[] debutCharge = new IntVar[nbVehicules];
        IntVar[] finCharge = new IntVar[nbVehicules];
        IntVar[] dureeCharge = new IntVar[nbVehicules];

        for (int v = 0; v < nbVehicules; v++) {
            debutCharge[v] = model.intVar("debut_" + v, 0, horizonMax);
            finCharge[v] = model.intVar("fin_" + v, 0, horizonMax);
            dureeCharge[v] = model.intVar("duree_" + v, 0, horizonMax);
        }

        for (int v = 0; v < nbVehicules; v++) {
            model.sum(assignation[v], "=", 1).post();
        }

        for (int v = 0; v < nbVehicules; v++) {
            Vehicule veh = vehicules.get(v);
            int chargeNecessaire = veh.chargeNecessaire();

            for (int s = 0; s < nbStations; s++) {
                Station st = stations.get(s);
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int duree = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);

                model.ifThen(
                        model.arithm(assignation[v][s], "=", 1),
                        model.and(
                                model.arithm(debutCharge[v], ">=", trajet),
                                model.arithm(dureeCharge[v], "=", duree)
                        )
                );
            }

            model.arithm(finCharge[v], "=", debutCharge[v], "+", dureeCharge[v]).post();
        }

        // Contraintes cumulatives
        for (int s = 0; s < nbStations; s++) {
            Station st = stations.get(s);
            ajouterContrainteCumulative(model, assignation, debutCharge, dureeCharge,
                    finCharge, nbVehicules, s, st.nbBornes, horizonMax);
        }

        // Minimiser l'écart entre le temps de fin max et min
        IntVar finMin = model.intVar("fin_min", 0, horizonMax);
        IntVar finMax = model.intVar("fin_max", 0, horizonMax);
        IntVar ecart = model.intVar("ecart", 0, horizonMax);

        model.min(finMin, finCharge).post();
        model.max(finMax, finCharge).post();
        model.arithm(ecart, "=", finMax, "-", finMin).post();

        model.setObjective(Model.MINIMIZE, ecart);

        Solver solver = model.getSolver();
        Solution solution = new Solution(model);
        while (solver.solve()) {
            solution.record();
        }

        if (solution.exists()) {
            afficherSolutionTemporelle(solution, assignation, debutCharge, finCharge,
                    vehicules, stations, graphe, "ÉQUITABLE");
            System.out.printf("\nÉcart optimal : %d min\n", solution.getIntVal(ecart));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    // Méthode utilitaire pour ajouter les contraintes cumulatives
    private static void ajouterContrainteCumulative(Model model, BoolVar[][] assignation,
                                                    IntVar[] debutCharge, IntVar[] dureeCharge,
                                                    IntVar[] finCharge, int nbVehicules,
                                                    int stationId, int capacite, int horizonMax) {
        List<Task> taches = new ArrayList<>();
        List<IntVar> hauteursTaches = new ArrayList<>();

        for (int v = 0; v < nbVehicules; v++) {
            IntVar debut = model.intVar("debut_cond_" + v + "_" + stationId, 0, horizonMax);
            IntVar duree = model.intVar("duree_cond_" + v + "_" + stationId, 0, horizonMax);
            IntVar fin = model.intVar("fin_cond_" + v + "_" + stationId, 0, horizonMax);
            IntVar hauteur = model.intVar("hauteur_" + v + "_" + stationId, 0, 1);

            model.ifOnlyIf(
                    model.arithm(assignation[v][stationId], "=", 1),
                    model.and(
                            model.arithm(debut, "=", debutCharge[v]),
                            model.arithm(duree, "=", dureeCharge[v]),
                            model.arithm(fin, "=", finCharge[v]),
                            model.arithm(hauteur, "=", 1)
                    )
            );

            model.ifOnlyIf(
                    model.arithm(assignation[v][stationId], "=", 0),
                    model.and(
                            model.arithm(hauteur, "=", 0),
                            model.arithm(duree, "=", 0)
                    )
            );

            // Créer une Task pour Choco Solver 5
            Task task = new Task(debut, duree, fin);
            taches.add(task);
            hauteursTaches.add(hauteur);
        }

        if (!taches.isEmpty()) {
            model.cumulative(
                    taches.toArray(new Task[0]),
                    hauteursTaches.toArray(new IntVar[0]),
                    model.intVar(capacite)
            ).post();
        }
    }

    // Calculer l'horizon temporel maximum
    private static int calculerHorizonMax(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        int horizonMax = 0;
        for (Vehicule veh : vehicules) {
            for (Station st : stations) {
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int charge = (int) Math.ceil((double) veh.chargeNecessaire() / st.puissance * 60);
                horizonMax = Math.max(horizonMax, trajet + charge);
            }
        }
        // Multiplier par le nombre de véhicules pour permettre la planification séquentielle
        return horizonMax * vehicules.size();
    }

    // Méthode d'affichage des solutions avec informations temporelles
    private static void afficherSolutionTemporelle(Solution solution, BoolVar[][] assignation,
                                                   IntVar[] debutCharge, IntVar[] finCharge,
                                                   List<Vehicule> vehicules, List<Station> stations,
                                                   Graphe graphe, String critere) {
        System.out.println("Solution trouvée pour critère : " + critere);
        System.out.println("-".repeat(70));

        int distanceTotale = 0;
        int finMax = 0;
        int finMin = Integer.MAX_VALUE;
        double finMoyen = 0;

        // Trier par ordre de fin de charge pour affichage
        List<InfoVehicule> infos = new ArrayList<>();

        for (int v = 0; v < vehicules.size(); v++) {
            Vehicule veh = vehicules.get(v);
            for (int s = 0; s < stations.size(); s++) {
                if (solution.getIntVal(assignation[v][s]) == 1) {
                    Station st = stations.get(s);
                    int distance = graphe.distances[veh.noeud][st.noeud];
                    int debut = solution.getIntVal(debutCharge[v]);
                    int fin = solution.getIntVal(finCharge[v]);
                    int duree = fin - debut;

                    infos.add(new InfoVehicule(veh, st, distance, debut, fin, duree));

                    distanceTotale += distance;
                    finMax = Math.max(finMax, fin);
                    finMin = Math.min(finMin, fin);
                    finMoyen += fin;
                }
            }
        }

        // Trier par temps de fin
        infos.sort(Comparator.comparingInt(i -> i.fin));

        for (InfoVehicule info : infos) {
            System.out.printf("Véhicule %d (noeud %d, charge: %d/%d kWh)\n",
                    info.vehicule.id, info.vehicule.noeud,
                    info.vehicule.chargeInitiale, info.vehicule.capaciteMax);
            System.out.printf("  → Station %d (noeud %d, %d bornes, %d kW)\n",
                    info.station.id, info.station.noeud,
                    info.station.nbBornes, info.station.puissance);
            System.out.printf("  → Distance: %d km, Trajet: %d min\n",
                    info.distance, info.distance);
            System.out.printf("  → Début charge: %d min, Durée: %d min, Fin: %d min\n\n",
                    info.debut, info.duree, info.fin);
        }

        finMoyen /= vehicules.size();

        System.out.println("-".repeat(70));
        System.out.println("STATISTIQUES :");
        System.out.printf("  Distance totale parcourue : %d km\n", distanceTotale);
        System.out.printf("  Temps de fin maximum : %d min\n", finMax);
        System.out.printf("  Temps de fin minimum : %d min\n", finMin);
        System.out.printf("  Temps de fin moyen : %.1f min\n", finMoyen);
        System.out.printf("  Écart (max-min) : %d min\n", finMax - finMin);
    }


    // Méthodes de création d'exemples
    private static Graphe creerGraphe(int nbPoints) {
        Graphe g = new Graphe(nbPoints);

        // Ajout des arcs avec distances pour créer aléatoirement un graphe de 100 noeuds
        Random rand = new Random(42);
        for (int i = 0; i < nbPoints; i++) {
            int v = rand.nextInt(nbPoints);
            if (i != v) {
                int distance = 1 + rand.nextInt(5); // distances entre 1 et 6 km
                g.ajouterArc(i, v, distance);
            }
        }


        g.calculerPlusCourtsChemin();
        return g;
    }

    // Méthodes de création d'exemples
    private static Graphe creerGrapheExemple() {
        Graphe g = new Graphe(8);

        // Ajout des arcs avec distances
        g.ajouterArc(0, 1, 5); g.ajouterArc(0, 2, 8); g.ajouterArc(1, 3, 7);
        g.ajouterArc(1, 4, 4); g.ajouterArc(2, 4, 6); g.ajouterArc(2, 5, 9);
        g.ajouterArc(3, 6, 5); g.ajouterArc(4, 6, 3); g.ajouterArc(4, 7, 8);
        g.ajouterArc(5, 7, 4); g.ajouterArc(6, 7, 6);

        g.calculerPlusCourtsChemin();
        return g;
    }


    private static List<Station> creerStationsExemple() {
        List<Station> stations = new ArrayList<>();
        stations.add(new Station(0, 2, 2, 50));  // 2 bornes, 50 kW
        stations.add(new Station(1, 5, 1, 150)); // 1 borne, 150 kW (charge rapide)
        return stations;
    }

    private static List<Station> creerStations(int nbStations, int nbPoints) {
        List<Station> stations = new ArrayList<>();
        for (int i = 0; i < nbStations; i++) {
            int noeud =  (int) (Math.random() * nbPoints);
            int puissance = (Math.random() < 0.5) ? 50 : 150; // 50 kW ou 150 kW
            stations.add(new Station(i, noeud, 10, puissance));
        }
        return stations;
    }

    private static List<Vehicule> creerVehiculesExemple() {
        List<Vehicule> vehicules = new ArrayList<>();
        vehicules.add(new Vehicule(0, 0, 20, 60, 60));  // 20 kWh restants, capacité 60 kWh
        vehicules.add(new Vehicule(1, 1, 10, 75, 75));  // 10 kWh restants, capacité 75 kWh
        vehicules.add(new Vehicule(2, 3, 30, 50, 50));  // 30 kWh restants, capacité 50 kWh
        vehicules.add(new Vehicule(3, 4, 15, 80, 80));  // 15 kWh restants, capacité 80 kWh
        vehicules.add(new Vehicule(4, 7, 25, 65, 65));  // 25 kWh restants, capacité 65 kWh
        return vehicules;
    }

    private static List<Vehicule> creerVehicules(int nbVehicules, int nbPoints) {
        List<Vehicule> vehicules = new ArrayList<>();
        for (int i = 0; i < nbVehicules; i++) {
            int noeud = (int) (Math.random() * nbPoints);
            int capaciteMax = 50 + (int) (Math.random() * 51); // entre 50 et 100 kWh
            int chargeInitiale = (int) (Math.random() * (capaciteMax / 2)); // entre 0 et la moitié de la capacité
            int capaciteActuelle = chargeInitiale;
            vehicules.add(new Vehicule(i, noeud, chargeInitiale, capaciteActuelle, capaciteMax));
        }
        return vehicules;
    }

    public static void main(String[] args) {
        // Exemple de données
        int nbPoints = 45;
        Graphe graphe = creerGraphe(nbPoints);
        List<Station> stations = creerStations(5, nbPoints);
        List<Vehicule> vehicules = creerVehicules(5, nbPoints);

        System.out.println("=== CONFIGURATION ===");
        System.out.println("Nombre de véhicules : " + vehicules.size());
        System.out.println("Nombre de stations : " + stations.size());
        int totalBornes = stations.stream().mapToInt(s -> s.nbBornes).sum();
        System.out.println("Nombre total de bornes : " + totalBornes);
        System.out.println();

        // Résoudre le problème avec différents critères
        resoudreMinimiserTemps(graphe, stations, vehicules);
        System.out.println("\n" + "=".repeat(80) + "\n");
        resoudreMinimiserCout(graphe, stations, vehicules);
//        System.out.println("\n" + "=".repeat(80) + "\n");
//        resoudreRepartitionEquitable(graphe, stations, vehicules);
    }

}