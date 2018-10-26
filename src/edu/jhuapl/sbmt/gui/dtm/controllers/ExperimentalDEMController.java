package edu.jhuapl.sbmt.gui.dtm.controllers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.controllers.browse.DtmBrowseController;
import edu.jhuapl.sbmt.gui.dtm.controllers.search.DtmSearchController;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DEMCreationPanel;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DEMCreator;

public class ExperimentalDEMController
{
	JPanel panel;
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	DEMCreationPanel createPanel;
	DtmSearchController searchController;
	DtmBrowseController browseController;


	public ExperimentalDEMController(ModelManager modelManager, PickManager pickManager, DEMCreator creationTool, SmallBodyViewConfig config)
	{
//		panel = new ExperimentalDEMPanel(modelManager, pickManager, creationTool);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		createPanel=new DEMCreationPanel(modelManager, pickManager, creationTool);
		searchController = new DtmSearchController(modelManager, pickManager, creationTool);
		browseController = new DtmBrowseController(modelManager, pickManager, config);
        init();
	}

	public void init()
    {
		panel.add(tabbedPane);
        tabbedPane.addTab("Create", createPanel);
        tabbedPane.addTab("Search", searchController.getPanel());
        tabbedPane.addTab("Browse", browseController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }



}
