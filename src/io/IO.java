package io;

import board.Board;
import board.SlowBoard;
import board.moves.Move;
import board.setup.Setup;
import game.Game;
import game.ai.evaluator.FinnEvaluator;
import game.ai.ordering.SystematicOrderer;
import game.ai.reducing.SimpleReducer;
import game.ai.search.AI;
import game.ai.search.PVSearch;
import visual.GamePanel;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
                int rankIndex = split[3].toLowerCase().charAt(0)-'a';
                board.setEnPassantChance(rankIndex, true);
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

        for(int i = 0; i < 8; i++){
            if(b.getEnPassantChance(i)){
                char file = (char) ('a'+i);
                char rank = (char) ('0'+(b.getActivePlayer() > 0 ? 6:3));
                builder.append(" ");
                builder.append(file);
                builder.append(rank);
                break;
            }
            if(i == 7){
                builder.append(" -");
            }
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

    public static HashMap<String, Object> parseCommands(String[] commands){
        String currentCommand = "";
        HashMap<String, List<String>> temp = new HashMap<>();
        for(String s:commands){
            if(s.length() == 0) continue;
            if(s.startsWith("-")){
                s = s.toLowerCase();
                if(s.length() == 1) temp.get(currentCommand).add("-");
                else{
                    currentCommand = s.substring(1);
                    temp.put(currentCommand, new LinkedList<>());
                }
            }else{
                temp.get(currentCommand).add(s);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        for(String key:temp.keySet()){
            List<String> s = temp.get(key);
            if(s.size() == 0){
                map.put(key, true);
            }
            else if(s.size() == 1){
                switch (s.get(0)){
                    case "true": map.put(key, true);break;
                    case "false": map.put(key, false);break;
                    default:{
                        double val = getNumber(s.get(0));
                        if(val != -1){
                            map.put(key, val);
                        }else{
                            map.put(key, s.get(0));
                        }
                    }
                }
            }else{
                StringBuilder full = new StringBuilder();
                for(int i = 0; i < s.size(); i++){
                    full.append(s.get(i));
                    if(i != s.size()-1){
                        full.append(" ");
                    }
                }
                map.put(key, full.toString());
            }
        }

        return map;

    }

    /**
     * creates an AI using the following commands.
     * it uses the SystematicOrderer, SimpleReducer and FinnEvaluator.
     * available commands:
     *
     *
     *      limit           [Integer]       the search limit (either in ms, or depth)
     *      mode            (time, depth)   telling the method to use a time limit or depth limit
     *      qdepth          [Integer]       the quiscence search depth
     *
     *      reducing        [3x Integer]    1. reduction value
     *                                      2. minimum depth at which reduction can occur
     *                                      3. minimum amount of moves after which reduction can occur
     *
     *      transposition   [Boolean]       enables/disables transposition tables
     *      nulls           [Boolean]       enables/disables null moves
     *      lmr             [Boolean]       enables/disables late move reduction
     *      debug           [Boolean]       enables/disables output printing
     *      iteration       [Boolean]       enables/disables iterative deepening
     *      killers         [Boolean]       enables/disables killer heuristic
     *
     * @param map
     * @return
     */
    public static AI parseAI(HashMap<String, Object> map){

        int limit = 3000;
        int qDepth = 4;
        int mode = PVSearch.FLAG_TIME_LIMIT;

        int red_reduction = 1;
        int red_minDepth = 3;
        int red_minMoves = 10;

        if(map.containsKey("limit")){
            limit = (int) ((double) map.get("limit"));
        }if(map.containsKey("qdepth")){
            qDepth = (int) ((double) map.get("qdepth"));
        }if(map.containsKey("mode")){
            if(map.get("mode").equals("time")){
                mode = PVSearch.FLAG_TIME_LIMIT;
            }else{
                mode = PVSearch.FLAG_DEPTH_LIMIT;
            }
        }if(map.containsKey("reducing")){
            String[] split = ((String)map.get("reducing")).split(" ");
            red_reduction = Integer.parseInt(split[0]);
            red_minDepth = Integer.parseInt(split[1]);
            red_minMoves = Integer.parseInt(split[2]);
        }

        PVSearch pvSearch = new PVSearch(
                new FinnEvaluator(),
                new SystematicOrderer(),
                new SimpleReducer(red_reduction, red_minDepth, red_minMoves),
                mode,limit,qDepth);

        if(map.containsKey("transposition")){
            pvSearch.setUse_transposition((Boolean)map.get("transposition"));
        }if(map.containsKey("nulls")){
            pvSearch.setUse_null_moves((Boolean)map.get("nulls"));
        }if(map.containsKey("iteration")){
            pvSearch.setUse_iteration((Boolean)map.get("iteration"));
        }if(map.containsKey("lmr")){
            pvSearch.setUse_LMR((Boolean)map.get("lmr"));
        }if(map.containsKey("debug")){
            pvSearch.setPrint_overview((Boolean)map.get("debug"));
        }if(map.containsKey("killers")){
            pvSearch.setUse_killer_heuristic((Boolean)map.get("killers"));
        }

        return pvSearch;
    }

    /**
     * generates a board object using the following commands. it uses
     * the SlowBoard implementation.
     *
     *      fen         [String]            fen-key for the board
     *
     * @param map
     * @return
     */
    public static Board parseBoard(HashMap<String, Object> map){
        if(map.containsKey("fen")){
            return IO.read_FEN(new SlowBoard(), (String) map.get("fen"));
        }else{
            return new SlowBoard(Setup.DEFAULT);
        }
    }

    public static String doubleToString(double s, int fractionDigits){
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(fractionDigits);
        String rounded = nf.format(s);
        return rounded;
    }

    /**
     * reads the input keys and parses them according to
     * @link parseBoard
     * @link parseAI
     *
     * It returns the best move as an integer according to the following formula:
     *      = (x_from + 8 * y_from) * 64 + (y_to + 8 * y_to)
     *
     * assuming v is the return value, x_from, y_from, x_to and y_to can be extracted
     * the following way:
     *
     *      t = mod(v,64)
     *      f = floor(v/64)
     *
     *      x_from = mod(f,8)
     *      y_from = floor(f/8)
     *      x_to = mod(t,8)
     *      y_to = floor(t/8)
     *
     * @param keys
     * @return
     */
    public static int parseInputForBestMove(String[] keys){
        HashMap<String, Object> map = parseCommands(keys);

        AI ai = parseAI(map);
        Board board = IO.parseBoard(map);

        System.out.println(board);

        Move m = ai.bestMove(board);

        int x_from = board.x(m.getFrom());
        int y_from = board.y(m.getFrom());
        int x_to = board.x(m.getTo());
        int y_to = board.y(m.getTo());

        int indexFrom = x_from + 8 * y_from;
        int indexTo = x_to + 8 * y_to;

        return indexFrom * 64 + indexTo;
    }

    /**
     * generates a game object using the following commands:
     *
     *
     *
     * @param commands
     * @return
     */
    public static Game parseGame(String[] commands){
        HashMap<String, Object> map = parseCommands(commands);

        Board b = new SlowBoard(Setup.DEFAULT);
        if(map.containsKey("fen")){
            String fen = (String) map.get("fen");
            b = IO.read_FEN(b, fen);
        }

        int w_qDepth;
        int b_qDepth;

        int w_limit;
        int b_limit;

        int w_mode;
        int b_mode;

        //AI ai1 = new PVSearch();

        return null;
    }

    //give the rank as a letter, get it back as an int
    private static int rankToIndex(char rank) {
        return "abcdefgh".indexOf(rank);
    }
    private static char indexToRank(int index) {
        return "abcdefgh".charAt(index-1);
    }

    /**
     * take a move in UCI notation (e2e4) and
     * transforms it to move object
     * @param input the UCI notation
     * @param board the board state
     * @return the move object
     */
    public static Move uciToMove(String input, Board board) {
        int fromx;
        int fromy;
        int tox;
        int toy;
        int from;
        int to;
        fromx = rankToIndex(input.charAt(0));
        tox = input.charAt(2);
        fromy = Character.getNumericValue(input.charAt(1)) - 1;
        toy = Character.getNumericValue(input.charAt(3)) - 1;
        from = board.index(fromx,fromy);
        to = board.index(tox,toy);

        return new Move(from,to,board);
    }

    //Move object -> e2e4
    public static String moveToUCI(Move move, Board board) {
        String toReturn = "";
        int tox = board.x(move.getTo());
        int toy = board.y(move.getTo());
        int fromx = board.x(move.getFrom());
        int fromy = board.y(move.getFrom());

        toReturn = toReturn + indexToRank(fromx);
        toReturn = toReturn + Integer.toString(fromy+1);
        toReturn = toReturn + indexToRank(tox);
        toReturn = toReturn + Integer.toString(toy+1);

        return toReturn;
    }


    public static void main(String[] args) {
        //System.exit(parseInputForBestMove(args));
        System.out.println(doubleToString(4.13000000001, 5));
    }
}
