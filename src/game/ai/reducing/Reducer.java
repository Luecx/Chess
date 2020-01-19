package game.ai.reducing;

import board.moves.Move;

public interface Reducer {

    public int reduce(Move move, int depth, int depthLeft, int moveIndex, boolean pv_node);
}
