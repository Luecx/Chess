package ai.evaluator;

import board.Board;
import board.setup.Setup;
import ai.tools.tensor.Tensor2D;
import ai.tools.tensor.Tensor3D;
import io.IO;

public class FinnEvaluator implements Evaluator {

    public static final Tensor2D PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5, -5,-10,  0,  0,-10, -5,  5},
            {5, 10, 10,-20,-20, 10, 10,  5},
            {0, 0, 0, 0, 0, 0, 0, 0}
    });

    public static final Tensor2D BISHOP_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -10, -10, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 5, 5, 10, 10, 5, 5, -10},
            {-10, 0, 10, 10, 10, 10, 0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 5, 0, 0, 0, 0, 5, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20,},
    });

    public static final Tensor2D ROOK_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 20, 20, 20, 20, 20, 20, 5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {0, 0, 0, 5, 5, 0, 0, 0}
    });

    public static final Tensor2D KNIGHT_VALUES = new Tensor2D(new double[][]{
            {-20, -16, -12, -12, -12, -12, -16, -20},
            {-8, -4, 0, 0, 0, 0, -4,-8 },
            {-12, 4, 8, 12, 12, 12, 4, -12},
            {-12, 2, 6, 10, 10, 6, 2, -12},
            {-12, 2, 6, 10, 10, 6, 2, -12},
            {-6, 10, 8, 6, 6, 8, 2, -6},
            {-16, -8, 0, 2, 2, 0, -8, -16},
            {-24, -50, -12, -12, -12, -12, -50, -24},
    });
    public static final Tensor2D QUEEN_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -5, -5, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-5, 0, 5, 5, 5, 5, 0, -5},
            {0, 0, 5, 5, 5, 5, 0, -5},
            {-10, 5, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 0, 0, 0, 0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}
    });

    public static final Tensor2D KING_VALUES_MID = new Tensor2D(new double[][]{
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {20, 20, 0, -50, -50, -50, 20, 20},
            {20, 30, 10, -50, 0, -50, 30, 20}
    });


    public static Tensor2D addScalarToTensor(Tensor2D tensor2D, double scalar){
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {
                tensor2D.add(scalar, i,n);
            }
        }
        return tensor2D;
    }

    public static Tensor2D negateTensor(Tensor2D tensor2D){
        Tensor2D tensor2D1 = new Tensor2D(tensor2D);
        tensor2D1.scale(-1);
        return tensor2D1;
    }

    public static Tensor2D flipTensor(Tensor2D tensor2D){
        Tensor2D res = new Tensor2D(8,8);
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {
                res.set(tensor2D.get(7-i, n), i,n);
            }
        }
        return res;
    }

    //pawn, rook,knight,bishop,queen,king
    public static final int[] EVALUATE_PRICE = new int[]{0, 100, 500, 320, 330, 900, 20000};
    public static final Tensor3D POSITION_PRICE =
            new Tensor3D(
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




    @Override
    public double evaluate(Board board) {


        int ev = 0;
        int v;
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {

                v = board.getPiece(i, n);
                if(v == 0) continue;

                ev += POSITION_PRICE.get(v+6, n,i);
            }
        }
        return ev;
    }
}
