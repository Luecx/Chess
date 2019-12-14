package game;

import board.Board;
import board.Move;
import game.ai.search.AI;

import java.util.ArrayList;

public class Game {

    private Board board;
    private Player playerWhite;
    private Player playerBlack;

    private boolean isInterrupted = false;

    private ArrayList<Runnable> listeners = new ArrayList<>();

    public Game(Board board, Player playerWhite, Player playerBlack) {
        this.playerWhite = playerWhite;
        this.playerBlack = playerBlack;
        this.board = board;
    }

    public void interrupt() {
        this.isInterrupted = true;
    }

    public boolean humansTurn() {
       return  board.getActivePlayer() == 1 && ! (playerWhite instanceof AI)||
               board.getActivePlayer() == -1 && ! (playerBlack instanceof AI);
    }

    public Board getBoard() {
        return board;
    }

    public void addBoardChangedListener(Runnable onChange){
        listeners.add(onChange);
    }

    public void move(Move m){
        if(board.isGameOver()) return;
        if (humansTurn()){
            board.move(m);
            listeners.forEach(Runnable::run);
        }while(!humansTurn() && !board.isGameOver() && !isInterrupted){
            AI AI = board.getActivePlayer() == 1 ? (AI) playerWhite: (AI) playerBlack;
            board.move(AI.bestMove(board.copy()));
            listeners.forEach(Runnable::run);
        }
        this.isInterrupted = false;
    }


}
