package edu.jhuapl.sbmt.gui.image.ui.custom;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class CustomImagesControlPanel extends JPanel
{
    JButton newButton;
    JButton editButton;

    public CustomImagesControlPanel()
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
