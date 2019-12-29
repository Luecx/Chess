package board.pieces;

import java.util.Arrays;

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

    public int size(){
        return size;
    }

    public int get(int index){
        return indices[index];
    }

    public boolean contains(int position){
        for(int i = 0; i < this.size; i++){
            if(this.indices[i] == position){
                return true;
            }
        }
        return false;
    }

    public void clear() {
        this.size = 0;
    }

    public PieceList copy() {
        PieceList copy = new PieceList(this.piece, this.size);
        copy.indices = Arrays.copyOf(this.indices, this.indices.length);
        return copy;
    }

    public void add(int position){
        this.indices[size] = position;
        this.size ++;
    }

    public void remove(int position){
        for(int i = 0; i < this.size; i++){
            if(this.indices[i] == position){
                this.indices[i] = this.indices[size];
                size--;
                return;
            }
        }
        size--;
    }

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

    public void move() {

    }
}
