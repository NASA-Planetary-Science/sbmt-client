package edu.jhuapl.sbmt.stateHistory.ui.popup;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

/**
 * Action corresponding to the lidar color menu item. This action does not
 * provide any color changing function but rather delegate to sub actions. (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 * @param <G1>
 */
public class MultiColorStateHistoryAction<G1> extends StateHistoryPopAction<G1>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryCollection refManager;

	// State vars
	/**
	 *
	 */
	private Map<JMenuItem, StateHistoryPopAction<G1>> actionM;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aParent
	 * @param aMenu
	 */
	public MultiColorStateHistoryAction(StateHistoryCollection aManager, Component aParent, JMenu aMenu)
	{
		refManager = aManager;

		actionM = new HashMap<>();

		// Form the static color menu items
		for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
		{
			StateHistoryPopAction<G1> tmpLPA = new FixedStateHistoryColorAction<G1>(aManager, color.color());
			JCheckBoxMenuItem tmpColorMI = new JCheckBoxMenuItem(tmpLPA);
			tmpColorMI.setText(color.toString().toLowerCase().replace('_', ' '));
			actionM.put(tmpColorMI, tmpLPA);

			aMenu.add(tmpColorMI);
		}
		aMenu.addSeparator();

		JMenuItem customColorMI = formMenuItem(new CustomStateHistoryColorAction<G1>(aManager, aParent), "Custom...");
		aMenu.add(customColorMI);
		aMenu.addSeparator();

		JMenuItem resetColorMI = formMenuItem(new ResetStateHistoryColorAction<G1>(aManager), "Reset");
		aMenu.add(resetColorMI);
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<G1> aItemL)
	{
		; // Nothing to do
	}

	/**
	 *
	 */
	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

//		// Determine if all selected items have the same (custom) color
//		Color initColor = refManager.getColorProviderTarget(aItemC.iterator().next()).getBaseColor();
//		boolean isSameCustomColor = true;
//		for (G1 aItem : aItemC)
//		{
//			Color evalColor = refManager.getColorProviderTarget(aItem).getBaseColor();
//			isSameCustomColor &= Objects.equals(initColor, evalColor) == true;
//			isSameCustomColor &= refManager.hasCustomColorProvider(aItem) == true;
//		}
//
//		// Update our child LidarPopActions
//		for (JMenuItem aMI : actionM.keySet())
//		{
//			StateHistoryPopAction<G1> tmpLPA = actionM.get(aMI);
//			tmpLPA.setChosenItems(aItemC, aMI);
//
//			// If all items have the same custom color and match one of the
//			// predefined colors then update the corresponding menu item.
//			if (tmpLPA instanceof FixedStateHistoryColorAction)
//			{
//				boolean isSelected = isSameCustomColor == true;
//				isSelected &= ((FixedStateHistoryColorAction) tmpLPA).getColor().equals(initColor) == true;
//				aMI.setSelected(isSelected);
//			}
//		}
	}

	/**
	 * Helper method to form and return the specified menu item.
	 * <P>
	 * The menu item will be registered into the action map.
	 *
	 * @param aAction Action corresponding to the menu item.
	 * @param aTitle The title of the menu item.
	 */
	/**
	 * @param aAction
	 * @param aTitle
	 * @return
	 */
	private JMenuItem formMenuItem(StateHistoryPopAction<G1> aAction, String aTitle)
	{
		JMenuItem retMI = new JMenuItem(aAction);
		retMI.setText(aTitle);

		actionM.put(retMI, aAction);

		return retMI;
	}

}
