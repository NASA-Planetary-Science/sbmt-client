package edu.jhuapl.sbmt.image2.ui.table.popup.boundaryColor;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class BoundaryColorAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{

	private PerspectiveImageCollection aManager;

	// State vars
		private Map<JMenuItem, PopAction<G1>> actionM;

    public BoundaryColorAction(PerspectiveImageCollection aManager, Component aParent, JMenu aMenu)
    {
    	this.aManager = aManager;


    	actionM = new HashMap<>();

		// Form the static color menu items
		for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
		{
			PopAction<G1> tmpLPA = new FixedImageColorAction<>(aManager, color.color());
			JCheckBoxMenuItem tmpColorMI = new JCheckBoxMenuItem(tmpLPA);
			tmpColorMI.setText(color.toString().toLowerCase().replace('_', ' '));
			actionM.put(tmpColorMI, tmpLPA);

			aMenu.add(tmpColorMI);
		}
		aMenu.addSeparator();

		JMenuItem customColorMI = formMenuItem(new CustomBoundaryColorAction<>(aManager, aParent), "Custom...");
		aMenu.add(customColorMI);
		aMenu.addSeparator();

		JMenuItem resetColorMI = formMenuItem(new ResetBoundaryColorAction<>(aManager), "Reset");
		aMenu.add(resetColorMI);
    }

    @Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() == 0)
			return;

		for (IPerspectiveImage aItem : aItemL)
		{
//			aManager.setImageBoundaryColor(aItem, color);
			//TODO FIX THIS
//			PerspectiveImageBoundary boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey);
//            boundary.setBoundaryColor(color);
		}
	}

    @Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);
		//TODO Fix this
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
//			PopAction<G1> tmpLPA = actionM.get(aMI);
//			tmpLPA.setChosenItems(aItemC, aMI);
//
//			// If all items have the same custom color and match one of the
//			// predefined colors then update the corresponding menu item.
//			if (tmpLPA instanceof FixedImageColorAction<?>)
//			{
//				boolean isSelected = isSameCustomColor == true;
//				isSelected &= ((FixedImageColorAction<?>) tmpLPA).getColor().equals(initColor) == true;
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
	private JMenuItem formMenuItem(PopAction<G1> aAction, String aTitle)
	{
		JMenuItem retMI = new JMenuItem(aAction);
		retMI.setText(aTitle);

		actionM.put(retMI, aAction);

		return retMI;
	}

//    public void actionPerformed(ActionEvent e)
//    {
//        for (ImageKeyInterface imageKey : imageKeys)
//        {
//            PerspectiveImageBoundary boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey);
//            boundary.setBoundaryColor(color);
//        }
//
//        this.imagePopupMenu.updateMenuItems();
//    }
}