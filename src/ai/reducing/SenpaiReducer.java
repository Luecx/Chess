package ai.reducing;

import board.Board;
import board.moves.Move;

public class SenpaiReducer implements Reducer {


    private int num_moves_not_reduced = 1;
    private int higher_reduction_depth = 4;
    private int division_factor = 4;

    public SenpaiReducer() {
    }

    public SenpaiReducer(int num_moves_not_reduced) {
        this.num_moves_not_reduced = num_moves_not_reduced;
    }

    @Override
    public int reduce(Board board, Move move, int depth, int depthLeft, int moveIndex, boolean pv_node) {
        if (
                moveIndex < num_moves_not_reduced ||
                pv_node ||
                board.givesCheck(move) ||
                move.getPieceTo() != 0
        ) {
            return 0;
        }



        if(depth > higher_reduction_depth){
            return Math.max(2, depthLeft / division_factor);
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

    /**
     * returns the depth-1 at which reductions > 1 can occur
     * @return
     */
    public int getHigher_reduction_depth() {
        return higher_reduction_depth;
    }

    /**
     * sets the depth-1 at which reductions > 1 can occur
     * @param higher_reduction_depth
     */
    public void setHigher_reduction_depth(int higher_reduction_depth) {
        this.higher_reduction_depth = higher_reduction_depth;
    }

    /**
     * at depth > higher_reduction_depth it will be reduced by max(2,ply/division_factor)
     *
     * @return
     */
    public int getDivision_factor() {
        return division_factor;
    }

    /**
     * at depth > higher_reduction_depth it will be reduced by max(2,ply/division_factor)
     *
     * @return
     */
    public void setDivision_factor(int division_factor) {
        this.division_factor = division_factor;
    }
}
