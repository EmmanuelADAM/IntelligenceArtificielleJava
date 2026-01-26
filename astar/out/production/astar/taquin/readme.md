# A Star : recherche informée

L'algortihme A-Star est un algortihme de recherche de solution "informée", c'est-à-dire qu'il se base sur une heuristique pour guider sa recherche, au contraire des algorithmes de recherche aveugles(profondeur, largeur) qui eux n'exploitent pas les connaissances sur le problème à résoudre.

La classe AlgoAStar contient l'algorithme A-Star.
La classe State est un état générique à étendre. Un état possède les variables f,g,h, ainsi que des fonctions de comparaison, etc.

Un exemple d'utilisation est donné par la classe StateTaquin pour la résolution du jeu du Taquin qui consiste à faire coulisser une pièce vide afin de réordonner des pièces.
Ainsi par exemple, l'objectif est d'atteindre le plus rapidement
<pre>
A B C D
E F G H
I J K L
M N O _
</pre>

à partir de 
<pre>
_ A C D
E B K G
N F J H
I M O L
</pre>

Exemple d'exécution : 
<pre>
solution trouvee en 15 ms

ABICD
FLGJE
KHMN_
PRXWO
UQVTS
------
ABICD
FLGJE
KHMNO
PRXW_
UQVTS
------
ABICD
FLGJE
KHMNO
PRXWS
UQVT_
------
ABICD
FLGJE
KHMNO
PRXWS
UQV_T
------
ABICD
FLGJE
KHMNO
PRX_S
UQVWT
------
ABICD
FLGJE
KHMNO
PR_XS
UQVWT
------
ABICD
FLGJE
KH_NO
PRMXS
UQVWT
------
ABICD
FLGJE
K_HNO
PRMXS
UQVWT
------
ABICD
F_GJE
KLHNO
PRMXS
UQVWT
------
ABICD
FG_JE
KLHNO
PRMXS
UQVWT
------
AB_CD
FGIJE
KLHNO
PRMXS
UQVWT
------
ABC_D
FGIJE
KLHNO
PRMXS
UQVWT
------
ABCD_
FGIJE
KLHNO
PRMXS
UQVWT
------
ABCDE
FGIJ_
KLHNO
PRMXS
UQVWT
------
ABCDE
FGI_J
KLHNO
PRMXS
UQVWT
------
ABCDE
FG_IJ
KLHNO
PRMXS
UQVWT
------
ABCDE
FGHIJ
KL_NO
PRMXS
UQVWT
------
ABCDE
FGHIJ
KLMNO
PR_XS
UQVWT
------
ABCDE
FGHIJ
KLMNO
P_RXS
UQVWT
------
ABCDE
FGHIJ
KLMNO
PQRXS
U_VWT
------
ABCDE
FGHIJ
KLMNO
PQRXS
UV_WT
------
ABCDE
FGHIJ
KLMNO
PQRXS
UVW_T
------
ABCDE
FGHIJ
KLMNO
PQR_S
UVWXT
------
ABCDE
FGHIJ
KLMNO
PQRS_
UVWXT
------
ABCDE
FGHIJ
KLMNO
PQRST
UVWX_
------
</pre>
