package board.bitboards;

import java.util.Random;

public class SlidingPieceBuffer {

    private long attack;
    private long magic;
    private long offset;

    public SlidingPieceBuffer(long attack, long magic, long offset) {
        this.attack = attack;
        this.magic = magic;
        this.offset = offset;
    }

    public void generateMagic() {
        Random r = new Random();


        long magic;
        while(true){
            magic = r.nextLong();
            for(int i = 0; i < Math.pow(2, offset); i++){
                long occ = fillAttackWithSample(attack, i);
                long key = occ;
                //TODO
            }
        }
    }

    private static long fillAttackWithSample(long attack, int index){
        long res = 0;
        int i = 0;

        while(attack != 0){
            int bit = BitBoard.bitscanForward(attack);
            if(BitBoard.getBit(index, i))
                res = BitBoard.setBit(res, bit);
            attack = BitBoard.lsbIsolation(attack);
            index ++;
        }

        return res;
    }

    public long getAttack() {
        return attack;
    }

    public void setAttack(long attack) {
        this.attack = attack;
    }

    public long getMagic() {
        return magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
