package game.ai.minimax.evaluator;

import board.Board;

public class SimpleEvaluator implements Evaluator {
    @Override
    public double evaluate(Board board) {
        double score = 0;
        for(int i = 0; i < 8; i++){
            for(int n = 0; n < 8; n++){
                int p = board.getPiece((byte)i,(byte)n);
                if(p == 0) continue;
                byte sign = (byte) (p / Math.abs(p));
                switch (Math.abs(p)){
                    case 1: score += sign * 1; break;
                    case 2: score += sign * 4; break;
                    case 3: score += sign * 3; break;
                    case 4: score += sign * 3.25; break;
                    case 5: score += sign * 7; break;
                    case 6: score += sign * 1000; break;
                }
            }
        }
        return score;
    }
}
