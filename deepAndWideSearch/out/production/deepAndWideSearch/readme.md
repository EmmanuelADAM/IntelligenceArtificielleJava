# Recherche en Largeur / Profondeur

- La classe *DeepAndBreadthSearch* présente un algorithme de recherche en largeur / profondeur basé sur la notion de noeuds libres et noeuds clos.<br>
Aucun backtracking sous forme de reccurrence, mais une création d'états à partir de l'état courant, qui une fois traité passe en noeuds clos.<br>
Les nouveaux états sont ajoutés en tête des noeuds libres si la recherche s'effectue en profondeur, ou en fin de liste si la recherche s'effecetue en largeur.<br>
Avant l'ajout, on véfirie qu'ils ne sont pas déjà présents en noeuds libres ou clos pour éviter de boucles.

- La classe *State* est une classe abstraite, à instancier pour résoudre votre problème.

- La classe *StateQueen* présente un exemple d'utilisation pour la résolution du pb des N-reines : placer n reines dans un échiquier nxn sans prise possible : 

Exemple avec 4 reines : 
<pre>
|_|_|X|_|
|X|_|_|_|
|_|_|_|X|
|_|X|_|_|
</pre>
Exemple avec 10 reines : 
<pre>
|_|_|_|_|_|_|_|_|_|X|
|_|_|_|_|_|_|_|X|_|_|
|_|_|_|_|X|_|_|_|_|_|
|_|_|X|_|_|_|_|_|_|_|
|X|_|_|_|_|_|_|_|_|_|
|_|_|_|_|_|X|_|_|_|_|
|_|X|_|_|_|_|_|_|_|_|
|_|_|_|_|_|_|_|_|X|_|
|_|_|_|_|_|_|X|_|_|_|
|_|_|_|X|_|_|_|_|_|_|
</pre>

Vous remarquerez que si la solution existe, elle est toujours trouvée.
Ceci s'appelle la **complétude**.<br>
Cependant, cela peut prendre du temps... <br>
Par exemple pour 10 reines, le temps de recherche est de 15ms pour une recherche en profondeur, 10 secondes pour une recherche en largeur. 
Ceci s'explique que la solution ici se trouve en fin d'arbre, lorsque la dernière reine a été posée..<br>
Mais si on teste 23 reines par exmple, même le parcour en profondeur est long, très long...

Un autre type de recherche, par voisinage est beaucoup plus intéressant; voir la [recherche aléatoire](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava).<br>
Avec ce type de recherche, le placement de 1000 reines peut être réalisé en 1.5 ms !



