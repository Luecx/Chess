package game.ai.evaluator;

import board.Board;
import game.ai.tools.tensor.Tensor2D;
import game.ai.tools.tensor.Tensor3D;
import game.ai.evaluator.FinnEvaluator;
import game.ai.evaluator.LateGameEvaluator;

public class NoahEvaluator implements Evaluator {

    public static final Tensor2D B_PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5, 5, 10, 25, 25, 10, 5, 5},
            {0, 0, 0, 20, 20, 0, 0, 0},
            {5, -5, -10, 0, 0, -10, -5, 5},
            {5, 10, 10, -20, -20, 10, 10, 5},
            {0, 0, 0, 0, 0, 0, 0, 0}
    });
    public static final Tensor2D W_PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 10, 10, -20, -20, 10, 10, 5},
            {5, -5, -10, 0, 0, -10, -5, 5},
            {0, 0, 0, 20, 20, 0, 0, 0},
            {5, 5, 10, 25, 25, 10, 5, 5},
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

    public static final int[] COMPLETE_EVALUATE_PRICE = new int[]{
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
    public static final int[] SIGNED_COMPLETE_EVALUATE_PRICE = new int[]{
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

    public static final Tensor3D midValue = FinnEvaluator.POSITION_PRICE;
    public static final Tensor3D endValue = LateGameEvaluator.POSITION_PRICE;

    //will get to this later. For now, I'm using this file to store position values
    @Override
    public double evaluate(Board board) {

        if (board.isGameOver()) {
            if (board.winner() == 0)
                System.out.println(board.winner());
            switch (board.winner()) {
                case 1:
                    return 1000000000;
                case 0:
                    return 0;
                case -1:
                    return -100000000;
            }
        }
        Tensor3D pieceValue = midValue;

        if (board.isEndgame()) {
            pieceValue = endValue;
        }

        int numWhiteBishops = 0;
        int numBlackBishops = 0;

        //pawn rook knight bishop queen king

        int[] wPawns = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] bPawns = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        double ev = 0;
        int v;
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {

                v = board.getPiece(i, n);
                if (v == 0) continue;

                ev += pieceValue.get(v + 6, n, i);

                switch (v) {
                    case -1:
                        bPawns[i+1] += 1;
                        break;
                    case 1:
                        wPawns[i+1] += 1;
                        break;
                    case 4: //bishop
                        numWhiteBishops += 1;
                        break;
                    case -4:
                        numBlackBishops += 1;
                        break;
//                    case 6: //king
//                        //This is for king safety
//                        if (!board.isEndgame()) {
//                            for (int j = -1; i < 2; j++) {
//                                if (i+j <= -1 || i+j>=8 || n+1<= -1 || n+1 >=8) {
//                                    continue;
//                                }
//                                if (board.getPiece(i + j, n + 1) > 0) ev += 15;
//                            }
//                            if (board.getPiece(i - 1, n) > 0) ev += 10;
//                            if (board.getPiece(i + 1, n) > 0) ev += 10;
//                        }
//                        break;
//                    case -6:
//                        if (!board.isEndgame()) {
//                            for (int j = -1; i < 2; j++) {
//                                if (i+j <= -1 || i+j>=8 || n-1<= -1 || n-1 >=8) {
//                                    continue;
//                                }
//                                if (board.getPiece(i + j, n - 1) < 0) ev -= 15;
//                            }
//                            if (board.getPiece(i - 1, n) < 0) ev -= 10;
//                            if (board.getPiece(i + 1, n) < 0) ev -= 10;
//                        }
//                    break;
                }
            }
        }

        //bishop pair
        if (numWhiteBishops > 1) ev += 50;
        if (numBlackBishops > 1) ev -= 50;

        //pawns
        for (int rank = 1; rank < 9; rank++) {
            //doubled
            if (wPawns[rank] > 1) ev -= 15;
            if (bPawns[rank] > 1) ev += 15;

            if (wPawns[rank] > 0) {
                //passed
                if (bPawns[rank - 1] == 0 && bPawns[rank] == 0 && bPawns[rank + 1] == 0) {
                    ev += 25;
                }
                //isolated
                if (wPawns[rank - 1] == 0 && wPawns[rank + 1] == 0) ev -= 15;
            }
            if (bPawns[rank] > 0) {
                //passed
                if (wPawns[rank - 1] == 0 && wPawns[rank] == 0 && wPawns[rank + 1] == 0) {
                    ev -= 25;
                }
                //isolated
                if (bPawns[rank - 1] == 0 && bPawns[rank + 1] == 0) ev += 15;
            }


        }


        return ev;
    }
}
