package ai.search;

import board.Board;
import board.FastBoard;
import board.moves.Move;
import board.moves.MoveListBuffer;
import board.setup.Setup;
import ai.evaluator.Evaluator;
import ai.tools.TranspositionTable;

import java.util.List;

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

    private TranspositionTable _hashed_positions = new TranspositionTable<int[]>((int)50E6);

    @Override
    public Move bestMove(Board board) {
        _bestMove = null;
        _board = board;

        _evaluatedNodes = 0;
        _visitedNodes = 0;

        minimax(depth);
        System.out.println("evaluated nodes: " + _evaluatedNodes + "    visited nodes: " + _visitedNodes);
        return _bestMove;
    }

    MoveListBuffer moveListBuffer = new MoveListBuffer(20);

    private double minimax(int tiefe){
        List<Move> moves = _board.getPseudoLegalMoves(moveListBuffer.get(tiefe));
        _visitedNodes ++;

//        long zobrist = _board.zobrist();
//        int[] field = ((SlowBoard)_board).getField();
//        if(_hashed_positions.get(zobrist) != null){
//            if(Arrays.equals((int[])_hashed_positions.get(zobrist), field) == false){
//                System.out.println("Hash collision");
//            }
//        }else{
//            _hashed_positions.put(zobrist, field);
//        }

        if (tiefe == 0  || moves == null){
            _evaluatedNodes++;
            return evaluator.evaluate(_board) * _board.getActivePlayer();
        }

        double max = -1000000000;
        for(Move m:moves){
            _board.move(m);
            double wert = -minimax(tiefe - 1);
            _board.undoMove();

//            if(tiefe == depth){
//                System.out.println(moves.indexOf(m) +1+ " / " + moves.size());
//            }

            if (wert > max) {
                max = wert;
                if (tiefe == depth)
                    _bestMove = m;
            }
        }
        return max;
    }

    public static void main(String[] args) {
        MiniMax m = new MiniMax(new Evaluator() {
            @Override
            public double evaluate(Board board) {
                return 0;
            }
        }, 5);
        FastBoard board = new FastBoard(Setup.DEFAULT);
        long t = System.currentTimeMillis();
        System.out.println(m.bestMove(board));
        System.out.println(System.currentTimeMillis()-t);
    }

}
