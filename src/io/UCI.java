package io;

import ai.evaluator.*;
import ai.evaluator.decider.SimpleDecider;
import ai.search.AdvancedSearch;
import ai.time_manager.SimpleTimeManager;
import ai.time_manager.TimeManager;
import board.Board;
import board.FastBoard;
import board.moves.Move;
import board.moves.MoveListBuffer;
import board.setup.Setup;
import ai.ordering.SystematicOrderer2;
import ai.reducing.SenpaiReducer;
import io.command_line.commands.Command;
import io.command_line.commands.CommandDataBase;
import io.command_line.commands.Executable;
import io.command_line.commands.arguments.BooleanArgument;
import io.command_line.commands.arguments.NumericArgument;
import io.command_line.commands.arguments.TextArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static io.IO.*;

public class UCI {

    private static String LOG_NAME = null;
    private static String ENGINENAME = "Waldi";
    private static Board b = new FastBoard(Setup.DEFAULT);

    private static TimeManager timeManager = new SimpleTimeManager();
    private static SenpaiReducer senpaiReducer = new SenpaiReducer(10,4,3);
    private static AdvancedSearch ai = new AdvancedSearch(
            new AdvancedEvaluator(new SimpleDecider()),
            //new SimpleEvaluator(),
            new SystematicOrderer2(),
            senpaiReducer,
            AdvancedSearch.FLAG_TIME_LIMIT,
            1000);



    private static CommandDataBase cdb = new CommandDataBase();

    static {
        long t = System.currentTimeMillis();
        System.out.print("registering commands...");
        cdb.registerCommand(
                new Command("debug", "enables/disables debugging")
                        .registerArgument(new BooleanArgument("ON", false))
                        .registerArgument(new BooleanArgument("OFF", false))
                        .setExecutable(c -> {
                            if(c.getArgument("on").isSet()){
                                ai.setDebug(true);
                            }else if(c.getArgument("off").isSet()){
                                ai.setDebug(false);
                            }else{
                                System.out.println("Error while parsing this command!");
                            }
                        })
        );
        cdb.registerCommand(
                new Command("setoption", "sets some options of the engine")
                        .registerArgument(new TextArgument("name", true, "none"))
                        .registerArgument(new TextArgument("value", false, "none"))
                        .setExecutable(c -> {
                            if (c.getTextArgument("value").isSet()) {
                                String value = c.getTextArgument("value").getValue();
                                switch (c.getTextArgument("name").getValue()) {
                                    case "log":
                                        useLog(Boolean.parseBoolean(value));

                                    case "null_moves":
                                        ai.setUse_null_moves(Boolean.parseBoolean(value));
                                        break;

                                    case "lmr":
                                        ai.setUse_LMR(Boolean.parseBoolean(value));
                                        break;

                                    case "killers":
                                        ai.setUse_killer_heuristic(Boolean.parseBoolean(value));
                                        break;

                                    case "transpositions":
                                        ai.setUse_transposition(Boolean.parseBoolean(value));
                                        break;

                                    case "iterative":
                                        ai.setUse_iteration(Boolean.parseBoolean(value));
                                        break;
                                    case "deepening_initial_depth":
                                        ai.setDeepening_start_depth(Integer.parseInt(value));
                                        break;

                                    case "razoring":
                                        ai.setUse_razoring(Boolean.parseBoolean(value));
                                        break;

                                    case "reduction_divisionFactor":
                                        senpaiReducer.setDivision_factor(Integer.parseInt(value));
                                        break;
                                    case "reduction_greaterReductionDepth":
                                        senpaiReducer.setHigher_reduction_depth(Integer.parseInt(value));
                                        break;
                                    case "reduction_numMovesNotReduced":
                                        senpaiReducer.setNum_moves_not_reduced(Integer.parseInt(value));
                                        break;


                                    case "futility_pruning":
                                        ai.setUse_futility_pruning(Boolean.parseBoolean(value));
                                        break;
                                    case "futility_pruning_margin":
                                        ai.setFutility_pruning_margin(Integer.parseInt(value));
                                        break;

                                    case "delta_pruning":
                                        ai.setUse_delta_pruning(Boolean.parseBoolean(value));
                                        break;
                                    case "delta_pruning_big_margin":
                                        ai.setDelta_pruning_big_margin(Integer.parseInt(value));
                                        break;
                                    case "delta_pruning_margin":
                                        ai.setDelta_pruning_margin(Integer.parseInt(value));
                                        break;
                                }
                            }
                        }));
        cdb.registerCommand(
                new Command("uci")
                        .setExecutable(c -> {
                            System.out.println("id name " + ENGINENAME);
                            System.out.println("id author Finn/Noah");
                            System.out.println("option name log type check default false");

                            System.out.println("option name null_moves type check default "+ai.isUse_null_moves());
                            System.out.println("option name lmr type check default " + ai.isUse_LMR());
                            System.out.println("option name killers type check default " + ai.isUse_killer_heuristic());
                            System.out.println("option name transpositions type check default " + ai.isUse_transposition());

                            System.out.println("option name iterative type check default " + ai.isUse_iteration());
                            System.out.println("option name deepening_initial_depth type spin default " + ai.getDeepening_start_depth() + " min 1 max 99");

                            System.out.println("option name razoring type check default " + ai.isUse_razoring());
                            System.out.println("option name reduction_divisionFactor type spin default "+senpaiReducer.getDivision_factor()+" min 1 max 100");
                            System.out.println("option name reduction_greaterReductionDepth type spin default "+senpaiReducer.getHigher_reduction_depth()+" min 0 max 100");
                            System.out.println("option name reduction_numMovesNotReduced type spin default "+senpaiReducer.getNum_moves_not_reduced()+" min 0 max 1000");

                            System.out.println("option name futility_pruning type check default "+ai.isUse_futility_pruning());
                            System.out.println("option name futility_pruning_margin type spin default "+ai.getFutility_pruning_margin()+" min 1 max 9999");

                            System.out.println("option name delta_pruning type check default "+ai.isUse_delta_pruning());
                            System.out.println("option name delta_pruning_margin type spin default "+ai.getDelta_pruning_margin()+" min 1 max 9999");
                            System.out.println("option name delta_pruning_big_margin type spin default "+ai.getDelta_pruning_big_margin()+" min 1 max 9999");


                            System.out.println("uciok");
                        }));
        cdb.registerCommand(
                new Command("isready")
                        .setExecutable(c -> {
                            System.out.println("readyok");
                        }));
        cdb.registerCommand(
                new Command("ucinewgame")
                        .setExecutable(c -> {
                            b = new FastBoard(Setup.DEFAULT);
                        }));
        cdb.registerCommand(
                new Command("print", "prints the board including the fen string to the console")
                        .setExecutable(c -> {
                            System.out.println(b);
                        }));
        cdb.registerCommand(
                new Command("position")
                        .registerArgument(new TextArgument("fen", false, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"))
                        .registerArgument(new TextArgument("moves", false, ""))
                        .setExecutable(c -> {
                            b = read_FEN(b, c.getTextArgument("fen").getValue());
                            if (c.getTextArgument("moves").isSet()) {
                                for (String s : c.getTextArgument("moves").getValue().split(" ")) {
                                    Move move = uciToMove(s, b);
                                    b.move(move);
                                }
                            }
                        }));
        cdb.registerCommand(
                new Command("go")
                        .registerArgument(new NumericArgument("wtime", false, 0d))
                        .registerArgument(new NumericArgument("btime", false, 60000d))
                        .registerArgument(new NumericArgument("winc", false, 0d))
                        .registerArgument(new NumericArgument("binc", false, 0d))
                        .registerArgument(new NumericArgument("movestogo", false, 0d))
                        .registerArgument(new NumericArgument("depth", false, 12d))
                        .setExecutable(c -> {
                            if (c.getNumericArgument("wtime").isSet() ||
                                c.getNumericArgument("btime").isSet() ||
                                c.getNumericArgument("winc").isSet() ||
                                c.getNumericArgument("binc").isSet() ||
                                c.getNumericArgument("movestogo").isSet()) {
                                ai.setLimit_flag(AdvancedSearch.FLAG_TIME_LIMIT);
                                ai.setLimit(timeManager.time(b.getActivePlayer(),
                                                             (int) (double) c.getNumericArgument("wtime").getValue(),
                                                             (int) (double) c.getNumericArgument("btime").getValue(),
                                                             (int) (double) c.getNumericArgument("winc").getValue(),
                                                             (int) (double) c.getNumericArgument("binc").getValue(),
                                                             (int) (double) c.getNumericArgument("movestogo").getValue()));
                            } else {
                                ai.setLimit_flag(AdvancedSearch.FLAG_DEPTH_LIMIT);
                                ai.setLimit((int) (double) c.getNumericArgument("depth").getValue());
                            }
                            String out = "bestmove " + moveToUCI(ai.bestMove(b), b);
                            System.out.println(out);
                            log(out + "\n");
                        }));
        cdb.registerCommand(
                new Command("perft", "print the perft results for the given position")
                        .registerArgument(new NumericArgument("depth", true, 5d))
                        .registerArgument(new BooleanArgument("dif", false))
                        .setExecutable(c -> System.out.println("total:" + Testing.perft_pseudo(b,
                                                                                               (int) (double) c.getNumericArgument("depth").getValue(),
                                                                                               new MoveListBuffer(20, 300),
                                                                                               c.getBooleanArgument("dif").getValue()))));

        cdb.registerCommand(
                new Command("eval", "print a detailed evaluation of the board")
                        .setExecutable(c -> ((AdvancedEvaluator)ai.getEvaluator()).printEvaluation(getBoard()))
        );


        System.out.println("           done! [" +String.format("%5s",(System.currentTimeMillis()-t)+ " ms") + "]");

    }

    public static void uciCommunication() {
        System.out.println("engine ready!");
//        evaluator2.setEvolvableValues(new double[]{
//                46.0, 51.0, 44.0, 40.0, 56.0, 38.0,
//                7.0, 17.0, 25.0, 7.0, -14.0, -9.0, -10.0,
//                -30.0, -38.0, -42.0, -41.0, -40.0, 36.0, 21.0,
//                -26.0, -58.0, 27.0, 1.0, -80.0, 4.0, 22.0, -11.0,
//                40.0, 43.0, 30.0, 28.0, 44.0, 39.0, 42.0, 30.0,
//                40.0, 63.0, 29.0, 37.0, 18.0, 9.0, -81.0, -88.0,
//                -89.0, -88.0, 77.0, 161.0, 10.0, -42.0, 117.0, 67.0, 19.0, 60.0, 49.0, 73.0
//        });

//        ai.getEvaluator().setEvolvableValues(new double[]{
//                161.0, 120.0, 28.0, 120.0, -29.0, 177.0, 591.0, 455.0, 463.0, 774.0, 2.0, 18.0,
//                37.0, 9.0, -26.0, -17.0, -27.0, -33.0, -19.0, -20.0, -19.0, -22.0, 21.0, 81.0,
//                -52.0, 24.0, 157.0, 51.0, -51.0, 84.0, 88.0, 18.0, 194.0, 120.0, -2.0, -10.0,
//                -29.0, -43.0, 591.0, 417.0, 461.0, 774.0, 55.0, 25.0, 34.0, 94.0, -9.0, 1.0,
//                -13.0, 9.0, -13.0, -20.0, -19.0, -21.0, 118.0, 187.0, 11.0, -14.0, 100.0, 11.0,
//                75.0, -15.0, -64.0, 63.0
//        });

        Scanner input = null;
        try{
            input = new Scanner(System.in);
            while (true) {
                String line = input.nextLine().trim();
                log("[IN] " + line + "\n");
                cdb.executeCommand(line);
            }
        }catch (Exception e){
            log("[EXCEPTION] " + e.getMessage()+ "\n");
            input.close();
        }
        
    }

    public static void useLog(boolean val) {
        if (val) {
            LOG_NAME = "waldi_" + System.currentTimeMillis();
        } else {
            LOG_NAME = null;
        }
    }

    public static void log(String s) {
        //System.out.println(s);
        if (LOG_NAME == null) return;
        try {
            new File("log/").mkdirs();
            new File("log/" + LOG_NAME + ".log").createNewFile();
            Files.write(Paths.get("log/" + LOG_NAME + ".log"), s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }

    /**
     * take a move in UCI notation (e2e4) and
     * transforms it to move object
     *
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
        fromx = fileToIndex(input.charAt(0));
        tox = fileToIndex(input.charAt(2));
        fromy = Character.getNumericValue(input.charAt(1)) - 1;
        toy = Character.getNumericValue(input.charAt(3)) - 1;
        from = board.index(fromx, fromy);
        to = board.index(tox, toy);
        int promotionTarget = input.length() > 4 ? "00rkbq".indexOf(input.charAt(4)) : 0;
        return board.generateMove(from, to, promotionTarget);
    }

    /**
     * brings the given move on the board to the uci notation
     *
     * @param move
     * @param board
     * @return
     */
    public static String moveToUCI(Move move, Board board) {
        String toReturn = "";
        int tox = board.x(move.getTo());
        int toy = board.y(move.getTo());
        int fromx = board.x(move.getFrom());
        int fromy = board.y(move.getFrom());
        toReturn = toReturn + indexToFile(fromx);
        toReturn = toReturn + (fromy + 1);
        toReturn = toReturn + indexToFile(tox);
        toReturn = toReturn + (toy + 1);
        if (move.isPromotion()) {
            toReturn += "00rnbq".toCharArray()[Math.abs(move.getPieceFrom())];
        }
        return toReturn;
    }

    /**
     * returns the commands database used to execute and parse uci commands
     * @return
     */
    public static CommandDataBase getCdb() {
        return cdb;
    }

    /**
     * returns the internal board object
     * @return
     */
    public static Board getBoard() {
        return b;
    }

    public static void main(String[] args) {
        uciCommunication();
    }
}