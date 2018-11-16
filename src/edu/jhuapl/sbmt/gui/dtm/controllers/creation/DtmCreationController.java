package edu.jhuapl.sbmt.gui.dtm.controllers.creation;

import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.controllers.DEMResultsTableController;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DEMCreationModelChangedListener;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DEMCreator;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DtmCreationPanel;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmCreationController
{
	DtmCreationPanel panel;
	DtmCreationModel model;
	private DEMResultsTableController resultsController;
	private DtmCreationControlController controlController;

	public DtmCreationController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config, DEMCreator creationTool)
	{
		model = new DtmCreationModel(modelManager);
		panel = new DtmCreationPanel();
		resultsController = new DEMResultsTableController(modelManager, pickManager);
		System.out.println("DtmCreationController: DtmCreationController: adding listener");
		model.addModelChangedListener(new DEMCreationModelChangedListener()
		{

			@Override
			public void demInfoListChanged(DEMInfo info)
			{
				resultsController.getTable().appendRow(new DEMKey(info.demfilename, info.name));
			}

			@Override
			public void demInfoListChanged(List<DEMInfo> infos)
			{
				for (DEMInfo info : infos)
				{
					resultsController.getJTable().setValueAt(info.name, infos.indexOf(info), 3);
				}
				resultsController.getJTable().repaint();
			}
		});

		try
		{
			model.initializeDEMList();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		controlController = new DtmCreationControlController(config, model, pickManager, creationTool);


		resultsController.getJTable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{

			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				model.setSelectedIndex(resultsController.getJTable().getSelectedRows());
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
