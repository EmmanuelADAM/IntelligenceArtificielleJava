/* =====================================================================
   Catalogue des ressources du site IntelligenceArtificielleJava
   ---------------------------------------------------------------------
   Deux formes d'entrées :

   1) Un projet (affiché en carte) :
     folder    dossier du projet dans le dépôt (obligatoire)
     title     titre lisible
     type      "demo" | "exo" | "tp" | "sujet" | "cours"
     desc      une ou deux phrases
     main      classe à lancer, chemin relatif au dossier
     readme    chemin complet d'un readme à lire (facultatif)
     files     autres fichiers utiles, chemins relatifs au dossier
     libs      bibliothèques nécessaires (étiquettes)
     todo      ce qu'il y a à faire (encadré)
     solutions [{ path, title }]  corrigés (fichier ou dossier)

   2) Un fichier seul, dans un groupe marqué compact: true
      (affiché en ligne, utile pour les nombreux exemples Choco) :
     file      chemin complet du fichier .java
     title, desc

   Les liens GitHub sont construits automatiquement.
   ===================================================================== */

window.SITE = {
  repo: "EmmanuelADAM/IntelligenceArtificielleJava",
  branch: "master",
  author: "Emmanuel Adam",
};

window.CATALOGUE = [
  /* ---------------------------------------------------------------- */
  {
    id: "recherche",
    title: "Recherche dans un espace d'états",
    tagline: "Largeur, profondeur, A*",
    intro:
      "Un problème devient un graphe d'états à parcourir. La recherche aveugle explore tout, systématiquement ; A* s'appuie sur une heuristique pour aller droit au but.",
    groups: [
      {
        title: "Recherche aveugle",
        items: [
          {
            folder: "deepAndWideSearch/src",
            title: "Largeur ou profondeur",
            type: "demo",
            desc: "Nœuds libres et nœuds clos, sans récursivité. Sur les N-reines, 10 reines prennent 15 ms en profondeur et 10 s en largeur.",
            main: "StateQueen.java",
            files: ["DeepAndBreadthSearch.java", "State.java"],
            readme: "deepAndWideSearch/src/readme.md",
          },
        ],
      },
      {
        title: "Recherche informée",
        items: [
          {
            folder: "astar/src/taquin",
            title: "A* sur le taquin",
            type: "demo",
            desc: "Une classe State générique (f, g, h) à étendre, appliquée au taquin : faire coulisser la case vide pour remettre les lettres dans l'ordre.",
            main: "StateTaquin.java",
            files: ["AlgoAStar.java", "State.java"],
            readme: "astar/src/taquin/readme.md",
          },
          {
            folder: "astar/src/chemin",
            title: "A* et Theta* dans un entrepôt",
            type: "demo",
            desc: "Plus court chemin sur une grille encombrée de conteneurs, avec voisinage à 4 ou 8 directions. Une fenêtre Swing affiche les nœuds explorés.",
            main: "AlgoAStar.java",
            files: ["AlgoThetaStar.java", "environment/Entrepot.java", "actions/TypeNeighborhood.java"],
            libs: ["Swing"],
          },
        ],
      },
    ],
  },

  /* ---------------------------------------------------------------- */
  {
    id: "locale",
    title: "Recherche locale et évolution",
    tagline: "Hasard, voisinage, génétique",
    intro:
      "Quand l'espace est trop grand pour être parcouru, on part d'une solution quelconque et on l'améliore pas à pas, ou on fait évoluer une population de solutions.",
    groups: [
      {
        title: "Hasard et voisinage",
        items: [
          {
            folder: "compteEstBonSimplifie/src",
            title: "Le compte est bon",
            type: "demo",
            desc: "Recherche aléatoire d'un enchaînement d'opérations qui atteint le nombre cible avec les plaques tirées.",
            main: "Solution.java",
            files: ["OperateurFunc.java"],
          },
          {
            folder: "nQueensRandom/src/centralised",
            title: "N-reines par réparation locale",
            type: "demo",
            desc: "Placement aléatoire, puis on déplace les reines les plus en conflit. Environ 1,5 s pour placer 1 000 reines, 11 ms pour 100.",
            main: "RandomQueens.java",
            readme: "nQueensRandom/src/centralised/readme.md",
          },
        ],
      },
      {
        title: "Algorithmes génétiques",
        items: [
          {
            folder: "algoGen/src/parcours",
            title: "Plus court parcours entre villes",
            type: "demo",
            desc: "Une séquence de villes sans doublon : la mutation devient une permutation et le croisement n'est gardé que s'il ne crée pas de doublon.",
            main: "ParcoursGenetique.java",
            files: ["Sequence.java"],
            libs: ["Swing"],
          },
          {
            folder: "algoGen/src/sac",
            title: "Le sac à dos",
            type: "demo",
            desc: "Une séquence binaire indique les produits retenus. On cherche le sac le plus intéressant sans dépasser le poids maximal.",
            main: "AlgoGPourSacs.java",
            files: ["Sequence.java", "CriteresSac.java"],
            libs: ["Swing"],
          },
        ],
      },
    ],
  },

  /* ---------------------------------------------------------------- */
  {
    id: "contraintes",
    title: "Programmation par contraintes",
    tagline: "CSP, COP, SAT",
    intro:
      "On décrit les variables, leurs domaines et les contraintes ; le solveur trouve une solution ou la meilleure. Les exemples utilisent Choco Solver, plus un premier pas avec Sat4J.",
    groups: [
      {
        title: "Supports",
        items: [
          {
            folder: "progParContraintes",
            title: "Introduction à Choco Solver",
            type: "cours",
            desc: "Modèle, variables, contraintes, stratégies de recherche (FirstFail, IntDomainMin…) et optimisation, sur l'exemple des N-reines.",
            readme: "progParContraintes/readmeChocoSolver.md",
            libs: ["Choco Solver"],
          },
          {
            folder: "progParContraintes",
            title: "Énoncés et solutions types",
            type: "sujet",
            desc: "Énoncés de TD avec leur modélisation : N-reines, carré magique, TecToNic, emplois du temps, voyageur de commerce, rôles, clés, atterrissages.",
            readme: "progParContraintes/readmeSolutions.md",
            libs: ["Choco Solver"],
          },
        ],
      },
      {
        title: "Puzzles et grands classiques",
        compact: true,
        items: [
          { file: "progParContraintes/src/NReines.java", title: "N-reines", desc: "Contraintes arithmétiques ne, sub, add sur des domaines bornés." },
          { file: "progParContraintes/src/NQueens.java", title: "N-reines, variantes", desc: "Plusieurs modélisations et stratégies de recherche comparées." },
          { file: "progParContraintes/src/MagicSquare.java", title: "Carré magique", desc: "Les entiers de 1 à n² avec la même somme sur chaque ligne, colonne et diagonale." },
          { file: "progParContraintes/src/SudokuChoco.java", title: "Sudoku", desc: "Valeurs différentes par ligne, colonne et bloc." },
          { file: "progParContraintes/src/TecToNic.java", title: "TecToNic", desc: "Régions colorées et cases voisines toutes différentes." },
          { file: "progParContraintes/src/ZebraPuzzle.java", title: "Énigme du zèbre", desc: "Cinq maisons, cinq attributs, quinze indices." },
          { file: "progParContraintes/src/Golomb.java", title: "Règle de Golomb", desc: "Des graduations dont toutes les distances deux à deux sont distinctes." },
        ],
      },
      {
        title: "Planification et affectation",
        compact: true,
        items: [
          { file: "progParContraintes/src/CourseScheduler.java", title: "Emploi du temps", desc: "Placer des cours dans des créneaux, des salles et chez des enseignants." },
          { file: "progParContraintes/src/CourseGroupScheduler.java", title: "Emploi du temps par groupes", desc: "Un groupe d'étudiants ne suit jamais deux cours en même temps." },
          { file: "progParContraintes/src/ProductionScheduling.java", title: "Ordonnancement de production", desc: "Des tâches avec précédences et date de fin imposée." },
          { file: "progParContraintes/src/AffectationTaches.java", title: "Affectation de tâches", desc: "Confier chaque tâche à un ouvrier au coût total minimal." },
          { file: "progParContraintes/src/Atterrissage.java", title: "Atterrissage d'avions", desc: "Une fenêtre et une date idéale par avion, des écarts entre avions, des pénalités d'avance ou de retard." },
          { file: "progParContraintes/src/VRPChoco.java", title: "Voyageur de commerce", desc: "Le circuit le plus court passant par toutes les villes." },
        ],
      },
      {
        title: "Sécurité et configuration",
        compact: true,
        items: [
          { file: "progParContraintes/src/RBAC.java", title: "Attribution de rôles (RBAC)", desc: "Donner à chaque utilisateur ses permissions avec le moins de rôles possible." },
          { file: "progParContraintes/src/KeyGen.java", title: "Générateur de clés", desc: "15 caractères, au moins 2 majuscules, 2 minuscules, 2 chiffres et 2 spéciaux, sans répétition." },
          { file: "progParContraintes/src/KeyGenSimple.java", title: "Générateur de clés sans solveur", desc: "La même chose en Java pur, pour comparer." },
          { file: "progParContraintes/src/ColorZone.java", title: "Zones réseau", desc: "Deux zones connectées n'ont jamais le même niveau : une coloration de graphe." },
          { file: "progParContraintes/src/ValidationFormulaire.java", title: "Validation de formulaire", desc: "Caractères autorisés dans un nom, âge entre 13 et 120." },
        ],
      },
      {
        title: "Optimisation",
        compact: true,
        items: [
          { file: "progParContraintes/src/OptimisationsDemos.java", title: "Premières optimisations", desc: "Minimiser une fonction objectif sous contraintes." },
          { file: "progParContraintes/src/Pareto.java", title: "Front de Pareto", desc: "Un sac à dos alimentaire avec deux objectifs : lipides et glucose." },
          { file: "progParContraintes/src/RetraitContraintes.java", title: "Contraintes souples", desc: "Chaque violation coûte une pénalité ; on minimise le total." },
          { file: "progParContraintes/src/RechargeVehicules.java", title: "Recharge de véhicules", desc: "Affecter des véhicules électriques aux bornes des stations." },
          { file: "progParContraintes/src/OptimisationCharge.java", title: "Recharge sur un réseau routier", desc: "Distances calculées par Floyd-Warshall, puis affectation optimale." },
          { file: "progParContraintes/src/OptimisationCharge2.java", title: "Recharge sur un réseau, version 2", desc: "Stations avec nombre de bornes et puissance, véhicules avec capacité." },
          { file: "progParContraintes/src/OptimisationCharge3.java", title: "Recharge sur un réseau, version 3", desc: "Variante du modèle précédent." },
        ],
      },
      {
        title: "Satisfiabilité (SAT)",
        compact: true,
        items: [
          { file: "solver/src/TestSat4J.java", title: "Premiers pas avec Sat4J", desc: "Donner des clauses au format DIMACS et récupérer un modèle." },
        ],
      },
    ],
  },

  /* ---------------------------------------------------------------- */
  {
    id: "jeux",
    title: "IA pour les jeux",
    tagline: "Minimax, alpha-bêta, MCTS",
    intro:
      "Des adversaires à battre, puis à améliorer. L'élagage alpha-bêta a besoin d'une bonne fonction d'évaluation ; MCTS s'en passe en simulant des parties au hasard.",
    groups: [
      {
        title: "Minimax et alpha-bêta",
        items: [
          {
            folder: "alphabetaTicTacToe/src",
            title: "Morpion en alpha-bêta",
            type: "exo",
            desc: "Minimax avec élagage alpha-bêta. L'évaluation des situations est imparfaite : l'IA peut être battue.",
            main: "appli/TicTacToeGame.java",
            readme: "alphabetaTicTacToe/src/Readme.md",
            todo: "Améliorer la méthode evaluate de Situation.java et les valeurs de SpecialSituations.java.",
            libs: ["Swing"],
          },
          {
            folder: "alphabetaConnect4/src",
            title: "Puissance 4 en alpha-bêta",
            type: "exo",
            desc: "Le même algorithme sur une grille 7×6, avec des motifs de danger pour évaluer les positions.",
            main: "appli/Connect4Game.java",
            readme: "alphabetaConnect4/src/Readme.md",
            todo: "Améliorer la méthode dangerValue de Situation.java et les motifs de DangerPattern.java.",
            libs: ["Swing"],
          },
        ],
      },
      {
        title: "Monte Carlo Tree Search",
        items: [
          {
            folder: "MCTS/OXO",
            title: "Morpion en MCTS",
            type: "exo",
            desc: "Quelques séries de 500 parties jouées seule suffisent à l'IA pour obtenir au pire un match nul.",
            main: "TicTacToeMCTS.java",
            files: ["MCTSNodeTTT.java"],
            readme: "MCTS/readme.md",
            todo: "Modifier le code pour que la personne joue au premier tour.",
          },
          {
            folder: "MCTS/P4",
            title: "Puissance 4 en MCTS",
            type: "exo",
            desc: "Ici MCTS se justifie : trop de combinaisons, et pas besoin d'écrire une heuristique.",
            main: "P4MCTS.java",
            files: ["P4MCTSNode.java"],
            readme: "MCTS/readme.md",
            todo: "Les diagonales ne sont pas prises en compte : les ajouter.",
          },
          {
            folder: "JeuxLudii",
            title: "Vos IA dans Ludii",
            type: "tp",
            desc: "Partir de l'UCT de D. Soemers, commenté, pour écrire deux IA testées dans l'application Ludii.",
            main: "TestLudii.java",
            files: ["UCTSoemers.java", "MyDummyAI.java"],
            readme: "JeuxLudii/readme.md",
            todo: "Une IA Progressive Bias pour Reversi, puis une IA Progressive Unpruning pour HexAmazons.",
            libs: ["Ludii"],
          },
        ],
      },
    ],
    extras: {
      title: "Rappel : la formule UCT",
      formula: "UCT(s, a) = Q(s, a) + C · √( ln N(s) / N(s, a) )",
      legend: [
        ["Q(s, a)", "récompense moyenne du coup a depuis l'état s (gains / visites)"],
        ["N(s)", "nombre de passages par l'état s"],
        ["N(s, a)", "nombre de fois où le coup a a été joué depuis s"],
        ["C", "équilibre entre exploration et exploitation"],
      ],
      variants:
        "Variantes vues dans le TP Ludii : Progressive Bias, Progressive Unpruning (PUP), RAVE, transpositions, Nested MCTS, Decoupled UCT, Double Progressive Widening.",
    },
  },

  /* ---------------------------------------------------------------- */
  {
    id: "clustering",
    title: "Clustering",
    tagline: "K-Means, DBSCAN",
    intro:
      "Regrouper des données sans étiquette. Des points colorés montrent l'algorithme au travail, puis on l'écrit soi-même sur des données réelles.",
    groups: [
      {
        title: "Démonstrations animées",
        items: [
          {
            folder: "Clustering/src/colorkmeans",
            title: "K-Means en couleurs",
            type: "demo",
            desc: "Des nuages de points se colorent au fil des itérations, à mesure que les centres se déplacent.",
            main: "AppliKMeansCouleurs.java",
            files: ["AlgoKMeans2D.java", "Cluster2D.java", "Data2D.java"],
            libs: ["JavaFX"],
          },
          {
            folder: "Clustering/src/colordbscan",
            title: "DBSCAN en couleurs",
            type: "demo",
            desc: "Les groupes s'étendent de voisin en voisin, sans fixer leur nombre à l'avance.",
            main: "AppliDBScaneCouleurs.java",
            files: ["AlgoDBScan2D.java", "Cluster2D.java", "Data2D.java"],
            libs: ["JavaFX"],
          },
        ],
      },
      {
        title: "TP",
        items: [
          {
            folder: "Clustering/src/testKMeans",
            title: "Écrire K-Means",
            type: "tp",
            desc: "K-Means avec initialisation K-Means++, sur des données générées puis sur un jeu de profils à 31 colonnes.",
            main: "AlgoKMeans.java",
            files: ["TP/KMeansJava.zip", "Data-Partens-EA-cl1.csv"],
            todo: "Partir du squelette KMeansJava.zip (Data, Cluster, AlgoKMeans).",
            solutions: [{ path: "Clustering/src/testKMeans/TP/soluce", title: "Corrigé" }],
          },
          {
            folder: "Clustering/src/testDBScan",
            title: "Écrire DBSCAN",
            type: "tp",
            desc: "Compléter l'algorithme puis visualiser les groupes trouvés dans la fenêtre JavaFX.",
            main: "TPAppliDBScanCouleurs.java",
            files: ["AlgoDBScan.java", "Cluster.java", "Data.java"],
            todo: "Compléter les TODO de Data, Cluster et AlgoDBScan.",
            libs: ["JavaFX"],
            solutions: [{ path: "Clustering/src/testDBScan/soluce", title: "Corrigé" }],
          },
        ],
      },
    ],
  },
];

/* Bibliothèques externes : affichées dans « Mise en route » */
window.LIBS = [
  { name: "Choco Solver", use: "Programmation par contraintes", how: "Maven : org.choco-solver:choco-solver", url: "https://choco-solver.org/" },
  { name: "Sat4J", use: "Satisfiabilité (SAT)", how: "Maven : org.ow2.sat4j:org.ow2.sat4j.core", url: "https://www.sat4j.org/" },
  { name: "JavaFX", use: "Démos de clustering", how: "Maven : org.openjfx:javafx-controls", url: "https://openjfx.io/" },
  { name: "Ludii", use: "TP IA pour les jeux", how: "Ludii.jar téléchargé sur ludii.games", url: "https://ludii.games/download.php" },
];
