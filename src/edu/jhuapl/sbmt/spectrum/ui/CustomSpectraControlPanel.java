package edu.jhuapl.sbmt.spectrum.ui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class CustomSpectraControlPanel extends JPanel
{
    JButton newButton;
    JButton editButton;

    public CustomSpectraControlPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        newButton = new JButton("New...");
        add(newButton);

        editButton = new JButton("Edit...");
        add(editButton);
    }

    public JButton getNewButton()
    {
        return newButton;
    }

    public JButton getEditButton()
    {
        return editButton;
    }
}