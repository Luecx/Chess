package board;

import board.bitboards.BitBoard;
import board.moves.Move;
import board.moves.MoveList;
import board.setup.Setup;
import io.IO;

import java.util.List;
import java.util.Stack;

public abstract class Board<T extends Board<T>> {


    protected byte activePlayer = 1;
    private boolean isEndgame = false;
    protected Stack<Move> moveHistory = new Stack<>();

    public Board(Setup setup) {
        this.setup(setup);
    }

    public Board() {
        this.reset();
    }


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
     * The method resets the board.
     * This includes:
     *      - resetting the meta information
     *      - clearing the board
     */
    public abstract void reset();

    /**
     * The method uses a predefined setup to
     * first reset the board and then place the pieces on the board
     * @param setup     the setup to apply
     */
    public void setup(Setup setup){
        setup.apply(this);
    }

    /**
     * returns the board represented as a string.
     * Capital letters for white pieces.
     */
    @Override
    public String toString() {



        StringBuilder builder = new StringBuilder();



        builder.append(String.format("%-40s : %-5d %n", "zobrist key", this.zobrist()));
        builder.append(String.format("%-40s : %-5d %n", "repetition", getCurrentRepetitionCount()));
        builder.append(String.format("%-40s : %-5d %n", "50 move rule",getCurrent50MoveRuleCount()));
        builder.append(String.format("%-40s : %-5s %n", "white kingside castle", getCastlingChance(1) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "white queenside castle", getCastlingChance(0) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "black kingside castle", getCastlingChance(3) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "black queenside castle", getCastlingChance(2) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "en passent square", getEnPassantSquare() >= 0 ?
                IO.getSquareString(getEnPassantSquare()): "-"));


        builder.append(" ┌───┬───┬───┬───┬───┬───┬───┬───┐\n");

        for (int r = 7; r >= 0; --r)
        {
            for (int f = 0; f <= 7; ++f)
                builder.append(" | " + IO.getPieceChar(getPiece(f,r)));

            if(r != 0){
                builder.append(" |\n ├───┼───┼───┼───┼───┼───┼───┼───┤\n");
            }else{
                builder.append(" |\n └───┴───┴───┴───┴───┴───┴───┴───┘\n");
            }
        }
        builder.append("fen: " + IO.write_FEN(this));


        return builder.toString();

    }




    /**
     * It returns a list of all moves that are available for the current
     * active player.
     * The method might return null if and only if the previous move from
     * the opponent was an illegal move
     * @return All the available moves
     */
    public abstract List<Move> getPseudoLegalMoves();

    /**
     * It returns a list of all moves that are available for the current
     * active player.
     * The method might return null if and only if the previous move from
     * the opponent was an illegal move
     * @return All the available moves
     */
    public abstract List<Move> getPseudoLegalMoves(MoveList list);

    /**
     * It returns a list of all moves that are available for the current
     * active player.
     * <p>
     * This method does check mate threats and will return a zero list
     * if the king is checkmated.
     * it should return an empty list if the king is checkmates
     * if the king is stalemated, the list should contain a single null move
     *
     * @return All the available moves
     */
    public abstract List<Move> getLegalMoves(MoveList list);

    /**
     * It returns a list of all moves that are available for the current
     * active player.
     * <p>
     * This method does check mate threats and will return a zero list
     * if the king is checkmated.
     * it should return an empty list if the king is checkmates
     * if the king is stalemated, the list should contain a single null move
     *
     * @return All the available moves
     */
    public abstract List<Move> getLegalMoves();

    /**
     * returns a list off all moves that capture a piece.
     * @return
     */
    public abstract List<Move> getCaptureMoves();

    /**
     * returns a list off all moves that capture a piece.
     * @return
     */
    public abstract List<Move> getCaptureMoves(MoveList list);




    /**
     * the method returns true if the given players king is at check
     *
     * @param player
     * @return
     */
    public abstract boolean isInCheck(int player);

    /**
     * The method returns true if the game is over due to repetitions/50-move rule
     *
     *
     * @return      true if the game is over, otherwise false.
     */
    public abstract boolean isDraw();




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
     * does a null move
     */
    public abstract void move_null();

    /**
     * undoes a null move
     */
    public abstract void undoMove_null();

    /**
     * tests if the given move is legal or not
     * @param m
     * @return
     */
    public abstract boolean isLegal(Move m);

    /**
     * returns true if the move puts the opponent king into check
     * @param m
     * @return
     */
    public abstract boolean givesCheck(Move m);

    /**
     * generates a new move object that can later be used for the move() method.
     * @param from
     * @param to
     * @param promotionTarget
     * @return
     */
    public abstract Move generateMove(int from, int to, int promotionTarget);

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
     * returns true if its possible to castle according to the following list:
     *    index = 0: white long castle
     *    index = 1: white short castle
     *    index = 2: black long castle
     *    index = 3: black short castle
     */
    public abstract boolean getCastlingChance(int index);

    /**
     * enables/disables castling according to the following list:
     *    index = 0: white long castle
     *    index = 1: white short castle
     *    index = 2: black long castle
     *    index = 3: black short castle
     */
    public abstract void setCastlingChance(int index, boolean value);

    /**
     * returns the amount of repetitions of the current game state
     * @return
     */
    public abstract int getCurrentRepetitionCount();

    /**
     * returns the counter for the 50 move rule.
     * @return
     */
    public abstract int getCurrent50MoveRuleCount();

    /**
     * returns the target square for en passant captures. returns -1 if en passant is not possible
     */
    public abstract int getEnPassantSquare();

    /**
     * enables en passant to the given square.
     * if the square is -1, en passant should be disabled
     */
    public abstract void setEnPassantSquare(int square);

    /**
     * the method returns +1 if white is to move next.
     * If black has to move next, it will return -1.
     * @return      the active player that has to move
     */
    public byte getActivePlayer() {
        return activePlayer;
    }

    /**
     * returns true if it is the endgame
     * @return endgame flag
     */
    public boolean isEndgame() {
        return isEndgame;
    }

    /**
     * returns sets the isEndgame flag
     * @param endgame      the flag
     */
    public void setEndgame(boolean endgame) {
        isEndgame = endgame;
    }


}
