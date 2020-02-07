package ai.monte_carlo.simulator;


import ai.monte_carlo.Node;
import ai.monte_carlo.nodedata.NodeData;

public interface Simulator<T extends NodeData> {

    double simulate(Node<T> leaf);

}
