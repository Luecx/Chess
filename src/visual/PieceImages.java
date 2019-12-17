package visual;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public enum PieceImages {


    WHITE_PAWN("resources/11.png","\u2659"),
    WHITE_ROOK("resources/21.png","\u2656"),
    WHITE_KNIGHT("resources/31.png","\u2658"),
    WHITE_BISHOP("resources/41.png","\u2657"),
    WHITE_QUEEN("resources/51.png","\u2655"),
    WHITE_KING("resources/61.png","\u2654"),

    BLACK_PAWN("resources/12.png","\u265F"),
    BLACK_ROOK("resources/22.png","\u265C"),
    BLACK_KNIGHT("resources/32.png","\u265E"),
    BLACK_BISHOP("resources/42.png","\u265D"),
    BLACK_QUEEN("resources/52.png","\u265B"),
    BLACK_KING("resources/62.png","\u265A");


    private Image image;
    private String string;

    PieceImages(String image, String string) {
        try {
            this.image = ImageIO.read(new File(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.string = string;
    }

    public static PieceImages getImage(int value){
        if(value == 0) return null;
        int index = Math.abs(value) - 1;
        int color = value < 0 ? 1:0;
        return PieceImages.values()[index + 6 * color];
    }

    public Image getImage() {
        return image;
    }

    public String getString() {
        return string;
    }

}
