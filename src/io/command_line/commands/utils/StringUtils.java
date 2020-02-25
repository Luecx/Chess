package io.command_line.commands.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class StringUtils {

    private static final DecimalFormat df = new DecimalFormat("#.###");
    private static final DecimalFormat df2 = new DecimalFormat("#");
    static {
        df.setRoundingMode(RoundingMode.CEILING);
        df2.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static String roundFull(double value) {
        return df2.format(value);
    }

    public static String round(double value) {
        return df.format(value);
    }

    public static boolean isNumeric(String str) {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }


}
