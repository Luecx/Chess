package game.ai.tools;

import board.moves.Move;

import java.util.List;

public class PVLine {

    private int movesInLine;
    private Move[] line;

    public PVLine(int movesInLine, int maxMoves) {
        this.movesInLine = movesInLine;
        this.line = new Move[maxMoves];
    }
    public PVLine(int maxMoves) {
        this.line = new Move[maxMoves];
    }

    public int getMovesInLine() {
        return movesInLine;
    }

    public void sort(List<Move> moves, int index){
        if(index >= line.length) return;
        for(Move m:moves){
            if(m.equals(line[index])){
                moves.add(0,m);
                return;
            }
        }
    }

    public void setMovesInLine(int movesInLine) {
        this.movesInLine = movesInLine;
    }

    public Move[] getLine() {
        return line;
    }

    public void setLine(Move[] line) {
        this.line = line;
    }
}
