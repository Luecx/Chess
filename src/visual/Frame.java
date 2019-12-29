package visual;


import board.Board;
import board.SlowBoard;
import board.setup.Setup;
import game.Game;
import game.Player;
import game.ai.evaluator.GeneticEvaluator;
import game.ai.evaluator.SimpleEvaluator;
import game.ai.ordering.NoahOrderer;
import game.ai.ordering.SystematicOrderer;
import game.ai.reducing.SimpleReducer;
import game.ai.search.AlphaBeta;
import game.ai.evaluator.FinnEvaluator;
import game.ai.ordering.SimpleOrderer;
import game.ai.search.MiniMax;
import game.ai.search.PVSearch;
import io.IOBoard;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

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

        b = IOBoard.read_lichess(new SlowBoard(), "rnb2rk1/p4ppp/2p3q1/2Pppb2/1p1PPnB1/1Q2B1P1/PP1NNP1P/R3K2R");
        //b = IOBoard.read_lichess(new SlowBoard(), "6k1/6pp/8/8/8/7q/8/5R1K");

        ArrayList<String> keys = new ArrayList<>();
        for(String s:args){
            keys.add(s);
        }

        //keys.add("white");
        //keys.add("black");

        PVSearch ai = new PVSearch(
                new FinnEvaluator(),
                new SystematicOrderer(),
                new SimpleReducer(),
                PVSearch.FLAG_DEPTH_LIMIT,
                12,4);


        ai.setUse_iteration(true);
        ai.setUse_null_moves(true);
        ai.setPrint_overview(true);
        ai.setUse_killer_heuristic(true);
        ai.setUse_LMR(true);
        ai.setUse_transposition(true);

        new Frame(b, new Player(){},ai);
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
