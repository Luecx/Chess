package board.repetitions;

import board.Board;
import board.SlowBoard;

import java.util.LinkedList;

public class RepetitionList {

    private LinkedList<Entry> entries = new LinkedList<>();

    private class Entry{

        public Entry(long zobrist, int count) {
            this.zobrist = zobrist;
            this.count = count;
        }

        private long zobrist;
        private int count;
    }

    public RepetitionList(){

    }

    public boolean add(long zobrist){
        for(Entry e:entries){
            if(e.zobrist == zobrist){
                e.count ++;
                System.out.println(e.count);
                return e.count >= 3;
            }
        }
        entries.addFirst(new Entry(zobrist, 1));
        return false;
    }

    public void sub(long zobrist){
        for(Entry e:entries){
            if(e.zobrist == zobrist){
                e.count --;
                if(e.count == 0){
                    entries.remove(e);
                }
                return;
            }
        }
    }

    public boolean add(SlowBoard board){
        return this.add(board.zobrist());
    }

    public void sub(SlowBoard board){
        this.sub(board.zobrist());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RepetitionList\n");

        for(Entry e:entries){
            builder.append(String.format("zobrist: %-30d   count: %-4d \n", e.zobrist, e.count));
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        RepetitionList list = new RepetitionList();
        list.add(1L);
        System.out.println(list);
        list.sub(1L);
        System.out.println(list);
    }


}
