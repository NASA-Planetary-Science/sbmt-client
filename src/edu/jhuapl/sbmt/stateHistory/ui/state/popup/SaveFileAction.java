package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryModelIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Save Tracks". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 */
class SaveFileAction extends PopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryCollection refManager;

	private StateHistoryRendererManager rendererManager;

	/**
	 *
	 */
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aParent
	 */
	public SaveFileAction(StateHistoryRendererManager rendererManager, Component aParent)
	{
		this.rendererManager = rendererManager;
		refManager = rendererManager.getHistoryCollection();
		refParent = aParent;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		Component rootComp = JOptionPane.getFrameForComponent(refParent);
		Set<StateHistory> workS = rendererManager.getSelectedItems();

		// Prompt the user for the save folder
		String title = "Specify the folder to save " + workS.size() + " state history files";
		File targPath = DirectoryChooser.showOpenDialog(rootComp, title);
		if (targPath == null)
			return;

		// Save all of the selected items into the target folder
		StateHistory history = null;
		int passCnt = 0;
		try
		{
			for (StateHistory stateHistory : workS)
			{
				history = stateHistory;
				StateHistoryModelIOHelper.saveIntervalToFile(refManager.getBodyName(), stateHistory, new File(targPath, stateHistory.getMetadata().getStateHistoryName()).getAbsolutePath());
				passCnt++;
			}
		}
		catch (Exception aExp)
		{
			String errMsg = "Failed to save " + (workS.size() - passCnt) + "files. Failed on state history file: ";
			errMsg += history.getMetadata().getStateHistoryName();
			JOptionPane.showMessageDialog(rootComp, errMsg, "Error Saving State History Files", JOptionPane.ERROR_MESSAGE);
			aExp.printStackTrace();
		}
	}

	/**
	 *
	 */
	@Override
	public void setChosenItems(Collection<StateHistory> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Update our associated MenuItem
		String displayStr = MessageUtil.toPluralForm("Save File", aItemC);
		aAssocMI.setText(displayStr);
	}

}
