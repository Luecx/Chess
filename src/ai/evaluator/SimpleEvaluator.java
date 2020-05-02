package ai.evaluator;

import ai.tools.tensor.Tensor2D;
import ai.tools.tensor.Tensor3D;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;

@Deprecated
public class SimpleEvaluator implements Evaluator {

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

    public static final Tensor3D BW_POSITION_PRICE = new Tensor3D(B_PAWN_VALUES, B_ROOK_VALUES, KNIGHT_VALUES, B_BISHOP_VALUES, QUEEN_VALUES, B_KING_VALUES_MID, W_PAWN_VALUES, W_ROOK_VALUES, KNIGHT_VALUES, W_BISHOP_VALUES, QUEEN_VALUES, W_KING_VALUES_MID);

    @Override
    public double evaluate(Board board) {
        double score = 0;

        if(board instanceof FastBoard){
            FastBoard fb = (FastBoard) board;


            for(int i = 0; i < 6; i++){
                for(int ind = 0; ind < fb.getWhite_pieces()[i].size(); ind++){
                    int sq = fb.getWhite_pieces()[i].get(ind);
                    score += W_POSITION_PRICE.get(i, BitBoard.fileIndex(sq), BitBoard.rankIndex(sq));
                    score += EVALUATE_PRICE[i+1];
                }

                for(int ind = 0; ind < fb.getBlack_pieces()[i].size(); ind++){
                    int sq = fb.getBlack_pieces()[i].get(ind);
                    score += B_POSITION_PRICE.get(i, BitBoard.fileIndex(sq), BitBoard.rankIndex(sq));
                    score += EVALUATE_PRICE[i+1];
                }
            }
        }
        else{
            for(int i = 0; i < 8; i++){
                for(int n = 0; n < 8; n++){
                    int p = board.getPiece((byte)i,(byte)n);
                    if(p == 0) continue;
                    byte sign = (byte) (p / Math.abs(p));
                    score += sign * EVALUATE_PRICE[Math.abs(p)];
                    score += sign * BW_POSITION_PRICE.get(Math.abs(p),n,i);
//                switch (Math.abs(p)){
//                    case 1: score += sign * 100; break;
//                    case 2: score += sign * 4; break;
//                    case 3: score += sign * 3; break;
//                    case 4: score += sign * 3.25; break;
//                    case 5: score += sign * 7; break;
//                    case 6: score += sign * 1000; break;
                    //}
                }
            }
        }

        return score;
    }

    @Override
    public double[] getEvolvableValues() {
        return new double[0];
    }

    @Override
    public void setEvolvableValues(double[] ar) {
    }

    @Override
    public Evaluator copy() {
        return null;
    }
    @Override
    public double staticExchangeEvaluation(Board board, int toSquare, int target, int fromSquare, int attacker, int color) {
        return 0;
    }
}
