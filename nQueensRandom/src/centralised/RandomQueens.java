import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**solver for the pb of the n-queens.. on a n x n chessboard, n queens are placed.
 * a queen cannot be able to see another one on its row, column, or its diagonals .
 * The algorithm used place randomly the queens, then ask to the queens that are the most in conflict to move until a solution is found..
 * */
public class RandomQueens
{
   /**
    * columns of the queens that are sorted by row : queen#0 is 
    * on the row 0, and on the column queens[0]
    */
   int[] queens;
   /** nb of conflicts for each queen */
   int[] conflicts;
   /** queen that has moved previously */
   int movedQueen;
   /** nb of queens */
   int nb;
   /** queens with maximal conflicts */
   List<Integer> conflictsQueens;
   /** verbose level: 0=only the placement of the queens, 1 = show moves, 2 = show grid */
   int verboseLevel = 2;
   /** randomizer used to setup the initial grid and to pickup a queens */
   Random rand;
   /** times spent to found the solutions */
   ArrayList<Integer> times;
   /** nb of moves spent to found the solutions */
   ArrayList<Integer> moves;
   /** memory used to found the solutions */
   ArrayList<Long> mem;
   Runtime runtime;
   


   /**initialize the arrays*/
   RandomQueens(int _nb) {
      nb = _nb;
      queens = new int[nb];
      conflicts = new int[nb];
      movedQueen = -1;
      conflictsQueens = new ArrayList<>();
      times = new ArrayList<>();
      moves = new ArrayList<>();
      mem = new ArrayList<>();
      runtime = Runtime.getRuntime();
      }

   /** distribute the queens randomly */
   void randomize()
   {
      if (rand == null) rand = new Random();
      Arrays.setAll(queens, i -> rand.nextInt(nb));
   }

   /** compute the conflicts for all the queens */
   void computeConflicts()
   {
      Arrays.fill(conflicts, 0);
      for (int i = 0; i < nb; i++)
      {
         int col = queens[i];
         for (int j = i + 1; j < nb; j++)
         {
            if (col == queens[j] || (col - i) == (queens[j] - j) || (col + i) == (queens[j] + j))
            {
               conflicts[i]++;
               conflicts[j]++;
            }
         }
      }
   }

   /** compute the list of queens having the maximal conflicts */
   void maxConflictIndices()
   {
      conflictsQueens.clear();
      int max = max(conflicts);
      for (int i = 0; i < nb; i++)
      {
         if (conflicts[i] == max)
         {
            conflictsQueens.add(i);
         }
      }
   }

   /**@return the max of a tab*/
   int max(int[] tab)
   {
      int max = tab[0];
      for(int v:tab) if(v>max)max=v;
      return max;
   }

   /** move the queen with max conflicts to reduce the nb of conflicts */
   void resolveOneConflict()
   {
      //extract the queens with maximal conflicts
      maxConflictIndices();
      //choose a queen randomly
      int nbConflicts = conflictsQueens.size();
      int noQueens = rand.nextInt(nbConflicts);
      int queenWithMaxConflicts = conflictsQueens.get(noQueens);
      //if it is the same hat has previously moved, choose another one if possible
      if (queenWithMaxConflicts == movedQueen && nbConflicts > 1) 
         queenWithMaxConflicts = conflictsQueens.get((noQueens+1)%nbConflicts);
      movedQueen = queenWithMaxConflicts;
      //found a better solution for this queen
      int best = searchConflictMinimum(queenWithMaxConflicts);
      if (verboseLevel > 0) System.out.println(
            "queens(" + queenWithMaxConflicts + ") moves from " + queens[queenWithMaxConflicts] + " to " + best);
      queens[queenWithMaxConflicts] = best;
   }

   /** find a column where the queen will have the minimum of conflicts
    * @param queen no of the queen that have to move
    * @return a better column for the queen */
   int searchConflictMinimum(int queen)
   {
      int min = Integer.MAX_VALUE;
      int colQ = queens[queen];
      int minC = 0;
      int nbConflicts = 0;
      // try all the column
      for (int col = 0; col < nb; col++)
      {
         if (col == colQ) continue;
         nbConflicts = 0;
         // compute the eventual conflicts if the column i is chosen 
         for (int j = 0; j < nb; j++)
         {
            if (col != j && (col == queens[j] || (col - queen) == (queens[j] - j) || (col + queen) == (queens[j] + j)))
               nbConflicts++;
         }
         // if a better place is found, take it
         // or if it is a place with the same nb on conflicts, choose it, or no....
         if ((nbConflicts < min) || (min == nbConflicts && rand.nextBoolean()))
         {
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
   void solve()
   {
      computeConflicts();
      int sum = sumConflicts();
      int nbMoves = 0;
      long timeA = System.currentTimeMillis();
      // the algorithm can be stuck in a local minima, so the algo run
      // until the sum on conflicts is null, or until a nb of loops (nb x nb here)
      while (sum > 0 && nbMoves < nb * nb)
      {
         resolveOneConflict();
         computeConflicts();
         sum = sumConflicts();
         // a little cheat : to encourage other queen to move, 
         // we decrease artificially the nb on conflicts for the queen that have just moved 
         if (movedQueen != -1) conflicts[movedQueen]--;
         nbMoves++;
      }
      // some lines to compute the time spent, the memory used, 
      long timeB = System.currentTimeMillis();
      System.out.println("solution found in " + (timeB - timeA) + " ms for " + nb + " queens..");
      long memory = (runtime.totalMemory() - runtime.freeMemory())/1024L;
      mem.add(memory);
      System.out.println("memory used = " + memory + "ko");
      times.add((int) (timeB - timeA));
      // just before the print, we reallocate the nb of conflict artificially decreased
      if (movedQueen >= 0) conflicts[movedQueen]++;
      System.out.println("nb of conflicts=" + sumConflicts() + ",  nb of moves= " + nbMoves);
      moves.add(nbMoves);
      System.out.println("here is the solution : ");
      System.out.println(this);
   }

   /** return the sum of conflicts */
   int sumConflicts()
   {
      int sum = 0;
      for (int c : conflicts) sum += c;
      return sum;
   }

   /** return the chessboard, the pos of the queens and theirs conflicts */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      /**just to gain some time when printing*/
      String coma=",";

      if (verboseLevel > 1)
      {
         String x="[X]";
         String noOne = "[ ]";
         for (int i = 0; i < nb; i++) 
         {
            for (int j = 0; j < nb; j++) 
            {
               if (queens[i] == j)
                  sb.append(x);
               else
                  sb.append(noOne);
            }
            sb.append("\n");
         }
      }
      sb.append("rows of queen#0 -- queen#").append(nb-1).append(": ");
      for (int i = 0; i < nb; i++)
         sb.append(queens[i]).append(coma);
      sb.append("\n");
      sb.append("conflicts of queen#0 -- queen#").append(nb-1).append(": ");
      for (int i = 0; i < nb; i++)
         sb.append(conflicts[i]).append(coma);
      return sb.toString();
   }


   /**launch nb resolutions and measure the performances*/
   public static void main(String[] args)
   {
      int nb = 1000;
      RandomQueens rq = new RandomQueens(nb);
      rq.verboseLevel = 0;
      int nbTests = 3;
      for (int i = 0; i < nbTests; i++)
      {
         rq.randomize();
         rq.computeConflicts();
         System.out.println("######## test " + i + ", placement des reines au hasard");
         System.out.println(rq);
         rq.solve();
         System.out.println("___________________________________________________________");
         System.gc();
         try { Thread.sleep(100); } catch (InterruptedException e){ }
      }
      IntStream isTime = rq.times.stream().mapToInt(Integer::intValue);
      IntStream isMoves = rq.moves.stream().mapToInt(Integer::intValue);
      LongStream isMem = rq.mem.stream().mapToLong(Long::longValue);
      System.out.println("pour " + nb + " reines, sur " + nbTests + " tests,");
      System.out.println("----  le tps moyen de résolution est " + isTime.average().getAsDouble() + " ms");
      System.out.println("--------  le tps max de résolution est " + Collections.max(rq.times) + " ms");
      System.out.println("--------  le tps min de résolution est " + Collections.min(rq.times) + " ms");
      System.out.println("----  le nb moyen de déplacements est " + isMoves.average().getAsDouble());
      System.out.println("--------  le nb max de déplacements est de " + Collections.max(rq.moves));
      System.out.println("--------  le nb min de déplacements est de " + Collections.min(rq.moves));
      System.out.println("----  la mémoire utilisée est en moyenne de " + isMem.average().getAsDouble() + " Mo");
   }

}
