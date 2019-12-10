package game.ai.minimax.ordering;

import board.Move;
import game.ai.minimax.tools.PVLine;

import java.util.List;

public class NoOrderer implements Orderer {
    @Override
    public <T extends Move> void sort(List<T> collection,int depth, PVLine lastIteration) {

    }
}
