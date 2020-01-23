package game.ai.reducing;

import board.moves.Move;

public class SenpaiReducer implements Reducer {
    @Override
    public int reduce(Move move, int depth, int depthLeft, int moveIndex, boolean pv_node) {
        if (
                        moveIndex < 4
                        || move.getPieceTo() != 0
        ) {
            return 0;
        }



        if(depth > 4){
            return depthLeft/3;
        }else{
            return 1;
        }

//
//        return depthLeft / 2;
    }
}
