package ai.time_manager;

public interface TimeManager {

    /**
     * returns the amount of ms the next move should be searched.
     *
     * @param color         the color of the ai
     * @param wtime         the time in ms for white left on the clock
     * @param btime         the time in ms for black left on the clock
     * @param winc          the time increment in ms for white
     * @param binc          the time increment in ms for black
     * @param movesToGo     the expected amount of moves left
     * @return
     */
    int time(int color, int wtime, int btime, int winc, int binc, int movesToGo);

}
