package visual;

import board.Move;
import game.Game;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by Luecx on 08.08.2017.
 */
public class GamePanel extends JPanel {

    private JButton[][] buttons = new JButton[8][8];

    private Game g;

    public Color color_black = Color.gray;
    public Color color_white = Color.white;
    public Color color_selected = Color.green;
    public Color color_lastMove = Color.yellow;
    public Color color_available = Color.pink;
    public Color color_takeable = Color.red;

    private static ImageIcon[][] icons = new ImageIcon[6][2];

    static {
        for (int i = 0; i < 6; i++) {
            for (int n = 0; n < 2; n++) {
                int a = i;
                if (a == 1 || a == 2) {
                    a++;
                } else if (a == 3) {
                    a = 1;
                }
                icons[a][n] = new ImageIcon("resources/" + (i + 1) + "" + (n + 1) + ".gif");
                icons[a][n].setImage(icons[a][n].getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
            }
        }
    }

    private ImageIcon getIcon(int x, int y) {
        if (g.getBoard().getPiece((byte) x, (byte) y) == 0) return null;
        return icons[(int) Math.abs(g.getBoard().getPiece((byte) x, (byte) y)) - 1][g.getBoard().getPiece((byte) x, (byte) y) > 0 ? 0 : 1];
    }

    public GamePanel(Game g, Frame f) {
        super();

        this.g = g;
        this.g.addBoardChangedListener(new Runnable() {
            @Override
            public void run() {
                selected = -1;
                updateBackgrounds();
            }
        });
        this.setLayout(new GridLayout(8, 8));

        for (int i = 7; i >= 0; i--) {
            for (int n = 0; n < 8; n++) {
                JButton b = new JButton();

                b.setLayout(null);
                b.setBorder(new LineBorder(Color.black, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setFont(new Font("Arial", 1, 50));
                b.addKeyListener(f);

                final int x = n;
                final int y = i;

                b.addActionListener(e -> click((byte) x, (byte) y));

                this.add(b);
                buttons[x][y] = b;
            }
        }
        updateBackgrounds();
        if (!this.g.humansTurn()) {
            runMoveInThread(null);
        }
    }


    public void updateBackgrounds() {
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {
                buttons[i][n].setBackground((i % 2 == 1 && n % 2 == 0 || i % 2 == 0 && n % 2 == 1 ? color_white : color_black));
                buttons[i][n].setIcon(getIcon(i, n));
            }
        }


        if(!g.getBoard().getMoveHistory().empty()){
            Move m = (Move) g.getBoard().getMoveHistory().peek();
            buttons[g.getBoard().x(m.getFrom())][g.getBoard().y(m.getFrom())].setBackground(color_lastMove);
            buttons[g.getBoard().x(m.getTo())][g.getBoard().y(m.getTo())].setBackground(color_lastMove);
        }

        if (selected != -1) {
            for (Object obj : g.getBoard().getAvailableMovesShallow()) {
                if (((Move) obj).getFrom() == selected) {
                    if (g.getBoard().getPiece(((Move) obj).getTo()) != 0) {
                        buttons[g.getBoard().x(((Move) obj).getTo())][g.getBoard().y(((Move) obj).getTo())].setBackground(color_takeable);
                    } else {
                        buttons[g.getBoard().x(((Move) obj).getTo())][g.getBoard().y(((Move) obj).getTo())].setBackground(color_available);
                    }
                }
            }
            buttons[g.getBoard().x(selected)][g.getBoard().y(selected)].setBackground(color_selected);
        }


    }

    private int selected = -1;

    private void click(byte x, byte y) {
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
                for (Object o : g.getBoard().getAvailableMovesShallow()) {
                    if (o instanceof Move) {
                        Move z = (Move) o;

                        if (selected == z.getFrom() && g.getBoard().index(x, y) == z.getTo()) {
                            runMoveInThread(z);
                        }
                    }
                }
            }
        }
        updateBackgrounds();
    }

    public void runMoveInThread(Move z){
        new Thread(new Runnable() {
            @Override
            public void run() {
                g.move(z);
            }
        }).start();
        this.selected = -1;
    }

    public void undo() {
        this.selected = -1;
        this.g.getBoard().undoMove();
        this.updateBackgrounds();
    }
}
