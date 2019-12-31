package visual;


import board.Board;
import board.SlowBoard;
import board.setup.Setup;
import game.Game;
import game.Player;
import game.ai.ordering.SystematicOrderer;
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

    public boolean isFlippedBoard() {
        return gamePanel.isFlippedBoard();
    }

    public void setFlippedBoard(boolean flippedBoard) {
        this.gamePanel.setFlippedBoard(flippedBoard);
    }

    public static void main(String[] args) {
        Board b = new SlowBoard(Setup.DEFAULT);

        //b = IOBoard.read_lichess(new SlowBoard(), "6k1/p4ppp/8/8/1p1K1P1P/1N4P1/r2N4/8");
        //b = IOBoard.read_lichess(new SlowBoard(), "6k1/6pp/8/8/8/7q/8/5R1K");
        //b = IOBoard.read_lichess(new SlowBoard(),"6k1/p4ppp/8/8/1p1K1P1P/1N4P1/r2N4/8 w - - 0 1");
        //b = IO.read_FEN(new SlowBoard(), "7k/6pp/8/8/1Q6/7q/8/5R1K w - - 0 1"); //should force draw

        ArrayList<String> keys = new ArrayList<>();
        for(String s:args){
            keys.add(s);
        }

//        keys.add("white");
//        keys.add("black");

        PVSearch ai1 = new PVSearch(
                new FinnEvaluator(),
                new SystematicOrderer(),
                new SimpleReducer(),
                PVSearch.FLAG_TIME_LIMIT,
                5000,4);

        PVSearch ai2 = new PVSearch(
                new FinnEvaluator(),
                new SystematicOrderer(),
                new SimpleReducer(),
                PVSearch.FLAG_TIME_LIMIT,
                5000,4);

        ai1.setUse_iteration(true);
        ai1.setUse_null_moves(true);
        ai1.setPrint_overview(true);
        ai1.setUse_killer_heuristic(true);
        ai1.setUse_LMR(true);
        ai1.setUse_transposition(true);
        ai1.setPrint_overview(true);

        ai2.setUse_iteration(true);
        ai2.setUse_null_moves(true);
        ai2.setPrint_overview(true);
        ai2.setUse_killer_heuristic(true);
        ai2.setUse_LMR(true);
        ai2.setUse_transposition(false);

        Player hm = new Player(){};


        //Player white = keys.contains("white") ? hm:ai;
        //Player black = keys.contains("black") ? hm:ai;

        boolean flip = keys.contains("flip");


        //new Frame(b,ai1,ai2);
        new Frame(b,new Player(){},ai1);
        //new Frame(b, white, black).setFlippedBoard(flip);
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
