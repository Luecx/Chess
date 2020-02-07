package ai.monte_carlo;

import board.Board;
import board.moves.Move;
import ai.monte_carlo.expandor.Expander;
import ai.monte_carlo.nodedata.ChessNodeData;
import ai.monte_carlo.nodedata.NodeData;
import ai.monte_carlo.selection.Selector;
import ai.monte_carlo.selection.UCT;
import ai.monte_carlo.simulator.Simulator;
import ai.search.AI;

import java.util.List;

public class MCTS<T extends NodeData> implements AI {

    private int amountOfPlayers = 2;

    private Node root;
    private Selector<T> selector = new UCT();
    private Simulator<T> simulator;
    private Expander<T> expander;
    private int maxDepth = 10000;

    public MCTS(Selector selector, Simulator<T> simulator, Expander<T> expander) {
        this.selector = selector;
        this.simulator = simulator;
        this.expander = expander;
    }

    public MCTS(int amountOfPlayers, Selector<T> selector, Simulator<T> simulator, Expander<T> expander) {
        this.amountOfPlayers = amountOfPlayers;
        this.selector = selector;
        this.simulator = simulator;
        this.expander = expander;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Node<T> run(Node<T> root, int times){
        this.root = root;
        int p = 0;
        for(int i = 0; i < times; i++){
            if((int)((double)i / times * 100) > p){
                p = (int)((double)i / times * 100);
                System.out.print("\r"+p+" %");
            }
            iteration();
        }
        Node<T> best = root.getChilds().get(0);
        for(Node<T> n:root.getChilds()){
            if(n.getTotal_score() / (double)n.getNumber_of_simulations() > best.getTotal_score() / (double)best.getNumber_of_simulations()){
                best = n;
            }
        }
//        System.err.println(root.getChilds());
//        System.err.println(best);
//        System.err.println();
        return best;
        //return selector.selectChild(root);
    }


    public String toString(){
        StringBuilder builder = new StringBuilder();
        toString(builder, 1, root, 100000);
        return builder.toString();
    }

    public String toString(int depth){
        StringBuilder builder = new StringBuilder();
        toString(builder, 1, root, depth);
        return builder.toString();
    }

    public static void toString(StringBuilder builder, int spaces, Node root, int depthleft){
        builder.append(String.format("%-"+spaces+"s [%-3s;%-3.5f](%-1s)\n", "",
                root.getNumber_of_simulations(),
                root.getTotal_score(),
                root.getNodeData()));
        if(depthleft == 0) return;
        if(root.getChilds() != null){
            for(Object n:root.getChilds()){
                toString(builder, spaces + 4, (Node)n, depthleft-1);
            }
        }
    }

    public void iteration(){
        Node leaf = selection();
        if(leaf.getDepth() < maxDepth){
            List<Node<T>> subLeafs = expansion(leaf);
            if(subLeafs == null || subLeafs.size() == 0) {
                return;
            }
            for(Node<T> n:subLeafs){
                double eval = simulation(n);
                backpropagation(n, eval);
            }
        }else{
            double eval = simulation(leaf);
            backpropagation(leaf, eval);
        }

    }

    public Node selection(){
        return selector.selectLeaf(root);
    }

    public List<Node<T>> expansion(Node leaf){
        List<Node<T>> newLeafs = expander.expand(leaf);
        if(newLeafs.size() == 0) return null;

        leaf.setChilds(newLeafs);
        for(Node<T> l:newLeafs){
            l.setParent(leaf);
            //leaf.getChilds().add(l);
            l.setPlayerIndex((leaf.getPlayerIndex()+1)%amountOfPlayers);
        }
        return newLeafs;
    }

    public double simulation(Node<T> leaf){
        return simulator.simulate(leaf);
    }

    public void backpropagation(Node selected, double score){
        Node cur = selected;
        cur.setNumber_of_simulations(cur.getNumber_of_simulations()+1);
        cur.setTotal_score(cur.getTotal_score() + score);
        while(cur != root){
            cur = cur.getParent();
            cur.setNumber_of_simulations(cur.getNumber_of_simulations()+1);
            if(cur.getPlayerIndex() == selected.getPlayerIndex()){
                cur.setTotal_score(cur.getTotal_score() + score);
            }
        }
    }

//    public static void main(String[] args) {
//        SlowBoard board = new SlowBoard(Setup.DEFAULT);
//
//        MCTS<ChessNodeData> mcts = new MCTS<ChessNodeData>(new UCT(), new EvaluatingSimulator(),new ChessExpander());
//        Node<ChessNodeData> node =  mcts.run(new Node(new ChessNodeData(board, null),0),1000);
//        System.out.println(mcts);
//        System.out.println(node.getNodeData().getBoard());
//    }

    @Override
    public Move bestMove(Board board) {
        this.maxDepth = 3;
        Node<ChessNodeData> node =  this.run(new Node(new ChessNodeData(board, null),0),10000);
        System.out.println(this.toString(2));
        return node.getNodeData().getMove();
    }
}
