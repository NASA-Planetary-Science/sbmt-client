package edu.jhuapl.sbmt.gui.dtm.controllers.browse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.DefaultComboBoxModel;

import edu.jhuapl.sbmt.gui.dtm.model.browse.DtmBrowseModel;
import edu.jhuapl.sbmt.gui.dtm.ui.browse.DtmBrowseControlPanel;

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
				// TODO Auto-generated method stub

			}
		});

		panel.getToggleAllDEMsButton().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub

			}
		});

//		panel.setDatasetComboBox(new JComboBox<String>(model.getDataSets()));
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
