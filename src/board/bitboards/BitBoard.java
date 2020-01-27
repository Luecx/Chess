package board.bitboards;

import com.sun.org.apache.bcel.internal.generic.NEW;
import sun.misc.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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


    public static final long[] bishopRelevantOccupancy = new long[] {
            0x0040201008040200L, 0x0000402010080400L, 0x0040004020100800L, 0x0020400040201000L,
            0x0010204000402000L, 0x0008102040004000L, 0x0004081020400000L, 0x0002040810204000L,
            0x0020100804020000L, 0x0040201008040000L, 0x0020402010080000L, 0x0010204020100000L,
            0x0008102040200000L, 0x0004081020400000L, 0x0002040810200000L, 0x0004081020400000L,
            0x0050080402000000L, 0x0020500804000000L, 0x0050205008000200L, 0x0008502050000400L,
            0x0004085020400800L, 0x0002040850005000L, 0x0004081020002000L, 0x0008102040004000L,
            0x0028440200000000L, 0x0010284400000000L, 0x0028102840020000L, 0x0044281020440200L,
            0x0002442800284400L, 0x0004085000500800L, 0x0008102000201000L, 0x0010204000402000L,
            0x0014224000000000L, 0x0008142040000000L, 0x0014081022400000L, 0x0022140014224000L,
            0x0044280028440200L, 0x0008500050080400L, 0x0010200020100800L, 0x0020400040201000L,
            0x000a102040000000L, 0x0004081020400000L, 0x000a000a10204000L, 0x0014001422400000L,
            0x0028002844020000L, 0x0050005008040200L, 0x0020002010080400L, 0x0040004020100800L,
            0x0004081020400000L, 0x0000040810204000L, 0x00000a1020400000L, 0x0000142240000000L,
            0x0000284402000000L, 0x0000500804020000L, 0x0000201008040200L, 0x0000402010080400L,
            0x0002040810204000L, 0x0004081020400000L, 0x000a102040000000L, 0x0014224000000000L,
            0x0028440200000000L, 0x0050080402000000L, 0x0020100804020000L, 0x0040201008040200L
    };

    public static final long[] rookRelevantOccupancy = new long[] {
            0x000101010101017eL, 0x000202020202027cL, 0x000404040404047aL, 0x0008080808080876L,
            0x001010101010106eL, 0x002020202020205eL, 0x004040404040403eL, 0x008080808080807eL,
            0x0001010101017e00L, 0x0002020202027c00L, 0x0004040404047a00L, 0x0008080808087600L,
            0x0010101010106e00L, 0x0020202020205e00L, 0x0040404040403e00L, 0x0080808080807e00L,
            0x00010101017e0100L, 0x00020202027c0200L, 0x00040404047a0400L, 0x0008080808760800L,
            0x00101010106e1000L, 0x00202020205e2000L, 0x00404040403e4000L, 0x00808080807e8000L,
            0x000101017e010100L, 0x000202027c020200L, 0x000404047a040400L, 0x0008080876080800L,
            0x001010106e101000L, 0x002020205e202000L, 0x004040403e404000L, 0x008080807e808000L,
            0x0001017e01010100L, 0x0002027c02020200L, 0x0004047a04040400L, 0x0008087608080800L,
            0x0010106e10101000L, 0x0020205e20202000L, 0x0040403e40404000L, 0x0080807e80808000L,
            0x00017e0101010100L, 0x00027c0202020200L, 0x00047a0404040400L, 0x0008760808080800L,
            0x00106e1010101000L, 0x00205e2020202000L, 0x00403e4040404000L, 0x00807e8080808000L,
            0x007e010101010100L, 0x007c020202020200L, 0x007a040404040400L, 0x0076080808080800L,
            0x006e101010101000L, 0x005e202020202000L, 0x003e404040404000L, 0x007e808080808000L,
            0x7e01010101010100L, 0x7c02020202020200L, 0x7a04040404040400L, 0x7608080808080800L,
            0x6e10101010101000L, 0x5e20202020202000L, 0x3e40404040404000L, 0x7e80808080808000L
    };

    public static final int[] bishopShifts = new int[]{
            58, 59, 59, 59, 59, 59, 59, 58,
            59, 59, 59, 59, 59, 59, 59, 59,
            59, 59, 57, 57, 57, 57, 59, 59,
            59, 59, 57, 55, 55, 57, 59, 59,
            59, 59, 57, 55, 55, 57, 59, 59,
            59, 59, 57, 57, 57, 57, 59, 59,
            59, 59, 59, 59, 59, 59, 59, 59,
            58, 59, 59, 59, 59, 59, 59, 58
    };

    public static final int[] rookShifts = new int[]{
            52, 53, 53, 53, 53, 53, 53, 52,
            53, 53, 53, 53, 53, 53, 53, 53,
            53, 53, 53, 53, 53, 53, 53, 53,
            53, 53, 53, 53, 53, 54, 54, 53,
            53, 53, 53, 53, 54, 53, 53, 53,
            53, 54, 53, 53, 53, 53, 53, 53,
            53, 53, 53, 53, 53, 54, 53, 53,
            52, 53, 53, 53, 53, 53, 53, 52};

    public static final long[] bishopMagics = new long[]{
            0xa00c50a401440040L, 0x0060120204450000L, 0x4150660228400014L, 0xc004218020108000L,
            0xc004218020108000L, 0x0164200041003210L, 0x0800004011000000L, 0x0002840280900800L,
            0x00a8449006020410L, 0x0000101081850044L, 0x0050100402b18048L, 0x4150660228400014L,
            0x4010008031020009L, 0xc004218020108000L, 0x0c80040401281800L, 0x4100920090880880L,
            0x8000009202020400L, 0xf041000204040081L, 0x0c214c0802008010L, 0x0c214c0802008010L,
            0x00c0400181800001L, 0x8000202201900800L, 0x08a3008044100400L, 0xd00a4020240c4400L,
            0x000062a010320801L, 0xd00a4020240c4400L, 0x0c214c0802008010L, 0x019108000260b004L,
            0x1810040000806104L, 0x0208060009608401L, 0x0004040800908405L, 0x80060a0080210108L,
            0xd00a4020240c4400L, 0x0820100020200430L, 0x000062a010320801L, 0x0010a01800210114L,
            0x2008020c00001010L, 0x0a08010810010040L, 0x0410028511018410L, 0x0004108060829401L,
            0x0010020900015100L, 0x0010020900015100L, 0x0002020201000200L, 0x8100420122003404L,
            0x0002401814402600L, 0x0164200041003210L, 0x00f2040904040a31L, 0x00050404148000c0L,
            0x0800004011000000L, 0x0ba300c210040c00L, 0x20c0810048122001L, 0x1442000084040048L,
            0x8000009202020400L, 0x0208842408021202L, 0x042004010a0c0024L, 0x0060120204450000L,
            0x0002840280900800L, 0x4100920090880880L, 0x2408100201042908L, 0x200a002000840400L,
            0x0004000010020200L, 0x0000c04204482182L, 0x00a8449006020410L, 0xa00c50a401440040L,
    };

    public static final long[] rookMagics = new long[]{
            0x0800004011000000L, 0x00002a0080400120L, 0x000028312000c044L, 0x0000400801081a08L,
            0x0000400801081a08L, 0x0000400801081a08L, 0x000062a010320801L, 0x0800004011000000L,
            0x4001403080000000L, 0x0489400043192000L, 0x0140401000402089L, 0x0042000c04420425L,
            0x0801000800500b00L, 0x0042000c04420425L, 0x0221201282000201L, 0x0042040886000000L,
            0x0000028000800230L, 0x0040003010802080L, 0x000c002020c08800L, 0x2408901010410004L,
            0x0000020014042200L, 0x0000090004000100L, 0x0001448006004480L, 0x0200818000001010L,
            0x0000001420010002L, 0x4021040c00820441L, 0x0148200080081000L, 0x000e204040240800L,
            0x0008000408024400L, 0x0002820080040080L, 0x0000020080800100L, 0x0042040886000000L,
            0x0000028000800230L, 0x0000420082001101L, 0x000a10000c200011L, 0x0004100021000418L,
            0x1001710501000800L, 0x0034004300400200L, 0x0000040213100100L, 0x0042040886000000L,
            0x0040000000014108L, 0x0200500020084004L, 0x00e0004801011010L, 0x0480881001010004L,
            0x2000104802048020L, 0x0000811008104002L, 0x028821a820c40011L, 0xc018000000020014L,
            0x0000028000800230L, 0x0042100620802010L, 0x4600281005200080L, 0x0000041002080020L,
            0x0002080015001010L, 0x0002820080040080L, 0x70004200040c8100L, 0x0200818000001010L,
            0xc018000000020014L, 0x0084041200802000L, 0x0084041200802000L, 0x0252900184842e00L,
            0x8020800200204500L, 0x8020800200204500L, 0x8020800200204500L, 0xc018000000020014L
    };

    public static final boolean VALIDATE_MAGICS                     = false;
    public static final boolean GENERATE_SLIDING_ATTACKS            = false;
    public static final String  SLIDING_ATTACKS_SOURCE              = "resources/slidingAttacks.txt";
    public static final long    NEW_LINE                            = 0xFFFFFFFFFFFFFFFFL;


    public static final long[][] all_hashes                         = new long[12][64];        //12 * 64
    public static final long[][] white_hashes                       = new long[6][64];      //6 * 64
    public static final long[][] black_hashes                       = new long[6][64];      //6 * 64


    public static final long[]                      KING_ATTACKS    = new long[]{
            0x0000000000000302L, 0x0000000000000704L, 0x0000000000000e08L, 0x0000000000001c10L,
            0x0000000000003820L, 0x0000000000007040L, 0x000000000000e080L, 0x000000000000c000L,
            0x0000000000030203L, 0x0000000000070407L, 0x00000000000e080eL, 0x00000000001c101cL,
            0x0000000000382038L, 0x0000000000704070L, 0x0000000000e080e0L, 0x0000000000c000c0L,
            0x0000000003020300L, 0x0000000007040700L, 0x000000000e080e00L, 0x000000001c101c00L,
            0x0000000038203800L, 0x0000000070407000L, 0x00000000e080e000L, 0x00000000c000c000L,
            0x0000000302030000L, 0x0000000704070000L, 0x0000000e080e0000L, 0x0000001c101c0000L,
            0x0000003820380000L, 0x0000007040700000L, 0x000000e080e00000L, 0x000000c000c00000L,
            0x0000030203000000L, 0x0000070407000000L, 0x00000e080e000000L, 0x00001c101c000000L,
            0x0000382038000000L, 0x0000704070000000L, 0x0000e080e0000000L, 0x0000c000c0000000L,
            0x0003020300000000L, 0x0007040700000000L, 0x000e080e00000000L, 0x001c101c00000000L,
            0x0038203800000000L, 0x0070407000000000L, 0x00e080e000000000L, 0x00c000c000000000L,
            0x0302030000000000L, 0x0704070000000000L, 0x0e080e0000000000L, 0x1c101c0000000000L,
            0x3820380000000000L, 0x7040700000000000L, 0xe080e00000000000L, 0xc000c00000000000L,
            0x0203000000000000L, 0x0407000000000000L, 0x080e000000000000L, 0x101c000000000000L,
            0x2038000000000000L, 0x4070000000000000L, 0x80e0000000000000L, 0x00c0000000000000L
    };
    public static final long[]                      KNIGHT_ATTACKS  = new long[]{
            0x0000000000020400L, 0x0000000000050800L, 0x00000000000a1100L, 0x0000000000142200L,
            0x0000000000284400L, 0x0000000000508800L, 0x0000000000a01000L, 0x0000000000402000L,
            0x0000000002040004L, 0x0000000005080008L, 0x000000000a110011L, 0x0000000014220022L,
            0x0000000028440044L, 0x0000000050880088L, 0x00000000a0100010L, 0x0000000040200020L,
            0x0000000204000402L, 0x0000000508000805L, 0x0000000a1100110aL, 0x0000001422002214L,
            0x0000002844004428L, 0x0000005088008850L, 0x000000a0100010a0L, 0x0000004020002040L,
            0x0000020400040200L, 0x0000050800080500L, 0x00000a1100110a00L, 0x0000142200221400L,
            0x0000284400442800L, 0x0000508800885000L, 0x0000a0100010a000L, 0x0000402000204000L,
            0x0002040004020000L, 0x0005080008050000L, 0x000a1100110a0000L, 0x0014220022140000L,
            0x0028440044280000L, 0x0050880088500000L, 0x00a0100010a00000L, 0x0040200020400000L,
            0x0204000402000000L, 0x0508000805000000L, 0x0a1100110a000000L, 0x1422002214000000L,
            0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L,
            0x0400040200000000L, 0x0800080500000000L, 0x1100110a00000000L, 0x2200221400000000L,
            0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L,
            0x0004020000000000L, 0x0008050000000000L, 0x00110a0000000000L, 0x0022140000000000L,
            0x0044280000000000L, 0x0088500000000000L, 0x0010a00000000000L, 0x0020400000000000L
    };
    public static final long[][]                    ROOK_ATTACKS    = new long[64][];
    public static final long[][]                    BISHOP_ATTACKS  = new long[64][];

    public static final SlidingPieceBuffer[]        ROOK_BUFFER     = new SlidingPieceBuffer[64];
    public static final SlidingPieceBuffer[]        BISHOP_BUFFER   = new SlidingPieceBuffer[64];

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
    public static final long seed                                   = 11291283;

    static {
        long t = System.currentTimeMillis();
        generateData();
        System.out.println("Init took: " + (System.currentTimeMillis()-t) + " ms");
    }

    public static void generateData() {
        generateBuffers();
        if(VALIDATE_MAGICS && !validateMagics())
            System.out.println("[Error] The magic numbers can not be used.");
        generateZobristKeys();

        if(GENERATE_SLIDING_ATTACKS){
            generateAttackTables();
        }else{
            loadAttackTables(SLIDING_ATTACKS_SOURCE);
        }
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
            long relevant = bishopRelevantOccupancy[i];
            BISHOP_BUFFER[i] = new SlidingPieceBuffer(relevant, bishopMagics[i], bishopShifts[i]);
            relevant = rookRelevantOccupancy[i];
            ROOK_BUFFER[i] = new SlidingPieceBuffer(relevant, rookMagics[i], rookShifts[i]);
        }
    }

    public static boolean validateMagics() {
        boolean works = true;
        for(int i = 0; i < 64; i++){
            System.out.println(i);
            if(!isValidMagic(ROOK_BUFFER[i].getMask(), ROOK_BUFFER[i].getShift(),
                    ROOK_BUFFER[i].getMagic(),i,false)) {
                System.err.println("Error at: " + i);
                works = false;
            }
            if(!isValidMagic(BISHOP_BUFFER[i].getMask(), BISHOP_BUFFER[i].getShift(),
                    BISHOP_BUFFER[i].getMagic(),i,true)){
                System.err.println("Error at: " + i);
                works = false;
            }
        }
        return works;
    }

    public static void generateAttackTables() {
        for(int n = 0; n < 64; n++){
            ROOK_ATTACKS[n] = new long[(int)Math.pow(2, 64-ROOK_BUFFER[n].getShift())];
            for(int i = 0; i < (int)Math.pow(2, 64-ROOK_BUFFER[n].getShift()); i++){
                long rel_occ = BitBoard.populateMask(ROOK_BUFFER[n].getMask(), i);
                int index =(int)((rel_occ * ROOK_BUFFER[n].getMagic()) >>> (ROOK_BUFFER[n].getShift()));
                ROOK_ATTACKS[n][index] = BitBoard.generateRookAttack(n, rel_occ);
            }
            BISHOP_ATTACKS[n] = new long[(int)Math.pow(2, 64-BISHOP_BUFFER[n].getShift())];
            for(int i = 0; i < (int)Math.pow(2, 64-BISHOP_BUFFER[n].getShift()); i++){
                long rel_occ = BitBoard.populateMask(BISHOP_BUFFER[n].getMask(), i);
                int index =(int)((rel_occ * BISHOP_BUFFER[n].getMagic()) >>> (BISHOP_BUFFER[n].getShift()));
                BISHOP_ATTACKS[n][index] = BitBoard.generateBishopAttack(n, rel_occ);
            }
        }
    }

    public static void loadAttackTables(String file) {
        try{
            FileInputStream fis = new FileInputStream(file);
            byte[] in = new byte[8];

            ArrayList<Long> ar = new ArrayList<>();
            int squareIndex = 0;
            int counter = 0;

            while(fis.read(in) != -1){
                counter++;
                long l = ((long) in[7] << 56)
                        | ((long) in[6] & 0xff) << 48
                        | ((long) in[5] & 0xff) << 40
                        | ((long) in[4] & 0xff) << 32
                        | ((long) in[3] & 0xff) << 24
                        | ((long) in[2] & 0xff) << 16
                        | ((long) in[1] & 0xff) << 8
                        | ((long) in[0] & 0xff);

                //long l = ByteBuffer.allocate(8).putLong(in).array();

                if(l == NEW_LINE){
                    long[] re = new long[ar.size()];
                    for(int i = 0; i < ar.size(); i++) {
                        re[i] = ar.get(i);
                    }
                    if(squareIndex > 63){
                        BISHOP_ATTACKS[squareIndex-64] = re;
                    }else{
                        ROOK_ATTACKS[squareIndex] = re;
                    }
                    ar.clear();
                    squareIndex++;
                }else{
                    ar.add(l);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeAttackTables(String file){
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for(int i = 0; i < 64; i++){
                ByteBuffer bb = ByteBuffer.allocate(ROOK_ATTACKS[i].length * Long.BYTES);
                bb.asLongBuffer().put(ROOK_ATTACKS[i]);
                System.out.println(bb.capacity());
                fos.write(bb.array());
                bb = ByteBuffer.allocate(Long.BYTES);
                bb.asLongBuffer().put(NEW_LINE);
                fos.write(bb.array());
            }
            for(int i = 0; i < 64; i++){
                ByteBuffer bb = ByteBuffer.allocate(BISHOP_ATTACKS[i].length * Long.BYTES);
                bb.asLongBuffer().put(BISHOP_ATTACKS[i]);
                fos.write(bb.array());
                bb = ByteBuffer.allocate(Long.BYTES);
                bb.asLongBuffer().put(NEW_LINE);
                fos.write(bb.array());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * can be used to recalculate the tables above
     */
    public static void generateRelevantOccupancy() {
        System.out.println("bishops:");
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
            System.out.print(String.format("0x%016xL, ",relevant));
            if(i%4 == 3){
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("rooks:");

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

            System.out.print(String.format("0x%016xL, ",relevant));
            if(i%4 == 3){
                System.out.println();
            }
        }
    }

    /**
     * can be used to recalculate the tables above
     */
    public static void generateKingAndRookAttacks() {
        System.out.println("Knight attacks:");
        for (int n = 0; n < 64; n++) {
            long attack = 0L;
            for(int[] k:new int[][]{{1,2},{-1,2},{1,-2},{-1,-2},{2,1},{2,-1},{-2,1},{-2,-1}}){
                int f = fileIndex(n) + k[0];
                int r = rankIndex(n) + k[1];
                if(f >= 0 && f < 8 && r >= 0 && r < 8){
                    attack = setBit(attack, squareIndex(r,f));
                }
            }
            System.out.print(String.format("0x%016xL, ",attack));
            if(n%4 == 3){
                System.out.println();
            }
        }
        System.out.println();
        System.out.println("King attacks:");
        for (int n = 0; n < 64; n++) {
            long attack = 0L;
            for(int[] k:new int[][]{{1,1},{-1,1},{1,-1},{-1,-1},{1,0},{0,-1},{-0,1},{-0,-1}}){
                int f = fileIndex(n) + k[0];
                int r = rankIndex(n) + k[1];
                if(f >= 0 && f < 8 && r >= 0 && r < 8){
                    attack = setBit(attack, squareIndex(r,f));
                }
            }
            System.out.print(String.format("0x%016xL, ",attack));
            if(n%4 == 3){
                System.out.println();
            }
        }

    }

    /**
     * can be used to recalculate the tables above
     */
    public static void generateMagicRookNumber(int square, int attempts){
        int offset = bitCount(rookRelevantOccupancy[square]);
        long magic = generateMagic(rookRelevantOccupancy[square], square, offset, false, attempts);
        while (magic == 0) {
            offset++;
            magic = generateMagic(rookRelevantOccupancy[square], square, offset, false, attempts);
        }
        System.out.println("generated magic for rook: index=" + square + " offset=" + offset + " number=" + String.format("0x%016xL", magic));
    }

    /**
     * can be used to recalculate the tables above
     */
    public static void generateMagicBishopNumber(int square, int attempts){
        int offset = bitCount(bishopRelevantOccupancy[square]);
        long magic = generateMagic(bishopRelevantOccupancy[square], square, offset, true, attempts);
        while (magic == 0) {
            offset++;
            magic = generateMagic(bishopRelevantOccupancy[square], square, offset, true, attempts);
        }
        System.out.println("generated magic for bishop: index=" + square + " offset=" + offset + " number=" + String.format("0x%016xL", magic));
    }


    public static long lookUpRookAttack(int index, long occupied){
        SlidingPieceBuffer buff = ROOK_BUFFER[index];
        return ROOK_ATTACKS[index][(int)((occupied & buff.getMask()) *
                buff.getMagic() >>> (buff.getShift()))];
    }

    public static long lookUpBishopAttack(int index, long occupied){
        SlidingPieceBuffer buff = BISHOP_BUFFER[index];
        return BISHOP_ATTACKS[index][(int)((occupied & buff.getMask()) *
                buff.getMagic() >>> (buff.getShift()))];
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
                return m;
            }
        }
        m = BitBoard.setBit(m, index + direction);
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
     * @param shift
     * @param bishop
     * @param attempts  amount of attempts to find magic. if no magic has been found, 0L is returned.
     * @return
     */
    public static long generateMagic(long mask, int squareIndex, int shift, boolean bishop, long attempts) {
        Random r = new Random(seed);
        int count = 0;
        long magic = randomLongSparseBits(r);
        while(!BitBoard.isValidMagic(mask, shift, magic, squareIndex, bishop)){
            magic = randomLongSparseBits(r);
            count++;
            if(count > attempts){
                return 0L;
            }
        }
        return magic;
    }

    /**
     * checks if the given magic is valid.
     * it requires the relevant occupancy mask and the bit count in that mask.
     * @param relevant
     * @param shift
     * @param magic
     * @return
     */
    public static boolean isValidMagic(long relevant, int shift, long magic, int squareIndex, boolean bishop){
        HashMap<Integer, Long> map = new HashMap<>();
        //long[] values = new long[(int)Math.pow(2, offset)];
        for(int i = 0; i < (int)Math.pow(2, 64-shift); i++){
            long rel_occ = BitBoard.populateMask(relevant, i);
            int index =(int)((rel_occ * magic) >>> (shift));
            if(map.containsKey(index)) {
                if(map.get(index) ==
                        (bishop ? BitBoard.generateBishopAttack(squareIndex, rel_occ) :
                                BitBoard.generateRookAttack(squareIndex, rel_occ))){
                    continue;
                }else{
                    return false;
                }
            }
            else{
                map.put(index,(bishop ? BitBoard.generateBishopAttack(squareIndex, rel_occ) :
                        BitBoard.generateRookAttack(squareIndex, rel_occ)));
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
        //generateKingAndRookAttacks();


        //writeAttackTables(SLIDING_ATTACKS_SOURCE);

//        ByteBuffer bb = ByteBuffer.allocate(1 * Long.BYTES);
//        bb.asLongBuffer().put(1123L);
//        System.out.println(Arrays.toString(" ".getBytes()));

//        printBitmap(123912938129L);
//        printBitmap(lookUpBishopAttack(28, 123912938129L));
//        printBitmap(generateBishopAttack(28,0L));

    }


}
