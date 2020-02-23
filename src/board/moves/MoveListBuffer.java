package board.moves;

import java.util.ArrayList;

public class MoveListBuffer {



    private MoveList[] lists;

    /**
     * creates a move list buffer.
     * this stores an amount of maxDepth lists each with an initial size of sizes.
     * @param maxDepth
     * @param sizes
     */
    public MoveListBuffer(int maxDepth, int sizes) {
        this.lists = new MoveList[maxDepth];
        for(int i = 0; i < maxDepth; i++){
            lists[i] = new MoveList(sizes);
        }
    }

    /**
     * returns the move list at that given index/depth
     * @param index
     * @return
     */
    public MoveList get(int index){
        return lists[index];
    }
}
