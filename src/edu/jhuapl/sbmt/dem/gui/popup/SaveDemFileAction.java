package edu.jhuapl.sbmt.dem.gui.popup;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.dem.Dem;
import edu.jhuapl.sbmt.dem.DemManager;
import edu.jhuapl.sbmt.dem.io.DemExportUtil;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Save DEMs"
 *
 * @author lopeznr1
 */
public class SaveDemFileAction extends PopAction<Dem>
{
	// Ref vars
	private final DemManager refManager;
	private final Component refParent;

	/** Standard Constructor */
	public SaveDemFileAction(DemManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<Dem> aItemL)
	{
		DemExportUtil.saveDemsToFolder(refManager, aItemL, refParent);
	}

	@Override
	public void setChosenItems(Collection<Dem> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		boolean isEnabled = aItemC.size() > 0;
		aAssocMI.setEnabled(isEnabled);

		// Determine the display string
		String displayStr = "Save DTM";
		displayStr = MessageUtil.toPluralForm(displayStr, aItemC);

		// Update the text of the associated MenuItem
		aAssocMI.setText(displayStr);
	}

}
