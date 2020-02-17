package ai.reducing;

import board.moves.Move;

public class SenpaiReducer implements Reducer {


    private int num_moves_not_reduced = 1;

    public SenpaiReducer() {
    }

    public SenpaiReducer(int num_moves_not_reduced) {
        this.num_moves_not_reduced = num_moves_not_reduced;
    }

    @Override
    public int reduce(Move move, int depth, int depthLeft, int moveIndex, boolean pv_node) {
        if (
                moveIndex < num_moves_not_reduced ||
                        pv_node
                        || move.getPieceTo() != 0
        ) {
            return 0;
        }



        if(depth > 3){
            return Math.max(2, depthLeft / 3);
        }else{
            return 1;
        }

//
//        return depthLeft / 2;
    }


    /**
     * This method gets the number of moves we don't reduce
     * @return the number of moves we don't reduce
     */
    public int getNum_moves_not_reduced() {
        return num_moves_not_reduced;
    }

    /**
     * This method sets the number of moves we don't reduce
     * @param num_moves_not_reduced      the number of moves we don't reduce
     */
    public void setNum_moves_not_reduced(int num_moves_not_reduced) {
        this.num_moves_not_reduced = num_moves_not_reduced;
    }
}
