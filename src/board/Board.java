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

    /**
     * Clears the board. This method should remove every
     * piece that is on the board.
     */
    public abstract void clear();

    /**
     * The methods returns a Stack of the move history.
     * <p>
     * Note that castling equals 2 Moves on the Stack.
     * @return
     */
    public Stack<Move> getMoveHistory() {
        return moveHistory;
    }

    /**
     * This resets the board to the initial state.
     * It should also place the the initial pieces
     * on the board.
     */
    public abstract void reset();

    /**
     * It returns a list of all moves that are available for the current
     * active player.
     * <p>
     * This method does not check any mate-threats!
     * <p>
     * @return All the available moves
     */
    public abstract List<Move> getAvailableMovesShallow();

    /**
     * It returns a list of all moves that are available for the current
     * active player.
     * <p>
     * This method does check mate threats and will return a zero list
     * if the king is checkmated.
     *
     * @return All the available moves
     */
    public abstract List<Move> getAvailableMovesComplete();

    /**
     * The method returns true if one side has won the game
     *
     * @return      true if the game is over, otherwise false.
     */
    public abstract boolean isGameOver();

    /**
     * The method transforms the internally stored index that is
     * used for moves to an x-coordinate.
     * the coordinate system starts in the bottom left with x
     * going to the right.
     *
     * @param index     = index(file, rank) = unique reference to a field.
     * @return corresponding x-coordinate.
     */
    public abstract int x(int index);

    /**
     * The method transforms the internally stored index that is
     * used for moves to an y-coordinate.
     * the coordinate system starts in the bottom left with y
     * going to the top.
     *
     * @param index
     * @return corresponding y-coordinate.
     */
    public abstract int y(int index);

    /**
     * the method transforms any (x,y), (column,row), (file, rank)
     * to an unique index. This index is unique and only references
     * one exact field on the board.
     * @param x = column = file
     * @param y = row = rank
     * @return
     */
    public abstract int index(int x, int y);

    /**
     * the methods returns the piece on field given (x,y).
     * The following mapping should be used:
     *
     * 0 : Empty
     * 1 : Pawn
     * 2 : Rook
     * 3 : Knight
     * 4 : Bishop
     * 5 : Queen
     * 6 : King
     *
     * Where a minus (-) sign indicates that it is a black piece.
     *
     * @param x = column = file
     * @param y = row = rank
     * @return the piece on the field
     */
    public abstract int getPiece(int x, int y);

    /**
     * the methods sets the piece on field given (x,y).
     * The following mapping should be used:
     *
     * 0 : Empty
     * 1 : Pawn
     * 2 : Rook
     * 3 : Knight
     * 4 : Bishop
     * 5 : Queen
     * 6 : King
     *
     * Where a minus (-) sign indicates that it is a black piece.
     *
     * @param x = column = file
     * @param y = row = rank
     * @param piece the piece to be placed according to the mapping
     */
    public abstract void setPiece(int x, int y, int piece);

    /**
     * the methods sets the piece on field given the unique index.
     * The following mapping should be used:
     *
     * 0 : Empty
     * 1 : Pawn
     * 2 : Rook
     * 3 : Knight
     * 4 : Bishop
     * 5 : Queen
     * 6 : King
     *
     * Where a minus (-) sign indicates that it is a black piece.
     *
     * @param index = index(file, rank) = unique reference to a field.
     * @param piece The piece to be placed
     */
    public abstract void setPiece(int index, int piece);

    /**
     * the methods returns the piece on field given the unique index.
     * The following mapping should be used:
     *
     * 0 : Empty
     * 1 : Pawn
     * 2 : Rook
     * 3 : Knight
     * 4 : Bishop
     * 5 : Queen
     * 6 : King
     *
     * Where a minus (-) sign indicates that it is a black piece.
     *
     * @param index = index(file, rank) = unique reference to a field.
     * @return the piece on the field
     */
    public abstract int getPiece(int index);

    /**
     * the method changes the active player that has to move next.
     *
     */
    public void changeActivePlayer() {
        activePlayer = (byte) -activePlayer;
    }

    /**
     * given a move m, this method will process the move on the board.
     * It will not check if the move is valid or not.
     * The method needs to take care of the following things:
     *  - changing the active player after the move
     *  - processing Castling moves
     * @param m     the move to process
     */
    public abstract void move(Move m);

    /**
     * this method should undo the last move and change the active player.
     */
    public abstract void undoMove();

    /**
     * the method is used to determine who won the game
     * @return +1 if white won/-1 if black won
     */
    public abstract int winner();

    /**
     * this method should create an exact deep copy of the board.
     * it should take care of the following:
     *  - pieces on the board
     *  - active player
     *  - move history
     *  - castling available
     *  - en passent available
     *
     * @return an exact copy
     */
    public abstract T copy();

    /**
     * this methods should create an empty instance of this board.
     *
     * @return      the new instance that has been reset
     */
    public abstract T newInstance();

    /**
     * the method recalculates the zobrist key of the current board
     * @return      the zobrist key for the board
     */
    public abstract long zobrist();

    /**
     * the method returns +1 if white is to move next.
     * If black has to move next, it will return -1.
     * @return      the active player that has to move
     */
    public byte getActivePlayer() {
        return activePlayer;
    }
}
