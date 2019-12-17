package board.moves;

import board.Board;

import java.util.Objects;

public class Move {


    int from;
    int to;
    int pieceFrom;
    int pieceTo;
    boolean isNull; // true if it is a null-move ie a "pass"

    short metaInformation;

    public Move(int from, int to, int pieceFrom, int pieceTo) {
        this.from = from;
        this.to = to;
        this.pieceFrom = pieceFrom;
        this.pieceTo = pieceTo;
        this.isNull = false;
    }

    public Move(int from, int to, Board board) {
        this.from = from;
        this.to = to;
        this.pieceFrom = board.getPiece(from);
        this.pieceTo = board.getPiece(to);
        this.isNull = false;
    }

    public Move() { // to create null move
        this.isNull = true;
    }

    public Move(int from, int to, Board board, short metaInformation) {
        this.from = from;
        this.to = to;
        this.pieceFrom = board.getPiece(from);
        this.pieceTo = board.getPiece(to);
        this.metaInformation = metaInformation;
    }

    public Move(int from, int to, int pieceFrom, int pieceTo, short metaInformation) {
        this.from = from;
        this.to = to;
        this.pieceFrom = pieceFrom;
        this.pieceTo = pieceTo;
        this.metaInformation = metaInformation;
    }

    public short getMetaInformation() {
        return metaInformation;
    }

    public void setMetaInformation(short metaInformation) {
        this.metaInformation = metaInformation;
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

    public boolean getIsNull() { return isNull; }

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
