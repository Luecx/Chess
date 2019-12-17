package game.ai.ordering;

import board.moves.Move;
import game.ai.evaluator.FinnEvaluator;
import game.ai.tools.PVLine;

import java.util.Comparator;
import java.util.List;

public class SimpleOrderer implements Orderer {


    /**
     * this methods sorts a list of moves by the following ranking:
     *
     *   1: PV-Moves from the last iteration (if lastIteration != null)
     *   2: Ranking by the evaluation price of the piece that moves + the piece that has been taken.
     *      This uses the EVALUATE_PRICE variable in the FinnEvaluator.
     * @param collection
     * @param depth
     * @param lastIteration
     * @param <T>
     */
    @Override
    public <T extends Move> void sort(List<T> collection, int depth,PVLine lastIteration) {
        if(lastIteration != null){

            collection.sort(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    int p1 = FinnEvaluator.EVALUATE_PRICE[Math.abs(o1.getPieceTo())] + FinnEvaluator.EVALUATE_PRICE[Math.abs(o1.getPieceFrom())];
                    int p2 = FinnEvaluator.EVALUATE_PRICE[Math.abs(o2.getPieceTo())] + FinnEvaluator.EVALUATE_PRICE[Math.abs(o2.getPieceFrom())];

                    return -Integer.compare(p1,p2);
                }
            });


            if(depth < lastIteration.getLine().length){
                int index = collection.indexOf(lastIteration.getLine()[depth]);
                if(index != -1){
                    Object object = collection.get(index);
                    collection.remove(index);
                    collection.add(0,(T)object);
                }
            }

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
