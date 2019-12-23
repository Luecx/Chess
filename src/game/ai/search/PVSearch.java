package game.ai.search;

import board.Board;
import board.moves.Move;
import game.ai.evaluator.Evaluator;
import game.ai.ordering.Orderer;
import game.ai.reducing.Reducer;
import game.ai.tools.*;

import java.util.List;

public class PVSearch implements AI {


    public static final int FLAG_TIME_LIMIT = 1;
    public static final int FLAG_DEPTH_LIMIT = 2;

    private Evaluator evaluator;
    private Orderer orderer;
    private Reducer reducer;

    private int limit;                              //limit for searching. could be a time in ms or a depth
    private int limit_flag;                         //limit flag to determine if limit is max depth or time

    private int quiesce_depth;                      //max search depth after the full-search has completed
    private boolean use_iteration = true;           //flag for iterative deepening
    private boolean use_transposition = false;      //flag for transposition tables
    private boolean print_overview = true;          //flag for output-printing
    private boolean use_null_moves = true;

    private boolean use_killer_heuristic = false;   //flag for killer tables
    private int killer_count = 2;
    private int null_move_reduction = 2;            //how much to reduce null moves
    private int depth_to_never_reduce = 2;          //how many plies to never reduce
    private int late_move_reduction = 2;            //how many plies to reduce by in LMR
    private boolean use_LMR = true;                 //flag for LMR
    private int num_moves_not_reduced;                  //the number of moves not to reduce (from the beginning of the list)


    private SearchOverview searchOverview;


    public PVSearch(Evaluator evaluator, Orderer orderer, Reducer reducer, int limit_flag,  int limit, int quiesce_depth) {
        this.evaluator = evaluator;
        this.limit_flag = limit_flag;
        this.reducer = reducer;
        this.limit = limit;
        this.orderer = orderer;
        this.quiesce_depth = quiesce_depth;
    }

    /**
     * getter for the killer heuristic flag.
     * If the flag is set to "true", beta cutoffs will be stored in a list
     * and this will be used to sort the moves if the orderer considers
     * killer moves
     * @return      killer heuristic flag
     */
    public boolean isUse_killer_heuristic() {
        return use_killer_heuristic;
    }

    /**
     * setter for the killer heuristic flag.
     * If the flag is set to "true", beta cutoffs will be stored in a list
     * and this will be used to sort the moves if the orderer considers
     * killer moves
     *
     * @param use_killer_heuristic      new flag for using the killer heuristic
     */
    public void setUse_killer_heuristic(boolean use_killer_heuristic) {
        this.use_killer_heuristic = use_killer_heuristic;
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
     * search from 1 and end at a maximum depth of limit.
     * If iterative deepening is not used, the algorithm will
     * directly search to a depth of limit.
     *
     * @return the maximum depth
     */
    public int getLimit() {
        return limit;
    }

    /**
     * max depth determines the maximum depth that is used
     * for searching the search-space.
     * If using iterative deepening, the algorithm will start a
     * search from 1 and end at a maximum depth of limit.
     * If iterative deepening is not used, the algorithm will
     * directly search to a depth of limit.
     * @param limit the new maximum depth
     */
    public void setLimit(int limit) {
        this.limit = limit;
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
     * depth of 1 and ends up at limit.
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
     * depth of 1 and ends up at limit.
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

    /**
     * null moves are used to reduce the search space dramatically.
     * It might cost some performance.
     * @return  the flag for the usage of null moves
     */
    public boolean isUse_null_moves() {
        return use_null_moves;
    }

    /**
     * null moves are used to reduce the search space dramatically.
     * It might cost some performance.
     * @param use_null_moves   the new flag for the usage of null moves
     */
    public void setUse_null_moves(boolean use_null_moves) {
        this.use_null_moves = use_null_moves;
    }

    /**
     * the limit flag determines if the given limit should be interpreted
     * as a maximum search depth or a maximum time the search is allowed to take.
     * the flag can be one of the following:
     *
     *      1 = FLAG_TIME_LIMIT
     *      2 = FLAG_DEPTH_LIMIT
     *
     * @return      the limit flag.
     */
    public int getLimit_flag() {
        return limit_flag;
    }

    /**
     * the limit flag determines if the given limit should be interpreted
     * as a maximum search depth or a maximum time the search is allowed to take.
     * the flag can be one of the following:
     *
     *      1 = FLAG_TIME_LIMIT
     *      2 = FLAG_DEPTH_LIMIT
     *
     * @param limit_flag    the new limit flag
     */
    public void setLimit_flag(int limit_flag) {
        this.limit_flag = limit_flag;
    }

    /**
     * this method returns the amount of killers that will be stored per depth
     * @return      the amount of killers
     */
    public int getKiller_count() {
        return killer_count;
    }

    /**
     * this methods will set the amount of killers that will be stored per depth
     * @param killer_count      new amount of killers
     */
    public void setKiller_count(int killer_count) {
        this.killer_count = killer_count;
    }

    /**
     * this method returns the number of ply null moves are reduced by
     * @return      the amount of killers
     */
    public int getNull_move_reduction() {
        return null_move_reduction;
    }

    /**
     * this methods will set the number of ply null moves are reduced by
     * @param reduction      the amount to reduce null moves by
     */
    public void setNull_move_reduction(int reduction) {
        this.null_move_reduction = reduction;
    }

    /**
     * this method returns the last search overview containing information
     * about the iterations, the flags used etc.
     * The search overview resets every time the bestMove() method is called.
     * @return
     */
    public SearchOverview getSearchOverview() {
        return searchOverview;
    }

    /**
     * This method returns the amount of plies to never reduce by.
     * That is, the number of plies we will always calculate before any reductions
     * @return the number of plies to never reduce
     */
    public int getDepth_to_never_reduce() {
        return depth_to_never_reduce;
    }

    /**
     * This method sets the amount of plies to never reduce by.
     * That is, the number of plies we will always calculate before any reductions
     * @param depth_to_never_reduce      the number of plies to never reduce by
     */
    public void setdepth_to_never_reduce(int depth_to_never_reduce) {
        this.depth_to_never_reduce = depth_to_never_reduce;
    }

    /**
     * This method gets the number of plies we reduce by in late move reduction
     * @return the number of plies to reduce by
     */
    public int getLate_move_reduction() {
        return late_move_reduction;
    }

    /**
     * This method sets the number of plies we reduce by in late move reduction
     * @param late_move_reduction      the number of plies to reduce by
     */
    public void setLate_move_reduction(int late_move_reduction) {
        this.late_move_reduction = late_move_reduction;
    }

    /**
     * This method gets the use LMR flag
     * @return the number of plies to reduce by
     */
    public boolean isUse_LMR() {
        return use_LMR;
    }
    /**
     * This method sets the use_LMR flag
     * @param use_LMR      a flag for using LMR
     */
    public void setUse_LMR(boolean use_LMR) {
        this.use_LMR = use_LMR;
    }
    
    /**
     * This method gets the number of moves we don't reduce
     * @return the number of moves we don't reduce
     */
    public int getNum_moves_not_reduced() {
        return num_moves_not_reduced;
    }

    /**
     * This method sets the number of moves we don't reduce
     * @param num_moves_not_reduced      the number of moves we don't reduce
     */
    public void setNum_moves_not_reduced(int num_moves_not_reduced) {
        this.num_moves_not_reduced = num_moves_not_reduced;
    }

    /**
     * the reducer is used to determine the amount of depths to reduce a search for a given move
     * @return      the reducer object
     */
    public Reducer getReducer() {
        return reducer;
    }

    /**
     * the reducer is used to determine the amount of depths to reduce a search for a given move
     * @param reducer       new reducer object
     */
    public void setReducer(Reducer reducer) {
        this.reducer = reducer;
    }

    private int _depth;
    private int _quiesceNodes;
    private int _visitedNodes;
    private int _terminalNodes;
    private Board _board;
    private Move _bestMove;

    private KillerTable _killerTable;
    private TranspositionTable<TranspositionEntry> _transpositionTable;

    @Override
    public Move bestMove(Board board) {
        _board = board;
        if(!use_iteration && limit_flag == FLAG_TIME_LIMIT){
            throw new RuntimeException("Cannot limit non iterative deepening on time");
        }

        searchOverview = new SearchOverview(
                use_iteration ? "ITERATIVE_DEEPENING":"",
                limit_flag == FLAG_TIME_LIMIT ? "TIME_LIMIT":"DEPTH_LIMIT",
                use_transposition ? "TRANSPOSITION TABLE":"",
                use_null_moves ? "NULL MOVES":"",
                use_killer_heuristic ? "KILLER HEURISTIC":""
        );
        searchOverview.setqDepth(quiesce_depth);

        long time = System.currentTimeMillis();


        if (use_transposition){
            _transpositionTable = new TranspositionTable<>((int) (50E6));
        }
        if (use_iteration) {
            if(limit_flag == FLAG_TIME_LIMIT){
                //<editor-fold desc="time limited iterative deepening">
                PVLine line = null;
                int depth = 1;
                long prevTime = System.currentTimeMillis();
                long prevNode = 1;
                double branchingFactor = 1;
                double expectedTime = 0;
                while (System.currentTimeMillis() - time + expectedTime < limit) {
                    line = iteration(depth++, line);
                    long iterationTime = System.currentTimeMillis() - prevTime;
                    prevTime = System.currentTimeMillis();

                    branchingFactor = Math.min(2,(double) (_visitedNodes + _quiesceNodes) / prevNode);
                    prevNode = (_visitedNodes + _quiesceNodes);
                    expectedTime = branchingFactor * iterationTime;
                }
                //</editor-fold>
            }else{
                //<editor-fold desc="depth limited iterative deepening">
                PVLine line = null;
                for (int i = 1; i <= limit; i++) {
                    line = iteration(i, line);
                }
                //</editor-fold>
            }
        } else {
            iteration(limit, null);
        }


        searchOverview.setDepth(this._depth);
        searchOverview.setTotalTime((int)(System.currentTimeMillis()-time));

        if(print_overview) searchOverview.printTotalSummary();
        return _bestMove;
    }


    /**
     * processes one iteration to the given depth.
     * it resets internal values like the best move.
     * It will also print an overview of the iteration.
     * @param depth
     * @param lastIteration
     * @return
     */
    public PVLine iteration(int depth, PVLine lastIteration) {


        if(use_killer_heuristic)
            _killerTable = new KillerTable(depth+1+null_move_reduction, killer_count);

        _depth              = depth;
        _bestMove           = null;
        _terminalNodes      = 0;
        _visitedNodes       = 0;
        _quiesceNodes       = 0;

        PVLine pline        = new PVLine(_depth);
        long time           = System.currentTimeMillis();


        pvSearch(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, depth, 0,pline, lastIteration);


        searchOverview.addIteration(
                _depth,
                _visitedNodes + _quiesceNodes,
                _visitedNodes,
                _terminalNodes,
                _quiesceNodes,
                (int)(System.currentTimeMillis() - time));

        if(print_overview) searchOverview.printIterationSummary();

        return pline;
    }

    /**
     * a function used do look up a value in the transposition table
     * @param zobrist
     * @param depth
     * @return
     */
    private TranspositionEntry transpositionLookUp(long zobrist, int depth) {
        if (use_transposition == false || !use_transposition) return null;
        TranspositionEntry en = _transpositionTable.get(zobrist);
        if (en != null && en.getSkipped_depths() >= (_depth - depth) && _board.getActivePlayer() == en.getColor()) {
            return en;
        }
        return null;
    }

    /**
     * a function used to place a value in the transposition table
     * @param key
     * @param depth
     * @param alpha
     * @param nodeType
     */
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

    static double working = 0;
    static double not = 0;

    /**
     * main search parth
     * @param alpha             lower limit
     * @param beta              upper limit
     * @param currentDepth      current ply
     * @param pLine             pvLine
     * @param loopIndex         the loop index from the previous recursive call
     * @param depthLeft         the depth left to search
     * @param lastIteration     pvLine from prev iteration (nullable)
     * @return
     */
    private double pvSearch(
            double alpha,
            double beta,
            int currentDepth,
            int depthLeft,
            int loopIndex,
            PVLine pLine,
            PVLine lastIteration) {
        _visitedNodes++;

        //TODO
        //<editor-fold desc="Transposition lookup">
        //used to determine if the node is a PV_Node and has a placed transposition entry.
        boolean transpositionHasBeenPlaced = false;
        long zobrist = _board.zobrist();
        TranspositionEntry transposition = transpositionLookUp(zobrist, currentDepth);
        if (transposition != null) {
            //TODO
        }
        //</editor-fold>


        //<editor-fold desc="quiesce search">
        List<Move> allMoves = currentDepth == 0 ? _board.getLegalMoves() : _board.getPseudoLegalMoves();
        if (depthLeft <= 0 || currentDepth >= _depth || allMoves.size() == 0 || _board.isGameOver()) {
            double val = Quiesce(alpha, beta, quiesce_depth);
            return val;
        }
        //</editor-fold>


        PVLine line = new PVLine(_depth - currentDepth);

        //<editor-fold desc="Null moves">
        double score;
        if(use_null_moves){
            Move nullMove = new Move();
            _board.move(nullMove);
            score = -pvSearch(
                    -alpha - 1,
                    -alpha,
                    currentDepth + 1,
                    depthLeft - 1 - null_move_reduction,
                    -1,
                    line,
                    lastIteration);
            _board.undoMove();
            if (score >= beta) {
                return beta;
            }
        }
        //</editor-fold>


        //<editor-fold desc="move-ordering">
        orderer.sort(allMoves, currentDepth, lastIteration, _board, _killerTable);
        //</editor-fold>


        //<editor-fold desc="Searching">
        boolean bSearchPv = true;
        
        for(int index = 0; index < allMoves.size(); index++){
            Move m = allMoves.get(index);


            _board.move(m);


            //<editor-fold desc="LMR">
            int to_reduce = 0;
            if (use_LMR && _depth > depth_to_never_reduce && m.getPieceTo() == 0 && index > num_moves_not_reduced) {
                to_reduce = reducer.reduce(m, currentDepth, bSearchPv);
            }
            //</editor-fold>


            //<editor-fold desc="recursion">
            if (bSearchPv) {
                //<editor-fold desc="pv node search">
                score = -pvSearch(
                        -beta,
                        -alpha,
                        currentDepth + 1,
                        depthLeft - 1,
                        index,
                        line,
                        lastIteration);
                //</editor-fold>
            } else {
                //<editor-fold desc="non pv search">
                score = -pvSearch(
                        -alpha - 1,
                        -alpha,
                        currentDepth + 1,
                        depthLeft - 1 - to_reduce,
                        index,
                        line,
                        null);
                if (score > alpha && score < beta || (to_reduce != 0 && score > beta))
                    score = -pvSearch(
                            -beta,
                            -alpha,
                            currentDepth + 1,
                            depthLeft - 1,
                            index,
                            line,
                            null); // re-search
                //</editor-fold>
            }
            //</editor-fold>


            _board.undoMove();


            //<editor-fold desc="beta cutoff">
            if (score >= beta) {
                if (_killerTable != null && m.getPieceTo() == 0){
                    _killerTable.put(currentDepth, m);
                }
                transpositionPlacement(zobrist, currentDepth, beta, TranspositionEntry.CUT_NODE);
                return beta;
            }
            //</editor-fold>


            //<editor-fold desc="killer node">
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

                bSearchPv = false;
            }
            //</editor-fold>
        }
        //</editor-fold>

        //TODO
        //<editor-fold desc="Transposition placing">
        if (!transpositionHasBeenPlaced)
            transpositionPlacement(zobrist, currentDepth, alpha, TranspositionEntry.ALL_NODE);
        //</editor-fold>

        return alpha;
    }

    /**
     * Qsearch after main search
     * @param alpha         lower limit
     * @param beta          upper limit
     * @param depth_left    depth left until stop
     * @return
     */
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
        List<Move> allMoves = _board.getCaptureMoves();
        orderer.sort(allMoves, 0, null, _board, null);
        for (Move m : allMoves) {
            _board.move(m);
            double score = -Quiesce(-beta, -alpha, depth_left - 1);
            _board.undoMove();
            if (score >= beta) {
                //System.out.println("beta cutoff");
                return beta;
            }
            if (score > alpha)
                alpha = score;

        }
        return alpha;
    }
}
