package game;

import board.Board;
import board.moves.Move;
import game.ai.search.AI;
import io.IO;

import java.util.ArrayList;

/**
 * This class can be used to play a game of chess.
 * It enables the option to play against an AI or even to let 2 AI's play
 * against each other.
 *
 */
public class Game {

    private Board board;
    private Player playerWhite;
    private Player playerBlack;

    private boolean isInterrupted = false;

    private ArrayList<Runnable> listeners = new ArrayList<>();

    /**
     * the constructor takes a board to be played on and two player instances.
     * A subclass of AI will be treated as an AI.
     * @param board
     * @param playerWhite
     * @param playerBlack
     */
    public Game(Board board, Player playerWhite, Player playerBlack) {
        this.playerWhite = playerWhite;
        this.playerBlack = playerBlack;
        this.board = board;
    }

    /**
     * it interrupts the automatic moving of an AI.
     * Usually used in a listener if 2 AI's play against each other.
     */
    public void interrupt() {
        this.isInterrupted = true;
    }

    /**
     * this methods returns true if the next move has to be done by a human.
     * it means that move() needs to be called.
     *
     */
    public boolean humansTurn() {
       return  board.getActivePlayer() == 1 && ! (playerWhite instanceof AI)||
               board.getActivePlayer() == -1 && ! (playerBlack instanceof AI);
    }

    /**
     * @return the board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * listeners can be added which are called once a move has been done by either
     * a human or the AI.
     * @param onChange the listener implementing run()
     */
    public void addBoardChangedListener(Runnable onChange){
        listeners.add(onChange);
    }

    /**
     * processes a move of a human.
     * assuming that 2 AI's play against each other, this method can be called using null as its
     * argument. This will trigger both AI's to start playing and will only stop if
     * one AI has won or interrupt() has been called.
     * The method will continue until the human has to move again.
     * @param m
     */
    public void move(Move m){
        if(board.isGameOver()) return;
        if (humansTurn()){
            //System.out.println("Moving: " + IO.algebraicNotation(board, m));
            board.move(m);
            listeners.forEach(Runnable::run);
            //System.out.println(board.isGameOver() + "  " + board.winner());
        }while(!humansTurn() && !board.isGameOver() && !isInterrupted){
            AI AI = board.getActivePlayer() == 1 ? (AI) playerWhite: (AI) playerBlack;
            Move move = AI.bestMove(board.copy());
            if(move == null)
                return;
            //System.out.println("Moving: " + IO.algebraicNotation(board, move));
            //System.out.println("fen: " + IO.write_FEN(board));
            board.move(move);
            listeners.forEach(Runnable::run);
        }
        this.isInterrupted = false;
    }

    /**
     * returns the player object that plays the white pieces
     * @return  the player object
     */
    public Player getPlayerWhite() {
        return playerWhite;
    }

    /**
     * returns the player object that plays the black pieces
     * @return  the player object
     */
    public Player getPlayerBlack() {
        return playerBlack;
    }
}
