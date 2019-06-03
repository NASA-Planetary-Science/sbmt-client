package edu.jhuapl.sbmt.dtm.controller.creation;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.controller.DEMResultsTableController;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreationModelChangedListener;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;
import edu.jhuapl.sbmt.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.dtm.ui.creation.DtmCreationPanel;

public class DtmCreationController
{
	DtmCreationPanel panel;
	DtmCreationModel model;
	private DEMResultsTableController resultsController;
	private DtmCreationControlController controlController;

	public DtmCreationController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config, DEMCreator creationTool, Renderer renderer)
	{
		model = new DtmCreationModel(modelManager);
		panel = new DtmCreationPanel();
		resultsController = new DEMResultsTableController(modelManager, pickManager, renderer);
		controlController = new DtmCreationControlController(config, model, pickManager, creationTool);

		model.addModelChangedListener(new DEMCreationModelChangedListener()
		{

			@Override
			public void demKeyListChanged(DEMKey info)
			{
				resultsController.getTable().appendRow(new DEMKey(info.demfilename, info.name));
			}

			@Override
			public void demKeysListChanged(List<DEMKey> keys)
			{
				keys.forEach(key -> resultsController.getJTable().setValueAt(key.name, keys.indexOf(key), 3));
				resultsController.getJTable().repaint();
			}

			@Override
			public void demKeyRemoved(DEMKey info)
			{
				resultsController.getTable().removeRow(model.getKeyList().indexOf(info));
			}

			@Override
			public void demKeysRemoved(DEMKey[] infos)
			{
				for (int i=infos.length-1; i>=0; i--)
				{
					int index = model.getKeyList().indexOf(infos[i]);
					resultsController.getTable().removeRow(index);
				}
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

		resultsController.getJTable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				model.setSelectedIndex(resultsController.getJTable().getSelectedRows());
			}
		});

		panel.addComponentListener(new ComponentListener()
		{

			@Override
			public void componentShown(ComponentEvent e)
			{
				 resultsController.addListener();
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e)
			{
			    resultsController.removeListener();
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
