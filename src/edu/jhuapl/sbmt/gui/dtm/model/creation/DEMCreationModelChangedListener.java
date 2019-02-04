package edu.jhuapl.sbmt.gui.dtm.model.creation;

import java.util.List;

import edu.jhuapl.sbmt.gui.dtm.model.creation.DtmCreationModel.DEMInfo;

public interface DEMCreationModelChangedListener
{
	public void demInfoListChanged(DEMInfo info);

	public void demInfoListChanged(List<DEMInfo> infos);

	public void demInfoRemoved(DEMInfo info);

	public void demInfosRemoved(DEMInfo[] info);

}
