package ai.monte_carlo.selection;

import ai.monte_carlo.Node;
import ai.monte_carlo.nodedata.NodeData;

public interface Selector<T extends NodeData> {

    Node selectLeaf(Node root);

    Node selectChild(Node root);

}
