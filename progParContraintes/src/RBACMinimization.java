
// RBACMinimization.java
// Exemple complet de modélisation RBAC + minimisation des "privilèges inutiles"
// Utilise ChocoSolver (4.x)

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

public class RBACMinimization {
    /** petite classe pour nommer les rôles, utilisateurs et privilèges */
    public record Pair(String name, String description) {
        public String toString() {
            return "("+name + " - "+ description + ")";
        }
    }


    public static void main(String[] args) {

        // -------------------------
        // 1) Données du problème
        // -------------------------
        // Privilèges P0..P6
        //juste pour l'affichage
        Pair[] privileges = new Pair[] {
                new Pair("P0","Lire dossier patient"),
                new Pair("P1","Modifier dossier patient"),
                new Pair("P2","Prescrire actes médicaux"),
                new Pair("P3","Gérer admissions"),
                new Pair("P4","Gérer lits"),
                new Pair("P5","Valider facturation"),
                new Pair("P6","Accéder statistiques globales")
        };
        int nbP = privileges.length;

        // Rôles R0..R5 et leurs noms
        Pair[] roles = new Pair[] {
                new Pair("R0","Médecin"),
                new Pair("R1","Infirmier"),
                new Pair("R2","Secrétaire"),
                new Pair("R3","Gestionnaire admin"),
                new Pair("R4","Interne"),
                new Pair("R5","Chef de service")
        };
        int nbR = roles.length;

        // matrice rolesPrivileges[r][p] = 1 si r fournit p
        int[][] rolesPrivileges = new int[][] {
                // P0 P1 P2 P3 P4 P5 P6
                { 1, 1, 1, 0, 0, 0, 0 }, // R0 Médecin: P0,P1,P2
                { 1, 0, 0, 0, 1, 0, 0 }, // R1 Infirmier: P0,P4
                { 1, 0, 0, 1, 0, 0, 0 }, // R2 Secrétaire: P0,P3
                { 0, 0, 0, 1, 0, 1, 1 }, // R3 Gestionnaire admin: P3,P5,P6
                { 1, 1, 0, 0, 0, 0, 0 }, // R4 Interne: P0,P1
                { 1, 1, 1, 0, 0, 0, 1 }  // R5 Chef de service: P0,P1,P2,P6
        };

        // Utilisateurs U0..U5 et leurs noms
        Pair[] users = new Pair[] {
                new Pair("U0","Médecin sénior"),
                new Pair("U1","Infirmière"),
                new Pair("U2","Secrétaire"),
                new Pair("U3","Interne"),
                new Pair("U4","Gestionnaire admin"),
                new Pair("U5","Médecin junior")
        };
        int nbU = users.length;

        // matrice need[u][p] = 1 si l'utilisateur u a besoin du privilège p
        int[][] need = new int[][] {
                // P0 P1 P2 P3 P4 P5 P6
                { 1, 1, 1, 0, 0, 0, 1 }, // U0: P0,P1,P2,P6
                { 1, 0, 0, 0, 1, 0, 0 }, // U1: P0,P4
                { 1, 0, 0, 1, 0, 0, 0 }, // U2: P0,P3
                { 1, 1, 1, 0, 0, 0, 0 }, // U3: P0,P1,P2
                { 0, 0, 0, 1, 0, 1, 1 }, // U4: P3,P5,P6
                { 1, 1, 1, 0, 0, 0, 0 }  // U5: P0,P1,P2
        };

        // matrice de hiérarchie : requires[p][q] = 1 si la permission p implique d’avoir la permission q
        int[][] requires = new int[][] {
                //P0 P1 P2 P3 P4 P5 P6
                { 0, 0, 0, 0, 0, 0, 0 }, // P0 -> rien
                { 1, 0, 0, 0, 0, 0, 0 }, // P1 -> P0
                { 1, 1, 0, 0, 0, 0, 0 }, // P2 -> P0,P1
                { 0, 0, 0, 0, 0, 0, 0 }, // P3 -> rien
                { 1, 0, 0, 0, 0, 0, 0 }, // P4 -> P0
                { 1, 0, 0, 0, 0, 0, 0 }, // P5 -> P0
                { 1, 0, 0, 0, 0, 0, 0 }  // P6 -> P0
        };

        // -------------------------
        // 2) Modèle Choco
        // -------------------------
        Model model = new Model("RBAC Minimisation des privilèges");

        // attrib[u][r] = 1 si on attribue le rôle r à l'utilisateur u
        BoolVar[][] roleU = new BoolVar[nbU][nbR];
        for (int u = 0; u < nbU; u++) {
            for (int r = 0; r < nbR; r++) {
                roleU[u][r] = model.boolVar("attrib_u" + (u) + "_r" + (r));
            }
        }

        // hasPriv[u][p] = 1 si l'utilisateur u obtient le privilège p via ses rôles
        BoolVar[][] hasPriv = new BoolVar[nbU][nbP];
        for (int u = 0; u < nbU; u++) {
            for (int p = 0; p < nbP; p++) {
                hasPriv[u][p] = model.boolVar("has_u" + (u) + "_p" + (p));
            }
        }

        // extra[u][p] = 1 si l'utilisateur a le privilège p et que p n'est pas nécessaire
        BoolVar[][] extra = new BoolVar[nbU][nbP];
        for (int u = 0; u < nbU; u++) {
            for (int p = 0; p < nbP; p++) {
                extra[u][p] = model.boolVar("extra_u" + (u) + "_p" + (p));
            }
        }

        // sumRoles[u] = nombre de rôles donnés à u
        IntVar[] sumRoles = new IntVar[nbU];
        for (int u = 0; u < nbU; u++) {
            sumRoles[u] = model.intVar("sumRoles_u" + (u), 0, nbR);
            model.sum(roleU[u], "=", sumRoles[u]).post();
        }


        // Pour tout u, si u possède p alors il doit aussi posséder toutes les permissions requises
        for (int u = 0; u < nbU; u++) {
            for (int p = 0; p < nbP; p++) {
                for (int q = 0; q < nbP; q++) {
                    if (requires[p][q] == 1) {
                        // hasPriv[u][p] -> hasPriv[u][q]
                        model.ifThen(
                                model.arithm(hasPriv[u][p], "=", 1),
                                model.arithm(hasPriv[u][q], "=", 1)
                        );
                    }
                }
            }
        }
        // -------------------------
        // 3) Contraintes organisationnelles
        // -------------------------

        // 3.1 Cardinalité: au maximum 2 rôles par utilisateur
        for (int u = 0; u < nbU; u++) {
            model.arithm(sumRoles[u], "<=", 2).post();
        }

        // 3.2 R5  ne peut être attribué qu'à un seul utilisateur
        int idxR5 = 5;
        BoolVar[] r5AssignedTo = new BoolVar[nbU];
        for (int u = 0; u < nbU; u++) r5AssignedTo[u] = roleU[u][idxR5];
        IntVar sumR5 = model.intVar("sumR5", 0, nbU);
        model.sum(r5AssignedTo, "=", sumR5).post();
        model.arithm(sumR5, "=", 1).post();

        // 3.3 Séparation des tâches (SoD) — paires incompatibles de rôles
        // Liste des paires interdites (ri, rj) en indices:
        int[][] incompatiblePairs = new int[][] {
                { 2, 0 }, // R2 incompatible avec R0
                { 2, 5 }, // R2 incompatible avec R5
                { 3, 0 }, // R3 incompatible avec R0
                { 3, 5 }  // R3 incompatible avec R5
        };
        // appliquer: pour tout utilisateur u, x[u][ri] + x[u][rj] <= 1
        for (int u = 0; u < nbU; u++) {
            for (int[] pair : incompatiblePairs) {
                int ri = pair[0], rj = pair[1];
                model.arithm(roleU[u][ri], "+", roleU[u][rj], "<=", 1).post();
            }
        }

        // 3.4 Couverture exacte des privilèges :
        // hasPriv[u][p] <-> OR_r( x[u][r] & roleProvides[r][p] )
        // On linearise avec deux directions:
        //  - si un rôle r qui fournit p est attribué => hasPriv = 1 (x[u][r] -> hasPriv)
        //  - si hasPriv = 1 => somme des x[u][r] (pour r fournissant p) >= 1
        for (int u = 0; u < nbU; u++) {
            for (int p = 0; p < nbP; p++) {
                // collect roles that provide p
                List<BoolVar> providers = new ArrayList<>();
                for (int r = 0; r < nbR; r++) {
                    if (rolesPrivileges[r][p] == 1) {
                        providers.add(roleU[u][r]);
                        // x[u][r] -> hasPriv[u][p]
                        model.ifThen(roleU[u][r], model.arithm(hasPriv[u][p], "=", 1));
                    }
                }
                // If there is at least one provider, enforce: hasPriv -> sum(providers) >= 1
                if (!providers.isEmpty()) {
                    IntVar sumProviders = model.intVar("sumProv_u" + (u+1) + "_p" + (p+1), 0, providers.size());
                    var tabProviders = providers.toArray(new BoolVar[0]);
//                    model.sum(tabProviders, "=", sumProviders).post();
                    // hasPriv == 1 => sumProviders >= 1
                    model.ifThen(model.arithm(hasPriv[u][p], "=", 1),  model.sum(tabProviders, ">=", 1));
                    // Conversely, if sumProviders >= 1 then hasPriv can be 1 (we already posted x->hasPriv)
                    // We don't force hasPriv==0 when sumProviders==0 because extra/need logic will set requirements below.
                } else {
                    // Si aucun rôle ne fournit p, impossible d'obtenir p : forcer hasPriv = 0
                    model.arithm(hasPriv[u][p], "=", 0).post();
                }
            }
        }

        // 3.5 Exiger que les privilèges nécessaires soient effectivement présents (couverture exacte)
        // Pour p nécessaire pour u: hasPriv[u][p] == 1
        for (int u = 0; u < nbU; u++) {
            for (int p = 0; p < nbP; p++) {
                if (need[u][p] == 1) {
                    model.arithm(hasPriv[u][p], "=", 1).post();
                }
            }
        }

        // 3.6 Définition de extra[u][p]
        // extra[u][p] = hasPriv[u][p] si p n'est pas nécessaire, sinon 0
        for (int u = 0; u < nbU; u++) {
            for (int p = 0; p < nbP; p++) {
                if (need[u][p] == 1) {
                    model.arithm(extra[u][p], "=", 0).post();
                } else {
                    // extra == hasPriv
                    model.arithm(extra[u][p], "=", hasPriv[u][p]).post();
                }
            }
        }

        // -------------------------
        // 4) Objectif d'optimisation
        // -------------------------
        // Minimiser : totalExtraPrivileges (pondéré fortement) + totalRoles (secondaire)
        // On forme : obj = 100 * totalExtra + 1 * totalRoles
        // (100 choisit pour prioriser la minimisation des extras)
        IntVar[] extrasFlatten = new IntVar[nbU * nbP];
        int idx = 0;
        for (int u = 0; u < nbU; u++) for (int p = 0; p < nbP; p++) extrasFlatten[idx++] = extra[u][p].boolVar();

        IntVar totalExtra = model.intVar("totalExtra", 0, nbU * nbP);
        model.sum(extrasFlatten, "=", totalExtra).post();

        IntVar totalRoles = model.intVar("totalRoles", 0, nbU * nbR);
        // sum over all sumRoles
        model.sum(sumRoles, "=", totalRoles).post();

        // objVar = 100 * totalExtra + totalRoles
        IntVar objVar = model.intVar("obj", 0, 1000);
        // scalar: coefficients correspond to [totalExtra, totalRoles]
        model.scalar(new IntVar[]{totalExtra, totalRoles}, new int[]{100, 1}, "=", objVar).post();

        // -------------------------
        // 5) Recherche de la solution optimale (recherche itérative améliorante)
        // -------------------------
        Solver solver = model.getSolver();

        // Nous itérons : on cherche une solution, on mémorise sa valeur, on ajoute contrainte obj < best et on continue.
        // On conserve la meilleure affectation trouvée pour l'afficher à la fin.
        int bestObj = Integer.MAX_VALUE;
        boolean foundAny = false;

        // To store best assignment
        int[][] bestAssign = new int[nbU][nbR];

        while (solver.solve()) {
            int curObj = objVar.getValue();
            foundAny = true;
            System.out.println("Solution trouvée (obj=" + curObj + ") -- roles par user:");
            for (int u = 0; u < nbU; u++) {
                System.out.print("  " + users[u] + " : ");
                for (int r = 0; r < nbR; r++) {
                    if (roleU[u][r].getValue() == 1) {
                        System.out.print(roles[r] + " ");
                    }
                }
                System.out.println();
            }
            System.out.println("  totalExtra=" + totalExtra.getValue() + ", totalRoles=" + totalRoles.getValue());
            System.out.println("---------------");

            // store if better
            if (curObj < bestObj) {
                bestObj = curObj;
                for (int u = 0; u < nbU; u++) {
                    for (int r = 0; r < nbR; r++) {
                        bestAssign[u][r] = roleU[u][r].getValue();
                    }
                }
            }

            // add constraint to find strictly better solution next iteration
            // (objVar is an IntVar, we post obj < curObj)
            model.arithm(objVar, "<", curObj).post();
        }

        // -------------------------
        // 6) Résultat final
        // -------------------------
        if (!foundAny) {
            System.out.println("Aucune solution trouvée : contraintes inconsitantes.");
        } else {
            System.out.println("\nMeilleure solution obtenue (obj=" + bestObj + "):");
            for (int u = 0; u < nbU; u++) {
                System.out.print(users[u] + " : ");
                boolean none = true;
                for (int r = 0; r < nbR; r++) {
                    if (bestAssign[u][r] == 1) {
                        System.out.print(roles[r] + "  ");
                        none = false;
                    }
                }
                if (none) System.out.print("(aucun rôle)");
                System.out.println();
            }

            // afficher détails: privilèges obtenus / privilèges inutiles
            int totExtra = 0, totRoles = 0;
            System.out.println("\nDétail par utilisateur :");
            for (int u = 0; u < nbU; u++) {
                int extraCount = 0;
                int roleCount = 0;
                System.out.println(" - " + users[u]);
                // rôles
                System.out.print("    Rôles attribués : ");
                for (int r = 0; r < nbR; r++) {
                    if (bestAssign[u][r] == 1) {
                        System.out.print(roles[r] + " ");
                        roleCount++;
                    }
                }
                System.out.println();
                // privilèges obtenus et inutiles
                System.out.print("    Privilèges obtenus : ");
                for (int p = 0; p < nbP; p++) {
                    // recompute hasPriv via rolesProvides & bestAssign
                    int has = 0;
                    for (int r = 0; r < nbR; r++) if (bestAssign[u][r] == 1 && rolesPrivileges[r][p] == 1) has = 1;
                    if (has == 1) System.out.print(privileges[p] + " ");
                }
                System.out.println();
                System.out.print("    Privilèges inutiles : ");
                for (int p = 0; p < nbP; p++) {
                    int has = 0;
                    for (int r = 0; r < nbR; r++) if (bestAssign[u][r] == 1 && rolesPrivileges[r][p] == 1) has = 1;
                    if (has == 1 && need[u][p] == 0) {
                        System.out.print(privileges[p] + " ");
                        extraCount++;
                    }
                }
                System.out.println(" (count=" + extraCount + ")");
                totExtra += extraCount;
                totRoles += roleCount;
            }

            System.out.println("\nTotal privilèges inutiles = " + totExtra);
            System.out.println("Total rôles attribués = " + totRoles);
            System.out.println("Score (100*extra + roles) = " + bestObj);
        }

    }
}
