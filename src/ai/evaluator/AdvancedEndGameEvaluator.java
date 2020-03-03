package ai.evaluator;

import ai.tools.tensor.Tensor1D;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.pieces.PieceList;
import io.IO;

public class AdvancedEndGameEvaluator implements LateGameEvaluator {

    public static final Tensor1D PAWN_VALUES_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
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

    public static final Tensor1D KNIGHT_VALUES_WHITE =(Tensor1D)  flipTensor(new Tensor1D(new double[]{
            -20, -16, -12, -12, -12, -12, -16, -20,
            -8, -4, 0, 0, 0, 0, -4,-8 ,
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



    public static final Tensor1D KING_VALUES_LATE_WHITE = (Tensor1D) flipTensor(new Tensor1D(new double[]{
            -30, -30, -30, -30, -30, -30, -30, -30,
            -30, -10, -10, -10, -10, -10, -10, -30,
            -30, -10, 30, 30, 30, 30, -10, -30,
            -30, -10, 30, 30, 30, 30, -10, -30,
            -30, -10, 30, 30, 30, 30, -10, -30,
            -30, -10, 30, 30, 30, 30, -10, -30,
            -30, -10, -10, -10, -10, -10, -10, -30,
            -30, -30, -30, -30, -30, -30, -30, -30
    })).scale(0.01);
    
    public static final Tensor1D KING_VALUES_LATE_BLACK = flipTensor(KING_VALUES_LATE_WHITE);
    public static final Tensor1D QUEEN_VALUES_BLACK = flipTensor(QUEEN_VALUES_WHITE);
    public static final Tensor1D ROOK_VALUES_BLACK = flipTensor(ROOK_VALUES_WHITE);
    public static final Tensor1D BISHOP_VALUES_BLACK = flipTensor(BISHOP_VALUES_WHITE);
    public static final Tensor1D KNIGHT_VALUES_BLACK = flipTensor(KNIGHT_VALUES_WHITE);
    public static final Tensor1D PAWN_VALUES_BLACK = flipTensor(PAWN_VALUES_WHITE);

    
    public static Tensor1D addScalarToTensor(Tensor1D tensor2D, double scalar){
        for (int i = 0; i < 64; i++) {
            tensor2D.add(scalar, i);
        }
        return tensor2D;
    }

    public static Tensor1D negateTensor(Tensor1D tensor2D){
        Tensor1D tensor2D1 = new Tensor1D(tensor2D);
        tensor2D1.scale(-1);
        return tensor2D1;
    }

    public static Tensor1D flipTensor(Tensor1D tensor){
        Tensor1D flipped = new Tensor1D(tensor);
        for(int i = 0; i < 32; i++){
            int file = BitBoard.fileIndex(i);
            int rank = BitBoard.rankIndex(i);
            int otherRank = 7 - rank;
            flipped.set(tensor.get(BitBoard.squareIndex(otherRank, file)), BitBoard.squareIndex(rank, file));
            flipped.set(tensor.get(BitBoard.squareIndex(rank, file)), BitBoard.squareIndex(otherRank, file));
        }
        return flipped;
    }

    //pawn, rook,knight,bishop,queen,king
    public static final int[] EVALUATE_PRICE = new int[]{0, 100, 500, 320, 330, 900, 20000};
//    public static final Tensor2D POSITION_PRICE =
//            new Tensor2D(
//                    addScalarToTensor(negateTensor(KING_VALUES_MID), -EVALUATE_PRICE[6]),
//                    addScalarToTensor(negateTensor(QUEEN_VALUES), -EVALUATE_PRICE[5]),
//                    addScalarToTensor(negateTensor(BISHOP_VALUES), -EVALUATE_PRICE[4]),
//                    addScalarToTensor(negateTensor(KNIGHT_VALUES), -EVALUATE_PRICE[3]),
//                    addScalarToTensor(negateTensor(ROOK_VALUES), -EVALUATE_PRICE[2]),
//                    addScalarToTensor(negateTensor(PAWN_VALUES), -EVALUATE_PRICE[1]),
//                    addScalarToTensor(negateTensor(KING_VALUES_MID), 0),
//                    addScalarToTensor(flipTensor(PAWN_VALUES), EVALUATE_PRICE[1]),
//                    addScalarToTensor(flipTensor(ROOK_VALUES), EVALUATE_PRICE[2]),
//                    addScalarToTensor(flipTensor(KNIGHT_VALUES), EVALUATE_PRICE[3]),
//                    addScalarToTensor(flipTensor(BISHOP_VALUES), EVALUATE_PRICE[4]),
//                    addScalarToTensor(flipTensor(QUEEN_VALUES), EVALUATE_PRICE[5]),
//                    addScalarToTensor(flipTensor(KING_VALUES_MID), EVALUATE_PRICE[6]));




    //public static final Tensor2D midValue = POSITION_PRICE;
    //public static final Tensor2D endValue = LateGameEvaluator.POSITION_PRICE;



    //correction values for tables
    private double PARAMETER_PAWN_TABLE_FACTOR      = 430.0;
    private double PARAMETER_ROOK_TABLE_FACTOR      = 215.0;
    private double PARAMETER_KNIGHT_TABLE_FACTOR    = 247.0;
    private double PARAMETER_BISHOP_TABLE_FACTOR    = 157.0;
    private double PARAMETER_QUEEN_TABLE_FACTOR     = -309.0;
    private double PARAMETER_KING_TABLE_FACTOR      = -5.0;


    private double PARAMETER_PAWN_VALUE             = 285.0;
    private double PARAMETER_ROOK_VALUE             = 1764.0;
    private double PARAMETER_KNIGHT_VALUE           = 903.0;
    private double PARAMETER_BISHOP_VALUE           = 989.0;
    private double PARAMETER_QUEEN_VALUE            = 3107.0;
    private double PARAMETER_KING_VALUE             = 20038;

    //add for every attacked field
    private double PARAMETER_ROOK_VISIBILITY         = 8.0;
    private double PARAMETER_BISHOP_VISIBILITY       = 12.0;
    private double PARAMETER_KNIGHT_VISIBILITY       = 22.0;
    private double PARAMETER_QUEEN_VISIBILITY        = 19.0;

    //add for every attacked field which is covered by opponent pawns
    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER           = 1;
    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER         = 11.0;
    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER         = 2.0;
    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER          = 10.0;


    //add if the amount of attacks to uncovered squares is 0
    private double PARAMETER_ROOK_TRAPPED                = -40.0;
    private double PARAMETER_BISHOP_TRAPPED              = -45.0;
    private double PARAMETER_KNIGHT_TRAPPED              = -44.0;
    private double PARAMETER_QUEEN_TRAPPED               = -45.0;

    private double PARAMETER_ROOK_KING_LINE         = 18.0;


    private double PARAMETER_PASSED_PAWN        = 130.0;
    private double PARAMETER_ISOLATED_PAWN      = -27.0;
    private double PARAMETER_DOUBLED_PAWN       = -76.0;
    private double PARAMETER_DOUBLE_BISHOP      = 180.0;
    private double PARAMETER_KING_SAFETY_1      = 0.0;       //10
    private double PARAMETER_KING_SAFETY_2      = 91.0;       //18
    private double PARAMETER_ROOK_HALF_OPEN     = 18.0;
    private double PARAMETER_ROOK_OPEN          = -7.0;
    private double PARAMETER_CONNECTED_PAWN     = 8.0;

    //will get to this later. For now, I'm using this file to store position values
    @Override
    public double evaluate(Board board) {

        FastBoard fb = (FastBoard) board;

        return (int)((evalateSide(1,
                           fb.getWhite_pieces(),
                           new Tensor1D[]{PAWN_VALUES_WHITE, ROOK_VALUES_WHITE, KNIGHT_VALUES_WHITE, BISHOP_VALUES_WHITE, QUEEN_VALUES_WHITE, KING_VALUES_LATE_BLACK},
                           
                           fb.getWhite_values(),
                           fb.getTeam_total()[0],
                           BitBoard.whitePassedPawnMask,
                           
                           fb.getBlack_values(),
                           fb.getTeam_total()[1],

                           fb.getOccupied()) 
               -evalateSide(-1,
                            fb.getBlack_pieces(),
                            new Tensor1D[]{PAWN_VALUES_BLACK, ROOK_VALUES_BLACK, KNIGHT_VALUES_BLACK, BISHOP_VALUES_BLACK, QUEEN_VALUES_BLACK, KING_VALUES_LATE_BLACK},

                            fb.getBlack_values(),
                            fb.getTeam_total()[1],
                            BitBoard.blackPassedPawnMask,

                            fb.getWhite_values(),
                            fb.getTeam_total()[0],

                            fb.getOccupied()))/ (PARAMETER_PAWN_VALUE/100));


//
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

            long totalOccupied) {



        int i,index;
        double ev = 0;

        //draw
        if(ourPieces[0].size() == 0 && ourPieces[1].size() == 0 && (ourPieces[2].size() + ourPieces[3].size() <= 1) && ourPieces[4].size() == 0) return 0;

        long opponentPawnCover =
                (color == 1 ?
                         BitBoard.shiftSouthWest(opponentPieceOccupancy[0]) | BitBoard.shiftSouthEast(opponentPieceOccupancy[0]) :
                         BitBoard.shiftNorthWest(opponentPieceOccupancy[0]) | BitBoard.shiftNorthEast(opponentPieceOccupancy[0]));


        //-------------------------PAWNS----------------------------------
        for (i = 0; i < ourPieces[0].size(); i++) {
            index = ourPieces[0].get(i);
            ev += pst[0].get(index) * PARAMETER_PAWN_TABLE_FACTOR;
            ev += PARAMETER_PAWN_VALUE;
            if((ourPassedPawnMask[index] & opponentPieceOccupancy[0]) == 0){
                ev += PARAMETER_PASSED_PAWN;
            }
            if((BitBoard.files_neighbour[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0){
                ev += PARAMETER_ISOLATED_PAWN;
            }
        }

        ev += PARAMETER_DOUBLED_PAWN *
              (color == 1 ?
                       BitBoard.bitCount(BitBoard.shiftNorth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]) :
                       BitBoard.bitCount(BitBoard.shiftSouth(ourPieceOccupancy[0]) & ourPieceOccupancy[0]));


        ev += PARAMETER_CONNECTED_PAWN *
              (color == 1 ?
                       (BitBoard.bitCount(BitBoard.shiftNorthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0])+
                        BitBoard.bitCount(BitBoard.shiftNorthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0])) :

                       (BitBoard.bitCount(BitBoard.shiftSouthEast(ourPieceOccupancy[0]) & ourPieceOccupancy[0])+
                        BitBoard.bitCount(BitBoard.shiftSouthWest(ourPieceOccupancy[0]) & ourPieceOccupancy[0])));

        //-------------------------ROOKS----------------------------------
        for (i = 0; i < ourPieces[1].size(); i++) {

            index = ourPieces[1].get(i);
            ev += pst[1].get(index)                          * PARAMETER_ROOK_TABLE_FACTOR;
            ev +=                                              PARAMETER_ROOK_VALUE;
            long attacks = BitBoard.lookUpRookAttack(index, totalOccupied) & ~ourTotalOccupancy;

            ev += PARAMETER_ROOK_VISIBILITY                  * BitBoard.bitCount(attacks);
            ev += PARAMETER_ROOK_VISIBILITY_PAWN_COVER       * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += PARAMETER_ROOK_TRAPPED                     * (attacks & opponentPawnCover) == attacks ? 1:0;

            ev += PARAMETER_ROOK_KING_LINE                   * ((BitBoard.lookUpRookAttack(index, 0L) & opponentPieceOccupancy[5]) > 0 ? 1 : 0);
            if((BitBoard.files[BitBoard.fileIndex(index)] & ourPieceOccupancy[0]) == 0){     //atleast half open
                if((BitBoard.files[BitBoard.fileIndex(index)] & opponentPieceOccupancy[0]) == 0){     //open
                    ev += PARAMETER_ROOK_OPEN;
                }
                ev += PARAMETER_ROOK_HALF_OPEN;
            }
        }

        //-------------------------KNIGHTS----------------------------------
        for (i = 0; i < ourPieces[2].size(); i++) {
            index = ourPieces[2].get(i);
            ev += pst[2].get(index) * PARAMETER_KNIGHT_TABLE_FACTOR;
            ev += PARAMETER_KNIGHT_VALUE;


            long attacks = BitBoard.KNIGHT_ATTACKS[index] & ~ourTotalOccupancy;
            ev += PARAMETER_KNIGHT_VISIBILITY                * BitBoard.bitCount(attacks);
            ev += PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER     * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += PARAMETER_KNIGHT_TRAPPED                   * (attacks & opponentPawnCover) == attacks ? 1:0;
        }

        //-------------------------BISHOPS-----------------------------------
        for (i = 0; i < ourPieces[3].size(); i++) {
            index = ourPieces[3].get(i);
            ev += pst[3].get(index) * PARAMETER_BISHOP_TABLE_FACTOR;
            ev += PARAMETER_BISHOP_VALUE;
            long attacks = BitBoard.lookUpBishopAttack(index, totalOccupied) & ~ourTotalOccupancy;

            ev += PARAMETER_BISHOP_VISIBILITY                * BitBoard.bitCount(attacks);
            ev += PARAMETER_BISHOP_VISIBILITY_PAWN_COVER     * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += PARAMETER_BISHOP_TRAPPED                   * (attacks & opponentPawnCover) == attacks ? 1:0;
        }
        if (ourPieces[3].size() > 1) ev += PARAMETER_DOUBLE_BISHOP;


        //-------------------------QUEENS------------------------------------
        for (i = 0; i < ourPieces[4].size(); i++) {
            index = ourPieces[4].get(i);
            ev += pst[4].get(index) * PARAMETER_QUEEN_TABLE_FACTOR;
            ev += PARAMETER_QUEEN_VALUE;

            long attacks = (BitBoard.lookUpBishopAttack(index, totalOccupied) |
                            BitBoard.lookUpRookAttack(index, totalOccupied))
                           & ~ourTotalOccupancy;

            ev += PARAMETER_QUEEN_VISIBILITY                * BitBoard.bitCount(attacks);
            ev += PARAMETER_QUEEN_VISIBILITY_PAWN_COVER     * BitBoard.bitCount(attacks & opponentPawnCover);
            ev += PARAMETER_QUEEN_TRAPPED                   * (attacks & opponentPawnCover) == attacks ? 1:0;
        }

        //-------------------------KINGS-------------------------------------
        for (i = 0; i < ourPieces[5].size(); i++) {
            index = ourPieces[5].get(i);
            //TODO
            ev += pst[5].get(index) * PARAMETER_KING_TABLE_FACTOR;
            ev += PARAMETER_KING_VALUE;
            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & ourTotalOccupancy)) *
                  PARAMETER_KING_SAFETY_1;
            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & opponentTotalOccupancy)) *
                  PARAMETER_KING_SAFETY_2;
        }
        return ev;

    }

    @Override
    public double[] getEvolvableValues() {

        return new double[]{
                PARAMETER_PAWN_TABLE_FACTOR,
                PARAMETER_ROOK_TABLE_FACTOR,
                PARAMETER_KNIGHT_TABLE_FACTOR,
                PARAMETER_BISHOP_TABLE_FACTOR,
                PARAMETER_QUEEN_TABLE_FACTOR,
                PARAMETER_KING_TABLE_FACTOR,
                PARAMETER_PAWN_VALUE,
                PARAMETER_ROOK_VALUE,
                PARAMETER_KNIGHT_VALUE,
                PARAMETER_BISHOP_VALUE,
                PARAMETER_QUEEN_VALUE,
                PARAMETER_KING_VALUE,
                //add for every attacked field
                PARAMETER_ROOK_VISIBILITY,
                PARAMETER_BISHOP_VISIBILITY,
                PARAMETER_KNIGHT_VISIBILITY,
                PARAMETER_QUEEN_VISIBILITY,
                //add for every attacked field which is covered by opponent pawns
                PARAMETER_ROOK_VISIBILITY_PAWN_COVER,
                PARAMETER_BISHOP_VISIBILITY_PAWN_COVER,
                PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER,
                PARAMETER_QUEEN_VISIBILITY_PAWN_COVER,
                //add if the amount of attacks to uncovered squares is 0
                PARAMETER_ROOK_TRAPPED,
                PARAMETER_BISHOP_TRAPPED,
                PARAMETER_KNIGHT_TRAPPED,
                PARAMETER_QUEEN_TRAPPED,
                PARAMETER_ROOK_KING_LINE,
                PARAMETER_PASSED_PAWN,
                PARAMETER_ISOLATED_PAWN,
                PARAMETER_DOUBLED_PAWN,
                PARAMETER_DOUBLE_BISHOP,
                PARAMETER_KING_SAFETY_1,       //10
                PARAMETER_KING_SAFETY_2,       //18
                PARAMETER_ROOK_HALF_OPEN,
                PARAMETER_ROOK_OPEN,
                PARAMETER_CONNECTED_PAWN,
                };
    }

    @Override
    public void setEvolvableValues(double[] ar) {

        PARAMETER_PAWN_TABLE_FACTOR=ar[0];
        PARAMETER_ROOK_TABLE_FACTOR=ar[1];
        PARAMETER_KNIGHT_TABLE_FACTOR=ar[2];
        PARAMETER_BISHOP_TABLE_FACTOR=ar[3];
        PARAMETER_QUEEN_TABLE_FACTOR=ar[4];
        PARAMETER_KING_TABLE_FACTOR=ar[5];


        PARAMETER_PAWN_VALUE=ar[6];
        PARAMETER_ROOK_VALUE=ar[7];
        PARAMETER_KNIGHT_VALUE=ar[8];
        PARAMETER_BISHOP_VALUE=ar[9];
        PARAMETER_QUEEN_VALUE=ar[10];
        PARAMETER_KING_VALUE=ar[11];

        //add for every attacked field
        PARAMETER_ROOK_VISIBILITY=ar[12];
        PARAMETER_BISHOP_VISIBILITY=ar[13];
        PARAMETER_KNIGHT_VISIBILITY=ar[14];
        PARAMETER_QUEEN_VISIBILITY=ar[15];

        //add for every attacked field which is covered by opponent pawns
        PARAMETER_ROOK_VISIBILITY_PAWN_COVER=ar[16];
        PARAMETER_BISHOP_VISIBILITY_PAWN_COVER=ar[17];
        PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER=ar[18];
        PARAMETER_QUEEN_VISIBILITY_PAWN_COVER=ar[19];

        //add if the amount of attacks to uncovered squares is 0
        PARAMETER_ROOK_TRAPPED=ar[20];
        PARAMETER_BISHOP_TRAPPED=ar[21];
        PARAMETER_KNIGHT_TRAPPED=ar[22];
        PARAMETER_QUEEN_TRAPPED=ar[23];

        PARAMETER_ROOK_KING_LINE=ar[24];

        PARAMETER_PASSED_PAWN=ar[25];
        PARAMETER_ISOLATED_PAWN=ar[26];
        PARAMETER_DOUBLED_PAWN=ar[27];
        PARAMETER_DOUBLE_BISHOP=ar[28];
        PARAMETER_KING_SAFETY_1=ar[29];       //10
        PARAMETER_KING_SAFETY_2=ar[30];       //18
        PARAMETER_ROOK_HALF_OPEN=ar[31];
        PARAMETER_ROOK_OPEN=ar[32];
        PARAMETER_CONNECTED_PAWN=ar[33];

        
    }

    @Override
    public AdvancedEndGameEvaluator copy() {
        AdvancedEndGameEvaluator evaluator = new AdvancedEndGameEvaluator();
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }

    public static void main(String[] args) {
        AdvancedEndGameEvaluator eval = new AdvancedEndGameEvaluator();
        FastBoard fb = new FastBoard();
        fb = IO.read_FEN(fb,"8/1b1r1k2/p2pP3/2pp1QN1/p2pNN2/2KNNB2/2NNR3/2N5 w - - 0 1");
        System.out.println(eval.evaluate(fb));
    }
}
