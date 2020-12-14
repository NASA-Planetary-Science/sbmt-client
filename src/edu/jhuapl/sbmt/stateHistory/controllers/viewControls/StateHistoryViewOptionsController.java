package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.StateHistoryViewOptionsModel;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
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
	public StateHistoryViewOptionsController(StateHistoryRendererManager rendererManager)
	{
		initializeViewControlPanel(rendererManager);
		view.getShowLightingPanel().getShowLighting().addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
				if (selected)
				{
					rendererManager.getRenderer().setFixedLightDirection(rendererManager.getRuns().getCurrentRun().getSunPosition());
					rendererManager.getRenderer().setLighting(LightingType.FIXEDLIGHT);
				}
				else
				{
					rendererManager.getRenderer().setLighting(LightingType.LIGHT_KIT);
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
	private void initializeViewControlPanel(StateHistoryRendererManager rendererManager)
	{
		DefaultComboBoxModel<RendererLookDirection> comboModelView = new DefaultComboBoxModel<RendererLookDirection>(
				RendererLookDirection.values());
		view = new StateHistoryViewOptionsPanel();
		StateHistoryViewOptionsModel model = new StateHistoryViewOptionsModel();
		StateHistoryCollection runs = rendererManager.getRuns();
		Renderer renderer = rendererManager.getRenderer();
//		view.getViewInputAngle().setText("30.0");

		//set the model for the view options, and set the action listener
		view.getViewOptions().setModel(comboModelView);

		view.getViewOptions().addActionListener(e ->
		{
			rendererManager.updateLookDirection((RendererLookDirection) view.getViewOptions().getSelectedItem());
//			updateLookDirection(rendererManager, model);
		});

//		//set the action listener for the reset to nadir button
//        view.getBtnResetCameraTo().addActionListener(e -> {
//            if( runs.getCurrentRun() == null) return;
//            renderer.setCameraFocalPoint(new double[] {0,0,0});
//        });

//        //set the action listener for the set view angle button
//        view.getSetViewAngle().addActionListener(e ->
//		{
//			if (e.getSource() != view.getSetViewAngle())
//				return;
//			double inputAngle = Double.parseDouble(view.getViewInputAngle().getText());
//			model.setViewInputAngleForStateHistory(inputAngle, runs.getCurrentRun());
//			view.getViewInputAngle().setText("" + model.getViewInputAngleForStateHistory(runs.getCurrentRun()));
//			renderer.setCameraViewAngle(model.getViewInputAngleForStateHistory(runs.getCurrentRun()));
//		});

        //Adds a property change listener for the StateHistoryCollection object to respond to POSITION_CHANGED events
        //This updates the lighting as well as the look direction
        rendererManager.addPropertyChangeListener(evt -> {

			if (!evt.getPropertyName().equals("POSITION_CHANGED")) return;
			RendererLookDirection selectedItem = (RendererLookDirection) view.getViewOptions().getSelectedItem();
			model.setRendererLookDirectionForStateHistory(selectedItem, runs.getCurrentRun());

//			updateLookDirection(rendererManager, model);
//			if ((renderer.getLighting() == LightingType.FIXEDLIGHT && runs.getCurrentRun() != null) == false) return;
//			renderer.setFixedLightDirection(runs.getCurrentRun().getSunPosition());
		});

        //on a change in selection in the table, reset the time fraction and update the state history's look angle
        //and look direction
        rendererManager.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;
			rendererManager.setTimeFraction(0.0, runs.getCurrentRun());
			RendererLookDirection lookDir = model.getRendererLookDirectionForStateHistory(runs.getCurrentRun());
			if (lookDir == null) lookDir = RendererLookDirection.FREE_VIEW;
			Double inputAngle = model.getViewInputAngleForStateHistory(runs.getCurrentRun());
			view.getViewOptions().setSelectedItem(lookDir);

//			if (inputAngle != null)
//				view.getViewInputAngle().setText("" + inputAngle);
		});

		view.getViewOptions().setSelectedItem(RendererLookDirection.FREE_VIEW);
		rendererManager.setRendererLookDirection(RendererLookDirection.FREE_VIEW);
//		view.getViewOptions().setSelectedIndex(0);
//		view.setCheckboxItemListener(e ->
//		{
//			JCheckBox fovCheckbox = (JCheckBox)e.getSource();
//			if (fovCheckbox.isSelected())
//			{
//				runs.addSelectedFov(fovCheckbox.getText());
//			}
//			else
//			{
//				runs.removeSelectedFov(fovCheckbox.getText());
//			}
//		});
	}



	/**
	 * @return the view
	 */
	public StateHistoryViewOptionsPanel getView()
	{
		return view;
	}
}
