package ai.evaluator;

import board.Board;

/**
 * The interface is used to create Evaluators.
 * Evaluators are used in Search-algorithms in the leaf-nodes and
 * take up most of the calculation time.
 *
 */
public interface Evaluator<T extends Evaluator<T>> {

    /**
     * this method returns a value that evaluates the board.
     * A higher score should favor white where a lower (maybe negative) score
     * favors black.
     *
     * @param board
     * @return the evaluation
     */
    public double evaluate(Board board);


    /**
     * defines the values that can be evolved/tuned
     * @return
     */
    public abstract double[] getEvolvableValues();

    /**
     * sets the tuned values
     * @param ar
     */
    public abstract void setEvolvableValues(double[] ar);

    /**
     * copies this evaluator object
     * @return
     */
    public abstract T copy();
}
