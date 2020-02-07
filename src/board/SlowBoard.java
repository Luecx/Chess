package board;

import board.bitboards.BitBoard;
import board.moves.Move;
import board.moves.MoveList;
import board.repetitions.RepetitionList;
import board.setup.Setup;
import game.Player;
import ai.evaluator.*;
import ai.ordering.SystematicOrderer2;
import ai.reducing.SenpaiReducer;
import ai.search.PVSearchFast;
import io.IO;
import visual.Frame;

import java.util.*;

public class SlowBoard extends Board<SlowBoard> {


    private static final int[] TURM_DIRECTIONS                  = {12, -12,   1, - 1};
    private static final int[] LAEUFER_DIRECTIONS               = {13,  11, -11, -13};
    private static final int[] SPRINGER_OFFSET                  = {23,  25,  14,  10, -10, -14, -23, -25};
    private static final int[] KOENIG_OFFSET                    = {12,  13,   1, -13, -12, -11, - 1,  11};



    public static final short MASK_NONE                         = (short)0;
    public static final short MASK_WHITE_KINGSIDE_CASTLING      = (short)1 << 0;
    public static final short MASK_WHITE_QUEENSIDE_CASTLING     = (short)1 << 1;
    public static final short MASK_BLACK_QUEENSIDE_CASTLING     = (short)1 << 2;
    public static final short MASK_BLACK_KINGSIDE_CASTLING      = (short)1 << 3;

    public static final short MASK_EN_PASSENT_MOVES             = (short)4080;
    public static final short MASK_GAMEOVER                     = (short)1 << 12;
    public static final short MASK_WINNER_WHITE                 = (short)1 << 13;
    public static final short MASK_WINNER_BLACK                 = (short)1 << 14;



    public static final short INDEX_WHITE_QUEENSIDE_ROOK        = (short)26;
    public static final short INDEX_WHITE_KINGSIDE_ROOK         = (short)26 + 7;
    public static final short INDEX_BLACK_QUEENSIDE_ROOK        = (short)26 + 7 * 12;
    public static final short INDEX_BLACK_KINGSIDE_ROOK         = (short)26 + 7 * 12 + 7;


    public static final int INVALID = Byte.MIN_VALUE;

    /**
     * bits:
     * 1:   white kingside castle
     * 2:   white queenside castle
     * 3:   black kingside castle
     * 4:   black queenside castle
     * 5:   en passent A
     * 6:   en passent B
     * 7:   en passent C
     * ...
     * ...
     * 12:  en passent G
     */
    protected short board_meta_information;
    protected long zobristKey;
    protected int[] field; //2x2 padding each side
    protected RepetitionList board_repetition_counter;



    public SlowBoard(Setup setup) {
        super(setup);
    }

    public SlowBoard() {
    }


    //<editor-fold desc="indexing">
    @Override
    public int index(int x, int y) {
        return (y + 2) * 12 + (x + 2);
    }

    @Override
    public int x(int index) {
        return (index % 12 - 2);
    }

    @Override
    public int y(int index) {
        return (index / 12 - 2);
    }
    //</editor-fold>

    //<editor-fold desc="zobrist">
    public long oldZobrist() {
        long h = 0;
        for(int i = 0; i < 8; i++){
            for(int n = 0; n < 8; n++){
                int piece = getPiece(i,n);
                if(piece == 0) continue;
                if(piece > 0){
                    h = BitBoard.xor(h, BitBoard.white_hashes[piece-1][i * 8 + n]);
                }else{
                    h = BitBoard.xor(h, BitBoard.black_hashes[-piece-1][i * 8 + n]);
                }
            }
        }
        return h;
    }

    @Override
    public long zobrist() {
        return zobristKey;
    }
    //</editor-fold>

    //<editor-fold desc="meta information stuff">
    public short getBoard_meta_information() {
        return board_meta_information;
    }

    @Override
    public int getCurrentRepetitionCount() {
        return this.board_repetition_counter.get(this.zobrist());
    }

    @Override
    public boolean getCastlingChance(int index) {
        return (board_meta_information & (1L << index)) > 0;
    }

    @Override
    public void setCastlingChance(int index, boolean value) {

        if(value){
            board_meta_information |= (1 << index);
        }else{
            board_meta_information &= ~(1 << index);
        }
    }

    @Override
    public boolean getEnPassantChance(int file) {
        return (board_meta_information & (1 << (4+file))) > 0;
    }

    @Override
    public void setEnPassantChance(int file, boolean value) {
        if(value){
            board_meta_information |= 1 << (4+file);
        }else{
            board_meta_information &= ~(1 << (4+file));
        }
    }

    @Override
    public int winner() {
        if((MASK_WINNER_WHITE & board_meta_information) > 0) return 1;
        if((MASK_WINNER_BLACK & board_meta_information) > 0) return -1;
        return 0;
    }

    @Override
    public boolean isGameOver() {
        return (MASK_GAMEOVER & board_meta_information) > 0;
    }
    //</editor-fold>

    //<editor-fold desc="moving">
    @Override
    public void move(Move m) {

        //<editor-fold desc="null move">
        if (m.getIsNull()) { // does null move stuff
            this.changeActivePlayer();
            m.setMetaInformation((short)(m.getMetaInformation() ^
                    (this.board_meta_information & MASK_EN_PASSENT_MOVES)));
            this.board_meta_information ^= m.getMetaInformation();

            this.moveHistory.push(m);
            return;
        }
        //</editor-fold>

        //<editor-fold desc="en passant">

        if(m.getPieceFrom() * getActivePlayer() == 1 &&
                m.getPieceTo() == 0 &&
                Math.abs((m.getTo() - m.getFrom())) % 12 != 0) {
            if (getActivePlayer() == 1) {
                if (m.getTo() - m.getFrom() == 13) {
                    this.setPiece(m.getFrom() + 1, 0);
                } else {
                    this.setPiece(m.getFrom() - 1, 0);
                }
            } else {
                if (m.getTo() - m.getFrom() == -13) {
                    this.setPiece(m.getFrom() - 1, 0);
                } else {
                    this.setPiece(m.getFrom() + 1, 0);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="castling">
        if (m.getPieceFrom() * getActivePlayer() == 6) {
            if (Math.abs(m.getTo() - m.getFrom()) == 2) {
                if (m.getTo() > m.getFrom()) {
                    this.moveSimpleMove(new Move(m.getFrom() + 3, m.getFrom() + 1, this));
                } else {
                    this.moveSimpleMove(new Move(m.getFrom() - 4, m.getFrom() - 1, this));
                }
            }
        }
        //</editor-fold>

        this.moveSimpleMove(m);

        //<editor-fold desc="promotion">
        if(m.getPieceFrom() * getActivePlayer() == 1){
            if((y(m.getTo()) == 7 && getActivePlayer() == 1) ||
                    (y(m.getTo()) == 0 && getActivePlayer() == -1)){
                this.setPiece(m.getTo(), 5 * getActivePlayer());
            }
        }
        //</editor-fold>

        //<editor-fold desc="gameover / en passant masking">
        short mask = (short)(board_meta_information & MASK_EN_PASSENT_MOVES);
        //System.err.println(board_meta_information);
        if(Math.abs(m.getPieceTo()) == 6){
            if(m.getPieceTo() > 0){
                mask |= MASK_WINNER_BLACK | MASK_GAMEOVER;
            }else{
                mask |= MASK_WINNER_WHITE | MASK_GAMEOVER;
            }
        }
        if(this.board_repetition_counter.add(this.zobrist())){
            mask |= MASK_GAMEOVER;
        }

        m.setMetaInformation((short)(m.getMetaInformation() | mask));

        this.board_meta_information ^= m.getMetaInformation();

        //</editor-fold>

        this.changeActivePlayer();
    }

    @Override
    public void undoMove() {
        if (this.moveHistory.size() == 0) return;

        Move last = this.moveHistory.peek();
        this.board_meta_information ^= last.getMetaInformation();

        //<editor-fold desc="threefold repetition">
        this.board_repetition_counter.sub(this.zobrist());
        //</editor-fold>

        //<editor-fold desc="null moves">
        if (this.moveHistory.peek().getIsNull()) { // null move stuff
            this.changeActivePlayer();
            this.moveHistory.pop();
            return;
        }
        //</editor-fold>

        //<editor-fold desc="en passant">
        if(Math.abs(last.getPieceFrom())== 1 &&
                last.getPieceTo() == 0 &&
                Math.abs((last.getTo() - last.getFrom())) % 12 != 0) {


            undoMoveSimpleMove();

            if (getActivePlayer() == 1) {
                if (last.getTo() - last.getFrom() == -13) {
                    this.setPiece(last.getFrom()-1,1);
                } else {
                    this.setPiece(last.getFrom()+1,1);
                }
            } else {
                if (last.getTo() - last.getFrom() == +13) {
                    this.setPiece(last.getFrom()+1,-1);
                } else {
                    this.setPiece(last.getFrom()-1,-1);
                }
            }
            changeActivePlayer();
            return;
        }
        //</editor-fold>

        undoMoveSimpleMove();

        //<editor-fold desc="castling">
        while(this.moveHistory.size() > 0 && this.moveHistory.peek().getPieceFrom() * last.getPieceFrom() > 0){
            undoMoveSimpleMove();
        }
        //</editor-fold>

        this.changeActivePlayer();
    }

    private void moveSimpleMove(Move m) {
        this.setPiece(m.getFrom(), 0);
        this.setPiece(m.getTo(), m.getPieceFrom());
        this.moveHistory.push(m);
    }

    private void undoMoveSimpleMove() {
        if (this.moveHistory.size() == 0) return;
        Move old = this.moveHistory.pop();
        this.setPiece(old.getFrom(), old.getPieceFrom());
        this.setPiece(old.getTo(), old.getPieceTo());
    }
    //</editor-fold>

    //<editor-fold desc="piece attacks">
    private void slidingPieces(int pos, int direction, MoveList list, short mask) {
        int c = pos + direction;
        while (field[c] != INVALID) {
            if (field[c] != 0) {
                if (field[c] * getActivePlayer() < 1) {
                    list.add(pos, c, this,mask);
                }
                return;
            }
            list.add(pos, c, this,mask);
            c += direction;
        }
    }
    private void slidingPieces_captures(int pos, int direction, MoveList list, short mask) {
        int c = pos + direction;
        while (field[c] != INVALID) {
            if (field[c] != 0) {
                if (field[c] * getActivePlayer() < 1) {
                    list.add(pos, c, this,mask);
                }
                return;
            }
            c += direction;
        }
    }

    @Override
    public boolean isAtCheck(int player) {
        return false;
    }

    protected void pseudeLegalMoves_pawn(int index, int i, int j, MoveList moves){
        if (j == 0 || j == 7) return;
        if (field[index + getActivePlayer() * 12] == 0) {
            moves.add(index, index + getActivePlayer() * 12, getActivePlayer(), (byte) 0);
            if ((j == getActivePlayer() || j == 7 + getActivePlayer()) && field[index + getActivePlayer() * 2 * 12] == 0) {
                moves.add(index, index + getActivePlayer() * 24, getActivePlayer(),0,(short) (1 << (i+4)));
            }
        }
        if(getActivePlayer() > 0){
            if (i > 0) {
                if (field[index + 11] < 0){
                    moves.add(index, index + 11, this);
                }

                if ((board_meta_information & ((short)1 << (4+i-1))) > 0 && j == 4){
                    moves.add(index, index + 11, 1,0);
                }
            }
            if (i < 7){
                if(field[index + 13] < 0) {
                    moves.add(index, index + 13, this);
                }

                if ((board_meta_information & ((short)1 << (4+i+1))) > 0 && j == 4){
                    moves.add(index, index + 13, 1,0);
                }
            }
        }else{
            if (i > 0) {
                if (field[index - 13] > 0){
                    moves.add(index, index - 13, this);
                }

                if ((board_meta_information & ((short)1 << (4+i-1))) > 0 && j == 3){
                    moves.add(index, index - 13, this);
                }
            }
            if (i < 7){
                if(field[index - 11] > 0) {
                    moves.add(index, index - 11, this);
                }

                if ((board_meta_information & ((short)1 << (4+i+1))) > 0 && j == 3){
                    moves.add(index, index - 11, this);
                }
            }
        }

    }
    protected void pseudeLegalMoves_pawn_captures(int index, int i, int j, MoveList moves){
        if (j == 0 || j == 7) return;
        if (field[index + getActivePlayer() * 11] != INVALID && field[index + getActivePlayer() * 11] * getActivePlayer() < 0) {
            moves.add(index, index + getActivePlayer() * 11, this);
        }
        if (field[index + getActivePlayer() * 13] != INVALID && field[index + getActivePlayer() * 13] * getActivePlayer() < 0) {
            moves.add(index, index + getActivePlayer() * 13, this);
        }

    }

    protected void pseudeLegalMoves_knight(int index, int i, int j, MoveList moves){
        for (int ar : SPRINGER_OFFSET) {
            if (this.field[ar + index] != INVALID && field[ar + index] * getActivePlayer() <= 0) {
                moves.add(index, ar + index, this);
            }
        }
    }
    protected void pseudeLegalMoves_knight_captures(int index, int i, int j, MoveList moves){
        for (int ar : SPRINGER_OFFSET) {
            if (this.field[ar + index] != INVALID && field[ar + index] * getActivePlayer() < 0) {
                moves.add(index, ar + index, this);
            }
        }
    }

    protected void pseudeLegalMoves_rook(int index, int i, int j, MoveList moves){
        short mask = MASK_NONE;
        switch (index){
            case INDEX_WHITE_QUEENSIDE_ROOK:
                if((board_meta_information & MASK_WHITE_QUEENSIDE_CASTLING) > 0)
                    mask = MASK_WHITE_QUEENSIDE_CASTLING;
                break;
            case INDEX_WHITE_KINGSIDE_ROOK:
                if((board_meta_information & MASK_WHITE_KINGSIDE_CASTLING) > 0)
                    mask = MASK_WHITE_KINGSIDE_CASTLING;
                break;
            case INDEX_BLACK_QUEENSIDE_ROOK:
                if((board_meta_information & MASK_BLACK_QUEENSIDE_CASTLING) > 0)
                    mask = MASK_BLACK_QUEENSIDE_CASTLING;
                break;
            case INDEX_BLACK_KINGSIDE_ROOK:
                if((board_meta_information & MASK_BLACK_KINGSIDE_CASTLING) > 0)
                    mask = MASK_BLACK_KINGSIDE_CASTLING;
                break;
        }
        for (int dir : TURM_DIRECTIONS) {
            slidingPieces(index, dir, moves, mask);
        }
    }
    protected void pseudeLegalMoves_rook_captures(int index, int i, int j, MoveList moves){
        short mask = MASK_NONE;
        switch (index){
            case INDEX_WHITE_QUEENSIDE_ROOK:
                if((board_meta_information & MASK_WHITE_QUEENSIDE_CASTLING) > 0)
                    mask = MASK_WHITE_QUEENSIDE_CASTLING;
                break;
            case INDEX_WHITE_KINGSIDE_ROOK:
                if((board_meta_information & MASK_WHITE_KINGSIDE_CASTLING) > 0)
                    mask = MASK_WHITE_KINGSIDE_CASTLING;
                break;
            case INDEX_BLACK_QUEENSIDE_ROOK:
                if((board_meta_information & MASK_BLACK_QUEENSIDE_CASTLING) > 0)
                    mask = MASK_BLACK_QUEENSIDE_CASTLING;
                break;
            case INDEX_BLACK_KINGSIDE_ROOK:
                if((board_meta_information & MASK_BLACK_KINGSIDE_CASTLING) > 0)
                    mask = MASK_BLACK_KINGSIDE_CASTLING;
                break;
        }
        for (int dir : TURM_DIRECTIONS) {
            slidingPieces_captures(index, dir, moves, mask);
        }
    }

    protected void pseudeLegalMoves_bishop(int index, int i, int j, MoveList moves){
        for (int dir : LAEUFER_DIRECTIONS) {
            slidingPieces(index, dir, moves, MASK_NONE);
        }

    }
    protected void pseudeLegalMoves_bishop_captures(int index, int i, int j, MoveList moves){
        for (int dir : LAEUFER_DIRECTIONS) {
            slidingPieces_captures(index, dir, moves, MASK_NONE);
        }
    }

    protected void pseudeLegalMoves_queen(int index, int i, int j, MoveList moves){
        for (int dir : TURM_DIRECTIONS) {
            slidingPieces(index, dir, moves, MASK_NONE);
        }
        for (int dir : LAEUFER_DIRECTIONS) {
            slidingPieces(index, dir, moves, MASK_NONE);
        }
    }
    protected void pseudeLegalMoves_queen_captures(int index, int i, int j, MoveList moves){
        for (int dir : TURM_DIRECTIONS) {
            slidingPieces_captures(index, dir, moves, MASK_NONE);
        }
        for (int dir : LAEUFER_DIRECTIONS) {
            slidingPieces_captures(index, dir, moves, MASK_NONE);
        }
    }

    protected void pseudeLegalMoves_king(int index, int i, int j, MoveList moves){
        for (int ar : KOENIG_OFFSET) {
            if (this.field[ar + index] != INVALID && field[ar + index] * getActivePlayer() <= 0) {
                if(this.getActivePlayer() == 1){
                    moves.add(index, ar + index, this, (short)
                            (((board_meta_information & MASK_WHITE_QUEENSIDE_CASTLING) > 0 ? MASK_WHITE_QUEENSIDE_CASTLING:0) |
                                    ((board_meta_information & MASK_WHITE_KINGSIDE_CASTLING) > 0 ? MASK_WHITE_KINGSIDE_CASTLING :0))
                    );
                }else{
                    moves.add(index, ar + index, this, (short)
                            (((board_meta_information & MASK_BLACK_QUEENSIDE_CASTLING) > 0 ? MASK_BLACK_QUEENSIDE_CASTLING:0) |
                                    ((board_meta_information & MASK_BLACK_KINGSIDE_CASTLING) > 0 ? MASK_BLACK_KINGSIDE_CASTLING:0))
                    );
                }
            }
        }

        //################################ CASTLING #####################################
        if (((index == index(4,0) && this.getActivePlayer() == 1) ||
                (index == index(4,7) && this.getActivePlayer() == -1))) {

            //-------------------------------- WHITE --------------------------------------
            if(getActivePlayer() == 1){
                //-------------------------------- KING SIDE --------------------------------------
                if (field[index+3] == 2
                        && field[index + 1] == 0
                        && field[index + 2] == 0
                        && (board_meta_information & MASK_WHITE_KINGSIDE_CASTLING) > 0) {
                    moves.add(index, index + 2, 6, 0, (short)3);
                }

                //-------------------------------- QUEEN SIDE --------------------------------------
                if (field[index-4] == 2
                        && field[index - 1] == 0
                        && field[index - 2] == 0
                        && field[index - 3] == 0
                        && (board_meta_information & MASK_WHITE_QUEENSIDE_CASTLING) > 0) {
                    moves.add(index, index - 2, 6, 0,(short)3);
                }
            }
            //-------------------------------- BLACK --------------------------------------

            else{
                //-------------------------------- KING SIDE --------------------------------------
                if (field[index+3] == -2
                        && field[index + 1] == 0
                        && field[index + 2] == 0
                        && (board_meta_information & MASK_BLACK_KINGSIDE_CASTLING) > 0) {
                    moves.add(index, index + 2, -6, 0,(short)12);
                }

                //-------------------------------- QUEEN SIDE --------------------------------------
                if (field[index-4] == -2
                        && field[index - 1] == 0
                        && field[index - 2] == 0
                        && field[index - 3] == 0
                        && (board_meta_information & MASK_BLACK_QUEENSIDE_CASTLING) > 0) {
                    moves.add(index, index - 2, -6, 0,(short)12);
                }
            }
        }
    }
    protected void pseudeLegalMoves_king_captures(int index, int i, int j, MoveList moves) {
        for (int ar : KOENIG_OFFSET) {
            if (this.field[ar + index] != INVALID && field[ar + index] * getActivePlayer() < 0) {
                if (this.getActivePlayer() == 1) {
                    moves.add(index, ar + index, this, (short)
                            (((board_meta_information & MASK_WHITE_QUEENSIDE_CASTLING) > 0 ? MASK_WHITE_QUEENSIDE_CASTLING : 0) |
                                    ((board_meta_information & MASK_WHITE_KINGSIDE_CASTLING) > 0 ? MASK_WHITE_KINGSIDE_CASTLING : 0))
                    );
                } else {
                    moves.add(index, ar + index, this, (short)
                            (((board_meta_information & MASK_BLACK_QUEENSIDE_CASTLING) > 0 ? MASK_BLACK_QUEENSIDE_CASTLING : 0) |
                                    ((board_meta_information & MASK_BLACK_KINGSIDE_CASTLING) > 0 ? MASK_BLACK_KINGSIDE_CASTLING : 0))
                    );
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="move generation">
    @Override
    public List<Move> getPseudoLegalMoves() {
        return this.getPseudoLegalMoves(new MoveList(50));
    }

    @Override
    public List<Move> getPseudoLegalMoves(MoveList moves) {
        moves.clear();
        for (int i = 26; i < 118; i++) {
            if (field[i] == INVALID || field[i] * activePlayer <= 0) continue;

            switch (field[i] * getActivePlayer()) {
                case 1:
                    pseudeLegalMoves_pawn(i, x(i), y(i), moves);
                    break;
                case 3:
                    pseudeLegalMoves_knight(i, 0, 0, moves);
                    break;
                case 2:
                    pseudeLegalMoves_rook(i, 0, 0, moves);
                    break;
                case 4:
                    pseudeLegalMoves_bishop(i, 0, 0, moves);
                    break;
                case 5:
                    pseudeLegalMoves_queen(i, 0, 0, moves);
                    break;
                case 6:
                    pseudeLegalMoves_king(i, 0, 0, moves);
                    break;
            }
        }

        return moves;
    }

    @Override
    public List<Move> getLegalMoves() {
        return this.getLegalMoves(new MoveList(50));
    }

    @Override
    public List<Move> getLegalMoves(MoveList list) {
        List<Move> moves = getPseudoLegalMoves(list);
        MoveList ml = new MoveList(50);

        boolean shortCastle= true;
        boolean longCastle = true;

        boolean inCheck = false;
        this.changeActivePlayer();
        List<Move> opMoves = getPseudoLegalMoves(ml);
        for(Move m:opMoves){
            if(m.getPieceTo() * this.getActivePlayer() == -6){
                inCheck = true;
                break;
            }
            if(this.getActivePlayer() == 1){
                if(m.getTo() == 115){
                    shortCastle = false;
                }
                if(m.getTo() == 112){
                    longCastle = false;
                }
            }else{
                if(m.getTo() == 31){
                    shortCastle = false;
                }
                if(m.getTo() == 28){
                    longCastle = false;
                }
            }
        }
        this.changeActivePlayer();

        for(int i = moves.size()-1; i>= 0; i--){
            Move observe = moves.get(i);
            move(observe);

            if(observe.getPieceFrom() == -this.getActivePlayer()*6){
                if(observe.getTo() - observe.getFrom() == 2 && (!shortCastle || inCheck)){
                    moves.remove(i);
                    undoMove();
                    continue;
                }
                else if(observe.getTo() - observe.getFrom() == -2 && (!longCastle ||inCheck)){
                    moves.remove(i);
                    undoMove();
                    continue;
                }
            }

            List<Move> opponent = getPseudoLegalMoves(ml);
            for(Move m:opponent){
                if(m.getPieceTo() * this.getActivePlayer() == -6){
                    moves.remove(i);
                    break;
                }
            }
            undoMove();
        }

        if(moves.size() == 0){
            if(!inCheck){
                moves.add(new Move());
            }
        }

        return moves;
    }

    @Override
    public List<Move> getCaptureMoves(MoveList moves) {
        moves.clear();
        for (int i = 26; i < 118; i++) {
            if (field[i] == INVALID || field[i] * activePlayer <= 0) continue;

            switch (field[i] * getActivePlayer()) {
                case 1:
                    pseudeLegalMoves_pawn_captures(i, x(i), y(i), moves);
                    break;
                case 3:
                    pseudeLegalMoves_knight_captures(i, 0, 0, moves);
                    break;
                case 2:
                    pseudeLegalMoves_rook_captures(i, 0, 0, moves);
                    break;
                case 4:
                    pseudeLegalMoves_bishop_captures(i, 0, 0, moves);
                    break;
                case 5:
                    pseudeLegalMoves_queen_captures(i, 0, 0, moves);
                    break;
                case 6:
                    pseudeLegalMoves_king_captures(i, 0, 0, moves);
                    break;
            }
        }
        return moves;
    }

    @Override
    public List<Move> getCaptureMoves() {
        return getCaptureMoves(new MoveList(10));
    }
    //</editor-fold>

    //<editor-fold desc="pieces">
    @Override
    public void setPiece(int x, int y, int piece) {
        this.setPiece(index(x,y), piece);
    }

    @Override
    public void setPiece(int index, int piece) {

        if(this.field[index] != 0 && this.field[index] != INVALID){
            if(this.field[index] > 0){
                this.zobristKey ^= BitBoard.white_hashes[this.field[index]-1][x(index) * 8 + y(index)];
            }else{
                this.zobristKey ^= BitBoard.black_hashes[-this.field[index]-1][x(index) * 8 + y(index)];
            }
        }

        field[index] = piece;

        if(field[index] != INVALID && piece != 0){
            if(piece > 0){
                this.zobristKey ^= BitBoard.white_hashes[piece-1][x(index) * 8 + y(index)];
            }else{
                this.zobristKey ^= BitBoard.black_hashes[-piece-1][x(index) * 8 + y(index)];
            }
        }
    }

    @Override
    public boolean previousMoveIsLegal() {
        return false;
    }

    @Override
    public int getPiece(int x, int y) {
        return field[index(x, y)];
    }

    @Override
    public int getPiece(int index) {
        return field[index];
    }
    //</editor-fold>


    @Override
    public void reset() {

        this.field = new int[12 * 12];
        this.board_meta_information = MASK_BLACK_KINGSIDE_CASTLING |
                MASK_WHITE_KINGSIDE_CASTLING |
                MASK_WHITE_QUEENSIDE_CASTLING|
                MASK_BLACK_QUEENSIDE_CASTLING;
        this.board_repetition_counter = new RepetitionList();
        this.moveHistory.clear();
        for (byte i = 0; i < 12; i++) {
            for (byte j = 0; j < 12; j++) {
                if (i < 2 || i > 9 || j < 2 || j > 9) {
                    field[i * 12 + j] = INVALID;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlowBoard slowBoard = (SlowBoard) o;
        return Arrays.equals(field, slowBoard.field);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 11; i >= 0; i--) {
            for (int n = 0; n < 12; n++) {
                int b = field[index((byte) (n - 2), (byte) (i - 2))];
                if (b < -10) {
                    builder.append("#");
                } else {
                    double offset = b > 0 ? 32:0;
                    switch (Math.abs(b)){
                        case 1: builder.append((char)('p' - offset)); break;
                        case 2: builder.append((char)('r' - offset)); break;
                        case 3: builder.append((char)('n' - offset)); break;
                        case 4: builder.append((char)('b' - offset)); break;
                        case 5: builder.append((char)('q' - offset)); break;
                        case 6: builder.append((char)('k' - offset)); break;
                        default:builder.append(" ");
                    }
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public SlowBoard newInstance() {
        return new SlowBoard();
    }

    @Override
    public SlowBoard copy() {
        SlowBoard board = new SlowBoard();
        if (this.getActivePlayer() != board.getActivePlayer()) board.changeActivePlayer();
        board.field = Arrays.copyOf(field, 144);
        board.board_meta_information = this.board_meta_information;
        board.board_repetition_counter = this.board_repetition_counter.copy();
        board.zobristKey = zobristKey;
        for (Move move : moveHistory) {
            board.moveHistory.push(move.copy());
        }
        return board;
    }



    public static void main(String[] args) {
        FastBoard b = new FastBoard(Setup.DEFAULT);

        b = IO.read_FEN(b, "8/2q1P1k1/8/8/8/8/8/4KQ2 w - - 0 1");
        //b = IO.read_FEN(b, "QBN2nbq/QBN2nbq/QBN2nbq/KBN2nbq/QBN2nbk/QBN2nbq/QBN2nbq/QBN2nbq w - - 0 1");
        //b = IO.read_FEN(b, "6k1/8/5K2/1Q6/8/8/8/8 w - - 0 1");
        //b = IO.read_FEN(b,"r1bq1rk1/ppp1bppp/4p3/n2pP3/3P3P/2PBBN2/P1P2PP1/1R1QK2R b Kq - 0 1");
        //b = IO.read_FEN(b,"8/8/8/8/5q2/5k2/8/4K3_w_-_-_0_1");


        PVSearchFast ai1 = new PVSearchFast(
                new NoahEvaluator2(),
                new SystematicOrderer2(),
                new SenpaiReducer(2),
                1,
                2000,
                0);
        ai1.setUse_iteration(true);
        ai1.setUse_killer_heuristic(true);
        ai1.setUse_null_moves(true);
        ai1.setUse_LMR(true);
        ai1.setUse_transposition(false);
        ai1.setUse_move_lists(true);



//        NoahEvaluator2 ev2 = new NoahEvaluator2();
//        ev2.setEvolvableValues(new double[]{+25.0, -25.0, +22.8, +64.1, +10.1, +23.8, +25.0, +10.0, +15.5});
//        PVSearchFast ai2 = new PVSearchFast(
//                ev2,
//                new SystematicOrderer2(),
//                new SenpaiReducer(2),
//                1,
//                2000,
//                0);
//        ai2.setUse_iteration(true);
//        ai2.setUse_killer_heuristic(true);
//        ai2.setUse_null_moves(true);
//        ai2.setUse_LMR(true);
//        ai2.setUse_transposition(false);

        //ai1.bestMove(b);

//        PVSearch ai2 = new PVSearch(
//                new NoahEvaluator(),
//                new SystematicOrderer2(),
//                new SenpaiReducer(2),
//                1,
//                5000,
//                0);
//        ai2.setUse_killer_heuristic(true);
//        ai2.setUse_null_moves(true);
//        ai2.setUse_LMR(true);
//        ai2.setUse_transposition(false);

        //MCTS<ChessNodeData> mcts = new MCTS<>(new UCT(), new EvaluatingSimulator(),new ChessExpander());


        new Frame(b,ai1, new Player(){}).getGamePanel().getGame().addBoardChangedListener(new Runnable() {
            @Override
            public void run() {
                //System.out.println(b);

            }
        });
    }

}
