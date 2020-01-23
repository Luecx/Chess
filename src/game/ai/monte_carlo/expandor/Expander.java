package game.ai.monte_carlo.expandor;


import game.ai.monte_carlo.Node;
import game.ai.monte_carlo.nodedata.NodeData;

import java.util.List;

public interface Expander<T extends NodeData> {





    List<Node<T>> expand(Node<T> root);

}
