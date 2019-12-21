package io;

import board.Board;
import board.SlowBoard;
import board.moves.Move;
import board.setup.Setup;

/**
 * the IOBoard class implements methods to generate boards encoded
 * boards. It can also decode them the other way around.
 *
 */
public class IOBoard {

    /**
     *
     * This method will generate a new board using a key
     * generated on lichess.com. It will not copy the following
     * things:
     *  - who has to move next
     *  - information about Castling
     *  - information about previous moves (no en passent)
     *
     * @param template      a template class to create a new board object
     * @param key           the key from lichess.com
     * @param <T>           a type argument for the board
     * @return              a new board of type T
     */
    public static <T extends Board<T>> T read_lichess(T template, String key){

        int x = 0;
        int y = 7;

        T board = template.newInstance();



        for(char c : key.toCharArray()){

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
        return board;
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

    public static void main(String[] args) {
        SlowBoard board = new SlowBoard(Setup.DEFAULT);
        System.out.println(algebraicNotation(board, board.getPseudoLegalMoves().get(10)));
    }
}
