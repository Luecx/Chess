package ai.tools.transpositions;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class TranspositionTable<V> extends Hashtable<Long, V> {//extends HashMap<Long, V> {

    public TranspositionTable(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public TranspositionTable(int initialCapacity) {
        super(initialCapacity);
    }

    public TranspositionTable() {
    }

    public TranspositionTable(Map<? extends Long, ? extends V> t) {
        super(t);
    }
//    public TranspositionTable(int initialCapacity, float loadFactor) {
//        super(initialCapacity, loadFactor);
//    }
//
//    public TranspositionTable(int initialCapacity) {
//        super(initialCapacity);
//    }
//
//    public TranspositionTable() {
//    }
//
//    public TranspositionTable(Map<? extends Long, ? extends V> m) {
//        super(m);
//    }

//    private V[] ar;
//    private int size;
//
//    private int maxSize;
//    private long hashMask;
//
//    public TranspositionTable(int keyBits){
//        this.maxSize = (int)(Math.pow(2, keyBits));
//        this.hashMask =  maxSize - 1;
//        this.ar = (V[]) new Object[maxSize];
//    }
//
//    public V get(long key){
//        int index = (int)(hashMask & key);
//        return ar[index];
//    }
//
//    public void clear(){
//        for(int i = 0; i < ar.length; i++){
//            ar[i] = null;
//        }
//    }
//
//    public void remove(long key){
//        int index = (int)(hashMask & key);
//        ar[index] = null;
//    }
//
//    public int size(){
//        return size;
//    }
//
//    public void put(long key, V value){
//        if(size() >= maxSize){
//            return;
//        }
//        int index = (int)(hashMask & key);
//        if(ar[index] == null){
//            size++;
//        }
//        ar[index] = value;
//    }


}
