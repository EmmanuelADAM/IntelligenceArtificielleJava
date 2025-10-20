import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import java.util.*;

/** Générateur de clés sécurisées utilisant la programmation par contraintes avec ChocoSolver
 * La clé générée respecte les contraintes suivantes :
 * - Longueur fixe de 15 caractères
 * - Au moins 2 majuscules, 2 minuscules, 2 chiffres et 2 caractères spéciaux
 * - Pas de caractères répétitifs consécutifs
 * - Pas de séquences ou motifs interdits
 * */
public class KeyGen {

    private static final int KEY_LENGTH = 15;

    // Définition des ensembles de caractères
    private static final char[] UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGITS = "0123456789".toCharArray();
    private static final char[] SPECIAL = "!@#$%^&*".toCharArray();

    // Séquences interdites
    private static final String[] FORBIDDEN_SEQUENCES = {
            "abc", "bcd", "cde", "def", "efg", "fgh", "ghi", "hij", "ijk", "jkl", "klm", "lmn", "mno", "nop", "opq", "pqr", "qrs", "rst", "stu", "tuv", "uvw", "vwx", "wxy", "xyz",
            "ABC", "BCD", "CDE", "DEF", "EFG", "FGH", "GHI", "HIJ", "IJK", "JKL", "KLM", "LMN", "MNO", "NOP", "OPQ", "PQR", "QRS", "RST", "STU", "TUV", "UVW", "VWX", "WXY", "XYZ",
            "012", "123", "234", "345", "456", "567", "678", "789",
            "210", "321", "432", "543", "654", "765", "876", "987",
            "qwerty", "azerty", "qwert", "asdfg", "zxcvb"
    };

    // Motifs interdits
    private static final String[] FORBIDDEN_PATTERNS = {
            "insa", "uphf", "icy", "INSA", "UPHF", "ICY"
    };




    /**
     * * Génère une clé sécurisée en utilisant la programmation par contraintes
     * */
    public String generateKey() {
        Model model = new Model("SecureKeyGenerator");

        // Variables: chaque position de la clé
        IntVar[] keyPositions = new IntVar[KEY_LENGTH];

        // Domaine: donner les caractères ASCII possibles pour chaque position (33='!' à 126='~')
        for (int i = 0; i < KEY_LENGTH; i++) {
            keyPositions[i] = model.intVar("pos_" + i, 33, 126);
        }

        // Contrainte 1:  cibler le domaine aux caractères autorisés
        Set<Integer> allowedChars = new HashSet<>();
        for (char c : UPPERCASE) allowedChars.add((int) c);
        for (char c : LOWERCASE) allowedChars.add((int) c);
        for (char c : DIGITS) allowedChars.add((int) c);
        for (char c : SPECIAL) allowedChars.add((int) c);

        int[] carAllowed =  allowedChars.stream().mapToInt(Integer::intValue).toArray();

        for (int i = 0; i < KEY_LENGTH; i++) {
            model.member(keyPositions[i], carAllowed).post();
        }

        // Contrainte 2: Au moins 2 majuscules
        IntVar uppercaseCount = model.intVar("uppercase", 2, KEY_LENGTH);
        countCharacters(model, keyPositions, getCharCodes(UPPERCASE), uppercaseCount);

        // Contrainte 3: Au moins 2 minuscules
        IntVar lowercaseCount = model.intVar("lowercase", 2, KEY_LENGTH);
        countCharacters(model, keyPositions, getCharCodes(LOWERCASE), lowercaseCount);

        // Contrainte 4: Au moins 2 chiffres
        IntVar digitCount = model.intVar("digits", 2, KEY_LENGTH);
        countCharacters(model, keyPositions, getCharCodes(DIGITS), digitCount);

        // Contrainte 5: Au moins 2 caractères spéciaux
        IntVar specialCount = model.intVar("special", 2, KEY_LENGTH);
        countCharacters(model, keyPositions, getCharCodes(SPECIAL), specialCount);

        // Contrainte 6: Pas de caractères répétitifs consécutifs
        for (int i = 0; i < KEY_LENGTH - 1; i++) {
            model.arithm(keyPositions[i], "!=", keyPositions[i + 1]).post();
        }

        // Recherche de solution avec randomisation
        model.getSolver().setSearch(
                org.chocosolver.solver.search.strategy.Search.randomSearch(keyPositions, System.currentTimeMillis())
        );

        // Trouver une solution
        if (model.getSolver().solve()) {
            StringBuilder key = new StringBuilder();
            for (IntVar asciiVar : keyPositions)  key.append((char) asciiVar.getValue());
            String keyStr = key.toString();

            int nbAttempts = 0;
            // Vérifications post-génération
            while (nbAttempts<100 && (containsForbiddenSequence(keyStr) || containsForbiddenPattern(keyStr))) {
                // Essayer de trouver une autre solution
                if (model.getSolver().solve()) {
                    key = new StringBuilder();
                    for (IntVar asciiVar : keyPositions) key.append((char) asciiVar.getValue());
                    keyStr = key.toString();
                }
                nbAttempts++;
            }
            if (nbAttempts >= 100) {
                System.out.println("Impossible de générer une clé sans séquence interdite après 100 tentatives.");
                return null;
            }
            return keyStr;
        } else {
            System.out.println("Aucune solution trouvée.");
            return null;
        }
    }

    /**
     * * * Convertit un tableau de caractères en leurs codes ASCII correspondants
     * */
    private int[] getCharCodes(char[] chars) {
        int[] codes = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            codes[i] = (int) chars[i];
        }
        return codes;
    }

    /**
     * * * Compte le nombre de caractères dans keyPositions qui appartiennent à allowedValues
     * */
    private void countCharacters(Model model, IntVar[] keyPositions, int[] allowedValues, IntVar count) {
        // Créer des variables booléennes pour chaque position
        BoolVar[] isInSet = new BoolVar[KEY_LENGTH];
        for (int i = 0; i < KEY_LENGTH; i++) {
            isInSet[i] = model.boolVar("isInSet_" + i);
            // isInSet[i] <- 1 si keyPositions[i] est dans allowedValues, <- 0 sinon
            model.ifThenElse(model.member(keyPositions[i], allowedValues),
                    model.arithm(isInSet[i], "=", 1),
                    model.arithm(isInSet[i], "=", 0));
            //model.ifThenElse ... peut être aussi fait en 1 ligne avec member reify :
            //model.member(keyPositions[i], allowedValues).reifyWith(isInSet[i]);
        }
        // La somme des booléens doit être égale à count
        // ou count <- sum(isInSet)
        model.sum(isInSet, "=", count).post();
    }

    /**
     * * * Vérifie si la clé contient des séquences interdites
     * */
    private boolean containsForbiddenSequence(String key) {
        String lowerKey = key.toLowerCase();
        for (String seq : FORBIDDEN_SEQUENCES) {
            if (key.contains(seq) || lowerKey.contains(seq.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * * * Vérifie si la clé contient des motifs interdits
     * */
    private boolean containsForbiddenPattern(String key) {
        String lowerKey = key.toLowerCase();
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (lowerKey.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * * * affiche les caractéristiques de la clé générée
     * */
    private void detailKey(String key) {
        System.out.println("Validation:");

        int uppercase = 0, lowercase = 0, digits = 0, special = 0;
        for (char c : key.toCharArray()) {
            if (Character.isUpperCase(c)) uppercase++;
            else if (Character.isLowerCase(c)) lowercase++;
            else if (Character.isDigit(c)) digits++;
            else special++;
        }

        System.out.println("  ✓ Longueur: " + key.length() + " caractères");
        System.out.println("  ✓ Majuscules: " + uppercase);
        System.out.println("  ✓ Minuscules: " + lowercase);
        System.out.println("  ✓ Chiffres: " + digits);
        System.out.println("  ✓ Spéciaux: " + special);
        System.out.println("  ✓ Pas de répétitions consécutives");
        System.out.println("  ✓ Pas de séquences interdites");
        System.out.println("  ✓ Pas de motifs interdits");
    }


    /**
     * * Point d'entrée principal pour tester le générateur de clés
     * */
    public static void main(String[] args) {
        KeyGen generator = new KeyGen();

        // Générer plusieurs clés
        System.out.println("=== Générateur de clés sécurisées ===\n");
        for (int i = 0; i < 5; i++) {
            String key = generator.generateKey();
            if (key != null) {
                System.out.println("Clé " + (i + 1) + ": " + key);
                generator.detailKey(key);
                System.out.println();
            }
        }
    }

}