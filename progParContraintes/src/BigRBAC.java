// RBACDevOpsFixed.java
// Cas d'étude DevOps : 20 rôles, 30 permissions, hiérarchies, instance cohérente.
// Réalisé pour ChocoSolver 4.x

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class BigRBAC {

    static void readJSonFile(String filename) {
// 1. Lire le fichier JSON
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
            JSONTokener tokener = new JSONTokener(inputStream);
            JSONObject jsonObject = new JSONObject(tokener);

            // 2. Extraire la liste des rôles
            JSONArray roles = jsonObject.getJSONArray("roles");
            System.out.println("Rôles : " + roles);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static void resolution()
    {

        Model model = new Model("RBAC DevOps - Consistent Instance");

        final int NUM_ROLES = 20;
        final int NUM_PERMS = 30;
        final int NUM_USERS = 12;

        // ---------- Noms (lisibilité pédagogique) ----------
        String[] roleNames = new String[]{
                "PlatformAdmin", "SRE", "SeniorDev", "Dev", "Ops", "ReleaseMgr", "SecurityEng",
                "InfraEng", "DBA", "NetAdmin", "MonitoringEng", "CI_CD_Eng", "BackupEng", "CloudAdmin",
                "ContainerEng", "BuildEng", "QAEng", "AuditEng", "TechLead", "SupportEng"
        };

        String[] permNames = new String[]{
                "P00_VIEW_LOGS", "P01_READ_CONFIG", "P02_WRITE_CONFIG", "P03_DEPLOY",
                "P04_ROLLBACK", "P05_MANAGE_CERTS", "P06_MANAGE_USERS", "P07_READ_METRICS",
                "P08_WRITE_METRICS", "P09_RESTART_SERVICES", "P10_MANAGE_NETWORK", "P11_DB_BACKUP",
                "P12_DB_RESTORE", "P13_DB_ADMIN", "P14_CONTAINER_CREATE", "P15_CONTAINER_DELETE",
                "P16_IMAGE_PUSH", "P17_IMAGE_PULL", "P18_ACCESS_SECRETS", "P19_MANAGE_FIREWALL",
                "P20_EDIT_PIPELINE", "P21_TRIGGER_PIPELINE", "P22_VIEW_AUDIT", "P23_RUN_MIGRATION",
                "P24_MANAGE_VPC", "P25_MANAGE_STORAGE", "P26_CREATE_VM", "P27_DELETE_VM",
                "P28_SSH_ACCESS", "P29_MANAGE_ROLES"
        };

        // ---------- Hiérarchie des permissions (directe) ----------
        // exemple réaliste : DEPLOY -> WRITE_CONFIG -> READ_CONFIG ; IMAGE_PUSH -> IMAGE_PULL etc.
        Map<Integer, List<Integer>> permDirect = new HashMap<>();
        permDirect.put(3, Arrays.asList(2));   // DEPLOY -> WRITE_CONFIG
        permDirect.put(2, Arrays.asList(1));   // WRITE_CONFIG -> READ_CONFIG
        permDirect.put(16, Arrays.asList(17)); // IMAGE_PUSH -> IMAGE_PULL
        permDirect.put(11, Arrays.asList(12)); // DB_BACKUP -> DB_RESTORE (backup implies restore capability for model)
        permDirect.put(24, Arrays.asList(25)); // MANAGE_VPC -> MANAGE_STORAGE (example)
        permDirect.put(14, Arrays.asList(15)); // CONTAINER_CREATE -> CONTAINER_DELETE (choice)
        permDirect.put(20, Arrays.asList(21)); // EDIT_PIPELINE -> TRIGGER_PIPELINE
        permDirect.put(19, Arrays.asList(7));  // MANAGE_FIREWALL -> READ_METRICS (just an artificial chain)
        permDirect.put(22, Arrays.asList(1));  // VIEW_AUDIT -> READ_CONFIG
        permDirect.put(29, Arrays.asList(6));  // MANAGE_ROLES -> MANAGE_USERS
        permDirect.forEach((k, v) ->
        {
            System.out.print("P" + k + " : ");
            v.forEach(e -> System.out.print("P" + e + "; "));
            System.out.println();
        });

        // ---------- Hiérarchie des rôles (directe) ----------
        Map<Integer, List<Integer>> roleDirect = new HashMap<>();
        roleDirect.put(0, Arrays.asList(1, 13)); // PlatformAdmin -> SRE, CloudAdmin
        roleDirect.put(1, Arrays.asList(4, 7));  // SRE -> Ops, InfraEng
        roleDirect.put(2, Arrays.asList(3));    // SeniorDev -> Dev
        roleDirect.put(5, Arrays.asList(11));   // ReleaseMgr -> CI_CD_Eng
        roleDirect.put(10, Arrays.asList(0));   // MonitoringEng -> PlatformAdmin (garder un lien mais DAG doit rester raisonnable)
        roleDirect.forEach((k, v) ->
        {
            System.out.print(roleNames[k] + " : ");
            v.forEach(e -> System.out.print(roleNames[e] + "; "));
            System.out.println();
        });
        // Note : we will compute closure; avoid cycles in these direct edges in practice.

        // ---------- Calcul des closures transitives (permissions & roles) ----------
        boolean[][] permClosure = new boolean[NUM_PERMS][NUM_PERMS];
        for (int i = 0; i < NUM_PERMS; i++) permClosure[i][i] = true;
        for (Map.Entry<Integer, List<Integer>> e : permDirect.entrySet()) {
            int a = e.getKey();
            for (int b : e.getValue()) permClosure[a][b] = true;
        }
        // Floyd-Warshall pour transitivité
        for (int k = 0; k < NUM_PERMS; k++)
            for (int i = 0; i < NUM_PERMS; i++)
                if (permClosure[i][k])
                    for (int j = 0; j < NUM_PERMS; j++)
                        permClosure[i][j] = permClosure[i][j] || permClosure[k][j];

        boolean[][] roleClosure = new boolean[NUM_ROLES][NUM_ROLES];
        for (int i = 0; i < NUM_ROLES; i++) roleClosure[i][i] = true;
        for (Map.Entry<Integer, List<Integer>> e : roleDirect.entrySet()) {
            int a = e.getKey();
            for (int b : e.getValue()) roleClosure[a][b] = true;
        }
        for (int k = 0; k < NUM_ROLES; k++)
            for (int i = 0; i < NUM_ROLES; i++)
                if (roleClosure[i][k])
                    for (int j = 0; j < NUM_ROLES; j++)
                        roleClosure[i][j] = roleClosure[i][j] || roleClosure[k][j];

        // ---------- Attributions directes permissions->rôles (déterministes) ----------
        // On génère 3..6 permissions directes par rôle, puis on s'assure que toute permission est couverte.
        Map<Integer, List<Integer>> rolePerm = new HashMap<>();
        rolePerm.put(0, Arrays.asList(0, 4, 9, 15, 20));
        rolePerm.put(1, Arrays.asList(7, 10, 11, 18));
        rolePerm.put(2, Arrays.asList(1, 8, 22, 27));
        rolePerm.put(3, Arrays.asList(0, 2, 13));
        rolePerm.put(4, Arrays.asList(5, 17, 23));
        rolePerm.put(5, Arrays.asList(3, 4, 20));
        rolePerm.put(6, Arrays.asList(6, 18, 19));
        rolePerm.put(7, Arrays.asList(1, 24, 26));
        rolePerm.put(8, Arrays.asList(11, 12, 13));
        rolePerm.put(9, Arrays.asList(10, 19, 25));
        rolePerm.put(10, Arrays.asList(0, 7, 8));
        rolePerm.put(11, Arrays.asList(20, 21, 16));
        rolePerm.put(12, Arrays.asList(11, 25));
        rolePerm.put(13, Arrays.asList(24, 26, 27, 28));
        rolePerm.put(14, Arrays.asList(14, 16));
        rolePerm.put(15, Arrays.asList(21, 28));
        rolePerm.put(16, Arrays.asList(0, 21, 23));
        rolePerm.put(17, Arrays.asList(22, 1));
        rolePerm.put(18, Arrays.asList(0, 2, 21));
        rolePerm.put(19, Arrays.asList(28, 9, 7));
        System.out.println("=".repeat(30));
        System.out.println("=== Role -> Direct Permissions ===");
        rolePerm.forEach((k, v) ->
        {
            System.out.print(roleNames[k] + " : ");
            v.forEach(e -> System.out.print(permNames[e] + "; "));
            System.out.println();
        });


        boolean[][] rolePermDirect = new boolean[NUM_ROLES][NUM_PERMS];
        for (int r = 0; r < NUM_ROLES; r++) {
            var chosen = rolePerm.get(r);
            for (int p : chosen) rolePermDirect[r][p] = true;
        }
        // Garantir couverture : pour chaque permission non-couverte, l'assigner à un rôle dédié (deterministic)
        for (int p = 0; p < NUM_PERMS; p++) {
            boolean covered = false;
            for (int r = 0; r < NUM_ROLES && !covered; r++) {
                if (rolePermDirect[r][p]) covered = true;
            }
            if (!covered) {
                // assign to role index p % NUM_ROLES deterministically
                rolePermDirect[p % NUM_ROLES][p] = true;
            }
        }

        // ---------- Calcul roleGivesPermFixed en appliquant la fermeture des permissions ----------
        boolean[][] roleGivesPermFixed = new boolean[NUM_ROLES][NUM_PERMS];
        for (int r = 0; r < NUM_ROLES; r++) {
            for (int q = 0; q < NUM_PERMS; q++) {
                if (rolePermDirect[r][q]) {
                    for (int p = 0; p < NUM_PERMS; p++) {
                        if (permClosure[q][p]) roleGivesPermFixed[r][p] = true;
                    }
                }
            }
        }


        // ---------- Construire des besoins utilisateurs compatibles ----------
        // On choisit les permissions demandées uniquement parmi celles couvertes par au moins one role.
        List<Integer> allProvidedPerms = new ArrayList<>();
        for (int p = 0; p < NUM_PERMS; p++) {
            boolean provided = false;
            for (int r = 0; r < NUM_ROLES && !provided; r++) {
                if (roleGivesPermFixed[r][p]) provided = true;
            }
            if (provided) allProvidedPerms.add(p);
        }
        // assure qu'il y a bien des permissions disponibles (devrait être vrai)
        if (allProvidedPerms.isEmpty()) {
            throw new RuntimeException("Aucune permission fournie — instance invalide.");
        }

        // ---------- Besoins en permissions des utilisateurs ----------
        Map<Integer, List<Integer>> userRequirements = new HashMap<>();
        userRequirements.put(0, Arrays.asList(0, 1, 7, 11, 18));
        userRequirements.put(1, Arrays.asList(0, 20, 16, 27));
        userRequirements.put(2, Arrays.asList(1, 2, 10, 24));
        userRequirements.put(3, Arrays.asList(0, 8, 11, 23, 21));
        userRequirements.put(4, Arrays.asList(0, 7, 18, 28));
        userRequirements.put(5, Arrays.asList(3, 20, 21, 2));
        userRequirements.put(6, Arrays.asList(11, 12, 13, 23));
        userRequirements.put(7, Arrays.asList(24, 26, 10));
        userRequirements.put(8, Arrays.asList(0, 21, 22, 1));
        userRequirements.put(9, Arrays.asList(17, 16, 14));
        userRequirements.put(10, Arrays.asList(28, 0, 2, 25));
        userRequirements.put(11, Arrays.asList(19, 10, 7));

        int[][] required = new int[NUM_USERS][];
        for (int u = 0; u < NUM_USERS; u++) {
            required[u] = userRequirements.get(u).stream().mapToInt(i -> i).toArray();
            Arrays.sort(required[u]);
        }

        // ---------- VARIABLES CHOCO ----------
        BoolVar[][] userRole = model.boolVarMatrix("UR", NUM_USERS, NUM_ROLES);

        // roleGivesPerm as BoolVar fixed to the precomputed values
        BoolVar[][] roleGivesPerm = new BoolVar[NUM_ROLES][NUM_PERMS];
        for (int r = 0; r < NUM_ROLES; r++) {
            for (int p = 0; p < NUM_PERMS; p++) {
                roleGivesPerm[r][p] = model.boolVar("RG_r" + (r + 1) + "_p" + (p + 1));
                if (roleGivesPermFixed[r][p]) model.arithm(roleGivesPerm[r][p], "=", 1).post();
                else model.arithm(roleGivesPerm[r][p], "=", 0).post();
            }
        }

        BoolVar[][] userHasPerm = model.boolVarMatrix("UP", NUM_USERS, NUM_PERMS);

        // ---------- Contraintes : rôle->rôle (hiérarchie) ----------
        // si userRole[u][parent] alors userRole[u][child] (parent implies child via closure)
        for (int parent = 0; parent < NUM_ROLES; parent++) {
            for (int child = 0; child < NUM_ROLES; child++) {
                if (parent != child && roleClosure[parent][child]) {
                    for (int u = 0; u < NUM_USERS; u++) {
                        model.ifThen(
                                model.arithm(userRole[u][parent], "=", 1),
                                model.arithm(userRole[u][child], "=", 1)
                        );
                    }
                }
            }
        }

        // ---------- Contraintes : userHasPerm construction via AND + OR ----------
        for (int u = 0; u < NUM_USERS; u++) {
            for (int p = 0; p < NUM_PERMS; p++) {
                List<BoolVar> contributors = new ArrayList<>();
                for (int r = 0; r < NUM_ROLES; r++) {
                    // temp_r = userRole[u][r] AND roleGivesPerm[r][p]
                    BoolVar temp = model.boolVar("tmp_u" + (u + 1) + "_r" + (r + 1) + "_p" + (p + 1));
                    model.addClausesBoolAndEqVar(userRole[u][r], roleGivesPerm[r][p], temp);
                    contributors.add(temp);
                }
                model.addClausesBoolOrArrayEqVar(contributors.toArray(new BoolVar[0]), userHasPerm[u][p]);
            }
        }

        // ---------- Contraintes : l'utilisateur doit posséder ses permissions requises ----------
        for (int u = 0; u < NUM_USERS; u++) {
            for (int pReq : required[u]) {
                model.arithm(userHasPerm[u][pReq], "=", 1).post();
            }
        }

        // ---------- Objectif : minimiser nombre total de rôles attribués ----------
        BoolVar[] allUserRoles = flatten(userRole);
        IntVar totalRoles = model.intVar("TotalAssignedRoles", 0, NUM_USERS * NUM_ROLES);
        model.sum(allUserRoles, "=", totalRoles).post();
        model.setObjective(Model.MINIMIZE, totalRoles);

        // ---------- Résolution ----------
        Solution best = model.getSolver().findOptimalSolution(totalRoles, true);
        //afficher le temps de résolution
        System.out.println("Temps de résolution (ms) : " + model.getSolver().getTimeCount());

        if (best == null) {
            System.out.println("Aucune solution trouvée — étrange, instance censée être cohérente.");
            return;
        }

        // ---------- Affichage résultat ----------
        System.out.println("=== SOLUTION OPTIMALE (DevOps instance cohérente) ===");
        System.out.println("Total roles assigned = " + best.getIntVal(totalRoles));
        System.out.println();

        for (int u = 0; u < NUM_USERS; u++) {
            System.out.println("User U" + (u + 1));
            System.out.print("  Assigned roles: ");
            List<Integer> assignedRoles = new ArrayList<>();
            for (int r = 0; r < NUM_ROLES; r++) {
                if (best.getIntVal(userRole[u][r]) == 1) {
                    assignedRoles.add(r);
                    System.out.print(roleNames[r] + " ");
                }
            }
            if (assignedRoles.isEmpty()) System.out.print("(none)");
            System.out.println();

            System.out.print("  Obtained perms: ");
            List<Integer> obtained = new ArrayList<>();
            for (int p = 0; p < NUM_PERMS; p++) {
                if (best.getIntVal(userHasPerm[u][p]) == 1) {
                    obtained.add(p);
                    System.out.print(permNames[p] + " ");
                }
            }
            if (obtained.isEmpty()) System.out.print("(none)");
            System.out.println();

            System.out.print("  Required perms: ");
            for (int p : required[u]) System.out.print(permNames[p] + " ");
            System.out.println();

            // missing (should be none)
            List<Integer> missing = new ArrayList<>();
            for (int p : required[u]) if (!obtained.contains(p)) missing.add(p);
            System.out.print("  Missing perms: ");
            if (missing.isEmpty()) System.out.print("(none)");
            else for (int p : missing) System.out.print(permNames[p] + " ");
            System.out.println();

            // extras
            List<Integer> extras = new ArrayList<>();
            for (int p : obtained) if (Arrays.binarySearch(required[u], p) < 0) extras.add(p);
            System.out.print("  Extra perms: ");
            if (extras.isEmpty()) System.out.print("(none)");
            else for (int p : extras) System.out.print(permNames[p] + " ");
            System.out.println("\n");
        }

        // ---------- Résumé roles -> perms (utile en TP) ----------
        System.out.println("=== Roles -> Permissions (after closure) ===");
        for (int r = 0; r < NUM_ROLES; r++) {
            System.out.print(roleNames[r] + " -> ");
            List<String> perms = new ArrayList<>();
            for (int p = 0; p < NUM_PERMS; p++) {
                if (roleGivesPermFixed[r][p]) perms.add(permNames[p]);
            }
            if (perms.isEmpty()) System.out.print("(none)");
            else for (String s : perms) System.out.print(s + " ");
            System.out.println();
        }

        System.out.println("\nTerminé.");

    }
    // helper to flatten BoolVar matrix to array
    static BoolVar[] flatten(BoolVar[][] m) {
        List<BoolVar> out = new ArrayList<>();
        for (BoolVar[] row : m) Collections.addAll(out, row);
        return out.toArray(new BoolVar[0]);
    }

    public static void main(String[] args) {
//        readJSonFile("C:\\Users\\emmanuel adam\\Documents\\GitHub\\IntelligenceArtificielleJava\\progParContraintes\\src\\RBACDevOPs.json");
        resolution();
    }

}
