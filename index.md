# IntelligenceArtificielleJava

Quelques algos en Java issus de l'Intelligence Artificielle

## Recherche aléatoire 

- [compteEstBonSimplifié](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/compteEstBonSimplifie/src) est un exemple de recherche aléatoire pour la solution au problème du "compte est bon" : https://fr.wikipedia.org/wiki/Des_chiffres_et_des_lettres#Le_Compte_est_bon

- [nQueensRandom](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/nQueensRandom/src/centralised) est un exemple de résolution du problèmes des n-reines par placement aléatoire puis adaptation pour diminuer les conflits. temps de traitement : env. 1.5 secondes pour placer 1000 reines dans un echiquier de 1000x1000; 11ms pour placer 100 reines sur un echiquier de 100x100, et 0.6ms pour placer 8 reines.... https://fr.wikipedia.org/wiki/Problème_des_huit_dames

- [MCTS](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/MCTS) : MonteCarlo Tree Search : 2 applications permettant à 1 joueur d'affronter une IA qui s'est entrainée à l'aide de l'algorithme MonteCarlo Tree Search.
  - TicTacToe est donc le jeu du morpion du du OXO.. L'IA par MonteCarlo n'a pas été optimisée, vous pouvez gagner.
  - P4 est le jeu de 'Puissance4' ou 'Aligne4'. L'IA a été ici optimisée.. 

## Jeux (alphabeta, minimax)

- [jeux](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/jeux)  contient des classes java permettant à 2 joueurs de s'affronter sur le jeu du Carré, et du Surakarta. A vous de définir une IA..
