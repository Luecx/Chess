package board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BitBoard {




    static final long not_a_file = 0xfefefefefefefefeL;
    static final long not_h_file = 0x7f7f7f7f7f7f7f7fL;

    static final long seed = 1123891283;

    static final long[][] all_hashes;        //12 * 64
    static final long[][] white_hashes;      //6 * 64
    static final long[][] black_hashes;      //6 * 64

    static final long[] KNIGHT_TABLE;
    static final SlidingPieceBuffer[] ROOK_TABLE;

    static {
        KNIGHT_TABLE = new long[64];
        for (int i = 0; i < 64; i++) {
            for (int[] k : new int[][]{{1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1}}) {
                int x = fileIndex(i);
                int y = rankIndex(i);
                int nx = x + k[0];
                int ny = y + k[1];
                if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    KNIGHT_TABLE[i] = set_bit(KNIGHT_TABLE[i], squareIndex(ny, nx));
                }
            }
        }
        ROOK_TABLE = new SlidingPieceBuffer[64];
        for (int i = 0; i < 64; i++) {
            long p = set_bit(0, i);
            long m = 0L;
            for (int n = 0; n < 7; n++) {
                m = set_bit(m, squareIndex(n, fileIndex(i)));
                m = set_bit(m, squareIndex(rankIndex(i), n));
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

    static final void print_bits(long number) {
        String s = Long.toBinaryString(number);
        String zeros = "0000000000000000000000000000000000000000000000000000000000000000"; //String of 64 zeros
        s = zeros.substring(s.length()) + s;
        for (int i = 7; i >= 0; i--) {
            for (int n = 0; n < 8; n++) {
                //System.out.print(antiDiagonalIndex(squareIndex(i,n)) + " ");
                System.out.print(s.charAt(squareIndex(i, n)));
            }
            System.out.println();
        }
        System.out.println();
    }

    static final int rankIndex(int square_index) {
        return square_index >> 3;
    }

    static final int fileIndex(int square_index) {
        return square_index & 7;
    }

    static final int squareIndex(int rank, int file) {
        return 8 * rank + file;
    }

    static final int diagonalIndex(int square_index) {
        return (rankIndex(square_index) - fileIndex(square_index)) & 15;
    }

    static final int antiDiagonalIndex(int square_index) {
        return (rankIndex(square_index) - fileIndex(square_index)) ^ 15;
    }

    static final long toggle_bit(long number, int index) {
        return (number ^= (1L << (63 - index)));
    }

    static final long set_bit(long number, int index) {
        return (number |= (1L << (63 - index)));
    }

    static final long unset_bit(long number, int index) {
        return (number &= ~(1L << (63 - index)));
    }

    static final boolean get_bit(long number, int index) {
        return ((number >> (63 - index)) & 1) == 1 ? true : false;
    }

    static final long xor(long a, long b) {
        return a ^ b;
    }

    static final long or(long a, long b) {
        return a | b;
    }

    static final long and(long a, long b) {
        return a & b;
    }

    static final long not(long a) {
        return ~a;
    }

    static final long shift_south(long b) {
        return b >> 8;
    }

    static final long shift_north(long b) {
        return b << 8;
    }

    static final long shift_east(long b) {
        return (b << 1) & not_a_file;
    }

    static final long shift_north_east(long b) {
        return (b << 9) & not_a_file;
    }

    static final long shift_south_east(long b) {
        return (b >> 7) & not_a_file;
    }

    static final long shift_west(long b) {
        return (b >> 1) & not_h_file;
    }

    static final long shift_south_west(long b) {
        return (b >> 9) & not_h_file;
    }

    static final long shift_north_west(long b) {
        return (b << 7) & not_h_file;
    }

    static final class SlidingPieceBuffer {
        long pointer;
        long mask;
        long magic;
        int shift;
    }
}
