import java.util.function.BiFunction;

/**
 * enumeration qui represente des opérations sur les entiers.
 * chaque operation est associe à son symbole, sa fonction de calcul et sa
 * fonction de test de faisabilite
 * 
 * @author emmanuel adam
 */
public enum OperateurFunc {
   ADDITION("+", (a,b) -> (a + b), (a,b) -> true),
   SOUSTRACTION("-", (a,b) -> (a - b), (a,b) -> (a > b)),
   MULTIPLICATION("*", (a,b) -> (a * b), (a,b) -> (a > 1 && b > 1)),
   DIVISION("/", (a,b) -> (a / b), (a,b) -> (b > 1 && a % b == 0));

   /** chaine representant le signe de l'operation */
   final String sign;

   /** opération artihmétique sur 2 entiers, fournissant un résultat entier */
   final BiFunction<Integer, Integer, Integer> calcul;
   /** test de validite d'une opération artithémtique sur 2 entiers a et b */
   final BiFunction<Integer, Integer, Boolean> test;

   OperateurFunc(String _sign, BiFunction<Integer, Integer, Integer> _calcul, BiFunction<Integer, Integer, Boolean> _test) {
      sign = _sign;
      calcul = _calcul;
      test = _test;
   }

   @Override
   public String toString() { return sign;}

}
