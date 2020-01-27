package board.bitboards;

public class SlidingPieceBuffer {

    private long mask;
    private long magic;
    private int shift;


    public SlidingPieceBuffer(long mask, long magic, int shift) {
        this.mask = mask;
        this.magic = magic;
        this.shift = shift;
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

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }
}
