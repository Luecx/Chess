package ai.monte_carlo.nodedata;

import board.Board;
import board.moves.Move;
import io.IO;

public class ChessNodeData implements NodeData {

    private Board board;
    private Move move;

    public ChessNodeData(Move move) {
        this.move = move;
    }

    public ChessNodeData(Board board, Move move) {
        this.board = board;
        this.move = move;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    @Override
    public String toString() {
        return IO.algebraicNotation(board, move);
    }
}
