package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.ui.state.popup;
//
//import java.awt.event.MouseEvent;
//import java.sql.Ref;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//import javax.swing.JMenuItem;
//
//import vtk.vtkProp;
//
//import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
//import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
//
//import glum.gui.action.PopupMenu;
//
///**
// * UI component that allows a custom lidar popup menu to be built. (based on code from lopeznr1)
// *
// * @author steelrj1
// */
///**
// * @author steelrj1
// *
// */
//public class StateHistoryPopupMenu extends PopupMenu<StateHistory>
//{
////	private StateHistoryRendererManager rendererManager;
//
//	/**
//	 *
//	 */
//	private Map<JMenuItem, StateHistoryPopAction<StateHistory>> actionM;
//
//	/**
//	 * Standard Constructor
//	 *
//	 * @param aManager
//	 */
//	public StateHistoryPopupMenu(StateHistoryRendererManager rendererManager)
//	{
//		super(rendererManager);
////		this.rendererManager = rendererManager;
//		actionM = new HashMap<>();
//	}
//
//	/**
//	 * Registers the specified StateHistoryPopAction into this StateHistoryPopupMenu.
//	 * <P>
//	 * A simple menu item will be created and associated with the specified
//	 * action.
//	 *
//	 * @param aAction
//	 * @param aTitle
//	 */
//	public void installPopAction(StateHistoryPopAction<StateHistory> aAction, String aTitle)
//	{
//		JMenuItem tmpMI = new JMenuItem(aAction);
//		tmpMI.setText(aTitle);
//
//		// Delegate
//		installPopAction(aAction, tmpMI);
//	}
//
//	/**
//	 * Registers the specified StateHistoryPopAction into this StateHistoryPopupMenu.
//	 * <P>
//	 * The action will be associated with the specified menu item.
//	 */
//	/**
//	 * @param aAction
//	 * @param aTargMI
//	 */
//	public void installPopAction(StateHistoryPopAction<StateHistory> aAction, JMenuItem aTargMI)
//	{
//		add(aTargMI);
//		actionM.put(aTargMI, aAction);
//	}
//
//	/**
//	 *
//	 */
////	@Override
//	public void showPopup(MouseEvent aEvent, vtkProp aPickedProp, int aPickedCellId, double[] aPickedPosition)
//	{
//		// Bail if we do not have selected items
//		Set<StateHistory> tmpS = re getSelectedItems();
//		if (tmpS.size() == 0)
//			return;
//		System.out.println("StateHistoryPopupMenu: showPopup: number elements " + tmpS.size());
//		// Update our StateHistoryPopActions
//		for (JMenuItem aMI : actionM.keySet())
//		{
//			StateHistoryPopAction<StateHistory> tmpLPA = actionM.get(aMI);
//			tmpLPA.setChosenItems(tmpS, aMI);
//		}
//
//		show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
//	}
//}
