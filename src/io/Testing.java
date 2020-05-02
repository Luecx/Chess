package io;

import board.Board;
import board.moves.Move;
import board.moves.MoveListBuffer;
import ai.search.AI;
import ai.tools.SearchOverview;

import java.util.ArrayList;
import java.util.List;

public class Testing {

   
    /**
     * calculates the perft value for a given board until it reaches a given depth.
     * @param board         the board object
     * @param depthLeft     set this to your depth
     * @param buffer        buffer to store moves
     * @param printDiv      display the childs at depth=1
     * @return
     */
    public static int perft_pseudo(Board board, int depthLeft, MoveListBuffer buffer,boolean printDiv){
        if(depthLeft == 0) {
            return 1;
        }
        int nodes = 0;

        List<Move> moves = board.getPseudoLegalMoves(buffer.get(depthLeft));

        for(Object m:moves){

            if(!board.isLegal((Move)m)){
                continue;
            }

            if(depthLeft == 1){
                nodes ++;
            }else{
                board.move((Move)m);
                int nd = perft_pseudo(board, depthLeft-1,buffer, false);
                nodes += nd;
                board.undoMove();

                if(printDiv){
                    System.out.println(IO.getSquareString(((Move) m).getFrom())+ IO.getSquareString(((Move) m).getTo()) + "  " + nd);
                }
            }


        }

        return nodes;
    }

    public static String loadingBar(int count, int max, String msg){
        StringBuilder builder = new StringBuilder();
        double p = count / (double)max;

        builder.append((int)(100*p)+"% [");
        for(int i = 0; i < 50 * p; i++){
            builder.append("=");
        }
        builder.append(">");
        for(int i = 0; i < 50*(1-p); i++){
            builder.append(" ");
        }
        builder.append("] ");
        builder.append(count + "/" + max + " " + msg);
        return builder.toString();
    }

    /**
     * call this to validate that the isLegal() function of the given board works
     * @param board
     */
    public static void perft_validation(Board board){
        board = IO.read_FEN(board, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        if(perft_pseudo(board, 5, new MoveListBuffer(10,128), true) != 4865609) throw new RuntimeException();
        board = IO.read_FEN(board, "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        if(perft_pseudo(board, 4, new MoveListBuffer(10,128), true) != 4085603) throw new RuntimeException();
        board = IO.read_FEN(board, "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - ");
        if(perft_pseudo(board, 6, new MoveListBuffer(10,128), true) != 11030083) throw new RuntimeException();
        board = IO.read_FEN(board, "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        if(perft_pseudo(board, 5, new MoveListBuffer(10,128), true) != 15833292) throw new RuntimeException();
        board = IO.read_FEN(board, "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8  ");
        if(perft_pseudo(board, 4, new MoveListBuffer(10,128), true) != 2103487) throw new RuntimeException();
        board = IO.read_FEN(board, "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 ");
        if(perft_pseudo(board, 4, new MoveListBuffer(10,128), true) != 3894594) throw new RuntimeException();
    }

    public static void main(String[] args) {



//        System.out.println(ObjectSizeFetcher.getObjectSize(new TranspositionEntry(1, 2, 3, 4, new Move(1, 2, 3, 45))));
//
//        System.out.println(new TranspositionEntry(1,2,3,4,new Move(1,2,3,45)));



    }

}
