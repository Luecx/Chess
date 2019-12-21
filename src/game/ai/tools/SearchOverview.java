package game.ai.tools;

import java.util.ArrayList;

public class SearchOverview {


    private int totalTime;
    private int depth;
    private int qDepth;

    private ArrayList<Integer> depths;
    private ArrayList<Integer> totalNodes;
    private ArrayList<Integer> fullDepthNodes;
    private ArrayList<Integer> qSearchNodes;
    private ArrayList<Integer> timings;
    private ArrayList<Integer> terminalNodes;

    private String[] flags;

    public SearchOverview(String... flags) {
        int flagCount = 0;
        for (String s : flags) {
            if (s.length() > 0) {
                flagCount++;
            }
        }
        this.flags = new String[flagCount];
        flagCount = 0;
        for (String s : flags) {
            if (s.length() > 0) {
                this.flags[flagCount] = s;
                flagCount++;
            }
        }
        this.depths = new ArrayList<>();
        this.totalNodes = new ArrayList<>();
        this.fullDepthNodes = new ArrayList<>();
        this.timings = new ArrayList<>();
        this.terminalNodes = new ArrayList<>();
        this.qSearchNodes = new ArrayList<>();
    }

    public void addIteration(int depth, int totalNodes, int fullDepthNodes, int terminal, int qNodes, int time) {
        this.depths.add(depth);
        this.timings.add(time);
        this.totalNodes.add(totalNodes);
        this.fullDepthNodes.add(fullDepthNodes);
        this.terminalNodes.add(terminal);
        this.qSearchNodes.add(qNodes);
    }

    public String[] getFlags() {
        return flags;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getTotalNodesLastIteration() {
        return totalNodes.get(totalNodes.size() - 1);
    }

    public int getTerminalNodesLastIteration() {
        return terminalNodes.get(terminalNodes.size() - 1);
    }

    public int getFullDepthNodesLastIteration() {
        return fullDepthNodes.get(fullDepthNodes.size() - 1);
    }

    public int getQSearchNodesLastIteration() {
        return qSearchNodes.get(qSearchNodes.size() - 1);
    }


    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getqDepth() {
        return qDepth;
    }

    public void setqDepth(int qDepth) {
        this.qDepth = qDepth;
    }

    /**
     * prints a summary of the last iteration.
     * <p>
     * It prints the time in [mm:ss:uuu] followed by the following parameters:
     * - total visited nodes: the total amount of nodes that have been visited including quiesce-search
     * - terminal nodes: the leaf nodes in qSearch.
     * - visited nodes full depth: the amount of nodes visited without qSearch.
     * - visited quiesce nodes: the amount of nodes visited only in qSearch.
     */
    public void printIterationSummary() {
        int min = (int) (timings.get(timings.size() - 1) / (60 * 1E3) % 60);
        int sec = (int) (timings.get(timings.size() - 1) / 1E3 % 60);
        int mil = (int) (timings.get(timings.size() - 1) % 1E3);
        System.out.format(
                "depth: %02d(+" + qDepth + ")\t" +
                        "time[m:s:ms]: %02d:%02d:%03d\t" +
                        "total: %9d\t" +
                        "terminal : %9d\t" +
                        "fullNodes: %9d\t" +
                        "qNodes: %9d\n",
                depths.get(timings.size() - 1),
                min, sec, mil,
                totalNodes.get(timings.size() - 1) + qSearchNodes.get(timings.size() - 1),
                terminalNodes.get(timings.size() - 1),
                fullDepthNodes.get(timings.size() - 1),
                qSearchNodes.get(timings.size() - 1));
    }

    public void printTotalSummary() {
        System.out.println("required time: " + totalTime + " ms");
    }
}
