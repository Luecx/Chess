package board;

import board.bitboards.BitBoard;
import board.moves.Move;
import board.pieces.PieceList;

import java.util.*;

public class FastBoard extends Board<FastBoard> {


    long[] white_values;        //6
    long[] black_values;        //6
    long[] team_total;          //2
    long[] occupied;            //2


    private PieceList[] white_pieces;
    private PieceList[] black_pieces;

    private Stack<Move> moveHistory = new Stack<>();

    public FastBoard() {
        super();
    }

    public void reset() {
        white_values = new long[6];
        black_values = new long[6];
        team_total = new long[2];
        occupied = new long[2];
        white_pieces = new PieceList[6];
        black_pieces = new PieceList[6];
        Random r = new Random((int) (Math.random() * 100000));
        for (int i = 0; i < 6; i++) {
            white_pieces[i] = new PieceList(i+1);
            black_pieces[i] = new PieceList(-(i+1));
        }
        setPiece(-2, 0 + 7 * 8);
        setPiece(-2, 7 + 7 * 8);
        setPiece(-3, 1 + 7 * 8);
        setPiece(-3, 6 + 7 * 8);
        setPiece(-4, 2 + 7 * 8);
        setPiece(-4, 5 + 7 * 8);
        setPiece(-5, 3 + 7 * 8);
        setPiece(-6, 4 + 7 * 8);
        setPiece(2, 0);
        setPiece(2, 7);
        setPiece(3, 1);
        setPiece(3, 6);
        setPiece(4, 2);
        setPiece(4, 5);
        setPiece(5, 3);
        setPiece(6, 4);
        for (int i = 0; i < 8; i++) {
            setPiece(1, 8 + i);
            setPiece(-1, 48 + i);
        }
        update_longs();
    }

    @Override
    public boolean getCastlingChance(int index) {
        return false;
    }

    @Override
    public boolean getEnPassantChance(int file) {
        return false;
    }

    @Override
    public void setEnPassantChance(int file, boolean value) {
    }

    @Override
    public void setCastlingChance(int index, boolean value) {

    }

    @Override
    public int getCurrentRepetitionCount() {
        return 0;
    }

    public void setPiece(int piece, int index) {
        int p = getPiece(index);
        if (p > 0) {
            white_pieces[p - 1].remove(new Integer(index));
            white_values[p - 1] = BitBoard.unsetBit(white_values[p - 1], index);
        } else if (p < 0) {
            black_pieces[-p - 1].remove(new Integer(index));
            black_values[-p - 1] = BitBoard.unsetBit(black_values[-p - 1], index);
        }
        if (piece == 0) return;
        if (piece > 0) {
            if (!white_pieces[piece - 1].contains(index)) {
                white_pieces[piece - 1].add(index);
                white_values[piece - 1] = BitBoard.setBit(white_values[piece - 1], index);
            }
        } else {
            if (!black_pieces[-piece - 1].contains(index)) {
                black_pieces[-piece - 1].add(index);
                black_values[-piece - 1] = BitBoard.setBit(black_values[-piece - 1], index);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FastBoard board = (FastBoard) o;
        return Arrays.equals(white_values, board.white_values) &&
                Arrays.equals(black_values, board.black_values);
    }

    public void update_longs() {
        team_total[0] = white_values[0];
        team_total[1] = black_values[0];
        for (int i = 1; i < 6; i++) {
            team_total[0] = BitBoard.or(team_total[0], white_values[i]);
            team_total[1] = BitBoard.or(team_total[1], black_values[i]);
        }
        occupied[0] = BitBoard.or(team_total[0], team_total[1]);
        occupied[1] = BitBoard.not(occupied[0]);
    }
//    public LinkedList<Move> possibleMoves(int id) {
//        int p = id == 1 ? 0 : 1;
//        int e = id == 1 ? 1 : 0;
//
//        int moveIndex = 0;
//        LinkedList<Move> moves = new LinkedList<>();
//
//        int index = 0;
//        long[] out = new long[50];
//
//        if (p == 0) {
//            long b1 = and(team_total[1], shiftSouthWest(values[0]));
//            long b2 = and(team_total[1], shiftSouthEast(values[0]));
//            long b3 = and(occupied[1], shiftSouth(values[0]));
//
//            for (int i = 16; i < 64; i++) {
//                if (getBit(b1, i)) {
//                    out[index] = (i-9) * 64 + i;
//                    index ++;
//                    //moves.add(new Move(i - 9, i, 1));
//                }
//                if (getBit(b2, i)) {
//                    out[index] = (i-7) * 64 + i;
//                    index ++;
//                    //moves.add(new Move(i - 7, i, 1));
//                }
//                if (getBit(b3, i)) {
//                    out[index] = (i-8) * 64 + i;
//                    index++;
//                    //moves.add(new Move(i - 8, i, 1));
//                }
//            }
//        } else {
//            long b1 = and(team_total[0], shiftNorthEast(values[6]));
//            long b2 = and(team_total[0], shiftNorthWest(values[6]));
//            long b3 = and(occupied[1], shiftNorth(values[6]));
//
//
//            for (int i = 48; i >= 0; i--) {
//                if (getBit(b1, i)) {
//                    moves.add(new Move(i + 9, i, 1));
//                }
//                if (getBit(b3, i)) {
//                    moves.add(new Move(i + 8, i, 1));
//                }
//                if (getBit(b2, i)) {
//                    moves.add(new Move(i + 7, i, 1));
//                }
//
//            }
//        }
//
//        for(int i = 0; i < 64; i++){
//            if(getBit(values[1 + p * 6], i)){
//                long v = and(KNIGHT_TABLE[i], not(team_total[p]));
//                for(int n = Math.max(0, i - 18); n < Math.min(63, i + 18); n++){
//                    if(getBit(v, n)){
//                        out[index] = i * 64 + n;
//                        index++;
//                        //moves.add(new Move(i,n, 1 + p * 6));
//                    }
//                }
//            }
//        }
//
//        return moves;
//
//
//    }


    @Override
    public boolean isGameOver() {
        return false;
    }

    @Override
    public int x(int index) {
        return BitBoard.fileIndex(index);
    }

    @Override
    public int y(int index) {
        return BitBoard.rankIndex(index);
    }

    @Override
    public int index(int x, int y) {
        return BitBoard.squareIndex(y, x);
    }

    @Override
    public int getPiece(int x, int y) {
        return getPiece(index(x, y));
    }

    @Override
    public int getPiece(int index) {
        for (int i = 0; i < 6; i++) {
            if (BitBoard.getBit(white_values[i], index)) {
                return i + 1;
            }
            if (BitBoard.getBit(black_values[i], index)) {
                return -i - 1;
            }
        }
        return 0;
    }

    private void slidingPieces(int pos, int direction, ArrayList<Move> list) {
        int c = pos + direction;
//        while (field[c] != INVALID) {
//            if (field[c] != 0) {
//                if (field[c] * getActivePlayer() < 1) {
//                    list.add(new Move(pos, c, this));
//                }
//                return;
//            } else {
//                list.add(new Move(pos, c, this));
//            }
//            c += direction;
//        }
    }


    public List<Move> getPseudoLegalMoves() {
        LinkedList<Move> moves = new LinkedList<>();

        int index;
        for (int i = 0; i < white_pieces[2].size(); i++) {
            index = white_pieces[2].get(i);
            long attacks = BitBoard.KNIGHT_TABLE[index] & ~occupied[0];
            while(attacks != 0){
                //moves.add(new Move(index, BitBoard.bitscanForward(attacks), 1,0));
                BitBoard.bitscanForward(attacks);
                attacks = BitBoard.lsbReset(attacks);
            }
        }




//        for (byte i = 0; i < 8; i++) {
//            for (byte j = 0; j < 8; j++) {
//
//                int index = index(i, j);
//                if(getActivePlayer() == 1 && !BitBoard.getBit(occupied[0], index)){
//                        continue;
//                }else if(getActivePlayer() == -1 && !BitBoard.getBit(occupied[1], index)){
//                    continue;
//                }
//
//                int piece = getPiece(index);
//
////                if (piece * getActivePlayer() <= 0) continue;
////                if (piece == getActivePlayer()) { // Bauern
////                    if (j == 0 || j == 7) continue;
////                    if (getPiece(index + getActivePlayer() * 12) == 0) {
////                        moves.add(new Move(index, index + getActivePlayer() * 12, getActivePlayer(), (byte) 0));
////                        if ((j == getActivePlayer() || j == 7 + getActivePlayer()) && getPiece(index + getActivePlayer() * 2 * 12) == 0) {
////                            moves.add(new Move(index, index + getActivePlayer() * 24, getActivePlayer(), (byte) 0));
////                        }
////                    }
////                    if (field[index + getActivePlayer() * 11] != INVALID && field[index + getActivePlayer() * 11] * getActivePlayer() < 0) {
////                        moves.add(new Move(index, index + getActivePlayer() * 11, this));
////                    }
////                    if (field[index + getActivePlayer() * 13] != INVALID && field[index + getActivePlayer() * 13] * getActivePlayer() < 0) {
////                        moves.add(new Move(index, index + getActivePlayer() * 13, this));
////                    }
//                //}
//            }
//        }
        return moves;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            for (int n = 0; n < 8; n++) {
                int b = getPiece(n, i);
                if (b < -10) {
                    builder.append("#");
                } else {
                    builder.append(Math.abs(b));
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public long zobrist() {
        long zob = 0;
        for (int i = 0; i < 6; i++) {
            for (int n = 0; n < white_pieces[i].size(); n++){
                zob = BitBoard.xor(zob, BitBoard.white_hashes[i][n]);
            }
            for (int n = 0; n < black_pieces[i].size(); n++){
                zob = BitBoard.xor(zob, BitBoard.black_hashes[i][n]);
            }
        }
        return zob;
    }

    @Override
    public void move(Move m) {
//        if(m.getPieceFrom() * getActivePlayer() == 6){
//            if(Math.abs(m.getTo()-m.getFrom()) == 2){
//                if(m.getTo() > m.getFrom()){
//                    this.moveSimpleMove(new Move(m.getFrom() + 3, m.getFrom() + 1, this));
//                }else{
//                    this.moveSimpleMove(new Move(m.getFrom() -4, m.getFrom() -1, this));
//                }
//            }
//        }
        this.moveSimpleMove(m);
        this.changeActivePlayer();
        this.update_longs();
    }

    @Override
    public void undoMove() {
        this.undoMoveSimpleMove();
        this.changeActivePlayer();
        this.update_longs();
    }

    @Override
    public int winner() {
        return 0;
    }

    private void moveSimpleMove(Move m) {
        this.setPiece(0, m.getFrom());
        this.setPiece(m.getPieceFrom(), m.getTo());
        this.moveHistory.push(m);
    }

    private void undoMoveSimpleMove() {
        if (this.moveHistory.size() == 0) return;
        Move old = this.moveHistory.pop();
        this.setPiece(old.getPieceFrom(), old.getFrom());
        this.setPiece(old.getPieceTo(), old.getTo());
    }

    @Override
    public List<Move> getLegalMoves() {
        return null;
    }

    @Override
    public List<Move> getCaptureMoves() {
        return null;
    }

    @Override
    public FastBoard copy() {
        FastBoard copy = new FastBoard();
        copy.occupied[0] = occupied[0];
        copy.occupied[1] = occupied[1];
        copy.team_total[0] = team_total[0];
        copy.team_total[1] = team_total[1];
        for (int i = 0; i < 6; i++) {
            copy.white_values[i] = this.white_values[i];
            copy.black_values[i] = this.black_values[i];
            copy.white_pieces[i] = white_pieces[i].copy();
            copy.black_pieces[i] = black_pieces[i].copy();
        }
        if(this.getActivePlayer() != copy.getActivePlayer()){
            copy.changeActivePlayer();
        }
        return copy;
    }



    @Override
    public FastBoard newInstance() {
        return null;
    }

    @Override
    public void setPiece(int x, int y, int piece) {
    }

    public static void main(String[] args) {
        FastBoard board = new FastBoard();

        //System.out.println(board.getPseudoLegalMoves());


        long time = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis()-time < 3E3){
            board.getPseudoLegalMoves();
            count ++;
        }
        System.out.println(count / 3);

    }
}
