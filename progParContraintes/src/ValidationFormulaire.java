import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

public class ValidationFormulaire {
    public static void main(String[] args) {
        Model model = new Model("Validation de formulaire");

        int MAX_NOM = 20;
        IntVar[] nomChars = model.intVarArray("nomChars", MAX_NOM, 32, 126); // ASCII visibles

        for (IntVar c : nomChars) {
            // 1️⃣ Lettres majuscules : [A-Z] = 65–90
            BoolVar isUpper = model.boolVar();
            model.and(c.ge(65).decompose(), c.le(90).decompose()).reifyWith(isUpper);

            // 2️⃣ Lettres minuscules : [a-z] = 97–122
            BoolVar isLower = model.boolVar();
            model.and(c.ge(97).decompose(), c.le(122).decompose()).reifyWith(isLower);

            // 3️⃣ Chiffres : [0–9] = 48–57
            BoolVar isDigit = model.boolVar();
            model.and(c.ge(48).decompose(), c.le(57).decompose()).reifyWith(isDigit);

            // 4️⃣ '_' (95) ou '-' (45)
            BoolVar isAllowedSymbol = model.boolVar();
            model.or(c.eq(95).decompose(), c.eq(45).decompose()).reifyWith(isAllowedSymbol);

            // 5️⃣ Un caractère est valide s’il satisfait au moins une des conditions
            model.addClausesBoolOrArrayEqualTrue(new BoolVar[]{
                    isUpper, isLower, isDigit, isAllowedSymbol
            });
        }

        // Exemple de contrainte numérique : âge entre 13 et 120
        IntVar age = model.intVar("age", 13, 120);

        Solver solver = model.getSolver();
        if (solver.solve()) {
            System.out.println("Formulaire valide !");
            System.out.println("Âge : " + age.getValue());
        } else {
            System.out.println("Formulaire invalide !");
        }
    }
}
