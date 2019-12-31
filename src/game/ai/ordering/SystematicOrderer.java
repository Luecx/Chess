package game.ai.ordering;

import board.Board;
import board.moves.Move;
import game.ai.tools.KillerTable;
import game.ai.tools.PVLine;
import game.ai.tools.TranspositionEntry;
import game.ai.tools.TranspositionTable;

import java.util.ArrayList;
import java.util.List;

public class SystematicOrderer implements Orderer {

    @Override
    public <T extends Move> void sort(
            List<T> collection,
            int depth,
            PVLine lastIteration,
            Board board,
            KillerTable killerTable,
            TranspositionTable transpositionTable){

        int initSize = collection.size();

        ArrayList<T> pvMoves = new ArrayList<>(5);
        ArrayList<T> hashMoves = new ArrayList<>(5);
        ArrayList<T> captureMoves = new ArrayList<>(5);
        ArrayList<T> killerMoves = new ArrayList<>(5);
        ArrayList<T> nonCaptureMoves = new ArrayList<>(5);

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
        //TODO: add stuff about the proper depth
        if (transpositionTable != null && pvMoves.size() == 0) {
            TranspositionEntry en = (TranspositionEntry) transpositionTable.get(zobrist);
            if (en != null && en.getDepth() <= depth) {
                Move hashMove = en.getBestMove();
                int index = collection.indexOf((T)hashMove);
                if (hashMove != null && index != -1) {
                    pvMoves.add((T)hashMove);
                    collection.remove(index);
                    //System.out.println("hi mom");
                }
            }
        }


        //capture moves / non capture / killers
        for(T m:collection){
            if(killerTable != null && killerTable.isKillerMove(depth, m)){
                killerMoves.add(m);
            }
            else if(m.getPieceTo() != 0){
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
