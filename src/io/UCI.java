package io;

import ai.evaluator.Evaluator;
import ai.evaluator.NoahEvaluator;
import ai.search.AdvancedSearch;
import board.Board;
import board.FastBoard;
import board.moves.Move;
import board.setup.Setup;
import ai.evaluator.NoahEvaluator2;
import ai.ordering.SystematicOrderer2;
import ai.reducing.SenpaiReducer;
import ai.search.PVSearch;
import ai.search.PVSearchFast;
//// coppied from someone else, probabally not using it, and will instead try to do something with Python
/// because I already have some thing for that.
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
public class UCI {

    private static String ENGINENAME = "Waldi"; // we should decide on a name of the engine
    private static Board b = new FastBoard(Setup.DEFAULT);
    private static Evaluator evaluator = new NoahEvaluator2();
    private static AdvancedSearch ai = new AdvancedSearch(
            evaluator,
            new SystematicOrderer2(),
            new SenpaiReducer(1),
            PVSearch.FLAG_TIME_LIMIT,
            1000);

    public static void uciCommunication() {

//        evaluator.setEvolvableValues(
//                new double[]{100.0, 100.0, 100.0, 100.0, 100.0, 60.0, 125.0, 495.0, 354.0, 315.0, 922.0,
//                        20003.0, 6.0, 5.0, -1.0, 2.0, 14.0, 27.0, -24.0, 41.0, 66.0, 5.0, 27.7, 20.0, 28.0, 8.0});
//        ai.setUse_transposition(true);

        Scanner input = new Scanner(System.in);
        while (true)
        {
            String inputString = input.nextLine();
            //log("[IN]: " + inputString + "\n");
            if ("uci".equals(inputString)) {
                inputUCI();
            }
            else if (inputString.startsWith("setoption")) {
                inputSetOption(inputString);
            }
            else if ("isready".equals(inputString)) {
                inputIsReady();
            }
            else if ("ucinewgame".equals(inputString)) {
                inputUCINewGame();
            }
            else if (inputString.startsWith("position")) {
                inputPosition(inputString);
            }
            else if (inputString.startsWith("go")) {
                inputGo(inputString);
            }
            else if ("print".equals(inputString)) {
                inputPrint();
            }
            else if("quit".equals(inputString)){
                break;
            }
        }
        input.close();
    }
    public static void inputUCI() {
        System.out.println("id name "+ENGINENAME);
        System.out.println("id author Finn/Noah");

        System.out.println("option name null_moves type check default true");
        System.out.println("option name lmr type check default true");
        System.out.println("option name killers type check default true");
        System.out.println("option name transpositions type check default true");
        System.out.println("option name iterative type check default true");
        System.out.println("option name razoring type check default false");

        System.out.println("uciok");
    }
    public static void inputSetOption(String inputString) {

        String[] args = inputString.split(" ");

        String name = args[2];
        String value = args[4];

        //log("[INTERNAL] setting options with: " + name + "=" + value+"\n");

        switch (name){
            case "null_moves": ai.setUse_null_moves(Boolean.parseBoolean(value));break;
            case "lmr": ai.setUse_LMR(Boolean.parseBoolean(value));break;
            case "killers": ai.setUse_killer_heuristic(Boolean.parseBoolean(value));break;
            case "transpositions": ai.setUse_transposition(Boolean.parseBoolean(value));break;
            case "iterative": ai.setUse_iteration(Boolean.parseBoolean(value));break;
            case "razoring": ai.setUse_razoring(Boolean.parseBoolean(value));break;
        }

        //set options
    }
    public static void inputIsReady() {
        System.out.println("readyok");
    }
    public static void inputUCINewGame() {
        //add code here
    }
    public static Board inputPosition(String input) {
        input=input.substring(9).concat(" ");
        if (input.contains("startpos ")) {
            input=input.substring(9);
            b = new FastBoard(Setup.DEFAULT);
        }
        else if (input.contains("fen")) {
            input=input.substring(4);
            b = IO.read_FEN(new FastBoard(), input);
        }
        if (input.contains("moves")) {
            input=input.substring(input.indexOf("moves")+6);
            String[] moveArr = input.split("\\s+");
            for (String uciMove : moveArr) {
                Move move = uciToMove(uciMove, b);
                b.move(move);
                //System.out.println(b);
            }
        }
        //System.out.println(b);
        return b;
    }
    public static void inputGo(String inputString) {

        String[] split = inputString.split(" ");

        ArrayList<String> commands = new ArrayList<>();
        for(String s:split){
            commands.add(s);
        }

        int wtime = (int) 3600E3;
        int btime = (int) 3600E3;

        int winc = 0;
        int binc = 0;

        int movestogo = 0;

        int mode = PVSearch.FLAG_TIME_LIMIT;
        int limit = 50000;

        if(commands.contains("wtime")){
            wtime = Integer.parseInt(commands.get(commands.indexOf("wtime")+1));
            mode = PVSearch.FLAG_TIME_LIMIT;
        }if(commands.contains("btime")){
            btime = Integer.parseInt(commands.get(commands.indexOf("btime")+1));
            mode = PVSearch.FLAG_TIME_LIMIT;
        }if(commands.contains("winc")){
            winc = Integer.parseInt(commands.get(commands.indexOf("winc")+1));
            mode = PVSearch.FLAG_TIME_LIMIT;
        }if(commands.contains("binc")){
            binc = Integer.parseInt(commands.get(commands.indexOf("binc")+1));
            mode = PVSearch.FLAG_TIME_LIMIT;
        }if(commands.contains("movestogo")){
            movestogo = Integer.parseInt(commands.get(commands.indexOf("movestogo")+1));
        }if(commands.contains("depth")){
            mode = PVSearch.FLAG_DEPTH_LIMIT;
            limit = Integer.parseInt(commands.get(commands.indexOf("depth")+1));
        }if(commands.contains("movetime")){
            mode = PVSearch.FLAG_TIME_LIMIT;
            limit = Integer.parseInt(commands.get(commands.indexOf("movetime")+1));
        }if(commands.contains("nodes")){
            throw new RuntimeException("Not yet supported");
        }if(commands.contains("mate")){
            throw new RuntimeException("Not yet supported");
        }



        ai.setLimit(limit);
        ai.setLimit_flag(mode);

        if(ai.getLimit_flag() == PVSearchFast.FLAG_TIME_LIMIT){
            if (b.getActivePlayer() == 1) {
                ai.setLimit(wtime/30 + winc);
            }
            if (b.getActivePlayer() == -1) {
                ai.setLimit(btime/30 + binc);
            }
            ai.setPrint_overview(false);
            if(ai.getLimit_flag() == PVSearch.FLAG_TIME_LIMIT && ai.getLimit() > 10000){
                ai.setLimit(10000);
            }
        }

        System.out.println(ai.getLimit());


        Move best = ai.bestMove(b);

        //log(moveToUCI(best,b)+" info " + ai.getSearchOverview().getInfo() + "\n");
        //log("tt:" + ai.isUse_transposition()+"\n");
        //log(IO.write_FEN(b) + "\n");
        //System.out.println("info " + ai.getSearchOverview().getInfo());
        System.out.println("bestmove " + moveToUCI(best,b));
    }

    public static void log(String s){
        try {
            new File("log.txt").createNewFile();
            Files.write(Paths.get("log.txt"), s.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    /**
     * take a move in UCI notation (e2e4) and
     * transforms it to move object
     * @param input the UCI notation
     * @param board the board state
     * @return the move object
     */
    public static Move uciToMove(String input, Board board) {
        int fromx;
        int fromy;
        int tox;
        int toy;
        int from;
        int to;
        fromx = IO.fileToIndex(input.charAt(0));
        tox = IO.fileToIndex(input.charAt(2));
        fromy = Character.getNumericValue(input.charAt(1)) - 1;
        toy = Character.getNumericValue(input.charAt(3)) - 1;
        from = board.index(fromx,fromy);
        to = board.index(tox,toy);

        int promotionTarget = input.length() > 4 ? "00rkbq".indexOf(input.charAt(4)) : 0;

        return board.generateMove(from, to, promotionTarget);
    }

    //Move object -> e2e4
    public static String moveToUCI(Move move, Board board) {
        String toReturn = "";
        int tox = board.x(move.getTo());
        int toy = board.y(move.getTo());
        int fromx = board.x(move.getFrom());
        int fromy = board.y(move.getFrom());

        toReturn = toReturn + IO.indexToFile(fromx);
        toReturn = toReturn + Integer.toString(fromy+1);
        toReturn = toReturn + IO.indexToFile(tox);
        toReturn = toReturn + Integer.toString(toy+1);

        if(move.isPromotion()){
            toReturn += "00rkbq".toCharArray()[Math.abs(move.getPieceFrom())];
        }

        return toReturn;
    }

    public static void inputPrint() {
        System.out.println(b);
        //BoardGeneration.drawArray(UserInterface.WP,UserInterface.WN,UserInterface.WB,UserInterface.WR,UserInterface.WQ,UserInterface.WK,UserInterface.BP,UserInterface.BN,UserInterface.BB,UserInterface.BR,UserInterface.BQ,UserInterface.BK);
    }

    public static void main(String[] args) {

       //log("UCI Started\n");

        uciCommunication();
    }
}