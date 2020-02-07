package ai.tools;

import board.moves.Move;

public class TranspositionEntry {

    public static final int PV_NODE = 1;
    public static final int CUT_NODE = 2;
    public static final int ALL_NODE = 3;

    private double val;
    private int depth;
    private int node_type;
    private int color;
    private Move bestMove;

    public TranspositionEntry(double val, int depth, int node_type, int color, Move bestMove) {
        this.val = val;
        this.depth = depth;
        this.node_type = node_type;
        this.color = color;
        this.bestMove = bestMove;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getNode_type() {
        return node_type;
    }

    public void setNode_type(int node_type) {
        this.node_type = node_type;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public int getDepthLeft() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public void setBestMove(Move bestMove) {
        this.bestMove = bestMove;
    }
}