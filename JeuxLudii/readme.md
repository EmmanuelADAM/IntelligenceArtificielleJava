# IA pour les jeux avec Ludii

On utilisera ici la bibliothèque / application Ludii pour tester des algorithmes IA spécialisés dans les jeux.

Principalement, les IA développées seront celles issues de la branche MCTS des IA pour jeux.

Pour rappel, ces IA fonctionnent selon la méthode exploration + exploitation : 
  - exploration de situations par jeux aléatoires
  - exploitation des situations les plus probables de mener à une victoire.

Pour rappel, le principe général de MCTS est :
 - Utiliser un arbre pour représenter le plateau de jeu, la situation de base étant la racine de l'arbre. 
 - Explorer l'arbre en sélectionnant les mouvements qui ont été le moins visités. 
 - Utiliser une simulation pour estimer la valeur de chaque mouvement. 
 - Éventuellement, _biaiser_ la sélection en faveur de mouvements qui ont été joués plus fréquemment.

De là, plusieurs implémentations sont possibles : 
 - **UCB (Upper Confidence Bound)** :
   - Explorer l'arbre en sélectionnant les mouvements qui ont la confiance la plus élevée.
   - La confiance pour un coup $a$ à partir d'un état $s$ est calculée à l'aide de la formule suivante :
     - $UCB(s, a) = Q(s, a) + E(s, a)$ où :
       - $Q(s, a)$ est la qualité, la récompense moyenne pour le coup $a$ dans l'état $s$ (nb gains/ nb visites).
       - $E(s, a)$ est un coefficient d'exploration, en général basé sur le nombre de fois que le coup $a$ a été joué à partir de $s$
 - **UCT (Upper Confidence bounds applied to Trees)**:
   - Explorer l'arbre en sélectionnant les mouvements qui ont la confiance (valeur bornée par une limite supérieure) la plus élevée. 
   - La limite supérieure de confiance pour un coup $a$ à partir d'un état $s$ est calculée à l'aide de la formule suivante : 
     - $UCT(s,a) = Q(s, a) + C \times \sqrt{\frac{log(max(1, N(s)))}{N(s,a)}}$ où : 
       - $Q(s, a)$ est la qualité, la récompense moyenne pour le coup $a$ dans l'état $s$ (nb gains/ nb visites). 
       - $N(s)$ est le nombre de passages par l'état $s$.
       - $N(s, a)$ est le nombre de fois que le coup $a$ a été joué dans l'état $s$.
       - $C$ est une constante qui contrôle l'équilibre entre l'exploration et l'exploitation.
 
UCT est en quelque sorte une implémentation particulière de UCB.

Il existe de nombreuses variantes, entre autres :
  - **Progressive Bias** : pour l'algorithme UCB, au coefficient d'exploration est ajouté un biais basé sur une heuristique de jeu.
  - **Progressive Unpruning (PUP)** : Les enfants d'un nœud générés par *expand()* sont tous effacés sauf celui choisi tant que le nœud n'a pas été visité un certain nombre de fois.
  - **Rapid Action Value Estimation (RAVE)** : simplement utilise une sauvegarde des valuations des nœuds générées lors d'une précédente utilisation.
  - **Transpositions and Move Groups (TMG)** : mémoïsation de séquences d'actions ayant des résultats similaires pour éviter le recalcul.
  - **Nested Monte Carlo Tree Search (NMCTS)** : MCTS est lancé récursivement par *expand()* (un NMCTS de niveau 1 correspond à un MCTS classique).
  - **Decoupled Upper Confidence bounds (DUCT)** : MCTS est lancé pour chaque noeud du second niveau  *expand()*
  - **Double Progressive Widening (DPW)** : Le nombre de fils généré par *expand()* est limité et s'accroît progressivement avec la profondeur.

Vous pouvez les adapter pour créer le vôtre ; le choix de l'algorithme dépend du jeu.

---
## Travail à réaliser.

Ce développement est réalisé à l'aide de l'application [Ludii](https://ludii.games/).
  - Télécharger l'application Ludii : [ludii.games/download.php](https://ludii.games/download.php)
    - Vous pouvez également télécharger les sources pour joindre la documentation. 
  - reprenez les codes existant dans cette page et incluez-les dans un nouveau projet
  - ajoutez la librairie ludiixxxx.jar que vous avez téléchargée à votre projet.

Parmi les codes de cette page : 
  - [MyDummyAI](./MonAgentLudique.java) est une "IA" qui choisit ses actions au hasard
  - [UCTSoemers](./UCTSoemers.java) est un exemple d'IA utilisant l'algorithme UCT. Code proposé par D.Soemers, auquel des commentaires ont été ajoutés.<br>
  Il y a plusieurs façons de réaliser un UCT, ceci est une version qui est légèrement différente de celle classiquement présentée.
  - [TestLudii](./TestLudii.java) est la classe principale, elle lance l'application Ludii en ajoutant les IA ci-dessus.

**Une fois l'application lancée, pour sélectionner une IA** : 
  - cliquez sur la roue dentée en bas à droite de la fenêtre
  - pour le joueur de votre choix, choisissez votre IA (par ex. ici EAAI ou UCTSoemers)

**Il y a deux IA à développer :**
- Développez une _IA de type Progressive BIAS_, adaptée au jeu Reversi.
  - Donc la valeur des cases occupées a un impact dans le choix d'un nœud (dans coef d'exploration)
    (appelez là ProgBiasVotreNom)
    - petit guide, en se basant sur UCTSoemers, on remplace le coefficient d'exploration par une valeur basée sur la position des pièces.
      - `final double explore = Math.sqrt(twoParentLog / child.visitCount);` devient <br>
      `final double explore = getExplorationPreference(child);` où
        - `double getExplorationPreference(final Node node)` est une fonction à définir
          - `var states = node.context.state().containerStates()[0];` permet d'obtenir la liste des cases du jeu et leurs contenus
          - `states.stateCell(i)` retourne la valeur du pion (0 = pas de pion, 1 = pions joueur 1, 2 = pions joueur 2 )
          - les cases sont numérotées de 0 à 63. 
          - il suffit d'avoir un tableau d'entiers indiquant les valeurs des cases (500 pour les coins (0, 7, 56, 63) par exemple, -150 pour ... etc.)
          - on peut ajouter également le nb de pièces du joueur dans ce calcul

- Développez une _IA de type PUP_, et appliquez là à un jeu à large choix comme HexAmazons. 
  - Pour cela il vous faut modifier fortement l'algo UCT (ou programmer par vous-même un algo MCTS)
    - dans `Node select(final Node current)`
      - on écrit `while (!current.unexpandedMoves.isEmpty())`plutôt que `if`
      - on ne sort pas de cette boucle par return
      - on efface les nœuds fils non choisis, tant que le nœud père n'a pas été visité n fois (n>=5 par exemple)

