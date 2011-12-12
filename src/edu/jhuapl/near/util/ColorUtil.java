package edu.jhuapl.near.util;

import java.awt.Color;

public class ColorUtil
{
    public enum DefaultColor
    {
        RED (Color.RED),
        PINK (Color.PINK),
        ORANGE (Color.ORANGE),
        YELLOW (Color.YELLOW),
        GREEN (Color.GREEN),
        MAGENTA (Color.MAGENTA),
        CYAN (Color.CYAN),
        BLUE (Color.BLUE),
        WHITE (Color.WHITE),
        LIGHT_GRAY (Color.LIGHT_GRAY),
        GRAY (Color.GRAY),
        DARK_GRAY (Color.DARK_GRAY),
        BLACK (Color.BLACK);

        private final Color color;
        DefaultColor(Color color)
        {
            this.color = color;
        }

        public Color color()
        {
            return color;
        }
    };

    // From http://stackoverflow.com/questions/223971/how-to-generate-spectrum-color-palettes
    static public Color[] generateColors(int n)
    {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
        {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }

}
