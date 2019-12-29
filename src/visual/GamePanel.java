package visual;

import board.SlowBoard;
import board.moves.Move;
import game.Game;
import game.ai.search.AI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A Panel that contains 64 buttons.
 * It can be used to display chess positions and play against the AI.
 *
 */
public class GamePanel extends JPanel {



    public static Color color_white = new Color(0xEBECD0);
    public static Color color_black = new Color(0x779556);

    public static Color color_selected_white = new Color(0xF6F879);
    public static Color color_selected_black = new Color(0xB9CC36);

    public static Color color_lastMove_white = new Color(0xF6F879);
    public static Color color_lastMove_black = new Color(0xB9CC36);

    public static Color color_available_white = new Color(0xadb066);
    public static Color color_available_black = new Color(0xB09F54);

    public static Color color_takeable_white = new Color(0xC8342F);
    public static Color color_takeable_black = new Color(0xC8342F);

//    public static Color color_white = new Color(0xCCE5FF);
//    public static Color color_black = new Color(0x004C99);
//
//    public static Color color_selected_white = new Color(0xF6F879);
//    public static Color color_selected_black = new Color(0xF6F879);
//
//    public static Color color_lastMove_white = new Color(0xF6F879);
//    public static Color color_lastMove_black = new Color(0xF6F879);
//
//    public static Color color_available_white = new Color(0xffcc99);
//    public static Color color_available_black = new Color(0xffcc99);
//
//    public static Color color_takeable_white = new Color(0xff6666);
//    public static Color color_takeable_black = new Color(0xff6666);


    public static boolean renderAvailableCells = true;
    public static boolean renderTakeableCells = true;
    public static boolean renderLastMove = true;
    public static boolean renderSelectedCell = true;

    private GamePanelButton[][] buttons = new GamePanelButton[8][8];
    private Game g;

    private int selected = -1;
    private boolean flippedBoard = false;




    public GamePanel(Game g, Frame f) {
        super();

        this.g = g;
        this.g.addBoardChangedListener(new Runnable() {
            @Override
            public void run() {
                selected = -1;
                render();
            }
        });
        this.setLayout(new GridLayout(8, 8));

        for (int i = 7; i >= 0; i--) {
            for (int n = 0; n < 8; n++) {
                GamePanelButton b = new GamePanelButton();

                b.setLayout(null);
                b.setBorder(new LineBorder(Color.black, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);

                b.setFont(new Font(b.getFont().getName(), b.getFont().getStyle(), 50));
                b.addKeyListener(f);

                final int x = n;
                final int y = i;

                b.addActionListener(e -> click((byte) x, (byte) y));

                this.add(b);
                buttons[x][y] = b;
            }
        }
        render();
        if (!this.g.humansTurn()) {
            runMoveInThread(null);
        }
    }


    public boolean isFlippedBoard() {
        return flippedBoard;
    }

    public void setFlippedBoard(boolean flippedBoard) {
        this.flippedBoard = flippedBoard;
    }

    private GamePanelButton getButton(int x, int y){
        if(!flippedBoard){
            return buttons[x][y];
        }else{
            return buttons[7-x][7-y];
        }
    }
    
    private void renderIcon(int x, int y){

        PieceImages img = PieceImages.getImage(g.getBoard().getPiece(x,y));

        if(img == null){
            getButton(x,y).clearContent();
            return;
        }


        if(img.getImage() != null){
            getButton(x,y).setPieceImage(img.getImage());
        } else{
            getButton(x,y).setText(img.getString());
            getButton(x,y).setFont(new Font(
                    getButton(x,y).getFont().getName(),
                    getButton(x,y).getFont().getStyle(),
                    getButton(x,y).getWidth() * 2 / 3));
        }
    }

    public void renderBackground() {
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {
                getButton(i,n).setBackground((i % 2 == 1 && n % 2 == 0 || i % 2 == 0 && n % 2 == 1 ? color_white : color_black));
                renderIcon(i,n);
            }
        }
    }

    public void renderPreviousMove() {
        if(!g.getBoard().getMoveHistory().empty()){
            Move m = (Move) g.getBoard().getMoveHistory().peek();

            renderSquarePrevMove(g.getBoard().x(m.getFrom()), g.getBoard().y(m.getFrom()));
            renderSquarePrevMove(g.getBoard().x(m.getTo()), g.getBoard().y(m.getTo()));


        }
    }

    public void renderAttacks() {
        if (selected != -1) {

            for (Object obj : g.getBoard().getLegalMoves()) {
                if (((Move) obj).getFrom() == selected) {
                    if (g.getBoard().getPiece(((Move) obj).getTo()) != 0) {
                        renderSquareTakeable(g.getBoard().x(((Move) obj).getTo()), g.getBoard().y(((Move) obj).getTo()));
                    } else {
                        renderSquareAvailable(g.getBoard().x(((Move) obj).getTo()), g.getBoard().y(((Move) obj).getTo()));
                    }
                }
            }
            renderSquareSelected(g.getBoard().x(selected), g.getBoard().y(selected));
        }
    }

    private void renderSquarePrevMove(int i, int n){
        if(!renderLastMove) return;
        if(isWhiteSquare(i,n)){
            getButton(i,n).setBackground(color_lastMove_white);
        }else{
            getButton(i,n).setBackground(color_lastMove_black);
        }
    }

    private void renderSquareAvailable(int i, int n){
        if(!renderAvailableCells) return;
        if(isWhiteSquare(i,n)){
            getButton(i,n).setBackground(color_available_white);
        }else{
            getButton(i,n).setBackground(color_available_black);
        }
    }

    private void renderSquareTakeable(int i, int n){
        if(!renderTakeableCells) return;
        if(isWhiteSquare(i,n)){
            getButton(i,n).setBackground(color_takeable_white);
        }else{
            getButton(i,n).setBackground(color_takeable_black);
        }
    }

    private void renderSquareSelected(int i, int n){
        if(!renderSelectedCell) return;
        if(isWhiteSquare(i,n)){
            getButton(i,n).setBackground(color_selected_white);
        }else{
            getButton(i,n).setBackground(color_selected_black);
        }
    }

    public boolean isWhiteSquare(int i, int n) {
        return i % 2 == 1 && n % 2 == 0 || i % 2 == 0 && n % 2 == 1;
    }

    public void render() {
        renderBackground();
        renderPreviousMove();
        renderAttacks();
        this.repaint();
        this.revalidate();
    }


    /**
     * this method processes a click on a button.
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     */
    private void click(byte x, byte y) {

        if(flippedBoard){
            y = (byte)(7-y);
            x = (byte)(7-x);
        }

        if (selected == -1) {
            if (g.getBoard().getPiece(x, y) * g.getBoard().getActivePlayer() > 0) {
                selected = g.getBoard().index(x, y);
            }
        } else {
            if (x == g.getBoard().x(selected) && g.getBoard().y(selected) == y) {
                selected = -1;
            } else if (g.getBoard().getPiece(x, y) * g.getBoard().getActivePlayer() > 0) {
                selected = g.getBoard().index(x, y);
            } else {
                for (Object o : g.getBoard().getLegalMoves()) {
                    if (o instanceof Move) {
                        Move z = (Move) o;

                        if (selected == z.getFrom() && g.getBoard().index(x, y) == z.getTo()) {
                            runMoveInThread(z);
                        }
                    }
                }
            }
        }
        render();
    }

    /**
     * This method will process a move.
     * It will create a new Thread to not interrupt the rendering.
     * The Thread will call the move() method of the game.
     * Once a move has been processed on the board, this Panel will
     * render.
     * @param z     the move to process
     */
    public void runMoveInThread(Move z){
        new Thread(() -> g.move(z)).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(((SlowBoard)g.getBoard()).getBoard_meta_informtion());

        this.selected = -1;
        render();
    }

    /**
     * This method will undo the last 2 moves of the board.
     * This might cause problems when dealing with AIs!
     */
    public void undo() {
        if(g.getPlayerWhite() instanceof AI ||g.getPlayerBlack() instanceof AI){
            return;
        }
        this.selected = -1;
        this.g.getBoard().undoMove();

        this.render();
    }


}
