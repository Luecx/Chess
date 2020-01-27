package game.ai.monte_carlo.simulator;

import game.ai.evaluator.NoahEvaluator;
import game.ai.monte_carlo.Node;
import game.ai.monte_carlo.expandor.Expander;
import game.ai.monte_carlo.nodedata.ChessNodeData;

import java.util.List;

public class EvaluatingSimulator implements Simulator<ChessNodeData> {

    static NoahEvaluator evaluator = new NoahEvaluator();

    @Override
    public double simulate(Node<ChessNodeData> root) {
        return -evaluator.evaluate(root.getNodeData().getBoard()) * root.getNodeData().getBoard().getActivePlayer() / 1000;
    }
}
