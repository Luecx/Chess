package ai.decider;

import board.Board;

public class SimpleDecider implements BoardStateDecider{


    @Override
    public int getGameState(Board board) {
        return 0;
    }
}
