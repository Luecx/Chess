package ai.ordering;

import ai.evaluator.AdvancedMidGameEvaluator;
import ai.tools.tables.CounterMoveTable;
import ai.tools.tables.KillerTable;
import ai.tools.transpositions.TranspositionEntry;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.moves.Move;

import java.util.ArrayList;
import java.util.List;

public class SystematicOrderer2 implements Orderer {



    ArrayList<Move> pvMoves = new ArrayList<>(10);
    ArrayList<Move> goodCaptures = new ArrayList<>(10);
    ArrayList<Move> killerMoves = new ArrayList<>(10);
    ArrayList<Move> badCaptures = new ArrayList<>(10);
    ArrayList<Move> nonCaptureMoves = new ArrayList<>(20);

    @Override
    public void sort(
            List<Move> collection,
            int depth,
            Board board,
            boolean pvNode,
            KillerTable killerTable,
            TranspositionTable transpositionTable,
            CounterMoveTable counterMoveTable){

        int initSize = collection.size();



        pvMoves.clear();
        goodCaptures.clear();
        killerMoves.clear();
        badCaptures.clear();
        nonCaptureMoves.clear();

        Move lastMove = null;
        if(!board.getMoveHistory().isEmpty()){
            lastMove = (Move) board.getMoveHistory().lastElement();
        }


        long zobrist = board.zobrist();
        if (transpositionTable != null && pvMoves.size() == 0) {
            TranspositionEntry en = (TranspositionEntry) transpositionTable.get(zobrist);
            if (en != null) {
                Move hashMove = en.getBestMove();
                int index = collection.indexOf(hashMove);
                if (hashMove != null && index != -1) {
                    pvMoves.add(collection.get(index));
                    collection.remove(index);
                }
            }
        }


        //capture moves / non capture / killers
        for(Move m:collection){
            if (!m.isCapture() && counterMoveTable != null && lastMove != null) {
                m.setOrderPriority((int)counterMoveTable.get(
                        Math.abs(lastMove.getPieceFrom())-1,
                        lastMove.getTo(),
                        Math.abs(m.getPieceFrom())-1,
                        m.getTo()));
            } else {
                NoahOrderer.setOrderPriority(m, board);
            }

            if (killerTable != null && killerTable.isKillerMove(depth, m)) {
                this.killerMoves.add(m);
            } else if (m.getPieceTo() != 0) {
                if (AdvancedMidGameEvaluator.EVALUATE_PRICE[Math.abs(m.getPieceTo())] >= AdvancedMidGameEvaluator.EVALUATE_PRICE[Math.abs(m.getPieceFrom())]) {
                    this.goodCaptures.add(m);
                } else {
                    this.badCaptures.add(m);
                }
            } else {
                this.nonCaptureMoves.add(m);
            }
        }

        killerMoves.sort((o1, o2) -> {
            int p1 = o1.getOrderPriority();
            int p2 = o2.getOrderPriority();

            return -Integer.compare(p1,p2);
        });
        goodCaptures.sort((o1, o2) -> {
            int p1 = o1.getOrderPriority();
            int p2 = o2.getOrderPriority();

            return -Integer.compare(p1,p2);
        });
        badCaptures.sort((o1, o2) -> {
            int p1 = o1.getOrderPriority();
            int p2 = o2.getOrderPriority();

            return -Integer.compare(p1,p2);
        });
        nonCaptureMoves.sort((o1, o2) -> {
            int p1 = o1.getOrderPriority();
            int p2 = o2.getOrderPriority();

            return -Integer.compare(p1,p2);
        });





        collection.clear();
        collection.addAll(pvMoves);
        collection.addAll(goodCaptures);
        collection.addAll(badCaptures);
        collection.addAll(killerMoves);
        collection.addAll(nonCaptureMoves);



        if(collection.size() != initSize){
            throw new RuntimeException();
        }
    }
}
