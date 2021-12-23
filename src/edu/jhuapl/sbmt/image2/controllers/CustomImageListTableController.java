package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.custom.table.CustomImageListTableView;

import glum.gui.action.PopupMenu;

public class CustomImageListTableController
{
	CustomImageListTableView tablePanel;

	public CustomImageListTableController(PerspectiveImageCollection collection, PopupMenu popupMenu)
	{
		this.tablePanel = new CustomImageListTableView(collection, popupMenu);
		this.tablePanel.setup();
	}


	public CustomImageListTableView getPanel()
	{
		return tablePanel;
	}
}
