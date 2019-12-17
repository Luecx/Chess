package game.ai.ordering;

import board.moves.Move;
import game.ai.evaluator.FinnEvaluator;
import game.ai.tools.PVLine;

import java.util.Comparator;
import java.util.List;

public class SimpleOrderer implements Orderer {


    public static final int[] EVALUATE_PRICE = new int[]{0, 100, 500, 320, 330, 900, 20000};


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

        collection.sort((o1, o2) -> {
            int p1 = EVALUATE_PRICE[Math.abs(o1.getPieceTo())] - EVALUATE_PRICE[Math.abs(o1.getPieceFrom())];
            int p2 = EVALUATE_PRICE[Math.abs(o2.getPieceTo())] - EVALUATE_PRICE[Math.abs(o2.getPieceFrom())];

//            System.out.println(o1 +"  " + p1);
//            System.out.println(o2 +"  " + p2);

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
