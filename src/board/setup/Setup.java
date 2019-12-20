package board.setup;

import board.Board;

import java.util.function.Consumer;

public enum Setup {

    DEFAULT(
            null, new int[][]{
                    {-2,-3,-4,-5,-6,-4,-3,-2},
                    {-1,-1,-1,-1,-1,-1,-1,-1},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    {+1,+1,+1,+1,+1,+1,+1,+1},
                    {+2,+3,+4,+5,+6,+4,+3,+2}
            }
    ),
    DUNSANY(
            null, new int[][]{
                    {-2,-3,-4,-5,-6,-4,-3,-2},
                    {-1,-1,-1,-1,-1,-1,-1,-1},
                    { 0, 0, 0, 0, 0, 0, 0, 0},
                    { 0, 0, 0,+1,+1, 0, 0, 0},
                    {+1,+1,+1,+1,+1,+1,+1,+1},
                    {+1,+1,+1,+1,+1,+1,+1,+1},
                    {+1,+1,+1,+1,+1,+1,+1,+1},
                    {+1,+1,+1,+1,+6,+1,+1,+1}
    }),
    CHESS960(
            new Consumer<Board>() {
                @Override
                public void accept(Board board) {
                    for(int i = 0; i< 4; i++)
                        board.setCastlingChance(0,false);
                    for(int i = 0; i < 8; i++){
                        board.setPiece(i,1,1);
                        board.setPiece(i,6,-1);
                        board.setPiece(i,0,i < 4 ? i+1: 8-i);
                        board.setPiece(i,7,i < 4 ? i+1: 8-i);
                    }
                }
            }, null),


    ;


    private final Consumer<Board> function;
    private final int[][] field;

    Setup(Consumer<Board> function, int[][] ints) {
        this.function = function;
        this.field = ints;

    }

    public void apply(Board board){
        board.reset();

        if(field != null){
            for(int i = 0; i < 8; i++){
                for(int n = 0; n < 8; n++){
                    board.setPiece(i,n, field[7-n][i]);
                }
            }
        }

        else{
            this.function.accept(board);
        }

    }


}
