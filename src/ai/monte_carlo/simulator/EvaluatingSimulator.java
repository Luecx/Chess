package ai.monte_carlo.simulator;

import ai.evaluator.AdvancedMidGameEvaluator;
import ai.evaluator.Evaluator;
import ai.monte_carlo.Node;
import ai.monte_carlo.nodedata.ChessNodeData;

public class EvaluatingSimulator implements Simulator<ChessNodeData> {

    static Evaluator evaluator = new AdvancedMidGameEvaluator();



    @Override
    public double simulate(Node<ChessNodeData> root) {
        return -evaluator.evaluate(root.getNodeData().getBoard()) * root.getNodeData().getBoard().getActivePlayer() / 1000;
    }
}
