package ai.monte_carlo.expandor;


import board.Board;
import board.moves.Move;
import ai.monte_carlo.Node;
import ai.monte_carlo.nodedata.ChessNodeData;

import java.util.ArrayList;
import java.util.List;

public class ChessExpander implements Expander<ChessNodeData> {


    @Override
    public List<Node<ChessNodeData>> expand(Node<ChessNodeData> root) {

        List<Move> moves;

        if(root.getDepth() < 3){
            moves = root.getNodeData().getBoard().getLegalMoves();
        }else{
            moves = root.getNodeData().getBoard().getPseudoLegalMoves();
        }

        List<Node<ChessNodeData>> values = new ArrayList<>(moves.size());

        for(Move m:moves){
            Board c = root.getNodeData().getBoard().copy();
            c.move(m);
            values.add(new Node<>(
                    new ChessNodeData(c, m),
                    root.getDepth() + 1));
        }

        return values;
    }
}
