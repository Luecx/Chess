package board.repetitions;

import board.Board;

import java.util.LinkedList;
import java.util.Objects;

public class RepetitionList {

    private LinkedList<Entry> entries = new LinkedList<>();

    private class Entry{


        /**
         * this entry class represents a board state in the history of the board.
         * it counts how many different board positions have occured and the amount of the occurencies.
         * @param zobrist
         * @param count
         * @param color
         */

        public Entry(long zobrist, int count, int color) {
            this.zobrist = zobrist;
            this.count = count;
            this.color = color;
        }

        /**
         * use the zobrist key to identify a board state. its is assumed that no hash collisions occur
         */
        private long zobrist;

        /**
         * the amount of times this position occured
         */
        private int count;

        /**
         * color to move
         */
        private int color;

        /**
         * checks if this entry equals another entry
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return zobrist == entry.zobrist &&
                    count == entry.count;
        }

        /**
         * hashcode to map this object to an int (not used afaik)
         * @return
         */
        @Override
        public int hashCode() {
            return Objects.hash(zobrist, count);
        }

        /**
         * getter for the zobrist key
         * @return
         */
        public long getZobrist() {
            return zobrist;
        }

        /**
         * setter for the zobrist key
         * @param zobrist
         */
        public void setZobrist(long zobrist) {
            this.zobrist = zobrist;
        }

        /**
         * getter for the count variable
         * @return
         */
        public int getCount() {
            return count;
        }

        /**
         * setter for the zobrist variable
         * @param count
         */
        public void setCount(int count) {
            this.count = count;
        }

        /**
         * returns the player who is to move
         * @return
         */
        public int getColor() {
            return color;
        }

        /**
         * sets the player who has to move
         * @param color
         */
        public void setColor(int color) {
            this.color = color;
        }
    }

    /**
     * empty constructor
     */
    public RepetitionList(){

    }

    /**
     * adds the board state represented by the zobrist key.
     * if the key already occured, the count for that position will increase by 1.
     * otherwise a new entry will be generated.
     * if the count of that position is higher than 2 (3-fold repetition), true will be returned.
     * otherwise false.
     * @param zobrist
     * @return              if the visual.game is over by 3-fold repetition
     */
    public boolean add(long zobrist, int color){
        for(Entry e:entries){
            if(e.zobrist == zobrist && e.color == color){
                e.count ++;
                return e.count >= 3;
            }
        }
        entries.addFirst(new Entry(zobrist, 1, color));
        return false;
    }

    /**
     * subtracts the board from the list. it reduces the count for the given entry.
     * @param zobrist
     */
    public void sub(long zobrist, int color){
        for(Entry e:entries){
            if(e.zobrist == zobrist && color == e.color){
                e.count --;
                if(e.count == 0){
                    entries.remove(e);
                }
                return;
            }
        }
    }

    /**
     * for more information look at {@link #add(long, int) add}
     * @param board
     * @return
     */
    public boolean add(Board board){
        return this.add(board.zobrist(), board.getActivePlayer());
    }

    /**
     * for more information look at {@link #sub(long, int) add}
     * @param board
     * @return
     */
    public void sub(Board board){
        this.sub(board.zobrist(), board.getActivePlayer());
    }

    /**
     * returns the amount the given zobrist key has already occured.
     * @param zobrist
     * @return
     */
    public int get(long zobrist, int color){
        for(Entry e:entries){
            if(e.zobrist == zobrist){
                return e.count;
            }
        }
        return 0;
    }

    /**
     * copies this list and creates a new one. the entry are also copied
     * @return
     */
    public RepetitionList copy(){
        LinkedList<Entry> new_entries = new LinkedList<>();
        for(Entry e:entries){
            new_entries.add(new Entry(e.zobrist, e.count, e.color));
        }
        RepetitionList res = new RepetitionList();
        res.entries = new_entries;
        return res;
    }

    /**
     * returns the list as a string
     * @return
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RepetitionList\n");

        for(Entry e:entries){
            builder.append(String.format("zobrist: %-30d   count: %-4d \n", e.zobrist, e.count+"("+e.getColor()+")"));
        }

        return builder.toString();
    }

    /**
     * checks if this list equals another list
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepetitionList list = (RepetitionList) o;
        if (entries.size() != list.entries.size()) return false;
        for(int i = 0; i < entries.size(); i++){
            if(!entries.get(i).equals(list.entries.get(i))) return false;
        }
        return true;
    }

    /**
     * used to hash this object (not used afaik)
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }



}
