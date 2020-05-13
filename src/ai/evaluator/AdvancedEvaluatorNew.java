package ai.evaluator;

import ai.evaluator.decider.BoardPhaseDecider;
import ai.tools.tensor.Tensor1D;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.pieces.PieceList;
import io.IO;
import io.UCI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class AdvancedEvaluatorNew implements Evaluator<AdvancedEvaluatorNew> {

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
    private double CONST_PARAMETER_PAWN_VALUE =                                 100;
    private double CONST_PARAMETER_KING_VALUE =                                10000;
    private double CONST_PARAMETER_KING_LONELY =                                -20;

    
    private int c = 0;
    
    /**
     * tunable params
     */
    private int PARAMETER_PAWN_TABLE_FACTOR =                               c++;
    private int PARAMETER_PAWN_CONNECTED =                                  c++;
    private int PARAMETER_PAWN_PASSED =                                     c++;
    private int PARAMETER_PAWN_ISOLATED =                                   c++;
    private int PARAMETER_PAWN_DOUBLED =                                    c++;
    private int PARAMETER_PAWN_CONNECTED_PASSED =                           c++;
    private int PARAMETER_PAWN_DOUBLED_ISOLATED =                           c++;

    private int PARAMETER_KNIGHT_TABLE_FACTOR =                             c++;
    private int PARAMETER_KNIGHT_VALUE =                                    c++;
    private int PARAMETER_KNIGHT_VISIBILITY =                               c++;
    private int PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER =                    c++;
    private int PARAMETER_KNIGHT_TRAPPED =                                  c++;

    private int PARAMETER_ROOK_TABLE_FACTOR =                               c++;
    private int PARAMETER_ROOK_VALUE =                                      c++;
    private int PARAMETER_ROOK_VISIBILITY =                                 c++;
    private int PARAMETER_ROOK_VISIBILITY_PAWN_COVER =                      c++;
    private int PARAMETER_ROOK_TRAPPED =                                    c++;
    private int PARAMETER_ROOK_KING_LINE =                                  c++;
    private int PARAMETER_ROOK_HALF_OPEN =                                  c++;
    private int PARAMETER_ROOK_OPEN =                                       c++;

    private int PARAMETER_BISHOP_TABLE_FACTOR =                             c++;
    private int PARAMETER_BISHOP_VALUE =                                    c++;
    private int PARAMETER_BISHOP_VISIBILITY =                               c++;
    private int PARAMETER_BISHOP_VISIBILITY_PAWN_COVER =                    c++;
    private int PARAMETER_BISHOP_TRAPPED =                                  c++;
    private int PARAMETER_BISHOP_DOUBLED =                                  c++;
    private int PARAMETER_BISHOP_CLOSED_PENALTY =                           c++;
    private int PARAMETER_BISHOP_OPEN_BONUS =                               c++;

    private int PARAMETER_QUEEN_TABLE_FACTOR =                              c++;
    private int PARAMETER_QUEEN_VALUE =                                     c++;
    private int PARAMETER_QUEEN_VISIBILITY =                                c++;
    private int PARAMETER_QUEEN_TRAPPED =                                   c++;
    private int PARAMETER_QUEEN_VISIBILITY_PAWN_COVER =                     c++;

    private int PARAMETER_KING_TABLE_FACTOR =                               c++;
    private int PARAMETER_KING_SAFETY_1 =                                   c++;
    private int PARAMETER_KING_SAFETY_2 =                                   c++;
    private int PARAMETER_KING_SAFETY_3 =                                   c++;
    private int PARAMETER_KING_PAWN_SHIELD =                                c++;

    private double[] PARAMSEARLY = new double[]{
            79,
            16,
            -3,
            -22,
            -3,
            20,
            -10,

            50,
            479,
            10,
            -15,
            -34,

            152,
            627,
            5,
            -13,
            -6,
            6,
            26,
            51,

            144,
            474,
            8,
            -10,
            -67,
            40,
            2,
            15,

            67,
            1477,
            3,
            12,
            -16,

            144,
            11,
            -73,
            -28,
            20};
    private double[] PARAMSLATE = new double[]{
            137,
            11,
            69,
            -11,
            -9,
            55,
            -40,

            156,
            349,
            6,
            3,
            -20,

            82,
            681,
            5,
            3,
            -16,
            9,
            -1,
            -16,


            91,
            374,
            2,
            7,
            -2,
            65,
            -12,
            15,

            46,
            1169,
            9,
            -69,
            12,

            12,
            -7,
            53,
            13,
            4};

    private double[] FEATURES = new double[PARAMSEARLY.length];
    private double[] FEATURE_CONST = new double[3];

    private double phase;

    private double[] pieceVals = new double[]{0,
            CONST_PARAMETER_PAWN_VALUE,
            PARAMSEARLY[PARAMETER_ROOK_VALUE],
            PARAMSEARLY[PARAMETER_KNIGHT_VALUE],
            PARAMSEARLY[PARAMETER_BISHOP_VALUE],
            PARAMSEARLY[PARAMETER_QUEEN_VALUE],
            CONST_PARAMETER_KING_VALUE};



    public AdvancedEvaluatorNew(BoardPhaseDecider phaseDecider) {
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

    public double[] getGradients(){
        double[] grads = new double[FEATURES.length * 2];

        for(int i = 0; i < FEATURES.length; i++){
            grads[i] = FEATURES[i] * (1-phase);
            grads[i+FEATURES.length] = FEATURES[i] * (phase);
        }

        return grads;
    }


    private boolean isComputing = false;


    public boolean isComputing() {
        return isComputing;
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

        if(isComputing) throw new RuntimeException();
        isComputing = true;

        phase = phaseDecider.getGamePhase(board);
        FastBoard fb = (FastBoard) board;

        if(probablyInsufficientMaterial(fb.getWhite_pieces(), fb.getBlack_pieces())) {
            isComputing = false;
            return 0;
        }

        Arrays.fill(FEATURE_CONST, 0);
        Arrays.fill(FEATURES, 0);

        featureWhite(fb, phase);
        featureBlack(fb, phase);

        double res = result(phase);
        isComputing = false;
        return res;
    }

    private double result(double phase){

        double early = 0;
        double late = 0;

        for(int i = 0; i < FEATURES.length; i++){
            early += PARAMSEARLY[i] * FEATURES[i];
            late += PARAMSLATE[i] * FEATURES[i];
        }

        double result = early * (1-phase) + late * phase;

        result += FEATURE_CONST[0] * CONST_PARAMETER_PAWN_VALUE;
        result += FEATURE_CONST[1] * CONST_PARAMETER_KING_VALUE;
        result += FEATURE_CONST[2] * CONST_PARAMETER_KING_LONELY;

        return result;

    }

    public void featureWhite(FastBoard fb, double phase){
        featureSide(1,
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

    public void featureBlack(FastBoard fb, double phase){
        featureSide(-1,
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
    public void featureSide(
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

        feature_knights(ourPieces, color, ourTotalOccupancy, opponentPawnCover, earlyPST, latePST, taper);
        feature_pawns(ourPieces, color, ourPassedPawnMask, ourPieceOccupancy, opponentPieceOccupancy, earlyPST, latePST, taper);
        feature_rooks(ourPieces, color, ourTotalOccupancy, ourPieceOccupancy, opponentPieceOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper);
        feature_bishops(ourPieces, color, ourTotalOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper, ourPieceOccupancy, opponentPieceOccupancy);
        feature_queens(ourPieces, color, ourTotalOccupancy, totalOccupied, opponentPawnCover, earlyPST, latePST, taper);
        feature_kings(ourPieces, color, ourPieceOccupancy, ourTotalOccupancy, opponentTotalOccupancy, attackedSquares, earlyPST, latePST, taper);

    }

    private void feature_knights(PieceList[] ourPieces,
                                   int color,
                                   long ourTotalOccupancy,
                                   long opponentPawnCover,
                                   Tensor1D[] pstEarly,
                                   Tensor1D[] pstLate,
                                   double taper) {
        for (int i = 0; i < ourPieces[2].size(); i++) {
            int index = ourPieces[2].get(i);

            long attacks = BitBoard.KNIGHT_ATTACKS[index] & ~ourTotalOccupancy;

            FEATURES[PARAMETER_KNIGHT_VALUE] += color;
            FEATURES[PARAMETER_KNIGHT_TABLE_FACTOR] += color * taper(pstEarly[2].get(index), pstLate[2].get(index), taper);
            FEATURES[PARAMETER_KNIGHT_VISIBILITY] += color * BitBoard.bitCount(attacks);
            FEATURES[PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER] += color * BitBoard.bitCount(attacks & opponentPawnCover);
            FEATURES[PARAMETER_KNIGHT_TRAPPED] += color * ((attacks & opponentPawnCover) == attacks ? 1 : 0);

        }
    }


    private void feature_pawns(PieceList[] ourPieces,
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

        for (int i = 0; i < ourPieces[0].size(); i++) {
            int index = ourPieces[0].get(i);


            FEATURES[PARAMETER_PAWN_TABLE_FACTOR] += color * taper(pstEarly[0].get(index), pstLate[0].get(index), taper);
            FEATURE_CONST[0] += color;

            boolean passed = (ourPassedPawnMask[index] & opponentPieceOccupancy[0]) == 0;
            boolean connected = ((1L << index) & (connectedPawnsEast | connectedPawnsWest)) != 0;
            boolean isolated = (BitBoard.files_neighbour[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0;
            //boolean doubled = ((BitBoard.files[index % 8] & ourPieceOccupancy[0]) - 1) != 0;

            if (passed && connected) {
                FEATURES[PARAMETER_PAWN_CONNECTED_PASSED] += color;
            } else {
                if (passed) {
                    FEATURES[PARAMETER_PAWN_PASSED] += color;
                }
                if (connected) {
                    FEATURES[PARAMETER_PAWN_CONNECTED] += color;
                }
            }

            if (isolated) {
                FEATURES[PARAMETER_PAWN_ISOLATED] += color;
            }

        }

        FEATURES[PARAMETER_PAWN_DOUBLED] += color * BitBoard.bitCount((BitBoard.shiftNorth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]));
    }

    private void feature_rooks(PieceList[] ourPieces,
                                 int color,
                                 long ourTotalOccupancy,
                                 long[] ourPieceOccupancy,
                                 long[] opponentPieceOccupancy,
                                 long totalOccupied,
                                 long opponentPawnCover,
                                 Tensor1D[] pstEarly,
                                 Tensor1D[] pstLate,
                                 double taper) {
        for (int i = 0; i < ourPieces[1].size(); i++) {
            int index = ourPieces[1].get(i);
            long attacks = BitBoard.lookUpRookAttack(index, totalOccupied) & ~ourTotalOccupancy;


            FEATURES[PARAMETER_ROOK_VALUE] += color;
            FEATURES[PARAMETER_ROOK_TABLE_FACTOR] += color * taper(pstEarly[1].get(index), pstLate[1].get(index), taper);
            FEATURES[PARAMETER_ROOK_VISIBILITY] += color * BitBoard.bitCount(attacks);
            FEATURES[PARAMETER_ROOK_VISIBILITY_PAWN_COVER] += color * BitBoard.bitCount(attacks & opponentPawnCover);
            FEATURES[PARAMETER_ROOK_TRAPPED] += color * ((attacks & opponentPawnCover) == attacks ? 1 : 0);
            FEATURES[PARAMETER_ROOK_KING_LINE] += color * ((BitBoard.lookUpRookAttack(index, 0L) & opponentPieceOccupancy[5]) > 0 ? 1 : 0);

            if ((BitBoard.files[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0) {     //atleast half open
                if ((BitBoard.files[BitBoard.fileIndex(index)] & opponentPieceOccupancy[0]) == 0) {     //open
                    FEATURES[PARAMETER_ROOK_OPEN] += color;
                }
                FEATURES[PARAMETER_ROOK_HALF_OPEN] += color;
            }
        }
    }

    private void feature_bishops(PieceList[] ourPieces,
                                   int color,
                                   long ourTotalOccupancy,
                                   long totalOccupied,
                                   long opponentPawnCover,
                                   Tensor1D[] pstEarly,
                                   Tensor1D[] pstLate,
                                   double taper,
                                   long[] ourPieceOccupancy,
                                   long[] opponentPieceOccupancy) {
        for (int i = 0; i < ourPieces[3].size(); i++) {
            int index = ourPieces[3].get(i);

            long attacks = BitBoard.lookUpBishopAttack(index, totalOccupied) & ~ourTotalOccupancy;


            FEATURES[PARAMETER_BISHOP_VALUE] += color;
            FEATURES[PARAMETER_BISHOP_TABLE_FACTOR] += color * taper(pstEarly[3].get(index), pstLate[3].get(index), taper);
            FEATURES[PARAMETER_BISHOP_VISIBILITY] += color * BitBoard.bitCount(attacks);
            FEATURES[PARAMETER_BISHOP_VISIBILITY_PAWN_COVER] += color * BitBoard.bitCount(attacks & opponentPawnCover);
            FEATURES[PARAMETER_BISHOP_TRAPPED] += color * ((attacks & opponentPawnCover) == attacks ? 1 : 0);



            int occupiedCenterSquares = BitBoard.bitCount(BitBoard.center_squares & (ourPieceOccupancy[0] | opponentPieceOccupancy[0]));
            //// VERY POSSIBLY WRONG --Noah
            if (occupiedCenterSquares <= 1)  {
                FEATURES[PARAMETER_BISHOP_OPEN_BONUS] += color;
            } else if (occupiedCenterSquares >= 3) {
                FEATURES[PARAMETER_BISHOP_CLOSED_PENALTY] += color;
            }
        }



        if (ourPieces[3].size() > 1) {
            FEATURES[PARAMETER_BISHOP_DOUBLED] += color;
        }
    }

    private void feature_queens(PieceList[] ourPieces,
                                  int color,
                                  long ourTotalOccupancy,
                                  long totalOccupied,
                                  long opponentPawnCover,
                                  Tensor1D[] pstEarly,
                                  Tensor1D[] pstLate,
                                  double taper) {
        for (int i = 0; i < ourPieces[4].size(); i++) {
            int index = ourPieces[4].get(i);

            long attacks = (BitBoard.lookUpBishopAttack(index, totalOccupied) |
                            BitBoard.lookUpRookAttack(index, totalOccupied))
                           & ~ourTotalOccupancy;

            FEATURES[PARAMETER_QUEEN_VALUE] += color;
            FEATURES[PARAMETER_QUEEN_TABLE_FACTOR] += color * taper(pstEarly[4].get(index), pstLate[4].get(index), taper);
            FEATURES[PARAMETER_QUEEN_VISIBILITY] += color * BitBoard.bitCount(attacks);
            FEATURES[PARAMETER_QUEEN_VISIBILITY_PAWN_COVER] += color * BitBoard.bitCount(attacks & opponentPawnCover);
            FEATURES[PARAMETER_QUEEN_TRAPPED] += color * ((attacks & opponentPawnCover) == attacks ? 1 : 0);

        }
    }


    private double feature_kings(PieceList[] ourPieces,
                                 int color,
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

                FEATURES[PARAMETER_KING_TABLE_FACTOR] += color * taper(pstEarly[5].get(index), pstLate[5].get(index), taper);
                FEATURES[PARAMETER_KING_SAFETY_1] += color * (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy));
                FEATURES[PARAMETER_KING_SAFETY_2] += color * (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy));
                FEATURES[PARAMETER_KING_SAFETY_3] += color * BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & attackedSquares);
                FEATURES[PARAMETER_KING_PAWN_SHIELD] += color * (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourPieceOccupancy[0]));

            }
            else{
                FEATURE_CONST[2] += color * BitBoard.chebyshevDistance(index, BitBoard.bitscanForward(opponentTotalOccupancy));
            }

            FEATURE_CONST[1] += color;




        }
        return ev;
    }


    @Override
    public double[] getEvolvableValues(){
        double[] res = new double[PARAMSEARLY.length + PARAMSLATE.length];
        for(int i = 0; i < res.length; i++){
            if(i < PARAMSEARLY.length) res[i] = PARAMSEARLY[i];
            else res[i] = PARAMSLATE[i-PARAMSEARLY.length];
        }
        return res;
    }

    @Override
    public void setEvolvableValues(double[] ar){
        PARAMSEARLY = Arrays.copyOfRange(ar, 0, PARAMSEARLY.length);
        PARAMSLATE = Arrays.copyOfRange(ar, PARAMSEARLY.length, PARAMSEARLY.length + PARAMSLATE.length);
    }


    public void printEval(FastBoard board){
        double res = this.evaluate(board);

        ArrayList<String> labels = new ArrayList<>();

        for(Field f:this.getClass().getDeclaredFields()){
            if(f.getName().startsWith("PARAMETER")){
                labels.add(f.getName());
            }
        }
        double phase = this.phaseDecider.getGamePhase(board);

        String format = "%-60s %15.3f %15.3f %15.3f %15.3f %15.3f %n";

        System.out.format("%-60s %15s %15s %15s %15s %15s %n", "name", "count", "early weight", "late weight", "res. weight", "total");

        for(int i = 0; i < 140; i++){
            System.out.print("=");
        }
        System.out.println();

        System.out.format(format, "PAWN_VALUE", FEATURE_CONST[0], CONST_PARAMETER_PAWN_VALUE, CONST_PARAMETER_PAWN_VALUE, CONST_PARAMETER_PAWN_VALUE, FEATURE_CONST[0] * CONST_PARAMETER_PAWN_VALUE);
        System.out.format(format, "KING_VALUE", FEATURE_CONST[1], CONST_PARAMETER_KING_VALUE, CONST_PARAMETER_KING_VALUE, CONST_PARAMETER_KING_VALUE, FEATURE_CONST[1] * CONST_PARAMETER_KING_VALUE);
        System.out.format(format, "OPPONENT_KING_ALONE", FEATURE_CONST[2], CONST_PARAMETER_KING_LONELY, CONST_PARAMETER_KING_LONELY, CONST_PARAMETER_KING_LONELY, FEATURE_CONST[2] * CONST_PARAMETER_KING_LONELY);

        for(int i = 0; i < FEATURES.length; i++){
            System.out.format(format, labels.get(i) , FEATURES[i], PARAMSEARLY[i], PARAMSLATE[i], taper(PARAMSEARLY[i], PARAMSLATE[i], phase), taper(PARAMSEARLY[i], PARAMSLATE[i], phase) * FEATURES[i]);
        }for(int i = 0; i < 140; i++){
            System.out.print("=");
        }
        System.out.println();
        System.out.format("%-124s %15.3f %n", "total", res);
    }



    @Override
    public AdvancedEvaluatorNew copy() {
        AdvancedEvaluatorNew evaluator = new AdvancedEvaluatorNew(phaseDecider);
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



}
