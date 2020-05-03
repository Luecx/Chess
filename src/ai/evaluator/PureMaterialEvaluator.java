package ai.evaluator;

import ai.evaluator.decider.SimpleDecider;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;

import java.util.Arrays;

public class PureMaterialEvaluator implements Evaluator<PureMaterialEvaluator> {

    private double[] params = new double[]{100,500,300,300,900,10000,100,500,300,300,900,10000};
    private double[] pieceVals = new double[]{0,100,500,300,320,900,100000};

    private SimpleDecider decider = new SimpleDecider();

    @Override
    public double evaluate(Board board) {
        double score = 0;
        FastBoard fb = (FastBoard) board;
        double p = decider.getGamePhase(board);

        for(int i = 0; i < 6; i++){
            score += (params[i] * p + params[i+6] * (1-p)) * fb.getWhite_pieces()[i].size();
            score -= (params[i] * p + params[i+6] * (1-p)) * fb.getBlack_pieces()[i].size();
        }

        return score;
    }

    @Override
    public double[] getEvolvableValues() {
        return params;
    }

    @Override
    public void setEvolvableValues(double[] ar) {
        this.params = ar;
    }

    @Override
    public PureMaterialEvaluator copy() {
        return null;
    }

    private int smallestAttackerSquare(long[] pieceOcc, long occupied, int square, int side){


        long occ = occupied;
        long[] pOcc = pieceOcc;
        long squareBB = 1L << square;




        //pawns
        if(side > 0){
            if((BitBoard.shiftNorthWest(pOcc[0]) & squareBB) != 0){
                return square-7;
            }
            if((BitBoard.shiftNorthEast(pOcc[0]) & squareBB) != 0){
                return square-9;
            }
        }else{
            if((BitBoard.shiftSouthWest(pOcc[0]) & squareBB) != 0){
                return square+9;
            }
            if((BitBoard.shiftSouthEast(pOcc[0]) & squareBB) != 0){
                return square+7;
            }
        }


        long nA = BitBoard.KNIGHT_ATTACKS[square];
        long kA = BitBoard.KING_ATTACKS[square];
        long rA = BitBoard.lookUpRookAttack(square, occ);
        long bA = BitBoard.lookUpBishopAttack(square, occ);

        if((nA & pOcc[2]) != 0){
            return BitBoard.bitscanForward(nA & pOcc[2]);
        }

        if((bA & pOcc[3]) != 0){
            return BitBoard.bitscanForward(bA & pOcc[3]);
        }

        if((rA & pOcc[1]) != 0){
            return BitBoard.bitscanForward(rA & pOcc[1]);
        }

        if(((rA | bA) & pOcc[4]) != 0){
            return BitBoard.bitscanForward((rA | bA)  & pOcc[4]);
        }

        if((kA & pOcc[5]) != 0){
            return BitBoard.bitscanForward(kA & pOcc[5]);
        }



        return -1;
    }

    public double staticExchangeEvaluation(Board board, int toSqare, int target, int fromSquare, int attacker, int color){


        long[] whiteOcc = Arrays.copyOf(((FastBoard)board).getWhite_values(), 6);
        long[] blackOcc = Arrays.copyOf(((FastBoard)board).getBlack_values(), 6);


        int gain[] = new int[32];
        int d = 0;
        gain[d] = (int) pieceVals[Math.abs(target)];


        long fromSet = 1L << fromSquare;
        long occ = ((FastBoard) board).getOccupied();


        do{
            d++;
            gain[d] = (int) (pieceVals[Math.abs(attacker)] - gain[d-1]);
            if(Math.max(-gain[d-1], gain[d]) < 0){
                break;
            }

            if(color == 1){
                whiteOcc[attacker-1] ^= (fromSet);
            }else{
                blackOcc[-attacker-1] ^= fromSet;
            }
            occ     ^= fromSet;
            color = -color;

            fromSquare = smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, toSqare, color);

            if(fromSquare == 64){
                smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, toSqare, color);
                throw new RuntimeException();
            }

            if(fromSquare == -1 || fromSquare == 64) break;
            attacker = board.getPiece(fromSquare);
            fromSet = 1L << fromSquare;
        }while (fromSet != 0);

        while (--d > 0)
            gain[d-1]= -Math.max(-gain[d-1], gain[d]);
        return gain[0];
    }

    public double staticExchangeEvaluation(Board board, int sq, int color){
        double val = 0;

        int nextCapturedPiece = Math.abs(board.getPiece(sq));


        long[] whiteOcc = Arrays.copyOf(((FastBoard)board).getWhite_values(), 6);
        long[] blackOcc = Arrays.copyOf(((FastBoard)board).getBlack_values(), 6);

        long occ = ((FastBoard) board).getOccupied();
        int minAttackerSquare = smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, sq, color);


        int gain[] = new int[32];
        int d = 0;

        while(minAttackerSquare != -1){

            int attackerPiece = board.getPiece(minAttackerSquare);

            occ ^= (1L << minAttackerSquare);
            if(color == 1){
                whiteOcc[attackerPiece-1] ^= (1L << minAttackerSquare);
                val += pieceVals[nextCapturedPiece];
                nextCapturedPiece = attackerPiece;
            }else{
                blackOcc[-attackerPiece-1] ^= (1L << minAttackerSquare);
                val -= pieceVals[nextCapturedPiece];
                nextCapturedPiece = -attackerPiece;
            }
            color = -color;

            minAttackerSquare = smallestAttackerSquare(color == 1 ? whiteOcc:blackOcc,occ, sq, color);
        }


        return val;




//        if(minAttackerSquare == -1) return val;
//
//        int attackedPiece = board.getPiece(sq);
//        int attackerPiece = board.getPiece(minAttackerSquare);
//
//
//
//
//
//
//        /* skip if the square isn't attacked anymore by this side */
//        if ( minAttackerSquare != -1)// && board.getPiece(minAttackerSquare) * color > 0)
//        {
//            board.setPiece(0, minAttackerSquare);
//            board.setPiece(attackerPiece, sq);
//
//            /* Do not consider captures if they lose material, therefor max zero */
//            //val = Math.max(0, pieceVals[Math.abs(attackedPiece)] - staticExchangeEvaluation(board, sq, -color));
//
//
//            val = pieceVals[Math.abs(attackedPiece)] - staticExchangeEvaluation(board, sq, -color);
//
//
//            board.setPiece(attackerPiece, minAttackerSquare);
//            board.setPiece(attackedPiece, sq);
//        }
//
//        return val;
    }


}
