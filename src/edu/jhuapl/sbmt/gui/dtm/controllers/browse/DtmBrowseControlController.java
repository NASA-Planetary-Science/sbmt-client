package edu.jhuapl.sbmt.gui.dtm.controllers.browse;

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
	}

	public DtmBrowseControlPanel getPanel()
	{
		return panel;
	}

}
