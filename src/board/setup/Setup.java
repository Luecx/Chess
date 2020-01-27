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
    NO_PAWNS(
            null, new int[][]{
            {-2,-3,-4,-5,-6,-4,-3,-2},
            { 0, 0, 0, 0, 0, 0, 0, 0},
            { 0, 0, 0, 0, 0, 0, 0, 0},
            { 0, 0, 0, 0, 0, 0, 0, 0},
            { 0, 0, 0, 0, 0, 0, 0, 0},
            { 0, 0, 0, 0, 0, 0, 0, 0},
            { 0, 0, 0, 0, 0, 0, 0, 0},
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
                    DEFAULT.apply(board);
                    for(int i = 0;i < 16; i++){
                        int i1 = (int)(Math.random() * 8);
                        int i2 = (int)(Math.random() * 8);

                        int p1 = board.getPiece(i1,0);
                        board.setPiece(i1,0,board.getPiece(i2,0));
                        board.setPiece(i2,0,p1);
                    }
                    for(int i = 0;i < 16; i++){
                        int i1 = (int)(Math.random() * 8);
                        int i2 = (int)(Math.random() * 8);

                        int p1 = board.getPiece(i1,7);
                        board.setPiece(i1,7,board.getPiece(i2,7));
                        board.setPiece(i2,7,p1);
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
