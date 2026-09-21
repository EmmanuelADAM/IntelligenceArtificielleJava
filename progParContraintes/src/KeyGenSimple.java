import java.util.*;

/** Générateur de clés sécurisées, version simple en Java pur (sans solveur de contraintes).
 * La clé générée respecte les contraintes suivantes :
 * - Longueur fixe de 15 caractères
 * - Au moins 2 majuscules, 2 minuscules, 2 chiffres et 2 caractères spéciaux
 * - Pas de caractères répétitifs consécutifs
 * - Pas de séquences ou motifs interdits
 *
 * Contrairement à KeyGen (qui modélise le problème avec ChocoSolver), cette version
 * construit directement une clé aléatoire conforme puis la valide, ce qui suffit
 * largement pour un problème de cette taille et évite la complexité d'un CSP.
 * */
public class KeyGenSimple {

    private static final int KEY_LENGTH = 15;
    private static final int MIN_PER_CATEGORY = 2;

    private static final char[] UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGITS = "0123456789".toCharArray();
    private static final char[] SPECIAL = "!@#$%^&*".toCharArray();
    private static final char[] ALL_CHARS = concat(UPPERCASE, LOWERCASE, DIGITS, SPECIAL);

    private static final String[] FORBIDDEN_SEQUENCES = {
            "abc", "bcd", "cde", "def", "efg", "fgh", "ghi", "hij", "ijk", "jkl", "klm", "lmn", "mno", "nop", "opq", "pqr", "qrs", "rst", "stu", "tuv", "uvw", "vwx", "wxy", "xyz",
            "012", "123", "234", "345", "456", "567", "678", "789",
            "210", "321", "432", "543", "654", "765", "876", "987",
            "qwerty", "azerty", "qwert", "asdfg", "zxcvb"
    };

    private static final String[] FORBIDDEN_PATTERNS = {"insa", "uphf", "icy"};

    private final Random random = new Random();

    /**
     * Génère une clé sécurisée : on part avec au moins 2 caractères de chaque catégorie,
     * on complète aléatoirement, on mélange, puis on régénère tant que la clé
     * ne respecte pas les contraintes de non-répétition et de motifs interdits.
     * */
    public String generateKey() {
        for (int attempt = 0; attempt < 100; attempt++) {
            String key = buildCandidateKey();
            if (isValid(key)) {
                return key;
            }
        }
        System.out.println("Impossible de générer une clé valide après 100 tentatives.");
        return null;
    }

    private String buildCandidateKey() {
        List<Character> chars = new ArrayList<>(KEY_LENGTH);
        addRandomChars(chars, UPPERCASE, MIN_PER_CATEGORY);
        addRandomChars(chars, LOWERCASE, MIN_PER_CATEGORY);
        addRandomChars(chars, DIGITS, MIN_PER_CATEGORY);
        addRandomChars(chars, SPECIAL, MIN_PER_CATEGORY);
        while (chars.size() < KEY_LENGTH) {
            chars.add(ALL_CHARS[random.nextInt(ALL_CHARS.length)]);
        }
        Collections.shuffle(chars, random);

        StringBuilder key = new StringBuilder(KEY_LENGTH);
        for (char c : chars) key.append(c);
        return key.toString();
    }

    private void addRandomChars(List<Character> target, char[] pool, int count) {
        for (int i = 0; i < count; i++) {
            target.add(pool[random.nextInt(pool.length)]);
        }
    }

    private boolean isValid(String key) {
        return !hasConsecutiveRepeats(key) && !containsForbiddenSequence(key) && !containsForbiddenPattern(key);
    }

    private boolean hasConsecutiveRepeats(String key) {
        for (int i = 0; i < key.length() - 1; i++) {
            if (key.charAt(i) == key.charAt(i + 1)) return true;
        }
        return false;
    }

    private boolean containsForbiddenSequence(String key) {
        String lowerKey = key.toLowerCase();
        for (String seq : FORBIDDEN_SEQUENCES) {
            if (lowerKey.contains(seq.toLowerCase())) return true;
        }
        return false;
    }

    private boolean containsForbiddenPattern(String key) {
        String lowerKey = key.toLowerCase();
        for (String pattern : FORBIDDEN_PATTERNS) {
            if (lowerKey.contains(pattern.toLowerCase())) return true;
        }
        return false;
    }

    private static char[] concat(char[]... arrays) {
        int total = 0;
        for (char[] a : arrays) total += a.length;
        char[] result = new char[total];
        int pos = 0;
        for (char[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    private void detailKey(String key) {
        int uppercase = 0, lowercase = 0, digits = 0, special = 0;
        for (char c : key.toCharArray()) {
            if (Character.isUpperCase(c)) uppercase++;
            else if (Character.isLowerCase(c)) lowercase++;
            else if (Character.isDigit(c)) digits++;
            else special++;
        }
        System.out.println("  [x] Longueur: " + key.length() + " caractères");
        System.out.println("  [x] Majuscules: " + uppercase);
        System.out.println("  [x] Minuscules: " + lowercase);
        System.out.println("  [x] Chiffres: " + digits);
        System.out.println("  [x] Spéciaux: " + special);
        System.out.println("  [x] Pas de répétitions consécutives");
        System.out.println("  [x] Pas de séquences interdites");
        System.out.println("  [x] Pas de motifs interdits");
    }

    public static void main(String[] args) {
        KeyGenSimple generator = new KeyGenSimple();

        System.out.println("=== Générateur de clés sécurisées (version simple) ===\n");
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
