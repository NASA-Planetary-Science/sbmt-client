package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryColorConfigPanel;

/**
 * Controller that governs the coloring views in the State History tab
 * @author steelrj1
 *
 */
public class StateHistoryColoringOptionsController implements ActionListener
{
	private StateHistoryColorConfigPanel colorConfigPanel;

	private StateHistoryRendererManager rendererManager;

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryColoringOptionsController(StateHistoryRendererManager rendererManager)
	{
		this.rendererManager = rendererManager;
		this.colorConfigPanel = new StateHistoryColorConfigPanel(this, rendererManager);
		colorConfigPanel.setBorder(BorderFactory.createTitledBorder("Trajectory Coloring"));
	}

	public void actionPerformed(ActionEvent e)
	{
		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
		rendererManager.installGroupColorProviders(srcGCP);
	};

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public JPanel getView()
	{
		return colorConfigPanel;
	}

	public void setEnabled(boolean enabled)
	{
		this.colorConfigPanel.setEnabled(enabled);
	}
}
