package io;

import ai.evaluator.*;
import ai.evaluator.decider.SimpleDecider;
import ai.ordering.ChiefOrderer;
import ai.ordering.DeweyOrderer;
import ai.ordering.NoOrderer;
import ai.search.AdvancedSearch;
import ai.time_manager.SimpleTimeManager;
import ai.time_manager.TimeManager;
import ai.tools.transpositions.TranspositionEntry;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
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
import io.sizeFetcher.InstrumentationAgent;

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
            new AdvancedEvaluatorNew(new SimpleDecider()),
            new DeweyOrderer(),
            senpaiReducer,
            AdvancedSearch.FLAG_TIME_LIMIT,
            1000);


    public static String getLogName() {
        return LOG_NAME;
    }

    public static String getENGINENAME() {
        return ENGINENAME;
    }

    public static Board getB() {
        return b;
    }

    public static TimeManager getTimeManager() {
        return timeManager;
    }

    public static SenpaiReducer getSenpaiReducer() {
        return senpaiReducer;
    }

    public static AdvancedSearch getAi() {
        return ai;
    }

    private static CommandDataBase cdb = new CommandDataBase();

    static {
        long t = System.currentTimeMillis();

        ai.getEvaluator().setEvolvableValues(new double[]{
                55.4703094475441, 13.648375478424141, -19.77593141178159, -16.224879318490203, -12.345671594506127, 20.3557362892776, -10.0, 49.421633996564154, 421.04475783854724, 9.641086486739166, -11.928263238269363, -38.18922184666997, 135.1224748592701, 612.3152195680466, 3.326648204218068, -7.708766996311868, -11.28661976567006, 24.880671312589662, 18.519102210498033, 31.029598715149334, 43.76123023196711, 451.8084526468821, 5.28137218904248, -5.632125715859021, -26.332175471409858, 34.2021603949306, -4.465078440616596, 4.989497688917051, 68.74920010635222, 1482.5767839966381, 1.807982886349699, 10.791399146837776, -11.28690159211988, 114.18549470842594, 5.3460352805394376, -57.081967552900494, -33.53754133417173, 23.847031547235893, 102.20623535972696, 18.490781061161112, 87.13077946644272, -16.286572437239293, -6.765374494492307, 75.5187170450166, -40.0, 130.25786563023865, 376.85095767293893, 12.054890300932136, -5.748839366003089, -20.81558761127461, 83.03494360851471, 732.14603878538, 4.923848211793169, -0.6943692249874183, 2.286807135769611, -8.342948227609005, -3.7467380667012167, 6.741856933251827, 76.08312043641705, 382.2816911053168, 8.191493775708867, 0.3888182950093371, 1.5020675749900578, 73.01276670659827, -13.631253788909248, 20.88737538608716, 47.980077292579445, 1191.740987410907, 11.354682559947324, -69.09967810559168, 9.129259760095925, 27.429651094159272, 1.6009076938645943, 50.780941378842314, 13.338632696382415, -3.527839234047727
        });

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
                                        break;

                                    case "null_moves":
                                        ai.setUse_null_moves(Boolean.parseBoolean(value));
                                        break;
                                    case "null_moves_reduction":
                                        ai.setNull_move_reduction(Integer.parseInt(value));
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
                            System.out.println("option name null_moves_reduction type spin default "+ai.getNull_move_reduction()+ " min 1 max 99");

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
                        .setExecutable(c -> ((AdvancedEvaluatorNew)ai.getEvaluator()).printEval((FastBoard) getBoard()))
        );


        System.out.println("           done! [" +String.format("%5s",(System.currentTimeMillis()-t)+ " ms") + "]");

    }

    public static void uciCommunication() {
        System.out.println("engine ready!");

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