import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import utils.AIUtils;

import java.util.concurrent.ThreadLocalRandom;

public class MyDummyAI extends AI {
    //-------------------------------------------------------------------------

    /** Our player index */
    protected int player = -1;

    //-------------------------------------------------------------------------

    /**
     * Constructor
     */
    public MyDummyAI()
    {
        this.friendlyName = "random moves";
    }

    /**
     * Selects a random move from the list of legal moves.
     * @return The selected move.
     */
    @Override
    public Move selectAction(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth) {
        // Get all legal moves for the current context
        FastArrayList<Move> legalMoves = game.moves(context).moves();

        // If we're playing a simultaneous-move game, some of the legal moves may be
        // different according to the players. Extract only the ones that we can choose.
        if (!game.isAlternatingMoveGame())
            legalMoves = AIUtils.extractMovesForMover(legalMoves, player);

        // Select and return a random move from the legal moves
        final int r = ThreadLocalRandom.current().nextInt(legalMoves.size());
        return legalMoves.get(r);
    }

    /**
     * Initializes the AI for a specific player in the game.
     * @param game The game being played.
     * @param playerID The ID of the player this AI will control.
     */
    @Override
    public void initAI(final Game game, final int playerID)
    {
        this.player = playerID;
    }
}
