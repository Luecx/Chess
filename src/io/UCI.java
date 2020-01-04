package io;

import board.Board;
import board.SlowBoard;
import board.moves.Move;
import board.setup.Setup;
import game.ai.search.PVSearch;
import io.IO;


//// coppied from someone else, probabally not using it, and will instead try to do something with Python
/// because I already have some thing for that.


import java.util.*;
public class UCI {
    static String ENGINENAME = "Waldi"; // we should decide on a name of the engine
    private static Board b = new SlowBoard(Setup.DEFAULT);;
    public static void uciCommunication() {
        while (true)
        {
            Scanner input = new Scanner(System.in);
            String inputString=input.nextLine();
            if ("uci".equals(inputString))
            {
                inputUCI();
            }
            else if (inputString.startsWith("setoption"))
            {
                inputSetOption(inputString);
            }
            else if ("isready".equals(inputString))
            {
                inputIsReady();
            }
            else if ("ucinewgame".equals(inputString))
            {
                inputUCINewGame();
            }
            else if (inputString.startsWith("position"))
            {
                inputPosition(inputString);
            }
            else if ("go".equals(inputString))
            {
                inputGo();
            }
            else if ("print".equals(inputString))
            {
                inputPrint();
            }
        }
    }
    public static void inputUCI() {
        System.out.println("id name "+ENGINENAME);
        System.out.println("id author Finn/Noah");
        //options go here
        System.out.println("uciok");
    }
    public static void inputSetOption(String inputString) {
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
            }
        }
        return b;
    }
    public static void inputGo() {
        Move best = PVSearch.bestMove(b);
        System.out.println(IO.moveToUCI(best,b));
    }
    public static void inputPrint() {
        //BoardGeneration.drawArray(UserInterface.WP,UserInterface.WN,UserInterface.WB,UserInterface.WR,UserInterface.WQ,UserInterface.WK,UserInterface.BP,UserInterface.BN,UserInterface.BB,UserInterface.BR,UserInterface.BQ,UserInterface.BK);
    }
}