package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;

import java.net.URL;
import javax.swing.*;

public class ToolBar extends JToolBar 
{
	public ToolBar()
	{
		JButton button = new JButton();
        //button.setActionCommand();
		button.setToolTipText("Select a point of Eros");
        button.addActionListener(new SelectPointAction());
        URL imageURL = ToolBar.class.getResource("/edu/jhuapl/near/data/point.png");
        button.setIcon(new ImageIcon(imageURL));

        add(button);

        button = new JButton();
        //button.setActionCommand();
        button.setToolTipText("Select a rectangular region of Eros");
        button.addActionListener(new SelectRegionAction());
        imageURL = ToolBar.class.getResource("/edu/jhuapl/near/data/rectangle.png");
        button.setIcon(new ImageIcon(imageURL));

        add(button);

        button = new JButton("Clear");
        //button.setActionCommand();
        button.setToolTipText("Clear any previous selection");
        button.addActionListener(new ClearSelectionAction());

        add(button);
	}
        
    private static class SelectPointAction extends AbstractAction
    {
        public SelectPointAction()
        {
            super("Select Point");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        }
    }

    private static class SelectRegionAction extends AbstractAction
    {
        public SelectRegionAction()
        {
            super("Select Region");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        }
    }

    private static class ClearSelectionAction extends AbstractAction
    {
        public ClearSelectionAction()
        {
            super("Clear Selection");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        }
    }
}
