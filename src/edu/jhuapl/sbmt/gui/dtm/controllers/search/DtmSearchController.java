package edu.jhuapl.sbmt.gui.dtm.controllers.search;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.gui.dtm.controllers.DEMResultsTableController;
import edu.jhuapl.sbmt.gui.dtm.ui.search.DtmSearchPanel;

public class DtmSearchController
{
	private DtmSearchPanel panel;
	DEMResultsTableController resultsController;

	public DtmSearchController(ModelManager modelManager, PickManager pickManager, Renderer renderer)
	{
		resultsController = new DEMResultsTableController(modelManager, pickManager, renderer);
		init();
	}


	public void init()
    {
        panel = new DtmSearchPanel();
        panel.addSubPanel(resultsController.getPanel());

    }

    public JPanel getPanel()
    {
        return panel;
    }
}
