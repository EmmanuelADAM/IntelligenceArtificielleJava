# ChocoSolver

Quelques exercices de CSP en ChocoSolver.

---

*Voir le ficher [readmeChocoSolver](./readmeChocoSolver.md) pour plus d'informations sur la librairie chocoSolver.*

---

### N Reines
Résolution du puzzle des n reines avec ChocoSolver.

- Placer n reines sur un échiquier de taille n x n de telle sorte qu'aucune reine ne puisse en attaquer une autre.

**Particularités :**
- utilisation de contraintes arithmétiques, de allDifferents
- différentes stratégies de recherche sont proposées.

♛⬜⬛⬜⬛⬜⬛⬜ 
⬜⬛⬜⬛♛⬛⬜⬛
⬛⬜⬛⬜⬛⬜⬛♛
⬜⬛⬜⬛⬜♛⬜⬛
⬛⬜♛⬜⬛⬜⬛⬜
⬜⬛⬜⬛⬜⬛♛⬛
⬛♛⬛⬜⬛⬜⬛⬜
⬜⬛⬜♛⬜⬛⬜⬛

Le code MagicSquare en ChocoSolver est disponible [ici](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/src/NQueens.java) permet de resoudre ce type de puzzle.


---

### Carré magique
Résolution du puzzle carré magique avec ChocoSolver.

- Dans un carré magique d'ordre n, on place les entiers de 1 à n² de telle sorte que la somme des nombres dans chaque ligne, chaque colonne et les deux diagonales soit la même.
- Cette somme constante, appelée constante magique, peut être calculée avec la formule : $M = n(n^2 + 1) / 2$.

**Particularités :** 
  - recopie d'une partie de tableau, 
  - somme de valeurs contraintes.

Le code MagicSquare en ChocoSolver est disponible [ici](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/src/MagicSquare.java) permet de resoudre ce type de puzzle.


---

### TecToNic 
Résolution du puzzle TecToNic avec ChocoSolver .
- Le puzzle consiste à remplir une grille avec des chiffres en respectant des contraintes spécifiques liées aux régions colorées.
- Les contraintes sont les suivantes : 
  1. Certaines cellules ont des valeurs données qui doivent être respectées.7
  2. Chaque région colorée doit contenir tous les chiffres de 1 à N (N = taille de la région) exactement une fois.
  3. Deux cellules adjacentes (horizontalement ou verticalement) ne peuvent pas contenir la même valeur.

Voici un exemple de puzzle :  
![TecToNic Example](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/TecToNic.jpg)
issue du site [villemin.gerard.free.fr/aJeux](http://villemin.gerard.free.fr/aJeux/Tectonic.htm)

**Particularités** : utilisation de allDifferent, inégalités.

Le code TecToNic en ChocoSolver est disponible [ici](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/src/TecToNic.java) permet de resoudre ce type de puzzle.

---

### Problème classique du voyageur de commerce (TSP : Traveling Salesman Problem)
Un grand classique :  exercice simple sur le problème de voyageur de commerce : construire le chemin hamiltonien (chemin qui passe 1 fois et 1 seule par tous les sommets du graphe) de longueur minimale.

Particularité : 
 - utilisation de contraintes de type circuit.
 - accès à un élément d'une matrice à partir d'indices de type variables contraintes.
 - somme des éléments d'un tableau de variables contraintes.

Le code en ChocoSolver est disponible [ici](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/src/VRPChoco.java) permet de resoudre ce type de problème.

---

### Problème d'affectation de rôles  

Le contrôle d’accès basé sur les rôles (**RBAC**) est un modèle d'autorisation permettant de contrôler l'accès des utilisateurs aux systèmes, aux applications et aux données en fonction de leurs rôles.

Des utilisateurs jouent des rôles et ont besoin de permissions..

Prenons :
 - 4 utilisateurs $U={u_0,u_1,u_2,u_3}$
 - 3 rôles $R={r_0,r_1,r_2}$
 - 5 permissions $P={p_0,p_1,p_2,p_3,p_4}$

Un rôle donne accès à des permissions :
 - $r_0 \rightarrow \{p_0, p_1\}$
 - $r_1 \rightarrow \{p_1, p_2, p_3\}$
 - $r_2 \rightarrow \{p_3, p_4\}$

Chaque utilisateur a besoin d’un ensemble minimal de permissions pour ses tâches : 
 - $u_0 \rightarrow \{p_0, p_1\}$
 - $u_1 \rightarrow \{p_1, p_3\}$
 - $u_2 \rightarrow \{p_3, p_4\}$
 - $u_3 \rightarrow \{p_0, p_2\}$

On ajoute que personne ne peut posséder les rôles $r_0$ et $r_2$ en même temps (conflit de rôles).

Particularité :
 - utilisation de variables "booléennes" (en fait entiers entre 0 et 1).
 - utilisation d'une matrice de variables contraintes.
 - produit scalaire entre une ligne de la matrice et un vecteur.

Le code en ChocoSolver est disponible [ici](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/src/RBAC.java) permet de resoudre ce type de problème.

---

### Problème de la génération de clé

Générateur de clés sécurisées utilisant la programmation par contraintes avec ChocoSolver

- La clé générée respecte les contraintes suivantes :
  - Longueur fixe de 15 caractères
  - Au moins 2 majuscules, 2 minuscules, 2 chiffres et 2 caractères spéciaux
  - Pas de caractères répétitifs consécutifs
  - Pas de séquences ou motifs interdits

Particularité :
  - utilisation de recherche d'une variable contrainte dans un ensemble.
  - utilisation de ifThenElse pour modéliser des contraintes conditionnelles.
  - exemple d'utilisation de la reification 
  - recherche aléatoire de solutions.

Le code en ChocoSolver est disponible [ici](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/blob/master/progParContraintes/src/KeyGen.java) permet de resoudre ce type de problème.
