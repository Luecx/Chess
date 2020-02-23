package board.pieces;

import java.util.Arrays;
import java.util.Objects;

public class PieceList {

    private int piece;
    private int size;
    private int[] indices;

    public PieceList(int piece, int max_size) {
        this.indices = new int[max_size];
        this.size = 0;
        this.piece = piece;
    }

    public PieceList(int piece) {
        this(piece, 10);
    }

    /**
     * returns the amount of pieces represented
     * @return
     */
    public int size(){
        return size;
    }

    /**
     * returns the position of the piece at the given index
     * @param index
     * @return
     */
    public int get(int index){
        return indices[index];
    }

    /**
     * checks if there is a piece on the given square
     * @param position
     * @return
     */
    public boolean contains(int position){
        for(int i = 0; i < this.size; i++){
            if(this.indices[i] == position){
                return true;
            }
        }
        return false;
    }

    /**
     * clears all positions
     */
    public void clear() {
        this.size = 0;
    }

    /**
     * copied this object
     * @return
     */
    public PieceList copy() {
        PieceList copy = new PieceList(this.piece, this.size);
        copy.indices = Arrays.copyOf(this.indices, this.indices.length);
        copy.size = this.size;
        return copy;
    }

    /**
     * adds a new position to this object and increase the size by 1
     * @param position
     */
    public void add(int position){
        this.indices[size] = position;
        this.size ++;
    }

    /**
     * removes a given position from this list
     * @param position
     */
    public void remove(int position){
        for(int i = 0; i < this.size; i++){
            if(this.indices[i] == position){
                this.indices[i] = this.indices[size-1];
                size--;
                return;
            }
        }
        size--;
    }

    /**
     * checks if this list equals another list.
     * this also checks if the positions are equal
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PieceList list = (PieceList) o;
        if(piece != list.piece || size != list.size) return false;
        for(int i = 0; i < size; i++){
            if(list.indices[i] != indices[i]){
                return false;
            }
        }
        return true;
    }

    /**
     * hashcode (not used afaik)
     * @return
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(piece, size);
        result = 31 * result + Arrays.hashCode(indices);
        return result;
    }

    /**
     * returns this list represented as a string
     * @return
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("PieceList [" + piece+ "] : {");
        for(int i = 0; i < this.size-1; i++){
            builder.append(this.indices[i] + ", ");
        }
        if(this.size > 0)
            builder.append(this.indices[size-1]);
        builder.append("}");

        return builder.toString();
    }

}
