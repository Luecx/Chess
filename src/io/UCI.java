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
    private static String ENGINENAME = "Waldi"; // we should decide on a name of the engine
    private static Board b = new FastBoard(Setup.DEFAULT);

    private static TimeManager timeManager = new SimpleTimeManager();
    private static AdvancedSearch ai = new AdvancedSearch(
            //new AdvancedEvaluator(new SimpleDecider()),
            new SimpleEvaluator(),
            new SystematicOrderer2(),
            new SenpaiReducer(1),
            AdvancedSearch.FLAG_TIME_LIMIT,
            1000);


    private static CommandDataBase cdb = new CommandDataBase();

    public static void uciCommunication() {
        System.out.println("starting engine...");
        ai.getEvaluator().setEvolvableValues(new double[]{
                100.0, 100.0, 100.0, 100.0, 98.0, 100.0, 219.0, 970.0, 673.0, 690.0,
                1371.0, 20039.0, 17.0, 18.0, 20.0, 15.0, -31.0, -16.0, -8.0, -46.0,
                -43.0, -46.0, -42.0, -44.0, 57.0, 86.0, -28.0, -69.0, 111.0, 22.0,
                58.0, 49.0, 4.0, 11.0, 101.0, 100.0, 101.0, 101.0, 99.0, 100.0, 219.0,
                970.0, 668.0, 685.0, 1370.0, 20038.0, 15.0, 3.0, 5.0, 60.0, 0.0, 10.0,
                -5.0, -32.0, -30.0, -41.0, -42.0, -45.0, 56.0, 86.0, -15.0, -66.0,
                118.0, 21.0, 60.0, 48.0, 2.0, 9.0
        });
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
                                    case "razoring":
                                        ai.setUse_razoring(Boolean.parseBoolean(value));
                                        break;
                                }
                            }
                        }));
        cdb.registerCommand(
                new Command("uci")
                        .setExecutable(c -> {
                            System.out.println("id name " + ENGINENAME);
                            System.out.println("id author Finn/Noah");
                            System.out.println("option name null_moves type check default true");
                            System.out.println("option name lmr type check default true");
                            System.out.println("option name killers type check default true");
                            System.out.println("option name transpositions type check default true");
                            System.out.println("option name iterative type check default true");
                            System.out.println("option name razoring type check default false");
                            System.out.println("option name log type check default false");
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
                        .registerArgument(new BooleanArgument("dif", false, false))
                        .setExecutable(c -> System.out.println("total:" + Testing.perft_pseudo(b,
                                                                                               (int) (double) c.getNumericArgument("depth").getValue(),
                                                                                               new MoveListBuffer(20, 300),
                                                                                               c.getBooleanArgument("dif").getValue()))));
        Scanner input = new Scanner(System.in);
        while (true) {
            String line = input.nextLine().trim();
            log("[IN] " + line + "\n");
            cdb.executeCommand(line);
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


    public static void main(String[] args) {
        uciCommunication();
    }
}