package game.ai.evaluator;

import board.Board;
import game.ai.tools.tensor.Tensor2D;
import game.ai.tools.tensor.Tensor3D;

public class NoahEvaluator implements Evaluator{

    public static final Tensor2D B_PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5, -5,-10,  0,  0,-10, -5,  5},
            {5, 10, 10,-20,-20, 10, 10,  5},
            {0, 0, 0, 0, 0, 0, 0, 0}
    });
    public static final Tensor2D W_PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 10, 10,-20,-20, 10, 10,  5},
            {5, -5,-10,  0,  0,-10, -5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {0, 0, 0, 0, 0, 0, 0, 0},
    });

    public static final Tensor2D B_BISHOP_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -10, -10, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 5, 5, 10, 10, 5, 5, -10},
            {-10, 0, 10, 10, 10, 10, 0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 5, 0, 0, 0, 0, 5, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20,},
    });

    public static final Tensor2D W_BISHOP_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -10, -10, -10, -10, -20,},
            {-10, 5, 0, 0, 0, 0, 5, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 0, 10, 10, 10, 10, 0, -10},
            {-10, 5, 5, 10, 10, 5, 5, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20},
    });

    public static final Tensor2D W_ROOK_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {5, 10, 10, 10, 10, 10, 10, 5},
            {0, 0, 0, 5, 5, 0, 0, 0}
    });

    public static final Tensor2D B_ROOK_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 10, 10, 10, 10, 10, 10, 5},
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
            {-5, 0, 5, 5, 5, 5, 0, -5},
            {-5, 0, 5, 5, 5, 5, 0, -5},
            {-10, 5, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 0, 0, 0, 0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}
    });

    public static final Tensor2D B_KING_VALUES_MID = new Tensor2D(new double[][]{
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {20, 20, 0, 0, 0, 0, 20, 20},
            {20, 30, 10, 0, 0, 10, 30, 20}
    });

    public static final Tensor2D W_KING_VALUES_MID = new Tensor2D(new double[][]{
            {20, 30, 10, 0, 0, 10, 30, 20},
            {20, 20, 0, 0, 0, 0, 20, 20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
    });

    public static final int[] EVALUATE_PRICE = new int[]{0, 100, 500, 320, 330, 900, 20000};

    public static final Tensor3D W_POSITION_PRICE = new Tensor3D(W_PAWN_VALUES, W_ROOK_VALUES, KNIGHT_VALUES, W_BISHOP_VALUES, QUEEN_VALUES, W_KING_VALUES_MID);
    public static final Tensor3D B_POSITION_PRICE = new Tensor3D(B_PAWN_VALUES, B_ROOK_VALUES, KNIGHT_VALUES, B_BISHOP_VALUES, QUEEN_VALUES, B_KING_VALUES_MID);

    public static final int[] UNSIGNED_COMPLETE_EVALUATE_PRICE = new int[] {
            EVALUATE_PRICE[6],
            EVALUATE_PRICE[5],
            EVALUATE_PRICE[4],
            EVALUATE_PRICE[3],
            EVALUATE_PRICE[2],
            EVALUATE_PRICE[1],
            EVALUATE_PRICE[0],
            EVALUATE_PRICE[1],
            EVALUATE_PRICE[2],
            EVALUATE_PRICE[3],
            EVALUATE_PRICE[4],
            EVALUATE_PRICE[5],
            EVALUATE_PRICE[6],
    };
    public static final int[] COMPLETE_EVALUATE_PRICE = new int[] {
            -EVALUATE_PRICE[6],
            -EVALUATE_PRICE[5],
            -EVALUATE_PRICE[4],
            -EVALUATE_PRICE[3],
            -EVALUATE_PRICE[2],
            -EVALUATE_PRICE[1],
            -EVALUATE_PRICE[0],
            EVALUATE_PRICE[1],
            EVALUATE_PRICE[2],
            EVALUATE_PRICE[3],
            EVALUATE_PRICE[4],
            EVALUATE_PRICE[5],
            EVALUATE_PRICE[6],
    };
    public static final Tensor3D COMPLETE_POSITION_PRICE = new Tensor3D(
         B_KING_VALUES_MID,
            QUEEN_VALUES,
            B_BISHOP_VALUES,
            KNIGHT_VALUES,
            B_ROOK_VALUES,
            B_PAWN_VALUES,

            QUEEN_VALUES,

            W_PAWN_VALUES,
            W_ROOK_VALUES,
            KNIGHT_VALUES,
            W_BISHOP_VALUES,
            QUEEN_VALUES,
            W_KING_VALUES_MID
    );

    //will get to this later. For now, I'm using this file to store position values
    @Override
    public double evaluate(Board board) {
        return 0;
    }

}
