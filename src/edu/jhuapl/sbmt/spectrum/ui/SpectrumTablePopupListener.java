package edu.jhuapl.sbmt.spectrum.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;

import glum.item.ItemManager;

/**
 * Object wh

import glum.item.ItemManager;ich the provides logic used to control when the lidar popup menu is
 * displayed.
 *
 * @author lopeznr1
 */
public class SpectrumTablePopupListener<G1> extends MouseAdapter
{
	// Ref vars
	private final ItemManager<G1> refManager;
	private final PopupMenu refPopupMenu;
	private final JTable refTable;
	private SpectrumBoundaryCollection boundaries;

	public SpectrumTablePopupListener(ItemManager<G1> aManager, SpectrumBoundaryCollection boundaries, SpectrumPopupMenu aPopupMenu, JTable aTable)
	{
		refManager = aManager;
		this.boundaries = boundaries;
		refPopupMenu = aPopupMenu;
		refTable = aTable;
	}

	@Override
	public void mouseClicked(MouseEvent aEvent)
	{
		// Handle the Color customization
		int row = refTable.rowAtPoint(aEvent.getPoint());
		int col = refTable.columnAtPoint(aEvent.getPoint());
//		if (aEvent.getClickCount() == 2 && row >= 0 && col == 1)
//		{
//			Set<G1> pickS = refManager.getSelectedItems();
//			if (pickS.size() == 0)
//				return;
//
//			G1 tmpItem = pickS.iterator().next();
//			Color oldColor = refManager.getColorProviderTarget(tmpItem).getBaseColor();
//			Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(refTable), oldColor);
//			if (tmpColor == null)
//				return;
//
//			ConstColorProvider tmpCP = new ConstColorProvider(tmpColor);
//			refManager.installCustomColorProviders(pickS, tmpCP, tmpCP);
//			return;
//		}

		maybeShowPopup(aEvent);
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		maybeShowPopup(aEvent);
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		maybeShowPopup(aEvent);
	}

	/**
	 * Helper method to handle the showing of the table popup menu.
	 */
	private void maybeShowPopup(MouseEvent aEvent)
	{
		// Bail if no provider popup menu
		if (refPopupMenu == null)
			return;
		// Bail if this is not a valid popup action
		if (aEvent.isPopupTrigger() == false)
			return;
		// TODO: Is this necessary?
		// Force the menu to be hidden by default
		refPopupMenu.setVisible(false);
		refPopupMenu.showPopup(aEvent, null, 0, null);
	}

}
