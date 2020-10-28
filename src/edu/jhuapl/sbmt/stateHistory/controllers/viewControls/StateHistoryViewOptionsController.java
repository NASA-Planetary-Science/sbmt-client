package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.StateHistoryViewOptionsModel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryViewOptionsPanel;

import glum.item.ItemEventType;

/**
 * @author steelrj1
 *
 */
public class StateHistoryViewOptionsController
{
	/**
	 * The view governed by this controller
	 */
	private StateHistoryViewOptionsPanel view;

	/**
	 * Constructor.  Sets properties and initializes the view control panel
	 * @param runs			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 */
	public StateHistoryViewOptionsController(StateHistoryCollection runs, Renderer renderer)
	{
		initializeViewControlPanel(runs, renderer);
		view.getShowLightingPanel().getShowLighting().addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
				if (selected)
				{
					renderer.setFixedLightDirection(runs.getCurrentRun().getSunPosition());
					renderer.setLighting(LightingType.FIXEDLIGHT);
				}
				else
				{
					renderer.setLighting(LightingType.LIGHT_KIT);
				}
			}
		});
	}

	/**
	 * Initializes the view control panel, sets action listeners, etc
	 *
 	 * @param runs			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 */
	private void initializeViewControlPanel(StateHistoryCollection runs, Renderer renderer)
	{
		DefaultComboBoxModel<RendererLookDirection> comboModelView = new DefaultComboBoxModel<RendererLookDirection>(
				RendererLookDirection.values());
		view = new StateHistoryViewOptionsPanel();
		StateHistoryViewOptionsModel model = new StateHistoryViewOptionsModel();

		view.getViewInputAngle().setText("30.0");

		//set the model for the view options, and set the action listener
		view.getViewOptions().setModel(comboModelView);

		view.getViewOptions().addActionListener(e ->
		{
			updateLookDirection(runs, renderer, model);
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
			updateLookDirection(runs, renderer, model);
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

			if (inputAngle != null)
				view.getViewInputAngle().setText("" + inputAngle);
		});

		view.getViewOptions().setSelectedIndex(0);
		view.setCheckboxItemListener(e ->
		{
			JCheckBox fovCheckbox = (JCheckBox)e.getSource();
			if (fovCheckbox.isSelected())
			{
				runs.addSelectedFov(fovCheckbox.getText());
			}
			else
			{
				runs.removeSelectedFov(fovCheckbox.getText());
			}
		});
	}

	/**
	 * Updates the look direction based on the selected option in user interface
	 *
	 * @param runs			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 * @param model			The model that contains information about the view options
	 */
	private void updateLookDirection(StateHistoryCollection runs, Renderer renderer, StateHistoryViewOptionsModel model)
	{
		RendererLookDirection selectedItem = (RendererLookDirection) view.getViewOptions().getSelectedItem();
		model.setRendererLookDirectionForStateHistory(selectedItem, runs.getCurrentRun());
		double[] upVector = { 0, 0, 1 };
		StateHistory currentRun = runs.getCurrentRun();

		if (currentRun == null) return; // can't do any view things if we don't have a trajectory / time history

		Vector3D targOrig = new Vector3D(renderer.getCameraFocalPoint());
		Vector3D targAxis = new Vector3D(runs.updateLookDirection(selectedItem));
		renderer.setCameraFocalPoint(new double[]{ 0, 0, 0 });
		double[] lookFromDirection;
		if (selectedItem == RendererLookDirection.FREE_VIEW)
		{
			lookFromDirection = renderer.getCamera().getPosition().toArray();
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(),
					renderer.getCamera().getUpUnit().toArray(), renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(false, Vector3D.ZERO, targOrig);
		}
		else
		{
			lookFromDirection = runs.updateLookDirection(selectedItem);
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(), upVector,
					renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(true, targAxis, targOrig);
		}
		renderer.getRenderWindowPanel().resetCameraClippingRange();
	}

	/**
	 * @return the view
	 */
	public StateHistoryViewOptionsPanel getView()
	{
		return view;
	}
}
