package ai.ordering;

import ai.tools.tables.CounterMoveTable;
import ai.tools.tables.KillerTable;
import ai.tools.transpositions.TranspositionEntry;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.bitboards.BitBoard;
import board.moves.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChiefOrderer implements Orderer {


    ArrayList<Move> pvMoves = new ArrayList<>(10);
    ArrayList<Move> mvvlvaCaps = new ArrayList<>(10);
    ArrayList<Move> seeCaps = new ArrayList<>(10);
    ArrayList<Move> killerMoves = new ArrayList<>(10);
    ArrayList<Move> pawnThreats = new ArrayList<>(10);
    ArrayList<Move> goodChecks = new ArrayList<>(10);
    ArrayList<Move> badCaptureMoves = new ArrayList<>(20);
    ArrayList<Move> nonCaptureMoves = new ArrayList<>(20);



    @Override
    public void sort(
            List<Move> collection,
            int depth,
            Board board,
            boolean pvNode,
            KillerTable killerTable,
            TranspositionTable transpositionTable,
            CounterMoveTable counterMoveTable) {



        int initSize = collection.size();

        pvMoves.clear();

        mvvlvaCaps.clear();
        seeCaps.clear();

        killerMoves.clear();

        pawnThreats.clear();
        goodChecks.clear();

        badCaptureMoves.clear();
        nonCaptureMoves.clear();


        int ttIndex = TTMoveIndex(collection, transpositionTable, board);
        if(ttIndex != -1){
            pvMoves.add(collection.get(ttIndex));
            collection.remove(ttIndex);
        }

        for(Move m:collection){

            int mvv_lva = mvv_lva(m);

            if(m.isCapture() && mvv_lva > 0){
                m.setOrderPriority(mvv_lva);
                mvvlvaCaps.add(m);
                continue;
            }

            if(m.isCapture() && m.getSeeScore() >= 0){
                m.setOrderPriority(m.getSeeScore());
                seeCaps.add(m);
                continue;
            }

            if(killerTable.isKillerMove(depth, m)){
                killerMoves.add(m);
                continue;
            }

            if(m.getSeeScore() >= 0 &&
               (m.getPieceFrom() == 1 && BitBoard.rankIndex(m.getTo()) >= 6 ||
                m.getPieceFrom() == -1 && BitBoard.rankIndex(m.getTo()) <= 2)
            ){
                m.setOrderPriority(m.getSeeScore());
                pawnThreats.add(m);
                continue;
            }

            if(m.getSeeScore() >= 0 && board.givesCheck(m)){
                m.setOrderPriority(m.getSeeScore());
                goodChecks.add(m);
                continue;
            }

            if(m.isCapture()){
                badCaptureMoves.add(m);
                continue;
            }


            nonCaptureMoves.add(m);
            //m.setOrderPriority((int)historyTable.get(m.getFrom(),m.getTo()));
        }

        nonCaptureMoves.sort(Comparator.comparingInt(Move::getOrderPriority));

        collection.clear();
        collection.addAll(pvMoves);
        collection.addAll(mvvlvaCaps);
        collection.addAll(seeCaps);
        collection.addAll(killerMoves);
        collection.addAll(pawnThreats);
        collection.addAll(goodChecks);
        collection.addAll(badCaptureMoves);
        collection.addAll(nonCaptureMoves);

        if(collection.size() != initSize){
            throw new RuntimeException("This is thrown if the initial amount of moves has changed during ordering (which should'nt be obviously)");
        }

    }


    private int[] mvvlvaScores = new int[]{0,100,500, 300,315,900,10000};

    private int mvv_lva(Move m){
        return mvvlvaScores[Math.abs(m.getPieceTo())] -  mvvlvaScores[Math.abs(m.getPieceFrom())];
    }


    /**
     * finds the index of the move in the collection which is contained in the tt
     * @param collection
     * @param table
     * @param board
     * @return
     */
    public int TTMoveIndex(List<Move> collection, TranspositionTable table, Board board){
        if (table != null) {
            TranspositionEntry en = (TranspositionEntry) table.get(board.zobrist());
            if (en != null) {
                Move hashMove = en.getBestMove();
                int index = collection.indexOf(hashMove);
                if (hashMove != null && index != -1) {
                    return index;
                }
            }
        }
        return -1;
    }
}
