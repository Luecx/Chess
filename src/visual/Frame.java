package visual;


import board.Board;
import board.SlowBoard;
import board.setup.Setup;
import game.Game;
import game.Player;
import game.ai.evaluator.NoahEvaluator;
import game.ai.ordering.SystematicOrderer;
import game.ai.reducing.SenpaiReducer;
import game.ai.reducing.SimpleReducer;
import game.ai.evaluator.FinnEvaluator;
import game.ai.search.PVSearch;
import io.IO;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

/**
 * Created by Luecx on 08.08.2017.
 */
public class Frame extends JFrame implements KeyListener {

    private GamePanel gamePanel;

    public Frame(Game game) {
        super();

        gamePanel = new GamePanel(game, this);

        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.add(gamePanel);

        this.addKeyListener(this);
        this.gamePanel.addKeyListener(this);

        this.setVisible(true);
    }

    public Frame(Board board, Player white, Player black) {
        super();

        gamePanel = new GamePanel(new Game(board, white, black), this);

        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setSize(800, 800);
        this.add(gamePanel);

        this.addKeyListener(this);
        this.gamePanel.addKeyListener(this);

        this.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent evt) {
                gamePanel.render();
            }
        });

        this.setVisible(true);
        this.gamePanel.render();
    }

    public Frame(Board board, Player white, Player black, ColorScheme colorScheme) {
        this(board, white, black);
        gamePanel.setColorScheme(colorScheme);
    }

    public boolean isFlippedBoard() {
        return gamePanel.isFlippedBoard();
    }

    public void setFlippedBoard(boolean flippedBoard) {
        this.gamePanel.setFlippedBoard(flippedBoard);
    }

    public static void main(String[] args) {
        Board b = new SlowBoard(Setup.DEFAULT);

        //b = IOBoard.read_lichess(new SlowBoard(), "6k1/p4ppp/8/8/1p1K1P1P/1N4P1/r2N4/8");
        //b = IO.read_FEN(new SlowBoard(),"8/8/8/8/3k4/5q2/3K4/8 w - - 0 1");
        //b = IO.read_FEN(new SlowBoard(), "7k/6pp/8/8/1Q6/7q/8/5R1K w - - 0 1"); //should force draw
        //b = IO.read_FEN(new SlowBoard(), "r3k2r/ppp3pp/5p2/3Pp3/2P3PN/1P1b1P1P/4R1K1/2B5 w - - 0 1");
        //b = IO.read_FEN(new SlowBoard(), "r3k2r/ppp3pp/5p2/3Pp3/2P3PN/1P1b1P1P/4R1K1/2B5 w - - 0 1");
        //b = IO.read_FEN(new SlowBoard(), "r1k5/ppp2p1p/6b1/8/r4P2/2P2B2/PP5P/3R2R1 w - - 0 1");

        //b = IO.read_FEN(new SlowBoard(), "8/4k3/8/4K3/4P3/8/8/8 w - - 0 1"); //KvP

        ArrayList<String> keys = new ArrayList<>();
        for(String s:args){
            keys.add(s);
        }

        boolean w = false;

        if(w){
            keys.add("black");
            keys.add("flip");
        }else{

            keys.add("white");
        }



        Player hm = new Player(){};

        PVSearch ai = new PVSearch(
                new NoahEvaluator(),
                new SystematicOrderer(),
                new SenpaiReducer() ,
                PVSearch.FLAG_TIME_LIMIT,
                5000,10);
        ai.setUse_iteration(true);
        ai.setUse_null_moves(true);
        ai.setPrint_overview(true);
        ai.setUse_killer_heuristic(true);
        ai.setUse_LMR(true);
        ai.setUse_transposition(false);
        ai.setPrint_overview(true);






        Player white = keys.contains("white") ? hm:ai;
        Player black = keys.contains("black") ? hm:ai;

        boolean flip = keys.contains("flip");

        new Frame(b, white, black).setFlippedBoard(flip);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        gamePanel.undo();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
