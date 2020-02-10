package ai.search;

import ai.evaluator.Evaluator;
import ai.evaluator.LateGameEvaluator;
import ai.ordering.Orderer;
import board.Board;
import board.FastBoard;
import board.moves.Move;
import board.moves.MoveListBuffer;

import java.util.List;

public class AdvancedSearch {



    private Evaluator evaluator;
    private Orderer orderer;

    private Board _board;
    private MoveListBuffer _buffer;

    private int qDepth;

    private double pvSearch(double alpha, double beta,int currentDepth, int depthLeft) {
        List<Move> allMoves = _board.getCaptureMoves(_buffer.get(currentDepth));
        if(allMoves == null){
            return Double.NaN;
        }
        if(depthLeft == 0 || _board.isGameOver()) {
            return qSearch(alpha, beta, currentDepth, qDepth);
        }
        int legalMoves = 0;
        double score;
        for (Move m:allMoves)  {
            _board.move(m);
            if (legalMoves == 0) {
                score = -pvSearch(-beta, -alpha, currentDepth+1, depthLeft-1);
            } else {
                score = -zwSearch(-alpha, currentDepth+1, depthLeft-1);
                if ( score > alpha && score < beta) // in fail-soft ... && score < beta ) is common
                    score = -pvSearch(-beta, -alpha, currentDepth+1, depthLeft-1); // re-search
            }
            _board.undoMove();
            if( score >= beta )
                return beta;   // fail-hard beta-cutoff
            if( score > alpha ) {
                alpha = score; // alpha acts like max in MiniMax
            }
        }

        //check for gameover
        if(legalMoves == 0){
            if(_board.isAtCheck(_board.getActivePlayer())){
                //checkmate
                return -LateGameEvaluator.INFTY;
            }else{
                //stalemate
                return 0;
            }
        }

        return alpha;
    }

    // fail-hard zero window search, returns either beta-1 or beta
    private double zwSearch(double beta, int currentDepth, int depthLeft ) {
        // alpha == beta - 1
        // this is either a cut- or all-node
        List<Move> allMoves = _board.getPseudoLegalMoves(_buffer.get(currentDepth));
        if(allMoves == null){
            return Double.NaN;
        }
        if( depthLeft == 0 ) {
            return qSearch(beta-1, beta, currentDepth, qDepth);
        }
        int legalMoves = 0;
        for (Move m:allMoves)  {
            _board.move(m);
            double score = -zwSearch(1-beta, currentDepth+1, depthLeft - 1);
            _board.undoMove();
            if(Double.isNaN(score)){
                continue;
            }
            if( score >= beta )
                return beta;   // fail-hard beta-cutoff
        }
        return beta-1; // fail-hard, return alpha
    }


    /**
     * Qsearch after main search
     * @param alpha         lower limit
     * @param beta          upper limit
     * @param depthLeft     depth left until stop
     * @return
     */
    private double qSearch(double alpha, double beta, int currentDepth, int depthLeft) {

        List<Move> allMoves = _board.getCaptureMoves(_buffer.get(currentDepth));

        if(allMoves == null){
            return Double.NaN;
        }

        double stand_pat = evaluator.evaluate(_board) * _board.getActivePlayer();

        //3-fold reps
        if (_board.isGameOver() || allMoves.size() == 0){
            return stand_pat;
        }
        if (depthLeft == 0) {
            return stand_pat;
        }
        if (stand_pat >= beta){
            return beta;
        }
        if (alpha < stand_pat)
            alpha = stand_pat;

        orderer.sort(allMoves, 0, null, _board, false, null, null);
        int legalMoves = 0;         //count the amount of legal captures.
        // if its 0, we do a full research with all moves (most of them will be illegal as well)
        for (Move m : allMoves) {

            _board.move(m);
            double score = -qSearch(-beta, -alpha, currentDepth + 1, depthLeft - 1);
            _board.undoMove();

            if(!Double.isNaN(score)){
                legalMoves ++;
                continue;
            }

            if (score >= beta) {
                return beta;
            }
            if (score > alpha)
                alpha = score;

        }

        //full research
        if(legalMoves == 0){
            allMoves = _board.getPseudoLegalMoves(_buffer.get(currentDepth));
            for (Move m : allMoves) {
                _board.move(m);
                double score = -qSearch(-beta, -alpha, currentDepth + 1, depthLeft - 1);
                _board.undoMove();

                if(!Double.isNaN(score)){
                    legalMoves ++;
                    continue;
                }

                if (score >= beta) {
                    return beta;
                }
                if (score > alpha)
                    alpha = score;

            }
        }

        //check for gameover
        if(legalMoves == 0){
            if(_board.isAtCheck(_board.getActivePlayer())){
                //checkmate
                return -LateGameEvaluator.INFTY;
            }else{
                //stalemate
                return 0;
            }
        }


        return alpha;
    }

}
