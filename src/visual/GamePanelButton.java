package visual;

import javax.swing.*;
import java.awt.*;

public class GamePanelButton extends JButton {

    private Image pieceImage;

    private GamePanel gamePanel;
    private int x;
    private int y;

    public GamePanelButton(GamePanel gamePanel, int x, int y) {
        this.gamePanel = gamePanel;
        this.x = x;
        this.y = y;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);



        if(gamePanel != null && gamePanel.getColorScheme() != null){
            int z = this.getWidth() / 5;
            Font f = new Font(
                    gamePanel.getColorScheme().getFont().getName(),
                    gamePanel.getColorScheme().getFont().getStyle(), z);
            g2d.setFont(f);

            if(gamePanel.isWhiteSquare(x,y)){
                g2d.setColor(gamePanel.getColorScheme().getColor_black());
            }else{
                g2d.setColor(gamePanel.getColorScheme().getColor_white());
            }

            if(this.x == 0){
                if(this.gamePanel.isFlippedBoard()){
                    g2d.drawString(""+(7-y+1), 5,z);
                }else{
                    g2d.drawString(""+(y+1), 5,z);

                }
            }

            if(this.y == 0){
                if(this.gamePanel.isFlippedBoard()){
                    g2d.drawString("" +(char)('a'+(7-this.x)), this.getWidth() - z,this.getHeight() - z / 2);

                }else{
                    g2d.drawString("" +(char)('a'+(this.x)), this.getWidth() - z,this.getHeight() - z / 2);

                }
            }



        }



        if(pieceImage != null){
            g2d.drawImage(pieceImage, 0,0,this.getWidth(), this.getHeight(), this);
        }
    }

    public void clearContent(){
        this.setText("");
        this.setPieceImage(null);
    }

    public Image getPieceImage() {
        return pieceImage;
    }

    public void setPieceImage(Image pieceImage) {
        this.pieceImage = pieceImage;
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(1000,1000));
        frame.setPreferredSize(new Dimension(1000,1000));
        GamePanelButton button = new GamePanelButton(null,0,0);
        button.setPieceImage(PieceImages.BLACK_KING.getImage());

        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(button, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
