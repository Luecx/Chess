package io;

import ai.evaluator.AdvancedEvaluator;
import ai.ordering.SystematicOrderer2;
import board.Board;
import board.FastBoard;
import board.moves.Move;
import board.moves.MoveListBuffer;
import ai.reducing.SenpaiReducer;
import ai.search.AI;
import ai.search.PVSearchFast;
import ai.tools.SearchOverview;

import java.util.ArrayList;
import java.util.List;

public class Testing {

    @Deprecated
    public static void compare(Board template, PVSearchFast ais[], String... FENs){

        //<editor-fold desc="Preheating">
        System.out.print("preheating...");
        for(int i = 0; i < 2; i++){
            Board board = IO.read_FEN(template, FENs[0]);
            for(PVSearchFast ai:ais){
                ai.setPrint_overview(false);
                ai.bestMove(board);
            }
        }
        System.out.println("preheating finished");
        //</editor-fold>

        String emptyLine =      "           ";
        String seperator =      "───────────";
        String format = "%10s";
        for(AI ignored :ais){
            seperator +=        "┼───────────────────────────────────────────";
            emptyLine +=        "│                                           ";
            format += " │ %-25s %-15s";
        }



        //<editor-fold desc="Header">
        ArrayList<String> all_flags = new ArrayList<>();
        for(PVSearchFast ai:ais){
            for(String f:ai.getSearchOverview().getFlags()){
                if(!all_flags.contains(f)){
                    all_flags.add(f);
                }
            }
        }

        String[] line1 = new String[1 + ais.length * 2];
        String[] line2 = new String[1 + ais.length * 2];
        line1[0] = "";
        line2[0] = "";
        for (int n = 0; n < ais.length; n++) {
            line1[1 + 2 * n] = "limit:";
            line1[1 + 2 * n + 1] = ""+ais[n].getLimit();
            line2[1 + 2 * n] = "qDepth:";
        }
        System.out.format(format+"\n", line1);
        System.out.format(format+"\n", line2);
        System.out.println(emptyLine);
        for(String flag:all_flags){
            String[] line = new String[1 + ais.length * 2];
            line[0] = "";
            for(int i = 0; i < ais.length; i++){
                boolean hasFlag = false;
                for(String f:ais[i].getSearchOverview().getFlags()){
                    if(f == flag){
                        hasFlag = true;
                    }
                }
                line[1+2*i+1] = "";
                if(hasFlag){
                    line[1+2*i] = flag;
                }else{
                    line[1+2*i] = "";
                }
            }
            System.out.format(format+"\n", line);
        }
        System.out.println(seperator);
        System.out.println(seperator);
        //</editor-fold>

        int[] avgDepth = new int[ais.length];
        int[] avgTime = new int[ais.length];
        int[] avgTotalNodes = new int[ais.length];
        int[] avgTerminalNodes = new int[ais.length];
        int[] avgQNodes = new int[ais.length];
        int[] avgFullDepthNodes = new int[ais.length];


        //<editor-fold desc="body">
        for(String fen:FENs){
            Board board = IO.read_FEN(template, fen);
            SearchOverview[] overviews = new SearchOverview[ais.length];
            for(int i = 0; i < overviews.length; i++){
                ais[i].bestMove(board);
                overviews[i] = ais[i].getSearchOverview();
            }
            String[] lMove = new String[1 + ais.length * 2];
            String[] lDepth = new String[1 + ais.length * 2];
            String[] lTime = new String[1 + ais.length * 2];
            String[] lNodes = new String[1 + ais.length * 2];
            String[] lQNodes = new String[1 + ais.length * 2];
            String[] lTerminal = new String[1 + ais.length * 2];
            String[] lfullDepth = new String[1 + ais.length * 2];

            lMove[0] = "";
            lDepth[0] = "";
            lTime[0] = "";
            lNodes[0] = "";
            lQNodes[0] = "";
            lTerminal[0] = "";
            lfullDepth[0] = "";

            for(int i = 0; i < ais.length; i++){

                lMove[1 + i * 2] = "move:";
                lMove[1 + i * 2 + 1] = IO.algebraicNotation(
                        board, ais[i].getSearchOverview().getMove()) + "[" +
                        ais[i].getSearchOverview().getEvaluation() + "]";

                lDepth[1 + i * 2] = "depth:";
                lDepth[1 + i * 2 + 1] = ""+overviews[i].getDepth();
                lTime[1 + i * 2] = "time[ms]:";
                lTime[1 + i * 2 + 1] = ""+overviews[i].getTotalTime();
                lNodes[1 + i * 2] = "total nodes:";
                lNodes[1 + i * 2 + 1] = ""+overviews[i].getTotalNodesLastIteration();
                lTerminal[1 + i * 2] = "terminal nodes:";
                lTerminal[1 + i * 2 + 1] = ""+overviews[i].getTerminalNodesLastIteration();
                lfullDepth[1 + i * 2] = "full depth nodes:";
                lfullDepth[1 + i * 2 + 1] = ""+overviews[i].getFullDepthNodesLastIteration();
                lQNodes[1 + i * 2] = "qSearch nodes:";
                lQNodes[1 + i * 2 + 1] = ""+overviews[i].getQSearchNodesLastIteration();

                avgDepth[i] += overviews[i].getDepth();
                avgTime[i] += overviews[i].getTotalTime();
                avgTotalNodes[i] += overviews[i].getTotalNodesLastIteration();
                avgTerminalNodes[i] += overviews[i].getTerminalNodesLastIteration();
                avgFullDepthNodes[i] += overviews[i].getFullDepthNodesLastIteration();
                avgQNodes[i] += overviews[i].getQSearchNodesLastIteration();
            }
            System.out.format(format+"\n", lMove);
            System.out.format(format+"\n", lDepth);
            System.out.format(format+"\n", lTime);
            System.out.format(format+"\n", lNodes);
            System.out.format(format+"\n", lQNodes);
            System.out.format(format+"\n", lTerminal);
            System.out.format(format+"\n", lfullDepth);

            if(fen != FENs[FENs.length-1]){
                System.out.println(seperator);
            }
        }
        System.out.println(seperator);
        System.out.println(seperator);
        //</editor-fold>


        //<editor-fold desc="summary">
        String[] lDepth = new String[1 + ais.length * 2];
        String[] lTime = new String[1 + ais.length * 2];
        String[] lNodes = new String[1 + ais.length * 2];
        String[] lQNodes = new String[1 + ais.length * 2];
        String[] lTerminal = new String[1 + ais.length * 2];
        String[] lfullDepth = new String[1 + ais.length * 2];

        lDepth[0] = "";
        lTime[0] = "";
        lNodes[0] = "";
        lQNodes[0] = "";
        lTerminal[0] = "";
        lfullDepth[0] = "";


        for(int i = 0; i < ais.length; i++) {
            lDepth[1 + i * 2] = "avg. depth:";
            lDepth[1 + i * 2 + 1] = "" + (double)avgDepth[i] / FENs.length;
            lTime[1 + i * 2] = "avg. time[ms]:";
            lTime[1 + i * 2 + 1] = "" + avgTime[i] / FENs.length;
            lNodes[1 + i * 2] = "avg. total nodes:";
            lNodes[1 + i * 2 + 1] = "" + avgTotalNodes[i] / FENs.length;
            lTerminal[1 + i * 2] = "avg. terminal nodes:";
            lTerminal[1 + i * 2 + 1] = "" + avgTerminalNodes[i] / FENs.length;
            lfullDepth[1 + i * 2] = "avg. full depth nodes:";
            lfullDepth[1 + i * 2 + 1] = "" + avgFullDepthNodes[i] / FENs.length;
            lQNodes[1 + i * 2] = "avg. qSearch nodes:";
            lQNodes[1 + i * 2 + 1] = "" + avgQNodes[i] / FENs.length;
        }
        System.out.format(format+"\n", lDepth);
        System.out.format(format+"\n", lTime);
        System.out.format(format+"\n", lNodes);
        System.out.format(format+"\n", lQNodes);
        System.out.format(format+"\n", lTerminal);
        System.out.format(format+"\n", lfullDepth);
        //</editor-fold>
    }

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

        System.out.println(perft_pseudo(
                IO.read_FEN(new FastBoard(), "p3p3/p3p3/p1K1p3/p3p3/P3P3/P1k1P3/P3P3/P3P3 w - - 0 1"),
                13,new MoveListBuffer(100,10),true));


        perft_validation(new FastBoard());

    }

}
