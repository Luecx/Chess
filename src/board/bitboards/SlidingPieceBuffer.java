package board.bitboards;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SlidingPieceBuffer {

    private long mask;
    private long magic;
    private int offset;


    public SlidingPieceBuffer(long mask, long magic, int offset) {
        this.mask = mask;
        this.magic = magic;
        this.offset = offset;
    }


    public long getMask() {
        return mask;
    }

    public void setMask(long mask) {
        this.mask = mask;
    }

    public long getMagic() {
        return magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
