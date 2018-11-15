package edu.jhuapl.sbmt.gui.dtm.controllers.creation;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.controllers.DEMResultsTableController;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DtmCreationPanel;

public class DtmCreationController
{
	DtmCreationPanel panel;
	DtmCreationModel model;
	private DEMResultsTableController resultsController;
	private DtmCreationControlController controlController;

	public DtmCreationController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config)
	{
		model = new DtmCreationModel();
		panel = new DtmCreationPanel();
		resultsController = new DEMResultsTableController(modelManager, pickManager);
		controlController = new DtmCreationControlController(config, model, pickManager);
		init();
	}

	public void init()
    {
        panel.addSubPanel(resultsController.getPanel());
        panel.addSubPanel(controlController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }

}
