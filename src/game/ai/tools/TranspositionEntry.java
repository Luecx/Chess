package game.ai.tools;

public class TranspositionEntry {

    public static final int PV_NODE = 1;
    public static final int CUT_NODE = 2;
    public static final int ALL_NODE = 3;

    private double val;
    private int skipped_depths;
    private int node_type;
    private int color;

    public TranspositionEntry(double val, int skipped_depths, int node_type, int color) {
        this.val = val;
        this.skipped_depths = skipped_depths;
        this.node_type = node_type;
        this.color = color;
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

    public int getSkipped_depths() {
        return skipped_depths;
    }

    public void setSkipped_depths(int skipped_depths) {
        this.skipped_depths = skipped_depths;
    }
}