package edu.jhuapl.sbmt.gui.dtm.controllers.creation;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dem.DEMPopupMenu;
import edu.jhuapl.sbmt.gui.dtm.controllers.DEMResultsTableController;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DEMCreationModelChangedListener;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel;
import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DEMCreator;
import edu.jhuapl.sbmt.gui.dtm.ui.creation.DtmCreationPanel;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class DtmCreationController
{
	DtmCreationPanel panel;
	DtmCreationModel model;
	private DEMResultsTableController resultsController;
	private DtmCreationControlController controlController;
	private DEMPopupMenu demPopupMenu;

	public DtmCreationController(ModelManager modelManager, PickManager pickManager, SmallBodyViewConfig config, DEMCreator creationTool, Renderer renderer)
	{
		model = new DtmCreationModel(modelManager);
		panel = new DtmCreationPanel();
		resultsController = new DEMResultsTableController(modelManager, pickManager);
		controlController = new DtmCreationControlController(config, model, pickManager, creationTool);

		// Construct popup menu (right click action)
		DEMCollection dems = (DEMCollection)modelManager.getModel(ModelNames.DEM);
        DEMBoundaryCollection boundaries = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);
        demPopupMenu = new DEMPopupMenu(modelManager.getPolyhedralModel(), dems, boundaries, renderer, panel);
        if (config.hasBigmap == false && config.hasMapmaker == false)
        {
        	controlController.panel.getMapmakerSubmitButton().setVisible(false);
        }

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

			@Override
			public void demInfoRemoved(DEMInfo info)
			{
				resultsController.getTable().removeRow(model.getInfoList().indexOf(info));
			}

			@Override
			public void demInfosRemoved(DEMInfo[] infos)
			{
				for (int i=infos.length-1; i>=0; i--)
				{
					int index = model.getInfoList().indexOf(infos[i]);
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

		resultsController.getJTable().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
            	imageListMaybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
            	imageListMaybeShowPopup(e);
            }
        });


		init();
	}

    // Popup menu for when using right clicks
    private void imageListMaybeShowPopup(MouseEvent e)
    {
    	if (model.getSelectedIndices() == null) return;
        if (e.isPopupTrigger())
        {
            int[] selectedIndices = model.getSelectedIndices();
            List<DEMKey> demKeys = new ArrayList<DEMKey>();
            for (int selectedIndex : selectedIndices)
            {
                DEMInfo demInfo = model.getSelectedItem();
                String name = /*model.getCustomDataFolder() + File.separator +*/ demInfo.demfilename;
                DEMKey demKey = new DEMKey(name, demInfo.name);
                demKeys.add(demKey);
            }
            demPopupMenu.setCurrentDEMs(demKeys);
            demPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
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
