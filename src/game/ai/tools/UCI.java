package game.ai.tools;

import board.Board;
import board.SlowBoard;
import board.setup.Setup;
import io.IO;


//// coppied from someone else, probabally not using it, and will instead try to do something with Python
/// because I already have some thing for that.


import java.util.*;
public class UCI {
    static String ENGINENAME = ""; // we should decide on a name of the engine
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
        System.out.println("id author Jonathan");
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
    public static SlowBoard inputPosition(String input) {
        SlowBoard b = new SlowBoard(Setup.DEFAULT);;
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
            //make each of the moves
        }
        return b;
    }
    public static void inputGo() {
        //search for best move
    }
    public static void inputPrint() {
        //BoardGeneration.drawArray(UserInterface.WP,UserInterface.WN,UserInterface.WB,UserInterface.WR,UserInterface.WQ,UserInterface.WK,UserInterface.BP,UserInterface.BN,UserInterface.BB,UserInterface.BR,UserInterface.BQ,UserInterface.BK);
    }
}