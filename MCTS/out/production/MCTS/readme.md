# MCTS : MonteCarlo Tree Search

Cet espace présente 2 applications permettant à 1 joueur d'affronter une IA qui s'est entrainée à l'aide de l'algorithme MonteCarlo Tree Search.
cf. [enoncé TP jeu](http://emmanuel.adam.free.fr/site/spip.php?article201)

Dans les codes ci-dessous, l'IA a été optimisée par rapport à l'algo de base. C'est à dire qu'elle va supposer que si c'est au tour de l'humain de joueur et qu'il peut immédiatement gagner, alors l'humain jouera le coup gagnant pour lui, et donc le coup perdant pour l'IA. Egalement, si l'IA peut gagner au coup suivant, elle le fera.

- [OXO](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/MCTS/OXO) est le jeu du TicTacToe ou du morpion... Le but étant dans une grille de 3x3 d'aligner 3 de ses pions en posant à tour de rôle un de ses pions.
    Le combre de combinaisons est donc borné par 9! = 362880
    L'algorithme MCTS par quelques séries de 500 parties jouées seul permet de valuer les situations pour guider l'IA vers un jeu gagnant ou au pire nul contre une personne.<br>
    *Modifiez ce code pour que ce soit à la personne de jouer au 1er tour.* 

- [P4](https://github.com/EmmanuelADAM/IntelligenceArtificielleJava/tree/master/MCTS/P4) est le jeu de 'Puissance4' ou 'Aligne4'. 
Ici l'intérêt du MCTS au niveau de la complexité est justifié; également pour éviter de mettre en place des heuristiques nécessaires aux autres techniques (minimax, alphabeta)..
*Dans ce jeu, les diagonales ne sont pas prises en compte, à compléter..*
