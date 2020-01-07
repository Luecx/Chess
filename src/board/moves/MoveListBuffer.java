package board.moves;

import java.util.ArrayList;

public class MoveListBuffer {


    private final int MAX_DEPTHS = 100;

    private MoveList[] lists = new MoveList[MAX_DEPTHS];

    public MoveListBuffer(int sizes) {
        for(int i = 0; i < MAX_DEPTHS; i++){
            lists[i] = new MoveList(sizes);
        }
    }

    public MoveList get(int index){
        return lists[index];
    }
}
