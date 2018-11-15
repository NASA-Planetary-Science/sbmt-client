package edu.jhuapl.sbmt.gui.dtm.controllers.creation;

import java.util.Vector;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.controllers.DEMResultsTableController;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DEMCreationModelChangedListener;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DtmCreationPanel;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmCreationController
{
	DtmCreationPanel panel;
	DtmCreationModel model;
	private DEMResultsTableController resultsController;
	private DtmCreationControlController controlController;

	public DtmCreationController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config)
	{
		model = new DtmCreationModel(modelManager);
		panel = new DtmCreationPanel();
		resultsController = new DEMResultsTableController(modelManager, pickManager);
		controlController = new DtmCreationControlController(config, model, pickManager);

		model.addModelChangedListener(new DEMCreationModelChangedListener()
		{

			@Override
			public void demInfoListChanged(DEMInfo info)
			{
				resultsController.getTable().appendRow(new DEMKey(info.demfilename, info.name));
			}

			@Override
			public void demInfoListChanged(Vector<DEMInfo> infos)
			{
				for (DEMInfo info : infos)
				{
					resultsController.getTable().appendRow(new DEMKey(info.demfilename, info.name));
				}
			}
		});


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
