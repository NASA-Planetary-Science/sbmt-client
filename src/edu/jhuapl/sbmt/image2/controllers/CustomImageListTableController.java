package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.saavtk.model.IPositionOrientationManager;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.custom.table.CustomImageListTableView;

import glum.gui.action.PopupMenu;

public class CustomImageListTableController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	CustomImageListTableView<G1> tablePanel;
	IPositionOrientationManager<SmallBodyModel> positionOrientationManager;

	public CustomImageListTableController(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.tablePanel = new CustomImageListTableView<G1>(collection, popupMenu);
		this.tablePanel.setup();
	}

	public CustomImageListTableView<G1> getPanel()
	{
		return tablePanel;
	}
}
