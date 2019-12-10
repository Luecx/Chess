package game.ai.minimax;

import board.Board;
import board.Move;
import game.ai.AI;
import game.ai.minimax.evaluator.Evaluator;

import java.util.LinkedList;

public class MiniMax implements AI {

    private Evaluator evaluator;

    private int depth;

    public MiniMax(Evaluator evaluator, int depth) {
        this.evaluator = evaluator;
        this.depth = depth;
    }

    private Move _bestMove;
    private Board _board;

    private int _evaluatedNodes;
    private int _visitedNodes;

    @Override
    public Move bestMove(Board board) {
        _bestMove = null;
        _board = board;

        _evaluatedNodes = 0;
        _visitedNodes = 0;

        minimax(board.getActivePlayer(), depth);
        System.out.println("evaluated nodes: " + _evaluatedNodes + "    visited nodes: " + _visitedNodes);
        return _bestMove;
    }

    private double minimax(int spieler, int tiefe){
        LinkedList<Move> moves = (LinkedList<Move>) _board.getAvailableMovesShallow();
        _visitedNodes ++;
        if (tiefe == 0 || moves.size() == 0 || _board.isGameOver()){
            _evaluatedNodes++;
            return evaluator.evaluate(_board) * spieler;
        }

        double max = -1000000000;
        for(Move m:moves){
            _board.move(m);
            double wert = -minimax(-spieler, tiefe - 1);
            _board.undoMove();

            if (wert > max) {
                max = wert;
                if (tiefe == depth)
                    _bestMove = m;
            }
        }
        return max;
    }

}
