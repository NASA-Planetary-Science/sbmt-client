package edu.jhuapl.sbmt.image2.ui.table.popup.rendering;

import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.pipeline.active.rendering.PerspectiveImageSimulateLightingPipeline;

import glum.gui.action.PopAction;

public class SimulateLightingAction<G1 extends PerspectiveImage> extends PopAction<G1>
{
	/**
	 *
	 */
	private final PerspectiveImageCollection aManager;

	private final Renderer renderer;

	/**
	 * @param imagePopupMenu
	 */
	public SimulateLightingAction(PerspectiveImageCollection aManager, Renderer renderer)
	{
		this.aManager = aManager;
		this.renderer = renderer;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		//TODO fix this
		PerspectiveImage aItem = aItemL.get(0);
		try
		{
			PerspectiveImageSimulateLightingPipeline.of(aItem, renderer, !aItem.isSimulateLighting());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (PerspectiveImage tempImage : aManager.getAllItems())
		{
			tempImage.setSimulateLighting(false);
		}
		aItem.setSimulateLighting(!aItem.isSimulateLighting());
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// If any items are not visible then set checkbox to unselected
		// in order to allow all chosen items to be toggled on
		boolean isSelected = true;
		for (G1 aItem : aItemC)
			isSelected &= aItem.isSimulateLighting() == true;
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);
	}
}