package ai.evaluator;

import board.Board;
import ai.tools.tensor.Tensor2D;
import ai.tools.tensor.Tensor3D;

import static ai.evaluator.FinnEvaluator.*;

public class LateGameEvaluator implements Evaluator {

    public static final Tensor2D PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {200, 200, 200, 200, 200, 200, 200, 200},
            {100, 100, 100, 100, 100, 100, 100, 100},
            {30,  30, 40, 50, 50, 40,  30,  30},
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
            {-50, -40, -30, -30, -30, -30, -40, -50},
            {-40, -20, 0, 0, 0, 0, -20, -40},
            {-30, 0, 10, 15, 15, 10, 0, -30},
            {-30, 5, 15, 20, 20, 15, 5, -30},
            {-30, 0, 15, 20, 20, 15, 0, -30},
            {-30, 5, 10, 15, 15, 10, 5, -30},
            {-40, -20, 0, 5, 5, 0, -20, -40},
            {-50, -40, -30, -30, -30, -30, -40, -50},
    });
    public static final Tensor2D QUEEN_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -5, -5, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-5, 0, 5, 10, 10, 5, 0, -5},
            {0, 0, 5, 10, 10, 5, 0, -5},
            {-10, 5, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 0, 0, 0, 0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}
    });

    public static final Tensor2D KING_VALUES_LATE = new Tensor2D(new double[][]{
            {-30, -30, -30, -30, -30, -30, -30, -30},
            {-30, -10, -10, -10, -10, -10, -10, -30},
            {-30, -10, 30, 30, 30, 30, -10, -30},
            {-30, -10, 30, 30, 30, 30, -10, -30},
            {-30, -10, 30, 30, 30, 30, -10, -30},
            {-30, -10, 30, 30, 30, 30, -10, -30},
            {-30, -10, -10, -10, -10, -10, -10, -30},
            {-30, -30, -30, -30, -30, -30, -30, -30}
    });

    public static final double INFTY = 1E8;

    public static final int[] EVALUATE_PRICE = new int[]{0, 100, 500, 320, 330, 900, 20000};
    //public static final Tensor3D POSITION_PRICE = new Tensor3D(PAWN_VALUES, ROOK_VALUES, KNIGHT_VALUES, BISHOP_VALUES, QUEEN_VALUES);
    public static final Tensor3D POSITION_PRICE =
            new Tensor3D(
                    addScalarToTensor(negateTensor(KING_VALUES_LATE), -EVALUATE_PRICE[6]),
                    addScalarToTensor(negateTensor(QUEEN_VALUES), -EVALUATE_PRICE[5]),
                    addScalarToTensor(negateTensor(BISHOP_VALUES), -EVALUATE_PRICE[4]),
                    addScalarToTensor(negateTensor(KNIGHT_VALUES), -EVALUATE_PRICE[3]),
                    addScalarToTensor(negateTensor(ROOK_VALUES), -EVALUATE_PRICE[2]),
                    addScalarToTensor(negateTensor(PAWN_VALUES), -EVALUATE_PRICE[1]),
                    addScalarToTensor(negateTensor(KING_VALUES_LATE), 0),
                    addScalarToTensor(flipTensor(PAWN_VALUES), EVALUATE_PRICE[1]),
                    addScalarToTensor(flipTensor(ROOK_VALUES), EVALUATE_PRICE[2]),
                    addScalarToTensor(flipTensor(KNIGHT_VALUES), EVALUATE_PRICE[3]),
                    addScalarToTensor(flipTensor(BISHOP_VALUES), EVALUATE_PRICE[4]),
                    addScalarToTensor(flipTensor(QUEEN_VALUES), EVALUATE_PRICE[5]),
                    addScalarToTensor(flipTensor(KING_VALUES_LATE), EVALUATE_PRICE[6]));

    @Override
    public double evaluate(Board board) {

        if(board.isGameOver()){
            switch (board.winner()){
                case 1: return INFTY;
                case 0: return 0;
                case -1: return -INFTY;
            }
        }

        int ev = 0;
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {

                int v = ((board.getPiece((byte) i, (byte) n)));
                int b = v > 0 ? 1 : -1;

                if (v != 0) {
                    ev += b * EVALUATE_PRICE[Math.abs(v)];

                    if (Math.abs(v) < 6) {
                        if (v > 0) {
                            ev += (b * POSITION_PRICE.get(Math.abs(v) - 1,7-n,i));
                        } else {
                            ev += (b * POSITION_PRICE.get(Math.abs(v) - 1,n,i));
                        }
                    } else {
                        if (v > 0) {
                            ev += (b * KING_VALUES_LATE.get(7-n,i));
                        } else {
                            ev += (b * KING_VALUES_LATE.get(n,i));
                        }
                    }
                }
            }
        }
        return ev;
    }
}
