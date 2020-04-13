package ai.evaluator.decider;

import board.Board;
import board.FastBoard;

public class SimpleDecider implements BoardPhaseDecider {

    static final double PawnPhase = 0;
    static final double KnightPhase = 1;
    static final double BishopPhase = 1;
    static final double RookPhase = 2;
    static final double QueenPhase = 4;
    static final double TotalPhase = PawnPhase*16 + KnightPhase*4 + BishopPhase*4 + RookPhase*4 + QueenPhase*2;

    @Override
    public double getGamePhase(Board board) {

        if(!(board instanceof FastBoard)) throw new RuntimeException();

        double phase = TotalPhase;

        phase -= (((FastBoard) board).getWhite_pieces()[0].size() + ((FastBoard) board).getBlack_pieces()[0].size()) * PawnPhase;
        phase -= (((FastBoard) board).getWhite_pieces()[1].size() + ((FastBoard) board).getBlack_pieces()[1].size()) * RookPhase;
        phase -= (((FastBoard) board).getWhite_pieces()[2].size() + ((FastBoard) board).getBlack_pieces()[2].size()) * KnightPhase;
        phase -= (((FastBoard) board).getWhite_pieces()[3].size() + ((FastBoard) board).getBlack_pieces()[3].size()) * BishopPhase;
        phase -= (((FastBoard) board).getWhite_pieces()[4].size() + ((FastBoard) board).getBlack_pieces()[4].size()) * QueenPhase;


        phase = (phase * 256 + (TotalPhase / 2)) / (TotalPhase * 256d);
        return phase;
    }


}
