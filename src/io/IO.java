package io;

import board.Board;
import board.bitboards.BitBoard;
import board.moves.Move;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * the IO class implements methods to generate boards encoded
 * boards. It can also decode them the other way around.
 *
 */
public class IO {

    /**
     *
     * This method will generate a new board using a FEN string.
     *
     * @param template      a template class to create a new board object
     * @param key           the key from lichess.com
     * @param <T>           a type argument for the board
     * @return              a new board of type T
     */
    public static <T extends Board<T>> T read_FEN(T template, String key){

        key = key.trim();
        key = key.replace("  ", " ");

        int x = 0;
        int y = 7;

        T board = template.newInstance();

        String[] split = key.split(" ");

        //<editor-fold desc="parsing pieces">
        for(char c : split[0].toCharArray()){

            int num = getNumber(c);
            if(num > 0){
                x += num;
                continue;
            }else{
                int black = (c >= 'a') ? -1:1;

                switch (Character.toUpperCase(c)){
                    case 'P': board.setPiece(x,y, 1 * black); break;
                    case 'R': board.setPiece(x,y, 2 * black); break;
                    case 'N': board.setPiece(x,y, 3 * black); break;
                    case 'B': board.setPiece(x,y, 4 * black); break;
                    case 'Q': board.setPiece(x,y, 5 * black); break;
                    case 'K': board.setPiece(x,y, 6 * black); break;
                }

                x ++;
            }

            if (c == '/'){
                x = 0;
                y --;
                continue;
            }



        }
        //</editor-fold>

        //<editor-fold desc="parsing active player">
        if(split.length >= 2 && split[1].length() == 1){
            if(!split[1].equals("w")){
                board.changeActivePlayer();
            }
        }
        //</editor-fold>

        //<editor-fold desc="parsing castling">
        if(split.length >= 3){

            for(int i = 0; i<4; i++){
                board.setCastlingChance(i, false);
            }

            for(char c:split[2].toCharArray()){
                switch (c){
                    case 'K': board.setCastlingChance(1, true); break;
                    case 'Q': board.setCastlingChance(0, true); break;
                    case 'k': board.setCastlingChance(3, true); break;
                    case 'q': board.setCastlingChance(2, true); break;
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="parsing en passant">
        if(split.length >= 4){
            if(!split[3].equals("-")){
                int square = IO.getSquareIndex(split[3]);
                board.setEnPassantSquare(square);
            }
        }
        //</editor-fold>

        return board;
    }

    /**
     * generates the FEN string for a given board
     * @param b
     * @return
     */
    public static String write_FEN(Board b){

        StringBuilder builder = new StringBuilder();

        for(int n = 7; n >= 0; n--){
            int counting = 0;
            for(int i = 0; i < 8; i++){
                int piece = b.getPiece(i,n);
                if(piece == 0){
                    counting++;
                }else{
                    if (counting != 0){
                        builder.append(counting);
                    }
                    counting = 0;
                    builder.append(getPieceChar(piece));
                }
            }
            if(counting != 0){
                builder.append(counting);
            }
            if(n != 0) builder.append("/");
        }

        builder.append(" ");
        builder.append(b.getActivePlayer() > 0 ? "w":"b");
        builder.append(" ");
        boolean anyCastling = false;
        if(b.getCastlingChance(0)) {
            anyCastling = true;
            builder.append("Q");
        }
        if(b.getCastlingChance(1)) {
            anyCastling = true;
            builder.append("K");
        }
        if(b.getCastlingChance(2)) {
            anyCastling = true;
            builder.append("q");
        }
        if(b.getCastlingChance(3)) {
            anyCastling = true;
            builder.append("k");
        }
        if(anyCastling == false){
            builder.append("-");
        }


        if(b.getEnPassantSquare() >= 0){
            builder.append(" ");
            builder.append(IO.indexToFile(b.getEnPassantSquare()));
            builder.append(BitBoard.rankIndex(b.getEnPassantSquare()));
        }else{
            builder.append(" -");
        }


        return builder.toString();
    }

    /**
     * returns the char for any given piece
     * @param piece
     * @return
     */
    public static char getPieceChar(int piece){
        char c = ' ';

        switch (Math.abs(piece)){
            case 0: return ' ';
            case 1: c = 'p'; break;
            case 2: c = 'r'; break;
            case 3: c = 'n'; break;
            case 4: c = 'b'; break;
            case 5: c = 'q'; break;
            case 6: c = 'k'; break;
        }

        if (piece >= 0){
            return Character.toUpperCase(c);
        }else{
            return Character.toLowerCase(c);
        }
    }

    /**
     * returns the index for a given piece represented
     * by a char.
     * @param c
     * @return
     */
    public static int getPieceIndex(char c){
        int black = (c >= 'a') ? -1:1;

        switch (Character.toUpperCase(c)){
            case 'P': return 1 * black;
            case 'R': return 2 * black;
            case 'N': return 3 * black;
            case 'B': return 4 * black;
            case 'Q': return 5 * black;
            case 'K': return 6 * black;
        }
        return 0;
    }

    /**
     * if the char represents a digit, the digit will be returned.
     * If the char represents something else, -1 will be returned.
     * @param c     the digit represented by the char
     * @return
     */
    public static int getNumber(char c){
        try{
            if (c >= '0' && c <= '9'){
                return c - '0';
            }
        }catch (Exception e){

        }
        return -1;
    }

    /**
     * if the string represents a number, the number will be returned.
     * If the string represents something else, -1 will be returned.
     * @param c     the number represented by the string
     * @return
     */
    public static double getNumber(String c){
        try{
            return Double.parseDouble(c);
        }catch (Exception e){

        }
        return -1;
    }

    /**
     * returns the algebraic notation for a move on a given board.
     * The board needs to be given in order to solve ambiguity.
     *
     * The rules can be read under:
     *
     * @see <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Rules</a>
     * @param board
     * @param move
     * @return
     */
    public static String algebraicNotation(Board board, Move move){
        StringBuilder result = new StringBuilder();

        if (move == null) {
            return "";
        }

        if(Math.abs(move.getPieceFrom()) == 6){
            if(move.getTo() - move.getFrom() == 2){
                return "O-O";
            }
            if(move.getFrom() - move.getTo() == 2){
                return "O-O-O";
            }
        }

        String pieceFrom = new String[]{
                "",
                "",
                "R",
                "N",
                "B",
                "Q",
                "K"}[Math.abs(move.getPieceFrom())];


        String ambiSolve = "";
        for(Object m:board.getLegalMoves()){
            if(((Move)m).getPieceFrom() == move.getPieceFrom() &&
                move.getFrom() != ((Move)m).getFrom() &&
                move.getTo() == ((Move)m).getTo()){

                if(board.x(((Move)m).getFrom()) != board.x(move.getFrom())){
                    ambiSolve = ""+(char)('a'+board.x(move.getFrom()));
                }else if(board.y(((Move)m).getFrom()) != board.y(move.getFrom())){
                    ambiSolve = ""+(board.y(move.getFrom())+1);
                }else{
                    ambiSolve = ""+(char)('a'+board.x(move.getFrom())) +""+ (board.y(move.getFrom())+1);
                }
            }
        }

        String destinationLetter = ""+(char)('a'+board.x(move.getTo()));
        String destinationNumber = ""+(board.y(move.getTo())+1);

        String capture = move.getPieceTo() != 0 ? "x":"";

        result.append(pieceFrom);
        result.append(ambiSolve);
        result.append(capture);
        result.append(destinationLetter);
        result.append(destinationNumber);

        return result.toString();
    }

    /**
     * parses a double value to a string
     * @param s
     * @param fractionDigits
     * @return
     */
    public static String doubleToString(double s, int fractionDigits){
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(fractionDigits);
        String rounded = nf.format(s);
        return rounded;
    }

    /**
     * returns the index for a given file [a-h]
     * @param rank
     * @return
     */
    public static int fileToIndex(char rank) {
        return "abcdefgh".indexOf(rank);
    }

    /**
     * returns the file represented as a char
     * @param index
     * @return
     */
    public static char indexToFile(int index) {
        return "abcdefgh".charAt(index);
    }

    /**
     * returns the square represented as a string (e.g. c4)
      * @param index
     * @return
     */
    public static String getSquareString(int index){
        return "" + indexToFile(BitBoard.fileIndex(index)) + (BitBoard.rankIndex(index) + 1);
    }

    /**
     * returns the square index given by its string representation
     * @param st
     * @return
     */
    public static int getSquareIndex(String st){
        return BitBoard.squareIndex(Integer.parseInt(""+st.charAt(1))-1, fileToIndex(st.charAt(0)));
    }

    public static void main(String[] args) {
        //System.exit(parseInputForBestMove(args));
        System.out.println(doubleToString(4.13000000001, 5));
    }
}
