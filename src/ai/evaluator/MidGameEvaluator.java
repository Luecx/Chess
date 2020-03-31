package ai.evaluator;

import board.Board;

/**
 * The interface is used to create Midgame-Evaluators.
 * Evaluators are used in Search-algorithms in the leaf-nodes and
 * take up most of the calculation time.
 *
 */
@Deprecated
public interface MidGameEvaluator<T extends MidGameEvaluator<T>> extends Evaluator<T>{

}
