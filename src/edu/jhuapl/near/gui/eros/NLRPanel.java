package edu.jhuapl.near.gui.eros;

import javax.swing.*;

import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.pick.PickManager;


public class NLRPanel extends JTabbedPane
{
    public NLRPanel(
    		final ErosModelManager modelManager,
    		final PickManager pickManager) 
    {
        setBorder(BorderFactory.createEmptyBorder());

        NLRBrowsePanel nlrBrowsePanel = new NLRBrowsePanel(modelManager);
        NLRSearchPanel nlrSearchPanel = new NLRSearchPanel(modelManager, pickManager);

        addTab("Browse", nlrBrowsePanel);
        addTab("Search", nlrSearchPanel);
    }
}
