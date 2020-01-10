package game.ai.evaluator;

import board.Board;
import game.ai.tools.tensor.Tensor2D;
import game.ai.tools.tensor.Tensor3D;
import game.ai.evaluator.FinnEvaluator;
import game.ai.evaluator.LateGameEvaluator;

import java.util.Arrays;

public class NoahEvaluator extends GeneticEvaluator<NoahEvaluator> implements Evaluator {

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

    public static final int[] EVALUATE_PRICE = new int[]{0, 100, 500, 315, 341, 950, 20000};

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


    private double PARAMATER_PASSED_PAWN        = 25;
    private double PARAMATER_ISOLATED_PAWN      = -15;
    private double PARAMATER_DOUBLED_PAWN       = -15;
    private double PARAMATER_DOUBLE_BISHOP      = 50;
    private double PARAMETER_KING_SAFETY_1      = 15;
    private double PARAMETER_KING_SAFETY_2      = 10;
    private double PARAMETER_ROOK_HALF_OPEN     = 15;
    private double PARAMETER_ROOK_OPEN          = 20;

    //will get to this later. For now, I'm using this file to store position values
    @Override
    public double evaluate(Board board) {

        if (board.isGameOver()) {
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

        //recording the file the rook is on
        int[] whiteRooks = {-1,-1};
        int[] blackRooks = {-1,-1};

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
                    case 2:
                        if (whiteRooks[0] == -1) {
                            whiteRooks[0]  = i;
                        } else if (whiteRooks[1]  == -1) {
                            whiteRooks[1] = i;
                        }
                    case -2:
                        if (blackRooks[0] == -1) {
                            blackRooks[0] = i;
                        } else if (blackRooks[1] == -1) {
                            blackRooks[1] = i;
                        }
                    case 4: //bishop
                        numWhiteBishops += 1;
                        break;
                    case -4:
                        numBlackBishops += 1;
                        break;
                    case 6: //king
                        //This is for king safety
                        if (!board.isEndgame()) {
                            for (int j = -1; j < 2; j++) {
                                if (i+j <= -1 || i+j>=8 || n+1<= -1 || n+1 >=8) {
                                    continue;
                                }
                                if (board.getPiece(i + j, n + 1) > 0) ev += PARAMETER_KING_SAFETY_1;
                            }
                            if (board.getPiece(i - 1, n) > 0) ev += PARAMETER_KING_SAFETY_2;
                            if (board.getPiece(i + 1, n) > 0) ev += PARAMETER_KING_SAFETY_2;
                        }
                        break;
                    case -6:
                        if (!board.isEndgame()) {
                            for (int j = -1; j < 2; j++) {
                                if (i+j <= -1 || i+j>=8 || n-1<= -1 || n-1 >=8) {
                                    continue;
                                }
                                if (board.getPiece(i + j, n - 1) < 0) ev -= PARAMETER_KING_SAFETY_1;
                            }
                            if (board.getPiece(i - 1, n) < 0) ev -= PARAMETER_KING_SAFETY_2;
                            if (board.getPiece(i + 1, n) < 0) ev -= PARAMETER_KING_SAFETY_2;
                        }
                        break;
                }
            }
        }


        //bishop pair
        if (numWhiteBishops > 1) ev += PARAMATER_DOUBLE_BISHOP;
        if (numBlackBishops > 1) ev -= PARAMATER_DOUBLE_BISHOP;

        //pawns
        for (int rank = 1; rank < 9; rank++) {
            //doubled
            if (wPawns[rank] > 1) ev += PARAMATER_DOUBLED_PAWN;
            if (bPawns[rank] > 1) ev -= PARAMATER_DOUBLED_PAWN;

            if (wPawns[rank] > 0) {
                //passed
                if (bPawns[rank - 1] == 0 && bPawns[rank] == 0 && bPawns[rank + 1] == 0) {
                    ev += PARAMATER_PASSED_PAWN;
                }
                //isolated
                if (wPawns[rank - 1] == 0 && wPawns[rank + 1] == 0) ev += PARAMATER_ISOLATED_PAWN;
            }
            if (bPawns[rank] > 0) {
                //passed
                if (wPawns[rank - 1] == 0 && wPawns[rank] == 0 && wPawns[rank + 1] == 0) {
                    ev -= PARAMATER_PASSED_PAWN;
                }
                //isolated
                if (bPawns[rank - 1] == 0 && bPawns[rank + 1] == 0) ev -= PARAMATER_ISOLATED_PAWN;
            }
        }
        /// rooks
        for (int file : whiteRooks) {
            if (file == -1) {
                continue;
            }
            if (wPawns[file]  == 0) {
                ev += 15;
                if (bPawns[file] == 0) {
                    ev += 20;
                }
            }
        }
        for (int file : blackRooks) {
            if (file == -1) {
                continue;
            }
            if (bPawns[file]  == 0) {
                ev += PARAMETER_ROOK_HALF_OPEN;
                if (wPawns[file] == 0) {
                    ev += PARAMETER_ROOK_OPEN;
                }
            }
        }


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
                PARAMETER_KING_SAFETY_2};
    }

    @Override
    public void setEvolvableValues(double[] ar) {
        PARAMATER_PASSED_PAWN = ar[0];
        PARAMATER_ISOLATED_PAWN = ar[1];
        PARAMATER_DOUBLED_PAWN = ar[2];
        PARAMATER_DOUBLE_BISHOP = ar[3];
        PARAMETER_KING_SAFETY_1 = ar[4];
        PARAMETER_KING_SAFETY_2 = ar[5];
    }

    @Override
    public NoahEvaluator copy() {
        NoahEvaluator evaluator = new NoahEvaluator();
        evaluator.setEvolvableValues(this.getEvolvableValues());
        return evaluator;
    }
}
