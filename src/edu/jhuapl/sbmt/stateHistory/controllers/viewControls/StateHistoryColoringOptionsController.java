package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.StateHistoryColorOptionsAttributeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.ui.color.ColorConfigPanel;
import edu.jhuapl.sbmt.stateHistory.ui.color.ColorMode;
import edu.jhuapl.sbmt.stateHistory.ui.color.GroupColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls.StateHistoryColoringOptionsPanel;

import glum.item.ItemEventType;

public class StateHistoryColoringOptionsController implements ActionListener
{
	private StateHistoryColoringOptionsPanel view;
	private StateHistoryColorOptionsAttributeModel model;
	private StateHistoryCollection runs;
	private TrajectoryActor trajectoryActor;
	private StateHistoryModel historyModel;
	private StateHistoryColoringFunctions coloringFunctionChoice;
	private Colormap colormapChoice;
	private ColorConfigPanel<?> colorConfigPanel;

	public StateHistoryColoringOptionsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		runs = historyModel.getRuns();

		colorConfigPanel = new ColorConfigPanel<>(this, runs, renderer);
		colorConfigPanel.setActiveMode(ColorMode.Simple);

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
		colormapChoice = (Colormap) view.getColormapComboBox().getSelectedItem();
		colormapChoice.setRangeMax(12);
		colormapChoice.setRangeMin(0);
		coloringFunctionChoice = ((StateHistoryColoringFunctions) view
				.getColorFunctionComboBox().getSelectedItem());

		view.getColorFunctionComboBox().addActionListener(e ->
		{
			coloringFunctionChoice = ((StateHistoryColoringFunctions) view
					.getColorFunctionComboBox().getSelectedItem());
			model.setColoringFunctionForStateHistory(coloringFunctionChoice, runs.getCurrentRun());
			view.getColormapComboBox().setEnabled(!(coloringFunctionChoice == StateHistoryColoringFunctions.PER_TABLE));
			trajectoryActor.setColoringFunction(coloringFunctionChoice.getColoringFunction(), colormapChoice);
			runs.refreshColoring(runs.getCurrentRun());
		});

		view.getColormapComboBox().addActionListener(e ->
		{
			colormapChoice = (Colormap) view.getColormapComboBox().getSelectedItem();
			model.setColormapForStateHistory(colormapChoice, runs.getCurrentRun());
			trajectoryActor.setColoringFunction(coloringFunctionChoice.getColoringFunction(), colormapChoice);
			runs.refreshColoring(runs.getCurrentRun());
		});

		historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
			@Override
			public void historySegmentCreated(StateHistory historySegment)
			{
				super.historySegmentCreated(historySegment);
			}
		});

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

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source == colorConfigPanel)
		{
			GroupColorProvider srcGCP = colorConfigPanel.getSourceGroupColorProvider();
			System.out.println("StateHistoryColoringOptionsController: actionPerformed: provider is " + srcGCP);

//			runs.installGroupColorProviders(srcGCP);
			trajectoryActor.setColoringProvider(srcGCP);
//			trajectory

			model.setColoringProviderForStateHistory(srcGCP, runs.getCurrentRun());
			runs.refreshColoring(runs.getCurrentRun());
		}
	}
}
