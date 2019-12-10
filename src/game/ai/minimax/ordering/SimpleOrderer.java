package game.ai.minimax.ordering;

import board.Move;
import game.ai.minimax.evaluator.FinnEvaluator;
import game.ai.minimax.tools.PVLine;

import java.util.Comparator;
import java.util.List;

public class SimpleOrderer implements Orderer {



    @Override
    public <T extends Move> void sort(List<T> collection, int depth,PVLine lastIteration) {
        if(lastIteration != null){
            collection.sort(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    int p1 = FinnEvaluator.EVALUATE_PRICE[Math.abs(o1.getPieceTo())] + FinnEvaluator.EVALUATE_PRICE[Math.abs(o1.getPieceFrom())];
                    int p2 = FinnEvaluator.EVALUATE_PRICE[Math.abs(o2.getPieceTo())] + FinnEvaluator.EVALUATE_PRICE[Math.abs(o2.getPieceFrom())];

                    if(depth < lastIteration.getLine().length && o1.equals(lastIteration.getLine()[depth])) p1 += 10000000;
                    if(depth < lastIteration.getLine().length && o2.equals(lastIteration.getLine()[depth])) p2 += 10000000;

                    return -Integer.compare(p1,p2);
                }
            });
        }else{
            collection.sort(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return -Integer.compare(
                            FinnEvaluator.EVALUATE_PRICE[Math.abs(o1.getPieceTo())] + FinnEvaluator.EVALUATE_PRICE[Math.abs(o1.getPieceFrom())],
                            FinnEvaluator.EVALUATE_PRICE[Math.abs(o2.getPieceTo())] + FinnEvaluator.EVALUATE_PRICE[Math.abs(o2.getPieceFrom())]);
                }
            });
        }

    }
}
