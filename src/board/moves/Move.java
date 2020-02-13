package board.moves;

import board.Board;

import java.util.Objects;

public class Move {



    public static final byte DEFAULT = 0;
    public static final byte EN_PASSENT = 1;
    public static final byte PROMOTION = 2;
    public static final byte CASTLING = 3;

    int from;
    int to;

    int pieceFrom;
    int pieceTo;

    byte    type;

    short metaInformation;
    long secureFields;
    long enPassentField;

    boolean isNull; // true if it is a null-move ie a "pass"
    int orderPriority;



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
        Move copy = new Move(from, to, pieceFrom, pieceTo, metaInformation);
        copy.setSecureFields(secureFields);
        copy.setEnPassentField(enPassentField);
        copy.setType(type);
        copy.setOrderPriority(orderPriority);
        return copy;
    }

    public int getOrderPriority() { return orderPriority; }

    public long getSecureFields() {
        return secureFields;
    }

    public void setSecureFields(long secureFields) {
        this.secureFields = secureFields;
    }

    public long getEnPassentField() {
        return enPassentField;
    }

    public void setEnPassentField(long enPassentField) {
        this.enPassentField = enPassentField;
    }


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public boolean isCastle_move() {
        return type == CASTLING;
    }

    public boolean isEn_passent_capture() {
        return type == EN_PASSENT;
    }

    public boolean isPromotion() {
        return type == PROMOTION;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setPieceFrom(int pieceFrom) {
        this.pieceFrom = pieceFrom;
    }

    public void setPieceTo(int pieceTo) {
        this.pieceTo = pieceTo;
    }

    public void setNull(boolean aNull) {
        isNull = aNull;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from == move.from &&
                to == move.to &&
                pieceFrom == move.pieceFrom &&
                pieceTo == move.pieceTo &&
                metaInformation == move.metaInformation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, pieceFrom, pieceTo, metaInformation);
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


    public void setOrderPriority(int orderPriority) {
        this.orderPriority = orderPriority;
    }



}
