package edu.jhuapl.sbmt.gui.lidar.popup;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.gui.lidar.color.ConstColorProvider;
import edu.jhuapl.sbmt.model.lidar.LidarManager;

/**
 * Object which the provides logic used to control when the lidar popup menu is
 * displayed.
 *
 * @author lopeznr1
 */
public class LidarTablePopupListener<G1> extends MouseAdapter
{
	// Ref vars
	private final LidarManager<G1> refManager;
	private final PopupMenu refPopupMenu;
	private final JTable refTable;

	public LidarTablePopupListener(LidarManager<G1> aManager, LidarPopupMenu<?> aPopupMenu, JTable aTable)
	{
		refManager = aManager;
		refPopupMenu = aPopupMenu;
		refTable = aTable;
	}

	@Override
	public void mouseClicked(MouseEvent aEvent)
	{
		// Handle the Color customization
		int row = refTable.rowAtPoint(aEvent.getPoint());
		int col = refTable.columnAtPoint(aEvent.getPoint());
		if (aEvent.getClickCount() == 2 && row >= 0 && col == 1)
		{
			Set<G1> pickS = refManager.getSelectedItems();
			if (pickS.size() == 0)
				return;

			G1 tmpItem = pickS.iterator().next();
			Color oldColor = refManager.getColorProviderTarget(tmpItem).getBaseColor();
			Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(refTable), oldColor);
			if (tmpColor == null)
				return;

			ConstColorProvider tmpCP = new ConstColorProvider(tmpColor);
			refManager.installCustomColorProviders(pickS, tmpCP, tmpCP);
			return;
		}

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
