
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.move.Move;

/**
 * A simple example implementation of a standard UCT approach.
 *
 * Only supports deterministic, alternating-move games.
 *
 * @author Dennis Soemers
 */
public class UCTSoemers extends AI
{

    //-------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;

    //-------------------------------------------------------------------------

    /**
     * Constructor
     */
    public UCTSoemers()
    {
        this.friendlyName = "UCTSoemers";
    }

    //-------------------------------------------------------------------------

    @Override
    /**for a giving game, in a specific context (state), select an action using the UCT (Upper Confidence bounds applied to Trees) algorithm
     * within a given time limit.
     * The reflexion can use a maximum of iterations and maximum depth.
     *
     * @param game the game being played
     * @param context the current context (state) of the game
     * @param maxSeconds the maximum allowable time for selecting an action
     * @param maxIterations the maximum number of iterations allowed for selecting an action
     * @param maxDepth the maximum depth of the MCTS tree to build
     * @return the best move to be made     * */
    public Move selectAction
            (
                    final Game game,
                    final Context context,
                    final double maxSeconds,
                    final int maxIterations,
                    final int maxDepth
            )
    {
        // Start out by creating a new root node (no tree reuse in this example)
        // it means that we do not seek if the node has already be explored
        final Node root = new Node(null, null, context);

        // We'll respect any limitations on max seconds and max iterations (d
        // if they are not set (-1), we use the maximum possible
        final long stopTime = (maxSeconds > 0.0) ? System.currentTimeMillis() + (long) (maxSeconds * 1000L) : Long.MAX_VALUE;
        final int maxIts = (maxIterations >= 0) ? maxIterations : Integer.MAX_VALUE;

        int numIterations = 0;

        // Our main loop through MCTS iterations
        while
        (
                numIterations < maxIts && 					// Respect iteration limit
                        System.currentTimeMillis() < stopTime && 	// Respect time limit
                        !wantsInterrupt								// Respect GUI user clicking the pause button
        )
        {
            // Start in root node
            Node current = root;

            // Traverse tree
            while (true)
            {
                //trial = a state of the game. over() is true if we've reached a terminal state
                if (current.context.trial().over()) break;

                // from the current node, we select a children by the UCT algorithm (mix of Quality and Exploration)
                current = select(current);

                // We've found a new node never expanded, time for playout!
                if (current.visitCount == 0) break;
            }

            Context contextEnd = current.context;

            // if the state is not terminal, Run a playout
            if (!contextEnd.trial().over())
            {
                contextEnd = new Context(contextEnd);
                /* general a full playout (until a terminal state is reached)
                     * @param context the current context (state) of the game
                     * @param ais the list of AI players involved in the playout
                     * @param thinkingTime the maximum time allowed for thinking during the playout
                     * @param playoutMoveSelector the move selector for biased actions during the playout
                     * @param maxNumBiasedActions the maximum number of biased actions allowed during the playout
                     * @param maxNumPlayoutActions the maximum number of actions allowed during the playout
                     * @param random the random number generator to be used during the playout
                    * @return the trial resulting from the playout
                    */
                game.playout
                        (
                                contextEnd,
                                null,
                                -1.0,
                                null,
                                0,
                                -1,
                                ThreadLocalRandom.current()
                        );
            }

            // we are in a final state, this computes utilities for all players at the of the playout,
            // which will all be values in [-1.0, 1.0]
            final double[] utilities = RankUtils.utilities(contextEnd);

            // Backpropagate utilities through the tree
            while (current != null)
            {
                current.visitCount += 1;
                for (int p = 1; p <= game.players().count(); ++p)
                {
                    current.scoreSums[p] += utilities[p];
                }
                current = current.parent;
            }

            // Increment iteration count
            ++numIterations;
        }

        // Return the move we wish to play
        return finalMoveSelection(root);
    }

    /**
     * Selects child of the given "current" node according to UCB1 equation.
     * N.B. This method also implements the "Expansion" phase of MCTS, and creates
     * a new node if the given current node has unexpanded moves.
     *
     * @param current current node
     * @return Selected node (if it has 0 visits, it will be a newly-expanded node).
     */
    public static Node select(final Node current)
    {
        // if there exist at least one child of this one that has not  been visited, choose one randomly
        if (!current.unexpandedMoves.isEmpty())
        {
            // randomly select an unexpanded move
            final Move move = current.unexpandedMoves.remove(
                    ThreadLocalRandom.current().nextInt(current.unexpandedMoves.size()));
            // create a copy of context
            final Context context = new Context(current.context);
            // apply the move
            context.game().apply(context, move);
            // create new node and return it
            return new Node(current, move, context);
        }

        // if nont, use UCB1 equation to select from all children, with random tie-breaking
        Node bestChild = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        final double twoParentLog = 2.0 * Math.log(Math.max(1, current.visitCount));
        int numBestFound = 0;

        final int numChildren = current.children.size();
        final int mover = current.context.state().mover();

        for (int i = 0; i < numChildren; ++i)
        {
            final Node child = current.children.get(i);
            final double exploit = child.scoreSums[mover] / child.visitCount;
            final double explore = Math.sqrt(twoParentLog / child.visitCount);

            final double ucb1Value = exploit + explore;

            // if we have a new winner
            if (ucb1Value > bestValue)
            {
                bestValue = ucb1Value;
                bestChild = child;
                numBestFound = 1;
            }
            else if //we have an ex-aequo, we decide with random tie-breaking
            (
                    ucb1Value == bestValue &&
                            ThreadLocalRandom.current().nextInt() % ++numBestFound == 0
                    //N.B. the random selection can be simplified
            )
            {
                bestChild = child;
            }
        }

        return bestChild;
    }

    /**
     * Selects the move we wish to play using the "Robust Child" strategy
     * (meaning that we play the move leading to the child of the root node
     * with the highest visit count).
     *
     * @param rootNode
     * @return
     */
    public static Move finalMoveSelection(final Node rootNode)
    {
        Node bestChild = null;
        int bestVisitCount = Integer.MIN_VALUE;
        int numBestFound = 0;

        final int numChildren = rootNode.children.size();

        for (int i = 0; i < numChildren; ++i)
        {
            final Node child = rootNode.children.get(i);
            final int visitCount = child.visitCount;

            // if we have a new winner
            if (visitCount > bestVisitCount)
            {
                bestVisitCount = visitCount;
                bestChild = child;
                numBestFound = 1;
            }
            else if //we have an ex-aequo, we decide with random tie-breaking
            (
                    visitCount == bestVisitCount &&
                            ThreadLocalRandom.current().nextInt() % ++numBestFound == 0
                //N.B. the random selection can be simplified
            )
            {
                // this case implements random tie-breaking
                bestChild = child;
            }
        }

        return bestChild.moveFromParent;
    }



    /**from the gui, init the id of the AI player
     * */
    @Override
    public void initAI(final Game game, final int playerID)
    {
        this.player = playerID;
    }

    /**just for the gui, tells that this AI doesn't fit with stochastic games and is for alternating moves*/
    @Override
    public boolean supportsGame(final Game game)
    {
        if (game.isStochasticGame())
            return false;

        if (!game.isAlternatingMoveGame())
            return true;

        return true;
    }

    //-------------------------------------------------------------------------

    /**
     * Inner class for nodes used by example UCT
     *
     * @author Dennis Soemers
     */
    private static class Node
    {
        /** Our parent node */
        private final Node parent;

        /** The move that led from parent to this node */
        private final Move moveFromParent;

        /** This objects contains the game state for this node (this is why we don't support stochastic games) */
        private final Context context;

        /** Visit count for this node */
        private int visitCount = 0;

        /** For every player, sum of utilities / scores backpropagated through this node */
        private final double[] scoreSums;

        /** Child nodes */
        private final List<Node> children = new ArrayList<Node>();

        /** List of moves for which we did not yet create a child node */
        private final FastArrayList<Move> unexpandedMoves;

        /**
         * Constructor
         *
         * @param parent
         * @param moveFromParent
         * @param context
         */
        public Node(final Node parent, final Move moveFromParent, final Context context)
        {
            this.parent = parent;
            this.moveFromParent = moveFromParent;
            this.context = context;
            final Game game = context.game();
            scoreSums = new double[game.players().count() + 1];

            // For simplicity, we just take ALL legal moves.
            // This means we do not support simultaneous-move games.
            unexpandedMoves = new FastArrayList<Move>(game.moves(context).moves());

            if (parent != null)
                parent.children.add(this);
        }

    }

    //-------------------------------------------------------------------------

}