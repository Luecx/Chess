package ai.decider;

import board.Board;

public interface BoardStateDecider {


    public static final int MIDGAME = 1;
    public static final int OPENING = 2;
    public static final int ENDGAME = 3;

    /**
     * decides about the state of the board
     * @param board
     * @return
     */
    int getGameState(Board board);

}