package board;

import ai.evaluator.NoahEvaluator2;
import ai.ordering.SystematicOrderer2;
import ai.reducing.SenpaiReducer;
import ai.search.PVSearchFast;
import board.bitboards.BitBoard;
import board.moves.Move;
import board.moves.MoveList;
import board.moves.MoveListBuffer;
import board.pieces.PieceList;
import board.repetitions.RepetitionList;
import board.setup.Setup;
import game.Game;
import game.Player;
import io.IO;
import io.Testing;
import visual.Frame;

import java.util.*;
import java.util.function.Consumer;

public class FastBoard extends Board<FastBoard> {

    private class BoardStatus{

        public static final short MASK_NONE                         = (short)0;
        public static final short MASK_WHITE_QUEENSIDE_CASTLING     = (short)1 << 0;
        public static final short MASK_WHITE_KINGSIDE_CASTLING      = (short)1 << 1;
        public static final short MASK_BLACK_QUEENSIDE_CASTLING     = (short)1 << 2;
        public static final short MASK_BLACK_KINGSIDE_CASTLING      = (short)1 << 3;

        public static final short MASK_DRAW_BY_THREE_FOLD           = (short)1 << 12;
        public static final short MASK_WINNER_WHITE                 = (short)1 << 13;
        public static final short MASK_WINNER_BLACK                 = (short)1 << 14;


        private long            enPassantTarget;
        private long            metaInformation;
        private int             fiftyMoveCounter;

        public BoardStatus(long enPassantTarget, long metaInformation, int fiftyMoveCounter) {
            this.enPassantTarget = enPassantTarget;
            this.metaInformation = metaInformation;
            this.fiftyMoveCounter = fiftyMoveCounter;
        }

        public BoardStatus() {
        }

        public long getEnPassantTarget() {
            return enPassantTarget;
        }

        public void setEnPassantTarget(long enPassantTarget) {
            this.enPassantTarget = enPassantTarget;
        }

        public long getMetaInformation() {
            return metaInformation;
        }

        public void setMetaInformation(long metaInformation) {
            this.metaInformation = metaInformation;
        }


        public int getFiftyMoveCounter() {
            return fiftyMoveCounter;
        }

        public void setFiftyMoveCounter(int fiftyMoveCounter) {
            this.fiftyMoveCounter = fiftyMoveCounter;
        }

        public BoardStatus copy() {
            return new BoardStatus(enPassantTarget, metaInformation, fiftyMoveCounter);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoardStatus that = (BoardStatus) o;
            return enPassantTarget == that.enPassantTarget &&
                   metaInformation == that.metaInformation &&
                   fiftyMoveCounter == that.fiftyMoveCounter;
        }

        @Override
        public int hashCode() {
            return Objects.hash(enPassantTarget, metaInformation, fiftyMoveCounter);
        }
    }


    private long[]          white_values;        //bitmap for each white piece
    private long[]          black_values;        //bitmap for each black piece
    private long[]          team_total;          //bitmap for white and black occupancy respectively
    private long            occupied;            //bitmap for occupied squares


    private int[]           indexBoard;          //contains indices for each square. positive indices for white
    private PieceList[]     white_pieces;
    private PieceList[]     black_pieces;

    private RepetitionList  repetitionList;
    private long            zobrist;


    private Stack<BoardStatus> boardStatus;


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
        boardStatus = new Stack<>();
        boardStatus.add(new BoardStatus());
        white_pieces = new PieceList[6];
        black_pieces = new PieceList[6];
        for (int i = 0; i < 6; i++) {
            white_pieces[i] = new PieceList(i+1);
            black_pieces[i] = new PieceList(-(i+1));
        }

        this.repetitionList = new RepetitionList();
        update_longs();
    }

    public BoardStatus getBoardStatus(){
        return boardStatus.lastElement();
    }

    @Override
    public boolean getCastlingChance(int index) {
        return (getBoardStatus().metaInformation & (1L << index)) != 0;
    }

    @Override
    public boolean getEnPassantChance(int file) {
        return (BitBoard.files[file] & getBoardStatus().enPassantTarget) != 0;
    }

    @Override
    public void setEnPassantChance(int square, boolean value) {
        if(value){
            getBoardStatus().enPassantTarget = 1L << square;
        }else{
            getBoardStatus().enPassantTarget = 0;
        }
    }

    @Override
    public void setCastlingChance(int index, boolean value) {
        if(value){
            getBoardStatus().metaInformation = (short)BitBoard.setBit(getBoardStatus().metaInformation, index);
        }else{
            getBoardStatus().metaInformation = (short)BitBoard.unsetBit(getBoardStatus().metaInformation, index);
        }
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
            zobrist ^= BitBoard.black_hashes[-p - 1][index];
        }
        indexBoard[index] = piece;

        if (piece > 0) {

            white_pieces[piece - 1].add(index);
            white_values[piece - 1] = BitBoard.setBit(white_values[piece - 1], index);
            zobrist ^= BitBoard.white_hashes[piece-1][index];
        } else if(piece < 0){
            black_pieces[-piece - 1].add(index);
            black_values[-piece - 1] = BitBoard.setBit(black_values[-piece - 1], index);
            zobrist ^= BitBoard.black_hashes[-piece - 1][index];
        }
        update_longs();
    }

    @Override
    public void setPiece(int x, int y, int piece) {
        setPiece(piece, index(x,y));
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
    public boolean isDraw() {
        return (getBoardStatus().metaInformation & FastBoard.BoardStatus.MASK_DRAW_BY_THREE_FOLD) != 0 ||
               getBoardStatus().getFiftyMoveCounter() >= 50;
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

        builder.append(String.format("%-40s : %-5d %n", "zobrist key", zobrist));
        builder.append(String.format("%-40s : %-5d %n", "repetition", repetitionList.get(zobrist)));
        builder.append(String.format("%-40s : %-5d %n", "50 move rule", getBoardStatus().getFiftyMoveCounter()));
        builder.append(String.format("%-40s : %-5s %n", "white kingside castle", getCastlingChance(1) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "white queenside castle", getCastlingChance(0) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "black kingside castle", getCastlingChance(3) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "black queenside castle", getCastlingChance(2) ? "true":"false"));
        builder.append(String.format("%-40s : %-5s %n", "en passent square", getBoardStatus().getEnPassantTarget() != 0 ?
                IO.getSquareString(BitBoard.bitscanForward(getBoardStatus().getEnPassantTarget())): "-"));

        return builder.toString() + super.toString();
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
    public int winner() {
        return 0;
    }

    @Override
    public void move_null() {

        BoardStatus previousStatus = getBoardStatus();
        BoardStatus newBoardStatus = new BoardStatus(0L,
                                                     previousStatus.getMetaInformation(),
                                                     previousStatus.getFiftyMoveCounter()+1);
        boardStatus.add(newBoardStatus);
        this.changeActivePlayer();
    }

    @Override
    public void undoMove_null() {
        boardStatus.pop();
        this.changeActivePlayer();
    }

    @Override
    public void move(Move m) {
        BoardStatus previousStatus = getBoardStatus();
        BoardStatus newBoardStatus = new BoardStatus(0L,
                                                     previousStatus.getMetaInformation(),
                                                     previousStatus.getFiftyMoveCounter()+1);
        boardStatus.add(newBoardStatus);

        if(m.isPromotion()){
            this.setPiece(0,m.getFrom());
            this.setPiece(m.getPieceFrom(), m.getTo());
            this.changeActivePlayer();
            this.moveHistory.push(m);
            return;
        }

        if(getActivePlayer() == 1){
            //checking if en passent is possible next
            if(m.getPieceFrom() == 1 && m.getTo() - m.getFrom() == 16){
                newBoardStatus.enPassantTarget = (1L << (m.getFrom() + 8));
            }

            //making sure that castling is not allowed after rook moved
            else if(m.getPieceFrom() == 2){
                if(m.getFrom() == 0 && (previousStatus.metaInformation & FastBoard.BoardStatus.MASK_WHITE_QUEENSIDE_CASTLING) != 0){
                    newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 0);
                }else if(m.getFrom() == 7 && (previousStatus.metaInformation & FastBoard.BoardStatus.MASK_WHITE_KINGSIDE_CASTLING) != 0){
                    newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 1);
                }
            }

            //making sure to remove the pawn after en passant
            else if(m.isEn_passent_capture()){
                this.setPiece(0,m.getTo() - 8);
            }
            //castling
            else if (m.isCastle_move()) {
                if (m.getTo() - m.getFrom() == 2) {
                    this.moveSimpleMove(new Move(7, 5, 2, 0));
                } else {
                    this.moveSimpleMove(new Move(0, 3, 2, 0));
                }

                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 0);
                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 1);
            }
            //king move will disable castling
            else if (
                    m.getPieceFrom() == 6 && (previousStatus.metaInformation & (FastBoard.BoardStatus.MASK_WHITE_QUEENSIDE_CASTLING | FastBoard.BoardStatus.MASK_WHITE_KINGSIDE_CASTLING)) != 0){

                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 0);
                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 1);
            }
        }
        else {
            //checking if en passent is possible next
            if (m.getPieceFrom() == -1 && m.getTo() - m.getFrom() == -16) {
                newBoardStatus.enPassantTarget = (1L << (m.getFrom() - 8));
            }


            //making sure that castling is not allowed after rook moved
            else if(m.getPieceFrom() == -2){
                if(m.getFrom() == 7*8 && (previousStatus.metaInformation & FastBoard.BoardStatus.MASK_BLACK_QUEENSIDE_CASTLING) != 0){
                    newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 2);
                }else if(m.getFrom() == 7*8+7 && (previousStatus.metaInformation & FastBoard.BoardStatus.MASK_BLACK_KINGSIDE_CASTLING) != 0){
                    newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 3);
                }
            }


            //making sure to remove the pawn after en passant
            else if(m.isEn_passent_capture()){
                this.setPiece(0,m.getTo()+8);
            }

            //castling
            else if(m.isCastle_move()){
                if(m.getTo() - m.getFrom() == 2){
                    this.moveSimpleMove(new Move(63,61,-2,0));
                }else{
                    this.moveSimpleMove(new Move(56,59,-2,0));
                }
                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 2);
                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 3);
            }
            //king move will disable castling
            else if (
                    m.getPieceFrom() == -6 &&
                    (previousStatus.metaInformation & (FastBoard.BoardStatus.MASK_BLACK_QUEENSIDE_CASTLING | FastBoard.BoardStatus.MASK_BLACK_KINGSIDE_CASTLING)) != 0) {

                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 2);
                newBoardStatus.metaInformation = BitBoard.unsetBit(newBoardStatus.metaInformation, 3);
            }
        }

        this.moveSimpleMove(m);
        this.changeActivePlayer();

        if (m.getPieceTo() != 0){
            newBoardStatus.fiftyMoveCounter = 1;
        }

        if (this.repetitionList.add(zobrist)) {
            newBoardStatus.metaInformation |= FastBoard.BoardStatus.MASK_DRAW_BY_THREE_FOLD;
        }
    }

    @Override
    public void undoMove() {
        if (this.moveHistory.size() == 0) return;

        this.boardStatus.pop();
        this.repetitionList.sub(zobrist);

        Move last = this.moveHistory.peek();

        if(last.isPromotion()){
            this.setPiece(-getActivePlayer(), last.getFrom());
            this.setPiece(last.getPieceTo(),last.getTo());
            this.changeActivePlayer();
            moveHistory.pop();
            return;
        }

        this.undoMoveSimpleMove();

        if(last.isCastle_move()){
            this.undoMoveSimpleMove();      //need to undo the rook move aswell
        }

        if(last.isEn_passent_capture()){
            if(last.getTo() < 30){
                this.setPiece(1,last.getTo()+8);
            }else{
                this.setPiece(-1,last.getTo()-8);
            }
        }

        this.changeActivePlayer();
        this.update_longs();
    }

    private void moveSimpleMove(Move m) {
        this.setPiece(0, m.getFrom());
        this.setPiece(m.getPieceFrom(), m.getTo());
        this.moveHistory.push(m);
    }

    private void undoMoveSimpleMove() {
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
        copy.boardStatus.add(getBoardStatus());
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

        long enPassantTarget = getBoardStatus().getEnPassantTarget();
        long rightAttacks = BitBoard.shiftSouthWest(team_total[1] | enPassantTarget & BitBoard.rank_6) & (this.white_values[0]) & ~BitBoard.rank_7;
        while (rightAttacks != 0) {
            int from = BitBoard.bitscanForward(rightAttacks);
            moves.add(from, from + 9, 1, indexBoard[from + 9])
                    .setType(((1L << (from + 9)) & enPassantTarget) != 0 ? Move.EN_PASSENT:Move.DEFAULT);
            rightAttacks = BitBoard.lsbReset(rightAttacks);
        }
        long leftAttacks = BitBoard.shiftSouthEast(team_total[1] | enPassantTarget & BitBoard.rank_6) & (this.white_values[0]) & ~BitBoard.rank_7;
        while (leftAttacks != 0) {
            int from = BitBoard.bitscanForward(leftAttacks);
            moves.add(from, from + 7, 1, indexBoard[from + 7])
                    .setType(((1L << (from + 7)) & enPassantTarget) != 0 ? Move.EN_PASSENT:Move.DEFAULT);
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


        long promotes = white_values[0] & BitBoard.rank_7 & ~BitBoard.shiftSouth(occupied);
        while(promotes != 0){
            int to = BitBoard.bitscanForward(promotes);
            moves.add(to, to+8, 5, 0).setType(Move.PROMOTION);
            moves.add(to, to+8, 4, 0).setType(Move.PROMOTION);
            moves.add(to, to+8, 3, 0).setType(Move.PROMOTION);
            moves.add(to, to+8, 2, 0).setType(Move.PROMOTION);
            promotes = BitBoard.lsbReset(promotes);
        }
        long capturePromotesLeft = white_values[0] & BitBoard.rank_7 & (BitBoard.shiftSouthEast(team_total[1]));
        while(capturePromotesLeft != 0){
            int to = BitBoard.bitscanForward(capturePromotesLeft);
            moves.add(to, to+7, 5, getPiece(to+7)).setType(Move.PROMOTION);
            moves.add(to, to+7, 4, getPiece(to+7)).setType(Move.PROMOTION);
            moves.add(to, to+7, 3, getPiece(to+7)).setType(Move.PROMOTION);
            moves.add(to, to+7, 2, getPiece(to+7)).setType(Move.PROMOTION);
            capturePromotesLeft = BitBoard.lsbReset(capturePromotesLeft);
        }
        long capturePromotesRight = white_values[0] & BitBoard.rank_7 & (BitBoard.shiftSouthWest(team_total[1]));
        while(capturePromotesRight != 0){
            int to = BitBoard.bitscanForward(capturePromotesRight);
            moves.add(to, to+9, 5, getPiece(to+9)).setType(Move.PROMOTION);
            moves.add(to, to+9, 4, getPiece(to+9)).setType(Move.PROMOTION);
            moves.add(to, to+9, 3, getPiece(to+9)).setType(Move.PROMOTION);
            moves.add(to, to+9, 2, getPiece(to+9)).setType(Move.PROMOTION);
            capturePromotesRight = BitBoard.lsbReset(capturePromotesRight);
        }

    }

    private void getPseudoLegalMovesBlackPawns(MoveList moves) {
        long enPassantTarget = getBoardStatus().getEnPassantTarget();
        long rightAttacks = BitBoard.shiftNorthWest(team_total[0] | enPassantTarget & BitBoard.rank_3) &
                (this.black_values[0]) & ~BitBoard.rank_2;
        while (rightAttacks != 0) {
            int from = BitBoard.bitscanForward(rightAttacks);
            moves.add(from, from - 7, -1, indexBoard[from - 7])
                    .setType(((1L << (from - 7)) & enPassantTarget) != 0 ? Move.EN_PASSENT: Move.DEFAULT);
            rightAttacks = BitBoard.lsbReset(rightAttacks);
        }
        long leftAttacks = BitBoard.shiftNorthEast(team_total[0] | enPassantTarget & BitBoard.rank_3) &
                (this.black_values[0]) & ~BitBoard.rank_2;
        while (leftAttacks != 0) {
            int from = BitBoard.bitscanForward(leftAttacks);
            moves.add(from, from - 9, -1, indexBoard[from - 9])
                    .setType(((1L << (from - 9)) & enPassantTarget) != 0 ? Move.EN_PASSENT: Move.DEFAULT);
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

        long promotes = black_values[0] & BitBoard.rank_2 & ~BitBoard.shiftNorth(occupied);
        while(promotes != 0){
            int to = BitBoard.bitscanForward(promotes);
            moves.add(to, to-8, -5, 0).setType(Move.PROMOTION);
            moves.add(to, to-8, -4, 0).setType(Move.PROMOTION);
            moves.add(to, to-8, -3, 0).setType(Move.PROMOTION);
            moves.add(to, to-8, -2, 0).setType(Move.PROMOTION);
            promotes = BitBoard.lsbReset(promotes);
        }

        long capturePromotesLeft = black_values[0] & BitBoard.rank_2 & (BitBoard.shiftNorthWest(team_total[0]));
        while(capturePromotesLeft != 0){
            int to = BitBoard.bitscanForward(capturePromotesLeft);
            moves.add(to, to-7, -5, getPiece(to-7)).setType(Move.PROMOTION);
            moves.add(to, to-7, -4, getPiece(to-7)).setType(Move.PROMOTION);
            moves.add(to, to-7, -3, getPiece(to-7)).setType(Move.PROMOTION);
            moves.add(to, to-7, -2, getPiece(to-7)).setType(Move.PROMOTION);
            capturePromotesLeft = BitBoard.lsbReset(capturePromotesLeft);
        }
        long capturePromotesRight = black_values[0] & BitBoard.rank_2 & (BitBoard.shiftNorthEast(team_total[0]));
        while(capturePromotesRight != 0){
            int to = BitBoard.bitscanForward(capturePromotesRight);
            moves.add(to, to-9, -5, getPiece(to-9)).setType(Move.PROMOTION);
            moves.add(to, to-9, -4, getPiece(to-9)).setType(Move.PROMOTION);
            moves.add(to, to-9, -3, getPiece(to-9)).setType(Move.PROMOTION);
            moves.add(to, to-9, -2, getPiece(to-9)).setType(Move.PROMOTION);
            capturePromotesRight = BitBoard.lsbReset(capturePromotesRight);
        }
    }

    private void getPseudoLegalMovesWhiteCastling(MoveList moves) {
        if(!BitBoard.getBit(white_values[5],4)) return;
        long metaInformation = getBoardStatus().getMetaInformation();

        if(BitBoard.getBit(metaInformation, 0) &&
                BitBoard.getBit(white_values[1], 0) &&
                (BitBoard.castling_white_queenside_mask & occupied) == 0){
            moves.add(4, 2, 6, 0).setType(Move.CASTLING);
        }

        if(BitBoard.getBit(metaInformation, 1) &&
                BitBoard.getBit(white_values[1], 7) &&
                (BitBoard.castling_white_kingside_mask & occupied) == 0){
            moves.add(4, 6, 6, 0).setType(Move.CASTLING);
        }
    }

    private void getPseudoLegalMovesBlackCastling(MoveList moves) {
        if(!BitBoard.getBit(black_values[5],4+56)) return;
        long metaInformation = getBoardStatus().getMetaInformation();

        if(BitBoard.getBit(metaInformation, 2) &&
                BitBoard.getBit(black_values[1], 0+56)&&
                (BitBoard.castling_black_queenside_mask & occupied) == 0){
            moves.add(4+56, 2+56, -6, 0).setType(Move.CASTLING);
        }

        if(BitBoard.getBit(metaInformation, 3) &&
                BitBoard.getBit(black_values[1], 7+56)&&
                (BitBoard.castling_black_kingside_mask & occupied) == 0){
            moves.add(4+56, 6+56, -6, 0).setType(Move.CASTLING);
        }
    }

    private void getPseudoLegalCaptures(int color, PieceList[] pieces, long opponent, MoveList moves){
        int index;
        for (int i = 0; i < pieces[1].size(); i++) {
            index = pieces[1].get(i);
            long attacks = BitBoard.lookUpRookAttack(index, occupied) & opponent;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 2*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[2].size(); i++) {
            index = pieces[2].get(i);
            long attacks = BitBoard.KNIGHT_ATTACKS[index] & opponent;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 3*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[3].size(); i++){
            index = pieces[3].get(i);
            long attacks = BitBoard.lookUpBishopAttack(index, occupied) & opponent;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 4*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[4].size(); i++){
            index = pieces[4].get(i);
            long attacks = BitBoard.lookUpBishopAttack(index, occupied) & opponent;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 5*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
            attacks = BitBoard.lookUpRookAttack(index, occupied) & opponent;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 5*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }

        for (int i = 0; i < pieces[5].size(); i++){
            index = pieces[5].get(i);
            long attacks = BitBoard.KING_ATTACKS[index] & opponent;
            while(attacks != 0){
                int to = BitBoard.bitscanForward(attacks);
                moves.add(index, to, 6*color,indexBoard[to]);
                attacks = BitBoard.lsbReset(attacks);
            }
        }
    }

    private void getPseudoLegalCaptureMovesWhitePawns(MoveList moves) {
        long enPassantTarget = getBoardStatus().getEnPassantTarget();
        long rightAttacks = BitBoard.shiftSouthWest(team_total[1] | enPassantTarget & BitBoard.rank_6) &
                (this.white_values[0]) & ~BitBoard.rank_8;
        while (rightAttacks != 0) {
            int from = BitBoard.bitscanForward(rightAttacks);
            moves.add(from, from + 9, 1, indexBoard[from + 9])
                    .setType(((1L << (from + 9)) & enPassantTarget) != 0 ? Move.EN_PASSENT:Move.DEFAULT);
            rightAttacks = BitBoard.lsbReset(rightAttacks);
        }
        long leftAttacks = BitBoard.shiftSouthEast(team_total[1] | enPassantTarget & BitBoard.rank_6) &
                (this.white_values[0]) & ~BitBoard.rank_8;
        while (leftAttacks != 0) {
            int from = BitBoard.bitscanForward(leftAttacks);
            moves.add(from, from + 7, 1, indexBoard[from + 7])
                    .setType(((1L << (from + 7)) & enPassantTarget) != 0 ? Move.EN_PASSENT:Move.DEFAULT);
            leftAttacks = BitBoard.lsbReset(leftAttacks);
        }

    }

    private void getPseudoLegalCaptureMovesBlackPawns(MoveList moves) {
        long enPassantTarget = getBoardStatus().getEnPassantTarget();
        long rightAttacks = BitBoard.shiftNorthWest(team_total[0] | enPassantTarget & BitBoard.rank_3) &
                (this.black_values[0]) & ~BitBoard.rank_1;
        while (rightAttacks != 0) {
            int from = BitBoard.bitscanForward(rightAttacks);
            moves.add(from, from - 7, -1, indexBoard[from - 7])
                    .setType(((1L << (from - 7)) & enPassantTarget) != 0 ? Move.EN_PASSENT:Move.DEFAULT);
            rightAttacks = BitBoard.lsbReset(rightAttacks);
        }
        long leftAttacks = BitBoard.shiftNorthEast(team_total[0] | enPassantTarget & BitBoard.rank_3) &
                (this.black_values[0]) & ~BitBoard.rank_1;
        while (leftAttacks != 0) {
            int from = BitBoard.bitscanForward(leftAttacks);
            moves.add(from, from - 9, -1, indexBoard[from - 9])
                    .setType(((1L << (from - 9)) & enPassantTarget) != 0 ? Move.EN_PASSENT:Move.DEFAULT);
            leftAttacks = BitBoard.lsbReset(leftAttacks);
        }

    }


    public List<Move> getPseudoLegalMoves() {
        return getPseudoLegalMoves(new MoveList(50));
    }

    @Override
    public MoveList getPseudoLegalMoves(MoveList list) {
        list.clear();
        if(getActivePlayer() == 1) {
            getPseudoLegalMoves(1 ,white_pieces, team_total[0], list);
            getPseudoLegalMovesWhitePawns(list);
            getPseudoLegalMovesWhiteCastling(list);
        }
        else if(getActivePlayer() ==-1) {
            getPseudoLegalMoves(-1,black_pieces, team_total[1], list);
            getPseudoLegalMovesBlackPawns(list);
            getPseudoLegalMovesBlackCastling(list);
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
        list.clear();
        if(getActivePlayer() == 1) {
            getPseudoLegalCaptures(1 ,white_pieces, team_total[1], list);
            getPseudoLegalCaptureMovesWhitePawns(list);
        }
        else if(getActivePlayer() ==-1) {
            getPseudoLegalCaptures(-1,black_pieces, team_total[0], list);
            getPseudoLegalCaptureMovesBlackPawns(list);
        }
        return list;
    }

    @Override
    public boolean isLegal(Move m) {

        int  thisKing;
        long opponentQueenBitboard;
        long opponentRookBitboard;
        long opponentBishopBitboard;


        if(this.getActivePlayer() == 1){
            thisKing = white_pieces[5].get(0);
            opponentQueenBitboard =     black_values[4];
            opponentRookBitboard =      black_values[1];
            opponentBishopBitboard =    black_values[3];
        }else{
            thisKing = black_pieces[5].get(0);
            opponentQueenBitboard =     white_values[4];
            opponentRookBitboard =      white_values[1];
            opponentBishopBitboard =    white_values[3];
        }

        if (m.isEn_passent_capture()) {
            this.move(m);
            boolean isOk =
                    (BitBoard.lookUpRookAttack(thisKing, occupied) & (opponentQueenBitboard | opponentRookBitboard)) == 0 &&
                    (BitBoard.lookUpBishopAttack(thisKing, occupied) & (opponentQueenBitboard | opponentBishopBitboard)) == 0;
            this.undoMove();
            return isOk;
        }

        if (m.isCastle_move()){
            long secure = 0L;
            if(this.getActivePlayer() == 1){
                secure = m.getTo() - m.getFrom() > 0 ? BitBoard.castling_white_kingside_safe:BitBoard.castling_white_queenside_safe;
                return (getAttackedSquaresFromBlack() & secure) == 0;
            }else {
                secure = m.getTo() - m.getFrom() > 0 ? BitBoard.castling_black_kingside_safe : BitBoard.castling_black_queenside_safe;
                return (getAttackedSquaresFromWhite() & secure) == 0;
            }
        }

//        if(m.isPromotion()){
//            this.occupied = BitBoard.unsetBit(this.occupied, m.getFrom());             //removing the moving piece
//            this.occupied = BitBoard.setBit(this.occupied, m.getTo());                  //setting a bit where moved
//
//            boolean underAttack = isUnderAttack(thisKing, -this.getActivePlayer());
//
//            this.occupied = BitBoard.unsetBit(this.occupied, m.getTo());             //removing the moving piece
//            this.occupied = BitBoard.setBit(this.occupied, m.getFrom());             //adding the moved piece
//            return !underAttack;
//        }

        boolean isCap = m.getPieceTo() != 0;

        this.occupied = BitBoard.unsetBit(this.occupied, m.getFrom());             //removing the moving piece
        this.occupied = BitBoard.setBit(this.occupied, m.getTo());                  //setting a bit where moved

        boolean isAttacked;

        if(Math.abs(m.getPieceFrom()) == 6){
            thisKing = m.getTo();
        }

        if(isCap){
            if(this.getActivePlayer() == 1){
                this.black_values[-m.getPieceTo()-1] = BitBoard.unsetBit(this.black_values[-m.getPieceTo()-1], m.getTo());
                isAttacked = isUnderAttack(thisKing, -this.getActivePlayer());
                this.black_values[-m.getPieceTo()-1] = BitBoard.setBit(this.black_values[-m.getPieceTo()-1], m.getTo());
            }else{
                this.white_values[m.getPieceTo()-1] = BitBoard.unsetBit(this.white_values[m.getPieceTo()-1], m.getTo());
                isAttacked = isUnderAttack(thisKing, -this.getActivePlayer());
                this.white_values[m.getPieceTo()-1] = BitBoard.setBit(this.white_values[m.getPieceTo()-1], m.getTo());
            }
        }else{
            isAttacked = isUnderAttack(thisKing, -this.getActivePlayer());
        }

        this.occupied = BitBoard.setBit(this.occupied, m.getFrom());                //adding the moved piece
        if(isCap){
            this.occupied = BitBoard.setBit(this.occupied, m.getTo());              //setting the piece back if captured
        }else{
            this.occupied = BitBoard.unsetBit(this.occupied, m.getTo());            //otherwise removing
        }


        return !isAttacked;
    }

    /**
     * this does not check for en passent attacks!
     * @param square
     * @param attacker
     * @return
     */
    public boolean isUnderAttack(int square, int attacker){

        long sq = BitBoard.setBit(0L, square);

        if(attacker == 1){
            return
                    (BitBoard.lookUpRookAttack(square, occupied) & (white_values[4] | white_values[1])) != 0 ||
                    (BitBoard.lookUpBishopAttack(square, occupied) & (white_values[4] | white_values[3])) != 0 ||
                    (BitBoard.KNIGHT_ATTACKS[square] & white_values[2]) != 0 ||
                    ((BitBoard.shiftSouthEast(sq) | BitBoard.shiftSouthWest(sq)) & white_values[0]) != 0;
        }else{
            return
                    (BitBoard.lookUpRookAttack(square, occupied) & (black_values[4] | black_values[1])) != 0 ||
                    (BitBoard.lookUpBishopAttack(square, occupied) & (black_values[4] | black_values[3])) != 0 ||
                    (BitBoard.KNIGHT_ATTACKS[square] & black_values[2]) != 0 ||
                    ((BitBoard.shiftNorthEast(sq) | BitBoard.shiftNorthWest(sq)) & black_values[0]) != 0;
        }
    }

    public boolean isCheck(Move m){
        return false;
    }

    @Override
    public boolean isAtCheck(int player) {
        if(player == 1){
            return (getAttackedSquaresFromBlack() & white_values[5]) != 0;
        }else{
            return (getAttackedSquaresFromWhite() & black_values[5]) != 0;
        }
    }

    @Deprecated
    public boolean previousMoveIsLegal() {
//        if(this.getActivePlayer() == 1){
//            if((getAttackedSquaresFromWhite() & (black_values[5] | getBoardStatus().secureSquares)) != 0){
//                return false;
//            }
//        }else{
//            if((getAttackedSquaresFromBlack() & (white_values[5] | getBoardStatus().secureSquares)) != 0){
//                return false;
//            }
//        }
        return true;
    }

    @Override
    public Move generateMove(int from, int to, int promotionTarget) {
        int pieceFrom = this.getPiece(from);
        int pieceTo = this.getPiece(to);

        Move m = new Move(from, to, pieceFrom, pieceTo);

        if(Math.abs(pieceFrom) == 6 && Math.abs(to-from) == 2){
            m.setType(Move.CASTLING);
        }
        if (Math.abs(pieceFrom) == 1){
            if (Math.abs(to - from) % 8 != 0 && pieceTo == 0) {
                m.setType(Move.EN_PASSENT);
            }
            if (pieceFrom > 0){
                if(((1L << to) & BitBoard.rank_8) != 0){
                    assert promotionTarget > 0;
                    m.setType(Move.PROMOTION);
                    m.setPieceTo(getActivePlayer() * promotionTarget);
                }
            }else{
                if(((1L << to) & BitBoard.rank_1) != 0){
                    assert promotionTarget > 0;
                    m.setType(Move.PROMOTION);
                    m.setPieceTo(getActivePlayer() * promotionTarget);
                }
            }
        }
        return m;
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

    public RepetitionList getRepetitionList() {
        return repetitionList;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(occupied, repetitionList, zobrist,getBoardStatus());
        result = 31 * result + Arrays.hashCode(white_values);
        result = 31 * result + Arrays.hashCode(black_values);
        result = 31 * result + Arrays.hashCode(team_total);
        result = 31 * result + Arrays.hashCode(indexBoard);
        result = 31 * result + Arrays.hashCode(white_pieces);
        result = 31 * result + Arrays.hashCode(black_pieces);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FastBoard board = (FastBoard) o;
        return occupied == board.occupied &&
                zobrist == board.zobrist &&
                getBoardStatus().equals(board.getBoardStatus()) &&
                Arrays.equals(white_values, board.white_values) &&
                Arrays.equals(black_values, board.black_values) &&
                Arrays.equals(team_total, board.team_total) &&
                Arrays.equals(indexBoard, board.indexBoard) &&
                Arrays.equals(white_pieces, board.white_pieces) &&
                Arrays.equals(black_pieces, board.black_pieces) &&
                Objects.equals(repetitionList, board.repetitionList);
    }

    public static void main(String[] args) {
        FastBoard board = new FastBoard(Setup.DEFAULT);
        board = IO.read_FEN(board, "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 ");
        System.out.println(Testing.perft_pseudo(board, 4, new MoveListBuffer(100),true));
        FastBoard finalBoard = board;
        Game g = new Frame(board, new Player(){}, new Player() {}).getGamePanel().getGame();
        g.addMoveAboutToHappenListener(move -> System.out.println(finalBoard.isLegal(move)));
        g.addBoardChangedListener(move -> System.out.println(finalBoard));


    }
}
