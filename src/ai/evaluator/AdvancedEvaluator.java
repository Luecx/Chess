package ai.evaluator;

import ai.evaluator.decider.BoardPhaseDecider;
import ai.tools.tensor.Tensor1D;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.pieces.PieceList;

import java.util.Arrays;

public class AdvancedEvaluator implements Evaluator<AdvancedEvaluator> {

    private BoardPhaseDecider phaseDecider;


    //<editor-fold desc="PST">
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

    //<editor-fold desc="EARLY PARAMS">
    private double PARAMETER_PAWN_TABLE_FACTOR_EARLY = 100.0;
    private double PARAMETER_ROOK_TABLE_FACTOR_EARLY = 100.0;
    private double PARAMETER_KNIGHT_TABLE_FACTOR_EARLY = 100.0;
    private double PARAMETER_BISHOP_TABLE_FACTOR_EARLY = 100.0;
    private double PARAMETER_QUEEN_TABLE_FACTOR_EARLY = 98.0;
    private double PARAMETER_KING_TABLE_FACTOR_EARLY = 100.0;

    private double PARAMETER_PAWN_VALUE_EARLY = 229.0;
    private double PARAMETER_ROOK_VALUE_EARLY = 970.0;
    private double PARAMETER_KNIGHT_VALUE_EARLY = 672.0;
    private double PARAMETER_BISHOP_VALUE_EARLY = 689.0;
    private double PARAMETER_QUEEN_VALUE_EARLY = 1370.0;
    private double PARAMETER_KING_VALUE_EARLY = 20039.0;

    //add for every attacked field
    private double PARAMETER_ROOK_VISIBILITY_EARLY = 21.0;
    private double PARAMETER_BISHOP_VISIBILITY_EARLY = 14.0;
    private double PARAMETER_KNIGHT_VISIBILITY_EARLY = 19.0;
    private double PARAMETER_QUEEN_VISIBILITY_EARLY = 38.0;

    //add for every attacked field which is covered by opponent pawns
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY = -13.0;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY = 1.0;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY = -9.0;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY = -48.0;

    //add if the amount of attacks to uncovered squares is 0
    private double PARAMETER_ROOK_TRAPPED_EARLY = -43.0;
    private double PARAMETER_BISHOP_TRAPPED_EARLY = -47.0;
    private double PARAMETER_KNIGHT_TRAPPED_EARLY = -42.0;
    private double PARAMETER_QUEEN_TRAPPED_EARLY = -45.0;

    private double PARAMETER_ROOK_KING_LINE_EARLY = 56.0;

    private double PARAMETER_PASSED_PAWN_EARLY = 86.0;
    private double PARAMETER_ISOLATED_PAWN_EARLY = -15.0;
    private double PARAMETER_DOUBLED_PAWN_EARLY = -66.0;
    private double PARAMETER_DOUBLE_BISHOP_EARLY = 118.0;
    private double PARAMETER_KING_SAFETY_1_EARLY = 22.0;
    private double PARAMETER_KING_SAFETY_2_EARLY = 58.0;
    private double PARAMETER_ROOK_HALF_OPEN_EARLY = 49.0;
    private double PARAMETER_ROOK_OPEN_EARLY = 4.0;
    private double PARAMETER_CONNECTED_PAWN_EARLY = 11.0;
    //</editor-fold>

    //<editor-fold desc="LATE_PARAMS">
    private double PARAMETER_PAWN_TABLE_FACTOR_LATE = 100.0;
    private double PARAMETER_ROOK_TABLE_FACTOR_LATE = 100.0;
    private double PARAMETER_KNIGHT_TABLE_FACTOR_LATE = 100.0;
    private double PARAMETER_BISHOP_TABLE_FACTOR_LATE = 100.0;
    private double PARAMETER_QUEEN_TABLE_FACTOR_LATE = 98.0;
    private double PARAMETER_KING_TABLE_FACTOR_LATE = 100.0;

    private double PARAMETER_PAWN_VALUE_LATE = 229.0;
    private double PARAMETER_ROOK_VALUE_LATE = 970.0;
    private double PARAMETER_KNIGHT_VALUE_LATE = 672.0;
    private double PARAMETER_BISHOP_VALUE_LATE = 689.0;
    private double PARAMETER_QUEEN_VALUE_LATE = 1370.0;
    private double PARAMETER_KING_VALUE_LATE = 20039.0;

    //add for every attacked field
    private double PARAMETER_ROOK_VISIBILITY_LATE = 21.0;
    private double PARAMETER_BISHOP_VISIBILITY_LATE = 14.0;
    private double PARAMETER_KNIGHT_VISIBILITY_LATE = 19.0;
    private double PARAMETER_QUEEN_VISIBILITY_LATE = 38.0;

    //add for every attacked field which is covered by opponent pawns
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE = -13.0;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE = 1.0;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE = -9.0;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE = -48.0;

    //add if the amount of attacks to uncovered squares is 0
    private double PARAMETER_ROOK_TRAPPED_LATE = -43.0;
    private double PARAMETER_BISHOP_TRAPPED_LATE = -47.0;
    private double PARAMETER_KNIGHT_TRAPPED_LATE = -42.0;
    private double PARAMETER_QUEEN_TRAPPED_LATE = -45.0;

    private double PARAMETER_ROOK_KING_LINE_LATE = 56.0;

    private double PARAMETER_PASSED_PAWN_LATE = 86.0;
    private double PARAMETER_ISOLATED_PAWN_LATE = -15.0;
    private double PARAMETER_DOUBLED_PAWN_LATE = -66.0;
    private double PARAMETER_DOUBLE_BISHOP_LATE = 118.0;
    private double PARAMETER_KING_SAFETY_1_LATE = 22.0;
    private double PARAMETER_KING_SAFETY_2_LATE = 58.0;
    private double PARAMETER_ROOK_HALF_OPEN_LATE = 49.0;
    private double PARAMETER_ROOK_OPEN_LATE = 4.0;
    private double PARAMETER_CONNECTED_PAWN_LATE = 11.0;
    //</editor-fold>


    public AdvancedEvaluator(BoardPhaseDecider phaseDecider) {
        this.phaseDecider = phaseDecider;
    }

    public double taper(double param1, double param2, double taper) {
        return param1 + (param2 - param1) * taper;
    }

    @Override
    public double evaluate(Board board) {
        double phase = phaseDecider.getGamePhase(board);
        FastBoard fb = (FastBoard) board;


        return evalateSide(1,
                           fb.getWhite_pieces(),
                           new Tensor1D[]{PAWN_VALUES_WHITE, ROOK_VALUES_WHITE, KNIGHT_VALUES_WHITE, BISHOP_VALUES_WHITE, QUEEN_VALUES_WHITE, KING_VALUES_MID_WHITE},
                           fb.getWhite_values(),
                           fb.getTeam_total()[0],
                           BitBoard.whitePassedPawnMask,
                           fb.getBlack_values(),
                           fb.getTeam_total()[1],
                           fb.getOccupied(),
                           phase)
               - evalateSide(-1,
                             fb.getBlack_pieces(),
                             new Tensor1D[]{PAWN_VALUES_BLACK, ROOK_VALUES_BLACK, KNIGHT_VALUES_BLACK, BISHOP_VALUES_BLACK, QUEEN_VALUES_BLACK, KING_VALUES_MID_BLACK},
                             fb.getBlack_values(),
                             fb.getTeam_total()[1],
                             BitBoard.blackPassedPawnMask,
                             fb.getWhite_values(),
                             fb.getTeam_total()[0],
                             fb.getOccupied(),
                             phase);
    }


    public double evalateSide(
            int color,
            PieceList[] ourPieces,
            Tensor1D[] pst,
            long[] ourPieceOccupancy,
            long ourTotalOccupancy,
            long[] ourPassedPawnMask,
            long[] opponentPieceOccupancy,
            long opponentTotalOccupancy,
            long totalOccupied,
            double taper) {


        long opponentPawnCover =
                (color == 1 ?
                         BitBoard.shiftSouthWest(opponentPieceOccupancy[0]) | BitBoard.shiftSouthEast(opponentPieceOccupancy[0]) :
                         BitBoard.shiftNorthWest(opponentPieceOccupancy[0]) | BitBoard.shiftNorthEast(opponentPieceOccupancy[0]));
        double eval = 0;

//        double ev = 0;
//        int i, index;
//
//        //-------------------------PAWNS----------------------------------
//        for (i = 0; i < ourPieces[0].size(); i++) {
//            index = ourPieces[0].get(i);
//            ev += pst[0].get(index) * PARAMETER_PAWN_TABLE_FACTOR_EARLY;
//            ev += PARAMETER_PAWN_VALUE_EARLY;
//            if((ourPassedPawnMask[index] & opponentPieceOccupancy[0]) == 0){
//                ev += PARAMETER_PASSED_PAWN_EARLY;
//            }
//            if((BitBoard.files_neighbour[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0){
//                ev += PARAMETER_ISOLATED_PAWN_EARLY;
//            }
//        }
//
//        ev += PARAMETER_DOUBLED_PAWN_EARLY *
//              (color == 1 ?
//                       BitBoard.bitCount(BitBoard.shiftNorth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]) :
//                       BitBoard.bitCount(BitBoard.shiftSouth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]));
//
//
//        ev += PARAMETER_CONNECTED_PAWN_EARLY *
//              (color == 1 ?
//                       (BitBoard.bitCount(BitBoard.shiftNorthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0])+
//                        BitBoard.bitCount(BitBoard.shiftNorthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0])) :
//
//                       (BitBoard.bitCount(BitBoard.shiftSouthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0])+
//                        BitBoard.bitCount(BitBoard.shiftSouthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0])));
//
//        //-------------------------ROOKS----------------------------------
//        for (i = 0; i < ourPieces[1].size(); i++) {
//
//            index = ourPieces[1].get(i);
//            ev += pst[1].get(index)                          * PARAMETER_ROOK_TABLE_FACTOR_EARLY;
//            ev +=                                              PARAMETER_ROOK_VALUE_EARLY;
//            long attacks = BitBoard.lookUpRookAttack(index, totalOccupied) & ~ourTotalOccupancy;
//
//            ev += PARAMETER_ROOK_VISIBILITY_EARLY                  * BitBoard.bitCount(attacks);
//            ev += PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY       * BitBoard.bitCount(attacks & opponentPawnCover);
//            ev += PARAMETER_ROOK_TRAPPED_EARLY                     * (attacks & opponentPawnCover) == attacks ? 1:0;
//
//            ev += PARAMETER_ROOK_KING_LINE_EARLY                   * ((BitBoard.lookUpRookAttack(index, 0L) & opponentPieceOccupancy[5]) > 0 ? 1 : 0);
//            if((BitBoard.files[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0){     //atleast half open
//                if((BitBoard.files[BitBoard.fileIndex(index)] & opponentPieceOccupancy[0]) == 0){     //open
//                    ev += PARAMETER_ROOK_OPEN_EARLY;
//                }
//                ev += PARAMETER_ROOK_HALF_OPEN_EARLY;
//            }
//        }
//
//        //-------------------------KNIGHTS----------------------------------
//        for (i = 0; i < ourPieces[2].size(); i++) {
//            index = ourPieces[2].get(i);
//            ev += pst[2].get(index) * PARAMETER_KNIGHT_TABLE_FACTOR_EARLY;
//            ev += PARAMETER_KNIGHT_VALUE_EARLY;
//
//
//            long attacks = BitBoard.KNIGHT_ATTACKS[index] & ~ourTotalOccupancy;
//            ev += PARAMETER_KNIGHT_VISIBILITY_EARLY                * BitBoard.bitCount(attacks);
//            ev += PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY     * BitBoard.bitCount(attacks & opponentPawnCover);
//            ev += PARAMETER_KNIGHT_TRAPPED_EARLY                   * (attacks & opponentPawnCover) == attacks ? 1:0;
//        }
//
//        //-------------------------BISHOPS-----------------------------------
//        for (i = 0; i < ourPieces[3].size(); i++) {
//            index = ourPieces[3].get(i);
//            ev += pst[3].get(index) * PARAMETER_BISHOP_TABLE_FACTOR_EARLY;
//            ev += PARAMETER_BISHOP_VALUE_EARLY;
//            long attacks = BitBoard.lookUpBishopAttack(index, totalOccupied) & ~ourTotalOccupancy;
//
//            ev += PARAMETER_BISHOP_VISIBILITY_EARLY                * BitBoard.bitCount(attacks);
//            ev += PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY     * BitBoard.bitCount(attacks & opponentPawnCover);
//            ev += PARAMETER_BISHOP_TRAPPED_EARLY                   * (attacks & opponentPawnCover) == attacks ? 1:0;
//        }
//        if (ourPieces[3].size() > 1) ev += PARAMETER_DOUBLE_BISHOP_EARLY;
//
//
//        //-------------------------QUEENS------------------------------------
//        for (i = 0; i < ourPieces[4].size(); i++) {
//            index = ourPieces[4].get(i);
//            ev += pst[4].get(index) * PARAMETER_QUEEN_TABLE_FACTOR_EARLY;
//            ev += PARAMETER_QUEEN_VALUE_EARLY;
//
//            long attacks = (BitBoard.lookUpBishopAttack(index, totalOccupied) |
//                            BitBoard.lookUpRookAttack(index, totalOccupied))
//                           & ~ourTotalOccupancy;
//
//            ev += PARAMETER_QUEEN_VISIBILITY_EARLY                * BitBoard.bitCount(attacks);
//            ev += PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY     * BitBoard.bitCount(attacks & opponentPawnCover);
//            ev += PARAMETER_QUEEN_TRAPPED_EARLY                   * (attacks & opponentPawnCover) == attacks ? 1:0;
//        }
//
//        //-------------------------KINGS-------------------------------------
//        for (i = 0; i < ourPieces[5].size(); i++) {
//            index = ourPieces[5].get(i);
//            //TODO
//            ev += pst[5].get(index) * PARAMETER_KING_TABLE_FACTOR_EARLY;
//            ev += PARAMETER_KING_VALUE_EARLY;
//            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy)) *
//                  PARAMETER_KING_SAFETY_1_EARLY;
//            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy)) *
//                  PARAMETER_KING_SAFETY_2_EARLY;
//        }
//        //return ev;

        eval += feature_knights(ourPieces, ourTotalOccupancy, opponentPawnCover, pst, taper);
        eval += feature_pawns(ourPieces, color, ourPassedPawnMask, ourPieceOccupancy, opponentPieceOccupancy, pst, taper);
        eval += feature_rooks(ourPieces, ourTotalOccupancy, ourPieceOccupancy, opponentPieceOccupancy, totalOccupied, opponentPawnCover, pst, taper);
        eval += feature_bishops(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, pst, taper);
        eval += feature_queens(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, pst, taper);
        eval += feature_kings(ourPieces, ourTotalOccupancy, opponentTotalOccupancy, pst, taper);

        return eval;

    }

    private double feature_knights(PieceList[] ourPieces,
                                   long ourTotalOccupancy,
                                   long opponentPawnCover,
                                   Tensor1D[] pst,
                                   double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[2].size(); i++) {
            int index = ourPieces[2].get(i);
            ev += taper(PARAMETER_KNIGHT_TABLE_FACTOR_EARLY, PARAMETER_KNIGHT_TABLE_FACTOR_LATE, taper)
                  * pst[2].get(index);
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
                                 Tensor1D[] pst,
                                 double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[0].size(); i++) {
            int index = ourPieces[0].get(i);
            ev += taper(PARAMETER_PAWN_TABLE_FACTOR_EARLY, PARAMETER_PAWN_TABLE_FACTOR_LATE, taper) * pst[0].get(index);
            ev += taper(PARAMETER_PAWN_VALUE_EARLY, PARAMETER_PAWN_VALUE_LATE, taper);
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
                                 Tensor1D[] pst,
                                 double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[1].size(); i++) {
            int index = ourPieces[1].get(i);
            ev += taper(PARAMETER_ROOK_TABLE_FACTOR_EARLY, PARAMETER_ROOK_TABLE_FACTOR_LATE, taper) * pst[1].get(index);
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
                                   Tensor1D[] pst,
                                   double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[3].size(); i++) {
            int index = ourPieces[3].get(i);
            ev += taper(PARAMETER_BISHOP_TABLE_FACTOR_EARLY, PARAMETER_BISHOP_TABLE_FACTOR_LATE, taper)
                  * pst[3].get(index);
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
                                  Tensor1D[] pst,
                                  double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[4].size(); i++) {
            int index = ourPieces[4].get(i);
            ev += taper(PARAMETER_QUEEN_TABLE_FACTOR_EARLY, PARAMETER_QUEEN_TABLE_FACTOR_LATE, taper)
                  * pst[4].get(index);
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
                                 Tensor1D[] pst,
                                 double taper) {
        double ev = 0;
        for (int i = 0; i < ourPieces[5].size(); i++) {
            int index = ourPieces[5].get(i);
            //TODO
            ev += taper(PARAMETER_KING_TABLE_FACTOR_EARLY, PARAMETER_KING_TABLE_FACTOR_LATE, taper)
                  * pst[5].get(index);
            ev += taper(PARAMETER_KING_VALUE_EARLY, PARAMETER_KING_VALUE_LATE, taper);
            ev += taper(PARAMETER_KING_SAFETY_1_EARLY, PARAMETER_KING_SAFETY_1_LATE, taper) *
                  (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy));
            ev += taper(PARAMETER_KING_SAFETY_2_EARLY, PARAMETER_KING_SAFETY_2_LATE, taper) *
                  (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy));
        }
        return ev;
    }

    public void printEvaluation(Board board){
        double phase = phaseDecider.getGamePhase(board);
        FastBoard fb = (FastBoard) board;
        double[] white = evaluateSideTrack(1,
                           fb.getWhite_pieces(),
                           new Tensor1D[]{PAWN_VALUES_WHITE, ROOK_VALUES_WHITE, KNIGHT_VALUES_WHITE, BISHOP_VALUES_WHITE, QUEEN_VALUES_WHITE, KING_VALUES_MID_WHITE},
                           fb.getWhite_values(),
                           fb.getTeam_total()[0],
                           BitBoard.whitePassedPawnMask,
                           fb.getBlack_values(),
                           fb.getTeam_total()[1],
                           fb.getOccupied(),
                           phase);
        double[] black = evaluateSideTrack(-1,
                             fb.getBlack_pieces(),
                             new Tensor1D[]{PAWN_VALUES_BLACK, ROOK_VALUES_BLACK, KNIGHT_VALUES_BLACK, BISHOP_VALUES_BLACK, QUEEN_VALUES_BLACK, KING_VALUES_MID_BLACK},
                             fb.getBlack_values(),
                             fb.getTeam_total()[1],
                             BitBoard.blackPassedPawnMask,
                             fb.getWhite_values(),
                             fb.getTeam_total()[0],
                             fb.getOccupied(),
                             phase);


    }

    public double[] evaluateSideTrack(
            int color,
            PieceList[] ourPieces,
            Tensor1D[] pst,
            long[] ourPieceOccupancy,
            long ourTotalOccupancy,
            long[] ourPassedPawnMask,
            long[] opponentPieceOccupancy,
            long opponentTotalOccupancy,
            long totalOccupied,
            double taper) {
        long opponentPawnCover =
                (color == 1 ?
                         BitBoard.shiftSouthWest(opponentPieceOccupancy[0]) | BitBoard.shiftSouthEast(opponentPieceOccupancy[0]) :
                         BitBoard.shiftNorthWest(opponentPieceOccupancy[0]) | BitBoard.shiftNorthEast(opponentPieceOccupancy[0]));
        double eval[] = new double[6];
        eval[0] += feature_pawns(ourPieces, color, ourPassedPawnMask, ourPieceOccupancy, opponentPieceOccupancy, pst, taper);
        eval[1] += feature_knights(ourPieces, ourTotalOccupancy, opponentPawnCover, pst, taper);
        eval[2] += feature_rooks(ourPieces, ourTotalOccupancy, ourPieceOccupancy, opponentPieceOccupancy, totalOccupied, opponentPawnCover, pst, taper);
        eval[3] += feature_bishops(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, pst, taper);
        eval[4] += feature_queens(ourPieces, ourTotalOccupancy, totalOccupied, opponentPawnCover, pst, taper);
        eval[5] += feature_kings(ourPieces, ourTotalOccupancy, opponentTotalOccupancy, pst, taper);
        return eval;
    }


    public double[] getEvolvableValues() {
        return new double[]{
                PARAMETER_PAWN_TABLE_FACTOR_EARLY,
                PARAMETER_ROOK_TABLE_FACTOR_EARLY,
                PARAMETER_KNIGHT_TABLE_FACTOR_EARLY,
                PARAMETER_BISHOP_TABLE_FACTOR_EARLY,
                PARAMETER_QUEEN_TABLE_FACTOR_EARLY,
                PARAMETER_KING_TABLE_FACTOR_EARLY,
                PARAMETER_PAWN_VALUE_EARLY,
                PARAMETER_ROOK_VALUE_EARLY,
                PARAMETER_KNIGHT_VALUE_EARLY,
                PARAMETER_BISHOP_VALUE_EARLY,
                PARAMETER_QUEEN_VALUE_EARLY,
                PARAMETER_KING_VALUE_EARLY,
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
                PARAMETER_PAWN_VALUE_LATE,
                PARAMETER_ROOK_VALUE_LATE,
                PARAMETER_KNIGHT_VALUE_LATE,
                PARAMETER_BISHOP_VALUE_LATE,
                PARAMETER_QUEEN_VALUE_LATE,
                PARAMETER_KING_VALUE_LATE,
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
        PARAMETER_PAWN_TABLE_FACTOR_EARLY = ar[0];
        PARAMETER_ROOK_TABLE_FACTOR_EARLY = ar[1];
        PARAMETER_KNIGHT_TABLE_FACTOR_EARLY = ar[2];
        PARAMETER_BISHOP_TABLE_FACTOR_EARLY = ar[3];
        PARAMETER_QUEEN_TABLE_FACTOR_EARLY = ar[4];
        PARAMETER_KING_TABLE_FACTOR_EARLY = ar[5];
        PARAMETER_PAWN_VALUE_EARLY = ar[6];
        PARAMETER_ROOK_VALUE_EARLY = ar[7];
        PARAMETER_KNIGHT_VALUE_EARLY = ar[8];
        PARAMETER_BISHOP_VALUE_EARLY = ar[9];
        PARAMETER_QUEEN_VALUE_EARLY = ar[10];
        PARAMETER_KING_VALUE_EARLY = ar[11];
        PARAMETER_ROOK_VISIBILITY_EARLY = ar[12];
        PARAMETER_BISHOP_VISIBILITY_EARLY = ar[13];
        PARAMETER_KNIGHT_VISIBILITY_EARLY = ar[14];
        PARAMETER_QUEEN_VISIBILITY_EARLY = ar[15];
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY = ar[16];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY = ar[17];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY = ar[18];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY = ar[19];
        PARAMETER_ROOK_TRAPPED_EARLY = ar[20];
        PARAMETER_BISHOP_TRAPPED_EARLY = ar[21];
        PARAMETER_KNIGHT_TRAPPED_EARLY = ar[22];
        PARAMETER_QUEEN_TRAPPED_EARLY = ar[23];
        PARAMETER_ROOK_KING_LINE_EARLY = ar[24];
        PARAMETER_PASSED_PAWN_EARLY = ar[25];
        PARAMETER_ISOLATED_PAWN_EARLY = ar[26];
        PARAMETER_DOUBLED_PAWN_EARLY = ar[27];
        PARAMETER_DOUBLE_BISHOP_EARLY = ar[28];
        PARAMETER_KING_SAFETY_1_EARLY = ar[29];
        PARAMETER_KING_SAFETY_2_EARLY = ar[30];
        PARAMETER_ROOK_HALF_OPEN_EARLY = ar[31];
        PARAMETER_ROOK_OPEN_EARLY = ar[32];
        PARAMETER_CONNECTED_PAWN_EARLY = ar[33];
        PARAMETER_PAWN_TABLE_FACTOR_LATE = ar[34];
        PARAMETER_ROOK_TABLE_FACTOR_LATE = ar[35];
        PARAMETER_KNIGHT_TABLE_FACTOR_LATE = ar[36];
        PARAMETER_BISHOP_TABLE_FACTOR_LATE = ar[37];
        PARAMETER_QUEEN_TABLE_FACTOR_LATE = ar[38];
        PARAMETER_KING_TABLE_FACTOR_LATE = ar[39];
        PARAMETER_PAWN_VALUE_LATE = ar[40];
        PARAMETER_ROOK_VALUE_LATE = ar[41];
        PARAMETER_KNIGHT_VALUE_LATE = ar[42];
        PARAMETER_BISHOP_VALUE_LATE = ar[43];
        PARAMETER_QUEEN_VALUE_LATE = ar[44];
        PARAMETER_KING_VALUE_LATE = ar[45];
        PARAMETER_ROOK_VISIBILITY_LATE = ar[46];
        PARAMETER_BISHOP_VISIBILITY_LATE = ar[47];
        PARAMETER_KNIGHT_VISIBILITY_LATE = ar[48];
        PARAMETER_QUEEN_VISIBILITY_LATE = ar[49];
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE = ar[50];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE = ar[51];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE = ar[52];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE = ar[53];
        PARAMETER_ROOK_TRAPPED_LATE = ar[54];
        PARAMETER_BISHOP_TRAPPED_LATE = ar[55];
        PARAMETER_KNIGHT_TRAPPED_LATE = ar[56];
        PARAMETER_QUEEN_TRAPPED_LATE = ar[57];
        PARAMETER_ROOK_KING_LINE_LATE = ar[58];
        PARAMETER_PASSED_PAWN_LATE = ar[59];
        PARAMETER_ISOLATED_PAWN_LATE = ar[60];
        PARAMETER_DOUBLED_PAWN_LATE = ar[61];
        PARAMETER_DOUBLE_BISHOP_LATE = ar[62];
        PARAMETER_KING_SAFETY_1_LATE = ar[63];
        PARAMETER_KING_SAFETY_2_LATE = ar[64];
        PARAMETER_ROOK_HALF_OPEN_LATE = ar[65];
        PARAMETER_ROOK_OPEN_LATE = ar[66];
        PARAMETER_CONNECTED_PAWN_LATE = ar[67];
    }

    @Override
    public AdvancedEvaluator copy() {
        AdvancedEvaluator evaluator = new AdvancedEvaluator(phaseDecider);
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }
}
