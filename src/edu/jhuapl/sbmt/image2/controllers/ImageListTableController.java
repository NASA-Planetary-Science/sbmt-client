package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.table.ImageListTableView;

import glum.gui.action.PopupMenu;

public class ImageListTableController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	ImageListTableView<G1> tablePanel;

	public ImageListTableController(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.tablePanel = new ImageListTableView<G1>(collection, popupMenu);
		this.tablePanel.setup();
	}

	public ImageListTableView<G1> getPanel()
	{
		return tablePanel;
	}
}
