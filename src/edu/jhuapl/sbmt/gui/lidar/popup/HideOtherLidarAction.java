package edu.jhuapl.sbmt.gui.lidar.popup;

import java.util.List;

import edu.jhuapl.sbmt.model.lidar.LidarManager;

/**
 * Object that defines the action: "Hide Other Items".
 *
 * @author lopeznr1
 */
class HideOtherLidarAction<G1> extends LidarPopAction<G1>
{
	// Ref vars
	private final LidarManager<G1> refManager;

	/**
	 * Standard Constructor
	 */
	public HideOtherLidarAction(LidarManager<G1> aManager)
	{
		refManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		refManager.setOthersHiddenExcept(aItemL);
	}
}