package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.ui.state.popup;
//
//import java.awt.event.ActionEvent;
//import java.util.Collection;
//import java.util.List;
//
//import javax.swing.AbstractAction;
//import javax.swing.JMenuItem;
//
//import com.google.common.collect.ImmutableList;
//
///**
// * Base Action specific to lidar popup menus.
// * <P>
// * Whenever the list of selected lidar objects changes this LidarPopAction will
// * be notified.
// *
// * (based on code from lopeznr1)
// *
// * @author steelrj1
// */
///**
// * @author steelrj1
// *
// * @param <G1>
// */
//public abstract class StateHistoryPopAction<G1> extends AbstractAction
//{
//	// State vars
//	/**
//	 *
//	 */
//	private ImmutableList<G1> itemL;
//
//	/**
//	 * Standard Constructor
//	 */
//	/**
//	 *
//	 */
//	public StateHistoryPopAction()
//	{
//		itemL = ImmutableList.of();
//	}
//
//	/**
//	 * Notification that the LidarPopAction should be executed on the specified
//	 * lidar objects.
//	 *
//	 * @param aItemL
//	 */
//	/**
//	 * @param aItemL
//	 */
//	public abstract void executeAction(List<G1> aItemL);
//
//	/**
//	 * Sets in the lidar objects that are currently selected.
//	 */
//	/**
//	 * @param aItemC
//	 * @param aAssocMI
//	 */
//	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
//	{
//		System.out.println("StateHistoryPopAction: setChosenItems: ");
//		itemL = ImmutableList.copyOf(aItemC);
//	}
//
//	/**
//	 *
//	 */
//	@Override
//	public void actionPerformed(ActionEvent aAction)
//	{
//		System.out.println("StateHistoryPopAction: actionPerformed: ");
//		executeAction(itemL);
//	}
//
//}
