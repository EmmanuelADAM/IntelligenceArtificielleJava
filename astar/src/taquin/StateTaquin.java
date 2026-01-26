package src.taquin;


import java.util.LinkedList;

/**a class that represent a "taquin" game
 * the objective is to move an empty square to reorder the blocs. i.e to reach
 * ABCD
 * EFGH
 * IJKL
 * MNO_ (_ is the empty bloc)
 * @author emmanueladam 
 * */
public class StateTaquin extends State{
    /**representation of the objective in a string*/
    static final String objective= "ABCDEFGHIJKLMNO_";
    /**dim of the square*/
    static final int dim = 4;
    /**representation of the state in a string*/
    String sequence;

    public StateTaquin(String sequence) {
        this.sequence = sequence;  }

    /**here, the heuristic is the sum of the manathan distances
     * between each letter and its final position
     * */
    @Override
    int evaluate() {
        h=0;
        for(int i=0; i<StateTaquin.dim*StateTaquin.dim; i++)
        {
            int lo = i/StateTaquin.dim;
            int co = i - lo*StateTaquin.dim;
            int pos = sequence.indexOf(StateTaquin.objective.charAt(i));
            int lc = pos/StateTaquin.dim;
            int cc = pos - lc*StateTaquin.dim;
            h += Math.abs(lo-lc) + Math.abs(co-cc);
        }
        return h;
    }

    /**
     * @return the liste of states reachable by moving the empty square (_)
     * */
    @Override
    public LinkedList<State> nextStates() {
        var neighbouring = new LinkedList<State>();
        int pos =sequence.indexOf("_");
        if ((pos+1)%StateTaquin.dim!=0)
        {
            byte[] tab = sequence.getBytes();
            byte b = tab[pos];
            tab[pos] = tab[pos+1];
            tab[pos+1] = b;
            String s = new String(tab);
            neighbouring.add(new StateTaquin(s));
        }
        if (pos%StateTaquin.dim  != 0)
        {
            byte[] tab = sequence.getBytes();
            byte b = tab[pos];
            tab[pos] = tab[pos-1];
            tab[pos-1] = b;
            String s = new String(tab);
            neighbouring.add(new StateTaquin(s));
        }
        if ((pos+StateTaquin.dim)<StateTaquin.dim*StateTaquin.dim)
        {
            byte[] tab = sequence.getBytes();
            byte b = tab[pos];
            tab[pos] = tab[pos+StateTaquin.dim];
            tab[pos+StateTaquin.dim] = b;
            String s = new String(tab);
            neighbouring.add(new StateTaquin(s));
        }
        if ((pos-StateTaquin.dim)>=0)
        {
            byte[] tab = sequence.getBytes();
            byte b = tab[pos];
            tab[pos] = tab[pos-StateTaquin.dim];
            tab[pos-StateTaquin.dim] = b;
            String s = new String(tab);
            neighbouring.add(new StateTaquin(s));
        }
        return neighbouring;
    }

    @Override
    public boolean checkState() {
        success = sequence.equals(StateTaquin.objective);
        return success;
    }

    /**cost between a parent and one of its 'child' = 1*/
    @Override
    public int costBetween(State p) { return 1; }

    /**two states are equals if they represent the same sequence*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateTaquin that = (StateTaquin) o;
        return sequence != null ? sequence.equals(that.sequence) : that.sequence == null;
    }

    @Override
    public String toString() {
        String s = "";
        for(int i=0; i<StateTaquin.dim*StateTaquin.dim; i+=StateTaquin.dim)
            s = s + sequence.substring(i, i+StateTaquin.dim) + "\n";
        return "StateTaquin{" +
                "sequence=\n" + s  +
                ", f=" + f +
                ", g=" + g +
                ", h=" + h +
                ", success=" + success +
                '}';
    }





    /**launch a resolution a display the solution*/
    public static void main(String[] arg)
    {
        String s = "EACDNBKGF_JHIMOL";
        var l = AlgoAStar.algoASTAR(new StateTaquin(s));
        l.forEach(System.out::println);
    }



}
