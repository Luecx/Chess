package visual;

import javax.swing.*;
import java.awt.*;

public class GamePanelButton extends JButton {

    private Image pieceImage;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(pieceImage != null){

            Graphics2D g2d = (Graphics2D) g;
            g2d.getRenderingHints().add(new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON));
            g2d.getRenderingHints().add(new RenderingHints(
                    RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
            g2d.getRenderingHints().add(new RenderingHints(
                    RenderingHints.KEY_DITHERING,
                    RenderingHints.VALUE_DITHER_ENABLE));
            g2d.getRenderingHints().add(new RenderingHints(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.getRenderingHints().add(new RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY));

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
}
