package game.ai.tools;

import board.moves.Move;

public class KillerTable {

    private Move[] moves;
    private int[] indices;

    private int max_depth;
    private int moves_per_depth;

    public KillerTable(int depth, int moves_per_depth){
        this.max_depth = depth;
        this.moves_per_depth = moves_per_depth;
        this.moves = new Move[max_depth * moves_per_depth];
    }

    public void put(int depth, Move move){
        this.moves[depth * moves_per_depth + indices[depth]] = move;
        System.out.println(move);
        this.indices[indices[depth]] = (this.indices[depth] + 1) % moves_per_depth;
    }

    public boolean isKillerMove(int depth, Move move){
        for(int i = depth * moves_per_depth; i < (depth + 1) * moves_per_depth; i++){
            if(move.equals(moves[i])){
                return true;
            }
        }
        return false;
    }

    public int getMax_depth() {
        return max_depth;
    }

    public int getMoves_per_depth() {
        return moves_per_depth;
    }
}
