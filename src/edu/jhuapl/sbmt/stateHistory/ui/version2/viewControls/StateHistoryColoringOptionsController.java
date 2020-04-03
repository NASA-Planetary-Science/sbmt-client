package edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;

import glum.item.ItemEventType;

public class StateHistoryColoringOptionsController
{
	private StateHistoryColoringOptionsPanel view;
	private StateHistoryColorOptionsAttributeModel model;
	private StateHistoryCollection runs;
	private TrajectoryActor trajectoryActor;
	private StateHistoryModel historyModel;

	public StateHistoryColoringOptionsController(StateHistoryModel historyModel)
	{
		this.historyModel = historyModel;
		runs = historyModel.getRuns();

		initializeViewControlPanel();
	}

	/**
	 *
	 */
	private void initializeViewControlPanel()
	{
		//create the attributes model and view objects
		model = new StateHistoryColorOptionsAttributeModel();
		view = new StateHistoryColoringOptionsPanel();
//		TrajectoryActor trajectoryActor = runs.getTrajectoryActorForStateHistory(runs.getCurrentRun());
//		System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel: traj actor " + trajectoryActor);
		Colormap colormapChoice = (Colormap) view.getColormapComboBox().getSelectedItem();
		colormapChoice.setRangeMax(12);
		colormapChoice.setRangeMin(0);
		StateHistoryColoringFunctions coloringFunctionChoice = ((StateHistoryColoringFunctions) view
				.getColorFunctionComboBox().getSelectedItem());

		view.getColorFunctionComboBox().addActionListener(e ->
		{
//			System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel colorfunc : current run " + runs.getCurrentRun());
			model.setColoringFunctionForStateHistory(coloringFunctionChoice, runs.getCurrentRun());
			view.getColormapComboBox().setEnabled(!(coloringFunctionChoice == StateHistoryColoringFunctions.PER_TABLE));
//			System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel colorfunc : trajectory actor " + trajectoryActor);
//			System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel colorfunc : colormap choice " + colormapChoice);
//			System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel colorfunc : coloring function " + coloringFunctionChoice.getColoringFunction());
			trajectoryActor.setColoringFunction(coloringFunctionChoice.getColoringFunction(), colormapChoice);
			runs.refreshColoring(runs.getCurrentRun());
		});

		view.getColormapComboBox().addActionListener(e ->
		{
			model.setColormapForStateHistory(colormapChoice, runs.getCurrentRun());
			trajectoryActor.setColoringFunction(coloringFunctionChoice.getColoringFunction(), colormapChoice);
			runs.refreshColoring(runs.getCurrentRun());
		});

		historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
			@Override
			public void historySegmentCreated(StateHistory historySegment)
			{
//				System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel his segment created: current run " + runs.getCurrentRun());

				super.historySegmentCreated(historySegment);
			}
		});

		runs.addPropertyChangeListener(new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				trajectoryActor = runs.getTrajectoryActorForStateHistory(runs.getCurrentRun());
//				System.out.println("StateHistoryColoringOptionsController: initializeViewControlPanel: prop changed: traj actor " + trajectoryActor);
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
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public StateHistoryColoringOptionsPanel getView()
	{
		return view;
	}
}
