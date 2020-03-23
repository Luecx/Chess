package ai.evaluator;

import ai.evaluator.decider.BoardPhaseDecider;
import board.Board;

public class AdvancedEvaluator implements Evaluator<AdvancedEvaluator> {

    private BoardPhaseDecider phaseDecider;


    public AdvancedEvaluator(BoardPhaseDecider phaseDecider) {
        this.phaseDecider = phaseDecider;
    }


    @Override
    public double evaluate(Board board) {
        double phase = phaseDecider.getGamePhase(board);
    }

    @Override
    public double[] getEvolvableValues() {
        return new double[0];
    }

    @Override
    public void setEvolvableValues(double[] ar) {

    }

    @Override
    public AdvancedEvaluator copy() {
        return null;
    }
}
