package game.ai.ordering;

import board.Move;
import game.ai.tools.PVLine;

import java.util.List;

public class NoOrderer implements Orderer {
    @Override
    public <T extends Move> void sort(List<T> collection,int depth, PVLine lastIteration) {

    }
}
