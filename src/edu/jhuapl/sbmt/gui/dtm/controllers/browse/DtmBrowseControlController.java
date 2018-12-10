package edu.jhuapl.sbmt.gui.dtm.controllers.browse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;

import edu.jhuapl.sbmt.gui.dtm.model.browse.DtmBrowseModel;
import edu.jhuapl.sbmt.gui.dtm.ui.browse.DtmBrowseControlPanel;
import edu.jhuapl.sbmt.model.dem.DEM;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;

public class DtmBrowseControlController
{
	DtmBrowseControlPanel panel;
	DtmBrowseModel model;

	public DtmBrowseControlController(DtmBrowseModel model)
	{
		this.model = model;
		this.panel = new DtmBrowseControlPanel();
		initControls();
	}

	private void initControls()
	{
		panel.getToggleAllBoundariesButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Set<DEM> images = model.getDems().getImages();
				DEMBoundaryCollection boundaries = model.getBoundaries();
				for (DEM dem : images)
				{
					if (boundaries.containsBoundary(dem.getKey()))
					{
						boundaries.removeBoundary(dem);
					}
					else
					{
						boundaries.addBoundary(dem);
					}
				}
			}
		});

		panel.getRemoveAllBoundariesButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				DEMBoundaryCollection boundaries = model.getBoundaries();
				boundaries.removeAllBoundaries();
				panel.getRemoveAllBoundariesButton().setEnabled(false);
			}
		});

		panel.getUnmapAllDEMsButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.getDems().removeDEMs();
				panel.getUnmapAllDEMsButton().setEnabled(false);
			}
		});

		panel.getDatasetComboBox().setModel(new DefaultComboBoxModel<String>(model.getDataSets()));

		panel.getDatasetComboBox().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					model.loadDtmSet((String)(panel.getDatasetComboBox().getSelectedItem()));
				}
				catch (FileNotFoundException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

	public DtmBrowseControlPanel getPanel()
	{
		return panel;
	}

}
