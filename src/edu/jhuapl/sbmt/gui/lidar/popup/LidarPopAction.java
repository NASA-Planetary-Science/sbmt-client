package edu.jhuapl.sbmt.gui.lidar.popup;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import com.google.common.collect.ImmutableList;

/**
 * Base Action specific to lidar popup menus.
 * <P>
 * Whenever the list of selected lidar objects changes this LidarPopAction will
 * be notified.
 *
 * @author lopeznr1
 */
public abstract class LidarPopAction<G1> extends AbstractAction
{
	// State vars
	private ImmutableList<G1> itemL;

	/**
	 * Standard Constructor
	 */
	public LidarPopAction()
	{
		itemL = ImmutableList.of();
	}

	/**
	 * Notification that the LidarPopAction should be executed on the specified
	 * lidar objects.
	 *
	 * @param aItemL
	 */
	public abstract void executeAction(List<G1> aItemL);

	/**
	 * Sets in the lidar objects that are currently selected.
	 */
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		itemL = ImmutableList.copyOf(aItemC);
	}

	@Override
	public void actionPerformed(ActionEvent aAction)
	{
		executeAction(itemL);
	}

}
