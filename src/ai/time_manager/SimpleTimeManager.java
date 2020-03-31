    package ai.time_manager;

    import ai.search.AdvancedSearch;
    import ai.search.PVSearchFast;

    import java.util.ArrayList;

    public class SimpleTimeManager implements TimeManager {
        @Override
        public int time(int color, int wtime, int btime, int winc, int binc, int movesToGo) {
            int time = color == 1 ? wtime:btime;
            int inc  = color == 1 ? winc :binc;

            return time / 30 + inc;
        }
    }
