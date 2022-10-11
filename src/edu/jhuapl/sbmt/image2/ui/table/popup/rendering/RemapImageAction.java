package edu.jhuapl.sbmt.image2.ui.table.popup.rendering;

import java.util.List;

import javax.swing.SwingUtilities;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class RemapImageAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection<G1> collection;

	/**
	 * @param imagePopupMenu
	 */
	public RemapImageAction(PerspectiveImageCollection<G1> collection)
	{
		this.collection = collection;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		SwingUtilities.invokeLater(() ->
		{
			for (G1 image : aItemL)
			{
				collection.setImageMapped(image, true, true);
			}
		});
	}

}
