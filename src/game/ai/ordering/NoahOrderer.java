package game.ai.ordering;

import board.moves.Move;
import game.ai.tools.PVLine;

import java.util.Comparator;
import java.util.List;

public class NoahOrderer implements Orderer {
    @Override
    public <T extends Move> void sort(List<T> collection, int depth, PVLine lastIteration) {
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
