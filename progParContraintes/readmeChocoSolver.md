# ChocoSolver

ChocoSolver est une bibliothèque Java  open-source dédiée à la résolution de problèmes de satisfaction de contraintes (CSP) et d'optimisation (COP).
Elle est utilisée dans les domaines d'applications de CSP et COP tels que la planification, l'ordonnancement, la configuration de produits,... 
#### Documentation et ressources
- [Site officiel de ChocoSolver](https://choco-solver.org/)

## Fonctionnalités principales
- **Choix de variables flexible** : ChocoSolver supporte différents types de variables, booléennes, entières, réelles, des ensembles, des graphes.
- **Large gamme de contraintes** : De ce fait, la bibliothèque offre une vaste collection de contraintes prédéfinies, telles que les contraintes arithmétiques, les contraintes logiques, les contraintes de table, et les contraintes globales.
- **Algorithmes de résolution avancés** : ChocoSolver intègre plusieurs algorithmes de résolution, y compris les techniques de backtracking, les heuristiques de recherche (first fail, ....), et les méthodes de propagation de contraintes.
- **Extensibilité** : Il est possible de définir ses propagateurs de contraintes, ses stratégies de recherche.
- **Support pour l'optimisation** : ChocoSolver permet également de résoudre des problèmes d'optimisation en minimisant ou maximisant une fonction objectif.

-----
## Exemple classique des N-reines

Exemple repris du tutoriel de ChocoSolver sur le problème des N reines (sur un échiquier de NxN, placer N reines de telle manière à ce qu'aucune reine ne puisse en attaquer une autre).

Cet exemple illustre différents types de contraintes arithmétiques.


```Java
int n = 8;
Model model = new Model(n + "-queens problem");
IntVar[] vars = new IntVar[n];
for(int q = 0; q < n; q++){
    vars[q] = model.intVar("Q_"+q, 1, n);
}
for(int i  = 0; i < n-1; i++){
    for(int j = i + 1; j < n; j++){
        model.arithm(vars[i], "!=",vars[j]).post();
        model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
        model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
    }
}
Solution solution = model.getSolver().findSolution();
if(solution != null){
    System.out.println(solution.toString());
}
```
1. créer un modèle de problème avec `Model model = new Model(n + "-queens problem");`

2. créer un tableau de variables entières 'contrainte' `IntVar[] vars = new IntVar[n];` représentant les positions des reines sur l'échiquier. <br>
Chaque variable contrainte peut prendre une valeur entre 1 et n (les colonnes de l'échiquier) `vars[q] = model.intVar("Q_"+q, 1, n);`.
3. On ajoute des contraintes pour s'assurer que deux reines ne peuvent pas se trouver sur la même ligne, colonne ou diagonale. Cela est fait en utilisant des contraintes arithmétiques.
4. Enfin, on utilise le solveur pour trouver une solution au problème 
5. et l'afficher si elle existe.

Ici, aucune stratégie de recherche n'est spécifiée, donc le solveur utilisera sa stratégie par défaut.

### Optimisation
Dans le tutoriel, plusieurs améliorations sont proposées.

**Déclaration de variables** : 
- ```IntVar[] vars = model.intVarArray("Q", n, 1, n, true);```
  - `true` demande à ce les variables soient énumérées et crées de suite
  - `false` (par défaut) demande à ce que les variables soient créées au fur et à mesure du besoin
  - on placera donc `true` lorsque le nombre de variables est faible et `false` dans le cas contraire.

**Déclaration de contraintes** :
- on remplace la déclaration des contraintes par celle-ci plus lisible :
`vars[i].ne(vars[j]).post();`<br>
`vars[i].ne(vars[j].sub(j - i)).post();`<br>
`vars[i].ne(vars[j].add(j - i)).post();`
  - `ne` est une abréviation de "not equal" (différent de)
- on peut aussi utiliser des contraintes extensionnelles, qui définissent les tables de valeurs autorisées, pour arc-b-consistance. <br>
A réserver pour des problèmes simples donc :<br>
  `vars[i].ne(vars[j]).extension().post();`<br>
  `vars[i].ne(vars[j].sub(j - i)).extension().post();`<br>
  `vars[i].ne(vars[j].add(j - i)).extension().post();`


---

## Paramètrage du solveur

Par défaut, le solveur de Choco utilise :
- comme sélecteur devariables : InputOrder = Parcourt les variables dans l'ordre où elles apparaissent dans le modèle.
- comme sélecteur de valeurs : IntDomainMin = Sélectionne la plus petite valeur dans le domaine de la variable.

Le solveur peut être paramétré de différentes façons.
**Stratégies de recherche** :<br>
Par défaut, le solveur utilise une stratégie de recherche par défaut. 
Cependant, il est possible de définir des stratégies personnalisées pour influencer l'ordre dans lequel les variables sont sélectionnées et le mode de sélection des valeurs à leur assigner.
Voici quelques exemples de stratégies de recherche que l'on peut utiliser via la classe Search :
```Java
Solver s = model.getSolver();
s.setSearch(Search.intVarSearch(
// selects the variable of smallest domain size
new FirstFail(model),
// selects the smallest domain value (lower bound)
new IntDomainMin(),
// apply equality (var = val)
DecisionOperatorFactory.makeIntEq(),
// variables to branch on
vars
```
- plus d'une 20e de stratégies de sélections de variables sont définies ici : [chocosolver/solver/search/strategy/selectors](https://javadoc.io/doc/org.choco-solver/choco-solver/latest/org.chocosolver.solver/org/chocosolver/solver/search/strategy/selectors/variables/package-summary.html) dont
- `FirstFail` : sélectionne la variable avec le plus petit domaine
- `AntiFirstFail` : sélectionne la variable avec le plus large domaine
- `Cyclic` : sélectionne les variables selon un ordre lexicographique
- `InputOrder` : sélectionne les variables dans l'ordre de déclaration
- `Random` : sélectionne une variable au hasard
- `Smallest` : sélectionne la variable avec la plus petite valeur dans son domaine...
- ...  

- une 20e de stratégies de sélections de valeurs sont définies ici : [chocosolver/solver/search/strategy/selectors/values](https://javadoc.io/doc/org.choco-solver/choco-solver/latest/org.chocosolver.solver/org/chocosolver/solver/search/strategy/selectors/values/package-summary.html) dont : 
- `IntDomainMin` : sélectionne la plus petite valeur du domaine
- `IntDomainMax` : sélectionne la plus grande valeur du domaine
- `IntDomainMiddle` : sélectionne la valeur médiane du domaine
- `IntDomainRandom` : sélectionne une valeur au hasard dans le domaine
- `IntDomainBest` : sélectionne la valeur qui minimise une fonction objectif
- `IntDomainWorst` : sélectionne la valeur qui maximise une fonction objectif
- ...

---
## Carré magique
Un autre exemple très classique : le carré magique ..

Dans un carré magique de $n x n$, on place les entiers de 1 à n^2 de telle manière que la somme des entiers de chaque ligne, chaque colonne et chaque diagonale soit égale à la même valeur, appelée constante magique.
Cette constante peut être calculée avec la formule : $M = n(n^2 + 1) / 2$.

Prenez le code de carré magique et tester différentes stratégies de sélection de variables et de valeurs sur des carrés de tailles différentes.

Encore trop long ? Forcez l'affectation d'une variable (par  exemple la première) pour réduire l'espace de recherche.

