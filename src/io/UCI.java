package io;

import board.Board;
import board.SlowBoard;
import board.moves.Move;
import board.setup.Setup;
import game.ai.evaluator.NoahEvaluator;
import game.ai.evaluator.NoahEvaluator2;
import game.ai.ordering.SystematicOrderer;
import game.ai.ordering.SystematicOrderer2;
import game.ai.reducing.SenpaiReducer;
import game.ai.reducing.SimpleReducer;
import game.ai.search.PVSearch;
import io.IO;


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
    private static Board b = new SlowBoard(Setup.DEFAULT);
    private static PVSearch ai = new PVSearch(
            new NoahEvaluator2(),
            new SystematicOrderer2(),
            new SenpaiReducer(),
            PVSearch.FLAG_TIME_LIMIT,
            1000,4);
    public static void uciCommunication() {


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
        System.out.println("option name transpositions type check default false");
        System.out.println("option name iterative type check default true");
        System.out.println("option name qdepth type spin min 0 max 50 default 10");

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
            case "qdepth": ai.setQuiesce_depth(Integer.parseInt(value));break;
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
            b = new SlowBoard(Setup.DEFAULT);
        }
        else if (input.contains("fen")) {
            input=input.substring(4);
            b = IO.read_FEN(new SlowBoard(), input);
        }
        if (input.contains("moves")) {
            input=input.substring(input.indexOf("moves")+6);
            String[] moveArr = input.split("\\s+");
            for (String uciMove : moveArr) {
                Move move = IO.uciToMove(uciMove, b);
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

        int wtime = (int) 60E3;
        int btime = (int) 60E3;

        int winc = 0;
        int binc = 0;

        int movestogo = 0;

        int mode = PVSearch.FLAG_TIME_LIMIT;
        int limit = 5000;

        if(commands.contains("wtime")){
            wtime = Integer.parseInt(commands.get(commands.indexOf("wtime")+1));
        }if(commands.contains("btime")){
            btime = Integer.parseInt(commands.get(commands.indexOf("btime")+1));
        }if(commands.contains("winc")){
            winc = Integer.parseInt(commands.get(commands.indexOf("winc")+1));
        }if(commands.contains("binc")){
            binc = Integer.parseInt(commands.get(commands.indexOf("binc")+1));
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
        if (b.getActivePlayer() == 1) {
            ai.setLimit(wtime/30);
        }
        if (b.getActivePlayer() == -1) {
            ai.setLimit(btime/30);
        }
        ai.setPrint_overview(false);
        if(ai.getLimit_flag() == PVSearch.FLAG_TIME_LIMIT && ai.getLimit() > 10000){
            ai.setLimit(10000);
        }

        Move best = ai.bestMove(b);

        log(IO.moveToUCI(best,b)+" info " + ai.getSearchOverview().getInfo() + "\n");
        System.out.println("info " + ai.getSearchOverview().getInfo());
        System.out.println("bestmove " + IO.moveToUCI(best,b));
    }

    public static void log(String s){
        try {
            new File("log.txt").createNewFile();
            Files.write(Paths.get("log.txt"), s.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
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