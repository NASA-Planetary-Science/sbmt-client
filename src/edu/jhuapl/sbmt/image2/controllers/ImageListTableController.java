package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.table.ImageListTableView;

import glum.gui.action.PopupMenu;

public class ImageListTableController
{
	ImageListTableView tablePanel;

	public ImageListTableController(PerspectiveImageCollection collection, PopupMenu popupMenu)
	{
		this.tablePanel = new ImageListTableView(collection, popupMenu);
		this.tablePanel.setup();
	}


	public ImageListTableView getPanel()
	{
		return tablePanel;
	}
}
