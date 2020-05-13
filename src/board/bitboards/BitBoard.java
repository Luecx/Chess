package board.bitboards;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class BitBoard {


    public static final long empty_set                              = 0x0000000000000000L;
    public static final long universal_set                          = 0xffffffffffffffffL;

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
    public static final long anti_diagonal_6                        = shiftSouth(anti_diagonal_7);
    public static final long anti_diagonal_5                        = shiftSouth(anti_diagonal_6);
    public static final long anti_diagonal_4                        = shiftSouth(anti_diagonal_5);
    public static final long anti_diagonal_3                        = shiftSouth(anti_diagonal_4);
    public static final long anti_diagonal_2                        = shiftSouth(anti_diagonal_3);
    public static final long anti_diagonal_1                        = shiftSouth(anti_diagonal_2);
    public static final long anti_diagonal_0                        = shiftSouth(anti_diagonal_1);

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

    public static final long center_squares                         = circle_d;
    public static final long center_squares_extended                = circle_c | circle_d;

    public static final long[] ranks = new long[]{rank_1, rank_2, rank_3, rank_4, rank_5, rank_6, rank_7, rank_8};
    public static final long[] files = new long[]{a_file, b_file, c_file, d_file, e_file, f_file, g_file, h_file};
    public static final long[] files_neighbour = new long[] {
            b_file,
            a_file | c_file,
            b_file | d_file,
            c_file | e_file,
            d_file | f_file,
            e_file | g_file,
            f_file | h_file,
            g_file};
    public static final long[] circles = new long[]{circle_a, circle_b, circle_c, circle_d};
    public static final long[] diagonals = new long[]{
            diagonal_0, diagonal_1, diagonal_2, diagonal_3, diagonal_4,
            diagonal_5, diagonal_6, diagonal_7, diagonal_8, diagonal_9,
            diagonal_10, diagonal_11, diagonal_12, diagonal_13, diagonal_14};
    public static final long[] antiDiagonals = new long[]{
            anti_diagonal_0, anti_diagonal_1, anti_diagonal_2, anti_diagonal_3, anti_diagonal_4,
            anti_diagonal_5, anti_diagonal_6, anti_diagonal_7, anti_diagonal_8, anti_diagonal_9,
            anti_diagonal_10, anti_diagonal_11, anti_diagonal_12, anti_diagonal_13, anti_diagonal_14};


    public static final long castling_white_queenside_mask          = 0x000000000000000EL;
    public static final long castling_white_kingside_mask           = 0x0000000000000060L;
    public static final long castling_black_queenside_mask          = castling_white_queenside_mask << (7 * 8);
    public static final long castling_black_kingside_mask           = castling_white_kingside_mask  << (7 * 8);

    //describes the fields that must not be attacked
    public static final long castling_white_queenside_safe          = 0x000000000000001CL;
    public static final long castling_white_kingside_safe           = castling_white_queenside_safe << 2;
    public static final long castling_black_queenside_safe          = castling_white_queenside_safe << (7 * 8);
    public static final long castling_black_kingside_safe           = castling_white_kingside_safe  << (7 * 8);


    public static final boolean VALIDATE_MAGICS                     = false;
    public static final boolean GENERATE_SLIDING_ATTACKS            = false;
    public static final String  SLIDING_ATTACKS_SOURCE              = "board/bitboards/slidingAttacks.txt";
    public static final long    NEW_LINE                            = 0xFFFFFFFFFFFFFFFFL;


    public static final long[][] all_hashes                         = new long[12][64];
    public static final long[][] white_hashes                       = new long[6][64];
    public static final long[][] black_hashes                       = new long[6][64];


    public static final long[] whitePassedPawnMask = new long[] {
            0x0303030303030300L, 0x0707070707070700L, 0x0e0e0e0e0e0e0e00L, 0x1c1c1c1c1c1c1c00L,
            0x3838383838383800L, 0x7070707070707000L, 0xe0e0e0e0e0e0e000L, 0xc0c0c0c0c0c0c000L,
            0x0303030303030000L, 0x0707070707070000L, 0x0e0e0e0e0e0e0000L, 0x1c1c1c1c1c1c0000L,
            0x3838383838380000L, 0x7070707070700000L, 0xe0e0e0e0e0e00000L, 0xc0c0c0c0c0c00000L,
            0x0303030303000000L, 0x0707070707000000L, 0x0e0e0e0e0e000000L, 0x1c1c1c1c1c000000L,
            0x3838383838000000L, 0x7070707070000000L, 0xe0e0e0e0e0000000L, 0xc0c0c0c0c0000000L,
            0x0303030300000000L, 0x0707070700000000L, 0x0e0e0e0e00000000L, 0x1c1c1c1c00000000L,
            0x3838383800000000L, 0x7070707000000000L, 0xe0e0e0e000000000L, 0xc0c0c0c000000000L,
            0x0303030000000000L, 0x0707070000000000L, 0x0e0e0e0000000000L, 0x1c1c1c0000000000L,
            0x3838380000000000L, 0x7070700000000000L, 0xe0e0e00000000000L, 0xc0c0c00000000000L,
            0x0303000000000000L, 0x0707000000000000L, 0x0e0e000000000000L, 0x1c1c000000000000L,
            0x3838000000000000L, 0x7070000000000000L, 0xe0e0000000000000L, 0xc0c0000000000000L,
            0x0300000000000000L, 0x0700000000000000L, 0x0e00000000000000L, 0x1c00000000000000L,
            0x3800000000000000L, 0x7000000000000000L, 0xe000000000000000L, 0xc000000000000000L,
            0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L,
            0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L
    };

    public static final long[] blackPassedPawnMask = new long[] {

            0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L,
            0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L,
            0x0000000000000003L, 0x0000000000000007L, 0x000000000000000eL, 0x000000000000001cL,
            0x0000000000000038L, 0x0000000000000070L, 0x00000000000000e0L, 0x00000000000000c0L,
            0x0000000000000303L, 0x0000000000000707L, 0x0000000000000e0eL, 0x0000000000001c1cL,
            0x0000000000003838L, 0x0000000000007070L, 0x000000000000e0e0L, 0x000000000000c0c0L,
            0x0000000000030303L, 0x0000000000070707L, 0x00000000000e0e0eL, 0x00000000001c1c1cL,
            0x0000000000383838L, 0x0000000000707070L, 0x0000000000e0e0e0L, 0x0000000000c0c0c0L,
            0x0000000003030303L, 0x0000000007070707L, 0x000000000e0e0e0eL, 0x000000001c1c1c1cL,
            0x0000000038383838L, 0x0000000070707070L, 0x00000000e0e0e0e0L, 0x00000000c0c0c0c0L,
            0x0000000303030303L, 0x0000000707070707L, 0x0000000e0e0e0e0eL, 0x0000001c1c1c1c1cL,
            0x0000003838383838L, 0x0000007070707070L, 0x000000e0e0e0e0e0L, 0x000000c0c0c0c0c0L,
            0x0000030303030303L, 0x0000070707070707L, 0x00000e0e0e0e0e0eL, 0x00001c1c1c1c1c1cL,
            0x0000383838383838L, 0x0000707070707070L, 0x0000e0e0e0e0e0e0L, 0x0000c0c0c0c0c0c0L,
            0x0003030303030303L, 0x0007070707070707L, 0x000e0e0e0e0e0e0eL, 0x001c1c1c1c1c1c1cL,
            0x0038383838383838L, 0x0070707070707070L, 0x00e0e0e0e0e0e0e0L, 0x00c0c0c0c0c0c0c0L,
    };

    public static final long[] bishopMasks = new long[] {
            0x0040201008040200L, 0x0000402010080400L, 0x0000004020100a00L, 0x0000000040221400L,
            0x0000000002442800L, 0x0000000204085000L, 0x0000020408102000L, 0x0002040810204000L,
            0x0020100804020000L, 0x0040201008040000L, 0x00004020100a0000L, 0x0000004022140000L,
            0x0000000244280000L, 0x0000020408500000L, 0x0002040810200000L, 0x0004081020400000L,
            0x0010080402000200L, 0x0020100804000400L, 0x004020100a000a00L, 0x0000402214001400L,
            0x0000024428002800L, 0x0002040850005000L, 0x0004081020002000L, 0x0008102040004000L,
            0x0008040200020400L, 0x0010080400040800L, 0x0020100a000a1000L, 0x0040221400142200L,
            0x0002442800284400L, 0x0004085000500800L, 0x0008102000201000L, 0x0010204000402000L,
            0x0004020002040800L, 0x0008040004081000L, 0x00100a000a102000L, 0x0022140014224000L,
            0x0044280028440200L, 0x0008500050080400L, 0x0010200020100800L, 0x0020400040201000L,
            0x0002000204081000L, 0x0004000408102000L, 0x000a000a10204000L, 0x0014001422400000L,
            0x0028002844020000L, 0x0050005008040200L, 0x0020002010080400L, 0x0040004020100800L,
            0x0000020408102000L, 0x0000040810204000L, 0x00000a1020400000L, 0x0000142240000000L,
            0x0000284402000000L, 0x0000500804020000L, 0x0000201008040200L, 0x0000402010080400L,
            0x0002040810204000L, 0x0004081020400000L, 0x000a102040000000L, 0x0014224000000000L,
            0x0028440200000000L, 0x0050080402000000L, 0x0020100804020000L, 0x0040201008040200L
    };

    public static final long[] rookMasks = new long[] {
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
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            53, 54, 54, 54, 54, 54, 54, 53,
            52, 53, 53, 53, 53, 53, 53, 52
    };

    public static final long[] bishopMagics = new long[]{
            0x0002020202020200L, 0x0002020202020000L, 0x0004010202000000L, 0x0004040080000000L,
            0x0001104000000000L, 0x0000821040000000L, 0x0000410410400000L, 0x0000104104104000L,
            0x0000040404040400L, 0x0000020202020200L, 0x0000040102020000L, 0x0000040400800000L,
            0x0000011040000000L, 0x0000008210400000L, 0x0000004104104000L, 0x0000002082082000L,
            0x0004000808080800L, 0x0002000404040400L, 0x0001000202020200L, 0x0000800802004000L,
            0x0000800400A00000L, 0x0000200100884000L, 0x0000400082082000L, 0x0000200041041000L,
            0x0002080010101000L, 0x0001040008080800L, 0x0000208004010400L, 0x0000404004010200L,
            0x0000840000802000L, 0x0000404002011000L, 0x0000808001041000L, 0x0000404000820800L,
            0x0001041000202000L, 0x0000820800101000L, 0x0000104400080800L, 0x0000020080080080L,
            0x0000404040040100L, 0x0000808100020100L, 0x0001010100020800L, 0x0000808080010400L,
            0x0000820820004000L, 0x0000410410002000L, 0x0000082088001000L, 0x0000002011000800L,
            0x0000080100400400L, 0x0001010101000200L, 0x0002020202000400L, 0x0001010101000200L,
            0x0000410410400000L, 0x0000208208200000L, 0x0000002084100000L, 0x0000000020880000L,
            0x0000001002020000L, 0x0000040408020000L, 0x0004040404040000L, 0x0002020202020000L,
            0x0000104104104000L, 0x0000002082082000L, 0x0000000020841000L, 0x0000000000208800L,
            0x0000000010020200L, 0x0000000404080200L, 0x0000040404040400L, 0x0002020202020200L

    };
    public static final long[] rookMagics = new long[]{
            0x0080001020400080L, 0x0040001000200040L, 0x0080081000200080L, 0x0080040800100080L,
            0x0080020400080080L, 0x0080010200040080L, 0x0080008001000200L, 0x0080002040800100L,
            0x0000800020400080L, 0x0000400020005000L, 0x0000801000200080L, 0x0000800800100080L,
            0x0000800400080080L, 0x0000800200040080L, 0x0000800100020080L, 0x0000800040800100L,
            0x0000208000400080L, 0x0000404000201000L, 0x0000808010002000L, 0x0000808008001000L,
            0x0000808004000800L, 0x0000808002000400L, 0x0000010100020004L, 0x0000020000408104L,
            0x0000208080004000L, 0x0000200040005000L, 0x0000100080200080L, 0x0000080080100080L,
            0x0000040080080080L, 0x0000020080040080L, 0x0000010080800200L, 0x0000800080004100L,
            0x0000204000800080L, 0x0000200040401000L, 0x0000100080802000L, 0x0000080080801000L,
            0x0000040080800800L, 0x0000020080800400L, 0x0000020001010004L, 0x0000800040800100L,
            0x0000204000808000L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L,
            0x0000040008008080L, 0x0000020004008080L, 0x0000010002008080L, 0x0000004081020004L,
            0x0000204000800080L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L,
            0x0000040008008080L, 0x0000020004008080L, 0x0000800100020080L, 0x0000800041000080L,
            0x00FFFCDDFCED714AL, 0x007FFCDDFCED714AL, 0x003FFFCDFFD88096L, 0x0000040810002101L,
            0x0001000204080011L, 0x0001000204000801L, 0x0001000082000401L, 0x0001FFFAABFAD1A2L
    };


    public static final long[]                      KING_ATTACKS    = new long[]{
            0x0000000000000302L, 0x0000000000000705L, 0x0000000000000e0aL, 0x0000000000001c14L,
            0x0000000000003828L, 0x0000000000007050L, 0x000000000000e0a0L, 0x000000000000c040L,
            0x0000000000030203L, 0x0000000000070507L, 0x00000000000e0a0eL, 0x00000000001c141cL,
            0x0000000000382838L, 0x0000000000705070L, 0x0000000000e0a0e0L, 0x0000000000c040c0L,
            0x0000000003020300L, 0x0000000007050700L, 0x000000000e0a0e00L, 0x000000001c141c00L,
            0x0000000038283800L, 0x0000000070507000L, 0x00000000e0a0e000L, 0x00000000c040c000L,
            0x0000000302030000L, 0x0000000705070000L, 0x0000000e0a0e0000L, 0x0000001c141c0000L,
            0x0000003828380000L, 0x0000007050700000L, 0x000000e0a0e00000L, 0x000000c040c00000L,
            0x0000030203000000L, 0x0000070507000000L, 0x00000e0a0e000000L, 0x00001c141c000000L,
            0x0000382838000000L, 0x0000705070000000L, 0x0000e0a0e0000000L, 0x0000c040c0000000L,
            0x0003020300000000L, 0x0007050700000000L, 0x000e0a0e00000000L, 0x001c141c00000000L,
            0x0038283800000000L, 0x0070507000000000L, 0x00e0a0e000000000L, 0x00c040c000000000L,
            0x0302030000000000L, 0x0705070000000000L, 0x0e0a0e0000000000L, 0x1c141c0000000000L,
            0x3828380000000000L, 0x7050700000000000L, 0xe0a0e00000000000L, 0xc040c00000000000L,
            0x0203000000000000L, 0x0507000000000000L, 0x0a0e000000000000L, 0x141c000000000000L,
            0x2838000000000000L, 0x5070000000000000L, 0xa0e0000000000000L, 0x40c0000000000000L
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
        System.out.print("generating attack tables...");
        long t = System.currentTimeMillis();
        generateData();
        System.out.println("       done! ["+String.format("%7s",(System.currentTimeMillis()-t)+ " ms") + "]");
    }

    public static void generateData() {
        generateBuffers();
        if(VALIDATE_MAGICS && !validateMagics())
            System.out.println("[Error] The magic numbers can not be used.");
        generateZobristKeys();

        if(GENERATE_SLIDING_ATTACKS){
            generateAttackTables();
        }else{
            loadAttackTables();
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
            long relevant = bishopMasks[i];
            BISHOP_BUFFER[i] = new SlidingPieceBuffer(relevant, bishopMagics[i], bishopShifts[i]);
            relevant = rookMasks[i];
            ROOK_BUFFER[i] = new SlidingPieceBuffer(relevant, rookMagics[i], rookShifts[i]);
        }
    }

    public static boolean validateMagics() {
        boolean works = true;
        for(int i = 0; i < 64; i++){
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

    public static void loadAttackTables() {
        try{

            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Path temp = Files.createTempFile("resource-", ".ext");

            //System.out.println(classLoader+ "  " + temp + "  "  + classLoader.getResourceAsStream(SLIDING_ATTACKS_SOURCE));

            Files.copy(classLoader.getResourceAsStream(SLIDING_ATTACKS_SOURCE), temp, StandardCopyOption.REPLACE_EXISTING);
            FileInputStream fis = new FileInputStream(temp.toFile());

            //System.out.println(fis);

            //FileInputStream ins = BitBoard.class.getResourceAsStream("images/search_folder.png");
            byte[] in = new byte[8];

            ArrayList<Long> ar = new ArrayList<>();
            int squareIndex = 0;
            int counter = 0;

            while(fis.read(in) != -1){
                counter++;
                long l = ((long) in[0] << 56)
                        | ((long) in[1] & 0xff) << 48
                        | ((long) in[2] & 0xff) << 40
                        | ((long) in[3] & 0xff) << 32
                        | ((long) in[4] & 0xff) << 24
                        | ((long) in[5] & 0xff) << 16
                        | ((long) in[6] & 0xff) << 8
                        | ((long) in[7] & 0xff);

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
            for(int[] k:new int[][]{{1,1},{-1,1},{1,-1},{-1,-1},{1,0},{0,1},{-1,0},{0,-1}}){
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
    public static void generatePassedPawnMask() {
        System.out.println("white pawns:");
        for(int i = 0; i < 64; i++){
            long mask = 0;
            for(int n = rankIndex(i)+1; n < 8; n++){
                mask = BitBoard.setBit(mask, squareIndex(n, fileIndex(i)));
                if(fileIndex(i) > 0)
                    mask = BitBoard.setBit(mask, squareIndex(n, fileIndex(i)-1));
                if(fileIndex(i) < 7)
                    mask = BitBoard.setBit(mask, squareIndex(n, fileIndex(i)+1));
            }
            System.out.print(String.format("0x%016xL, ",mask));
            if(i%4 == 3){
                System.out.println();
            }
        }

        System.out.println("black pawns:");

        for(int i = 0; i < 64; i++){
            long mask = 0;
            for(int n = rankIndex(i)-1; n >= 0; n--){
                mask = BitBoard.setBit(mask, squareIndex(n, fileIndex(i)));
                if(fileIndex(i) > 0)
                    mask = BitBoard.setBit(mask, squareIndex(n, fileIndex(i)-1));
                if(fileIndex(i) < 7)
                    mask = BitBoard.setBit(mask, squareIndex(n, fileIndex(i)+1));
            }
            System.out.print(String.format("0x%016xL, ",mask));
            if(i%4 == 3){
                System.out.println();
            }
        }
    }

    /**
     * can be used to recalculate the tables above
     */
    public static void generateMagicRookNumber(int square, int attempts){
        int offset = bitCount(rookMasks[square]);
        long magic = generateMagic(rookMasks[square], square, offset, false, attempts);
        while (magic == 0) {
            offset++;
            magic = generateMagic(rookMasks[square], square, offset, false, attempts);
        }
        System.out.println("generated magic for rook: index=" + square + " shift=" + (64-offset) + " number=" + String.format("0x%016xL", magic));
    }

    /**
     * can be used to recalculate the tables above
     */
    public static void generateMagicBishopNumber(int square, int attempts){
        int offset = bitCount(bishopMasks[square]);
        long magic = generateMagic(bishopMasks[square], square, offset, true, attempts);
        while (magic == 0) {
            offset++;
            magic = generateMagic(bishopMasks[square], square, offset, true, attempts);
        }
        System.out.println("generated magic for bishop: index=" + square + " offset=" + offset + " number=" + String.format("0x%016xL", magic));
    }


    public static long lookUpRookAttack(int index, long occupied){
        SlidingPieceBuffer buff = ROOK_BUFFER[index];
        return ROOK_ATTACKS[index][(int)((occupied & buff.getMask()) *
                buff.getMagic() >>> (buff.getShift()))];

        //return generateRookAttack(index, occupied);
    }

    public static long lookUpBishopAttack(int index, long occupied){
        SlidingPieceBuffer buff = BISHOP_BUFFER[index];
        return BISHOP_ATTACKS[index][(int)((occupied & buff.getMask()) *
                buff.getMagic() >>> (buff.getShift()))];

        //return generateBishopAttack(index, occupied);
    }

    
    public static long generateAttack(int index, int dx, int dy, long occupied){

        long m = 0L;

        int c = index + dy * 8 + dx;
        int x = fileIndex(index);
        int y = rankIndex(index);

        x += dx;
        y += dy;
          while (index >= 0 && index < 64 && x >= 0 && x < 8 && y >= 0 && y < 8) {
            if (getBit(occupied, c)) {
                m = setBit(m, c);
                return m;
            }
            m = setBit(m, c);
            c += dy * 8 + dx;
            x += dx;
            y += dy;
        }


        return m;
    }

    public static long generateRookAttack(int index, long occupied){
        return generateAttack(index, 1,0, occupied) |
                generateAttack(index, -1,0, occupied) |
                generateAttack(index, 0,1, occupied) |
                generateAttack(index, 0,-1, occupied);
    }

    public static long generateBishopAttack(int index, long occupied){
        return generateAttack(index, 1,1, occupied) |
                generateAttack(index, 1,-1, occupied) |
                generateAttack(index, -1,1, occupied) |
                generateAttack(index, -1,-1, occupied);
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
    public static final int bitscanForward  (long bb){
        return Long.numberOfTrailingZeros(bb);
//        assert bb != 0;
//        return index64[(int)(((bb ^ (bb-1L)) * debruijn64) >>> 58)];
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
        return Long.bitCount(bb);
//        int counter = 0;
//        while(bb != 0){
//            bb = lsbReset(bb);
//            counter ++;
//        }
//        return counter;
    }

    /**
     * The Chebyshev distance is the maximum of the absolute rank- and file-distance of both squares.
     * @param f1
     * @param r1
     * @param f2
     * @param r2
     * @return
     */
    public static final int chebyshevDistance(int f1,int r1, int f2, int r2){
        return Math.max(Math.abs(r2-r1), Math.abs(f2-f1));
    }

    /**
     * The Chebyshev distance is the maximum of the absolute rank- and file-distance of both squares.
     * @param sq1
     * @param sq2
     * @return
     */
    public static final int chebyshevDistance(int sq1,int sq2){
        return chebyshevDistance(fileIndex(sq1), rankIndex(sq1), fileIndex(sq2), rankIndex(sq2));
    }

    /**
     * the orthogonal Manhattan-Distance is the sum of both absolute rank- and file-distance distances
     * @param f1
     * @param r1
     * @param f2
     * @param r2
     * @return
     */
    public static final int manhattanDistance(int f1,int r1, int f2, int r2){
        return Math.max(Math.abs(r2-r1), Math.abs(f2-f1));
    }

    /**
     * the orthogonal Manhattan-Distance is the sum of both absolute rank- and file-distance distances
     * @param sq1
     * @param sq2
     * @return
     */
    public static final int manhattanDistance(int sq1,int sq2){
        return manhattanDistance(fileIndex(sq1), rankIndex(sq1), fileIndex(sq2), rankIndex(sq2));
    }

    public static void main(String[] args) {


        long time = System.currentTimeMillis();

        long b = 1239013111231928319L;

        BitBoard.printBitmap(b);

        for(int i = 0; i < 1E9; i++){
            BitBoard.bitCount(b+1);
        }


        System.out.println(System.currentTimeMillis() - time);

        //generatePassedPawnMask();

        //System.out.println("written");

//        writeAttackTables(SLIDING_ATTACKS_SOURCE);



//        BitBoard.printBitmap(castling_white_queenside_safe);
//        BitBoard.printBitmap(castling_white_kingside_safe);


    }


}
