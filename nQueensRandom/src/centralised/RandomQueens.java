import java.util.*;
import java.util.stream.IntStream;
/**
 * solver for the pb of the n-queens.. on a n x n chessboard, n queens are placed.
 * a queen cannot be able to see another one on its row, column, or its diagonals .
 * The algorithm used place randomly the queens, then ask to the queens that are the most in conflict to move until a solution is found..
 */
class RandomQueens {
    volatile static int nombre=0;
    int no;
    /**
     * columns of the queens that are sorted by row : queen#0 is
     * on the row 0, and on the column queens[0]
     */
    private final int[]  queens;
    /**
     * nb of conflicts for each queen
     */
    private final int[] conflicts;
    /**
     * queen that has moved previously
     */
    private int movedQueen;
    /**
     * nb of queens
     */
    static int nb;
    /**
     * queens with maximal conflicts
     */
    private final  List<Integer> conflictsQueens;
    /**
     * verbose level: 0=only the placement of the queens, 1 = show moves, 2 = show grid
     */
    static int verboseLevel = 2;
    /**
     * randomizer used to setup the initial grid and to pickup a queens
     */
    private Random rand;
    /**
     * times spent to found the solutions
     */
    static  List<Integer> times;
    /**
     * nb of moves spent to found the solutions
     */
    static List<Integer> moves;

    static Runtime runtime;


    /**
     * initialize the arrays
     */
    private RandomQueens(int _nb) {
        no = RandomQueens.nombre;
        RandomQueens.nombre = nb+1;
        nb = _nb;
        queens = new int[nb];
        conflicts = new int[nb];
        movedQueen = -1;
        conflictsQueens = new ArrayList<>();
        times = new ArrayList<>();
        moves = new ArrayList<>();
        runtime = Runtime.getRuntime();
    }

    void go()
    {
        randomize();
        computeConflicts();
        String name = Thread.currentThread().getName();
        System.out.println(name + " ######## test   placement des reines au hasard");
        System.out.println(name +" "+ this);
        solve();
        System.out.println(name + " ___________________________________________________________");

    }





    /**
     * distribute the queens randomly
     */
    private  void randomize() {
        if (rand == null) rand = new Random();
        Arrays.setAll(queens, i -> rand.nextInt(nb));
    }


    /**
     * compute the conflicts for all the queens
     */
    private  void computeConflicts() {
        Arrays.fill(conflicts, 0);
        for (int i = 0; i < nb; i++) {
            int col = queens[i];
            for (int j = i + 1; j < nb; j++) {
                if (col == queens[j] || (col - i) == (queens[j] - j) || (col + i) == (queens[j] + j)) {
                    conflicts[i]++;
                    conflicts[j]++;
                }
            }
        }
    }

    /**
     * compute the list of queens having the maximal conflicts
     */
    private void maxConflictIndices() {
        conflictsQueens.clear();
        int max = max(conflicts);
        for (int i = 0; i < nb; i++) {
            if (conflicts[i] == max) {
                conflictsQueens.add(i);
            }
        }
    }

    /**
     * @return the max of a tab
     */
    private int max(int[] tab) {
        int max = tab[0];
        for (int v : tab) if (v > max) max = v;
        return max;
    }

    /**
     * move the queen with max conflicts to reduce the nb of conflicts
     */
    private void resolveOneConflict() {
        //extract the queens with maximal conflicts
        maxConflictIndices();
        //choose a queen randomly
        int nbConflicts = conflictsQueens.size();
        int noQueens = rand.nextInt(nbConflicts);
        int queenWithMaxConflicts = conflictsQueens.get(noQueens);
        //if it is the same hat has previously moved, choose another one if possible
        if (queenWithMaxConflicts == movedQueen && nbConflicts > 1)
            queenWithMaxConflicts = conflictsQueens.get((noQueens + 1) % nbConflicts);
        movedQueen = queenWithMaxConflicts;
        //found a better solution for this queen
        int best = searchConflictMinimum(queenWithMaxConflicts);
        if (verboseLevel > 0) System.out.println(Thread.currentThread().getName()  +
                "queens(" + queenWithMaxConflicts + ") moves from " + queens[queenWithMaxConflicts] + " to " + best);
        queens[queenWithMaxConflicts] = best;
    }

    /**
     * find a column where the queen will have the minimum of conflicts
     *
     * @param queen no of the queen that have to move
     * @return a better column for the queen
     */
    private int searchConflictMinimum(int queen) {
        int min = Integer.MAX_VALUE;
        int colQ = queens[queen];
        int minC = 0;
        int nbConflicts;
        // try all the column
        for (int col = 0; col < nb; col++) {
            if (col == colQ) continue;
            nbConflicts = 0;
            // compute the eventual conflicts if the column i is chosen
            for (int j = 0; j < nb; j++) {
                if (col != j && (col == queens[j] || (col - queen) == (queens[j] - j) || (col + queen) == (queens[j] + j)))
                    nbConflicts++;
            }
            // if a better place is found, take it
            // or if it is a place with the same nb on conflicts, choose it, or no....
            if ((nbConflicts < min) || (min == nbConflicts && rand.nextBoolean())) {
                min = nbConflicts;
                minC = col;
            }
        }
        return minC;
    }

    /**
     * launch a cycle resolveOneConflict()-computeConflicts() until the solution is
     * found or until a threshold
     */
    private void solve() {
        if (rand == null) rand = new Random();
        computeConflicts();
        int sum = sumConflicts();
        int nbMoves = 0;
        long timeA = System.currentTimeMillis();
        // the algorithm can be stuck in a local minima, so the algo run
        // until the sum on conflicts is null, or until a nb of loops (nb x nb here)
        while (sum > 0 && nbMoves < nb * (Math.log(nb)+1)) {
            resolveOneConflict();
            computeConflicts();
            sum = sumConflicts();
            // a little cheat : to encourage other queen to move,
            // we decrease artificially the nb on conflicts for the queen that have just moved
            if (movedQueen != -1) conflicts[movedQueen]--;
            nbMoves++;
            Thread.yield();
        }
        // some lines to compute the time spent, the memory used,
        long timeB = System.currentTimeMillis();
        if (sum==0) System.out.println(Thread.currentThread().getName()+" solution found in " + (timeB - timeA) + " ms for " + nb + " queens..");
        else System.out.println(Thread.currentThread().getName()+" no solution after  " + (nbMoves) + " moves..");
        times.add((int) (timeB - timeA));
        // just before the print, we reallocate the nb of conflict artificially decreased
        if (movedQueen >= 0) conflicts[movedQueen]++;
        System.out.println(Thread.currentThread().getName()+" nb of conflicts=" + sumConflicts() + ",  nb of moves= " + nbMoves);
        moves.add(nbMoves);
        System.out.println(Thread.currentThread().getName()+" here is the current state : ");
        System.out.println(Thread.currentThread().getName()+" " +this);
    }

    /**
     * return the sum of conflicts
     */
    private int sumConflicts() {
        int sum = 0;
        for (int c : conflicts) sum += c;
        return sum;
    }

    /**
     * return the chessboard, the pos of the queens and theirs conflicts
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("q"+no+":");
        /*just to gain some time when printing*/
        String coma = ",";

        if (verboseLevel > 1) {
            String x = "[X]";
            String noOne = "[ ]";
            for (int i = 0; i < nb; i++) {
                for (int j = 0; j < nb; j++) {
                    if (queens[i] == j)
                        sb.append(x);
                    else
                        sb.append(noOne);
                }
                sb.append("\n");
            }
        }
        sb.append("rows of queen#0 -- queen#").append(nb - 1).append(": ");
        for (int i = 0; i < nb; i++)
            sb.append(queens[i]).append(coma);
        sb.append("\n");
        sb.append(Thread.currentThread().getName()).append(" conflicts of queen#0 -- queen#").append(nb - 1).append(": ");
        for (int i = 0; i < nb; i++)
            sb.append(conflicts[i]).append(coma);
        return sb.toString();
    }


    /**
     * launch nb resolutions and measure the performances
     */
    public static void main(String[] args) {
        RandomQueens.nb = 1000;
        RandomQueens.verboseLevel = 0;
        int nbTests = 5;
        var cheffes = new ThreadGroup("course");
        for (int i = 0; i < nbTests; i++) {
            RandomQueens rq = new RandomQueens(nb);
            Thread queenProcess = new Thread(cheffes, rq::go, "test-"+i);
            queenProcess.start();
        }
        while(cheffes.activeCount()!=0) Thread.yield();
        IntStream isTime = RandomQueens.times.stream().mapToInt(Integer::intValue);
        IntStream isMoves = RandomQueens.moves.stream().mapToInt(Integer::intValue);
        System.out.println("pour " + nb + " reines, sur " + nbTests + " tests,");
        System.out.println("----  le tps moyen de resolution est " + isTime.average().getAsDouble() + " ms");
        System.out.println("--------  le tps max de resolution est " + Collections.max(RandomQueens.times) + " ms");
        System.out.println("--------  le tps min de resolution est " + Collections.min(RandomQueens.times) + " ms");
        System.out.println("----  le nb moyen de deplacements est " + isMoves.average().getAsDouble());
        System.out.println("--------  le nb max de deplacements est de " + Collections.max(RandomQueens.moves));
        System.out.println("--------  le nb min de deplacements est de " + Collections.min(RandomQueens.moves));
    }
}
