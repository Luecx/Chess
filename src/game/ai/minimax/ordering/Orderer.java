package game.ai.minimax.ordering;

import board.Move;
import game.ai.minimax.tools.PVLine;

import java.util.List;

public interface Orderer {

    <T extends Move> void sort(List<T> collection, int depth, PVLine lastIteration);

}
