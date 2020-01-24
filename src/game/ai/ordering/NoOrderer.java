package game.ai.ordering;

import board.Board;
import board.moves.Move;
import game.ai.tools.KillerTable;
import game.ai.tools.PVLine;
import game.ai.tools.TranspositionTable;

import java.util.List;

public class NoOrderer implements Orderer {


    @Override
    public void sort(List<Move> collection, int depth, PVLine lastIteration, Board board, boolean pvNode,  KillerTable killerTable,
                                      TranspositionTable transpositionTable) {
    }
}
