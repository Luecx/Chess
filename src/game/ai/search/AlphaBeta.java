package game.ai.search;

import board.Board;
import board.moves.Move;
import game.ai.evaluator.Evaluator;
import game.ai.ordering.Orderer;
import game.ai.tools.PVLine;
import game.ai.tools.TranspositionEntry;
import game.ai.tools.TranspositionTable;

import java.util.*;

public class AlphaBeta implements AI {

    private Evaluator evaluator;
    private Orderer orderer;

    private int max_depth;                          //max search depth for iterative deepening
    private int quiesce_depth;                      //max search depth after the full-search has completed
    private boolean use_iteration = true;           //flag for iterative deepening
    private boolean use_transposition = false;      //flag for transposition tables
    private boolean print_overview = false;         //flag for output-printing

    private final int NULL_MOVE_REDUCTION = 3;

    public AlphaBeta(Evaluator evaluator, Orderer orderer, int max_depth, int quiesce_depth) {
        this.evaluator = evaluator;
        this.max_depth = max_depth;
        this.orderer = orderer;
        this.quiesce_depth = quiesce_depth;
    }

    /**
     * getter for the flag for output-printing
     * @return  the printing-flag
     */
    public boolean isPrint_overview() {
        return print_overview;
    }

    /**
     * setter for the flag for output-printing
     * @param print_overview    the new printing-flag
     */
    public void setPrint_overview(boolean print_overview) {
        this.print_overview = print_overview;
    }

    /**
     * getter for the evaluator that is used to evaluate the
     * board position at leaf-nodes
     * @return  the evaluator
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    /**
     * setter for the evaluator that is used to evaluate the
     * board position at leaf-nodes
     * @param evaluator the new evaluator
     */
    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * getter for the sorter that is used to sort the
     * moves in order to reduce the search-space
     * @return  the sorter
     */
    public Orderer getOrderer() {
        return orderer;
    }

    /**
     * setter for the sorter that is used to sort the
     * moves in order to reduce the search-space
     * @param orderer  the new sorter
     */
    public void setOrderer(Orderer orderer) {
        this.orderer = orderer;
    }

    /**
     * max depth determines the maximum depth that is used
     * for searching the search-space.
     * If using iterative deepening, the algorithm will start a
     * search from 1 and end at a maximum depth of max_depth.
     * If iterative deepening is not used, the algorithm will
     * directly search to a depth of max_depth.
     *
     * @return the maximum depth
     */
    public int getMax_depth() {
        return max_depth;
    }

    /**
     * max depth determines the maximum depth that is used
     * for searching the search-space.
     * If using iterative deepening, the algorithm will start a
     * search from 1 and end at a maximum depth of max_depth.
     * If iterative deepening is not used, the algorithm will
     * directly search to a depth of max_depth.
     * @param max_depth the new maximum depth
     */
    public void setMax_depth(int max_depth) {
        this.max_depth = max_depth;
    }

    /**
     * quiesce_depth determines the depth that is searched
     * after the full depth is completed. It is useful for stability
     * @return  the quiesce depth
     */
    public int getQuiesce_depth() {
        return quiesce_depth;
    }

    /**
     * quiesce_depth determines the depth that is searched
     * after the full depth is completed. It is useful for stability
     * @param quiesce_depth   the new quiesce depth
     */
    public void setQuiesce_depth(int quiesce_depth) {
        this.quiesce_depth = quiesce_depth;
    }

    /**
     * iterative deepening is used to speed up the search process.
     * It searches the game tree multiple time and begins at a
     * depth of 1 and ends up at max_depth.
     * By using the information from the previous iteration, it reduces
     * the nodes that need to be evaluated.
     * @return  the flag to use iterative deepening
     */
    public boolean isUse_iteration() {
        return use_iteration;
    }

    /**
     * iterative deepening is used to speed up the search process.
     * It searches the game tree multiple time and begins at a
     * depth of 1 and ends up at max_depth.
     * By using the information from the previous iteration, it reduces
     * the nodes that need to be evaluated.
     * @param use_iteration   new flag to use iterative deepening
     */
    public void setUse_iteration(boolean use_iteration) {
        this.use_iteration = use_iteration;
    }

    /**
     * transposition tables are used to reduce the search space but
     * can cost stability due to hash collisions.
     * Especially useful in the late-game.
     *
     * @return  the flag for the usage of transposition tables.
     */
    public boolean isUse_transposition() {
        return use_transposition;
    }

    /**
     * transposition tables are used to reduce the search space but
     * can cost stability due to hash collisions.
     * Especially useful in the late-game.
     *
     * @param use_transposition   new flag for the usage of transposition tables.
     */
    public void setUse_transposition(boolean use_transposition) {
        this.use_transposition = use_transposition;
    }

    private int _depth;
    private int _quiesceNodes;
    private int _visitedNodes;
    private int _terminalNodes;
    private Board _board;
    private Move _bestMove;

    private TranspositionTable<TranspositionEntry> _transpositionTable;

    @Override
    public Move bestMove(Board board) {
        _board = board;
        if (use_transposition){
            _transpositionTable = new TranspositionTable<>((int) (50E6));
        }
        long time = System.currentTimeMillis();
        if (use_iteration) {
            PVLine line = null;
            for (int i = 1; i <= max_depth; i++) {
                line = iteration(i, line);
            }
        } else {
            iteration(max_depth, null);
        }
        if(print_overview)
            System.out.println("required time: " + (System.currentTimeMillis() - time) + " ms");
        return _bestMove;
    }

    /**
     * prints a summary of the current iteration after is has finished.
     * If print_overview is disabled, the method will not do anything.
     *
     * It prints the time in [mm:ss:uuu:nnnnnn] followed by the following parameters:
     *  - total visited nodes: the total amount of nodes that have been visited including quiesce-search
     *  - terminal nodes: the leaf nodes in qSearch.
     *  - visited nodes full depth: the amount of nodes visited without qSearch.
     *  - visited quiesce nodes: the amount of nodes visited only in qSearch.
     *
     * @param nanos     the time in nanoseconds that was needed.
     */
    private void printIterationSummary(long nanos){

        if (!print_overview) return;

        int min = (int) (nanos / (60 * 1E9) % 60);
        int sec = (int) (nanos / 1E9 % 60);
        int mil = (int) (nanos / 1E6 % 1E3);
        int nano = (int) (nanos % 1E6);

        System.out.format(
                        "required time[m:s:ms:ns]: %02d:%02d:%03d:%06d   " +
                        "total visited nodes: %9d   " +
                        "terminal nodes: %9d   " +
                        "visited nodes full depth: %9d   " +
                        "visited quiesce nodes: %9d\n",
                min,sec,mil,nano,
                _visitedNodes + _quiesceNodes, _terminalNodes, _visitedNodes, _quiesceNodes);
    }

    /**
     * processes one iteration to the given depth.
     * it resets internal values like the best move.
     * It will also call printIterationSummary().
     * @param depth
     * @param lastIteration
     * @return
     */
    public PVLine iteration(int depth, PVLine lastIteration) {
        this._depth = depth;
        _bestMove = null;
        _terminalNodes = 0;
        _visitedNodes = 0;
        _quiesceNodes = 0;
        PVLine pline = new PVLine(_depth);
        long time = System.nanoTime();
        pvSearch(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, pline, lastIteration);
        printIterationSummary(System.nanoTime()-time);

        return pline;
    }

    private TranspositionEntry transpositionLookUp(long zobrist, int depth) {
        if (use_transposition == false || !use_transposition) return null;
        TranspositionEntry en = _transpositionTable.get(zobrist);
        if (en != null && en.getSkipped_depths() >= (_depth - depth) && _board.getActivePlayer() == en.getColor()) {
            return en;
        }
        return null;
    }

    private void transpositionPlacement(long key, int depth, double alpha, int nodeType) {
        if (!use_transposition || _transpositionTable == null || _transpositionTable.isFull()) return;
        TranspositionEntry en = _transpositionTable.get(key);
        if (en == null) {
            _transpositionTable.put(key, new TranspositionEntry(alpha, _depth - depth, nodeType, _board.getActivePlayer()));
        } else {
            if (en.getSkipped_depths() > depth) {
                en.setVal(alpha);
            }
        }
    }

    private double alphaBetaSearch(double alpha, double beta, int currentDepth, PVLine pLine, PVLine lastIteration) {
        _visitedNodes++;


        //used to determine if the node is a PV_Node and has a placed transposition entry.
        boolean transpositionHasBeenPlaced = false;
        long zobrist = _board.zobrist();
        TranspositionEntry transposition = transpositionLookUp(zobrist, currentDepth);
        if (transposition != null) {
            if(transposition.getNode_type() == TranspositionEntry.PV_NODE){
                return transposition.getVal();
            }else if (transposition.getNode_type() == TranspositionEntry.CUT_NODE){
                beta = transposition.getVal();
            }else{
                alpha = transposition.getVal();
            }
        }


        List<Move> allMoves = currentDepth == 0 ? _board.getLegalMoves() : _board.getPseudoLegalMoves();
        if (currentDepth == _depth || allMoves.size() == 0 || _board.isGameOver()) {
            double val = Quiesce(alpha, beta, quiesce_depth);
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
                transpositionPlacement(zobrist, currentDepth, beta, TranspositionEntry.CUT_NODE);
                return beta;
            }


            if (score > alpha) {
                alpha = score;

                transpositionHasBeenPlaced = true;
                transpositionPlacement(zobrist, currentDepth, alpha, TranspositionEntry.PV_NODE);

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

        if (!transpositionHasBeenPlaced)
            transpositionPlacement(zobrist, currentDepth, alpha, TranspositionEntry.ALL_NODE);

        return alpha;
    }

    private double pvSearch(double alpha, double beta, int currentDepth, PVLine pLine, PVLine lastIteration) {
        _visitedNodes++;


        //used to determine if the node is a PV_Node and has a placed transposition entry.
        boolean transpositionHasBeenPlaced = false;
        long zobrist = _board.zobrist();
        TranspositionEntry transposition = transpositionLookUp(zobrist, currentDepth);
        if (transposition != null) {
            if(transposition.getNode_type() == TranspositionEntry.PV_NODE){
                return transposition.getVal();
            }else if (transposition.getNode_type() == TranspositionEntry.CUT_NODE){
                beta = transposition.getVal();
            }else{
                alpha = transposition.getVal();
            }
        }
        List<Move> allMoves = currentDepth == 0 ? _board.getLegalMoves() : _board.getPseudoLegalMoves();
        if (currentDepth >= _depth || allMoves.size() == 0 || _board.isGameOver()) {
            double val = Quiesce(alpha, beta, quiesce_depth);
            return val;
        }

        PVLine line = new PVLine(_depth - currentDepth); // here is where I moved it
        //null move
        Move nullMove = new Move();
        _board.move(nullMove);
        double score = -pvSearch(-alpha - 1, -alpha, currentDepth + 1 + NULL_MOVE_REDUCTION, line, lastIteration);
        _board.undoMove();
        if (score >= beta) { return beta; }

        orderer.sort(allMoves, currentDepth, lastIteration);
        //PVLine line = new PVLine(_depth - currentDepth); // moved it up
        boolean bSearchPv = true;


        for (Move m : allMoves) {

            _board.move(m);
            //double score; // moved it up
            if (bSearchPv) {
                score = -pvSearch(-beta, -alpha, currentDepth + 1, line, lastIteration);
            } else {
                score = -pvSearch(-alpha - 1, -alpha, currentDepth + 1, line, lastIteration);
                if (score > alpha && score < beta) // in fail-soft ... && score < beta ) is common
                    score = -pvSearch(-beta, -alpha, currentDepth + 1, line, lastIteration); // re-search
            }
            _board.undoMove();


            if (score >= beta) {
                transpositionPlacement(zobrist, currentDepth, beta, TranspositionEntry.CUT_NODE);
                return beta;
            }


            if (score > alpha) {
                alpha = score;

                transpositionHasBeenPlaced = true;
                transpositionPlacement(zobrist, currentDepth, alpha, TranspositionEntry.PV_NODE);


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

        if (!transpositionHasBeenPlaced)
            transpositionPlacement(zobrist, currentDepth, alpha, TranspositionEntry.ALL_NODE);

        return alpha;
    }

    private double Quiesce(double alpha, double beta, int depth_left) {
        _quiesceNodes++;
        double stand_pat = evaluator.evaluate(_board) * _board.getActivePlayer();
        if (depth_left == 0) {
            _terminalNodes++;
            return stand_pat;
        }
        if (stand_pat >= beta){
            _terminalNodes++;
            return beta;
        }
        if (alpha < stand_pat)
            alpha = stand_pat;
        List<Move> allMoves = _board.getPseudoLegalMoves();
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
