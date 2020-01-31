package edu.jhuapl.sbmt.dtm.controller;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.controller.browse.DtmBrowseController;
import edu.jhuapl.sbmt.dtm.controller.creation.DtmCreationController;
import edu.jhuapl.sbmt.dtm.controller.search.DtmSearchController;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;

public class ExperimentalDEMController
{
	JPanel panel;
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	DtmSearchController searchController;
	DtmBrowseController browseController;
	DtmCreationController creationController;
	SmallBodyViewConfig config;

	public ExperimentalDEMController(ModelManager modelManager, PickManager pickManager, DEMCreator creationTool, SmallBodyViewConfig config, Renderer renderer)
	{
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		searchController = new DtmSearchController(modelManager, pickManager, renderer);
		browseController = new DtmBrowseController(modelManager, pickManager, config, renderer);
		creationController = new DtmCreationController(modelManager, pickManager, config, creationTool, renderer);
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
