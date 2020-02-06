package game.ai.evaluator;

import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.pieces.PieceList;
import game.ai.tools.tensor.Tensor;
import game.ai.tools.tensor.Tensor1D;
import game.ai.tools.tensor.Tensor2D;
import game.ai.tools.tensor.Tensor3D;

import static game.ai.evaluator.FinnEvaluator.flipTensor;

public class NoahEvaluator2 extends GeneticEvaluator<NoahEvaluator2> implements Evaluator {

    public static final Tensor1D PAWN_VALUES = new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0, 0, 0, 0, 0, 0, 0, 0});

    public static final Tensor1D BISHOP_VALUES = new Tensor1D(new double[]{
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20,
    });

    public static final Tensor1D ROOK_VALUES = new Tensor1D(new double[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 20, 20, 20, 20, 20, 20, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0
    });

    public static final Tensor1D KNIGHT_VALUES = new Tensor1D(new double[]{
            -20, -16, -12, -12, -12, -12, -16, -20,
            -8, -4, 0, 0, 0, 0, -4,-8 ,
            -12, 4, 8, 12, 12, 12, 4, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -12, 2, 6, 10, 10, 6, 2, -12,
            -6, 10, 8, 6, 6, 8, 2, -6,
            -16, -8, 0, 2, 2, 0, -8, -16,
            -24, -50, -12, -12, -12, -12, -50, -24,
    });
    public static final Tensor1D QUEEN_VALUES = new Tensor1D(new double[]{
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
    });

    public static final Tensor1D KING_VALUES_MID = new Tensor1D(new double[]{
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, -50, -50, -50, 0, 20, 20,
            20, 30, -50, -50, 0, 10, 30, 20
    });


    public static Tensor1D addScalarToTensor(Tensor1D tensor2D, double scalar){
        for (int i = 0; i < 64; i++) {
            tensor2D.add(scalar, i);
        }
        return tensor2D;
    }

    public static Tensor1D negateTensor(Tensor1D tensor2D){
        Tensor1D tensor2D1 = new Tensor1D(tensor2D);
        System.out.println(tensor2D1);
        tensor2D1.scale(-1);
        System.out.println(tensor2D1);
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
    public static final Tensor2D POSITION_PRICE =
            new Tensor2D(
                    addScalarToTensor(negateTensor(KING_VALUES_MID), -EVALUATE_PRICE[6]),
                    addScalarToTensor(negateTensor(QUEEN_VALUES), -EVALUATE_PRICE[5]),
                    addScalarToTensor(negateTensor(BISHOP_VALUES), -EVALUATE_PRICE[4]),
                    addScalarToTensor(negateTensor(KNIGHT_VALUES), -EVALUATE_PRICE[3]),
                    addScalarToTensor(negateTensor(ROOK_VALUES), -EVALUATE_PRICE[2]),
                    addScalarToTensor(negateTensor(PAWN_VALUES), -EVALUATE_PRICE[1]),
                    addScalarToTensor(negateTensor(KING_VALUES_MID), 0),
                    addScalarToTensor(flipTensor(PAWN_VALUES), EVALUATE_PRICE[1]),
                    addScalarToTensor(flipTensor(ROOK_VALUES), EVALUATE_PRICE[2]),
                    addScalarToTensor(flipTensor(KNIGHT_VALUES), EVALUATE_PRICE[3]),
                    addScalarToTensor(flipTensor(BISHOP_VALUES), EVALUATE_PRICE[4]),
                    addScalarToTensor(flipTensor(QUEEN_VALUES), EVALUATE_PRICE[5]),
                    addScalarToTensor(flipTensor(KING_VALUES_MID), EVALUATE_PRICE[6]));




    public static final Tensor2D midValue = POSITION_PRICE;
    //public static final Tensor2D endValue = LateGameEvaluator.POSITION_PRICE;


    private double PARAMATER_PASSED_PAWN        = 25;
    private double PARAMATER_ISOLATED_PAWN      = -25;
    private double PARAMATER_DOUBLED_PAWN       = -35;
    private double PARAMATER_DOUBLE_BISHOP      = 50;
    private double PARAMETER_KING_SAFETY_1      = 10;
    private double PARAMETER_KING_SAFETY_2      = -10;
    private double PARAMETER_ROOK_HALF_OPEN     = 10;
    private double PARAMETER_ROOK_OPEN          = 25;
    private double PARAMETER_CONNECTED_PAWN     = 0;

    //will get to this later. For now, I'm using this file to store position values
    @Override
    public double evaluate(Board board) {

        FastBoard fb = (FastBoard) board;

        if (fb.isGameOver()) {
            switch (board.winner()) {
                case 1:
                    return LateGameEvaluator.INFTY-1;
                case 0:
                    return 0;
                case -1:
                    return -(LateGameEvaluator.INFTY-1);
            }
        }
        Tensor2D pieceValue = midValue;

//        if (board.isEndgame()) {
//            pieceValue = endValue;
//        }

        int numWhiteBishops = 0;
        int numBlackBishops = 0;

//        //recording the file the rook is on
//        int[] whiteRooks = {-1,-1};
//        int[] blackRooks = {-1,-1};
//
//        //pawn rook knight bishop queen king
//
//        int[] wPawns = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        int[] bPawns = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//
//        int lastWPawnFile = -1;
//        int lastBPawnFile = -1;

        double ev = 0;
        int v;

        int i;
        int index;


        numWhiteBishops = fb.getWhite_pieces()[3].size();
        numBlackBishops = fb.getBlack_pieces()[3].size();

        for(i = 0; i < fb.getWhite_pieces()[0].size(); i ++) {
            index = fb.getWhite_pieces()[0].get(i);
            ev += pieceValue.get(7, index);
        }
        for(i = 0; i < fb.getWhite_pieces()[1].size(); i ++) {
            index = fb.getWhite_pieces()[1].get(i);
            ev += pieceValue.get(8, index);
        }for(i = 0; i < fb.getWhite_pieces()[2].size(); i ++) {
            index = fb.getWhite_pieces()[2].get(i);
            ev += pieceValue.get(9, index);
        }for(i = 0; i < fb.getWhite_pieces()[3].size(); i ++) {
            index = fb.getWhite_pieces()[3].get(i);
            ev += pieceValue.get(10, index);
        }for(i = 0; i < fb.getWhite_pieces()[4].size(); i ++) {
            index = fb.getWhite_pieces()[4].get(i);
            ev += pieceValue.get(11, index);
        }for(i = 0; i < fb.getWhite_pieces()[5].size(); i ++) {
            index = fb.getWhite_pieces()[5].get(i);
            ev += pieceValue.get(12, index);
            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[0])) *
                    PARAMETER_KING_SAFETY_1;
            ev += (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[1])) *
                    PARAMETER_KING_SAFETY_2;
        }
        for(i = 0; i < fb.getBlack_pieces()[0].size(); i ++) {
            index = fb.getBlack_pieces()[0].get(i);
            ev += pieceValue.get(5, index);
        }
        for(i = 0; i < fb.getBlack_pieces()[1].size(); i ++) {
            index = fb.getBlack_pieces()[1].get(i);
            ev += pieceValue.get(4, index);
        }for(i = 0; i < fb.getBlack_pieces()[2].size(); i ++) {
            index = fb.getBlack_pieces()[2].get(i);
            ev += pieceValue.get(3, index);
        }for(i = 0; i < fb.getBlack_pieces()[3].size(); i ++) {
            index = fb.getBlack_pieces()[3].get(i);
            ev += pieceValue.get(2, index);
        }for(i = 0; i < fb.getBlack_pieces()[4].size(); i ++) {
            index = fb.getBlack_pieces()[4].get(i);
            ev += pieceValue.get(1, index);
        }for(i = 0; i < fb.getBlack_pieces()[5].size(); i ++) {
            index = fb.getBlack_pieces()[5].get(i);
            ev += pieceValue.get(0, index);
            ev -= (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[1])) *
                    PARAMETER_KING_SAFETY_1;
            ev -= (BitBoard.bitCount(BitBoard.KING_ATTACKS[index] & fb.getTeam_total()[0])) *
                    PARAMETER_KING_SAFETY_2;
        }


        //bishop pair
        if (numWhiteBishops > 1) ev += PARAMATER_DOUBLE_BISHOP;
        if (numBlackBishops > 1) ev -= PARAMATER_DOUBLE_BISHOP;

        ev -= PARAMATER_DOUBLED_PAWN *
                BitBoard.bitCount(BitBoard.shiftNorth(fb.getWhite_values()[0]) & fb.getWhite_values()[0]);
        ev += PARAMATER_DOUBLED_PAWN *
                BitBoard.bitCount(BitBoard.shiftSouth(fb.getBlack_values()[0]) & fb.getBlack_values()[0]);


        ev += PARAMETER_CONNECTED_PAWN *
                (BitBoard.bitCount(BitBoard.shiftNorthEast(fb.getWhite_values()[0]) & fb.getWhite_values()[0])+
                BitBoard.bitCount(BitBoard.shiftNorthWest(fb.getWhite_values()[0]) & fb.getWhite_values()[0]));

        ev -= PARAMETER_CONNECTED_PAWN *
                (BitBoard.bitCount(BitBoard.shiftSouthEast(fb.getBlack_values()[0]) & fb.getBlack_values()[0])+
                BitBoard.bitCount(BitBoard.shiftSouthWest(fb.getBlack_values()[0]) & fb.getBlack_values()[0]));
        
        //pawns
//        for (int rank = 1; rank < 9; rank++) {
//            //doubled
//            if (wPawns[rank] > 1) ev += PARAMATER_DOUBLED_PAWN;
//            if (bPawns[rank] > 1) ev -= PARAMATER_DOUBLED_PAWN;
//
//            if (wPawns[rank] > 0) {
//                //passed
//                if (bPawns[rank - 1] == 0 && bPawns[rank] == 0 && bPawns[rank + 1] == 0) {
//                    ev += PARAMATER_PASSED_PAWN;
//                }
//                //isolated
//                if (wPawns[rank - 1] == 0 && wPawns[rank + 1] == 0) ev += PARAMATER_ISOLATED_PAWN;
//            }
//            if (bPawns[rank] > 0) {
//                //passed
//                if (wPawns[rank - 1] == 0 && wPawns[rank] == 0 && wPawns[rank + 1] == 0) {
//                    ev -= PARAMATER_PASSED_PAWN;
//                }
//                //isolated
//                if (bPawns[rank - 1] == 0 && bPawns[rank + 1] == 0) ev -= PARAMATER_ISOLATED_PAWN;
//            }
//        }
        /// rooks


//        for (int file : whiteRooks) {
//            if (file == -1) {
//                continue;
//            }
//            if (wPawns[file]  == 0) {
//                ev += PARAMETER_ROOK_HALF_OPEN;
//                if (bPawns[file] == 0) {
//                    ev += PARAMETER_ROOK_OPEN;
//                }
//            }
//        }
//        for (int file : blackRooks) {
//            if (file == -1) {
//                continue;
//            }
//            if (bPawns[file]  == 0) {
//                ev -= PARAMETER_ROOK_HALF_OPEN;
//                if (wPawns[file] == 0) {
//                    ev -= PARAMETER_ROOK_OPEN;
//                }
//            }
//        }


        return ev;
    }

    @Override
    public double[] getEvolvableValues() {
        return new double[]{
                PARAMATER_PASSED_PAWN,
                PARAMATER_ISOLATED_PAWN,
                PARAMATER_DOUBLED_PAWN,
                PARAMATER_DOUBLE_BISHOP,
                PARAMETER_KING_SAFETY_1,
                PARAMETER_KING_SAFETY_2,
                PARAMETER_ROOK_OPEN,
                PARAMETER_ROOK_HALF_OPEN};
    }

    @Override
    public void setEvolvableValues(double[] ar) {
        PARAMATER_PASSED_PAWN = ar[0];
        PARAMATER_ISOLATED_PAWN = ar[1];
        PARAMATER_DOUBLED_PAWN = ar[2];
        PARAMATER_DOUBLE_BISHOP = ar[3];
        PARAMETER_KING_SAFETY_1 = ar[4];
        PARAMETER_KING_SAFETY_2 = ar[5];
        PARAMETER_ROOK_OPEN = ar[6];
        PARAMETER_ROOK_HALF_OPEN = ar[7];
    }

    @Override
    public NoahEvaluator2 copy() {
        NoahEvaluator2 evaluator = new NoahEvaluator2();
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }
}
