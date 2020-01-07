package board.moves;

import board.Board;

import java.util.*;
import java.util.function.Consumer;

public class MoveList implements Iterable<Move>, List<Move> {

    private Move[] moves;
    private int size = 0;


    public MoveList(int maxSize) {
        this.moves = new Move[maxSize];
        for(int i = 0; i < this.moves.length; i++){
            this.moves[i] = new Move();
        }
    }

    public void clear(){
        this.size = 0;
    }

    @Override
    public Move get(int index) {
        return this.moves[index];
    }

    @Override
    public Move set(int index, Move element) {
        Move old = this.moves[index];
        this.moves[index] = element;
        return old;
    }

    @Override
    public void add(int index, Move element) {
        this.swap(index, size);
        this.set(index, element);
    }

    public void swap(int index1, int index2){
        Move m = moves[index1];
        moves[index1] = moves[index2];
        moves[index2] = m;
    }

    @Override
    public Move remove(int index) {
        Move m = moves[index];
        moves[index] = moves[size-1];
        moves[size-1] = m;
        size--;
        return m;
    }

    @Override
    public int indexOf(Object o) {
        for(int i = 0; i < size; i++){
            if(moves[i].equals(o)){
                return i;
            }
        }return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for(int i = size-1; i >= 0; i++){
            if(o.equals(moves[i])){
                return i;
            }
        }return -1;
    }

    public void setCapacity(int newCap){
        Move[] ar = new Move[newCap];
        for(int i = 0; i < this.moves.length; i++){
            ar[i] = this.moves[i];
        }
        for(int i = this.moves.length; i < ar.length; i++){
            ar[i] = new Move();
        }
        this.moves = ar;
    }

    @Override
    public ListIterator<Move> listIterator() {
        return this.listIterator(-1);
    }

    @Override
    public ListIterator<Move> listIterator(int pindex) {
        return new ListIterator<Move>() {

            int index = pindex;

            @Override
            public boolean hasNext() {
                return index < size - 1;
            }

            @Override
            public Move next() {
                return moves[++index];
            }

            @Override
            public boolean hasPrevious() {
                return index > 0;
            }

            @Override
            public Move previous() {
                return moves[--index];
            }

            @Override
            public int nextIndex() {
                return index + 1;
            }

            @Override
            public int previousIndex() {
                return index - 1;
            }

            @Override
            public void remove() {
                throw new RuntimeException();
            }

            @Override
            public void set(Move move) {
                throw new RuntimeException();
            }

            @Override
            public void add(Move move) {
                throw new RuntimeException();
            }
        };
    }

    @Override
    public List<Move> subList(int fromIndex, int toIndex) {
        throw new RuntimeException();
    }

    public void add(int from, int to, int pieceFrom, int pieceTo) {
        this.add(from, to, pieceFrom, pieceTo, (short) 0);
    }

    public void add(int from, int to, Board board) {
        this.add(from,to,board.getPiece(from),board.getPiece(to), (short)0);
    }

    public void add() {
        this.add(0,0,0,0,(short)0);
        this.moves[size].isNull = true;
    }

    public void add(int from, int to, Board board, short metaInformation) {
        this.add(from,to,board.getPiece(from),board.getPiece(to), metaInformation);
    }

    public void add(int from, int to, int pieceFrom, int pieceTo, short metaInformation) {
        if(this.size == moves.length){
            this.setCapacity(moves.length * 2);
        }
        this.moves[size].isNull = false;
        this.moves[size].from = from;
        this.moves[size].to = to;
        this.moves[size].pieceFrom = pieceFrom;
        this.moves[size].pieceTo = pieceTo;
        this.moves[size].metaInformation = metaInformation;
        this.size ++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        for(int i = 0; i < this.size; i++){
            if(o.equals(moves[i])) return true;
        }
        return false;
    }

    @Override
    public Iterator<Move> iterator() {
        return new Iterator<Move>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public Move next() {
                return moves[index++];
            }
        };
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(moves, size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean add(Move move) {
        if(size == this.moves.length){
            setCapacity(size * 2);
        }
        this.moves[size] = move;
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean addAll(Collection<? extends Move> c) {
        for(Move m:c){
            moves[size] = m;
            size++;
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Move> c) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void forEach(Consumer<? super Move> action) {
        for(int i = 0; i < size; i++){
            action.accept(moves[i]);
        }
    }

    @Override
    public Spliterator<Move> spliterator() {
        throw new RuntimeException("Not yet implemented");
    }
}
