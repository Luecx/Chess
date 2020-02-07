package ai.reducing;

import board.moves.Move;

public class SimpleReducer implements Reducer{

    private int late_move_reduction;
    private int depth_to_never_reduce;
    private int num_moves_not_reduced;

    public SimpleReducer() {
        this.late_move_reduction = 1;
        this.depth_to_never_reduce = 2;
        this.num_moves_not_reduced = 12;
    }

    public SimpleReducer(int late_move_reduction, int depth_to_never_reduce, int num_moves_not_reduced) {
        this.late_move_reduction = late_move_reduction;
        this.depth_to_never_reduce = depth_to_never_reduce;
        this.num_moves_not_reduced = num_moves_not_reduced;
    }

    @Override
    public int reduce(Move move, int depth, int depthLeft, int moveIndex,boolean pv_node) {
        if(
                move.getPieceTo() != 0 ||
                pv_node ||
                moveIndex < num_moves_not_reduced){
            return 0;
        }
        if(depth > 3){
            return late_move_reduction;
        }else{
            return late_move_reduction;
        }
    }

    /**
     * This method gets the number of plies we reduce by in late move reduction
     * @return the number of plies to reduce by
     */
    public int getLate_move_reduction() {
        return late_move_reduction;
    }

    /**
     * This method sets the number of plies we reduce by in late move reduction
     * @param late_move_reduction      the number of plies to reduce by
     */
    public void setLate_move_reduction(int late_move_reduction) {
        this.late_move_reduction = late_move_reduction;
    }


    /**
     * This method returns the amount of plies to never reduce by.
     * That is, the number of plies we will always calculate before any reductions
     * @return the number of plies to never reduce
     */
    public int getDepth_to_never_reduce() {
        return depth_to_never_reduce;
    }

    /**
     * This method sets the amount of plies to never reduce by.
     * That is, the number of plies we will always calculate before any reductions
     * @param depth_to_never_reduce      the number of plies to never reduce by
     */
    public void setdepth_to_never_reduce(int depth_to_never_reduce) {
        this.depth_to_never_reduce = depth_to_never_reduce;
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
