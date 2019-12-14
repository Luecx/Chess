package game.ai.tools;

public class TranspositionEntry {
    private double val;
    private int skipped_depths;

    public TranspositionEntry(double val, int skipped_depths) {
        this.val = val;
        this.skipped_depths = skipped_depths;
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