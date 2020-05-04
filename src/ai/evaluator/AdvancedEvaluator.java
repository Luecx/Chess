package ai.evaluator;

import ai.evaluator.decider.BoardPhaseDecider;
import ai.evaluator.decider.SimpleDecider;
import ai.tools.tensor.Tensor1D;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.pieces.PieceList;
import io.IO;
import io.UCI;

import java.util.Arrays;

public class AdvancedEvaluator implements Evaluator<AdvancedEvaluator> {

    /**
     * used to determine the gamestate (0 for early game, 1 for endgame)
     */
    private BoardPhaseDecider phaseDecider;



    /**
     * piece square tables (PST) for white pieces.
     * the first row equals line-8.
     * They are scaled by 0.01 (=1/100) in order to be tuned using PARAMETER_...._TABLE_FACTOR_....
     */
    //<editor-fold desc="Early PST">


    public static final Tensor1D PAWN_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0})).scale(0.01);

    public static final Tensor1D BISHOP_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -40, -10, -10, -40, -10, -20,
            })).scale(0.01);

    public static final Tensor1D ROOK_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 20, 20, 20, 20, 20, 20, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 5, 7, 5, 7, 0, -5
    })).scale(0.01);

    public static final Tensor1D KNIGHT_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -16, -12, -12, -12, -12, -16, -20,
            -8, -4, 0, 0, 0, 0, -4, -8,
            -12, 4, 8, 12, 12, 12, 4, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -6, 10, 8, 6, 6, 8, 2, -6,
            -16, -8, 0, 2, 2, 0, -8, -16,
            -24, -60, -12, -12, -12, -12, -60, -24,
            })).scale(0.01);
    public static final Tensor1D QUEEN_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, 2, -5, -10, -10, -20
    })).scale(0.01);

    public static final Tensor1D KING_VALUES_MID_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, -10, -50, -50, -50, 20, 20,
            20, 30, 25, -50, 0, -50, 30, 20
    })).scale(0.01);

    public static final Tensor1D KING_VALUES_MID_BLACK = flipTensor(KING_VALUES_MID_WHITE);
    public static final Tensor1D QUEEN_VALUES_BLACK = flipTensor(QUEEN_VALUES_WHITE);
    public static final Tensor1D ROOK_VALUES_BLACK = flipTensor(ROOK_VALUES_WHITE);
    public static final Tensor1D BISHOP_VALUES_BLACK = flipTensor(BISHOP_VALUES_WHITE);
    public static final Tensor1D KNIGHT_VALUES_BLACK = flipTensor(KNIGHT_VALUES_WHITE);
    public static final Tensor1D PAWN_VALUES_BLACK = flipTensor(PAWN_VALUES_WHITE);

    public static final Tensor1D[] WHITE_PST_EARLY = new Tensor1D[]{
            PAWN_VALUES_WHITE,
            ROOK_VALUES_WHITE,
            KNIGHT_VALUES_WHITE,
            BISHOP_VALUES_WHITE,
            QUEEN_VALUES_WHITE,
            KING_VALUES_MID_WHITE};
    public static final Tensor1D[] BLACK_PST_EARLY = new Tensor1D[]{
            PAWN_VALUES_BLACK,
            ROOK_VALUES_BLACK,
            KNIGHT_VALUES_BLACK,
            BISHOP_VALUES_BLACK,
            QUEEN_VALUES_BLACK,
            KING_VALUES_MID_BLACK};


    public static Tensor1D flipTensor(Tensor1D tensor) {
        Tensor1D flipped = new Tensor1D(tensor);
        for (int i = 0; i < 32; i++) {
            int file = BitBoard.fileIndex(i);
            int rank = BitBoard.rankIndex(i);
            int otherRank = 7 - rank;
            flipped.set(tensor.get(BitBoard.squareIndex(otherRank, file)), BitBoard.squareIndex(rank, file));
            flipped.set(tensor.get(BitBoard.squareIndex(rank, file)), BitBoard.squareIndex(otherRank, file));
        }
        return flipped;
    }
    //</editor-fold>

    //<editor-fold desc="Late PST">
    public static final Tensor1D PAWN_VALUES_WHITE_LATE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            100, 150, 150, 150, 150, 150, 150, 100,
            10, 30, 60, 100, 100, 60, 30, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5,  0,  0,-20,-20,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0})).scale(0.01);

    public static final Tensor1D BISHOP_VALUES_WHITE_LATE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20,
            })).scale(0.01);

    public static final Tensor1D ROOK_VALUES_WHITE_LATE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 20, 20, 20, 20, 20, 20, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 5, 7, 5, 7, 0, -5
    })).scale(0.01);

    public static final Tensor1D KNIGHT_VALUES_WHITE_LATE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -16, -12, -12, -12, -12, -16, -20,
            -8, -4, 0, 0, 0, 0, -4, -8,
            -12, 4, 8, 12, 12, 12, 4, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -6, 10, 8, 6, 6, 8, 2, -6,
            -16, -8, 0, 2, 2, 0, -8, -16,
            -24, -50, -12, -12, -12, -12, -50, -24,
            })).scale(0.01);
    public static final Tensor1D QUEEN_VALUES_WHITE_LATE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
    })).scale(0.01);

    public static final Tensor1D KING_VALUES_WHITE_LATE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -10, -10, -10, -10, -10, -10, -10, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10,   0,  30,  30,  30,  30,   0, -10,
            -10,   0,  30,  70,  70,  30,   0, -10,
            -10,   0,  30,  70,  70,  30,   0, -10,
            -10,   0,  30,  30,  30,  30,   0, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10, -10, -10, -10, -10, -10, -10, -10,
    })).scale(0.01);

    public static final Tensor1D KING_VALUES_BLACK_LATE = flipTensor(KING_VALUES_WHITE_LATE);
    public static final Tensor1D QUEEN_VALUES_BLACK_LATE = flipTensor(QUEEN_VALUES_WHITE_LATE);
    public static final Tensor1D ROOK_VALUES_BLACK_LATE = flipTensor(ROOK_VALUES_WHITE_LATE);
    public static final Tensor1D BISHOP_VALUES_BLACK_LATE = flipTensor(BISHOP_VALUES_WHITE_LATE);
    public static final Tensor1D KNIGHT_VALUES_BLACK_LATE = flipTensor(KNIGHT_VALUES_WHITE_LATE);
    public static final Tensor1D PAWN_VALUES_BLACK_LATE = flipTensor(PAWN_VALUES_WHITE_LATE);

    public static final Tensor1D[] WHITE_PST_LATE = new Tensor1D[]{
            PAWN_VALUES_WHITE_LATE,
            ROOK_VALUES_WHITE_LATE,
            KNIGHT_VALUES_WHITE_LATE,
            BISHOP_VALUES_WHITE_LATE,
            QUEEN_VALUES_WHITE_LATE,
            KING_VALUES_WHITE_LATE};
    public static final Tensor1D[] BLACK_PST_LATE = new Tensor1D[]{
            PAWN_VALUES_BLACK_LATE,
            ROOK_VALUES_BLACK_LATE,
            KNIGHT_VALUES_BLACK_LATE,
            BISHOP_VALUES_BLACK_LATE,
            QUEEN_VALUES_BLACK_LATE,
            KING_VALUES_BLACK_LATE};


    //</editor-fold>


    /**
     * some constants like pawn/king values
     */
    private double CONST_PARAMETER_KING_VALUE_EARLY =                                10000;
    private double CONST_PARAMETER_PAWN_VALUE_LATE =                                 100;
    private double CONST_PARAMETER_KING_VALUE_LATE =                                 10000;
    private double CONST_PARAMETER_PAWN_VALUE_EARLY =                                100;

    /**
     * tunable params
     */
    private double PARAMETER_PAWN_TABLE_FACTOR_EARLY =                               79;
    private double PARAMETER_PAWN_CONNECTED_EARLY =                                  16;
    private double PARAMETER_PAWN_PASSED_EARLY =                                     -3;
    private double PARAMETER_PAWN_ISOLATED_EARLY =                                   -22;
    private double PARAMETER_PAWN_DOUBLED_EARLY =                                    -3;
    private double PARAMETER_PAWN_CONNECTED_PASSED_EARLY =                           20;
    private double PARAMETER_PAWN_DOUBLED_ISOLATED_EARLY =                           -10;

    private double PARAMETER_KNIGHT_TABLE_FACTOR_EARLY =                             50;
    private double PARAMETER_KNIGHT_VALUE_EARLY =                                    479;
    private double PARAMETER_KNIGHT_VISIBILITY_EARLY =                               10;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY =                    -15;
    private double PARAMETER_KNIGHT_TRAPPED_EARLY =                                  -34;

    private double PARAMETER_ROOK_TABLE_FACTOR_EARLY =                               152;
    private double PARAMETER_ROOK_VALUE_EARLY =                                      627;
    private double PARAMETER_ROOK_VISIBILITY_EARLY =                                 5;
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY =                      -13;
    private double PARAMETER_ROOK_TRAPPED_EARLY =                                    -6;
    private double PARAMETER_ROOK_KING_LINE_EARLY =                                  6;
    private double PARAMETER_ROOK_HALF_OPEN_EARLY =                                  26;
    private double PARAMETER_ROOK_OPEN_EARLY =                                       51;

    private double PARAMETER_BISHOP_TABLE_FACTOR_EARLY =                             144;
    private double PARAMETER_BISHOP_VALUE_EARLY =                                    474;
    private double PARAMETER_BISHOP_VISIBILITY_EARLY =                               8;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY =                    -10;
    private double PARAMETER_BISHOP_TRAPPED_EARLY =                                  -67;
    private double PARAMETER_BISHOP_DOUBLED_EARLY =                                  40;
    private double PARAMETER_BISHOP_CLOSED_PENALTY_EARLY =                           2;
    private double PARAMETER_BISHOP_OPEN_BONUS_EARLY =                               15;

    private double PARAMETER_QUEEN_TABLE_FACTOR_EARLY =                              67;
    private double PARAMETER_QUEEN_VALUE_EARLY =                                     1477;
    private double PARAMETER_QUEEN_VISIBILITY_EARLY =                                3;
    private double PARAMETER_QUEEN_TRAPPED_EARLY =                                   12;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY =                     -16;

    private double PARAMETER_KING_TABLE_FACTOR_EARLY =                               144;
    private double PARAMETER_KING_SAFETY_1_EARLY =                                   11;
    private double PARAMETER_KING_SAFETY_2_EARLY =                                   -73;
    private double PARAMETER_KING_SAFETY_3_EARLY =                                   -28;
    private double PARAMETER_KING_PAWN_SHIELD_EARLY =                                20;



    private double PARAMETER_PAWN_TABLE_FACTOR_LATE =                                137;
    private double PARAMETER_PAWN_PASSED_LATE =                                      69;
    private double PARAMETER_PAWN_ISOLATED_LATE =                                    -11;
    private double PARAMETER_PAWN_DOUBLED_LATE =                                     -9;
    private double PARAMETER_PAWN_CONNECTED_LATE =                                   11;
    private double PARAMETER_PAWN_CONNECTED_PASSED_LATE =                            55;
    private double PARAMETER_PAWN_DOUBLED_ISOLATED_LATE =                            -40;

    private double PARAMETER_ROOK_TABLE_FACTOR_LATE =                                82;
    private double PARAMETER_ROOK_VALUE_LATE =                                       681;
    private double PARAMETER_ROOK_VISIBILITY_LATE =                                  5;
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE =                       3;
    private double PARAMETER_ROOK_TRAPPED_LATE =                                     -16;
    private double PARAMETER_ROOK_KING_LINE_LATE =                                   9;
    private double PARAMETER_ROOK_HALF_OPEN_LATE =                                   -1;
    private double PARAMETER_ROOK_OPEN_LATE =                                        -16;

    private double PARAMETER_KNIGHT_TABLE_FACTOR_LATE =                              156;
    private double PARAMETER_KNIGHT_VALUE_LATE =                                     349;
    private double PARAMETER_KNIGHT_VISIBILITY_LATE =                                6;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE =                     3;
    private double PARAMETER_KNIGHT_TRAPPED_LATE =                                   -20;

    private double PARAMETER_BISHOP_TABLE_FACTOR_LATE =                              91;
    private double PARAMETER_BISHOP_VALUE_LATE =                                     374;
    private double PARAMETER_BISHOP_VISIBILITY_LATE =                                2;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE =                     7;
    private double PARAMETER_BISHOP_TRAPPED_LATE =                                   -2;
    private double PARAMETER_BISHOP_DOUBLED_LATE =                                   65;
    private double PARAMETER_BISHOP_CLOSED_PENALTY_LATE =                            -12;
    private double PARAMETER_BISHOP_OPEN_BONUS_LATE =                                15;

    private double PARAMETER_QUEEN_TABLE_FACTOR_LATE =                               46;
    private double PARAMETER_QUEEN_VALUE_LATE =                                      1169;
    private double PARAMETER_QUEEN_VISIBILITY_LATE =                                 9;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE =                      12;
    private double PARAMETER_QUEEN_TRAPPED_LATE =                                    -69;

    private double PARAMETER_KING_TABLE_FACTOR_LATE =                                12;
    private double PARAMETER_KING_SAFETY_1_LATE =                                    -7;
    private double PARAMETER_KING_SAFETY_2_LATE =                                    53;
    private double PARAMETER_KING_SAFETY_3_LATE =                                    13;
    private double PARAMETER_KING_PAWN_SHIELD_LATE =                                 4;


    double[] pieceVals = new double[]{0,
                                      CONST_PARAMETER_PAWN_VALUE_EARLY,
                                      PARAMETER_ROOK_VALUE_EARLY,
                                      PARAMETER_KNIGHT_VALUE_EARLY,
                                      PARAMETER_BISHOP_VALUE_EARLY,
                                      PARAMETER_QUEEN_VALUE_EARLY,
                                      CONST_PARAMETER_KING_VALUE_EARLY};




    private String[] evalNames  = new String[]{"Pawns", "Rooks", "Knights", "Bishops",
                                               "Queen Position", "Queen Existence", "Queen visibility", "Queen covered visibility", "Queen trapped",
                                               "King position", "King friendly pieces", "King hostile pieces", "King pawn shield"};

    private double[] evalResults = new double[evalNames.length];


    public AdvancedEvaluator(BoardPhaseDecider phaseDecider) {
        this.phaseDecider = phaseDecider;
    }

    /**
     * taper is used to interpolate between to params.
     * taper should equal 0 for early game and 1 for late game.
     * @param param1
     * @param param2
     * @param taper
     * @return
     */
    public double taper(double param1, double param2, double taper) {
        return param1 + (param2 - param1) * taper;
    }

    /**
     * returns true if the board is likely to be a draw
     */
    public boolean probablyInsufficientMaterial(PieceList[] white, PieceList[] black){
        if(white[0].size() != 0 || black[0].size() != 0) return false; //if pawns exist


        int wAdv = white[1].size() * 5 + white[2].size() * 3 + white[3].size() * 3 + white[4].size() * 9;
        int bAdv = black[1].size() * 5 + black[2].size() * 3 + black[3].size() * 3 + black[4].size() * 9;

        int advantage = wAdv - bAdv;


        if(wAdv < 10 && bAdv < 10 && Math.abs(advantage) < 4) return true;
        return false;
    }


    /**
     * used to evaluate the board.
     * First the game phase aka. taper is evaluated.
     * Then blacks evaluation is subtracted from white evaluation.
     *
     *
     *
     * @param board
     * @return
     */
    @Override
    public double evaluate(Board board) {
        double phase = phaseDecider.getGamePhase(board);
        FastBoard fb = (FastBoard) board;

        if(probablyInsufficientMaterial(fb.getWhite_pieces(), fb.getBlack_pieces())) return 0;



        return evaluateWhite(fb, phase) - evaluateBlack(fb, phase);
    }

    public double evaluateWhite(FastBoard fb, double phase){
        return evaluateSide(1,
                            fb.getWhite_pieces(),
                            WHITE_PST_EARLY,
                            WHITE_PST_LATE,
                            fb.getWhite_values(),
                            fb.getTeam_total()[0],
                            BitBoard.whitePassedPawnMask,
                            fb.getBlack_values(),
                            fb.getTeam_total()[1],
                            fb.getOccupied(),
                            fb.getAttackedSquaresFromBlack(),
                            phase);
    }

    public double evaluateBlack(FastBoard fb, double phase){
        return evaluateSide(-1,
                            fb.getBlack_pieces(),
                            BLACK_PST_EARLY,
                            BLACK_PST_LATE,
                            fb.getBlack_values(),
                            fb.getTeam_total()[1],
                            BitBoard.blackPassedPawnMask,
                            fb.getWhite_values(),
                            fb.getTeam_total()[0],
                            fb.getOccupied(),
                            fb.getAttackedSquaresFromWhite(),
                            phase);
    }


    /**
     * evaluates the position for the given side.
     * requires the following params:
     * @param color                     1 for white, -1 for black
     * @param ourPieces                 an array of PieceList's for this colored pieces
     * @param earlyPST                  an array of PST for this side for the early game
     * @param latePST                   an array of PST for this side for the late game
     * @param ourPieceOccupancy         an array of bitboards which mark the occupancy of our pieces
     * @param ourTotalOccupancy         a bitboard which marks all occupied squares by this color
     * @param ourPassedPawnMask         an array of masks for passed pawns detection
     * @param opponentPieceOccupancy    an array for the occupancy of opponent pieces
     * @param opponentTotalOccupancy    a bitboard which marks all occupied squares by the opponent
     * @param totalOccupied             a bitboard which marks all occupied squares
     * @param attackedSquares           a bitboard which marks all squares attacked by the opponent
     * @param taper                     the taper value for interpolation
     * @return
     */
    public double evaluateSide(
            int color,
            PieceList[] ourPieces,
            Tensor1D[] earlyPST,
            Tensor1D[] latePST,
            long[] ourPieceOccupancy,
            long ourTotalOccupancy,
            long[] ourPassedPawnMask,
            long[] opponentPieceOccupancy,
            long opponentTotalOccupancy,
            long totalOccupied,
            long attackedSquares,
            double taper) {


        /**
         * detect which squares are covered by the opponent
         */
        long opponentPawnCover =
                (color == 1 ?
                         BitBoard.shiftSouthWest(opponentPieceOccupancy[0]) | BitBoard.shiftSouthEast(opponentPieceOccupancy[0]) :
                         BitBoard.shiftNorthWest(opponentPieceOccupancy[0]) | BitBoard.shiftNorthEast(opponentPieceOccupancy[0]));
        double eval = 0;




        eval += feature_knights(ourPieces, ourTotalOccupancy, opponentPawnCover, earlyPST, latePST, taper);
        eval += feature_pawns(ourPieces, color, ourPassedPawnMask, ourPieceOccupancy, opponentPieceOccupancy, earlyPST, latePST, taper);
        eval += feature_rooks(ourPieces, ourTotalOccupancy, ourPieceOccupancy, opponentPieceOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper);
        eval += feature_bishops(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper, ourPieceOccupancy, opponentPieceOccupancy);
        eval += feature_queens(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper);
        eval += feature_kings(ourPieces, ourPieceOccupancy, ourTotalOccupancy, opponentTotalOccupancy, attackedSquares, earlyPST, latePST, taper);

        return eval;

    }

    private double feature_knights(PieceList[] ourPieces,
                                   long ourTotalOccupancy,
                                   long opponentPawnCover,
                                   Tensor1D[] pstEarly,
                                   Tensor1D[] pstLate,
                                   double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[2].size(); i++) {
            int index = ourPieces[2].get(i);
            ev += taper(PARAMETER_KNIGHT_TABLE_FACTOR_EARLY, PARAMETER_KNIGHT_TABLE_FACTOR_LATE, taper) *
                  taper(pstEarly[2].get(index), pstLate[2].get(index), taper);;
            ev += taper(PARAMETER_KNIGHT_VALUE_EARLY, PARAMETER_KNIGHT_VALUE_LATE, taper);
            long attacks = BitBoard.KNIGHT_ATTACKS[index] & ~ourTotalOccupancy;
            ev += taper(PARAMETER_KNIGHT_VISIBILITY_EARLY, PARAMETER_KNIGHT_VISIBILITY_LATE, taper)
                  * BitBoard.bitCount(attacks);
            ev += taper(PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY, PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE, taper)
                  * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += taper(PARAMETER_KNIGHT_TRAPPED_EARLY, PARAMETER_KNIGHT_TRAPPED_LATE, taper)
                  * (attacks & opponentPawnCover) == attacks ? 1 : 0;
        }
        evalResults[2] = ev;
        return ev;
    }


    private double feature_pawns(PieceList[] ourPieces,
                                 int color,
                                 long[] ourPassedPawnMask,
                                 long[] ourPieceOccupancy,
                                 long[] opponentPieceOccupancy,
                                 Tensor1D[] pstEarly,
                                 Tensor1D[] pstLate,
                                 double taper) {


        //connected from east and west
        long connectedPawnsEast;
        long connectedPawnsWest;

        if(color == 1){
            connectedPawnsEast = BitBoard.shiftNorthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0];
            connectedPawnsWest = BitBoard.shiftNorthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0];
        }else{
            connectedPawnsEast = BitBoard.shiftSouthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0];
            connectedPawnsWest = BitBoard.shiftSouthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0];
        }

        double ev = 0;
        for (int i = 0; i < ourPieces[0].size(); i++) {
            int index = ourPieces[0].get(i);
            ev += taper(PARAMETER_PAWN_TABLE_FACTOR_EARLY, PARAMETER_PAWN_TABLE_FACTOR_LATE, taper) *
                  taper(pstEarly[0].get(index), pstLate[0].get(index), taper);
            ev += taper(CONST_PARAMETER_PAWN_VALUE_EARLY, CONST_PARAMETER_PAWN_VALUE_LATE, taper);

            boolean passed = (ourPassedPawnMask[index] & opponentPieceOccupancy[0]) == 0;
            boolean connected = ((1L << index) & (connectedPawnsEast | connectedPawnsWest)) != 0;
            boolean isolated = (BitBoard.files_neighbour[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0;
            boolean doubled = ((BitBoard.files[index % 8] & ourPieceOccupancy[0]) - 1) != 0;

            if (passed && connected) {
                ev += taper(PARAMETER_PAWN_CONNECTED_PASSED_EARLY, PARAMETER_PAWN_CONNECTED_PASSED_LATE, taper);
            } else {
                if (passed) {
                    ev += taper(PARAMETER_PAWN_PASSED_EARLY, PARAMETER_PAWN_PASSED_LATE, taper);
                }
                if (connected) {
                    ev += taper(PARAMETER_PAWN_CONNECTED_EARLY, PARAMETER_PAWN_CONNECTED_LATE, taper);
                }
            }

            if (isolated) {
                    ev += taper(PARAMETER_PAWN_ISOLATED_EARLY, PARAMETER_PAWN_ISOLATED_LATE, taper);
               }

            /// didn't work, and is somewhat costly
//            if (doubled && isolated) {
//                ev += taper(PARAMETER_PAWN_DOUBLED_ISOLATED_EARLY,PARAMETER_PAWN_DOUBLED_ISOLATED_LATE, taper);
//            } else {
//                if (doubled) { // note that this counts doubled pawns twice---once for each pawn
//                    ev += taper(PARAMETER_PAWN_DOUBLED_EARLY, PARAMETER_PAWN_DOUBLED_LATE, taper);
//                }
//                if (isolated) {
//                    ev += taper(PARAMETER_PAWN_ISOLATED_EARLY, PARAMETER_PAWN_ISOLATED_LATE, taper);
//                }
//            }
        }



        ev += taper(PARAMETER_PAWN_DOUBLED_EARLY, PARAMETER_PAWN_DOUBLED_LATE, taper) *
              (color == 1 ?
                       BitBoard.bitCount((BitBoard.shiftNorth(ourPieceOccupancy[0]) | BitBoard.shiftNorth(BitBoard.shiftNorth(ourPieceOccupancy[0])) ) & ourPieceOccupancy[0]) :
                       BitBoard.bitCount((BitBoard.shiftNorth(ourPieceOccupancy[0]) | BitBoard.shiftNorth(BitBoard.shiftNorth(ourPieceOccupancy[0])) ) & ourPieceOccupancy[0]));
//        ev += taper(PARAMETER_PAWN_CONNECTED_EARLY, PARAMETER_PAWN_CONNECTED_LATE, taper) *
//                       (BitBoard.bitCount(connectedPawnsEast) +
//                        BitBoard.bitCount(connectedPawnsWest));
        //evalResults[0] = ev;
        return ev;
    }

    private double feature_rooks(PieceList[] ourPieces,
                                 long ourTotalOccupancy,
                                 long[] ourPieceOccupancy,
                                 long[] opponentPieceOccupancy,
                                 long totalOccupied,
                                 long opponentPawnCover,
                                 Tensor1D[] pstEarly,
                                 Tensor1D[] pstLate,
                                 double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[1].size(); i++) {
            int index = ourPieces[1].get(i);
            ev += taper(PARAMETER_ROOK_TABLE_FACTOR_EARLY, PARAMETER_ROOK_TABLE_FACTOR_LATE, taper) *
                        taper(pstEarly[1].get(index), pstLate[1].get(index), taper);
            ev += taper(PARAMETER_ROOK_VALUE_EARLY, PARAMETER_ROOK_VALUE_LATE, taper);
            long attacks = BitBoard.lookUpRookAttack(index, totalOccupied) & ~ourTotalOccupancy;
            ev += taper(PARAMETER_ROOK_VISIBILITY_EARLY, PARAMETER_ROOK_VISIBILITY_LATE, taper)
                  * BitBoard.bitCount(attacks);
            ev += taper(PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY, PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE, taper)
                  * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += taper(PARAMETER_ROOK_TRAPPED_EARLY, PARAMETER_ROOK_TRAPPED_LATE, taper)
                  * (attacks & opponentPawnCover) == attacks ? 1 : 0;
            ev += taper(PARAMETER_ROOK_KING_LINE_EARLY, PARAMETER_ROOK_KING_LINE_LATE, taper)
                  * ((BitBoard.lookUpRookAttack(index, 0L) & opponentPieceOccupancy[5]) > 0 ? 1 : 0);
            if ((BitBoard.files[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0) {     //atleast half open
                if ((BitBoard.files[BitBoard.fileIndex(index)] & opponentPieceOccupancy[0]) == 0) {     //open
                    ev += taper(PARAMETER_ROOK_OPEN_EARLY, PARAMETER_ROOK_OPEN_LATE, taper);
                }
                ev += taper(PARAMETER_ROOK_HALF_OPEN_EARLY, PARAMETER_ROOK_HALF_OPEN_LATE, taper);
            }
        }
        //evalResults[1] = ev;
        return ev;
    }

    private double feature_bishops(PieceList[] ourPieces,
                                   long ourTotalOccupancy,
                                   long totalOccupied,
                                   long opponentPawnCover,
                                   Tensor1D[] pstEarly,
                                   Tensor1D[] pstLate,
                                   double taper,
                                   long[] ourPieceOccupancy,
                                   long[] opponentPieceOccupancy) {
        double ev = 0;
        for (int i = 0; i < ourPieces[3].size(); i++) {
            int index = ourPieces[3].get(i);
            ev += taper(PARAMETER_BISHOP_TABLE_FACTOR_EARLY, PARAMETER_BISHOP_TABLE_FACTOR_LATE, taper) *
                  taper(pstEarly[3].get(index), pstLate[3].get(index), taper);
            ev += taper(PARAMETER_BISHOP_VALUE_EARLY, PARAMETER_BISHOP_VALUE_LATE, taper);
            long attacks = BitBoard.lookUpBishopAttack(index, totalOccupied) & ~ourTotalOccupancy;
            ev += taper(PARAMETER_BISHOP_VISIBILITY_EARLY, PARAMETER_BISHOP_VISIBILITY_LATE, taper)
                  * BitBoard.bitCount(attacks);
            ev += taper(PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY, PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE, taper)
                  * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += taper(PARAMETER_BISHOP_TRAPPED_EARLY, PARAMETER_BISHOP_TRAPPED_LATE, taper)
                  * (attacks & opponentPawnCover) == attacks ? 1 : 0;
            //// VERY POSSIBLY WRONG --Noah
            if (BitBoard.bitCount(BitBoard.center_squares & (ourPieceOccupancy[0] | opponentPieceOccupancy[0])) <= 1)  {
                ev += taper(PARAMETER_BISHOP_OPEN_BONUS_EARLY, PARAMETER_BISHOP_OPEN_BONUS_LATE,taper);
            } else if (BitBoard.bitCount(BitBoard.center_squares & (ourPieceOccupancy[0] | opponentPieceOccupancy[0])) >= 3) {
                ev += taper(PARAMETER_BISHOP_CLOSED_PENALTY_EARLY, PARAMETER_BISHOP_CLOSED_PENALTY_LATE,taper);
            }
        }
        if (ourPieces[3].size() > 1) {
            ev += taper(PARAMETER_BISHOP_DOUBLED_EARLY, PARAMETER_BISHOP_DOUBLED_LATE, taper);
        }
        //evalResults[3] = ev;
        return ev;
    }

    private double feature_queens(PieceList[] ourPieces,
                                  long ourTotalOccupancy,
                                  long totalOccupied,
                                  long opponentPawnCover,
                                  Tensor1D[] pstEarly,
                                  Tensor1D[] pstLate,
                                  double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[4].size(); i++) {
            int index = ourPieces[4].get(i);

            long attacks = (BitBoard.lookUpBishopAttack(index, totalOccupied) |
                            BitBoard.lookUpRookAttack(index, totalOccupied))
                           & ~ourTotalOccupancy;

//            evalResults[4] = taper(PARAMETER_QUEEN_TABLE_FACTOR_EARLY, PARAMETER_QUEEN_TABLE_FACTOR_LATE, taper) *
//                             taper(pstEarly[4].get(index), pstLate[4].get(index), taper);
//            evalResults[5] = taper(PARAMETER_QUEEN_VALUE_EARLY, PARAMETER_QUEEN_VALUE_LATE, taper);
//            evalResults[6] = taper(PARAMETER_QUEEN_VISIBILITY_EARLY, PARAMETER_QUEEN_VISIBILITY_LATE, taper)
//                             * BitBoard.bitCount(attacks);
//            evalResults[7] = taper(PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY, PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE, taper)
//                             * BitBoard.bitCount(attacks & opponentPawnCover);
//            evalResults[8] = taper(PARAMETER_QUEEN_TRAPPED_EARLY, PARAMETER_QUEEN_TRAPPED_LATE, taper)
//                             * (attacks & opponentPawnCover) == attacks ? 1 : 0;
//
//            ev += evalResults[4];
//            ev += evalResults[5];
//            ev += evalResults[6];
//            ev += evalResults[7];
//            ev += evalResults[8];

            ev += taper(PARAMETER_QUEEN_TABLE_FACTOR_EARLY, PARAMETER_QUEEN_TABLE_FACTOR_LATE, taper) *
                             taper(pstEarly[4].get(index), pstLate[4].get(index), taper);
            ev += taper(PARAMETER_QUEEN_VALUE_EARLY, PARAMETER_QUEEN_VALUE_LATE, taper);
            ev += taper(PARAMETER_QUEEN_VISIBILITY_EARLY, PARAMETER_QUEEN_VISIBILITY_LATE, taper)
                             * BitBoard.bitCount(attacks);
            ev += taper(PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY, PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE, taper)
                             * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += taper(PARAMETER_QUEEN_TRAPPED_EARLY, PARAMETER_QUEEN_TRAPPED_LATE, taper)
                             * (attacks & opponentPawnCover) == attacks ? 1 : 0;
        }
        //evalResults[4] = ev;
        return ev;
    }


    private double feature_kings(PieceList[] ourPieces,
                                 long[] ourPieceOccupancy,
                                 long ourTotalOccupancy,
                                 long opponentTotalOccupancy,
                                 long attackedSquares,
                                 Tensor1D[] pstEarly,
                                 Tensor1D[] pstLate,
                                 double taper) {


        boolean opponentKingIsAlone = BitBoard.bitCount(opponentTotalOccupancy) == 1;

        double ev = 0;
        for (int i = 0; i < ourPieces[5].size(); i++) {
            int index = ourPieces[5].get(i);

            if(!opponentKingIsAlone){
                ev += taper(PARAMETER_KING_TABLE_FACTOR_EARLY, PARAMETER_KING_TABLE_FACTOR_LATE, taper) *
                      taper(pstEarly[5].get(index), pstLate[5].get(index), taper);
                ev += taper(PARAMETER_KING_SAFETY_1_EARLY, PARAMETER_KING_SAFETY_1_LATE, taper) *
                      (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy));
                ev += taper(PARAMETER_KING_SAFETY_2_EARLY, PARAMETER_KING_SAFETY_2_LATE, taper) *
                      (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy));
                ev += taper(PARAMETER_KING_PAWN_SHIELD_EARLY, PARAMETER_KING_PAWN_SHIELD_LATE, taper) *
                      (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourPieceOccupancy[0]));


                ev += taper(PARAMETER_KING_SAFETY_3_EARLY, PARAMETER_KING_SAFETY_3_LATE, taper) * BitBoard.bitCount(
                        attackedSquares & BitBoard.KING_ATTACKS[index]);
            }else{
                ev -= BitBoard.chebyshevDistance(index, BitBoard.bitscanForward(opponentTotalOccupancy)) * 20;
            }
            ev += taper(CONST_PARAMETER_KING_VALUE_EARLY, CONST_PARAMETER_KING_VALUE_LATE, taper);




//            evalResults[9] = taper(PARAMETER_KING_TABLE_FACTOR_EARLY, PARAMETER_KING_TABLE_FACTOR_LATE, taper) *
//                             taper(pstEarly[5].get(index), pstLate[5].get(index), taper);
//            evalResults[10] = taper(PARAMETER_KING_SAFETY_1_EARLY, PARAMETER_KING_SAFETY_1_LATE, taper) *
//                            (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy));
//            evalResults[11] = taper(PARAMETER_KING_SAFETY_2_EARLY, PARAMETER_KING_SAFETY_2_LATE, taper) *
//                            (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy));
//            evalResults[12] = taper(PARAMETER_KING_PAWN_SHIELD_EARLY, PARAMETER_KING_PAWN_SHIELD_LATE, taper) *
//                              (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourPieceOccupancy[0]));
//
//            ev += evalResults[9];
//            ev += evalResults[10];
//            ev += evalResults[11];
//            ev += evalResults[12];



        }
        return ev;
    }

    public void printEvaluation(Board board){
        double phase = phaseDecider.getGamePhase(board);
        FastBoard fb = (FastBoard) board;
        double w = evaluateWhite(fb, phase);
        double[] whiteRes = Arrays.copyOf(evalResults, evalResults.length);
        double b = evaluateBlack(fb, phase);
        double[] blackRes = Arrays.copyOf(evalResults, evalResults.length);


        StringBuilder builder = new StringBuilder();


        String format = "%-30s | %-20s | %-20s %n";

        builder.append(String.format(format, "feature", "white", "black"));
        builder.append("───────────────────────────────┼──────────────────────┼────────────────────────\n");

        for(int i = 0; i < evalNames.length; i++){

            builder.append(String.format(format, evalNames[i], whiteRes[i], blackRes[i]));
        }
        builder.append("───────────────────────────────┼──────────────────────┼────────────────────────\n");
        builder.append(String.format(format, "total", w, b));
        builder.append("eval=" + (w-b));

        System.out.println(builder.toString());
    }



    @Override
    public double[] getEvolvableValues(){
        return new double[]{
                PARAMETER_PAWN_TABLE_FACTOR_EARLY,
                PARAMETER_PAWN_CONNECTED_EARLY,
                PARAMETER_PAWN_PASSED_EARLY,
                PARAMETER_PAWN_ISOLATED_EARLY,
                PARAMETER_PAWN_DOUBLED_EARLY,
                PARAMETER_PAWN_CONNECTED_PASSED_EARLY,
                PARAMETER_PAWN_DOUBLED_ISOLATED_EARLY,
                PARAMETER_KNIGHT_TABLE_FACTOR_EARLY,
                PARAMETER_KNIGHT_VALUE_EARLY,
                PARAMETER_KNIGHT_VISIBILITY_EARLY,
                PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_KNIGHT_TRAPPED_EARLY,
                PARAMETER_ROOK_TABLE_FACTOR_EARLY,
                PARAMETER_ROOK_VALUE_EARLY,
                PARAMETER_ROOK_VISIBILITY_EARLY,
                PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_ROOK_TRAPPED_EARLY,
                PARAMETER_ROOK_KING_LINE_EARLY,
                PARAMETER_ROOK_HALF_OPEN_EARLY,
                PARAMETER_ROOK_OPEN_EARLY,
                PARAMETER_BISHOP_TABLE_FACTOR_EARLY,
                PARAMETER_BISHOP_VALUE_EARLY,
                PARAMETER_BISHOP_VISIBILITY_EARLY,
                PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_BISHOP_TRAPPED_EARLY,
                PARAMETER_BISHOP_DOUBLED_EARLY,
                PARAMETER_BISHOP_CLOSED_PENALTY_EARLY,
                PARAMETER_BISHOP_OPEN_BONUS_EARLY,
                PARAMETER_QUEEN_TABLE_FACTOR_EARLY,
                PARAMETER_QUEEN_VALUE_EARLY,
                PARAMETER_QUEEN_VISIBILITY_EARLY,
                PARAMETER_QUEEN_TRAPPED_EARLY,
                PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_KING_TABLE_FACTOR_EARLY,
                PARAMETER_KING_SAFETY_1_EARLY,
                PARAMETER_KING_SAFETY_2_EARLY,
                PARAMETER_KING_SAFETY_3_EARLY,
                PARAMETER_KING_PAWN_SHIELD_EARLY,
                PARAMETER_PAWN_TABLE_FACTOR_LATE,
                PARAMETER_PAWN_PASSED_LATE,
                PARAMETER_PAWN_ISOLATED_LATE,
                PARAMETER_PAWN_DOUBLED_LATE,
                PARAMETER_PAWN_CONNECTED_LATE,
                PARAMETER_PAWN_CONNECTED_PASSED_LATE,
                PARAMETER_PAWN_DOUBLED_ISOLATED_LATE,
                PARAMETER_ROOK_TABLE_FACTOR_LATE,
                PARAMETER_ROOK_VALUE_LATE,
                PARAMETER_ROOK_VISIBILITY_LATE,
                PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_ROOK_TRAPPED_LATE,
                PARAMETER_ROOK_KING_LINE_LATE,
                PARAMETER_ROOK_HALF_OPEN_LATE,
                PARAMETER_ROOK_OPEN_LATE,
                PARAMETER_KNIGHT_TABLE_FACTOR_LATE,
                PARAMETER_KNIGHT_VALUE_LATE,
                PARAMETER_KNIGHT_VISIBILITY_LATE,
                PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_KNIGHT_TRAPPED_LATE,
                PARAMETER_BISHOP_TABLE_FACTOR_LATE,
                PARAMETER_BISHOP_VALUE_LATE,
                PARAMETER_BISHOP_VISIBILITY_LATE,
                PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_BISHOP_TRAPPED_LATE,
                PARAMETER_BISHOP_DOUBLED_LATE,
                PARAMETER_BISHOP_CLOSED_PENALTY_LATE,
                PARAMETER_BISHOP_OPEN_BONUS_LATE,
                PARAMETER_QUEEN_TABLE_FACTOR_LATE,
                PARAMETER_QUEEN_VALUE_LATE,
                PARAMETER_QUEEN_VISIBILITY_LATE,
                PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_QUEEN_TRAPPED_LATE,
                PARAMETER_KING_TABLE_FACTOR_LATE,
                PARAMETER_KING_SAFETY_1_LATE,
                PARAMETER_KING_SAFETY_2_LATE,
                PARAMETER_KING_SAFETY_3_LATE,
                PARAMETER_KING_PAWN_SHIELD_LATE,
                };}
    @Override
    public void setEvolvableValues(double[] ar){
        PARAMETER_PAWN_TABLE_FACTOR_EARLY = ar[0];
        PARAMETER_PAWN_CONNECTED_EARLY = ar[1];
        PARAMETER_PAWN_PASSED_EARLY = ar[2];
        PARAMETER_PAWN_ISOLATED_EARLY = ar[3];
        PARAMETER_PAWN_DOUBLED_EARLY = ar[4];
        PARAMETER_PAWN_CONNECTED_PASSED_EARLY = ar[5];
        PARAMETER_PAWN_DOUBLED_ISOLATED_EARLY = ar[6];
        PARAMETER_KNIGHT_TABLE_FACTOR_EARLY = ar[7];
        PARAMETER_KNIGHT_VALUE_EARLY = ar[8];
        PARAMETER_KNIGHT_VISIBILITY_EARLY = ar[9];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY = ar[10];
        PARAMETER_KNIGHT_TRAPPED_EARLY = ar[11];
        PARAMETER_ROOK_TABLE_FACTOR_EARLY = ar[12];
        PARAMETER_ROOK_VALUE_EARLY = ar[13];
        PARAMETER_ROOK_VISIBILITY_EARLY = ar[14];
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY = ar[15];
        PARAMETER_ROOK_TRAPPED_EARLY = ar[16];
        PARAMETER_ROOK_KING_LINE_EARLY = ar[17];
        PARAMETER_ROOK_HALF_OPEN_EARLY = ar[18];
        PARAMETER_ROOK_OPEN_EARLY = ar[19];
        PARAMETER_BISHOP_TABLE_FACTOR_EARLY = ar[20];
        PARAMETER_BISHOP_VALUE_EARLY = ar[21];
        PARAMETER_BISHOP_VISIBILITY_EARLY = ar[22];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY = ar[23];
        PARAMETER_BISHOP_TRAPPED_EARLY = ar[24];
        PARAMETER_BISHOP_DOUBLED_EARLY = ar[25];
        PARAMETER_BISHOP_CLOSED_PENALTY_EARLY = ar[26];
        PARAMETER_BISHOP_OPEN_BONUS_EARLY = ar[27];
        PARAMETER_QUEEN_TABLE_FACTOR_EARLY = ar[28];
        PARAMETER_QUEEN_VALUE_EARLY = ar[29];
        PARAMETER_QUEEN_VISIBILITY_EARLY = ar[30];
        PARAMETER_QUEEN_TRAPPED_EARLY = ar[31];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY = ar[32];
        PARAMETER_KING_TABLE_FACTOR_EARLY = ar[33];
        PARAMETER_KING_SAFETY_1_EARLY = ar[34];
        PARAMETER_KING_SAFETY_2_EARLY = ar[35];
        PARAMETER_KING_SAFETY_3_EARLY = ar[36];
        PARAMETER_KING_PAWN_SHIELD_EARLY = ar[37];
        PARAMETER_PAWN_TABLE_FACTOR_LATE = ar[38];
        PARAMETER_PAWN_PASSED_LATE = ar[39];
        PARAMETER_PAWN_ISOLATED_LATE = ar[40];
        PARAMETER_PAWN_DOUBLED_LATE = ar[41];
        PARAMETER_PAWN_CONNECTED_LATE = ar[42];
        PARAMETER_PAWN_CONNECTED_PASSED_LATE = ar[43];
        PARAMETER_PAWN_DOUBLED_ISOLATED_LATE = ar[44];
        PARAMETER_ROOK_TABLE_FACTOR_LATE = ar[45];
        PARAMETER_ROOK_VALUE_LATE = ar[46];
        PARAMETER_ROOK_VISIBILITY_LATE = ar[47];
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE = ar[48];
        PARAMETER_ROOK_TRAPPED_LATE = ar[49];
        PARAMETER_ROOK_KING_LINE_LATE = ar[50];
        PARAMETER_ROOK_HALF_OPEN_LATE = ar[51];
        PARAMETER_ROOK_OPEN_LATE = ar[52];
        PARAMETER_KNIGHT_TABLE_FACTOR_LATE = ar[53];
        PARAMETER_KNIGHT_VALUE_LATE = ar[54];
        PARAMETER_KNIGHT_VISIBILITY_LATE = ar[55];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE = ar[56];
        PARAMETER_KNIGHT_TRAPPED_LATE = ar[57];
        PARAMETER_BISHOP_TABLE_FACTOR_LATE = ar[58];
        PARAMETER_BISHOP_VALUE_LATE = ar[59];
        PARAMETER_BISHOP_VISIBILITY_LATE = ar[60];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE = ar[61];
        PARAMETER_BISHOP_TRAPPED_LATE = ar[62];
        PARAMETER_BISHOP_DOUBLED_LATE = ar[63];
        PARAMETER_BISHOP_CLOSED_PENALTY_LATE = ar[64];
        PARAMETER_BISHOP_OPEN_BONUS_LATE = ar[65];
        PARAMETER_QUEEN_TABLE_FACTOR_LATE = ar[66];
        PARAMETER_QUEEN_VALUE_LATE = ar[67];
        PARAMETER_QUEEN_VISIBILITY_LATE = ar[68];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE = ar[69];
        PARAMETER_QUEEN_TRAPPED_LATE = ar[70];
        PARAMETER_KING_TABLE_FACTOR_LATE = ar[71];
        PARAMETER_KING_SAFETY_1_LATE = ar[72];
        PARAMETER_KING_SAFETY_2_LATE = ar[73];
        PARAMETER_KING_SAFETY_3_LATE = ar[74];
        PARAMETER_KING_PAWN_SHIELD_LATE = ar[75];
    }

    @Override
    public AdvancedEvaluator copy() {
        AdvancedEvaluator evaluator = new AdvancedEvaluator(phaseDecider);
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }

    private int smallestAttackerSquare(long[] pieceOcc, long occupied, int square, int side){


        long occ = occupied;
        long[] pOcc = pieceOcc;
        long squareBB = 1L << square;




        //pawns
        if(side > 0){
            if((BitBoard.shiftNorthWest(pOcc[0]) & squareBB) != 0){
                return square-7;
            }
            if((BitBoard.shiftNorthEast(pOcc[0]) & squareBB) != 0){
                return square-9;
            }
        }else{
            if((BitBoard.shiftSouthWest(pOcc[0]) & squareBB) != 0){
                return square+9;
            }
            if((BitBoard.shiftSouthEast(pOcc[0]) & squareBB) != 0){
                return square+7;
            }
        }


        long nA = BitBoard.KNIGHT_ATTACKS[square];
        long kA = BitBoard.KING_ATTACKS[square];
        long rA = BitBoard.lookUpRookAttack(square, occ);
        long bA = BitBoard.lookUpBishopAttack(square, occ);

        if((nA & pOcc[2]) != 0){
            return BitBoard.bitscanForward(nA & pOcc[2]);
        }

        if((bA & pOcc[3]) != 0){
            return BitBoard.bitscanForward(bA & pOcc[3]);
        }

        if((rA & pOcc[1]) != 0){
            return BitBoard.bitscanForward(rA & pOcc[1]);
        }

        if(((rA | bA) & pOcc[4]) != 0){
            return BitBoard.bitscanForward((rA | bA)  & pOcc[4]);
        }

        if((kA & pOcc[5]) != 0){
            return BitBoard.bitscanForward(kA & pOcc[5]);
        }



        return -1;
    }

    public double staticExchangeEvaluation(Board board, int toSqare, int target, int fromSquare, int attacker, int color){


        long[] whiteOcc = Arrays.copyOf(((FastBoard)board).getWhite_values(), 6);
        long[] blackOcc = Arrays.copyOf(((FastBoard)board).getBlack_values(), 6);


        int gain[] = new int[32];
        int d = 0;
        gain[d] = (int) pieceVals[Math.abs(target)];


        long fromSet = 1L << fromSquare;
        long occ = ((FastBoard) board).getOccupied();


        do{
            d++;
            gain[d] = (int) (pieceVals[Math.abs(attacker)] - gain[d-1]);
            if(Math.max(-gain[d-1], gain[d]) < 0){
                break;
            }

            if(color == 1){
                whiteOcc[attacker-1] ^= (fromSet);
            }else{
                blackOcc[-attacker-1] ^= fromSet;
            }
            occ     ^= fromSet;
            color = -color;

            fromSquare = smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, toSqare, color);

            if(fromSquare == 64){
                smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, toSqare, color);
                throw new RuntimeException();
            }

            if(fromSquare == -1 || fromSquare == 64) break;
            attacker = board.getPiece(fromSquare);
            fromSet = 1L << fromSquare;
        }while (fromSet != 0);

        while (--d > 0)
            gain[d-1]= -Math.max(-gain[d-1], gain[d]);
        return gain[0];
    }

    public double staticExchangeEvaluation(Board board, int sq, int color){
        double val = 0;

        int nextCapturedPiece = Math.abs(board.getPiece(sq));


        long[] whiteOcc = Arrays.copyOf(((FastBoard)board).getWhite_values(), 6);
        long[] blackOcc = Arrays.copyOf(((FastBoard)board).getBlack_values(), 6);

        long occ = ((FastBoard) board).getOccupied();
        int minAttackerSquare = smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, sq, color);


        int gain[] = new int[32];
        int d = 0;

        while(minAttackerSquare != -1){

            int attackerPiece = board.getPiece(minAttackerSquare);

            occ ^= (1L << minAttackerSquare);
            if(color == 1){
                whiteOcc[attackerPiece-1] ^= (1L << minAttackerSquare);
                val += pieceVals[nextCapturedPiece];
                nextCapturedPiece = attackerPiece;
            }else{
                blackOcc[-attackerPiece-1] ^= (1L << minAttackerSquare);
                val -= pieceVals[nextCapturedPiece];
                nextCapturedPiece = -attackerPiece;
            }
            color = -color;

            minAttackerSquare = smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, sq, color);
        }


        return val;




//        if(minAttackerSquare == -1) return val;
//
//        int attackedPiece = board.getPiece(sq);
//        int attackerPiece = board.getPiece(minAttackerSquare);
//
//
//
//
//
//
//        /* skip if the square isn't attacked anymore by this side */
//        if ( minAttackerSquare != -1)// && board.getPiece(minAttackerSquare) * color > 0)
//        {
//            board.setPiece(0, minAttackerSquare);
//            board.setPiece(attackerPiece, sq);
//
//            /* Do not consider captures if they lose material, therefor max zero */
//            //val = Math.max(0, pieceVals[Math.abs(attackedPiece)] - staticExchangeEvaluation(board, sq, -color));
//
//
//            val = pieceVals[Math.abs(attackedPiece)] - staticExchangeEvaluation(board, sq, -color);
//
//
//            board.setPiece(attackerPiece, minAttackerSquare);
//            board.setPiece(attackedPiece, sq);
//        }
//
//        return val;
    }



    public static void main(String[] args) {
//        double[] ar = new double[]{
//                189.0, 252.0, 86.0, 252.0, -161.0, 309.0, 100,
//                855.0, 647.0, 630.0, 1038.0, 2.0, 18.0, 29.0, 9.0, -30.0,
//                -21.0, -29.0, -34.0, -19.0, -22.0, -18.0, -21.0, 1.0, 32.0, -55.0, 37.0, 140.0, 52.0, -183.0, 49.0,
//                141.0, 30.0, 406.0, 147.0, 107.0, 70.0, -161.0, -80.0, 855.0, 546.0, 655.0, 1038.0, 45.0, 15.0, 28.0,
//                99.0, -3.0, 4.0, -5.0, 4.0, -14.0, -20.0, -19.0, -21.0, 105.0, 241.0, -4.0, 3.0, 79.0, 7.0, 123.0, 3.0,
//                -82.0, 67.0
//        };

//
//        double[] ar = new AdvancedEvaluator(null).getEvolvableValues();
//
//        System.out.println(Arrays.toString(ar));
//
//        for(int i = 0; i < ar.length; i++){
//            ar[i] = Math.round(ar[i]);
//        }
//
//        Evaluator.createParameters(AdvancedEvaluator.class, ar);


//        long t = System.currentTimeMillis();
//        for(int i = 0; i < 1E7; i++){
//            getBinomial(10,0.1);
//        }
//        System.out.println(System.currentTimeMillis()-t);



        FastBoard fb = IO.read_FEN(new FastBoard(), "8/4kR2/8/3K4/8/8/8/8 b - - 5 3");

        UCI.getAi().setLimit_flag(2);
        UCI.getAi().setLimit(20);
        UCI.getAi().setUse_qSearch(true);
        UCI.getAi().setUse_null_moves(false);
        UCI.getAi().setDebug(true);
        System.out.println(fb);
        UCI.getAi().bestMove(fb);


    }
}
