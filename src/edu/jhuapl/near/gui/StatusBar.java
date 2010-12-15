package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class StatusBar extends JPanel
{
    private JLabel leftLabel;
    private JLabel rightLabel;

    public StatusBar()
    {
        setLayout(new BorderLayout());
        leftLabel = new JLabel(" ", SwingConstants.LEFT);
        add(leftLabel, BorderLayout.CENTER);
        rightLabel = new JLabel(" ", SwingConstants.RIGHT);
        add(rightLabel, BorderLayout.EAST);

        Font font = new Font("Monospaced", Font.PLAIN, 13);
        rightLabel.setFont(font);

        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public void setLeftText(String text)
    {
        if (text.length() == 0)
            text = " ";
        leftLabel.setText(text);
    }

    public void setRightText(String text)
    {
        if (text.length() == 0)
            text = " ";
        rightLabel.setText(text);
    }
}
