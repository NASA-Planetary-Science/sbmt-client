package edu.jhuapl.near.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

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

    /**
     * Purpose: this icon simply draws a square with its border in black and
     * its inside in the specified color (passed into the constructor).
     * It is used for showing the current color of something.
     */
    public static class ColorIcon implements Icon
    {

        private int width = 12;
        private int height = 12;
        private Color color;

        public ColorIcon(Color color)
        {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setColor(color);
            g2d.fillRect(x +1 ,y + 1,width -2 ,height -2);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(x +1 ,y + 1,width -2 ,height -2);

            g2d.dispose();
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }

        public Color getColor()
        {
            return color;
        }
    }
}
