package board.repetitions;

import board.Board;
import board.SlowBoard;

import java.util.LinkedList;
import java.util.Objects;

public class RepetitionList {

    private LinkedList<Entry> entries = new LinkedList<>();

    private class Entry{

        public Entry(long zobrist, int count) {
            this.zobrist = zobrist;
            this.count = count;
        }

        private long zobrist;
        private int count;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return zobrist == entry.zobrist &&
                    count == entry.count;
        }

        @Override
        public int hashCode() {
            return Objects.hash(zobrist, count);
        }

        public long getZobrist() {
            return zobrist;
        }

        public void setZobrist(long zobrist) {
            this.zobrist = zobrist;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public RepetitionList(){

    }

    public boolean add(long zobrist){
        for(Entry e:entries){
            if(e.zobrist == zobrist){
                e.count ++;
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

    public RepetitionList copy(){
        LinkedList<Entry> new_entries = new LinkedList<>();
        for(Entry e:entries){
            new_entries.add(new Entry(e.zobrist, e.count));
        }
        RepetitionList res = new RepetitionList();
        res.entries = new_entries;
        return res;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepetitionList list = (RepetitionList) o;
        if (entries.size() != list.entries.size()) return false;
        for(int i = 0; i < entries.size(); i++){
            if(!entries.get(i).equals(list.entries.size())) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }



}
