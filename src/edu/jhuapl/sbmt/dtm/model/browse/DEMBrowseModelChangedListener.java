package edu.jhuapl.sbmt.dtm.model.browse;

import java.util.List;

import edu.jhuapl.sbmt.dtm.model.DEMKey;

public interface DEMBrowseModelChangedListener
{
	public void demKeyListChanged(DEMKey key);

	public void demKeysListChanged(List<DEMKey> key);
}
