package edu.jhuapl.sbmt.gui.dtm.model.creation;

import java.util.Vector;

import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;

public interface DEMCreationModelChangedListener
{
	public void demInfoListChanged(DEMInfo info);

	public void demInfoListChanged(Vector<DEMInfo> infos);
}
