import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.constraints.Constraint;

import java.util.*;

public class OptimisationCharge {

    record Station(int id, int noeud, int nbBornes, int puissance) {}

    record Vehicule(int id, int noeud, int chargeInitiale, int capaciteActuelle, int capaciteMax) {
        // Méthode supplémentaire pour calculer la charge nécessaire
        public int chargeNecessaire() {
            return capaciteMax - chargeInitiale;
        }
    }

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

        /** ajouter relation  entre u et v dans la matrice d'adjacence
         *
         * @param u point départ
         * @param v point arrivée
         * @param distance entre u et v (et v et u)
         */
        public void ajouterArc(int u, int v, int distance) {
            distances[u][v] = distance;
            distances[v][u] = distance; // graphe non orienté
        }

        /**
         * calcul de la plus petite distance entre 2 points du graphe par
         * algorithme de Floyd-Warshall (cf. vos cours d'algo, de graphe...)
         * algo long (en O(n^3)), mais acceptable pour petits graphes
         */
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

    public static void main(String[] args) {
        // Exemple de données
        Graphe graphe = creerGrapheExemple();
        List<Station> stations = creerStationsExemple();
        List<Vehicule> vehicules = creerVehiculesExemple();

        // Résoudre le problème avec différents critères
        resoudreMinimiserTemps(graphe, stations, vehicules);
        System.out.println("\n" + "=".repeat(80) + "\n");
        resoudreMinimiserCout(graphe, stations, vehicules);
        System.out.println("\n" + "=".repeat(80) + "\n");
        resoudreRepartitionEquitable(graphe, stations, vehicules);
    }

    // Critère 1 : Minimiser le temps total
    public static void resoudreMinimiserTemps(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        System.out.println("=== OPTIMISATION : MINIMISER LE TEMPS TOTAL ===\n");

        Model model = new Model("EV Charging - Min Temps");
        int nbVehicules = vehicules.size();
        int nbStations = stations.size();

        // Variables de décision : assignation[v][s] = 1 si véhicule v va à station s
        BoolVar[][] assignation = new BoolVar[nbVehicules][nbStations];
        for (int v = 0; v < nbVehicules; v++) {
            for (int s = 0; s < nbStations; s++) {
                assignation[v][s] = model.boolVar("assign_" + v + "_" + s);
            }
        }

        // Contrainte : chaque véhicule va à exactement une station
        for (int v = 0; v < nbVehicules; v++) {
            model.sum(assignation[v], "=", 1).post();
        }

        // Contrainte : capacité des stations (nombre de bornes)
        for (int s = 0; s < nbStations; s++) {
            IntVar[] assignationsStation = new IntVar[nbVehicules];
            for (int v = 0; v < nbVehicules; v++) {
                assignationsStation[v] = assignation[v][s];
            }
            model.sum(assignationsStation, "<=", stations.get(s).nbBornes).post();
        }

        // Calcul du temps pour chaque véhicule
        IntVar[] tempsVehicule = new IntVar[nbVehicules];
        for (int v = 0; v < nbVehicules; v++) {
            Vehicule veh = vehicules.get(v);
            int chargeNecessaire = veh.chargeNecessaire();

            // Temps max possible (trajet + charge la plus lente)
            int tempsMax = 0;
            for (Station st : stations) {
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int charge = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60); // en minutes
                tempsMax = Math.max(tempsMax, trajet + charge);
            }

            tempsVehicule[v] = model.intVar("temps_" + v, 0, tempsMax);

            // Lier le temps à l'assignation
            for (int s = 0; s < nbStations; s++) {
                Station st = stations.get(s);
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int charge = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);
                int tempsTotal = trajet + charge;

                // Si assigné à cette station, alors temps = tempsTotal
                model.ifThen(
                        model.arithm(assignation[v][s], "=", 1),
                        model.arithm(tempsVehicule[v], "=", tempsTotal)
                );
            }
        }

        // Objectif : minimiser le temps maximum (makespan)
        IntVar tempsSolution = model.intVar("temps_solution", 0, 10000);
        model.max(tempsSolution, tempsVehicule).post();
        model.setObjective(Model.MINIMIZE, tempsSolution);

        // Résolution
        Solver solver = model.getSolver();
        solver.setSearch(Search.domOverWDegSearch(model.retrieveIntVars(true)));

        Solution solution = new Solution(model);
        while (solver.solve()) {
            solution.record();
        }

        if (solution.exists()) {
            afficherSolution(solution, assignation, vehicules, stations, graphe, "TEMPS");
            System.out.printf("Temps optimal : %d min\n", solution.getIntVal(tempsSolution));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    // Critère 2 : Minimiser le coût (distance totale parcourue)
    public static void resoudreMinimiserCout(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        System.out.println("=== OPTIMISATION : MINIMISER LE COÛT (DISTANCE) ===\n");

        Model model = new Model("EV Charging - Min Coût");
        int nbVehicules = vehicules.size();
        int nbStations = stations.size();

        BoolVar[][] assignation = new BoolVar[nbVehicules][nbStations];
        for (int v = 0; v < nbVehicules; v++) {
            for (int s = 0; s < nbStations; s++) {
                assignation[v][s] = model.boolVar("assign_" + v + "_" + s);
            }
        }

        for (int v = 0; v < nbVehicules; v++) {
            model.sum(assignation[v], "=", 1).post();
        }

        for (int s = 0; s < nbStations; s++) {
            IntVar[] assignationsStation = new IntVar[nbVehicules];
            for (int v = 0; v < nbVehicules; v++) {
                assignationsStation[v] = assignation[v][s];
            }
            model.sum(assignationsStation, "<=", stations.get(s).nbBornes).post();
        }

        // Calcul de la distance pour chaque véhicule
        IntVar[] distanceVehicule = new IntVar[nbVehicules];
        for (int v = 0; v < nbVehicules; v++) {
            Vehicule veh = vehicules.get(v);
            int maxDist = 0;
            for (Station st : stations) {
                maxDist = Math.max(maxDist, graphe.distances[veh.noeud][st.noeud]);
            }

            distanceVehicule[v] = model.intVar("dist_" + v, 0, maxDist);

            for (int s = 0; s < nbStations; s++) {
                Station st = stations.get(s);
                int dist = graphe.distances[veh.noeud][st.noeud];

                model.ifThen(
                        model.arithm(assignation[v][s], "=", 1),
                        model.arithm(distanceVehicule[v], "=", dist)
                );
            }
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
            afficherSolution(solution, assignation, vehicules, stations, graphe, "COÛT");
            System.out.printf("Distance totale optimale : %d km\n", solution.getIntVal(distanceTotale));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    // Critère 3 : Répartition équitable (minimiser l'écart-type des temps)
    public static void resoudreRepartitionEquitable(Graphe graphe, List<Station> stations, List<Vehicule> vehicules) {
        System.out.println("=== OPTIMISATION : RÉPARTITION ÉQUITABLE ===\n");

        Model model = new Model("EV Charging - Équitable");
        int nbVehicules = vehicules.size();
        int nbStations = stations.size();

        BoolVar[][] assignation = new BoolVar[nbVehicules][nbStations];
        for (int v = 0; v < nbVehicules; v++) {
            for (int s = 0; s < nbStations; s++) {
                assignation[v][s] = model.boolVar("assign_" + v + "_" + s);
            }
        }

        for (int v = 0; v < nbVehicules; v++) {
            model.sum(assignation[v], "=", 1).post();
        }

        for (int s = 0; s < nbStations; s++) {
            IntVar[] assignationsStation = new IntVar[nbVehicules];
            for (int v = 0; v < nbVehicules; v++) {
                assignationsStation[v] = assignation[v][s];
            }
            model.sum(assignationsStation, "<=", stations.get(s).nbBornes).post();
        }

        IntVar[] tempsVehicule = new IntVar[nbVehicules];
        for (int v = 0; v < nbVehicules; v++) {
            Vehicule veh = vehicules.get(v);
            int chargeNecessaire = veh.chargeNecessaire();

            int tempsMax = 0;
            for (Station st : stations) {
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int charge = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);
                tempsMax = Math.max(tempsMax, trajet + charge);
            }

            tempsVehicule[v] = model.intVar("temps_" + v, 0, tempsMax);

            for (int s = 0; s < nbStations; s++) {
                Station st = stations.get(s);
                int trajet = graphe.distances[veh.noeud][st.noeud];
                int charge = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);
                int tempsTotal = trajet + charge;

                model.ifThen(
                        model.arithm(assignation[v][s], "=", 1),
                        model.arithm(tempsVehicule[v], "=", tempsTotal)
                );
            }
        }

        // Minimiser l'écart entre le temps max et le temps min
        IntVar tempsMin = model.intVar("temps_min", 0, 10000);
        IntVar tempsMax = model.intVar("temps_max", 0, 10000);
        IntVar ecart = model.intVar("ecart", 0, 10000);

        model.min(tempsMin, tempsVehicule).post();
        model.max(tempsMax, tempsVehicule).post();
        model.arithm(ecart, "=", tempsMax, "-", tempsMin).post();

        model.setObjective(Model.MINIMIZE, ecart);

        Solver solver = model.getSolver();

        Solution solution = new Solution(model);
        while (solver.solve()) {
            solution.record();
        }

        if (solution.exists()) {
            afficherSolution(solution, assignation, vehicules, stations, graphe, "ÉQUITABLE");
            System.out.printf("Écart optimal : %d min\n", solution.getIntVal(ecart));
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }

    // Méthode d'affichage des solutions
    private static void afficherSolution(Solution solution, BoolVar[][] assignation,
                                         List<Vehicule> vehicules, List<Station> stations,
                                         Graphe graphe, String critere) {
        System.out.println("Solution trouvée pour critère : " + critere);
        System.out.println("-".repeat(60));

        int distanceTotale = 0;
        int tempsMax = 0;
        int[] tempsVehicules = new int[vehicules.size()];

        for (int v = 0; v < vehicules.size(); v++) {
            Vehicule veh = vehicules.get(v);
            for (int s = 0; s < stations.size(); s++) {
                if (solution.getIntVal(assignation[v][s]) == 1) {
                    Station st = stations.get(s);
                    int distance = graphe.distances[veh.noeud][st.noeud];
                    int chargeNecessaire = veh.chargeNecessaire();
                    int tempsCharge = (int) Math.ceil((double) chargeNecessaire / st.puissance * 60);
                    int tempsTotal = distance + tempsCharge;

                    System.out.printf("Véhicule %d (noeud %d, charge: %d/%d kWh)\n",
                            veh.id, veh.noeud, veh.chargeInitiale, veh.capaciteMax);
                    System.out.printf("  → Station %d (noeud %d, %d bornes, %d kW)\n",
                            st.id, st.noeud, st.nbBornes, st.puissance);
                    System.out.printf("  → Distance: %d km, Temps charge: %d min, Temps total: %d min\n\n",
                            distance, tempsCharge, tempsTotal);

                    distanceTotale += distance;
                    tempsVehicules[v] = tempsTotal;
                    tempsMax = Math.max(tempsMax, tempsTotal);
                }
            }
        }

        // Statistiques
        int tempsMin = Integer.MAX_VALUE;
        double tempsMoyen = 0;
        for (int temps : tempsVehicules) {
            tempsMin = Math.min(tempsMin, temps);
            tempsMoyen += temps;
        }
        tempsMoyen /= tempsVehicules.length;

        System.out.println("-".repeat(60));
        System.out.println("STATISTIQUES :");
        System.out.printf("  Distance totale parcourue : %d km\n", distanceTotale);
        System.out.printf("  Temps maximum : %d min\n", tempsMax);
        System.out.printf("  Temps minimum : %d min\n", tempsMin);
        System.out.printf("  Temps moyen : %.1f min\n", tempsMoyen);
        System.out.printf("  Écart (max-min) : %d min\n", tempsMax - tempsMin);
    }

    // Méthodes de création d'exemples
    private static Graphe creerGrapheExemple() {
        Graphe g = new Graphe(8);

        // Ajout des arcs avec distances
        g.ajouterArc(0, 1, 5);
        g.ajouterArc(0, 2, 8);
        g.ajouterArc(1, 3, 7);
        g.ajouterArc(1, 4, 4);
        g.ajouterArc(2, 4, 6);
        g.ajouterArc(2, 5, 9);
        g.ajouterArc(3, 6, 5);
        g.ajouterArc(4, 6, 3);
        g.ajouterArc(4, 7, 8);
        g.ajouterArc(5, 7, 4);
        g.ajouterArc(6, 7, 6);

        g.calculerPlusCourtsChemin();
        return g;
    }

    private static List<Station> creerStationsExemple() {
        List<Station> stations = new ArrayList<>();
        stations.add(new Station(0, 2, 1, 50));  // 3 bornes, 50 kW
        stations.add(new Station(1, 5, 2, 150)); // 2 bornes, 150 kW (charge rapide)
        stations.add(new Station(2, 6, 1, 22));  // 4 bornes, 22 kW (charge lente)
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
}