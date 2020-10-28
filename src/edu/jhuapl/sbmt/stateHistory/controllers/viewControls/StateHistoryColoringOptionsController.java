package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.StateHistoryColorOptionsAttributeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryColorConfigPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryColoringOptionsPanel;

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
	 * The <pre>StateHistoryColorOptionsAttributeModel</pre> that holds information related to the coloring of the state history segments
	 */
	private StateHistoryColorOptionsAttributeModel model;

	/**
	 * The collection of State Histories loaded into the tool
	 */
	private StateHistoryCollection runs;

	/**
	 * The active <pre>TrajectoryActor</pre> being colored
	 */
	private TrajectoryActor trajectoryActor;

	/**
	 * The current coloring function choice from the picker
	 */
	private StateHistoryColoringFunctions coloringFunctionChoice;

	/**
	 * The current colormap choice from the picker
	 */
//	private Colormap colormapChoice;

	private ColoringDataManager coloringDataManager;

	private StateHistoryColorConfigPanel colorConfigPanel;

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryColoringOptionsController(StateHistoryCollection runs, Renderer renderer, ColoringDataManager coloringDataManager)
	{
		this.runs = runs;
		this.coloringDataManager = coloringDataManager;
		this.colorConfigPanel = new StateHistoryColorConfigPanel(this, runs, renderer);
		initializeViewControlPanel(runs);
	}

	public void actionPerformed(ActionEvent e)
	{
		GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
		runs.installGroupColorProviders(srcGCP);
	};

	/**
	 *
	 */
	private void initializeViewControlPanel(StateHistoryCollection runs)
	{
		//create the attributes model and view objects
		model = new StateHistoryColorOptionsAttributeModel();
		view = new StateHistoryColoringOptionsPanel(coloringDataManager);

		runs.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				trajectoryActor = runs.getTrajectoryActorForStateHistory(runs.getCurrentRun());
			}
		});

//		//upon a change in selection in the table, update the UI widgets to reflect the current colormap and
//		//coloring function for this state history
//		runs.addListener((aSource, aEventType) ->
//		{
//			if (aEventType != ItemEventType.ItemsSelected) return;
//
//			StateHistoryColoringFunctions stateHistoryColoringFunction = model.getColoringFunctionForStateHistory(runs.getCurrentRun());
////			if (stateHistoryColoringFunction != null)
////				view.getColorFunctionComboBox().setSelectedItem(stateHistoryColoringFunction);
////			Colormap stateHistoryColormap = model.getColormapForStateHistory(runs.getCurrentRun());
////			if (stateHistoryColormap != null)
////				view.getColormapComboBox().setSelectedItem(stateHistoryColormap);
//		});

		view.setPlateColoringsItemListener(e ->
		{
			JComboBoxWithItemState<String> combo = (JComboBoxWithItemState<String>)e.getSource();
			runs.setFootprintPlateColoring(combo.getModel().getSelectedItem().toString());

		});
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public JPanel getView()
	{
		JPanel horizPanel = new JPanel();
		horizPanel.setLayout(new BoxLayout(horizPanel, BoxLayout.X_AXIS));
		this.colorConfigPanel.setBorder(BorderFactory.createTitledBorder("Trajectory Coloring"));
		this.colorConfigPanel.setAlignmentY(0.5f);
		horizPanel.add(this.colorConfigPanel);
		horizPanel.add(view);

		return horizPanel;
	}

	public void setEnabled(boolean enabled)
	{
		this.view.setEnabled(enabled);
		this.colorConfigPanel.setEnabled(enabled);
	}
}
