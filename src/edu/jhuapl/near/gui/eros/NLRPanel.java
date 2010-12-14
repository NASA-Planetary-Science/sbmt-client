package edu.jhuapl.near.gui.eros;

import javax.swing.*;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;


public class NLRPanel extends JTabbedPane
{
    public NLRPanel(
            final ModelManager modelManager,
            final PickManager pickManager)
    {
        setBorder(BorderFactory.createEmptyBorder());

        NLRBrowsePanel nlrBrowsePanel = new NLRBrowsePanel(modelManager);
        NLRSearchPanel nlrSearchPanel = new NLRSearchPanel(modelManager, pickManager);

        addTab("Browse", nlrBrowsePanel);
        addTab("Search", nlrSearchPanel);
    }
}
