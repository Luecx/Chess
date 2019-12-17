package game.ai.ordering;

import board.moves.Move;
import game.ai.tools.PVLine;

import java.util.Comparator;
import java.util.List;

public class NoahOrderer implements Orderer {
    @Override
    public <T extends Move> void sort(List<T> collection, int depth, PVLine lastIteration) {
        collection.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return 0;
            }
        });
    }
}
