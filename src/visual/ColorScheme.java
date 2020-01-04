package visual;

import java.awt.*;

public enum ColorScheme {



    GREEN(new Color(0xEBECD0),
            new Color(0x779556),
            new Color(0xF6F879),
            new Color(0xB9CC36),
            new Color(0xF6F879),
            new Color(0xB9CC36),
            new Color(0xadb066),
            new Color(0xB09F54),
            new Color(0xC8342F),
            new Color(0xC8342F),
            new Font("Arial", Font.TRUETYPE_FONT, 0)),

    GRAY(new Color(0xA0A0A0),
            new Color(0x666666),
            new Color(0xEDEDED),
            new Color(0xBCBCBC),
            new Color(0xEDEDED),
            new Color(0x212121),
            new Color(0xB4B4B4),
            new Color(0x828282),
            new Color(0xC8342F),
            new Color(0xC8342F),
            new Font("Arial", Font.TRUETYPE_FONT, 0));



    ColorScheme(Color color_white,
                Color color_black,

                Color color_selected_white,
                Color color_selected_black,

                Color color_lastMove_white,
                Color color_lastMove_black,

                Color color_available_white,
                Color color_available_black,

                Color color_takeable_white,
                Color color_takeable_black,

                Font font) {

        this.font = font;

        this.color_white = color_white;
        this.color_black = color_black;
        this.color_selected_white = color_selected_white;
        this.color_selected_black = color_selected_black;
        this.color_lastMove_white = color_lastMove_white;
        this.color_lastMove_black = color_lastMove_black;
        this.color_available_white = color_available_white;
        this.color_available_black = color_available_black;
        this.color_takeable_white = color_takeable_white;
        this.color_takeable_black = color_takeable_black;
    }


    private final Font font;

    private final Color color_white;
    private final Color color_black;

    private final Color color_selected_white;
    private final Color color_selected_black;

    private final Color color_lastMove_white;
    private final Color color_lastMove_black;

    private final Color color_available_white;
    private final Color color_available_black;

    private final Color color_takeable_white;
    private final Color color_takeable_black;


    public Color getColor_white() {
        return color_white;
    }

    public Color getColor_black() {
        return color_black;
    }

    public Color getColor_selected_white() {
        return color_selected_white;
    }

    public Color getColor_selected_black() {
        return color_selected_black;
    }

    public Color getColor_lastMove_white() {
        return color_lastMove_white;
    }

    public Color getColor_lastMove_black() {
        return color_lastMove_black;
    }

    public Color getColor_available_white() {
        return color_available_white;
    }

    public Color getColor_available_black() {
        return color_available_black;
    }

    public Color getColor_takeable_white() {
        return color_takeable_white;
    }

    public Color getColor_takeable_black() {
        return color_takeable_black;
    }

    public Font getFont() {
        return font;
    }
}
