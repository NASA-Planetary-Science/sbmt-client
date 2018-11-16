package edu.jhuapl.sbmt.gui.dtm.controllers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.controllers.browse.DtmBrowseController;
import edu.jhuapl.sbmt.gui.dtm.controllers.creation.DtmCreationController;
import edu.jhuapl.sbmt.gui.dtm.controllers.search.DtmSearchController;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DEMCreator;

public class ExperimentalDEMController
{
	JPanel panel;
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	DtmSearchController searchController;
	DtmBrowseController browseController;
	DtmCreationController creationController;
	SmallBodyViewConfig config;

	public ExperimentalDEMController(ModelManager modelManager, PickManager pickManager, DEMCreator creationTool, SmallBodyViewConfig config)
	{
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		searchController = new DtmSearchController(modelManager, pickManager);
		browseController = new DtmBrowseController(modelManager, pickManager, config);
		creationController = new DtmCreationController(modelManager, pickManager, config, creationTool);
		this.config = config;
        init();
	}

	public void init()
    {
		panel.add(tabbedPane);
        tabbedPane.addTab("Create", creationController.getPanel());
        if (!config.dtmBrowseDataSourceMap.isEmpty())
        	tabbedPane.addTab("Browse", browseController.getPanel());
        if (!config.dtmSearchDataSourceMap.isEmpty())
        	tabbedPane.addTab("Search", searchController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }
}
