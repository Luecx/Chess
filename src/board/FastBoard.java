package board;

import board.bitboards.BitBoard;
import board.moves.Move;
import board.moves.MoveList;
import board.moves.MoveListBuffer;
import board.pieces.PieceList;
import board.repetitions.RepetitionList;
import board.setup.Setup;
import game.Game;
import game.Player;
import game.ai.evaluator.Evaluator;
import game.ai.evaluator.NoahEvaluator;
import game.ai.evaluator.NoahEvaluator2;
import io.IO;
import io.Testing;
import visual.Frame;

import java.util.*;

public class FastBoard extends Board<FastBoard> {


    private long[]          white_values;        //bitmap for each white piece
    private long[]          black_values;        //bitmap for each black piece
    private long[]          team_total;          //bitmap for white and black occupancy respectively
    private long            occupied;            //bitmap for occupied squares


    private int[]           indexBoard;   //contains indices for each square. positive indices for white
    private PieceList[]     white_pieces;
    private PieceList[]     black_pieces;

    private RepetitionList  repetitionList;
    private long            zobrist;

    public FastBoard() {
        super();
    }

    public FastBoard(Setup setup) {
        super(setup);
    }

    public void reset() {

        indexBoard = new int[64];

        white_values = new long[6];
        black_values = new long[6];
        team_total = new long[2];
        occupied = 0L;
        white_pieces = new PieceList[6];
        black_pieces = new PieceList[6];
        for (int i = 0; i < 6; i++) {
            white_pieces[i] = new PieceList(i+1);
            black_pieces[i] = new PieceList(-(i+1));
        }

        this.repetitionList = new RepetitionList();
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
            zobrist ^= BitBoard.white_hashes[p-1][index];
        } else if (p < 0) {
            black_pieces[-p - 1].remove(new Integer(index));
            black_values[-p - 1] = BitBoard.unsetBit(black_values[-p - 1], index);
            zobrist ^= BitBoard.white_hashes[-p - 1][index];
        }
        indexBoard[index] = piece;

        if (piece == 0) return;

        if (piece > 0) {
            white_pieces[piece - 1].add(index);
            white_values[piece - 1] = BitBoard.setBit(white_values[piece - 1], index);
            zobrist ^= BitBoard.white_hashes[piece-1][index];
        } else {
            black_pieces[-piece - 1].add(index);
            black_values[-piece - 1] = BitBoard.setBit(black_values[-piece - 1], index);
            zobrist ^= BitBoard.white_hashes[-piece - 1][index];
        }
        update_longs();
    }

    @Override
    public void setPiece(int x, int y, int piece) {
        setPiece(piece, index(x,y));
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
        occupied = BitBoard.or(team_total[0], team_total[1]);
    }

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
        return indexBoard[index];
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
        return zobrist;
//        long zob = 0;
//        for (int i = 0; i < 6; i++) {
//            for (int n = 0; n < white_pieces[i].size(); n++){
//                zob = BitBoard.xor(zob, BitBoard.white_hashes[i][n]);
//            }
//            for (int n = 0; n < black_pieces[i].size(); n++){
//                zob = BitBoard.xor(zob, BitBoard.black_hashes[i][n]);
//            }
//        }
//        return zob;
    }

    @Override
    public void move(Move m) {

        if(m.getIsNull()){
            this.changeActivePlayer();
            this.moveHistory.add(m);
            return;
        }



        this.moveSimpleMove(m);
        this.changeActivePlayer();
        this.update_longs();
        this.repetitionList.add(zobrist);
    }

    @Override
    public void undoMove() {

        if(this.moveHistory.size() == 0) return;

        Move last = moveHistory.peek();
        if(last.getIsNull()){
            this.changeActivePlayer();
            moveHistory.pop();
            return;
        }

        this.repetitionList.sub(zobrist);
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
    public FastBoard copy() {
        FastBoard copy = new FastBoard();
        copy.occupied = occupied;
        copy.repetitionList = this.repetitionList.copy();
        copy.zobrist = this.zobrist;
        copy.indexBoard = Arrays.copyOf(indexBoard, 64);
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

    private void getPseudoLegalMoves(int color, PieceList[] pieces, long team, MoveList moves){
        int index;
        for (int i = 0; i < pieces[1].size(); i++) {
            index = pieces[1].get(i);
            long attacks = BitBoard.lookUpRookAttack(index, occupied) & ~team;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 2*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[2].size(); i++) {
            index = pieces[2].get(i);
            long attacks = BitBoard.KNIGHT_ATTACKS[index] & ~team;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 3*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[3].size(); i++){
            index = pieces[3].get(i);
            long attacks = BitBoard.lookUpBishopAttack(index, occupied) & ~team;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 4*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[4].size(); i++){
            index = pieces[4].get(i);
            long attacks = BitBoard.lookUpBishopAttack(index, occupied) & ~team;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 5*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
            attacks = BitBoard.lookUpRookAttack(index, occupied) & ~team;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 5*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[5].size(); i++){
            index = pieces[5].get(i);
            long attacks = BitBoard.KING_ATTACKS[index] & ~team;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 6*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }
    }

    private void getPseudoLegalMovesWhitePawns(MoveList moves) {
        long rightAttacks = BitBoard.shiftSouthWest(team_total[1]) & this.white_values[0] & ~BitBoard.rank_8;
        while (rightAttacks != 0) {
            int from = BitBoard.bitscanForward(rightAttacks);
            moves.add(from, from + 9, 1, indexBoard[from + 9]);
            rightAttacks = BitBoard.lsbReset(rightAttacks);
        }
        long leftAttacks = BitBoard.shiftSouthEast(team_total[1]) & this.white_values[0] & ~BitBoard.rank_8;
        while (leftAttacks != 0) {
            int from = BitBoard.bitscanForward(leftAttacks);
            moves.add(from, from + 7, 1, indexBoard[from + 7]);
            leftAttacks = BitBoard.lsbReset(leftAttacks);
        }
        long advance1 = BitBoard.shiftNorth(white_values[0]) & ~occupied & ~BitBoard.rank_8;
        long attacks = advance1;
        while (attacks != 0) {
            int to = BitBoard.bitscanForward(attacks);
            moves.add(to-8, to, 1, indexBoard[to]);
            attacks = BitBoard.lsbReset(attacks);
        }

        long advance2 = BitBoard.shiftNorth(advance1) & ~occupied & BitBoard.rank_4;
        while (advance2 != 0) {
            int to = BitBoard.bitscanForward(advance2);
            moves.add(to-16, to, 1, indexBoard[to]);
            advance2 = BitBoard.lsbReset(advance2);
        }
    }

    private void getPseudoLegalMovesBlackPawns(MoveList moves) {
        long rightAttacks = BitBoard.shiftNorthWest(team_total[0]) & this.black_values[0] & ~BitBoard.rank_1;
        while (rightAttacks != 0) {
            int from = BitBoard.bitscanForward(rightAttacks);
            moves.add(from, from - 7, -1, indexBoard[from - 7]);
            rightAttacks = BitBoard.lsbReset(rightAttacks);
        }
        long leftAttacks = BitBoard.shiftNorthEast(team_total[0]) & this.black_values[0] & ~BitBoard.rank_1;
        while (leftAttacks != 0) {
            int from = BitBoard.bitscanForward(leftAttacks);
            moves.add(from, from - 9, -1, indexBoard[from - 9]);
            leftAttacks = BitBoard.lsbReset(leftAttacks);
        }
        long advance1 = BitBoard.shiftSouth(black_values[0]) & ~occupied & ~BitBoard.rank_1;
        long attacks = advance1;


        while (attacks != 0) {
            int to = BitBoard.bitscanForward(attacks);
            moves.add(to+8, to, -1, indexBoard[to]);
            attacks = BitBoard.lsbReset(attacks);
        }

        long advance2 = BitBoard.shiftSouth(advance1) & ~occupied & BitBoard.rank_5;
        while (advance2 != 0) {
            int to = BitBoard.bitscanForward(advance2);
            moves.add(to+16, to, -1, indexBoard[to]);
            advance2 = BitBoard.lsbReset(advance2);
        }
    }

    public long getAttackedSquaresFromWhite(){

        long attacks = 0L;

        attacks |= BitBoard.shiftNorthEast(white_values[0]) | BitBoard.shiftNorthWest(white_values[0]);
        for (int i = 0; i < white_pieces[1].size(); i++) {
            attacks |= BitBoard.lookUpRookAttack(white_pieces[1].get(i), occupied);
        }for (int i = 0; i < white_pieces[2].size(); i++) {
            attacks |= BitBoard.KNIGHT_ATTACKS[white_pieces[2].get(i)];
        }for (int i = 0; i < white_pieces[3].size(); i++){
            attacks |= BitBoard.lookUpBishopAttack(white_pieces[3].get(i), occupied);
        }for (int i = 0; i < white_pieces[4].size(); i++){
            attacks |= BitBoard.lookUpBishopAttack(white_pieces[4].get(i), occupied);
            attacks |=  BitBoard.lookUpRookAttack(white_pieces[4].get(i), occupied);
        }for (int i = 0; i < white_pieces[5].size(); i++){
            attacks |= BitBoard.KING_ATTACKS[white_pieces[5].get(i)];
        }
        return attacks & ~team_total[0];
    }

    public long getAttackedSquaresFromBlack(){

        long attacks = 0L;

        attacks |= BitBoard.shiftSouthWest(black_values[0]) | BitBoard.shiftSouthEast(black_values[0]);
        for (int i = 0; i < black_pieces[1].size(); i++) {
            attacks |= BitBoard.lookUpRookAttack(black_pieces[1].get(i), occupied);
        }for (int i = 0; i < black_pieces[2].size(); i++) {
            attacks |= BitBoard.KNIGHT_ATTACKS[black_pieces[2].get(i)];
        }for (int i = 0; i < black_pieces[3].size(); i++){
            attacks |= BitBoard.lookUpBishopAttack(black_pieces[3].get(i), occupied);
        }for (int i = 0; i < black_pieces[4].size(); i++){
            attacks |= BitBoard.lookUpBishopAttack(black_pieces[4].get(i), occupied);
            attacks |=  BitBoard.lookUpRookAttack(black_pieces[4].get(i), occupied);
        }for (int i = 0; i < black_pieces[5].size(); i++){
            attacks |= BitBoard.KING_ATTACKS[black_pieces[5].get(i)];
        }
        return attacks & ~team_total[1];
    }

    public List<Move> getPseudoLegalMoves() {
        return getPseudoLegalMoves(new MoveList(50));
    }

    @Override
    public MoveList getPseudoLegalMoves(MoveList list) {
        list.clear();
        if(getActivePlayer() == 1) {
            if((getAttackedSquaresFromWhite() & black_values[5]) != 0){
                return null;
            }
            getPseudoLegalMoves(1 ,white_pieces, team_total[0], list);
            getPseudoLegalMovesWhitePawns(list);
        }
        else if(getActivePlayer() ==-1) {
            if((getAttackedSquaresFromBlack() & white_values[5]) != 0){
                return null;
            }
            getPseudoLegalMoves(-1,black_pieces, team_total[1], list);
            getPseudoLegalMovesBlackPawns(list);
        }
        return list;
    }

    @Override
    public List<Move> getLegalMoves(MoveList list) {
        return getPseudoLegalMoves(list);
    }

    @Override
    public List<Move> getLegalMoves() {
        return getPseudoLegalMoves();
    }

    @Override
    public List<Move> getCaptureMoves() {
        return getPseudoLegalMoves();
    }

    @Override
    public List<Move> getCaptureMoves(MoveList list) {
        return getPseudoLegalMoves(list);
    }

    @Override
    public boolean isAtCheck(int player) {
        if(player == 1){
            return (getAttackedSquaresFromBlack() & white_values[5]) != 0;
        }else{
            return (getAttackedSquaresFromWhite() & black_values[5]) != 0;
        }
    }

    @Override
    public FastBoard newInstance() {
        return new FastBoard();
    }

    public long[] getWhite_values() {
        return white_values;
    }

    public long[] getBlack_values() {
        return black_values;
    }

    public long[] getTeam_total() {
        return team_total;
    }

    public long getOccupied() {
        return occupied;
    }

    public int[] getIndexBoard() {
        return indexBoard;
    }

    public PieceList[] getWhite_pieces() {
        return white_pieces;
    }

    public PieceList[] getBlack_pieces() {
        return black_pieces;
    }

    public static void main(String[] args) {
        FastBoard board = new FastBoard(Setup.DEFAULT);
        Evaluator ev1 = new NoahEvaluator();
        Evaluator ev2 = new NoahEvaluator2();

        Frame f = new Frame(board, new Player() {}, new Player() {});
        Game g = f.getGamePanel().getGame();
        g.addBoardChangedListener(() -> {
            System.out.println("ev1: " + ev1.evaluate(g.getBoard()));
            System.out.println("ev2: " + ev2.evaluate(g.getBoard()));
            System.out.println();
        });
    }
}
