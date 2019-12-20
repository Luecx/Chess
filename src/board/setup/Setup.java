package board.setup;

import board.Board;

public enum Setup {

    DEFAULT(
            new int[][]{
                    {-2,-3,-4,-5,-6,-4,-3,-2},
                    {-1,-1,-1,-1,-1,-1,-1,-1},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    {+1,+1,+1,+1,+1,+1,+1,+1},
                    {+2,+3,+4,+5,+6,+4,+3,+2}
            }

    );


    private final int[][] field;

    Setup(int[][] ints) {
        this.field = ints;

    }

    public void apply(Board board){
        board.reset();
        for(int i = 0; i < 8; i++){
            for(int n = 0; n < 8; n++){
                board.setPiece(i,n, field[7-n][i]);
            }
        }
    }


}
