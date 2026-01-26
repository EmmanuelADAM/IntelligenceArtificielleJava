import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

public class RechargeVehicules {

    static void affectationRecharge1() {
        // Données d'entrée
        int[] capaciteStations = {2, 3, 1}; // Nombre de bornes par station
        int[] puissanceStations = {50, 100, 22}; // Puissance (kW) par station
        int[][] positionsStations = {{1}, {3}, {5}}; // Nœuds où se trouvent les stations

        // Véhicules : {nœud initial, charge initiale (kWh), capacité max (kWh)}
        int[][] vehicules = {
                {0, 10, 50}, // Véhicule 0: nœud 0, 10kWh, 50kWh max
                {2, 5, 60},  // Véhicule 1: nœud 2, 5kWh, 60kWh max
                {4, 20, 40}, // Véhicule 2: nœud 4, 20kWh, 40kWh max
                {1, 15, 55}, // Véhicule 3: nœud 1, 15kWh, 55kWh max
                {3, 8, 45}   // Véhicule 4: nœud 3, 8kWh, 45kWh max
        };

        // Matrice des distances entre nœuds (exemple)
        int[][] distances = {
                {0, 10, 20, 15, 30, 25}, // Nœud 0
                {10, 0, 12, 8, 22, 18},  // Nœud 1
                {20, 12, 0, 17, 10, 35}, // Nœud 2
                {15, 8, 17, 0, 19, 12},  // Nœud 3
                {30, 22, 10, 19, 0, 5},  // Nœud 4
                {25, 18, 35, 12, 5, 0}   // Nœud 5
        };

        int nVehicules = vehicules.length;
        int nStations = capaciteStations.length;
        int nNoeuds = distances.length;

        // Modèle ChocoSolver
        Model model = new Model("Recharge véhicules");

        // Variables de décision : x[i][j] = 1 si véhicule i est affecté à station j
        IntVar[][] x = model.intVarMatrix("x", nVehicules, nStations, 0, 1);

        // Temps de charge pour chaque véhicule (en heures)
        IntVar[] tempsCharge = model.intVarArray("t", nVehicules, 0, 100);

        // Temps de trajet pour chaque véhicule (en heures, vitesse moyenne = 50 km/h)
        IntVar[] tempsTrajet = model.intVarArray("trajet", nVehicules, 0, 100);

        // Contraintes

        // 1. Chaque véhicule est affecté à exactement une station
        for (int i = 0; i < nVehicules; i++) {
            IntVar[] affectation = new IntVar[nStations];
            for (int j = 0; j < nStations; j++) {
                affectation[j] = x[i][j];
            }
            model.sum(affectation, "=", 1).post();
        }

        // 2. Capacité des stations
        for (int j = 0; j < nStations; j++) {
            IntVar[] vehiculesParStation = new IntVar[nVehicules];
            for (int i = 0; i < nVehicules; i++) {
                vehiculesParStation[i] = x[i][j];
            }
            model.sum(vehiculesParStation, "<=", capaciteStations[j]).post();
        }

        // 3. Temps de charge = (capacité max - charge initiale) / puissance
        for (int i = 0; i < nVehicules; i++) {
            int chargeManquante = vehicules[i][2] - vehicules[i][1];
            for (int j = 0; j < nStations; j++) {
                model.ifThen(
                        model.arithm(x[i][j], "=", 1),
                        model.arithm(tempsCharge[i], "=", chargeManquante / puissanceStations[j])
                );
            }
        }

        // 4. Temps de trajet = distance / vitesse (vitesse = 50 km/h)
        for (int i = 0; i < nVehicules; i++) {
            int noeudInitial = vehicules[i][0];
            for (int j = 0; j < nStations; j++) {
                int noeudStation = positionsStations[j][0];
                int distance = distances[noeudInitial][noeudStation];
                model.ifThen(
                        model.arithm(x[i][j], "=", 1),
                        model.arithm(tempsTrajet[i], "=", distance / 50)
                );
            }
        }

        // 5. Temps total = temps de charge + temps de trajet
        IntVar[] tempsTotal = model.intVarArray("tempsTotal", nVehicules, 0, 200);
        for (int i = 0; i < nVehicules; i++) {
            model.arithm(tempsTotal[i], "=", tempsCharge[i], "+", tempsTrajet[i]).post();
        }

        // Objectif : minimiser le temps total maximal
        IntVar maxTempsTotal = model.intVar("maxTempsTotal", 0, 200);
        model.max(maxTempsTotal, tempsTotal).post();
        model.setObjective(Model.MINIMIZE, maxTempsTotal);

        // Résolution
        Solver solver = model.getSolver();
        Solution solution = solver.findOptimalSolution(tempsTotal[0], Model.MINIMIZE);

        // Affichage de la solution
        if (solution != null) {
            System.out.println("Solution optimale trouvée :");
            for (int i = 0; i < nVehicules; i++) {
                for (int j = 0; j < nStations; j++) {
                    if (solution.getIntVal(x[i][j]) == 1) {
                        System.out.printf(
                                "Véhicule %d (nœud %d) -> Station %d (nœud %d): " +
                                        "Temps charge = %d h, Temps trajet = %d h, Temps total = %d h%n",
                                i, vehicules[i][0], j, positionsStations[j][0],
                                solution.getIntVal(tempsCharge[i]),
                                solution.getIntVal(tempsTrajet[i]),
                                solution.getIntVal(tempsTotal[i])
                        );
                    }
                }
            }
            System.out.println("Temps total maximal : " + solution.getIntVal(maxTempsTotal) + " h");
        } else {
            System.out.println("Pas de solution trouvée.");
        }
    }

    static void affectationRecharge2() {
        // Données d'entrée
        int[] capaciteStations = {2, 2}; // Nombre de bornes par station
        int[] puissanceStations = {50, 100}; // Puissance (kW) par station
        int[][] positionsStations = {{1}, {3}}; // Nœuds des stations

        // Véhicules : {nœud initial, charge initiale (kWh), capacité max (kWh)}
        int[][] vehicules = {
                {0, 10, 50}, // Véhicule 0
                {2, 5, 60},  // Véhicule 1
                {4, 20, 40}, // Véhicule 2
                {1, 15, 55}, // Véhicule 3
                {3, 8, 45}   // Véhicule 4
        };

        // Matrice des distances entre nœuds
        int[][] distances = {
                {0, 10, 20, 15, 30, 25},
                {10, 0, 12, 8, 22, 18},
                {20, 12, 0, 17, 10, 35},
                {15, 8, 17, 0, 19, 12},
                {30, 22, 10, 19, 0, 5},
                {25, 18, 35, 12, 5, 0}
        };

        int nVehicules = vehicules.length;
        int nStations = capaciteStations.length;
        int horizon = 24; // Horizon temporel (heures)

        // Modèle ChocoSolver
        Model model = new Model("Recharge véhicules");

        // Variables de décision
        IntVar[][] x = model.intVarMatrix("x", nVehicules, nStations, 0, 1); // Affectation
        IntVar[] start = model.intVarArray("start", nVehicules, 0, horizon); // Heure de début
        IntVar[] tempsCharge = model.intVarArray("t", nVehicules, 0, horizon); // Durée de charge
        IntVar[] tempsTrajet = model.intVarArray("trajet", nVehicules, 0, horizon); // Durée de trajet
        IntVar[] end = model.intVarArray("end", nVehicules, 0, horizon); // Heure de fin

        // Contraintes

        // 1. Chaque véhicule est affecté à exactement une station
        for (int i = 0; i < nVehicules; i++) {
            IntVar[] affectation = new IntVar[nStations];
            for (int j = 0; j < nStations; j++) {
                affectation[j] = x[i][j];
            }
            model.sum(affectation, "=", 1).post();
        }

        // 2. Temps de charge = (capacité max - charge initiale) / puissance
        for (int i = 0; i < nVehicules; i++) {
            int chargeManquante = vehicules[i][2] - vehicules[i][1];
            IntVar[] tempsChargeParStation = new IntVar[nStations];
            for (int j = 0; j < nStations; j++) {
                tempsChargeParStation[j] = model.intVar(0, horizon);
                model.ifThen(
                        model.arithm(x[i][j], "=", 1),
                        model.arithm(tempsChargeParStation[j], "=", chargeManquante / puissanceStations[j])
                );
                model.ifThen(
                        model.arithm(x[i][j], "=", 0),
                        model.arithm(tempsChargeParStation[j], "=", 0)
                );
            }
            model.sum(tempsChargeParStation, "=", tempsCharge[i]).post();
        }

        // 3. Temps de trajet = distance / vitesse (vitesse = 50 km/h)
        for (int i = 0; i < nVehicules; i++) {
            int noeudInitial = vehicules[i][0];
            IntVar[] tempsTrajetParStation = new IntVar[nStations];
            for (int j = 0; j < nStations; j++) {
                tempsTrajetParStation[j] = model.intVar(0, horizon);
                int noeudStation = positionsStations[j][0];
                int distance = distances[noeudInitial][noeudStation];
                model.ifThen(
                        model.arithm(x[i][j], "=", 1),
                        model.arithm(tempsTrajetParStation[j], "=", distance / 50)
                );
                model.ifThen(
                        model.arithm(x[i][j], "=", 0),
                        model.arithm(tempsTrajetParStation[j], "=", 0)
                );
            }
            model.sum(tempsTrajetParStation, "=", tempsTrajet[i]).post();
        }

        // 4. Heure de fin = heure de début + temps de charge
        for (int i = 0; i < nVehicules; i++) {
            model.arithm(end[i], "=", start[i], "+", tempsCharge[i]).post();
        }

        // 5. Heure de début >= temps de trajet (le véhicule doit arriver avant de commencer)
        for (int i = 0; i < nVehicules; i++) {
            model.arithm(start[i], ">=", tempsTrajet[i]).post();
        }

        // 6. Contraintes de capacité des stations (pas de chevauchement)
        for (int j = 0; j < nStations; j++) {
            Task[] tasks = new Task[nVehicules];
            IntVar[] demands = new IntVar[nVehicules];
            for (int i = 0; i < nVehicules; i++) {
                // Durée de charge pour cette station
                IntVar duree = model.intVar(0, horizon);
                model.ifThen(
                        model.arithm(x[i][j], "=", 1),
                        model.arithm(duree, "=", tempsCharge[i])
                );
                model.ifThen(
                        model.arithm(x[i][j], "=", 0),
                        model.arithm(duree, "=", 0)
                );
                tasks[i] = new Task(start[i], duree, end[i]);
                demands[i] = model.intVar(0, 1);
                model.ifThen(
                        model.arithm(x[i][j], "=", 1),
                        model.arithm(demands[i], "=", 1)
                );
                model.ifThen(
                        model.arithm(x[i][j], "=", 0),
                        model.arithm(demands[i], "=", 0)
                );
            }
            // Contrainte cumulative : pas plus de 'capaciteStations[j]' véhicules en même temps
            model.cumulative(tasks, demands, model.intVar(capaciteStations[j])).post();
        }

        // Objectif : minimiser l'heure de fin maximale (makespan)
        IntVar makespan = model.intVar("makespan", 0, horizon);
        model.max(makespan, end).post();
        model.setObjective(Model.MINIMIZE, makespan);

        // Résolution
        Solver solver = model.getSolver();
        Solution solution = solver.findOptimalSolution(makespan, Model.MINIMIZE);

        // Affichage de la solution
        if (solution != null) {
            System.out.println("Solution optimale (makespan = " + solution.getIntVal(makespan) + " h) :");
            for (int i = 0; i < nVehicules; i++) {
                for (int j = 0; j < nStations; j++) {
                    if (solution.getIntVal(x[i][j]) == 1) {
                        System.out.printf(
                                "Véhicule %d (nœud %d) -> Station %d (nœud %d): " +
                                        "Début = %d h, Fin = %d h, Durée charge = %d h, Trajet = %d h%n",
                                i, vehicules[i][0], j, positionsStations[j][0],
                                solution.getIntVal(start[i]),
                                solution.getIntVal(end[i]),
                                solution.getIntVal(tempsCharge[i]),
                                solution.getIntVal(tempsTrajet[i])
                        );
                    }
                }
            }
        } else {
            System.out.println("Pas de solution trouvée.");
        }
    }


    public static void main(String[] args) {
        affectationRecharge2();
    }
}
