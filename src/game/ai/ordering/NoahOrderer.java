package game.ai.ordering;

import board.Board;
import board.moves.Move;
import game.ai.evaluator.NoahEvaluator;
import game.ai.tools.KillerTable;
import game.ai.tools.PVLine;
import game.ai.tools.TranspositionTable;

import java.util.Comparator;
import java.util.List;

public class NoahOrderer implements Orderer {

    public static void setOrderPriority(Move move, Board tokenSB) {
        int priority = 0;
        //  int color = move.getPieceFrom() > 0 ? 1 : -1;

        priority += NoahEvaluator.COMPLETE_EVALUATE_PRICE[move.getPieceTo() + 6];
        //priority += Math.abs(NoahEvaluator.EVALUATE_PRICE[Math.abs(move.getPieceTo())]);

//        priority -= NoahEvaluator.UNSIGNED_COMPLETE_EVALUATE_PRICE[move.getPieceFrom() + 6];
        priority -= Math.abs(move.getPieceFrom()); //to capture with least valuable piece


        priority += NoahEvaluator.COMPLETE_POSITION_PRICE.get(move.getPieceFrom()+6,tokenSB.x(move.getTo()),    tokenSB.y(move.getTo()));
        priority -= NoahEvaluator.COMPLETE_POSITION_PRICE.get(move.getPieceFrom()+6,tokenSB.x(move.getFrom()),  tokenSB.y(move.getFrom()));
//        if (color == 1) {
//            priority += NoahEvaluator.W_POSITION_PRICE.get( move.getPieceFrom()-1,   tokenSB.x(move.getTo()),    tokenSB.y(move.getTo()));
//            priority -= NoahEvaluator.W_POSITION_PRICE.get( move.getPieceFrom()-1,   tokenSB.x(move.getFrom()),  tokenSB.y(move.getFrom()));
//        }
//        if (color == -1) {
//            priority += NoahEvaluator.B_POSITION_PRICE.get(Math.abs(move.getPieceFrom())-1,tokenSB.x(move.getTo()),tokenSB.y(move.getTo()));
//            priority -= NoahEvaluator.B_POSITION_PRICE.get(Math.abs(move.getPieceFrom())-1,tokenSB.x(move.getFrom()),tokenSB.y(move.getFrom()));
//        }

        move.setOrderPriority(priority);
    }
    
    @Override
    public <T extends Move> void sort(
            List<T> collection,
            int depth,
            PVLine lastIteration,
            Board board,
            KillerTable killerTable,
            TranspositionTable transpositionTable) {

        for (Move m:collection){
            setOrderPriority(m, board);
        }

        collection.sort((o1, o2) -> {
            int p1 = o1.getOrderPriority();
            int p2 = o2.getOrderPriority();

            return -Integer.compare(p1,p2);
        });

        if(lastIteration != null){
            if(depth < lastIteration.getLine().length){
                int index = collection.indexOf(lastIteration.getLine()[depth]);
                if(index != -1){
                    Object object = collection.get(index);
                    collection.remove(index);
                    collection.add(0,(T)object);
                }
            }
        }
    }
}
