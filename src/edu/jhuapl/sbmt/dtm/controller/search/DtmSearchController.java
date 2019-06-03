package edu.jhuapl.sbmt.dtm.controller.search;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.dtm.controller.DEMResultsTableController;
import edu.jhuapl.sbmt.dtm.ui.search.DtmSearchPanel;

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
    }

    public JPanel getPanel()
    {
        return panel;
    }
}
