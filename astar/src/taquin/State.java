package src.taquin;

import java.util.LinkedList;

/**a generic state for the A star algorithm
 * @author emmanueladam
 * */
public abstract class State implements Comparable<State>{
    /**estimated cost from the initial state to the final one, though this state*/
    int f;
    /**cost from the initial state*/
    int g;
    /**estimated cost to the final state*/
    int h;
    /**the state is a leaf if it is not possible to build other state from it*/
    boolean leaf;
    /**success*/
    boolean success;
    /**parent of the state*/
    State parent;

    /**evaluate the cost to the final state*/
    abstract int evaluate();
    /**return the states reachable from the current state*/
    public abstract LinkedList<State> nextStates();
    /**check and return is the state a success (= a leaf without conflict)*/
    public abstract boolean checkState();
    /** @return the cost from a direct parent p */
    public abstract int costBetween(State p);

    /** return the path from start to the node n*/
    LinkedList<State> rebuildPath()
    {
        var l = new LinkedList<State>();
        State s = this;
        while (s!=null)
        {
            l.addFirst(s);
            s = s.getParent();
        }
        return l;
    }

    /**update f = g+h*/
    public void updateF() {  f = g+h;}
    public int getF() { return f; }
    public int getG() {  return g; }
    public void setG(int g) { this.g = g; }

    public boolean isLeaf() { return leaf; }
    public boolean isSuccess() { return success;}

    public State getParent() { return parent; }
    public void setParent(State parent) { this.parent = parent; }

    /**compare the state basis on the f value*/
    @Override
    public int compareTo(State o) {
        return f-o.f;
    }
}
