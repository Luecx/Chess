package board.bitboards;

import java.util.Random;

public class BitBoard {

    public static final int index64[] = {
                0, 47,  1, 56, 48, 27,  2, 60,
                57, 49, 41, 37, 28, 16,  3, 61,
                54, 58, 35, 52, 50, 42, 21, 44,
                38, 32, 29, 23, 17, 11,  4, 62,
                46, 55, 26, 59, 40, 36, 15, 53,
                34, 51, 20, 43, 31, 22, 10, 45,
                25, 39, 14, 33, 19, 30,  9, 24,
                13, 18,  8, 12,  7,  6,  5, 63
    };
    public static final long debruijn64    = 0x03f79d71b4cb0a89L;


    public static final long not_a_file    = 0xfefefefefefefefeL;
    public static final long not_h_file    = 0x7f7f7f7f7f7f7f7fL;

    public static final long empty_set     = 0x0000000000000000L;
    public static final long universal_set = 0xffffffffffffffffL;





    public static final long seed = 1123891283;

    public static final long[][] all_hashes;        //12 * 64
    public static final long[][] white_hashes;      //6 * 64
    public static final long[][] black_hashes;      //6 * 64




    public static final long[] KNIGHT_TABLE;
    public static final long[] ROOK_TABLE;

    public static final SlidingPieceBuffer[] ROOK_BUFFER;

    static {
        KNIGHT_TABLE = new long[64];
        ROOK_TABLE = new long[64];

        ROOK_BUFFER = new SlidingPieceBuffer[64];

        for (int i = 0; i < 64; i++) {
            for (int[] k : new int[][]{{1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1}}) {
                int x = fileIndex(i);
                int y = rankIndex(i);
                int nx = x + k[0];
                int ny = y + k[1];
                if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    KNIGHT_TABLE[i] = setBit(KNIGHT_TABLE[i], squareIndex(ny, nx));
                }
            }
        }




        for (int i = 0; i < 64; i++) {
            long p = setBit(0, i);
            long m = 0L;
            for (int n = 0; n < 7; n++) {
                m = setBit(m, squareIndex(n, fileIndex(i)));
                m = setBit(m, squareIndex(rankIndex(i), n));
            }
            //ROOK_TABLE[i] = new SlidingPieceBuffer(p, m, );
        }


        Random r = new Random(seed);

        white_hashes = new long[6][64];
        black_hashes = new long[6][64];
        all_hashes = new long[12][64];

        for(int i = 0; i < 6; i++){
            for(int n = 0; n < 64; n++){
                white_hashes[i][n] = r.nextLong();
                black_hashes[i][n] = r.nextLong();
            }
            all_hashes[i] = white_hashes[i];
            all_hashes[i+6] = black_hashes[i];
        }
    }

    public static final void printBitmap(long number) {
        String s = Long.toBinaryString(number);
        for (int i = 7; i >= 0; i--) {
            for (int n = 0; n < 8; n++) {
                //System.out.print(antiDiagonalIndex(squareIndex(i,n)) + " ");
                System.out.print(getBit(number, squareIndex(i,n)) ? 1:0);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static final int rankIndex(int square_index) {
        return square_index >>> 3;
    }

    public static final int fileIndex(int square_index) {
        return square_index & 7;
    }

    public static final int squareIndex(int rank, int file) {
        return 8 * rank + file;
    }

    public static final int diagonalIndex(int square_index) {
        return (rankIndex(square_index) - fileIndex(square_index)) & 15;
    }

    public static final int antiDiagonalIndex(int square_index) {
        return (rankIndex(square_index) - fileIndex(square_index)) ^ 15;
    }

    /**
     * toggles the bit
     * @param number    number to manipulate
     * @param index     index of bit starting at the LST
     * @return          the manipulated number
     */
    public static final long toggleBit(long number, int index) {
        return (number ^= (1L << index));
    }

    /**
     * set the bit
     * @param number    number to manipulate
     * @param index     index of bit starting at the LST
     * @return          the manipulated number
     */
    public static final long setBit(long number, int index) {
        return (number |= (1L << index));
    }

    /**
     * unset the bit
     * @param number    number to manipulate
     * @param index     index of bit starting at the LST
     * @return          the manipulated number
     */
    public static final long unsetBit(long number, int index) {
        return (number &= ~(1L << index));
    }

    /**
     * get the bit
     * @param number    number to manipulate
     * @param index     index of bit starting at the LST
     * @return          the manipulated number
     */
    public static final boolean getBit(long number, int index) {
        return ((number >>> index) & 1) == 1 ? true : false;
    }


    /**
     * isolates the lsb:
     *
     *       x          &        -x         =     LS1B_of_x
     * . . . . . . . .     1 1 1 1 1 1 1 1     . . . . . . . .
     * . . 1 . 1 . . .     1 1 . 1 . 1 1 1     . . . . . . . .
     * . 1 . . . 1 . .     1 . 1 1 1 . 1 1     . . . . . . . .
     * . . . . . . . .     1 1 1 1 1 1 1 1     . . . . . . . .
     * . 1 . . . 1 . .  &  1 . 1 1 1 . 1 1  =  . . . . . . . .
     * . . 1 . 1 . . .     . . 1 1 . 1 1 1     . . 1 . . . . .
     * . . . . . . . .     . . . . . . . .     . . . . . . . .
     * . . . . . . . .     . . . . . . . .     . . . . . . . .
     *
     *
     * @param number
     * @return
     */
    public static final long lsbIsolation(long number){
        return number & -number;
    }

    /**
     * resets the lsb:
     *
     *       x          &      (x-1)        =  x_with_reset_LS1B
     * . . . . . . . .     . . . . . . . .     . . . . . . . .
     * . . 1 . 1 . . .     . . 1 . 1 . . .     . . 1 . 1 . . .
     * . 1 . . . 1 . .     . 1 . . . 1 . .     . 1 . . . 1 . .
     * . . . . . . . .     . . . . . . . .     . . . . . . . .
     * . 1 . . . 1 . .  &  . 1 . . . 1 . .  =  . 1 . . . 1 . .
     * . . 1 . 1 . . .     1 1 . . 1 . . .     . . . . 1 . . .
     * . . . . . . . .     1 1 1 1 1 1 1 1     . . . . . . . .
     * . . . . . . . .     1 1 1 1 1 1 1 1     . . . . . . . .
     * @param number
     * @return
     */
    public static final long lsbReset(long number){
        return number & (number-1);
    }




    public static final long xor(long a, long b) {
        return a ^ b;
    }
    public static final long or(long a, long b) {
        return a | b;
    }
    public static final long and(long a, long b) {
        return a & b;
    }
    public static final long not(long a) {
        return ~a;
    }


    public static final long shiftWest(long b) {
        return (b >>> 1) & not_h_file;
    }

    public static final long shiftEast(long b) {
        return (b << 1) & not_a_file;
    }

    public static final long shiftSouth(long b) {
        return b >>> 8;
    }

    public static final long shiftNorth(long b) {
        return b << 8;
    }



    public static final long shiftNorthEast(long b) {
        return (b << 9) & not_a_file;
    }

    public static final long shiftSouthEast(long b) {
        return (b >>> 7) & not_a_file;
    }



    public static final long shiftSouthWest(long b) {
        return (b >>> 9) & not_h_file;
    }

    public static final long shiftNorthWest(long b) {
        return (b << 7) & not_h_file;
    }

    /**
     * returns the index of the LSB
     * @param bb
     * @return
     */
    public static final int bitscanForward(long bb){
        assert bb != 0;
        return index64[(int)(((bb ^ (bb-1L)) * debruijn64) >>> 58)];
    }

    /**
     * returns the index of the MSB
     * @param bb
     * @return
     */
    public static final int bitscanReverse(long bb){
        assert bb != 0;
        bb |= bb >>> 1;
        bb |= bb >>> 2;
        bb |= bb >>> 4;
        bb |= bb >>> 8;
        bb |= bb >>> 16;
        bb |= bb >>> 32;
        return index64[(int)((bb * debruijn64) >>> 58)];
    }

    public static void main(String[] args) {


        printBitmap(setBit(0,63));
        printBitmap(lsbReset(setBit(0,63)));
        printBitmap(lsbIsolation(setBit(0,63)));


    }
}
