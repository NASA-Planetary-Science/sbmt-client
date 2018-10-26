package edu.jhuapl.sbmt.gui.dtm.controllers.browse;

import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dtm.controllers.DEMResultsTableController;
import edu.jhuapl.sbmt.gui.dtm.model.browse.DEMBrowseModelChangedListener;
import edu.jhuapl.sbmt.gui.dtm.model.browse.DtmBrowseModel;
import edu.jhuapl.sbmt.gui.dtm.ui.browse.DtmBrowsePanel;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmBrowseController
{
	private DtmBrowsePanel panel;
	private DEMResultsTableController resultsController;
	private DtmBrowseModel model;

	public DtmBrowseController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config)
	{
		resultsController = new DEMResultsTableController(modelManager, pickManager);
		panel = new DtmBrowsePanel();
        model = new DtmBrowseModel(modelManager, pickManager, config);


        model.addModelChangedListener(new DEMBrowseModelChangedListener()
		{

			@Override
			public void demKeysListChanged(DEMKey key)
			{
				resultsController.getTable().appendRow(key);
			}

			@Override
			public void demKeysListChanged(Vector<DEMKey> keys)
			{
				for (DEMKey key : keys)
				{
					System.out.println(
							"DtmBrowseController.DtmBrowseController(...).new DEMBrowseModelChangedListener() {...}: demKeysListChanged: key is " + key.fileName);
					resultsController.getTable().appendRow(key);
				}
				System.out.println(
						"DtmBrowseController.DtmBrowseController(...).new DEMBrowseModelChangedListener() {...}: demKeysListChanged: number of rows " + resultsController.getTable().getNumberOfRows());
			}
		});

        try
		{
			model.loadAllDtmPaths();
		}
        catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

		init();
	}

	public void init()
    {

        panel.addSubPanel(resultsController.getPanel());

    }

    public JPanel getPanel()
    {
        return panel;
    }

}
