import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;

/**
 * Problème d'attribution minimale de rôles (RBAC - Role-Based Access Control)
 *
 * Données :
 * - 4 utilisateurs : u0, u1, u2, u3
 * - 3 rôles : r0, r1, r2
 * - 5 permissions : p0, p1, p2, p3, p4
 *
 * Objectif : Attribuer des rôles aux utilisateurs pour satisfaire leurs besoins
 *            en permissions tout en minimisant le nombre total de rôles attribués.
 */
public class RBAC {

    public static void solve() {
        Model model = new Model("RBAC Minimization");

        // Dimensions du problème
        int nbUsers = 4;
        int nbRoles = 3;
        int nbPerms = 5;

        // ==================== DONNÉES DU PROBLÈME ====================

        // Matrice : rolePermissions[role][perm] = 1 si le rôle donne la permission
        int[][] rolePermissions = {
                {1, 1, 0, 0, 0},  // r0 → {p0, p1}
                {0, 1, 1, 1, 0},  // r1 → {p1, p2, p3}
                {0, 0, 1, 1, 1}   // r2 → {p2, p3, p4}
        };

        // Matrice : userNeeds[user][perm] = 1 si l'utilisateur a besoin de cette permission
        int[][] userNeeds = {
                {1, 1, 0, 0, 0},  // u0 → {p0, p1}
                {0, 1, 0, 1, 0},  // u1 → {p1, p3}
                {0, 0, 0, 1, 1},  // u2 → {p3, p4}
                {1, 0, 1, 0, 0}   // u3 → {p0, p2}
        };


        // ==================== VARIABLES DE DÉCISION ====================

        // hasRole[user][role] = 1 si l'utilisateur a ce rôle
        BoolVar[][] hasRole = new BoolVar[nbUsers][nbRoles];
        for (int u = 0; u < nbUsers; u++) {
            for (int r = 0; r < nbRoles; r++) {
                hasRole[u][r] = model.boolVar("user_" + u + "_role_" + r);
            }
        }


        // ==================== CONTRAINTES ====================

        // Pour chaque utilisateur, pour chaque permission dont il a besoin,
        // il doit avoir au moins un rôle qui la lui fournit
        for (int u = 0; u < nbUsers; u++) {
            for (int p = 0; p < nbPerms; p++) {
                if (userNeeds[u][p] == 1) {
                    // Si l'utilisateur 'u' a besoin de la permission 'p'
                    // Il doit avoir au moins un rôle qui donne 'p'

                    // Créer une contrainte : au moins un des rôles qui donnent 'p' doit être attribué
                    // On utilise une somme pondérée : sum(hasRole[u][r] * rolePermissions[r][p]) >= 1
                    IntVar sumRolesForPerm = model.intVar("u" + u + "_p" + p, 0, nbRoles);

                    int[] coefficients = new int[nbRoles];
                    for (int r = 0; r < nbRoles; r++) {
                        coefficients[r] = rolePermissions[r][p];
                    }

                    model.scalar(hasRole[u], coefficients, "=", sumRolesForPerm).post();
                    model.arithm(sumRolesForPerm, ">=", 1).post();
                }
            }
        }


        // ==================== OBJECTIF : MINIMISER LE NOMBRE DE RÔLES ====================

        // Compter le nombre total de rôles attribués
        IntVar totalRoles = model.intVar("totalRoles", 0, nbUsers * nbRoles);
        BoolVar[] allRoles = new BoolVar[nbUsers * nbRoles];
        int idx = 0;
        for (int u = 0; u < nbUsers; u++) {
            for (int r = 0; r < nbRoles; r++) {
                allRoles[idx++] = hasRole[u][r];
            }
            // Si l'utilisateur a r0, alors il ne peut pas avoir r2
            // hasRole[u][0] + hasRole[u][2] <= 1
            BoolVar[] incompatibleRoles = {hasRole[u][0], hasRole[u][2]};
            model.sum(incompatibleRoles, "<=", 1).post();
        }
        model.sum(allRoles, "=", totalRoles).post();

        // Minimiser
        model.setObjective(Model.MINIMIZE, totalRoles);


        // ==================== RÉSOLUTION ====================

        Solver solver = model.getSolver();
        solver.setSearch(Search.minDomLBSearch(allRoles));

        System.out.println("========================================");
        System.out.println("PROBLÈME D'ATTRIBUTION DE RÔLES RBAC");
        System.out.println("========================================\n");

        // Afficher les données
        System.out.println("--- Configuration des rôles ---");
        String[] roleNames = {"r0", "r1", "r2"};
        String[] permNames = {"p0", "p1", "p2", "p3", "p4"};
        for (int r = 0; r < nbRoles; r++) {
            System.out.print(roleNames[r] + " → {");
            boolean first = true;
            for (int p = 0; p < nbPerms; p++) {
                if (rolePermissions[r][p] == 1) {
                    if (!first) System.out.print(", ");
                    System.out.print(permNames[p]);
                    first = false;
                }
            }
            System.out.println("}");
        }

        System.out.println("\n--- Besoins des utilisateurs ---");
        String[] userNames = {"u0", "u1", "u2", "u3"};
        for (int u = 0; u < nbUsers; u++) {
            System.out.print(userNames[u] + " → {");
            boolean first = true;
            for (int p = 0; p < nbPerms; p++) {
                if (userNeeds[u][p] == 1) {
                    if (!first) System.out.print(", ");
                    System.out.print(permNames[p]);
                    first = false;
                }
            }
            System.out.println("}");
        }

        System.out.println("\n========================================");
        System.out.println("RECHERCHE DE LA SOLUTION OPTIMALE...");
        System.out.println("========================================\n");

        if (solver.solve()) {
            System.out.println("✓ SOLUTION OPTIMALE TROUVÉE\n");
            System.out.println("Nombre total de rôles attribués : " + totalRoles.getValue());
            System.out.println();

            // Afficher l'attribution des rôles
            for (int u = 0; u < nbUsers; u++) {
                System.out.print(userNames[u] + " ← {");
                boolean first = true;
                for (int r = 0; r < nbRoles; r++) {
                    if (hasRole[u][r].getValue() == 1) {
                        if (!first) System.out.print(", ");
                        System.out.print(roleNames[r]);
                        first = false;
                    }
                }
                System.out.println("}");
            }

            // Vérification : afficher les permissions obtenues
            System.out.println("\n--- Vérification des permissions obtenues ---");
            for (int u = 0; u < nbUsers; u++) {
                System.out.print(userNames[u] + " obtient {");
                boolean[] obtainedPerms = new boolean[nbPerms];

                // Calculer les permissions obtenues via les rôles attribués
                for (int r = 0; r < nbRoles; r++) {
                    if (hasRole[u][r].getValue() == 1) {
                        for (int p = 0; p < nbPerms; p++) {
                            if (rolePermissions[r][p] == 1) {
                                obtainedPerms[p] = true;
                            }
                        }
                    }
                }

                boolean first = true;
                for (int p = 0; p < nbPerms; p++) {
                    if (obtainedPerms[p]) {
                        if (!first) System.out.print(", ");
                        System.out.print(permNames[p]);
                        first = false;
                    }
                }
                System.out.print("} - Besoins : {");

                first = true;
                for (int p = 0; p < nbPerms; p++) {
                    if (userNeeds[u][p] == 1) {
                        if (!first) System.out.print(", ");
                        System.out.print(permNames[p]);
                        first = false;
                    }
                }
                System.out.println("}");

                // Vérifier que tous les besoins sont satisfaits
                boolean allSatisfied = true;
                for (int p = 0; p < nbPerms; p++) {
                    if (userNeeds[u][p] == 1 && !obtainedPerms[p]) {
                        allSatisfied = false;
                        System.out.println("  ⚠ MANQUE: " + permNames[p]);
                    }
                }
                if (allSatisfied) {
                    System.out.println("  ✓ Tous les besoins sont satisfaits");
                }
            }

            System.out.println("\n--- Statistiques ---");
            System.out.println("Temps de résolution : " + solver.getTimeCount() + "s");
            System.out.println("Nombre de nœuds explorés : " + solver.getNodeCount());

        } else {
            System.out.println("✗ Aucune solution trouvée!");
        }
    }

    public static void main(String[] args) {
        solve();
    }
}