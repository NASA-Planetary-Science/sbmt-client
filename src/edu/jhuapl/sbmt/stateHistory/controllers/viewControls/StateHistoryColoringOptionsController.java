package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryColorConfigPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryColoringOptionsPanel;

import glum.item.ItemEventType;

/**
 * Controller that governs the coloring views in the State History tab
 * @author steelrj1
 *
 */
public class StateHistoryColoringOptionsController implements ActionListener
{
	/**
	 * The view governed by this controller
	 */
	private StateHistoryColoringOptionsPanel view;

	/**
	 * The collection of State Histories loaded into the tool
	 */
	private StateHistoryCollection runs;

	private ColoringDataManager coloringDataManager;

	private StateHistoryColorConfigPanel colorConfigPanel;

	private StateHistoryRendererManager rendererManager;

	private JPanel horizPanel = new JPanel();

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryColoringOptionsController(StateHistoryRendererManager rendererManager, ColoringDataManager coloringDataManager)
	{
		this.runs = rendererManager.getRuns();
		this.rendererManager = rendererManager;
		this.coloringDataManager = coloringDataManager;
		this.colorConfigPanel = new StateHistoryColorConfigPanel(this, rendererManager);

        rendererManager.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsChanged) return;
			if (rendererManager.getRuns().getCurrentRun() == null) return;
			renderView();
		});

		initializeViewControlPanel(runs);
		renderView();
	}

	public void actionPerformed(ActionEvent e)
	{
		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
		rendererManager.installGroupColorProviders(srcGCP);
	};

	/**
	 *
	 */
	private void initializeViewControlPanel(StateHistoryCollection runs)
	{
		//create the view
		view = new StateHistoryColoringOptionsPanel(coloringDataManager);

		view.setPlateColoringsItemListener(e ->
		{
			JComboBoxWithItemState<String> combo = (JComboBoxWithItemState<String>)e.getSource();
			rendererManager.setFootprintPlateColoring(combo.getModel().getSelectedItem().toString());
		});
	}

	private void renderView()
	{
		horizPanel.removeAll();
		horizPanel.setLayout(new BoxLayout(horizPanel, BoxLayout.X_AXIS));
		horizPanel.add(view);
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public JPanel getView()
	{
		return horizPanel;
	}

	public void setEnabled(boolean enabled)
	{
		this.view.setEnabled(enabled);
		this.colorConfigPanel.setEnabled(enabled);
	}
}
