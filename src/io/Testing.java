package io;

import board.Board;
import board.SlowBoard;
import board.moves.Move;
import board.moves.MoveListBuffer;
import board.setup.Setup;
import game.ai.evaluator.FinnEvaluator;
import game.ai.evaluator.NoahEvaluator;
import game.ai.ordering.SystematicOrderer;
import game.ai.ordering.SystematicOrderer2;
import game.ai.reducing.SenpaiReducer;
import game.ai.reducing.SimpleReducer;
import game.ai.search.AI;
import game.ai.search.PVSearch;
import game.ai.tools.SearchOverview;

import java.util.ArrayList;
import java.util.List;

public class Testing {

    public static void compare(Board template, PVSearch ais[], String... FENs){

        //<editor-fold desc="Preheating">
        System.out.print("preheating...");
        for(int i = 0; i < 2; i++){
            Board board = IO.read_FEN(template, FENs[0]);
            for(PVSearch ai:ais){
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
        for(PVSearch ai:ais){
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
            line2[1 + 2 * n + 1] = ""+ais[n].getQuiesce_depth();
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

    public static int perft_pseudo(Board board, int depth, MoveListBuffer buffer){
        if(depth == 0) {
            return 1;
        }
        int nodes = 0;

        List<Move> moves = board.getPseudoLegalMoves(buffer.get(depth));

        if(depth == 1){
            return moves.size();
        }

        for(Object m:moves){
            board.move((Move)m);
            nodes += perft_pseudo(board, depth-1,buffer);
            board.undoMove();
        }
        return nodes;
    }

    public static void main(String[] args) {

        PVSearch ai1 = new PVSearch(
                new NoahEvaluator(),
                new SystematicOrderer2(),
                new SenpaiReducer(),
                2,
                8,
                6);
        ai1.setUse_killer_heuristic(true);
        ai1.setUse_null_moves(true);
        ai1.setUse_LMR(true);
        ai1.setUse_transposition(false);
        ai1.setUse_move_lists(true);



        PVSearch ai2 = new PVSearch(
                new NoahEvaluator(),
                new SystematicOrderer(),
                new SenpaiReducer(),
                2,
                8,
                6);

        ai2.setUse_killer_heuristic(true);
        ai2.setUse_null_moves(true);
        ai2.setUse_LMR(true);
        ai2.setUse_transposition(false);
        ai2.setUse_move_lists(true);

        compare(new SlowBoard(), new PVSearch[]{ai1,ai2},
            "r2q1rk1/1pp1bppp/p1npbn2/4p1B1/B3P3/2NP1N2/PPPQ1PPP/2KR3R",
            "r2q1rk1/ppp2ppp/2n2n2/2b5/2B2Bb1/2NP1N2/PPPQ2PP/R4R1K",
            "r1bq1rk1/1p2ppbp/p1np1np1/8/3NP3/1BN1B2P/PPP2PP1/R2Q1RK1",
            "r2qk2r/ppp1nppp/1bn1b3/1B6/1P1pP3/5N2/PB3PPP/RN1Q1RK1",
            "r2qkb1r/pp1n1pp1/2p1pn1p/8/3P1B1P/3Q1NN1/PPP2PP1/2KR3R");



    }




}
