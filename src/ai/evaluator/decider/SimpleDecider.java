package ai.decider;

import ai.evaluator.Evaluator;
import ai.evaluator.LateGameEvaluator;
import ai.evaluator.MidGameEvaluator;
import board.Board;

public class SimpleDecider implements BoardStateDecider{

    private MidGameEvaluator midGameEvaluator;
    private LateGameEvaluator lateGameEvaluator;

    public SimpleDecider(MidGameEvaluator midGameEvaluator, LateGameEvaluator lateGameEvaluator) {
        this.midGameEvaluator = midGameEvaluator;
        this.lateGameEvaluator = lateGameEvaluator;
    }

    @Override
    public int getGameState(Board board) {
        int wP = 0,bP = 0;
        int[] p = new int[]{1,5,3,3,9,0};
        for(int i = 0; i < 64; i++){
            if(board.getPiece(i) > 0){
                wP += p[board.getPiece(i)-1];
            }else if(board.getPiece(i) < 0){
                bP += p[-board.getPiece(i)-1];
            }
        }
        if(wP <= 13 && bP <= 13){
            return BoardStateDecider.ENDGAME;
        }

        return BoardStateDecider.MIDGAME;
    }

    @Override
    public Evaluator getEvaluator(Board board) {
        if(getGameState(board) == BoardStateDecider.ENDGAME) return lateGameEvaluator;
        return midGameEvaluator;
    }
}
