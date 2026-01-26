## Recherche aléatoire et par voisinage

- nQueensRandom est un exemple de résolution du problèmes par adaptation locale, ici adapté au problème des n-reines.

n reines se placent  aléatoirement sur un échiquier, puis :<br>
-Tant qu'il subsiste des conflits
 - lister les reines les plus en conflit
 - en prendre une qui n'a pas bougé recemment
 - choisir pour elle une place diminuant au plus la somme des conflits

L'algorithme est efficace :  env. 1.5 secondes pour placer 1000 reines dans un echiquier de 1000x1000; 11ms pour placer 100 reines sur un echiquier de 100x100, *etc.*

Attention, il *peut ne pas être complet* si ce sont toujours les mêmes reines qui bougent.<br>
Ici ce problème est résolu  :
- en tirant aléatoirement parmi les reines ayant le même nombre de conflits, et parmi les solutions possibles pour une reine.
- en empêchant la reine ayant bougé au temps  de 're' bouger au temps t+1 même si c'est la reine avec le plus de conflits.

