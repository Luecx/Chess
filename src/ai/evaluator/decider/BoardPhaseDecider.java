package ai.evaluator.decider;

import board.Board;

public interface BoardPhaseDecider {


    public static final int OPENING = -1;
    public static final int MIDGAME = 0;
    public static final int ENDGAME = 1;

    /**
     * decides which phase of the game we are in.
     * It should return a value between 0 and 1 where 0 would mean that we are the opening with midgame in mind
     * and 1 would be a lategame with lategame in mind.
     * @param board
     * @return
     */
    double getGamePhase(Board board);

}
