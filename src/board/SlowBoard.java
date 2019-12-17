package board;

import game.Game;
import game.Player;
import game.ai.search.AlphaBeta;
import game.ai.evaluator.FinnEvaluator;
import game.ai.ordering.SimpleOrderer;
import io.IOBoard;
import visual.Frame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlowBoard extends Board<SlowBoard> {


    private static final int[] TURM_DIRECTIONS = {12, -12, 1, -1};
    private static final int[] LAEUFER_DIRECTIONS = {13, 11, -11, -13};
    private static final int[] SPRINGER_OFFSET = {23, 25, 14, 10, -10, -14, -23, -25};
    private static final int[] KOENIG_OFFSET = {12, 13, 1, -13, -12, -11, -1, 11};

    private static int INVALID = Byte.MIN_VALUE;

    private int[] field; //2x2 padding each side

//    private boolean white_shortCastle = false;
//    private boolean white_longCastle = false;
//    private boolean black_shortCastle = false;
//    private boolean black_longCastle = false;


    @Override
    public int getPiece(int x, int y) {
        return field[index(x, y)];
    }

    public int index(int x, int y) {
        return (y + 2) * 12 + (x + 2);
    }

    public int x(int index) {
        return (index % 12 - 2);
    }

    public int y(int index) {
        return (index / 12 - 2);
    }

    @Override
    public int getPiece(int index) {
        return field[index];
    }

    @Override
    public SlowBoard copy() {
        SlowBoard board = new SlowBoard();
        if (this.getActivePlayer() != board.getActivePlayer()) board.changeActivePlayer();
        board.field = Arrays.copyOf(field, 144);
        for (Move move : moveHistory) {
            board.moveHistory.push(move.copy());
        }
        return board;
    }

    @Override
    public long zobrist() {
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
    public void move(Move m) {
        if (m.getIsNull()) { // does null move stuff
            this.changeActivePlayer();
            this.moveHistory.push(m);
            return;
        }
        if (m.getPieceFrom() * getActivePlayer() == 6) {
            if (Math.abs(m.getTo() - m.getFrom()) == 2) {
                if (m.getTo() > m.getFrom()) {
                    this.moveSimpleMove(new Move(m.getFrom() + 3, m.getFrom() + 1, this));
                } else {
                    this.moveSimpleMove(new Move(m.getFrom() - 4, m.getFrom() - 1, this));
                }
            }
        }
        this.moveSimpleMove(m); // stuff for every non-null move

        if(m.getPieceFrom() * getActivePlayer() == 1){
            if((y(m.getTo()) == 7 && getActivePlayer() == 1) ||
                    (y(m.getTo()) == 0 && getActivePlayer() == -1)){
                this.field[m.getTo()] = 5 * getActivePlayer();
            }
        }

        this.changeActivePlayer();
    }

    private void moveSimpleMove(Move m) {
        this.field[m.getFrom()] = 0;
        this.field[m.getTo()] = m.getPieceFrom();
        this.moveHistory.push(m);
    }

    private void undoMoveSimpleMove() {
        if (this.moveHistory.size() == 0) return;
        Move old = this.moveHistory.pop();
        this.field[old.getFrom()] = old.getPieceFrom();
        this.field[old.getTo()] = old.getPieceTo();
    }

    private void slidingPieces(int pos, int direction, ArrayList<Move> list) {
        int c = pos + direction;
        while (field[c] != INVALID) {
            if (field[c] != 0) {
                if (field[c] * getActivePlayer() < 1) {
                    list.add(new Move(pos, c, this));
                }
                return;
            } else {
                list.add(new Move(pos, c, this));
            }
            c += direction;
        }
    }

    @Override
    public List<Move> getAvailableMovesComplete() {
        return getAvailableMovesShallow();
    }

    @Override
    public List<Move> getAvailableMovesShallow() {
        ArrayList<Move> moves = new ArrayList<>(50);
        if (isGameOver()) return moves;
        for (byte i = 0; i < 8; i++) {
            for (byte j = 0; j < 8; j++) {
                int index = index(i, j);
                if (field[index] * getActivePlayer() <= 0) continue;
                if (field[index] == getActivePlayer()) { // Bauern
                    if (j == 0 || j == 7) continue;
                    if (field[index + getActivePlayer() * 12] == 0) {
                        moves.add(new Move(index, index + getActivePlayer() * 12, getActivePlayer(), (byte) 0));
                        if ((j == getActivePlayer() || j == 7 + getActivePlayer()) && field[index + getActivePlayer() * 2 * 12] == 0) {
                            moves.add(new Move(index, index + getActivePlayer() * 24, getActivePlayer(), (byte) 0));
                        }
                    }
                    if (field[index + getActivePlayer() * 11] != INVALID && field[index + getActivePlayer() * 11] * getActivePlayer() < 0) {
                        moves.add(new Move(index, index + getActivePlayer() * 11, this));
                    }
                    if (field[index + getActivePlayer() * 13] != INVALID && field[index + getActivePlayer() * 13] * getActivePlayer() < 0) {
                        moves.add(new Move(index, index + getActivePlayer() * 13, this));
                    }
                } else if (getPiece(i, j) == getActivePlayer() * 3) { // Springer
                    for (int ar : SPRINGER_OFFSET) {
                        if (this.field[ar + index] != INVALID && field[ar + index] * getActivePlayer() <= 0) {
                            moves.add(new Move(index, ar + index, this));
                        }
                    }
                } else if (getPiece(i, j) == getActivePlayer() * 2) { // Türme
                    for (int dir : TURM_DIRECTIONS) {
                        slidingPieces(index, dir, moves);
                    }
                } else if (getPiece(i, j) == getActivePlayer() * 5) { // Dame
                    for (int dir : TURM_DIRECTIONS) {
                        slidingPieces(index, dir, moves);
                    }
                    for (int dir : LAEUFER_DIRECTIONS) {
                        slidingPieces(index, dir, moves);
                    }
                } else if (getPiece(i, j) == getActivePlayer() * 4) { // Läufer
                    for (int dir : LAEUFER_DIRECTIONS) {
                        slidingPieces(index, dir, moves);
                    }
                } else if (getPiece(i, j) == getActivePlayer() * 6) { //König
                    for (int ar : KOENIG_OFFSET) {
                        if (this.field[ar + index] != INVALID && field[ar + index] * getActivePlayer() <= 0) {
                            moves.add(new Move(index, ar + index, this));
                        }
                    }
                    if (((index == index(4,0) && this.getActivePlayer() == 1) ||
                            (index == index(4,7) && this.getActivePlayer() == -1))) {
                        if (field[index+3] * getActivePlayer() == 2 &&
                        field[index + 1] == 0 && field[index + 2] == 0) {
                            moves.add(new Move(index, index + 2, this));
                        } if (field[index-4] * getActivePlayer() == 2 &&
                                field[index - 1] == 0 && field[index - 2] == 0 && field[index - 3] == 0) {
                            moves.add(new Move(index, index - 2, this));
                        }
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public void undoMove() {
        if (this.moveHistory.size() == 0) return;

        if (this.moveHistory.peek().getIsNull()) { // null move stuff
            this.changeActivePlayer();
            this.moveHistory.pop();
            return;
        }

        int turn = this.moveHistory.peek().getPieceFrom() > 0 ? 1:-1;

        while(this.moveHistory.size() > 0 && turn * this.moveHistory.peek().getPieceFrom() > 0){
            undoMoveSimpleMove();
        }

        this.changeActivePlayer();
    }

    @Override
    public void reset() {
        this.clear();
        field[index((byte) 0, (byte) 0)] = (byte) 2;
        field[index((byte) 1, (byte) 0)] = (byte) 3;
        field[index((byte) 2, (byte) 0)] = (byte) 4;
        field[index((byte) 3, (byte) 0)] = (byte) 5;
        field[index((byte) 4, (byte) 0)] = (byte) 6;
        field[index((byte) 5, (byte) 0)] = (byte) 4;
        field[index((byte) 6, (byte) 0)] = (byte) 3;
        field[index((byte) 7, (byte) 0)] = (byte) 2;
        field[index((byte) 0, (byte) 1)] = (byte) 1;
        field[index((byte) 1, (byte) 1)] = (byte) 1;
        field[index((byte) 2, (byte) 1)] = (byte) 1;
        field[index((byte) 3, (byte) 1)] = (byte) 1;
        field[index((byte) 4, (byte) 1)] = (byte) 1;
        field[index((byte) 5, (byte) 1)] = (byte) 1;
        field[index((byte) 6, (byte) 1)] = (byte) 1;
        field[index((byte) 7, (byte) 1)] = (byte) 1;
        field[index((byte) 0, (byte) 7)] = -(byte) 2;
        field[index((byte) 1, (byte) 7)] = -(byte) 3;
        field[index((byte) 2, (byte) 7)] = -(byte) 4;
        field[index((byte) 3, (byte) 7)] = -(byte) 5;
        field[index((byte) 4, (byte) 7)] = -(byte) 6;
        field[index((byte) 5, (byte) 7)] = -(byte) 4;
        field[index((byte) 6, (byte) 7)] = -(byte) 3;
        field[index((byte) 7, (byte) 7)] = -(byte) 2;
        field[index((byte) 0, (byte) 6)] = -(byte) 1;
        field[index((byte) 1, (byte) 6)] = -(byte) 1;
        field[index((byte) 2, (byte) 6)] = -(byte) 1;
        field[index((byte) 3, (byte) 6)] = -(byte) 1;
        field[index((byte) 4, (byte) 6)] = -(byte) 1;
        field[index((byte) 5, (byte) 6)] = -(byte) 1;
        field[index((byte) 6, (byte) 6)] = -(byte) 1;
        field[index((byte) 7, (byte) 6)] = -(byte) 1;
//        black_longCastle = true;
//        black_shortCastle = true;
//        white_longCastle = true;
//        white_shortCastle = true;
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
                    builder.append(Math.abs(b));
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public boolean isGameOver() {
        boolean whiteKing = false;
        boolean blackKing = false;
        for (byte i = 0; i < 8; i++) {
            for (byte j = 0; j < 8; j++) {
                if (getPiece(i, j) == 6) whiteKing = true;
                if (getPiece(i, j) == -6) blackKing = true;
            }
        }
        return (!whiteKing || !blackKing);
    }

    public int[] getField() {
        return field;
    }

    public static void main(String[] args) {
        AlphaBeta alphaBeta1 = new AlphaBeta(new FinnEvaluator(), new SimpleOrderer(), 6    ,0);
        alphaBeta1.setUse_iteration(false);
        alphaBeta1.setUse_transposition(true);
        alphaBeta1.setPrint_overview(true);
        SlowBoard board = IOBoard.read_lichess(new SlowBoard(), "1r6/1rkn4/2nb2R1/2p1p3/1P2Pp2/1QP2P2/2K5/8");
        //new Frame(new Game(board, new Player(){}, alphaBeta1));
        System.out.println(alphaBeta1.bestMove(board));
    }

    @Override
    public SlowBoard newInstance() {
        return new SlowBoard();
    }

    @Override
    public void setPiece(int x, int y, int piece) {
        field[index(x,y)] = piece;
    }

    @Override
    public void setPiece(int index, int piece) {
        field[index] = piece;
    }

    @Override
    public int winner() {
        boolean whiteKing = false;
        boolean blackKing = false;
        for (byte i = 0; i < 8; i++) {
            for (byte j = 0; j < 8; j++) {
                if (getPiece(i, j) == 6) whiteKing = true;
                if (getPiece(i, j) == -6) blackKing = true;
            }
        }
        if(whiteKing && blackKing) return 0;
        if(whiteKing) return 1;
        if(blackKing) return -1;
        return 0;
    }

    @Override
    public void clear() {
        this.field = new int[12 * 12];
        for (byte i = 0; i < 12; i++) {
            for (byte j = 0; j < 12; j++) {
                if (i < 2 || i > 9 || j < 2 || j > 9) {
                    field[i * 12 + j] = INVALID;
                }
            }
        }
    }
}
