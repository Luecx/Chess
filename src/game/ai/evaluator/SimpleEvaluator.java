package game.ai.evaluator;

import board.Board;

public class SimpleEvaluator implements Evaluator {

    double[] values = new double[]{-1000,-7,-3.25,-3,-4,-1,0,1,4,3,3.25,7,1000};

    @Override
    public double evaluate(Board board) {
        double score = 0;
        for(int i = 0; i < 8; i++){
            for(int n = 0; n < 8; n++){
                int p = board.getPiece((byte)i,(byte)n);
                if(p == 0) continue;
                score += values[p+6];
            }
        }
        return score;
    }
}
