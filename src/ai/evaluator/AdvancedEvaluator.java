package ai.evaluator;

import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import ai.tools.tensor.Tensor1D;
import io.IO;

public class AdvancedEvaluator extends GeneticEvaluator<AdvancedEvaluator> implements Evaluator {

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
    
    public static final Tensor1D KING_VALUES_MID_BLACK = flipTensor(KING_VALUES_MID_WHITE);
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
    private double PARAMETER_PAWN_TABLE_FACTOR      = 100;
    private double PARAMETER_ROOK_TABLE_FACTOR      = 100;
    private double PARAMETER_KNIGHT_TABLE_FACTOR    = 100;
    private double PARAMETER_BISHOP_TABLE_FACTOR    = 100;
    private double PARAMETER_QUEEN_TABLE_FACTOR     = 100;
    private double PARAMETER_KING_TABLE_FACTOR      = 100;


    private double PARAMETER_PAWN_VALUE             = 100;
    private double PARAMETER_ROOK_VALUE             = 500;
    private double PARAMETER_KNIGHT_VALUE           = 320;
    private double PARAMETER_BISHOP_VALUE           = 330;
    private double PARAMETER_QUEEN_VALUE            = 900;
    private double PARAMETER_KING_VALUE             = 20000;

    private double PARAMETER_ROOK_VISIBILTY         = 5;
    private double PARAMETER_BISHOP_VISIBILTY       = 5;

    private double PARAMETER_ROOK_VISIBILTY_PAWN_COVER           = -10;
    private double PARAMETER_BISHOP_VISIBILTY_PAWN_COVER         = -10;

    private double PARAMETER_ROOK_KING_LINE         = 40;

    private double PARAMETER_PASSED_PAWN        = 25;
    private double PARAMETER_ISOLATED_PAWN      = -25;
    private double PARAMETER_DOUBLED_PAWN       = -35;
    private double PARAMETER_DOUBLE_BISHOP      = 57;
    private double PARAMETER_KING_SAFETY_1      = 10;       //10
    private double PARAMETER_KING_SAFETY_2      = 18;       //18
    private double PARAMETER_ROOK_HALF_OPEN     = 10;
    private double PARAMETER_ROOK_OPEN          = 25;
    private double PARAMETER_CONNECTED_PAWN     = 10;

    //will get to this later. For now, I'm using this file to store position values
    @Override
    public double evaluate(Board board) {

        FastBoard fb = (FastBoard) board;

        int numWhiteBishops = 0;
        int numBlackBishops = 0;


        double ev = 0;
        int v;

        int i;
        int index;


        long whitePawnCover = BitBoard.shiftNorthWest(fb.getWhite_values()[0]) | BitBoard.shiftNorthEast(fb.getWhite_values()[0]);
        long blackPawnCover = BitBoard.shiftSouthWest(fb.getBlack_values()[0]) | BitBoard.shiftSouthEast(fb.getBlack_values()[0]);

        long whitePawnUncovered = ~whitePawnCover;
        long blackPawnUncovered = ~blackPawnCover;

        numWhiteBishops = fb.getWhite_pieces()[3].size();
        numBlackBishops = fb.getBlack_pieces()[3].size();


        //-----------------------white evaluation-----------------------------------------------------

        for (i = 0; i < fb.getWhite_pieces()[0].size(); i++) {
            index = fb.getWhite_pieces()[0].get(i);
            ev += PAWN_VALUES_WHITE.get(index) * PARAMETER_PAWN_TABLE_FACTOR;
            ev += PARAMETER_PAWN_VALUE;
            if((BitBoard.whitePassedPawnMask[index] & fb.getBlack_values()[0]) == 0){
                ev += PARAMETER_PASSED_PAWN;
            }
            if((BitBoard.files_neighbour[BitBoard.fileIndex(index)] & fb.getWhite_values()[0]) == 0){
                ev += PARAMETER_ISOLATED_PAWN;
            }
        }
        for (i = 0; i < fb.getWhite_pieces()[1].size(); i++) {
            index = fb.getWhite_pieces()[1].get(i);
            ev += ROOK_VALUES_WHITE.get(index) * PARAMETER_ROOK_TABLE_FACTOR;
            ev += PARAMETER_ROOK_VALUE;
            long attacks = BitBoard.lookUpRookAttack(index, fb.getOccupied());
            ev += PARAMETER_ROOK_VISIBILTY * BitBoard.bitCount(attacks & ~fb.getTeam_total()[0]);
            ev += PARAMETER_ROOK_VISIBILTY_PAWN_COVER * BitBoard.bitCount(attacks & blackPawnCover);
            ev += PARAMETER_ROOK_KING_LINE * ((BitBoard.lookUpRookAttack(index, 0L) & fb.getBlack_values()[5]) > 0 ? 1 : 0);
            if((BitBoard.files[BitBoard.fileIndex(index)] & fb.getWhite_values()[0]) == 0){     //atleast half open
                if((BitBoard.files[BitBoard.fileIndex(index)] & fb.getBlack_values()[0]) == 0){     //open
                    ev += PARAMETER_ROOK_OPEN;
                }
                ev += PARAMETER_ROOK_HALF_OPEN;
            }
        }
        for (i = 0; i < fb.getWhite_pieces()[2].size(); i++) {
            index = fb.getWhite_pieces()[2].get(i);
            ev += KNIGHT_VALUES_WHITE.get(index) * PARAMETER_KNIGHT_TABLE_FACTOR;
            ev += PARAMETER_KNIGHT_VALUE;
        }
        for (i = 0; i < fb.getWhite_pieces()[3].size(); i++) {
            index = fb.getWhite_pieces()[3].get(i);
            ev += BISHOP_VALUES_WHITE.get(index) * PARAMETER_BISHOP_TABLE_FACTOR;
            ev += PARAMETER_BISHOP_VALUE;
            long attacks = BitBoard.lookUpBishopAttack(index, fb.getOccupied());

            ev += PARAMETER_BISHOP_VISIBILTY                * BitBoard.bitCount(attacks & ~fb.getTeam_total()[0]);
            ev += PARAMETER_BISHOP_VISIBILTY_PAWN_COVER     * BitBoard.bitCount(attacks & blackPawnCover);
        }
        for (i = 0; i < fb.getWhite_pieces()[4].size(); i++) {
            index = fb.getWhite_pieces()[4].get(i);
            ev += QUEEN_VALUES_WHITE.get(index) * PARAMETER_QUEEN_TABLE_FACTOR;
            ev += PARAMETER_QUEEN_VALUE;
        }
        for (i = 0; i < fb.getWhite_pieces()[5].size(); i++) {
            index = fb.getWhite_pieces()[5].get(i);
            ev += (board.isEndgame() ? KING_VALUES_LATE_WHITE.get(index) : KING_VALUES_MID_WHITE.get(index)) * PARAMETER_KING_TABLE_FACTOR;
            ev += PARAMETER_KING_VALUE;
            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[0])) *
                  PARAMETER_KING_SAFETY_1;
            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[1])) *
                  PARAMETER_KING_SAFETY_2;
        }













        //-----------------------black evaluation-----------------------------------------------------



        for (i = 0; i < fb.getBlack_pieces()[0].size(); i++) {
            index = fb.getBlack_pieces()[0].get(i);
            ev -= PAWN_VALUES_BLACK.get(index) * PARAMETER_PAWN_TABLE_FACTOR;
            ev -= PARAMETER_PAWN_VALUE;
            if((BitBoard.blackPassedPawnMask[index] & fb.getWhite_values()[0]) == 0){
                ev -= PARAMETER_PASSED_PAWN;
            }
            if((BitBoard.files_neighbour[BitBoard.fileIndex(index)] & fb.getBlack_values()[0]) == 0){
                ev -= PARAMETER_ISOLATED_PAWN;
            }
        }
        for (i = 0; i < fb.getBlack_pieces()[1].size(); i++) {
            index = fb.getBlack_pieces()[1].get(i);
            ev -= ROOK_VALUES_BLACK.get(index) * PARAMETER_ROOK_TABLE_FACTOR;
            ev -= PARAMETER_ROOK_VALUE;
            long attacks = BitBoard.lookUpRookAttack(index, fb.getOccupied());
            ev -= PARAMETER_ROOK_VISIBILTY * BitBoard.bitCount(attacks & ~fb.getTeam_total()[1]);
            ev -= PARAMETER_ROOK_VISIBILTY_PAWN_COVER * BitBoard.bitCount(attacks & whitePawnCover);
            ev -= PARAMETER_ROOK_KING_LINE * ((BitBoard.lookUpRookAttack(index, 0L) & fb.getWhite_values()[5]) > 0 ? 1 : 0);
            if((BitBoard.files[BitBoard.fileIndex(index)] & fb.getBlack_values()[0]) == 0){         //atleast half open
                if((BitBoard.files[BitBoard.fileIndex(index)] & fb.getWhite_values()[0]) == 0){     //open
                    ev -= PARAMETER_ROOK_OPEN;
                }
                ev -= PARAMETER_ROOK_HALF_OPEN;
            }
        }
        for (i = 0; i < fb.getBlack_pieces()[2].size(); i++) {
            index = fb.getBlack_pieces()[2].get(i);
            ev -= KNIGHT_VALUES_BLACK.get(index) * PARAMETER_KNIGHT_TABLE_FACTOR;
            ev -= PARAMETER_KNIGHT_VALUE;
        }
        for (i = 0; i < fb.getBlack_pieces()[3].size(); i++) {
            index = fb.getBlack_pieces()[3].get(i);
            ev -= BISHOP_VALUES_BLACK.get(index) * PARAMETER_BISHOP_TABLE_FACTOR;
            ev -= PARAMETER_BISHOP_VALUE;
            long attacks = BitBoard.lookUpBishopAttack(index, fb.getOccupied());
            ev -= PARAMETER_BISHOP_VISIBILTY                * BitBoard.bitCount(attacks & ~fb.getTeam_total()[1]);
            ev -= PARAMETER_BISHOP_VISIBILTY_PAWN_COVER     * BitBoard.bitCount(attacks & whitePawnCover);
        }
        for(i = 0; i < fb.getBlack_pieces()[4].size(); i ++) {
            index = fb.getBlack_pieces()[4].get(i);
            ev -= QUEEN_VALUES_BLACK.get(index) * PARAMETER_QUEEN_TABLE_FACTOR;
            ev -= PARAMETER_QUEEN_VALUE;
        }
        for(i = 0; i < fb.getBlack_pieces()[5].size(); i ++) {
            index = fb.getBlack_pieces()[5].get(i);

            ev -= (board.isEndgame() ? KING_VALUES_LATE_BLACK.get(index) : KING_VALUES_MID_BLACK.get(index)) * PARAMETER_KING_TABLE_FACTOR;
            //ev -= KING_VALUES_MID_BLACK.get(index) * PARAMETER_KING_TABLE_FACTOR;
            ev -= PARAMETER_KING_VALUE;
            ev -= (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[1])) *
                    PARAMETER_KING_SAFETY_1;
            ev -= (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[0])) *
                    PARAMETER_KING_SAFETY_2;
        }

        //bishop pair
        if (numWhiteBishops > 1) ev += PARAMETER_DOUBLE_BISHOP;
        if (numBlackBishops > 1) ev -= PARAMETER_DOUBLE_BISHOP;

        ev += PARAMETER_DOUBLED_PAWN *
              BitBoard.bitCount(BitBoard.shiftNorth(fb.getWhite_values()[0]) & fb.getWhite_values()[0]);
        ev -= PARAMETER_DOUBLED_PAWN *
              BitBoard.bitCount(BitBoard.shiftSouth(fb.getBlack_values()[0]) & fb.getBlack_values()[0]);

        ev += PARAMETER_CONNECTED_PAWN *
                (BitBoard.bitCount(BitBoard.shiftNorthEast(fb.getWhite_values()[0]) & fb.getWhite_values()[0])+
                BitBoard.bitCount(BitBoard.shiftNorthWest(fb.getWhite_values()[0]) & fb.getWhite_values()[0]));
        ev -= PARAMETER_CONNECTED_PAWN *
                (BitBoard.bitCount(BitBoard.shiftSouthEast(fb.getBlack_values()[0]) & fb.getBlack_values()[0])+
                BitBoard.bitCount(BitBoard.shiftSouthWest(fb.getBlack_values()[0]) & fb.getBlack_values()[0]));

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
                PARAMETER_ROOK_VISIBILTY,
                PARAMETER_BISHOP_VISIBILTY,
                PARAMETER_ROOK_VISIBILTY_PAWN_COVER,
                PARAMETER_BISHOP_VISIBILTY_PAWN_COVER,
                PARAMETER_ROOK_KING_LINE,
                PARAMETER_PASSED_PAWN,
                PARAMETER_ISOLATED_PAWN,
                PARAMETER_DOUBLED_PAWN,
                PARAMETER_DOUBLE_BISHOP,
                PARAMETER_KING_SAFETY_1,
                PARAMETER_KING_SAFETY_2,
                PARAMETER_ROOK_HALF_OPEN,
                PARAMETER_ROOK_OPEN,
                PARAMETER_CONNECTED_PAWN,
        };
    }

    @Override
    public void setEvolvableValues(double[] ar) {
        PARAMETER_PAWN_TABLE_FACTOR = ar[0];
        PARAMETER_ROOK_TABLE_FACTOR = ar[1];
        PARAMETER_KNIGHT_TABLE_FACTOR = ar[2];
        PARAMETER_BISHOP_TABLE_FACTOR = ar[3];
        PARAMETER_QUEEN_TABLE_FACTOR = ar[4];
        PARAMETER_KING_TABLE_FACTOR = ar[5];
        PARAMETER_PAWN_VALUE = ar[6];
        PARAMETER_ROOK_VALUE = ar[7];
        PARAMETER_KNIGHT_VALUE = ar[8];
        PARAMETER_BISHOP_VALUE = ar[9];
        PARAMETER_QUEEN_VALUE = ar[10];
        PARAMETER_KING_VALUE = ar[11];
        PARAMETER_ROOK_VISIBILTY = ar[12];
        PARAMETER_BISHOP_VISIBILTY = ar[13];
        PARAMETER_ROOK_VISIBILTY_PAWN_COVER = ar[14];
        PARAMETER_BISHOP_VISIBILTY_PAWN_COVER = ar[15];
        PARAMETER_ROOK_KING_LINE = ar[16];
        PARAMETER_PASSED_PAWN = ar[17];
        PARAMETER_ISOLATED_PAWN = ar[18];
        PARAMETER_DOUBLED_PAWN = ar[19];
        PARAMETER_DOUBLE_BISHOP = ar[20];
        PARAMETER_KING_SAFETY_1 = ar[21];
        PARAMETER_KING_SAFETY_2 = ar[22];
        PARAMETER_ROOK_HALF_OPEN = ar[23];
        PARAMETER_ROOK_OPEN = ar[24];
        PARAMETER_CONNECTED_PAWN = ar[25];
    }

    @Override
    public AdvancedEvaluator copy() {
        AdvancedEvaluator evaluator = new AdvancedEvaluator();
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }

    public static void main(String[] args) {
        AdvancedEvaluator eval = new AdvancedEvaluator();
        FastBoard fb = new FastBoard();
        fb = IO.read_FEN(fb,"rnbq1k1r/pppppppp/4bn2/8/8/4BN2/PPPPPPPP/RNBQK2R w KQkq - 0 1");
        System.out.println(eval.evaluate(fb));
    }
}
