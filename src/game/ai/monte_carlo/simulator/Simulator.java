package game.ai.monte_carlo.simulator;


import game.ai.monte_carlo.Node;
import game.ai.monte_carlo.nodedata.NodeData;

public interface Simulator<T extends NodeData> {

    double simulate(Node<T> leaf);

}
