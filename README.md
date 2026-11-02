# Intelligence artificielle en Java

**Site du cours : https://emmanueladam.github.io/IntelligenceArtificielleJava/**

Des algorithmes classiques d'IA en Java, à lire, lancer et modifier. Chaque dossier est un petit projet indépendant à ouvrir dans IntelliJ IDEA, Eclipse ou VS Code (JDK 17 ou plus récent).

| Thème | Contenu |
|---|---|
| [Recherche dans un espace d'états](https://emmanueladam.github.io/IntelligenceArtificielleJava/#recherche) | Largeur / profondeur, A* sur le taquin, A* et Theta* sur une grille |
| [Recherche locale et évolution](https://emmanueladam.github.io/IntelligenceArtificielleJava/#locale) | Le compte est bon, N-reines par réparation locale, algorithmes génétiques |
| [Programmation par contraintes](https://emmanueladam.github.io/IntelligenceArtificielleJava/#contraintes) | Choco Solver : puzzles, planification, sécurité, optimisation ; Sat4J |
| [IA pour les jeux](https://emmanueladam.github.io/IntelligenceArtificielleJava/#jeux) | Minimax et alpha-bêta, MCTS, TP Ludii |
| [Clustering](https://emmanueladam.github.io/IntelligenceArtificielleJava/#clustering) | K-Means et DBSCAN, démos JavaFX et TP |

## Tester en ligne, sans rien installer

[![Ouvrir dans GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/EmmanuelADAM/IntelligenceArtificielleJava?quickstart=1)

Le Codespace s'ouvre avec Java 21 et les bibliothèques du cours. Dans le terminal :

```
lancer progParContraintes/src/NReines.java
```

Les programmes avec fenêtre (Swing, JavaFX, Ludii) s'affichent sur le bureau virtuel : onglet **Ports**, ligne **6080**, icône globe, mot de passe `vscode`.

## Bibliothèques

| Bibliothèque | Pour | Ajout |
|---|---|---|
| [Choco Solver](https://choco-solver.org/) | Programmation par contraintes | Maven `org.choco-solver:choco-solver` |
| [Sat4J](https://www.sat4j.org/) | SAT | Maven `org.ow2.sat4j:org.ow2.sat4j.core` |
| [JavaFX](https://openjfx.io/) | Démos de clustering | Maven `org.openjfx:javafx-controls` |
| [Ludii](https://ludii.games/download.php) | TP IA pour les jeux | `Ludii.jar` téléchargé sur ludii.games |

## Mettre à jour le site

Le site est une seule page (`index.html`) qui lit la liste des programmes dans `assets/catalogue.js`.
Pour ajouter un exemple, copier une entrée de `catalogue.js` dans le bon thème et l'adapter. Les liens GitHub sont générés automatiquement.

---
Supports pédagogiques d'Emmanuel Adam, fournis tels quels pour un usage éducatif. Merci de citer la source en cas de réutilisation.
