package edu.jhuapl.sbmt.gui.dtm.model.browse;

import java.util.Vector;

import edu.jhuapl.sbmt.model.dem.DEMKey;

public interface DEMBrowseModelChangedListener
{
	public void demKeysListChanged(DEMKey key);

	public void demKeysListChanged(Vector<DEMKey> key);
}
