package board.bitboards;

import java.util.Random;

public class BitBoard {

    public static final long h_file                                 = 0x8080808080808080L;
    public static final long g_file                                 = h_file >>> 1;
    public static final long f_file                                 = h_file >>> 2;
    public static final long e_file                                 = h_file >>> 3;
    public static final long d_file                                 = h_file >>> 4;
    public static final long c_file                                 = h_file >>> 5;
    public static final long b_file                                 = h_file >>> 6;
    public static final long a_file                                 = h_file >>> 7;

    public static final long rank_1                                 = 0x00000000000000FFL;
    public static final long rank_2                                 = rank_1 << 8;
    public static final long rank_3                                 = rank_1 << 16;
    public static final long rank_4                                 = rank_1 << 24;
    public static final long rank_5                                 = rank_1 << 32;
    public static final long rank_6                                 = rank_1 << 40;
    public static final long rank_7                                 = rank_1 << 48;
    public static final long rank_8                                 = rank_1 << 56;

    public static final long anti_diagonal_7                        = 0x102040810204080L;
    public static final long anti_diagonal_6                        = shiftNorth(anti_diagonal_7);
    public static final long anti_diagonal_5                        = shiftNorth(anti_diagonal_6);
    public static final long anti_diagonal_4                        = shiftNorth(anti_diagonal_5);
    public static final long anti_diagonal_3                        = shiftNorth(anti_diagonal_4);
    public static final long anti_diagonal_2                        = shiftNorth(anti_diagonal_3);
    public static final long anti_diagonal_1                        = shiftNorth(anti_diagonal_2);
    public static final long anti_diagonal_0                        = shiftNorth(anti_diagonal_1);

    public static final long anti_diagonal_8                        = shiftNorth(anti_diagonal_7);
    public static final long anti_diagonal_9                        = shiftNorth(anti_diagonal_8);
    public static final long anti_diagonal_10                       = shiftNorth(anti_diagonal_9);
    public static final long anti_diagonal_11                       = shiftNorth(anti_diagonal_10);
    public static final long anti_diagonal_12                       = shiftNorth(anti_diagonal_11);
    public static final long anti_diagonal_13                       = shiftNorth(anti_diagonal_12);
    public static final long anti_diagonal_14                       = shiftNorth(anti_diagonal_13);

    public static final long diagonal_7                             = 0x8040201008040201L;
    public static final long diagonal_8                             = shiftNorth(diagonal_7);
    public static final long diagonal_9                             = shiftNorth(diagonal_8);
    public static final long diagonal_10                            = shiftNorth(diagonal_9);
    public static final long diagonal_11                            = shiftNorth(diagonal_10);
    public static final long diagonal_12                            = shiftNorth(diagonal_11);
    public static final long diagonal_13                            = shiftNorth(diagonal_12);
    public static final long diagonal_14                            = shiftNorth(diagonal_13);

    public static final long diagonal_6                             = shiftSouth(diagonal_7);
    public static final long diagonal_5                             = shiftSouth(diagonal_6);
    public static final long diagonal_4                             = shiftSouth(diagonal_5);
    public static final long diagonal_3                             = shiftSouth(diagonal_4);
    public static final long diagonal_2                             = shiftSouth(diagonal_3);
    public static final long diagonal_1                             = shiftSouth(diagonal_2);
    public static final long diagonal_0                             = shiftSouth(diagonal_1);

    public static final long not_a_file                             = ~a_file;
    public static final long not_h_file                             = ~h_file;
    public static final long not_rank_1                             = ~rank_1;
    public static final long not_rank_8                             = ~rank_8;

    public static final long circle_a                               = 0xFF818181818181FFL;
    public static final long circle_b                               = 0x7E424242427E00L;
    public static final long circle_c                               = 0x3C24243C0000L;
    public static final long circle_d                               = 0x1818000000L;

    public static final long[] ranks = new long[]{rank_1, rank_2, rank_3, rank_4, rank_5, rank_6, rank_7, rank_8};
    public static final long[] files = new long[]{a_file, b_file, c_file, d_file, e_file, f_file, g_file, h_file};
    public static final long[] circles = new long[]{circle_a, circle_b, circle_c, circle_d};
    public static final long[] diagonals = new long[]{
            diagonal_0, diagonal_1, diagonal_2, diagonal_3, diagonal_4,
            diagonal_5, diagonal_6, diagonal_7, diagonal_8, diagonal_9,
            diagonal_10, diagonal_11, diagonal_12, diagonal_13, diagonal_14};
    public static final long[] antiDiagonals = new long[]{
            anti_diagonal_0, anti_diagonal_1, anti_diagonal_2, anti_diagonal_3, anti_diagonal_4,
            anti_diagonal_5, anti_diagonal_6, anti_diagonal_7, anti_diagonal_8, anti_diagonal_9,
            anti_diagonal_10, anti_diagonal_11, anti_diagonal_12, anti_diagonal_13, anti_diagonal_14};
    
    public static final long empty_set                              = 0x0000000000000000L;
    public static final long universal_set                          = 0xffffffffffffffffL;


    public static final long[] bishopMagics = new long[]{
            0x1102a0040920c000L, 0x4150660228400014L, 0x4150660228400014L, 0xc004218020108000L,
            0xc004218020108000L, 0x0800004011000000L, 0x0800004011000000L, 0x202284290090000aL,
            0x0c214c0802008010L, 0x4000890104208004L, 0x4150660228400014L, 0x0220042080001004L,
            0x0800004011000000L, 0x0800004011000000L, 0x0c80040401281800L, 0x0000010401014000L,
            0x0000051660040000L, 0x08280088c1081002L, 0x0080040104018200L, 0x4150660228400014L,
            0x0800004011000000L, 0x4010008031020009L, 0x041090010a014080L, 0x0080040104018200L,
            0x4010008031020009L, 0x8002068108500000L, 0x0008710044026000L, 0x0000010401014000L,
            0x00100011c0280000L, 0x0000080042804001L, 0x300015000080a000L, 0x300015000080a000L,
            0xc004218020108000L, 0xc004218020108000L, 0x0080040104018200L, 0x0000002010608000L,
            0x0000020060820000L, 0x4010008031020009L, 0x0000051660040000L, 0xc004218020108000L,
            0x0800004011000000L, 0x0800004011000000L, 0x4010008031020009L, 0x0000080042804001L,
            0x0000002010608000L, 0x0080040104018200L, 0x0080040104018200L, 0x0000080042804001L,
            0x0800004011000000L, 0x0c80040401281800L, 0x0080040104018200L, 0x2081124c01040200L,
            0x0080040104018200L, 0x2081124c01040200L, 0x2081124c01040200L, 0x0200104202002008L,
            0x0c80040401281800L, 0x0c80040401281800L, 0x0080040104018200L, 0x0080040104018200L,
            0x0040000480280104L, 0x0080040104018200L, 0xf041000204040081L, 0x0000101081850044L
    };

    public static final long[] rookMagics = new long[]{
            0x0800004011000000L, 0x00002a0080400120L, 0x0000100841840000L, 0x8002068108500000L,
            0x0000080042804001L, 0x000062a010320801L, 0x000062a010320801L, 0x0800004011000000L,
            0x4001403080000000L, 0x0004004082440000L, 0x300c001403008022L, 0x4284000c0060c000L,
            0x4284000c0060c000L, 0x00880004b0400800L, 0x0041800100921800L, 0x8002068108500000L,
            0x4480002000000012L, 0x0080040104018200L, 0x0000020060820000L, 0x208020841000a002L,
            0x0000020800081000L, 0x0100180004106006L, 0x002809000c020000L, 0x0172180490000000L,
            0x0000002010608000L, 0x0000002010608000L, 0xe000011200220004L, 0xe000011200220004L,
            0xe000011200220004L, 0xe000011200220004L, 0x1080000d80031000L, 0x1142000040000000L,
            0x0800004011000000L, 0x0080040104018200L, 0x2000004121480000L, 0x0000002010608000L,
            0x0000002010608000L, 0x0800000a40840000L, 0x0000001024208000L, 0x8002000028404000L,
            0x0000002010608000L, 0x0000002010608000L, 0x0000002010608000L, 0xe000011200220004L,
            0xe000011200220004L, 0xe000011200220004L, 0x0000100841840000L, 0x0010400008000800L,
            0x0000000000000200L, 0x0000008400402400L, 0x0000008400402400L, 0x0000008400402400L,
            0x0000008400402400L, 0x0048810a00040080L, 0x0000081042012400L, 0x0000000000000200L,
            0x0800004011000000L, 0x041090010a014080L, 0x041090010a014080L, 0x041090010a014080L,
            0xa18020104202c200L, 0x041090010a014080L, 0x0080040104018200L, 0x0800004011000000L
    };
    public static final boolean GENERATE_MAGICS                     = false;     //if set to false, the magics from the tables will be used
    public static final boolean VALIDATE_MAGICS                     = false;


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
    public static final long debruijn64                             = 0x03f79d71b4cb0a89L;
    public static final long seed                                   = 1123891283;

    public static final long[][] all_hashes                         = new long[12][64];        //12 * 64
    public static final long[][] white_hashes                       = new long[6][64];      //6 * 64
    public static final long[][] black_hashes                       = new long[6][64];      //6 * 64


    public static final long[]                      KNIGHT_ATTACKS  = new long[64];
    public static final long[][]                    ROOK_ATTACKS    = new long[64][];
    public static final long[][]                    BISHOP_ATTACKS  = new long[64][];

    public static final SlidingPieceBuffer[]        ROOK_BUFFER     = new SlidingPieceBuffer[64];
    public static final SlidingPieceBuffer[]        BISHOP_BUFFER   = new SlidingPieceBuffer[64];

    static {
        generateBuffers();
        if(VALIDATE_MAGICS && !validateMagics())
            System.out.println("[Error] The magic numbers can not be used.");
        generateZobristKeys();
        generateAttackTables();
    }

    public static void generateZobristKeys() {
        Random r = new Random(seed);
        for(int i = 0; i < 6; i++){
            for(int n = 0; n < 64; n++){
                white_hashes[i][n] = r.nextLong();
                black_hashes[i][n] = r.nextLong();
            }
            all_hashes[i] = white_hashes[i];
            all_hashes[i+6] = black_hashes[i];
        }
    }

    public static void generateBuffers() {

        for (int i = 0; i < 64; i++){
            int file = fileIndex(i);
            int rank = rankIndex(i);

            long relevant = (diagonals[diagonalIndex(rank, file)] | antiDiagonals[antiDiagonalIndex(rank, file)]) & ~(1L << i);
            if(file != 0){
                relevant &= not_a_file;
            }if(file != 7){
                relevant &= not_h_file;
            }if(rank != 0){
                relevant &= not_rank_1;
            }if(rank != 7){
                relevant &= not_rank_8;
            }
            if(GENERATE_MAGICS){
                BISHOP_BUFFER[i] = new SlidingPieceBuffer(relevant,
                        generateMagic(relevant, i,bitCount(relevant), true), bitCount(relevant));
            }else{
                BISHOP_BUFFER[i] = new SlidingPieceBuffer(relevant, bishopMagics[i], bitCount(relevant));
            }
        }

        for (int i = 0; i < 64; i++){
            int file = fileIndex(i);
            int rank = rankIndex(i);

            long relevant = (files[file] | ranks[rank]) & ~(1L << i);
            if(file != 0){
                relevant &= not_a_file;
            }if(file != 7){
                relevant &= not_h_file;
            }if(rank != 0){
                relevant &= not_rank_1;
            }if(rank != 7){
                relevant &= not_rank_8;
            }
            if(GENERATE_MAGICS){
                ROOK_BUFFER[i] = new SlidingPieceBuffer(relevant,
                        generateMagic(relevant, i,bitCount(relevant), false), bitCount(relevant));
            }else{
                ROOK_BUFFER[i] = new SlidingPieceBuffer(relevant, rookMagics[i], bitCount(relevant));
            }
        }

        for (int i = 0; i < 64; i++) {
            for (int[] k : new int[][]{{1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1}}) {
                int x = fileIndex(i);
                int y = rankIndex(i);
                int nx = x + k[0];
                int ny = y + k[1];
                if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    KNIGHT_ATTACKS[i] = setBit(KNIGHT_ATTACKS[i], squareIndex(ny, nx));
                }
            }
        }

    }

    public static boolean validateMagics() {
        for(int i = 0; i < 64; i++){
            if(!isValidMagic(ROOK_BUFFER[i].getMask(), ROOK_BUFFER[i].getOffset(),
                    ROOK_BUFFER[i].getMagic(),i,false)) return false;
            if(!isValidMagic(BISHOP_BUFFER[i].getMask(), BISHOP_BUFFER[i].getOffset(),
                    BISHOP_BUFFER[i].getMagic(),i,true)) return false;
        }
        return true;
    }

    public static void generateAttackTables() {
        for(int n = 0; n < 64; n++){
            ROOK_ATTACKS[n] = new long[(int)Math.pow(2, ROOK_BUFFER[n].getOffset())];
            for(int i = 0; i < (int)Math.pow(2, ROOK_BUFFER[n].getOffset()); i++){
                long rel_occ = BitBoard.populateMask(ROOK_BUFFER[n].getMask(), i);
                int index =(int)((rel_occ * ROOK_BUFFER[n].getMagic()) >>> (64-ROOK_BUFFER[n].getOffset()));
                ROOK_ATTACKS[n][index] = BitBoard.generateRookAttack(n, rel_occ);
            }
            BISHOP_ATTACKS[n] = new long[(int)Math.pow(2, BISHOP_BUFFER[n].getOffset())];
            for(int i = 0; i < (int)Math.pow(2, BISHOP_BUFFER[n].getOffset()); i++){
                long rel_occ = BitBoard.populateMask(BISHOP_BUFFER[n].getMask(), i);
                int index =(int)((rel_occ * BISHOP_BUFFER[n].getMagic()) >>> (64-BISHOP_BUFFER[n].getOffset()));
                BISHOP_ATTACKS[n][index] = BitBoard.generateBishopAttack(n, rel_occ);
            }
        }

    }


    public static long lookUpRookAttack(int index, long occupied){
        SlidingPieceBuffer buff = ROOK_BUFFER[index];
        return ROOK_ATTACKS[index][(int)((occupied & buff.getMask()) *
                buff.getMagic() >>> (64 - buff.getOffset()))];
    }

    public static long lookUpBishopAttack(int index, long occupied){
        SlidingPieceBuffer buff = BISHOP_BUFFER[index];
        return BISHOP_ATTACKS[index][(int)((occupied & buff.getMask()) *
                buff.getMagic() >>> (64 - buff.getOffset()))];
    }

    public static long generateAttack(int index, int direction, long occupied){
        if((BitBoard.circle_a & (1L << index)) > 0){
            if(index + direction >= 64 || index + direction < 0 || (BitBoard.circle_a & (1L << (index+direction))) > 0){
                return 0L;
            }
        }
        long m = BitBoard.setBit(0L, index+direction);
        while((BitBoard.circle_a & (1L << index+direction)) == 0){
            m = BitBoard.setBit(m, index + direction);
            index += direction;
            if(BitBoard.getBit(occupied, index)){
                break;
            }
        }
        return m;
    }

    public static long generateRookAttack(int index, long occupied){
        return generateAttack(index, 1, occupied) |
                generateAttack(index, -1, occupied) |
                generateAttack(index, 8, occupied) |
                generateAttack(index, -8, occupied);
    }

    public static long generateBishopAttack(int index, long occupied){
        return generateAttack(index, 7, occupied) |
                generateAttack(index, 9, occupied) |
                generateAttack(index, -7, occupied) |
                generateAttack(index, -9, occupied);
    }


    /**
     * generates some random long number
     * @param random
     * @return
     */
    public static long randomLong(Random random) {
        long u1, u2, u3, u4;
        u1 = (long)(random.nextLong()) & 0xFFFF; u2 = (long)(random.nextLong()) & 0xFFFF;
        u3 = (long)(random.nextLong()) & 0xFFFF; u4 = (long)(random.nextLong()) & 0xFFFF;
        return u1 | (u2 << 16) | (u3 << 32) | (u4 << 48);
    }

    /**
     * generates a random long number with few ones.
     * @param random
     * @return
     */
    public static long randomLongSparseBits(Random random) {
        return randomLong(random) & randomLong(random) & randomLong(random);
    }

    /**
     * generates a perfect magic hash.
     * it requires a mask of the relevant blockers.
     * the offset should amount the amount of bits in the mask.
     * it needs to know if the magic is for a rook or a bishop
     * @param mask
     * @param squareIndex
     * @param offset
     * @param bishop
     * @return
     */
    public static long generateMagic(long mask, int squareIndex, int offset, boolean bishop) {
        Random r = new Random(seed);
        long magic = randomLongSparseBits(r);
        while(!BitBoard.isValidMagic(mask, offset, magic, squareIndex, bishop)){
            magic = randomLongSparseBits(r);
        }
        System.out.println(String.format("0x%016x",magic) +"L,");
        return magic;
    }

    /**
     * checks if the given magic is valid.
     * it requires the relevant occupancy mask and the bit count in that mask.
     * @param relevant
     * @param offset
     * @param magic
     * @return
     */
    public static boolean isValidMagic(long relevant, int offset, long magic, int squareIndex, boolean bishop){
        long[] values = new long[(int)Math.pow(2, offset)];
        for(int i = 0; i < (int)Math.pow(2, offset); i++){
            long rel_occ = BitBoard.populateMask(relevant, i);
            int index =(int)((rel_occ * magic) >>> (64-offset));
            if(values[index] != 0) {
                if(values[index] ==
                        (bishop ? BitBoard.generateBishopAttack(squareIndex, rel_occ) :
                                BitBoard.generateRookAttack(squareIndex, rel_occ))){
                    continue;
                }else{
                    return false;
                }
            }
            else{
                values[index] = (bishop ? BitBoard.generateBishopAttack(squareIndex, rel_occ) :
                        BitBoard.generateRookAttack(squareIndex, rel_occ));
            }
        }
        return true;
    }

    /**
     * populates the given bitboard with 1 and zeros.
     * The population is unique for the given index.
     * A 1 or 0 will only be placed where there is a 1 in the input.
     * @param mask
     * @param index
     * @return
     */
    public static long populateMask(long mask, int index){
        long res = 0;
        int i = 0;

        while(mask != 0){
            int bit = BitBoard.bitscanForward(mask);
            if(BitBoard.getBit(index, i))
                res = BitBoard.setBit(res, bit);
            mask = BitBoard.lsbReset(mask);
            i ++;
        }

        return res;
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
        return 7 + rankIndex(square_index) - fileIndex(square_index);
    }

    public static final int antiDiagonalIndex(int square_index) {
        return rankIndex(square_index) + fileIndex(square_index);
    }

    public static final int diagonalIndex(int rank, int file){
        return 7 + rank - file;
    }

    public static final int antiDiagonalIndex(int rank, int file){
        return rank + file;
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

    /**
     * returns the amount of set bits in the given bitboard.
     * @param bb
     * @return
     */
    public static final int bitCount(long bb) {
        int counter = 0;
        while(bb > 0){
            bb = lsbReset(bb);
            counter ++;
        }
        return counter;
    }



    public static void main(String[] args) {
//        long mg = 0x0080081000200080L;
//        long relOcc = ROOK_BUFFER[2].getMask();
//        int offset = ROOK_BUFFER[2].getOffset();

        long occupied = 0x1238912312893121L;
        BitBoard.printBitmap(occupied);

        long time = System.currentTimeMillis();
        long total = 0;
        for(int i = 0; i < 10000000; i++){
            total +=bitCount(lookUpRookAttack(i%64, occupied));
        }
        System.out.println(total);
        System.out.println(System.currentTimeMillis()-time);
        //System.out.println(SlidingPieceBuffer.isValidMagic(relOcc, offset, mg, 2, false));

        //System.out.println(SlidingPieceBuffer.isValidMagic(relOcc,offset, mg));



    }


}
