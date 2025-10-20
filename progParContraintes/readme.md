# ChocoSolver

Quelques exercices de CSP en ChocoSolver.

---

*Voir le ficher [readmeChocoSolver (../readmeChocoSolver.md) pour plus d'informations sur la librairie chocoSolver.*

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

Le code TecToNic en ChocoSolver est disponible [ici](./src/main/java/progParContraintes/tectonic/TecToNic.java) permet de resoudre ce type de puzzle.
