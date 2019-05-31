package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.model.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * ItemHandler used to process LidarFileSpecs.
 *
 * @author lopeznr1
 */
class FileSpecItemHandler extends BasicItemHandler<LidarFileSpec>
{
	// Ref vars
	private final LidarFileSpecManager refManager;

	/**
	 * Standard Constructor
	 */
	public FileSpecItemHandler(LidarFileSpecManager aManager, QueryComposer<?> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(LidarFileSpec aFileSpec, int aColIdx)
	{
		switch (aColIdx)
		{
			case 0:
				return refManager.getIsVisible(aFileSpec);
			case 1:
				return refManager.getColor(aFileSpec);
			case 2:
				return refManager.getNumberOfPoints(aFileSpec);
			case 3:
				return aFileSpec.getName();
			case 4:
				return aFileSpec.getTimeBeg();
			case 5:
				return aFileSpec.getTimeEnd();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Index: " + aColIdx);
	}

	@Override
	public void setColumnValue(LidarFileSpec aFileSpec, int aColIdx, Object aValue)
	{
		List<LidarFileSpec> tmpL = ImmutableList.of(aFileSpec);
		if (aColIdx == 0)
			refManager.setIsVisible(tmpL, (boolean) aValue);
		else if (aColIdx == 1)
			refManager.setColor(tmpL, (Color) aValue);
		else
			throw new UnsupportedOperationException("Column is not supported. Index: " + aColIdx);
	}

}
