package ai.tools;

import java.util.Arrays;

public class TranspositionTable<V> {



    private int[] table;
    private int[] nextPtrs;
    private long[] hashValues;
    private V[] elements;
    private int nextHashValuePos;
    private int hashMask;
    private int size;

    public TranspositionTable(int maxElements) {
        int sz = 128;
        int desiredTableSize = maxElements * 4 / 3;
        while (sz < desiredTableSize) sz <<= 1;
        this.table = new int[sz];
        this.nextPtrs = new int[maxElements];
        this.hashValues = new long[maxElements];
        this.elements = (V[]) new Object[sz];
        Arrays.fill(table, -1);
        this.hashMask = sz - 1;
    }

    public V get(long key) {
        long hash = key;
        int hc = (int) hash & hashMask;
        int k = table[hc];
        if (k != -1)
            do {
                if (hashValues[k] == hash)
                    return elements[k];
                k = nextPtrs[k];
            } while (k != -1);
        return null;
    }

    public void clear() {

    }

    public V put(long key, V val) {
        long hash = key;
        int hc = (int) hash & hashMask;
        int k = table[hc];
        if (k == -1) {
            // Start a new bucket: none there before
            k = nextHashValuePos++;
            table[hc] = k;
        } else {
            // traverse the bucket, looking for a matching hash
            int lastk;
            do {
                if (hashValues[k] == hash) {
                    V old = elements[k];
                    elements[k] = val;
                    return old;
                }
                lastk = k;
                k = nextPtrs[k];
            } while (k != -1);
            // ... if not there, append to end of bucket
            k = nextHashValuePos++;
            nextPtrs[lastk] = k;
        }
        // Append value, either to end of bucket or
        // to start of new bucket
        hashValues[k] = hash;
        nextPtrs[k] = -1;
        elements[k] = val;
        size++;
        return null;
    }

    public boolean isFull(){
        return size > elements.length - 10;
    }

    public int getSize() {
        return size;
    }

}
