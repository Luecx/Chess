package board;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public abstract class Board<T extends Board<T>> {


    protected byte activePlayer = 1;
    protected Stack<Move> moveHistory = new Stack<>();


    public Board() {
        this.reset();
    }

    public Stack<Move> getMoveHistory() {
        return moveHistory;
    }

    public abstract void reset();

    public abstract List<Move> getAvailableMovesShallow();

    public abstract List<Move> getAvailableMovesComplete();

    public abstract boolean isGameOver();

    public abstract int x(int index);

    public abstract int y(int index);

    public abstract int index(int x, int y);

    public abstract int getPiece(int x, int y);

    public abstract int getPiece(int index);

    public void changeActivePlayer() {
        activePlayer = (byte) -activePlayer;
    }

    public abstract void move(Move m);

    public abstract void undoMove();

    public abstract T copy();

    public abstract long zobrist();


    public byte getActivePlayer() {
        return activePlayer;
    }
}
