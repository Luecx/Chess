package io;

import board.Board;

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
}
