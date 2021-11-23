package edu.jhuapl.sbmt.image2.controllers;

import javax.swing.JPanel;

import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.table.ImageListTableView;

public class ImageListTableController
{
	ImageListTableView tablePanel;

	public ImageListTableController(PerspectiveImageCollection collection)
	{
		this.tablePanel = new ImageListTableView(collection);
		this.tablePanel.setup();
	}


	public JPanel getPanel()
	{
		return tablePanel;
	}
}
