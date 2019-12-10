package board;

import java.util.Objects;

public class Move {


    int from;
    int to;
    int pieceFrom;
    int pieceTo;

    public Move(int from, int to, int pieceFrom, int pieceTo) {
        this.from = from;
        this.to = to;
        this.pieceFrom = pieceFrom;
        this.pieceTo = pieceTo;
    }

    public Move(int from, int to, Board board) {
        this.from = from;
        this.to = to;
        this.pieceFrom = board.getPiece(from);
        this.pieceTo = board.getPiece(to);
    }

    public int getPieceFrom() {
        return pieceFrom;
    }

    public int getPieceTo() {
        return pieceTo;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public Move copy() {
        return new Move(from, to, pieceFrom, pieceTo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from == move.from &&
                to == move.to &&
                pieceFrom == move.pieceFrom &&
                pieceTo == move.pieceTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, pieceFrom, pieceTo);
    }

    @Override
    public String toString() {
        return "SlowMove{" +
                "from=" + from +
                ", to=" + to +
                ", pieceFrom=" + pieceFrom +
                ", pieceTo=" + pieceTo +
                '}';
    }

}
