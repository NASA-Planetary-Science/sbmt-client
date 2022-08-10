package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.custom.table.CustomImageListTableView;

import glum.gui.action.PopupMenu;

public class CustomImageListTableController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	CustomImageListTableView tablePanel;

	public CustomImageListTableController(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.tablePanel = new CustomImageListTableView(collection, popupMenu);
		this.tablePanel.setup();
	}

	public CustomImageListTableView getPanel()
	{
		return tablePanel;
	}
}
