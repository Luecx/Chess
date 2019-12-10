package game.ai.minimax.evaluator;

import board.Board;

public interface Evaluator {

    public double evaluate(Board board);

}
