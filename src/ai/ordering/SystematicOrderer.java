package ai.ordering;

import ai.tools.tables.HistoryTable;
import board.Board;
import board.moves.Move;
import ai.tools.tables.KillerTable;
import ai.tools.PVLine;
import ai.tools.transpositions.TranspositionEntry;
import ai.tools.transpositions.TranspositionTable;

import java.util.ArrayList;
import java.util.List;

public class SystematicOrderer implements Orderer {

    @Override
    public void sort(
            List<Move> collection,
            int depth,
            PVLine lastIteration,
            Board board,
            boolean pvNode,
            KillerTable killerTable,
            HistoryTable historyTable,
            TranspositionTable transpositionTable){

        int initSize = collection.size();

        ArrayList<Move> pvMoves = new ArrayList<>(10);
        ArrayList<Move> captureMoves = new ArrayList<>(10);
        ArrayList<Move> killerMoves = new ArrayList<>(10);
        ArrayList<Move> nonCaptureMoves = new ArrayList<>(10);

        for (Move m:collection){
            NoahOrderer.setOrderPriority(m, board);
        }

        //PV nodes
        if(lastIteration != null){
            if(depth < lastIteration.getLine().length){
                int index = collection.indexOf(lastIteration.getLine()[depth]);
                if(index != -1){
                    pvMoves.add(collection.get(index));
                    collection.remove(index);
                }
            }
        }


        //hash moves

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
            if(killerTable != null && killerTable.isKillerMove(depth, m)){
                killerMoves.add(m);
            } else if(m.getPieceTo() != 0){
                captureMoves.add(m);
            } else{
                nonCaptureMoves.add(m);
            }
        }



        killerMoves.sort((o1, o2) -> {
            int p1 = o1.getOrderPriority();
            int p2 = o2.getOrderPriority();

            return -Integer.compare(p1,p2);
        });
        captureMoves.sort((o1, o2) -> {
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
        collection.addAll(captureMoves);
        collection.addAll(killerMoves);
        collection.addAll(nonCaptureMoves);



        if(collection.size() != initSize){
            throw new RuntimeException();
        }
    }
}
