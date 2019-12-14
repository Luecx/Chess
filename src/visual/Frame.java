package visual;


import board.Board;
import board.SlowBoard;
import game.Game;
import game.Player;
import game.ai.evaluator.SimpleEvaluator;
import game.ai.search.AlphaBeta;
import game.ai.evaluator.FinnEvaluator;
import game.ai.ordering.SimpleOrderer;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

        this.setVisible(true);
    }

    public static void main(String[] args) {
        Player player = new Player() {};
        Player enemy = new AlphaBeta(new SimpleEvaluator(), new SimpleOrderer(), 6,6);
        ((AlphaBeta) enemy).setUse_iteration(true);
        ((AlphaBeta) enemy).setUse_transposition(false);
        ((AlphaBeta) enemy).setPrint_overview(true);

        Frame f = new Frame(new SlowBoard(), player, enemy);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {


        //gamePanel.undo();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
