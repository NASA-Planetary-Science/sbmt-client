package edu.jhuapl.sbmt.dtm.controller.browse;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.controller.DEMResultsTableController;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.model.browse.DEMBrowseModelChangedListener;
import edu.jhuapl.sbmt.dtm.model.browse.DtmBrowseModel;
import edu.jhuapl.sbmt.dtm.ui.browse.DtmBrowsePanel;

public class DtmBrowseController
{
	private DtmBrowsePanel panel;
	private DEMResultsTableController resultsController;
	private DtmBrowseControlController controlsController;
	private DtmBrowseModel model;

	public DtmBrowseController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config, Renderer renderer)
	{
		resultsController = new DEMResultsTableController(modelManager, pickManager, renderer);
		panel = new DtmBrowsePanel();
        model = new DtmBrowseModel(modelManager, pickManager, config);
		controlsController = new DtmBrowseControlController(model);

        model.addModelChangedListener(new DEMBrowseModelChangedListener()
		{
			@Override
			public void demKeyListChanged(DEMKey key)
			{
				resultsController.addKey(key);
			}

			@Override
			public void demKeysListChanged(List<DEMKey> keys)
			{
				keys.forEach(k -> resultsController.addKey(k));
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

        model.getDems().addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				boolean enabled = false;
				if (model.getDems().getImages().size() > 0) enabled = true;
				controlsController.getPanel().getUnmapAllDEMsButton().setEnabled(enabled);
			}
		});

        model.getBoundaries().addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				boolean enabled = false;
				if (model.getBoundaries().getProps().size() > 0) enabled = true;
				controlsController.getPanel().getRemoveAllBoundariesButton().setEnabled(enabled);
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
        panel.addSubPanel(controlsController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }
}
