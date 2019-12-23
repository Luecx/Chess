package game.ai.reducing;

import board.moves.Move;

public class SimpleReducer implements Reducer{

    @Override
    public int reduce(Move move, int depth, boolean pv_node) {
        if(move.getPieceTo() != 0 || pv_node){
            return 0;
        }
        return 1;
    }
}
