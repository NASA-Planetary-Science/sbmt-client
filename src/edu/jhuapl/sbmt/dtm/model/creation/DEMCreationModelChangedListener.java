package edu.jhuapl.sbmt.dtm.model.creation;

import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.model.browse.DEMBrowseModelChangedListener;

public interface DEMCreationModelChangedListener extends DEMBrowseModelChangedListener
{
	public void demKeyRemoved(DEMKey info);

	public void demKeysRemoved(DEMKey[] info);
}
