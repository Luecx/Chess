package ai.search;

import ai.evaluator.AdvancedEvaluator;
import ai.evaluator.Evaluator;
import ai.evaluator.decider.SimpleDecider;
import ai.ordering.Orderer;
import ai.ordering.SystematicOrderer2;
import ai.reducing.Reducer;
import ai.reducing.SenpaiReducer;
import ai.tools.tables.HistoryTable;
import ai.tools.tables.KillerTable;
import ai.tools.transpositions.TranspositionEntry;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.FastBoard;
import board.moves.Move;
import board.moves.MoveListBuffer;
import board.setup.Setup;
import io.IO;
import io.UCI;

import java.util.List;

public class AdvancedSearch implements AI {

    public static final int                             MAX_CHECKMATE_VALUE     = (int)1E8;     //this value means checkmate at depth = 0
    public static final int                             MIN_CHECKMATE_VALUE     = (int)1E7;     //this is the minimum value which is interpreted as checkmate
    public static final int                             MAXIMUM_STORE_DEPTH     = 128;

    public static final int                             FLAG_TIME_LIMIT         = 1;
    public static final int                             FLAG_DEPTH_LIMIT        = 2;

    protected Evaluator                                 evaluator;
    protected Orderer                                   orderer;
    protected Reducer                                   reducer;

    protected int                                       limit;                                  //limit for searching. could be a time in ms or a depth
    protected int                                       limit_flag;                             //limit flag to determine if limit is max depth or time

    protected boolean                                   use_qSearch             = true;         //flag for qSearch
    protected boolean                                   use_iteration           = true;         //flag for iterative deepening
    protected boolean                                   use_transposition       = true;         //flag for transposition tables
    protected boolean                                   use_null_moves          = true;         //flag for null moves
    protected boolean                                   use_LMR                 = true;         //flag for LMR
    protected boolean                                   use_razoring            = true;         //flag for razoring
    protected boolean                                   use_killer_heuristic    = true;         //flag for killer tables
    protected boolean                                   use_history_heuristic   = true;         //flag for history heuristic
    protected boolean                                   use_aspiration          = false;        //flag for aspiriation windows
    protected boolean                                   use_futility_pruning    = true;         //flag for futility pruning at depth<=1 nodes
    protected boolean                                   use_delta_pruning       = true;         //flag for delta pruning inside quiescence search

    protected boolean                                   debug                   = false;        //searches all moves at root with full window
    protected boolean                                   print_overview          = true;         //flag for output-printing

    protected int                                       deepening_start_depth   = 1;            //initial depth for it-deepening
    protected int                                       killer_count            = 3;            //amount of killer moves
    protected int                                       null_move_reduction     = 2;            //how much to reduce null moves
    protected int                                       razor_margin            = 300;          //margin for razoring
    protected int                                       futility_pruning_margin = 200;          //safety margin for futility pruning
    protected int                                       delta_pruning_margin    = 200;          //safety margin for futility pruning
    protected int                                       delta_pruning_big_margin= 1000;         //safety margin for delta pruning without checking nodes
    protected int[]                                     delta_pruning_captures  =               //adding these values to captures for delta pruning
                                    new int[]{0,100,500,300,315,800};

    private KillerTable                                 _killerTable;
    private HistoryTable                                _historyTable;
    private TranspositionTable<TranspositionEntry>      _transpositionTable;
    private Board                                       _board;
    private MoveListBuffer                              _buffer;

    private double                                      _score;
    private int                                         _nodes;
    private int                                         _selDepth;

    private int                                         _betaCutoffs;
    private int                                         _qSearchNodes;
    private int                                         _pvSearchNodes;
    private int                                         _transpositionHits;


    public AdvancedSearch(Evaluator evaluator, Orderer orderer, Reducer reducer, int limit_flag, int limit) {
        this.evaluator = evaluator;
        this.orderer = orderer;
        this.reducer = reducer;
        this._buffer = new MoveListBuffer(MAXIMUM_STORE_DEPTH, 128);
        this.limit_flag = limit_flag;
        this.limit = limit;
    }

    /**
     * getter for the history heuristic flag.
     * If the flag is set to "true", beta cutoffs will be be stored in a table by their move from-to value
     * and this will be used to sort the moves if the orderer makes use of the history heuristic
     * @return      history heuristic flag
     */
    public boolean isUse_history_heuristic() {
        return use_history_heuristic;
    }

    /**
     * setter for the history heuristic flag.
     * If the flag is set to "true", beta cutoffs will be be stored in a table by their move from-to value
     * and this will be used to sort the moves if the orderer makes use of the history heuristic
     * killer moves
     *
     * @param use_history_heuristic      new flag for using the history heuristic
     */
    public void setUse_history_heuristic(boolean use_history_heuristic) {
        this.use_history_heuristic = use_history_heuristic;
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
     * setter for debugging
     * @return
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * enables/disabled debugging.
     * This includes searching each move at the root with an infinite window and printing its score
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
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
     * iterative deepening is used to speed up the search process.
     * It searches the visual.game tree multiple time and begins at a
     * depth of 1 and ends up at limit.
     * By using the information from the previous iterationGradient, it reduces
     * the nodes that need to be evaluated.
     * @return  the flag to use iterative deepening
     */
    public boolean isUse_iteration() {
        return use_iteration;
    }

    /**
     * iterative deepening is used to speed up the search process.
     * It searches the visual.game tree multiple time and begins at a
     * depth of 1 and ends up at limit.
     * By using the information from the previous iterationGradient, it reduces
     * the nodes that need to be evaluated.
     * @param use_iteration   new flag to use iterative deepening
     */
    public void setUse_iteration(boolean use_iteration) {
        this.use_iteration = use_iteration;
    }

    /**
     * transposition tables are used to reduce the search space but
     * can cost stability due to hash collisions.
     * Especially useful in the late-visual.game.
     *
     * @return  the flag for the usage of transposition tables.
     */
    public boolean isUse_transposition() {
        return use_transposition;
    }

    /**
     * transposition tables are used to reduce the search space but
     * can cost stability due to hash collisions.
     * Especially useful in the late-visual.game.
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
     * This method sets the use_qSearch flag
     * @return      a flag for using qSearch
     */
    public boolean isUse_qSearch() {
        return use_qSearch;
    }
    /**
     * This method sets the use_qSearch flag
     * @param use_qSearch      a flag for using qSearch
     */
    public void setUse_qSearch(boolean use_qSearch) {
        this.use_qSearch = use_qSearch;
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

    /**
     * returns the evaluator object for this search
     * @return
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    /**
     * sets the evaluator for this search
     * @param evaluator
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
     * flag for razoring
     * @return
     */
    public boolean isUse_razoring() {
        return use_razoring;
    }

    /**
     * sets the flag to use razoring.
     * if set to true, razoring will be enabled
     * @param use_razoring
     */
    public void setUse_razoring(boolean use_razoring) {
        this.use_razoring = use_razoring;
    }

    /**
     * returns a value by which null moves are going to be reduced
     * @return
     */
    public int getNull_move_reduction() {
        return null_move_reduction;
    }

    /**
     * returns the depth in ply by which null moves are reduced
     * @param null_move_reduction
     */
    public void setNull_move_reduction(int null_move_reduction) {
        this.null_move_reduction = null_move_reduction;
    }

    /**
     * returns the offset for razoring
     * @return
     */
    public int getRazor_margin() {
        return razor_margin;
    }

    /**
     * sets the offset for razoring
     * @param razor_margin
     */
    public void setRazor_margin(int razor_margin) {
        this.razor_margin = razor_margin;
    }


    /**
     * gets the usage of aspiriation windows for depth > 2 searches
     * @return
     */
    public boolean isUse_aspiration() {
        return use_aspiration;
    }

    /**
     * sets the usage of aspiriation windows for depth > 2 searches
     * @param use_aspiration
     */
    public void setUse_aspiration(boolean use_aspiration) {
        this.use_aspiration = use_aspiration;
    }

    /**
     * gets the usage of futility pruning at depth<=1 nodes
     * @return
     */
    public boolean isUse_futility_pruning() {
        return use_futility_pruning;
    }

    /**
     * sets the usage of futility pruning at depth<=1 nodes
     * @param use_futility_pruning
     */
    public void setUse_futility_pruning(boolean use_futility_pruning) {
        this.use_futility_pruning = use_futility_pruning;
    }

    /**
     * gets the usage of delta pruning at quiescence nodes
     * @return
     */
    public boolean isUse_delta_pruning() {
        return use_delta_pruning;
    }

    /**
     * sets the usage of delta pruning at quiescence nodes
     * @param use_delta_pruning
     */
    public void setUse_delta_pruning(boolean use_delta_pruning) {
        this.use_delta_pruning = use_delta_pruning;
    }

    /**
     * gets the futiltiy margin for depth<=1 nodes
     * @return
     */
    public int getFutility_pruning_margin() {
        return futility_pruning_margin;
    }

    /**
     * sets the futiltiy margin for depth<=1 nodes
     */
    public void setFutility_pruning_margin(int futility_pruning_margin) {
        this.futility_pruning_margin = futility_pruning_margin;
    }

    /**
     * gets the delta pruning margin for quiescence nodes
     * @return
     */
    public int getDelta_pruning_margin() {
        return delta_pruning_margin;
    }

    /**
     * sets the delta pruning margin for quiescence nodes
     */
    public void setDelta_pruning_margin(int delta_pruning_margin) {
        this.delta_pruning_margin = delta_pruning_margin;
    }

    /**
     * gets the big margin for delta pruning at quiescence nodes.
     * If the stand_pat is this much below alpha, this node will not be searched at all.
     * @return
     */
    public int getDelta_pruning_big_margin() {
        return delta_pruning_big_margin;
    }

    /**
     * sets the big margin for delta pruning at quiescence nodes.
     * If the stand_pat is this much below alpha, this node will not be searched at all.
     */
    public void setDelta_pruning_big_margin(int delta_pruning_big_margin) {
        this.delta_pruning_big_margin = delta_pruning_big_margin;
    }

    /**
     * gets the initial depth at which iterative deepening is started
     * @return
     */
    public int getDeepening_start_depth() {
        return deepening_start_depth;
    }

    /**
     * sets the initial depth at which iterative deepening is started
     * @return
     */
    public void setDeepening_start_depth(int deepening_start_depth) {
        this.deepening_start_depth = deepening_start_depth;
    }

    private double pvSearch(double alpha, double beta, int currentDepth, int depthLeft, boolean pv, boolean extension) {
        _selDepth = Math.max(_selDepth, currentDepth);



        long        zobrist         = _board.zobrist();
        double      origonalAlpha   = alpha;
        double      score           = Double.NEGATIVE_INFINITY;
        double      highestScore    = score;
        double      eval            = evaluator.evaluate(_board) * _board.getActivePlayer();
        int         legalMoves      = 0;
        boolean     isInCheck       = _board.isInCheck(_board.getActivePlayer());
        Move        bestMove        = null;


        if(_board.isDraw()){
            _nodes ++;
            _pvSearchNodes ++;
            return 0;
        }
        if(depthLeft <= 0) {
            score = qSearch(alpha, beta, currentDepth,0);
            return score;
        }

        _nodes ++;
        _pvSearchNodes ++;


        /**
         * mate distance pruning
         */
        double mating_value = MAX_CHECKMATE_VALUE - currentDepth;
        if (mating_value < beta) {
            beta = mating_value;
            if (alpha >= mating_value) return mating_value;
        }
        mating_value = -MAX_CHECKMATE_VALUE + currentDepth;
        if (mating_value > alpha) {
            alpha = mating_value;
            if (beta <= mating_value) return mating_value;
        }



        /**
         * checking for transpositions
         */
        if (use_transposition) {
            TranspositionEntry tt = retrieveFromTT(zobrist, currentDepth, depthLeft);
            if(tt != null){

                _transpositionHits ++;

                if (tt.getNode_type() == TranspositionEntry.PV_NODE && tt.getVal() >= alpha){
                    return tt.getVal();
                }else if (tt.getNode_type() == TranspositionEntry.CUT_NODE) {
                    if(tt.getVal() >= beta){
                        return beta;
                    }
                    if(alpha < tt.getVal() && !pv){
                        alpha = tt.getVal();
                    }
                } else if (tt.getNode_type() == TranspositionEntry.ALL_NODE) {
                    if (tt.getVal() <= alpha) {
                        return alpha;
                    }
                    if(beta > tt.getVal() && !pv){
                        beta = tt.getVal();
                    }


                }
            }
        }


        /**
         * doing razoring
         */
        if (use_razoring) {
            if (!extension &&
                !pv &&
                !_board.isInCheck(_board.getActivePlayer()) &&
                depthLeft <= 2 &&
                (evaluator.evaluate(_board) * _board.getActivePlayer()) <= alpha - razor_margin) {

                if (depthLeft == 1) {
                    return qSearch(alpha, beta, currentDepth,0);
                }
                double rWindow = alpha - razor_margin;
                double value = qSearch(rWindow, rWindow + 1, currentDepth,0);
                if (value <= rWindow) {
                    return value;
                }
            }
        }


        /**
         * null move pruning
         */
        if (use_null_moves) {
            if(!pv && !isInCheck){
                _board.move_null();
                score = -pvSearch(-alpha-1, -alpha, currentDepth+1, depthLeft-1-null_move_reduction, false, false);
                _board.undoMove_null();
                if (score >= beta) {
                    return beta;
                }
            }

        }


        /**
         * generating all legal moves and sorting them
         */
        List<Move> allMoves = _board.getPseudoLegalMoves(_buffer.get(currentDepth));
        if(allMoves.size() == 0){
            return eval;
        }
        orderer.sort(allMoves, currentDepth, null, _board, pv, _killerTable,_historyTable, _transpositionTable);

        /**
         * calculate SEE value
         */
        for(Move m:allMoves){
            if(m.getType() == Move.DEFAULT)
                m.setSeeScore(getSEE(m));
        }

        /**
         * looping over all moves
         */
        for (Move m:allMoves)  {

            if(!_board.isLegal(m)){
                continue;
            }



            boolean givesCheck = _board.givesCheck(m);
            boolean moveCanBePruned =
                    !pv &&
                            !m.isCapture() &&
                            !m.isPromotion() &&
                            !givesCheck;


            //TODO: futility pruning
            /**
             * futility pruning. if eval is way smaller than alpha it probably wont raise alpha
             */
            if (
                    moveCanBePruned
                    && !isInCheck
                    && depthLeft <= 1
                    && Math.abs(alpha) < MIN_CHECKMATE_VALUE
                    && Math.abs(beta)  < MIN_CHECKMATE_VALUE
                    && eval <= alpha - futility_pruning_margin
                ){
                continue;
            }

            /**
             * if the SEE value of the capture is too small, dont bother this move.
             */
            if (!pv
                    && depthLeft <= 4
                    && m.isCapture()
                    && m.getSeeScore() < -100){
                continue;
            }



            int reduction = use_LMR ? reducer.reduce(_board, m, currentDepth, depthLeft, legalMoves, pv) : 0;
            int extensions = _board.givesCheck(m) & m.getSeeScore() >= 0 ? 1:0;

            _board.move(m);

            if (debug && currentDepth == 0){
                score = -pvSearch(- Double.POSITIVE_INFINITY, -Double.NEGATIVE_INFINITY, currentDepth+1, depthLeft-1-reduction+extensions, true, false);
                System.out.format("%-6s %10.1f %n",UCI.moveToUCI(m, _board), score);
            }else{
                if (legalMoves == 0 && pv) {
                    score = -pvSearch(-beta, -alpha, currentDepth+1, depthLeft-1-reduction+extensions, true, false);
                } else {
                    score = -pvSearch(-alpha-1, -alpha, currentDepth+1, depthLeft-1-reduction+extensions, false, false);
                    if (score > alpha && score < beta && pv) // in fail-soft ... && score < beta ) is common
                        score = -pvSearch(-beta, -alpha, currentDepth+1, depthLeft-1-reduction+extensions, true,false); // re-search
                }

            }


            _board.undoMove();



            legalMoves++;


            /**
             * beta cutoff
             */
            if( score >= beta       ){
                if(use_killer_heuristic && m.getPieceTo() == 0)     _killerTable.put(currentDepth, m.copy());
                if(use_history_heuristic)                           _historyTable.add(depthLeft*depthLeft,m.getFrom(), m.getTo());
                if(use_transposition)                               placeInTT(zobrist, currentDepth, depthLeft, beta, TranspositionEntry.CUT_NODE, m.copy());
                _betaCutoffs ++;
                return beta;   // fail-hard beta-cutoff
            }

            /**
             * keeping track of the best move
             */
            if( score > highestScore){
                highestScore = score;
                bestMove = m.copy();
            }

            /**
             * raising alpha /pv node
             */
            if( score > alpha       ){
                alpha = score; // alpha acts like max in MiniMax
                bestMove = m.copy();
            }

        }

        /**
         * if there are no leg
         */
        if(legalMoves == 0){
            if(_board.isInCheck(_board.getActivePlayer())){
                //checkmate
                return -MAX_CHECKMATE_VALUE+currentDepth;
            }else{
                //stalemate
                return 0;
            }
        }

        /**
         * storing in the TT
         */
        if(bestMove != null){
            if (pv && highestScore >= alpha && highestScore <= beta) {
                placeInTT(zobrist, currentDepth, depthLeft, highestScore, TranspositionEntry.PV_NODE, bestMove);
            } else {
                if (use_transposition) {
                    placeInTT(zobrist, currentDepth, depthLeft, alpha, TranspositionEntry.ALL_NODE, bestMove);
                }
            }
        }

        return alpha;
    }

    /**
     * qSearch initiated with depthLeft = 0
     * @param alpha
     * @param beta
     * @param currentDepth
     * @param depthLeft
     * @return
     */
    public double qSearch(double alpha, double beta, int currentDepth, int depthLeft) {



        _nodes ++;
        _qSearchNodes ++;
        if(_board.isDraw()){
            return 0;
        }

        double      stand_pat       = evaluator.evaluate(_board) * _board.getActivePlayer();
        Move        bestMove        = null;
        double      bestScore       = Double.NEGATIVE_INFINITY;
        double      origonalAlpha   = alpha;
        long        zobrist         = _board.zobrist();

        /**
         * if no quiescene shall be used at all
         */
        if(!use_qSearch){
            return stand_pat;
        }

        /**
         * delta pruning check if there is no way that any move could increase alpha
         *
         */
        if(use_delta_pruning){
            if(stand_pat < alpha-delta_pruning_big_margin){
                return alpha;
            }
        }

        List<Move> allMoves = _board.getCaptureMoves(_buffer.get(currentDepth));
        if (allMoves.size() == 0){
            return stand_pat;
        }
        if (stand_pat >= beta){
            return beta;
        }
        if (alpha < stand_pat)
            alpha = stand_pat;


        orderer.sort(allMoves, 0, null, _board, false, _killerTable, _historyTable, null);
        int legalMoves = 0;
        for (Move m : allMoves) {

            if(!_board.isLegal(m)) continue;


            /**
             * delta pruning check if there is no way that any move could increase alpha
             *
             */
            if(use_delta_pruning){
                if(stand_pat+delta_pruning_captures[Math.abs(m.getPieceTo())] < alpha-delta_pruning_big_margin){
                    continue;
                }
            }
            /**
             * prune moves with SEE < 0
             */
            if(getSEE(m) < 0){
                continue;
            }

            _board.move(m);
            double score = -qSearch(-beta, -alpha, currentDepth+1, depthLeft);
            _board.undoMove();

            legalMoves ++;


            if (score >= beta) {
                return beta;
            }
            if (score > alpha){
                alpha = score;
//                bestMove = m.copy();
            }
                if (score > bestScore){
                bestScore = score;
//                bestMove = m.copy();
            }
        }


        if(legalMoves == 0 ){
            return stand_pat;
        }


//        if(!_board.isInCheck(_board.getActivePlayer())){
//            if(legalMoves == 0){
//                return alpha;
//            }
//        }


//        allMoves = _board.getPseudoLegalMoves(_buffer.get(currentDepth));
//
//        for (Move m : allMoves) {
//
//            if(!_board.isLegal(m)) continue;
//
//            _board.move(m);
//            double score = -qSearch(-beta, -alpha, currentDepth+1, depthLeft);
//            _board.undoMove();
//
//            legalMoves ++;
//
//            if (score >= beta) {
//                return beta;
//            }
//            if (score > alpha){
//                alpha = score;
//            }
//            if (score > bestScore){
//                bestScore = score;
//            }
//        }






//        if(legalMoves == 0){
//            if(!_board.isInCheck(_board.getActivePlayer())){
//                return stand_pat;
//            }
//
//        }



//        if(bestMove != null){
//            if (alpha <= origonalAlpha) {
//                placeInTT(zobrist, currentDepth, depthLeft, alpha, TranspositionEntry.ALL_NODE, bestMove);
//            }
//        }

        return alpha;
    }

    public double qSearch(Board board){
        this._board             = board;

        return qSearch(-1000000,1000000, 0,0) * board.getActivePlayer();
    }

    /**
     * returns the bestMove for the board
     * @param board     the current board that stores information about pieces etc.
     * @return
     */
    @Override
    public Move bestMove(Board board) {

        this._board             = board;
        this._killerTable       = use_killer_heuristic  ? new KillerTable(MAXIMUM_STORE_DEPTH, killer_count)    :null;
        this._historyTable      = use_history_heuristic ? new HistoryTable()                                    :null;

        if(this._transpositionTable == null)    this._transpositionTable = new TranspositionTable<>();
        else                                    this._transpositionTable.clear();

        /**
         * without iterations
         */
        if(!use_iteration && limit_flag == FLAG_DEPTH_LIMIT){

            iteration(limit);

            return _transpositionTable.get(board.zobrist()).getBestMove();
        }

        /**
         * with iterations/time limit
         */
        if(limit_flag == FLAG_TIME_LIMIT){

            long time = System.currentTimeMillis();
            int depth = deepening_start_depth;
            long prevTime = System.currentTimeMillis();
            long prevNode = 1;
            double branchingFactor;
            double expectedTime = 0;
            while (System.currentTimeMillis() - time + expectedTime < limit) {
                iteration(depth++);

                long iterationTime = System.currentTimeMillis() - prevTime;
                prevTime = System.currentTimeMillis();
                branchingFactor = Math.min(2, (double) (_nodes) / prevNode);
                expectedTime = branchingFactor * iterationTime;
                if (depth > MAXIMUM_STORE_DEPTH/2) {
                    break;
                }
            }

        }
        /**
         * with iterations but depth limit
         */
        else{
            if(limit > MAXIMUM_STORE_DEPTH/2){
                limit = MAXIMUM_STORE_DEPTH/2;
            }
            for(int i = Math.min(limit, deepening_start_depth); i <= limit; i++){
                iteration(i);
            }
        }

        /**
         * returning the best move stored in the TT.
         */
        Move m = _transpositionTable.get(board.zobrist()).getBestMove();
        _transpositionTable.clear();
        return m;
    }

    public void iteration(int depth) {

        /**
         * resetting some basic search information
         */

        _pvSearchNodes = 0;
        _qSearchNodes = 0;
        _transpositionHits = 0;

        _nodes = 0;
        _selDepth = 0;



        /**
         * initiating time measurements
         */
        long t0 = System.currentTimeMillis();


        /**
         * calculating bounds for the search.
         * If aspiration shall be used, aspiration windows will be initiated
         */
        double alphaInc;
        double betaInc;

        if(use_aspiration && depth > 2){
            alphaInc = -25;
            betaInc = 25;
        }else{
            alphaInc =Double.NEGATIVE_INFINITY;
            betaInc = Double.POSITIVE_INFINITY;
        }

        double pvResult = pvSearch(alphaInc+_score, betaInc+_score, 0, depth, true, false);

        //System.out.println("attempted with: <" + (alphaInc+_score) + "|" + (_score + betaInc)+">" + " -> " + pvResult);

        /**
         * this loop will only be entered if the previous search either failed
         */
        while (pvResult <= (alphaInc+_score) || pvResult >= (_score+betaInc)){
            if(pvResult <= (alphaInc+_score)){
                alphaInc*=4;
            }
            if(pvResult >= (betaInc+_score)){
                betaInc*=4;
            }

            pvResult = pvSearch(alphaInc+_score, betaInc+_score, 0, depth, true, false);
            //System.out.println("attempted with: <" + (alphaInc+_score) + "|" + (_score + betaInc)+">" + " -> " + pvResult);
        }

        _score = pvResult;

        /**
         * printing the infoString, sending the infoString to UCI (for logging)
         */

        String infoString = buildInfoString(depth, System.currentTimeMillis()-t0);
        System.out.println(infoString);
        System.out.println(_transpositionTable.size());

        UCI.log(infoString+"\n");
    }

    /**
     * places a transposition in the transposition table.
     * It will not override PV-entries if the new value itself isnt a pv-entry.
     * It will also not override values if the depthLeft of the new value is smaller than the old one
     *
     * @param zobrist
     * @param depth
     * @param depthLeft
     * @param alpha
     * @param type
     * @param bestMove
     */
    public void placeInTT(long zobrist, int depth, int depthLeft, double alpha, int type, Move bestMove){
        TranspositionEntry en = _transpositionTable.get(zobrist);

        if(en != null && en.getNode_type() == TranspositionEntry.PV_NODE && type != TranspositionEntry.PV_NODE) return;

        if(en != null){
            if(en.getDepthLeft() > depthLeft){
                return;
            }
        }

        _transpositionTable.put(zobrist, new TranspositionEntry(zobrist, alpha, depthLeft, type, _board.getActivePlayer(), bestMove));
    }

    /**
     * checks if there is a tt-entry for the given zobrist key.
     * It checks if the depthLeft of that stored entry is >= than the current depthLeft and the color matches on the board
     * @param zobrist
     * @param depth
     * @param depthLeft
     * @return
     */
    public TranspositionEntry retrieveFromTT(long zobrist, int depth, int depthLeft){
        TranspositionEntry en = _transpositionTable.get(zobrist);

        if(en != null && en.getDepthLeft() >= depthLeft && en.getZobrist() == zobrist && en.getColor() == _board.getActivePlayer()){
            //System.out.println("retrieved");
            return en;
        }
        return null;
    }

    /**
     * calculates the SEE score for the given move using the internal board object.
     * @param m
     * @return
     */
    public int getSEE(Move m){
        return (int)evaluator.staticExchangeEvaluation(_board, m.getTo(), m.getPieceTo(), m.getFrom(), m.getPieceFrom(), _board.getActivePlayer());
    }

    /**
     * returns a string containing standardised information
     * @return
     */
    public String buildInfoString(int depth, long time){
        StringBuilder builder = new StringBuilder();


        builder.append("info ");
        builder.append("depth "         + depth                         + " ");
        builder.append("seldepth "      + _selDepth                     + " ");

        builder.append("score ");
        builder.append("cp "            + (int)_score                   + " ");
        if(Math.abs(_score) > MIN_CHECKMATE_VALUE){
            builder.append("mate "  + (int)(MAX_CHECKMATE_VALUE-Math.abs(_score))/2   + " ");
        }

        builder.append("nodes "         + _nodes                        + " ");

        //TODO
        builder.append("nps "           + _nodes/Math.max(1,time)*1000  + " ");
        builder.append("tbhits "        + 0                             + " ");
        builder.append("time "          + time                          + " ");
        builder.append("pv "            + extractPV());

        return builder.toString();
    }

    /**
     * returns a string containing non standardised information for debugging purpose
     * @return
     */
    public String buildMetaInfoString(){
        StringBuilder builder = new StringBuilder();

        builder.append("info string");

        builder.append(" pvSearchNodes " + _pvSearchNodes);
        builder.append(" qSearchNodes " + _qSearchNodes);
        builder.append(" tt hits " + _transpositionHits);
        builder.append(" beta cutoffs " + _betaCutoffs);

        return builder.toString();
    }

    /**
     * extracts the pv line from the tt table.
     *
     * @return
     */
    public String extractPV(){
        StringBuilder builder = new StringBuilder();
        TranspositionEntry en = _transpositionTable.get(_board.zobrist());

        int counter = 0;

        while(en != null && counter < _selDepth){
            if(en.getColor() != _board.getActivePlayer()) break;
            counter++;
            builder.append(UCI.moveToUCI(en.getBestMove(), _board) + " ");
            _board.move(en.getBestMove());
            en = _transpositionTable.get(_board.zobrist());
        }

        for(int i = 0; i < counter; i++){
            _board.undoMove();
        }

        return builder.toString();
    }


    public static void main(String[] args) {
        FastBoard fb = new FastBoard(Setup.DEFAULT);


        fb = IO.read_FEN(fb, "r3kbr1/1p1q1p1p/3p2p1/pNn1p2n/P1Q1P3/1N6/1PPB1PPP/R3R1K1 w q -");

        //        AdvancedSearch advancedSearch = new AdvancedSearch(
//                new AdvancedEvaluator(new SimpleDecider()),
//                new SystematicOrderer2(),
//                new SenpaiReducer(1), 1, 0);

        //advancedSearch.bestMove()

//        advancedSearch.use_history_heuristic = true;
//        advancedSearch.use_transposition = false;
        //advancedSearch.bestMove(fb);




        AdvancedSearch advancedSearch = new AdvancedSearch(
                new AdvancedEvaluator(new SimpleDecider()),
                new SystematicOrderer2(),
                new SenpaiReducer(5), 2, 10);

        advancedSearch.setUse_delta_pruning(true);
        advancedSearch.setDeepening_start_depth(1);
        advancedSearch.setUse_futility_pruning(true);

        advancedSearch.bestMove(fb);



//        FastBoard finalFb = fb;
//        new Frame(fb,new Player() {},  advancedSearch).setFlippedBoard(true);
//


    }
}
