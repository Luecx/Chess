package ai.search;

import ai.evaluator.AdvancedEvaluator;
import ai.evaluator.decider.SimpleDecider;
import ai.evaluator.Evaluator;
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
import visual.game.Player;
import io.UCI;
import visual.Frame;

import java.util.*;

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

    protected boolean                                   print_overview          = true;         //flag for output-printing


    protected int                                       killer_count            = 3;            //amount of killer moves
    protected int                                       null_move_reduction     = 2;            //how much to reduce null moves
    protected int                                       razor_offset            = 300;



    private KillerTable                                 _killerTable;
    private HistoryTable                                _historyTable;
    private TranspositionTable<TranspositionEntry>      _transpositionTable;
    private Board                                       _board;
    private MoveListBuffer                              _buffer;

    private double                                      _score;
    private int                                         _nodes;
    private int                                         _selDepth;

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
    public int getRazor_offset() {
        return razor_offset;
    }

    /**
     * sets the offset for razoring
     * @param razor_offset
     */
    public void setRazor_offset(int razor_offset) {
        this.razor_offset = razor_offset;
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

    private double pvSearch(double alpha, double beta, int currentDepth, int depthLeft, boolean pv, boolean extension) {
        _selDepth = Math.max(_selDepth, currentDepth);



        long        zobrist         = _board.zobrist();
        double      origonalAlpha   = alpha;
        double      score           = Double.NEGATIVE_INFINITY;
        double      highestScore    = score;
        double      eval            = evaluator.evaluate(_board) * _board.getActivePlayer();
        int         legalMoves      = 0;
        Move        bestMove        = null;


        if(_board.isDraw()){
            _nodes ++;
            return 0;
        }
        if(depthLeft <= 0) {
            return qSearch(alpha, beta, currentDepth,0);
        }

        _nodes ++;




        if (use_transposition) {
            TranspositionEntry tt = retrieveFromTT(zobrist, currentDepth, depthLeft);
            if(tt != null){
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
                    if (tt.getVal() <= alpha) return alpha;
                    if(beta > tt.getVal() && !pv){
                        beta = tt.getVal();
                    }
                }
            }
        }


        if (use_razoring) {
            if (!extension &&
                !pv &&
                !_board.isInCheck(_board.getActivePlayer()) &&
                depthLeft <= 2 &&
                (evaluator.evaluate(_board) * _board.getActivePlayer()) <= alpha - razor_offset) {

                if (depthLeft == 1) {
                    return qSearch(alpha, beta, currentDepth,0);
                }
                double rWindow = alpha - razor_offset;
                double value = qSearch(rWindow, rWindow + 1, currentDepth,0);
                if (value <= rWindow) {
                    return value;
                }
            }
        }


        if (use_null_moves) {
            if(!pv && !_board.isInCheck(_board.getActivePlayer())){
                _board.move_null();
                score = -pvSearch(-alpha-1, -alpha, currentDepth+1, depthLeft-1-null_move_reduction, false, false);
                _board.undoMove_null();
                if (score >= beta) {
                    return beta;
                }
            }

        }


        List<Move> allMoves = _board.getPseudoLegalMoves(_buffer.get(currentDepth));
        if(allMoves.size() == 0){
            return eval;
        }
        orderer.sort(allMoves, currentDepth, null, _board, pv, _killerTable,_historyTable, _transpositionTable);

        for (Move m:allMoves)  {

            if(!_board.isLegal(m)){
                continue;
            }

            int reduction = use_LMR ? reducer.reduce(_board, m, currentDepth, depthLeft, legalMoves, pv) : 0;
            int extensions = _board.givesCheck(m) ? 1:0;

            _board.move(m);
            if (legalMoves == 0) {
                score = -pvSearch(-beta, -alpha, currentDepth+1, depthLeft-1-reduction+extensions, pv, false);
            } else {
                score = -pvSearch(-alpha-1, -alpha, currentDepth+1, depthLeft-1-reduction+extensions, false, false);
                if (score > alpha && score < beta) // in fail-soft ... && score < beta ) is common
                    score = -pvSearch(-beta, -alpha, currentDepth+1, depthLeft-1-reduction+extensions, false,false); // re-search
            }
            _board.undoMove();



            legalMoves++;
            if( score >= beta       ){
                if(use_killer_heuristic && m.getPieceTo() == 0)     _killerTable.put(currentDepth, m.copy());
                if(use_history_heuristic)                           _historyTable.add(depthLeft*depthLeft,m.getFrom(), m.getTo());
                if(use_transposition)                               placeInTT(zobrist, currentDepth, depthLeft, beta, TranspositionEntry.CUT_NODE, m.copy());
                return beta;   // fail-hard beta-cutoff
            }
            if( score > highestScore){
                highestScore = score;
                bestMove = m.copy();
            }
            if( score > alpha       ){
                alpha = score; // alpha acts like max in MiniMax
                bestMove = m.copy();
            }

        }

        //check for gameover
        if(legalMoves == 0){
            if(_board.isInCheck(_board.getActivePlayer())){
                //checkmate
                return -MAX_CHECKMATE_VALUE+currentDepth;
            }else{
                //stalemate
                return 0;
            }
        }

        if(bestMove != null){
            if (alpha > origonalAlpha) {
                placeInTT(zobrist, currentDepth, depthLeft, alpha, TranspositionEntry.PV_NODE, bestMove);
            } else {
                if (use_transposition) {
                    placeInTT(zobrist, currentDepth, depthLeft, alpha, TranspositionEntry.ALL_NODE, bestMove);
                }
            }
        }

        return alpha;
    }

    public double qSearch(double alpha, double beta, int currentDepth, int depthLeft) {



        _nodes ++;
        if(_board.isDraw()){
            return 0;
        }

        double      stand_pat       = evaluator.evaluate(_board) * _board.getActivePlayer();
        Move        bestMove        = null;
        double      origonalAlpha   = alpha;
        long        zobrist         = _board.zobrist();

        if(!use_qSearch){
            return stand_pat;
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
        int legalMoves = 0;         //count the amount of legal captures.
        // if its 0, we do a full research with all moves (most of them will be illegal as well)
        for (Move m : allMoves) {

            if(!_board.isLegal(m)) continue;

            _board.move(m);
            double score = -qSearch(-beta, -alpha, currentDepth+1, depthLeft);
            _board.undoMove();

            legalMoves ++;


            if (score >= beta) {
//                if (use_transposition) {
//                    placeInTT(zobrist, currentDepth, depthLeft, beta, TranspositionEntry.CUT_NODE, bestMove);
//                }
                return beta;
            }
            if (score > alpha){
//                bestMove = m.copy();
                alpha = score;
            }

        }


        if(legalMoves == 0){
            return stand_pat;
        }



        if(bestMove != null){
            if (alpha > origonalAlpha) {
                placeInTT(zobrist, currentDepth, depthLeft, alpha, TranspositionEntry.PV_NODE, bestMove);
            } else {
//                if (use_transposition) {
//                    placeInTT(zobrist, currentDepth, depthLeft, alpha, TranspositionEntry.ALL_NODE, bestMove);
//                }
            }
        }



//        //full research
//        if(legalMoves == 0){
//            allMoves = _board.getPseudoLegalMoves(_buffer.get(currentDepth));
//            for (Move m : allMoves) {
//                _board.move(m);
//                double score = -qSearch(-beta, -alpha, currentDepth + 1, depthLeft - 1);
//                _board.undoMove();
//
//                if(!Double.isNaN(score)){
//                    legalMoves ++;
//                    continue;
//                }
//
//                if (score >= beta) {
//                    return beta;
//                }
//                if (score > alpha)
//                    alpha = score;
//
//            }
//        }
//
//        //check for gameover
//        if(legalMoves == 0){
//            if(_board.isInCheck(_board.getActivePlayer())){
//                //checkmate
//                return -LateGameEvaluator.INFTY;
//            }else{
//                //stalemate
//                return 0;
//            }
//        }


        return alpha;
    }

    public double qSearch(Board board){
        this._board             = board;
//        this._killerTable       = use_killer_heuristic  ? new KillerTable(MAXIMUM_STORE_DEPTH, killer_count)    :null;
//        this._historyTable      = use_history_heuristic ? new HistoryTable()                                    :null;
//        if(this._transpositionTable == null)    this._transpositionTable = new TranspositionTable<>();
//        else                                    this._transpositionTable.clear();

        return qSearch(-1000000,1000000, 0,0) * board.getActivePlayer();
    }

    @Override
    public Move bestMove(Board board) {

        this._board             = board;
        this._killerTable       = use_killer_heuristic  ? new KillerTable(MAXIMUM_STORE_DEPTH, killer_count)    :null;
        this._historyTable      = use_history_heuristic ? new HistoryTable()                                    :null;

        if(this._transpositionTable == null)    this._transpositionTable = new TranspositionTable<>();
        else                                    this._transpositionTable.clear();



        if(!use_iteration && limit_flag == FLAG_DEPTH_LIMIT){
            long time = System.currentTimeMillis();
            iteration(limit);
            String infoString = buildInfoString(limit, System.currentTimeMillis()-time);
            System.out.println(infoString);
            UCI.log(infoString+"\n");

            return _transpositionTable.get(board.zobrist()).getBestMove();
        }

        if(limit_flag == FLAG_TIME_LIMIT){
            long time = System.currentTimeMillis();
            int depth = 1;
            long prevTime = System.currentTimeMillis();
            long prevNode = 1;
            double branchingFactor;
            double expectedTime = 0;
            while (System.currentTimeMillis() - time + expectedTime < limit) {
                long t0 = System.currentTimeMillis();
                iteration(depth++);
                String infoString = buildInfoString(depth-1, System.currentTimeMillis()-t0);
                System.out.println(infoString);
                UCI.log(infoString+"\n");

                long iterationTime = System.currentTimeMillis() - prevTime;
                prevTime = System.currentTimeMillis();
                branchingFactor = Math.min(2, (double) (_nodes) / prevNode);
                expectedTime = branchingFactor * iterationTime;
                if (depth > MAXIMUM_STORE_DEPTH/2) {
                    break;
                }
            }
        }else{
            if(limit > MAXIMUM_STORE_DEPTH/2){
                limit = MAXIMUM_STORE_DEPTH/2;
            }
            for(int i = 1; i <= limit; i++){
                long t0 = System.currentTimeMillis();
                iteration(i);
                String infoString = buildInfoString(i,System.currentTimeMillis()-t0);
                System.out.println(infoString);
                UCI.log(infoString+"\n");

            }
        }

        return _transpositionTable.get(board.zobrist()).getBestMove();
    }

    public void iteration(int depth) {
        _nodes = 0;
        _selDepth = 0;

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


        //_score = pvSearch(Double.NEGATIVE_INFINITY,  Double.POSITIVE_INFINITY, 0, depth, true, false);
    }

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

    public TranspositionEntry retrieveFromTT(long zobrist, int depth, int depthLeft){
        TranspositionEntry en = _transpositionTable.get(zobrist);

        if(en != null && en.getDepthLeft() >= depthLeft && en.getZobrist() == zobrist && en.getColor() == _board.getActivePlayer()){
            //System.out.println("retrieved");
            return en;
        }
        return null;
    }

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


        fb = IO.read_FEN(fb, "7k/8/8/8/8/8/8/K7");


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
                new SenpaiReducer(1), 2, 10);

        advancedSearch.setUse_transposition(false);
        advancedSearch.setUse_aspiration(false);

        advancedSearch.bestMove(fb);



//        FastBoard finalFb = fb;
//        new Frame(fb,new Player() {},  advancedSearch).setFlippedBoard(true);
//


    }
}
