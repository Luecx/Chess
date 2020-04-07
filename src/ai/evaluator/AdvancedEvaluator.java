package ai.evaluator;

import ai.evaluator.decider.BoardPhaseDecider;
import ai.evaluator.decider.SimpleDecider;
import ai.tools.tensor.Tensor1D;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.moves.Move;
import board.pieces.PieceList;
import io.IO;

import java.util.Arrays;

public class AdvancedEvaluator implements Evaluator<AdvancedEvaluator> {

    private BoardPhaseDecider phaseDecider;


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
            -20, -10, -10, -10, -10, -10, -10, -20,
            })).scale(0.01);

    public static final Tensor1D ROOK_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 20, 20, 20, 20, 20, 20, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0
    })).scale(0.01);

    public static final Tensor1D KNIGHT_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -16, -12, -12, -12, -12, -16, -20,
            -8, -4, 0, 0, 0, 0, -4, -8,
            -12, 4, 8, 12, 12, 12, 4, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -6, 10, 8, 6, 6, 8, 2, -6,
            -16, -8, 0, 2, 2, 0, -8, -16,
            -24, -50, -12, -12, -12, -12, -50, -24,
            })).scale(0.01);
    public static final Tensor1D QUEEN_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
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
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0})).scale(0.01);

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
            0, 0, 0, 5, 5, 0, 0, 0
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

    public static final Tensor1D KING_VALUES_BLACK_LATE = flipTensor(KING_VALUES_MID_WHITE);
    public static final Tensor1D QUEEN_VALUES_BLACK_LATE = flipTensor(QUEEN_VALUES_WHITE);
    public static final Tensor1D ROOK_VALUES_BLACK_LATE = flipTensor(ROOK_VALUES_WHITE);
    public static final Tensor1D BISHOP_VALUES_BLACK_LATE = flipTensor(BISHOP_VALUES_WHITE);
    public static final Tensor1D KNIGHT_VALUES_BLACK_LATE = flipTensor(KNIGHT_VALUES_WHITE);
    public static final Tensor1D PAWN_VALUES_BLACK_LATE = flipTensor(PAWN_VALUES_WHITE);

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


    private double CONST_PARAMETER_KING_VALUE_EARLY =                                8751;
    private double CONST_PARAMETER_PAWN_VALUE_LATE =                                 100;
    private double CONST_PARAMETER_KING_VALUE_LATE =                                 8751;
    private double CONST_PARAMETER_PAWN_VALUE_EARLY =                                100;

    private double PARAMETER_PAWN_TABLE_FACTOR_EARLY =                               44;
    private double PARAMETER_ROOK_TABLE_FACTOR_EARLY =                               44;
    private double PARAMETER_KNIGHT_TABLE_FACTOR_EARLY =                             44;
    private double PARAMETER_BISHOP_TABLE_FACTOR_EARLY =                             44;
    private double PARAMETER_QUEEN_TABLE_FACTOR_EARLY =                              43;
    private double PARAMETER_KING_TABLE_FACTOR_EARLY =                               44;
    private double PARAMETER_ROOK_VALUE_EARLY =                                      424;
    private double PARAMETER_KNIGHT_VALUE_EARLY =                                    293;
    private double PARAMETER_BISHOP_VALUE_EARLY =                                    301;
    private double PARAMETER_QUEEN_VALUE_EARLY =                                     598;
    private double PARAMETER_ROOK_VISIBILITY_EARLY =                                 9;
    private double PARAMETER_BISHOP_VISIBILITY_EARLY =                               6;
    private double PARAMETER_KNIGHT_VISIBILITY_EARLY =                               8;
    private double PARAMETER_QUEEN_VISIBILITY_EARLY =                                17;
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY =                      -6;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY =                    0;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY =                    -4;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY =                     -21;
    private double PARAMETER_ROOK_TRAPPED_EARLY =                                    -19;
    private double PARAMETER_BISHOP_TRAPPED_EARLY =                                  -21;
    private double PARAMETER_KNIGHT_TRAPPED_EARLY =                                  -18;
    private double PARAMETER_QUEEN_TRAPPED_EARLY =                                   -20;
    private double PARAMETER_ROOK_KING_LINE_EARLY =                                  24;
    private double PARAMETER_PASSED_PAWN_EARLY =                                     38;
    private double PARAMETER_ISOLATED_PAWN_EARLY =                                   -7;
    private double PARAMETER_DOUBLED_PAWN_EARLY =                                    -29;
    private double PARAMETER_DOUBLE_BISHOP_EARLY =                                   52;
    private double PARAMETER_KING_SAFETY_1_EARLY =                                   10;
    private double PARAMETER_KING_SAFETY_2_EARLY =                                   25;
    private double PARAMETER_ROOK_HALF_OPEN_EARLY =                                  21;
    private double PARAMETER_ROOK_OPEN_EARLY =                                       2;
    private double PARAMETER_CONNECTED_PAWN_EARLY =                                  5;

    private double PARAMETER_PAWN_TABLE_FACTOR_LATE =                                44;
    private double PARAMETER_ROOK_TABLE_FACTOR_LATE =                                44;
    private double PARAMETER_KNIGHT_TABLE_FACTOR_LATE =                              44;
    private double PARAMETER_BISHOP_TABLE_FACTOR_LATE =                              44;
    private double PARAMETER_QUEEN_TABLE_FACTOR_LATE =                               43;
    private double PARAMETER_KING_TABLE_FACTOR_LATE =                                44;
    private double PARAMETER_ROOK_VALUE_LATE =                                       424;
    private double PARAMETER_KNIGHT_VALUE_LATE =                                     293;
    private double PARAMETER_BISHOP_VALUE_LATE =                                     301;
    private double PARAMETER_QUEEN_VALUE_LATE =                                      598;
    private double PARAMETER_ROOK_VISIBILITY_LATE =                                  9;
    private double PARAMETER_BISHOP_VISIBILITY_LATE =                                6;
    private double PARAMETER_KNIGHT_VISIBILITY_LATE =                                8;
    private double PARAMETER_QUEEN_VISIBILITY_LATE =                                 17;
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE =                       -6;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE =                     0;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE =                     -4;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE =                      -21;
    private double PARAMETER_ROOK_TRAPPED_LATE =                                     -19;
    private double PARAMETER_BISHOP_TRAPPED_LATE =                                   -21;
    private double PARAMETER_KNIGHT_TRAPPED_LATE =                                   -18;
    private double PARAMETER_QUEEN_TRAPPED_LATE =                                    -20;
    private double PARAMETER_ROOK_KING_LINE_LATE =                                   24;
    private double PARAMETER_PASSED_PAWN_LATE =                                      38;
    private double PARAMETER_ISOLATED_PAWN_LATE =                                    -7;
    private double PARAMETER_DOUBLED_PAWN_LATE =                                     -29;
    private double PARAMETER_DOUBLE_BISHOP_LATE =                                    52;
    private double PARAMETER_KING_SAFETY_1_LATE =                                    10;
    private double PARAMETER_KING_SAFETY_2_LATE =                                    25;
    private double PARAMETER_ROOK_HALF_OPEN_LATE =                                   21;
    private double PARAMETER_ROOK_OPEN_LATE =                                        2;
    private double PARAMETER_CONNECTED_PAWN_LATE =                                   5;



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


        return evalateSide(1,
                           fb.getWhite_pieces(),
                           WHITE_PST_EARLY,
                           WHITE_PST_LATE,
                           fb.getWhite_values(),
                           fb.getTeam_total()[0],
                           BitBoard.whitePassedPawnMask,
                           fb.getBlack_values(),
                           fb.getTeam_total()[1],
                           fb.getOccupied(),
                           phase)

               - evalateSide(-1,
                             fb.getBlack_pieces(),
                             BLACK_PST_EARLY,
                             BLACK_PST_LATE,
                             fb.getBlack_values(),
                             fb.getTeam_total()[1],
                             BitBoard.blackPassedPawnMask,
                             fb.getWhite_values(),
                             fb.getTeam_total()[0],
                             fb.getOccupied(),
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
     * @param taper                     the taper value for interpolation
     * @return
     */
    public double evalateSide(
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
        eval += feature_bishops(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper);
        eval += feature_queens(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper);
        eval += feature_kings(ourPieces, ourTotalOccupancy, opponentTotalOccupancy, earlyPST, latePST, taper);

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
        double ev = 0;
        for (int i = 0; i < ourPieces[0].size(); i++) {
            int index = ourPieces[0].get(i);
            ev += taper(PARAMETER_PAWN_TABLE_FACTOR_EARLY, PARAMETER_PAWN_TABLE_FACTOR_LATE, taper) *
                  taper(pstEarly[0].get(index), pstLate[0].get(index), taper);
            ev += taper(CONST_PARAMETER_PAWN_VALUE_EARLY, CONST_PARAMETER_PAWN_VALUE_LATE, taper);
            if ((ourPassedPawnMask[index] & opponentPieceOccupancy[0]) == 0) {
                ev += taper(PARAMETER_PASSED_PAWN_EARLY, PARAMETER_PASSED_PAWN_LATE, taper);
            }
            if ((BitBoard.files_neighbour[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0) {
                ev += taper(PARAMETER_ISOLATED_PAWN_EARLY, PARAMETER_ISOLATED_PAWN_LATE, taper);
            }
        }
        ev += taper(PARAMETER_DOUBLED_PAWN_EARLY, PARAMETER_DOUBLED_PAWN_LATE, taper) *
              (color == 1 ?
                       BitBoard.bitCount(BitBoard.shiftNorth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]) :
                       BitBoard.bitCount(BitBoard.shiftSouth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]));
        ev += taper(PARAMETER_CONNECTED_PAWN_EARLY, PARAMETER_CONNECTED_PAWN_LATE, taper) *
              (color == 1 ?
                       (BitBoard.bitCount(BitBoard.shiftNorthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0]) +
                        BitBoard.bitCount(BitBoard.shiftNorthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0])) :
                       (BitBoard.bitCount(BitBoard.shiftSouthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0]) +
                        BitBoard.bitCount(BitBoard.shiftSouthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0])));
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
        return ev;
    }

    private double feature_bishops(PieceList[] ourPieces,
                                   long ourTotalOccupancy,
                                   long totalOccupied,
                                   long opponentPawnCover,
                                   Tensor1D[] pstEarly,
                                   Tensor1D[] pstLate,
                                   double taper) {
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
        }
        if (ourPieces[3].size() > 1) {
            ev += taper(PARAMETER_DOUBLE_BISHOP_EARLY, PARAMETER_DOUBLE_BISHOP_LATE, taper);
        }
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
            ev += taper(PARAMETER_QUEEN_TABLE_FACTOR_EARLY, PARAMETER_QUEEN_TABLE_FACTOR_LATE, taper) *
                  taper(pstEarly[4].get(index), pstLate[4].get(index), taper);
            ev += taper(PARAMETER_QUEEN_VALUE_EARLY, PARAMETER_QUEEN_VALUE_LATE, taper);
            long attacks = (BitBoard.lookUpBishopAttack(index, totalOccupied) |
                            BitBoard.lookUpRookAttack(index, totalOccupied))
                           & ~ourTotalOccupancy;
            ev += taper(PARAMETER_QUEEN_VISIBILITY_EARLY, PARAMETER_QUEEN_VISIBILITY_LATE, taper)
                  * BitBoard.bitCount(attacks);
            ev += taper(PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY, PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE, taper)
                  * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += taper(PARAMETER_QUEEN_TRAPPED_EARLY, PARAMETER_QUEEN_TRAPPED_LATE, taper)
                  * (attacks & opponentPawnCover) == attacks ? 1 : 0;
        }
        return ev;
    }


    private double feature_kings(PieceList[] ourPieces,
                                 long ourTotalOccupancy,
                                 long opponentTotalOccupancy,
                                 Tensor1D[] pstEarly,
                                 Tensor1D[] pstLate,
                                 double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[5].size(); i++) {
            int index = ourPieces[5].get(i);
            //TODO
            ev += taper(PARAMETER_KING_TABLE_FACTOR_EARLY, PARAMETER_KING_TABLE_FACTOR_LATE, taper) *
                  taper(pstEarly[5].get(index), pstLate[5].get(index), taper);;
            ev += taper(CONST_PARAMETER_KING_VALUE_EARLY, CONST_PARAMETER_KING_VALUE_LATE, taper);
            ev += taper(PARAMETER_KING_SAFETY_1_EARLY, PARAMETER_KING_SAFETY_1_LATE, taper) *
                  (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy));
            ev += taper(PARAMETER_KING_SAFETY_2_EARLY, PARAMETER_KING_SAFETY_2_LATE, taper) *
                  (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy));
        }
        return ev;
    }

//    public void printEvaluation(Board board){
//        double phase = phaseDecider.getGamePhase(board);
//        FastBoard fb = (FastBoard) board;
//        double[] white = evaluateSideTrack(1,
//                           fb.getWhite_pieces(),
//                           new Tensor1D[]{PAWN_VALUES_WHITE, ROOK_VALUES_WHITE, KNIGHT_VALUES_WHITE, BISHOP_VALUES_WHITE, QUEEN_VALUES_WHITE, KING_VALUES_MID_WHITE},
//                           fb.getWhite_values(),
//                           fb.getTeam_total()[0],
//                           BitBoard.whitePassedPawnMask,
//                           fb.getBlack_values(),
//                           fb.getTeam_total()[1],
//                           fb.getOccupied(),
//                           phase);
//        double[] black = evaluateSideTrack(-1,
//                             fb.getBlack_pieces(),
//                             new Tensor1D[]{PAWN_VALUES_BLACK, ROOK_VALUES_BLACK, KNIGHT_VALUES_BLACK, BISHOP_VALUES_BLACK, QUEEN_VALUES_BLACK, KING_VALUES_MID_BLACK},
//                             fb.getBlack_values(),
//                             fb.getTeam_total()[1],
//                             BitBoard.blackPassedPawnMask,
//                             fb.getWhite_values(),
//                             fb.getTeam_total()[0],
//                             fb.getOccupied(),
//                             phase);
//
//
//    }

//    public double[] evaluateSideTrack(
//            int color,
//            PieceList[] ourPieces,
//            Tensor1D[] pst,
//            long[] ourPieceOccupancy,
//            long ourTotalOccupancy,
//            long[] ourPassedPawnMask,
//            long[] opponentPieceOccupancy,
//            long opponentTotalOccupancy,
//            long totalOccupied,
//            double taper) {
//        long opponentPawnCover =
//                (color == 1 ?
//                         BitBoard.shiftSouthWest(opponentPieceOccupancy[0]) | BitBoard.shiftSouthEast(opponentPieceOccupancy[0]) :
//                         BitBoard.shiftNorthWest(opponentPieceOccupancy[0]) | BitBoard.shiftNorthEast(opponentPieceOccupancy[0]));
//        double eval[] = new double[6];
//        eval[0] += feature_pawns(ourPieces, color, ourPassedPawnMask, ourPieceOccupancy, opponentPieceOccupancy, pst, taper);
//        eval[1] += feature_knights(ourPieces, ourTotalOccupancy, opponentPawnCover, pst, taper);
//        eval[2] += feature_rooks(ourPieces, ourTotalOccupancy, ourPieceOccupancy, opponentPieceOccupancy, totalOccupied, opponentPawnCover, pst, taper);
//        eval[3] += feature_bishops(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, pst, taper);
//        eval[4] += feature_queens(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, pst, taper);
//        eval[5] += feature_kings(ourPieces, ourTotalOccupancy, opponentTotalOccupancy, pst, taper);
//        return eval;
//    }


    public double[] getEvolvableValues() {

        //return new double[0];

        return new double[]{
                PARAMETER_PAWN_TABLE_FACTOR_EARLY,
                PARAMETER_ROOK_TABLE_FACTOR_EARLY,
                PARAMETER_KNIGHT_TABLE_FACTOR_EARLY,
                PARAMETER_BISHOP_TABLE_FACTOR_EARLY,
                PARAMETER_QUEEN_TABLE_FACTOR_EARLY,
                PARAMETER_KING_TABLE_FACTOR_EARLY,

                PARAMETER_ROOK_VALUE_EARLY,
                PARAMETER_KNIGHT_VALUE_EARLY,
                PARAMETER_BISHOP_VALUE_EARLY,
                PARAMETER_QUEEN_VALUE_EARLY,

                PARAMETER_ROOK_VISIBILITY_EARLY,
                PARAMETER_BISHOP_VISIBILITY_EARLY,
                PARAMETER_KNIGHT_VISIBILITY_EARLY,
                PARAMETER_QUEEN_VISIBILITY_EARLY,
                PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY,
                PARAMETER_ROOK_TRAPPED_EARLY,
                PARAMETER_BISHOP_TRAPPED_EARLY,
                PARAMETER_KNIGHT_TRAPPED_EARLY,
                PARAMETER_QUEEN_TRAPPED_EARLY,
                PARAMETER_ROOK_KING_LINE_EARLY,
                PARAMETER_PASSED_PAWN_EARLY,
                PARAMETER_ISOLATED_PAWN_EARLY,
                PARAMETER_DOUBLED_PAWN_EARLY,
                PARAMETER_DOUBLE_BISHOP_EARLY,
                PARAMETER_KING_SAFETY_1_EARLY,
                PARAMETER_KING_SAFETY_2_EARLY,
                PARAMETER_ROOK_HALF_OPEN_EARLY,
                PARAMETER_ROOK_OPEN_EARLY,
                PARAMETER_CONNECTED_PAWN_EARLY,
                PARAMETER_PAWN_TABLE_FACTOR_LATE,
                PARAMETER_ROOK_TABLE_FACTOR_LATE,
                PARAMETER_KNIGHT_TABLE_FACTOR_LATE,
                PARAMETER_BISHOP_TABLE_FACTOR_LATE,
                PARAMETER_QUEEN_TABLE_FACTOR_LATE,
                PARAMETER_KING_TABLE_FACTOR_LATE,

                PARAMETER_ROOK_VALUE_LATE,
                PARAMETER_KNIGHT_VALUE_LATE,
                PARAMETER_BISHOP_VALUE_LATE,
                PARAMETER_QUEEN_VALUE_LATE,

                PARAMETER_ROOK_VISIBILITY_LATE,
                PARAMETER_BISHOP_VISIBILITY_LATE,
                PARAMETER_KNIGHT_VISIBILITY_LATE,
                PARAMETER_QUEEN_VISIBILITY_LATE,
                PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE,
                PARAMETER_ROOK_TRAPPED_LATE,
                PARAMETER_BISHOP_TRAPPED_LATE,
                PARAMETER_KNIGHT_TRAPPED_LATE,
                PARAMETER_QUEEN_TRAPPED_LATE,
                PARAMETER_ROOK_KING_LINE_LATE,
                PARAMETER_PASSED_PAWN_LATE,
                PARAMETER_ISOLATED_PAWN_LATE,
                PARAMETER_DOUBLED_PAWN_LATE,
                PARAMETER_DOUBLE_BISHOP_LATE,
                PARAMETER_KING_SAFETY_1_LATE,
                PARAMETER_KING_SAFETY_2_LATE,
                PARAMETER_ROOK_HALF_OPEN_LATE,
                PARAMETER_ROOK_OPEN_LATE,
                PARAMETER_CONNECTED_PAWN_LATE,
                };
    }

    public void setEvolvableValues(double[] ar) {

        int i = 0;

        PARAMETER_PAWN_TABLE_FACTOR_EARLY = ar[i++];
        PARAMETER_ROOK_TABLE_FACTOR_EARLY = ar[i++];
        PARAMETER_KNIGHT_TABLE_FACTOR_EARLY = ar[i++];
        PARAMETER_BISHOP_TABLE_FACTOR_EARLY = ar[i++];
        PARAMETER_QUEEN_TABLE_FACTOR_EARLY = ar[i++];
        PARAMETER_KING_TABLE_FACTOR_EARLY = ar[i++];

        PARAMETER_ROOK_VALUE_EARLY = ar[i++];
        PARAMETER_KNIGHT_VALUE_EARLY = ar[i++];
        PARAMETER_BISHOP_VALUE_EARLY = ar[i++];
        PARAMETER_QUEEN_VALUE_EARLY = ar[i++];

        PARAMETER_ROOK_VISIBILITY_EARLY = ar[i++];
        PARAMETER_BISHOP_VISIBILITY_EARLY = ar[i++];
        PARAMETER_KNIGHT_VISIBILITY_EARLY = ar[i++];
        PARAMETER_QUEEN_VISIBILITY_EARLY = ar[i++];
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY = ar[i++];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY = ar[i++];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY = ar[i++];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY = ar[i++];
        PARAMETER_ROOK_TRAPPED_EARLY = ar[i++];
        PARAMETER_BISHOP_TRAPPED_EARLY = ar[i++];
        PARAMETER_KNIGHT_TRAPPED_EARLY = ar[i++];
        PARAMETER_QUEEN_TRAPPED_EARLY = ar[i++];
        PARAMETER_ROOK_KING_LINE_EARLY = ar[i++];
        PARAMETER_PASSED_PAWN_EARLY = ar[i++];
        PARAMETER_ISOLATED_PAWN_EARLY = ar[i++];
        PARAMETER_DOUBLED_PAWN_EARLY = ar[i++];
        PARAMETER_DOUBLE_BISHOP_EARLY = ar[i++];
        PARAMETER_KING_SAFETY_1_EARLY = ar[i++];
        PARAMETER_KING_SAFETY_2_EARLY = ar[i++];
        PARAMETER_ROOK_HALF_OPEN_EARLY = ar[i++];
        PARAMETER_ROOK_OPEN_EARLY = ar[i++];
        PARAMETER_CONNECTED_PAWN_EARLY = ar[i++];

        PARAMETER_PAWN_TABLE_FACTOR_LATE = ar[i++];
        PARAMETER_ROOK_TABLE_FACTOR_LATE = ar[i++];
        PARAMETER_KNIGHT_TABLE_FACTOR_LATE = ar[i++];
        PARAMETER_BISHOP_TABLE_FACTOR_LATE = ar[i++];
        PARAMETER_QUEEN_TABLE_FACTOR_LATE = ar[i++];
        PARAMETER_KING_TABLE_FACTOR_LATE = ar[i++];

        PARAMETER_ROOK_VALUE_LATE = ar[i++];
        PARAMETER_KNIGHT_VALUE_LATE = ar[i++];
        PARAMETER_BISHOP_VALUE_LATE = ar[i++];
        PARAMETER_QUEEN_VALUE_LATE = ar[i++];

        PARAMETER_ROOK_VISIBILITY_LATE = ar[i++];
        PARAMETER_BISHOP_VISIBILITY_LATE = ar[i++];
        PARAMETER_KNIGHT_VISIBILITY_LATE = ar[i++];
        PARAMETER_QUEEN_VISIBILITY_LATE = ar[i++];
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE = ar[i++];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE = ar[i++];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE = ar[i++];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE = ar[i++];
        PARAMETER_ROOK_TRAPPED_LATE = ar[i++];
        PARAMETER_BISHOP_TRAPPED_LATE = ar[i++];
        PARAMETER_KNIGHT_TRAPPED_LATE = ar[i++];
        PARAMETER_QUEEN_TRAPPED_LATE = ar[i++];
        PARAMETER_ROOK_KING_LINE_LATE = ar[i++];
        PARAMETER_PASSED_PAWN_LATE = ar[i++];
        PARAMETER_ISOLATED_PAWN_LATE = ar[i++];
        PARAMETER_DOUBLED_PAWN_LATE = ar[i++];
        PARAMETER_DOUBLE_BISHOP_LATE = ar[i++];
        PARAMETER_KING_SAFETY_1_LATE = ar[i++];
        PARAMETER_KING_SAFETY_2_LATE = ar[i++];
        PARAMETER_ROOK_HALF_OPEN_LATE = ar[i++];
        PARAMETER_ROOK_OPEN_LATE = ar[i++];
        PARAMETER_CONNECTED_PAWN_LATE = ar[i++];
    }

    @Override
    public AdvancedEvaluator copy() {
        AdvancedEvaluator evaluator = new AdvancedEvaluator(phaseDecider);
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }

    private int smallestAttackerSquare(Board b, int square, int side){

        FastBoard fb = (FastBoard) b;

        PieceList[] pieces = side > 0 ? ((FastBoard)b).getWhite_pieces() : ((FastBoard)b).getBlack_pieces();
        int index;
        long squareBB = 1L << square;


        //pawns
        if(side > 0){
            if((BitBoard.shiftNorthWest(fb.getWhite_values()[0]) & squareBB) != 0){
                return square-7;
            }
            if((BitBoard.shiftNorthEast(fb.getWhite_values()[0]) & squareBB) != 0){
                return square-9;
            }
        }else{
            if((BitBoard.shiftSouthWest(fb.getBlack_values()[0]) & squareBB) != 0){
                return square+9;
            }
            if((BitBoard.shiftSouthEast(fb.getBlack_values()[0]) & squareBB) != 0){
                return square+7;
            }
        }


        //knights
        for (int i = 0; i < pieces[2].size(); i++) {
            index = pieces[2].get(i);
            if((BitBoard.KNIGHT_ATTACKS[index] & squareBB) != 0){
                return index;
            }
        }

        //bishops
        for (int i = 0; i < pieces[3].size(); i++){
            index = pieces[3].get(i);
            if((BitBoard.lookUpBishopAttack(index, fb.getOccupied()) & squareBB) != 0){
                return index;
            }
        }

        //rooks
        for (int i = 0; i < pieces[1].size(); i++){
            index = pieces[1].get(i);
            if((BitBoard.lookUpRookAttack(index, fb.getOccupied()) & squareBB) != 0){
                return index;
            }
        }

        //queen
        for (int i = 0; i < pieces[4].size(); i++) {
            index = pieces[4].get(i);
            if ((
                        (BitBoard.lookUpRookAttack(index, fb.getOccupied()) |
                         BitBoard.lookUpBishopAttack(index, fb.getOccupied()))
                        & squareBB) != 0) {
                return index;
            }
        }

        //kings
        for (int i = 0; i < pieces[5].size(); i++) {
            index = pieces[5].get(i);
            if((BitBoard.KING_ATTACKS[index] & squareBB) != 0){
                return index;
            }
        }

        return -1;
    }


    double[] pieceVals = new double[]{0, CONST_PARAMETER_PAWN_VALUE_EARLY,
                                      PARAMETER_ROOK_VALUE_EARLY,
                                      PARAMETER_KNIGHT_VALUE_EARLY,
                                      PARAMETER_BISHOP_VALUE_EARLY,
                                      PARAMETER_QUEEN_VALUE_EARLY,
                                      CONST_PARAMETER_KING_VALUE_EARLY};


    public double staticExchangeEvaluation(Board board, int sq, int color){
        double val = 0;

        int minAttackerSquare = smallestAttackerSquare(board, sq, color);

        if(minAttackerSquare == -1) return val;

        int attackedPiece = board.getPiece(sq);
        int attackerPiece = board.getPiece(minAttackerSquare);


        /* skip if the square isn't attacked anymore by this side */
        if ( minAttackerSquare != -1)// && board.getPiece(minAttackerSquare) * color > 0)
        {
            board.setPiece(0, minAttackerSquare);
            board.setPiece(attackerPiece, sq);

            /* Do not consider captures if they lose material, therefor max zero */
            //val = Math.max(0, pieceVals[Math.abs(attackedPiece)] - staticExchangeEvaluation(board, sq, -color));

            val = pieceVals[Math.abs(attackedPiece)] - staticExchangeEvaluation(board, sq, -color);


            board.setPiece(attackerPiece, minAttackerSquare);
            board.setPiece(attackedPiece, sq);
        }

        return val;
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



        FastBoard fb = IO.read_FEN(new FastBoard(), "rnbqk1nr/pppp1ppp/8/4P3/1b6/8/PPP1PPPP/RNBQKBNR w QKqk -");

        //System.out.println(BitBoard.squareIndex(4,4));

        AdvancedEvaluator av =  new AdvancedEvaluator(new SimpleDecider());




        av.staticExchangeEvaluation(fb, 51, 1);


    }
}
