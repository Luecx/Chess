package board.moves;

import board.Board;
import board.SlowBoard;
import game.ai.evaluator.NoahEvaluator;

import java.util.Objects;

public class Move {


    int from;
    int to;
    int pieceFrom;
    int pieceTo;
    boolean isNull; // true if it is a null-move ie a "pass"
    int orderPriority;
    static SlowBoard tokenSB = new SlowBoard();

    short metaInformation;

    public Move(int from, int to, int pieceFrom, int pieceTo) {
        this.from = from;
        this.to = to;
        this.pieceFrom = pieceFrom;
        this.pieceTo = pieceTo;
        setOrderPriority();
        this.isNull = false;
//        System.out.println(from);
//        System.out.println(to);
    }

    public Move(int from, int to, Board board) {
        this.from = from;
        this.to = to;
        this.pieceFrom = board.getPiece(from);
        this.pieceTo = board.getPiece(to);
        setOrderPriority();
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

    public int getOrderPriority() { return orderPriority; }

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

//    int from;
//    int to;
//    int pieceFrom;
//    int pieceTo;
    public void setOrderPriority() {
        int priority = 0;
        int color = pieceFrom > 0 ? 1 : -1;
        priority += Math.abs(NoahEvaluator.EVALUATE_PRICE[Math.abs(pieceTo)]);
        priority -= Math.abs(pieceFrom); //to capture with least valuable piece
        if (color == 1) {
            priority += NoahEvaluator.W_POSITION_PRICE.get(Math.abs(pieceFrom)-1,tokenSB.x(to),tokenSB.y(to));
            priority -= NoahEvaluator.W_POSITION_PRICE.get(pieceFrom-1,tokenSB.x(from),tokenSB.y(from));
        }
        if (color == -1) {
            priority += NoahEvaluator.B_POSITION_PRICE.get(Math.abs(pieceFrom)-1,tokenSB.x(to),tokenSB.y(to));
            priority -= NoahEvaluator.B_POSITION_PRICE.get(Math.abs(pieceFrom)-1,tokenSB.x(from),tokenSB.y(from));
        }

        orderPriority = priority;
    }

    public static void main(String[] args) {
        System.out.println(tokenSB.y(5));
        Move m = new Move(41,42,1,1);

    }

}
