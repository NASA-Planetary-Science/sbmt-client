package edu.jhuapl.sbmt.dtm.controller.browse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.DefaultComboBoxModel;

import edu.jhuapl.sbmt.dtm.model.browse.DtmBrowseModel;
import edu.jhuapl.sbmt.dtm.ui.browse.DtmBrowseControlPanel;

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
		panel.getRemoveAllBoundariesButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.getBoundaries().removeAllBoundaries();
				panel.getRemoveAllBoundariesButton().setEnabled(false);
			}
		});

		panel.getUnmapAllDEMsButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.getDems().removeDEMs();
				model.getBoundaries().removeAllBoundaries();
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