package ai.monte_carlo.expandor;


import ai.monte_carlo.Node;
import ai.monte_carlo.nodedata.NodeData;

import java.util.List;

public interface Expander<T extends NodeData> {





    List<Node<T>> expand(Node<T> root);

}
