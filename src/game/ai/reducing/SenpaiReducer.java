package game.ai.reducing;

import board.moves.Move;

public class SenpaiReducer implements Reducer {
    @Override
    public int reduce(Move move, int depth, int depthLeft, int moveIndex, boolean pv_node) {
        if (
                depth < 2
                        || move.getPieceFrom() == 0
                        || move.getPieceTo() != 0
                        || pv_node
        ) {
            return 0;
        }



        if(depth < 6) return 1;
        return (int)Math.sqrt(depthLeft-1);


//
//        return depthLeft / 2;
    }
}
