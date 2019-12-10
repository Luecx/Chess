package game.ai.minimax;

import board.Board;
import board.Move;
import game.ai.AI;
import game.ai.minimax.evaluator.Evaluator;
import game.ai.minimax.ordering.Orderer;
import game.ai.minimax.tools.PVLine;
import game.ai.minimax.tools.TranspositionTable;

import java.util.*;

public class AlphaBeta implements AI {

    private Evaluator evaluator;
    private Orderer orderer;

    private int max_depth;
    private int quiesce_depth;
    private boolean use_iteration = true;
    private boolean use_transposition = false;

    public AlphaBeta(Evaluator evaluator, Orderer orderer, int max_depth, int quiesce_depth) {
        this.evaluator = evaluator;
        this.max_depth = max_depth;
        this.orderer = orderer;
        this.quiesce_depth = quiesce_depth;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Orderer getOrderer() {
        return orderer;
    }

    public void setOrderer(Orderer orderer) {
        this.orderer = orderer;
    }

    public int getMax_depth() {
        return max_depth;
    }

    public void setMax_depth(int max_depth) {
        this.max_depth = max_depth;
    }

    public int getQuiesce_depth() {
        return quiesce_depth;
    }

    public void setQuiesce_depth(int quiesce_depth) {
        this.quiesce_depth = quiesce_depth;
    }

    public boolean isUse_iteration() {
        return use_iteration;
    }

    public void setUse_iteration(boolean use_iteration) {
        this.use_iteration = use_iteration;
    }

    public boolean isUse_transposition() {
        return use_transposition;
    }

    public void setUse_transposition(boolean use_transposition) {
        this.use_transposition = use_transposition;
    }

    private int _depth;
    private int _quiesceNodes;
    private int _visitedNodes;
    private int _evaluatedNodes;
    private Board _board;
    private Move _bestMove;

    private TranspositionTable<TranspositionTable.TranspositionEntry> _transpositionTable;

    @Override
    public Move bestMove(Board board) {
        _board = board;
        long time = System.currentTimeMillis();
        if (use_iteration) {
            PVLine line = null;
            for (int i = 1; i <= max_depth; i++) {
                line = iteration(i, line);
            }
        } else {
            iteration(max_depth, null);
        }
        System.out.println("required time: " + (System.currentTimeMillis() - time) + " ms");
        return _bestMove;
    }

    public PVLine iteration(int depth, PVLine lastIteration) {
        this._depth = depth;
        _bestMove = null;
        _evaluatedNodes = 0;
        _visitedNodes = 0;
        _transpositionTable = new TranspositionTable<>((int) (50E6));
        PVLine pline = new PVLine(_depth);
        pvSearch(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, pline, lastIteration);
        System.out.println(
                "    evaluated nodes: " + _evaluatedNodes +
                        "    visited nodes:   " + _visitedNodes +
                        "    quiesce nodes:   " + _quiesceNodes +
                        "    hashed entries:  " + _transpositionTable.getSize());
        return pline;
    }

    private double transpositionLookUp(long zobrist, int tiefe) {
        if (use_transposition == false) return Double.NaN;
        TranspositionTable.TranspositionEntry en = _transpositionTable.get(zobrist);
        if (en != null && en.getDepth() <= tiefe) {
            return en.getVal();
        }
        return Double.NaN;
    }

    private void transpositionPlacement(long key, int depth, double alpha) {
        if (_transpositionTable.isFull() || !use_transposition) return;
        TranspositionTable.TranspositionEntry en = _transpositionTable.get(key);
        if (en == null) {
            _transpositionTable.put(key, new TranspositionTable.TranspositionEntry(alpha, depth));
        } else {
            if (en.getDepth() > depth) {
                en.setVal(alpha);
            }
        }
    }

    private double alphaBetaSearch(double alpha, double beta, int currentDepth, PVLine pLine, PVLine lastIteration) {
        _visitedNodes++;
        long zobrist = _board.zobrist();
        double transposition = transpositionLookUp(zobrist, currentDepth);
        if (!Double.isNaN(transposition)) {
            return transposition;
        }
        List<Move> allMoves = _board.getAvailableMovesShallow();
        if (currentDepth == _depth || allMoves.size() == 0 || _board.isGameOver()) {
            double val = Quiesce(alpha, beta, quiesce_depth);
            transpositionPlacement(zobrist, currentDepth, val);
            return val;
        }
        orderer.sort(allMoves, currentDepth, lastIteration);
        PVLine line = new PVLine(_depth - currentDepth);
        for (Move m : allMoves) {
            _board.move(m);
            double score;
            score = -alphaBetaSearch(-beta, -alpha, currentDepth + 1, line, lastIteration);
            _board.undoMove();
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
                pLine.getLine()[0] = m;
                for (int i = 0; i < line.getMovesInLine(); i++) {
                    pLine.getLine()[i + 1] = line.getLine()[i];
                }
                pLine.setMovesInLine(line.getMovesInLine() + 1);
                if (currentDepth == 0) {
                    _bestMove = m;
                }
            }
        }
        transpositionPlacement(zobrist, currentDepth, alpha);
        return alpha;
    }

    private double pvSearch(double alpha, double beta, int currentDepth, PVLine pLine, PVLine lastIteration) {
        _visitedNodes++;
        long zobrist = _board.zobrist();
        double transposition = transpositionLookUp(zobrist, currentDepth);
        if (!Double.isNaN(transposition)) {
            return transposition;
        }
        List<Move> allMoves = _board.getAvailableMovesShallow();
        if (currentDepth == _depth || allMoves.size() == 0 || _board.isGameOver()) {
            double val = Quiesce(alpha, beta, quiesce_depth);
            transpositionPlacement(zobrist, currentDepth, val);
            return val;
        }
        orderer.sort(allMoves, currentDepth, lastIteration);
        PVLine line = new PVLine(_depth - currentDepth);
        boolean bSearchPv = true;
        for (Move m : allMoves) {
            _board.move(m);
            double score;
            if (bSearchPv) {
                score = -pvSearch(-beta, -alpha, currentDepth + 1, line, lastIteration);
            } else {
                score = -pvSearch(-alpha - 1, -alpha, currentDepth + 1, line, lastIteration);
                if (score > alpha && score < beta) // in fail-soft ... && score < beta ) is common
                    score = -pvSearch(-beta, -alpha, currentDepth + 1, line, lastIteration); // re-search
            }
            _board.undoMove();
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
                pLine.getLine()[0] = m;
                for (int i = 0; i < line.getMovesInLine(); i++) {
                    pLine.getLine()[i + 1] = line.getLine()[i];
                }
                pLine.setMovesInLine(line.getMovesInLine() + 1);
                if (currentDepth == 0) {
                    _bestMove = m;
                }
            }
            bSearchPv = false;
        }
        transpositionPlacement(zobrist, currentDepth, alpha);
        return alpha;
    }

    private double Quiesce(double alpha, double beta, int depth_left) {
        _quiesceNodes++;
        _evaluatedNodes++;
        double stand_pat = evaluator.evaluate(_board) * _board.getActivePlayer();
        if (depth_left == 0) {
            return stand_pat;
        }
        if (stand_pat >= beta)
            return beta;
        if (alpha < stand_pat)
            alpha = stand_pat;
        List<Move> allMoves = _board.getAvailableMovesShallow();
        orderer.sort(allMoves, 0, null);
        for (Move m : allMoves) {
            if (m.getPieceTo() * m.getPieceFrom() < 0) {
                _board.move(m);
                double score = -Quiesce(-beta, -alpha, depth_left - 1);
                _board.undoMove();
                if (score >= beta)
                    return beta;
                if (score > alpha)
                    alpha = score;
            }
        }
        return alpha;
    }
}
