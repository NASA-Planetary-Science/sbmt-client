package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.StateHistoryColorOptionsAttributeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryColoringOptionsPanel;

import glum.item.ItemEventType;

/**
 * Controller that governs the coloring views in the State History tab
 * @author steelrj1
 *
 */
public class StateHistoryColoringOptionsController //implements ActionListener
{
	/**
	 * The view governed by this controller
	 */
	private StateHistoryColoringOptionsPanel view;

	/**
	 * The <pre>StateHistoryColorOptionsAttributeModel</pre> that holds information related to the coloring of the state history segments
	 */
	private StateHistoryColorOptionsAttributeModel model;

//	/**
//	 * The collection of State Histories loaded into the tool
//	 */
//	private StateHistoryCollection runs;

	/**
	 * The active <pre>TrajectoryActor</pre> being colored
	 */
	private TrajectoryActor trajectoryActor;

//	/**
//	 *
//	 */
//	private StateHistoryModel historyModel;

	/**
	 * The current coloring function choice from the picker
	 */
	private StateHistoryColoringFunctions coloringFunctionChoice;

	/**
	 * The current colormap choice from the picker
	 */
	private Colormap colormapChoice;

	private ColoringDataManager coloringDataManager;

//	/**
//	 *
//	 */
//	private ColorConfigPanel<?> colorConfigPanel;

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryColoringOptionsController(StateHistoryCollection runs, Renderer renderer, ColoringDataManager coloringDataManager)
	{
//		this.historyModel = historyModel;
//		runs = historyModel.getRuns();

//		colorConfigPanel = new ColorConfigPanel<>(this, runs, renderer);
//		colorConfigPanel.setActiveMode(ColorMode.Simple);
//		this.runs = runs;
		this.coloringDataManager = coloringDataManager;
		initializeViewControlPanel(runs);
	}

	/**
	 *
	 */
	private void initializeViewControlPanel(StateHistoryCollection runs)
	{
		//create the attributes model and view objects
		model = new StateHistoryColorOptionsAttributeModel();
		view = new StateHistoryColoringOptionsPanel(coloringDataManager);
		colormapChoice = (Colormap) view.getColormapComboBox().getSelectedItem();
		colormapChoice.setRangeMax(12);
		colormapChoice.setRangeMin(0);
		coloringFunctionChoice = ((StateHistoryColoringFunctions) view
				.getColorFunctionComboBox().getSelectedItem());

		view.getColorFunctionComboBox().addActionListener(e ->
		{
			coloringFunctionChoice = ((StateHistoryColoringFunctions) view
					.getColorFunctionComboBox().getSelectedItem());
			colormapChoice.setRangeMax(12);
			colormapChoice.setRangeMin(0);
			model.setColoringFunctionForStateHistory(coloringFunctionChoice, runs.getCurrentRun());
			view.getColormapComboBox().setEnabled(!(coloringFunctionChoice == StateHistoryColoringFunctions.PER_TABLE));
			trajectoryActor.setColoringFunction(coloringFunctionChoice.getColoringFunction(), colormapChoice);
			runs.refreshColoring(runs.getCurrentRun());
		});

		view.getColormapComboBox().addActionListener(e ->
		{
			colormapChoice = (Colormap) view.getColormapComboBox().getSelectedItem();
			colormapChoice.setRangeMax(12);
			colormapChoice.setRangeMin(0);
			model.setColormapForStateHistory(colormapChoice, runs.getCurrentRun());
			trajectoryActor.setColoringFunction(coloringFunctionChoice.getColoringFunction(), colormapChoice);
			runs.refreshColoring(runs.getCurrentRun());
		});

//		historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
//		{
//			@Override
//			public void historySegmentCreated(StateHistory historySegment)
//			{
//				super.historySegmentCreated(historySegment);
//			}
//		});

		runs.addPropertyChangeListener(new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				trajectoryActor = runs.getTrajectoryActorForStateHistory(runs.getCurrentRun());
			}
		});

		//upon a change in selection in the table, update the UI widgets to reflect the current colormap and
		//coloring function for this state history
		runs.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;

			StateHistoryColoringFunctions stateHistoryColoringFunction = model.getColoringFunctionForStateHistory(runs.getCurrentRun());
			if (stateHistoryColoringFunction != null)
				view.getColorFunctionComboBox().setSelectedItem(stateHistoryColoringFunction);
			Colormap stateHistoryColormap = model.getColormapForStateHistory(runs.getCurrentRun());
			if (stateHistoryColormap != null)
				view.getColormapComboBox().setSelectedItem(stateHistoryColormap);
		});

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
		return view;
//		return colorConfigPanel;
	}

//	@Override
//	public void actionPerformed(ActionEvent e)
//	{
//		Object source = e.getSource();
//		if (source == colorConfigPanel)
//		{
//			GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
//			System.out.println("StateHistoryColoringOptionsController: actionPerformed: provider is " + srcGCP);
//
////			runs.installGroupColorProviders(srcGCP);
//			trajectoryActor.setColoringProvider(srcGCP);
////			trajectory
//
//			model.setColoringProviderForStateHistory(srcGCP, runs.getCurrentRun());
//			runs.refreshColoring(runs.getCurrentRun());
//		}
//	}
}
