package edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls;

import javax.swing.DefaultComboBoxModel;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

import glum.item.ItemEventType;

public class StateHistoryViewOptionsController
{
	private StateHistoryViewOptionsPanel view;
	private StateHistoryViewOptionsModel model;
	private Renderer renderer;
	private StateHistoryCollection runs;
	private StateHistoryModel historyModel;

	public StateHistoryViewOptionsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		this.runs = historyModel.getRuns();
		this.renderer = renderer;
		initializeViewControlPanel();
	}

	/**
	 *
	 */
	private void initializeViewControlPanel()
	{
		DefaultComboBoxModel<RendererLookDirection> comboModelView = new DefaultComboBoxModel<RendererLookDirection>(
				RendererLookDirection.values());
		view = new StateHistoryViewOptionsPanel();
		model = new StateHistoryViewOptionsModel();

		view.getViewInputAngle().setText("30.0");

		//set the model for the view options, and set the action listener
		view.getViewOptions().setModel(comboModelView);

		view.getViewOptions().addActionListener(e ->
		{
			updateLookDirection();
		});

		//set the action listener for the reset to nadir button
        view.getBtnResetCameraTo().addActionListener(e -> {
            if( runs.getCurrentRun() == null) return;
            renderer.setCameraFocalPoint(new double[] {0,0,0});
        });

        //set the action listener for the set view angle button
        view.getSetViewAngle().addActionListener(e ->
		{
			if (e.getSource() != view.getSetViewAngle())
				return;
			double inputAngle = Double.parseDouble(view.getViewInputAngle().getText());
			model.setViewInputAngleForStateHistory(inputAngle, runs.getCurrentRun());
			view.getViewInputAngle().setText("" + model.getViewInputAngleForStateHistory(runs.getCurrentRun()));
			renderer.setCameraViewAngle(model.getViewInputAngleForStateHistory(runs.getCurrentRun()));
		});

        //Adds a property change listener for the StateHistoryCollection object to respond to POSITION_CHANGED events
        //This updates the lighting as well as the look direction
        runs.addPropertyChangeListener(evt -> {

			if (!evt.getPropertyName().equals("POSITION_CHANGED")) return;
			updateLookDirection();
			if ((renderer.getLighting() == LightingType.FIXEDLIGHT && runs.getCurrentRun() != null) == false) return;
			renderer.setFixedLightDirection(runs.getCurrentRun().getSunPosition());
		});

        //on a change in selection in the table, reset the time fraction and update the state history's look angle
        //and look direction
        runs.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;
			runs.setTimeFraction(0.0);
			RendererLookDirection lookDir = model.getRendererLookDirectionForStateHistory(runs.getCurrentRun());
			Double inputAngle = model.getViewInputAngleForStateHistory(runs.getCurrentRun());
			view.getViewOptions().setSelectedItem(lookDir);
			view.getViewInputAngle().setText("" + inputAngle);
		});

		view.getViewOptions().setSelectedIndex(0);
	}

	/**
	 * Updates the look direction based on the selected option in user interface
	 */
	private void updateLookDirection()
	{
		RendererLookDirection selectedItem = (RendererLookDirection) view.getViewOptions().getSelectedItem();
		model.setRendererLookDirectionForStateHistory(selectedItem, runs.getCurrentRun());
		double[] upVector = { 0, 0, 1 };
		StateHistory currentRun = runs.getCurrentRun();

		if (currentRun == null) return; // can't do any view things if we don't have a trajectory / time history

		Vector3D targOrig = new Vector3D(renderer.getCameraFocalPoint());
		Vector3D targAxis = new Vector3D(runs.updateLookDirection(selectedItem, historyModel.getScalingFactor()));
		renderer.setCameraFocalPoint(new double[]{ 0, 0, 0 });
		double[] lookFromDirection;
		if (selectedItem == RendererLookDirection.FREE_VIEW)
		{
			lookFromDirection = renderer.getCameraPosition();
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(),
					renderer.getCamera().getUpUnit().toArray(), renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(false, Vector3D.ZERO, targOrig);
		}
		else
		{
			lookFromDirection = runs.updateLookDirection(selectedItem, historyModel.getScalingFactor());
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(), upVector,
					renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(true, targAxis, targOrig);
		}
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public StateHistoryViewOptionsPanel getView()
	{
		return view;
	}

}
