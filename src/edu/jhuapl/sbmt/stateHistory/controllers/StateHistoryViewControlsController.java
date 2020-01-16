package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;

import glum.item.ItemEventType;

public class StateHistoryViewControlsController implements ItemListener
{
	public boolean earthEnabled = true;
	private StateHistoryViewControlsPanel view;
	private StateHistoryModel historyModel;
	private StateHistoryCollection runs;
	private Renderer renderer;

	public StateHistoryViewControlsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		this.renderer = renderer;
		this.runs = historyModel.getRuns();
		this.view = new StateHistoryViewControlsPanel();
		initializeViewControlPanel();
	}

	private void initializeViewControlPanel()
    {
        String[] distanceChoices = {"Distance to Center", "Distance to Surface"};
        DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);
        DefaultComboBoxModel<RendererLookDirection> comboModelView = new DefaultComboBoxModel<RendererLookDirection>(RendererLookDirection.values());
        view.getDistanceOptions().setModel(comboModelDistance);
        view.getViewOptions().setModel(comboModelView);
        view.getShowEarthPointer().addItemListener(this);
        view.getShowSunPointer().addItemListener(this);
        view.getShowSpacecraftMarker().addItemListener(this);
        view.getShowSpacecraft().addItemListener(this);
        view.getShowLighting().addItemListener(this);

        runs.addPropertyChangeListener(evt -> {

			if (!evt.getPropertyName().equals("POSITION_CHANGED")) return;

			updateLookDirection();
			if ((renderer.getLighting() == LightingType.FIXEDLIGHT && runs.getCurrentRun() != null) == false) return;

			renderer.setFixedLightDirection(runs.getCurrentRun().getSunPosition());
		});

        runs.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;
			runs.setTimeFraction(runs.getCurrentRun(), 0.0);
			updateLookDirection();
		});

        view.getViewOptions().addActionListener(e -> { updateLookDirection(); });


        view.getBtnResetCameraTo().addActionListener(e -> {
            if( runs.getCurrentRun() == null) return;
            renderer.setCameraFocalPoint(new double[] {0,0,0});
        });

        view.getDistanceOptions().addActionListener(e ->
        {
	        String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
	        runs.setDistanceText(selectedItem);
        });

        view.getEarthSlider().addChangeListener(e ->
        {
        	JSlider slider = (JSlider) e.getSource();
        	runs.setEarthDirectionMarkerSize(slider.getValue());
        });

        view.getSunSlider().addChangeListener(e ->
        {
          	JSlider slider = (JSlider) e.getSource();
           	runs.setSunDirectionMarkerSize(slider.getValue());
        });

        view.getSpacecraftSlider().addChangeListener(e ->
        {
        	JSlider slider = (JSlider) e.getSource();
        	runs.setSpacecraftDirectionMarkerSize(slider.getValue());
        });

        view.getSetViewAngle().addActionListener(e ->
        {
            if(e.getSource() != view.getSetViewAngle()) return;

            if(!(Double.parseDouble(view.getViewInputAngle().getText())>120.0 || Double.parseDouble(view.getViewInputAngle().getText())<1.0)){
                renderer.setCameraViewAngle(Double.parseDouble(view.getViewInputAngle().getText()));
            }else if(Double.parseDouble(view.getViewInputAngle().getText())>120){
                view.getViewInputAngle().setText("120.0");
                renderer.setCameraViewAngle(120.0);
            }else{
                view.getViewInputAngle().setText("1.0");
                renderer.setCameraViewAngle(1.0);
            }

        });

        view.getViewOptions().setSelectedIndex(0);
        view.updateUI();
    }

	private void updateLookDirection()
	{
		RendererLookDirection selectedItem = (RendererLookDirection)view.getViewOptions().getSelectedItem();
		double[] upVector = {0,1,0};
        StateHistory currentRun = runs.getCurrentRun();

        if (currentRun != null) 	// can't do any view things if we don't have a trajectory / time history
        {
			Vector3D targOrig = new Vector3D(renderer.getCameraFocalPoint());

			if (selectedItem == RendererLookDirection.FREE_VIEW)
			{
				((RenderPanel)renderer.getRenderWindowPanel()).setZoomOnly(false, Vector3D.ZERO, targOrig);
				return;
			}
			else
			{
				Vector3D targAxis = new Vector3D(runs.updateLookDirection(selectedItem, historyModel.getScalingFactor()));

	            double[] lookFromDirection = runs.updateLookDirection(selectedItem, historyModel.getScalingFactor());
	            renderer.setCameraFocalPoint(new double[] {0, 0, 0});
	            renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());

				((RenderPanel)renderer.getRenderWindowPanel()).setZoomOnly(true, targAxis, targOrig);

				//toggle the ability to show the spacecraft depending on what mode we're in
				boolean scSelected = (selectedItem == RendererLookDirection.SPACECRAFT);
				view.getShowSpacecraft().setEnabled(!scSelected);
		        view.getDistanceOptions().setEnabled(!scSelected);
			}
        }
	}

    @Override
    public void itemStateChanged(ItemEvent e) throws NullPointerException
    {
        Object source = e.getItemSelectable();
        StateHistory currentRun = runs.getCurrentRun();
        JCheckBox showEarthMarker = view.getShowEarthPointer();
        JCheckBox showSunMarker = view.getShowSunPointer();
        JCheckBox showSpacecraft = view.getShowSpacecraft();
        JCheckBox showSpacecraftMarker = view.getShowSpacecraftMarker();
        JComboBox distanceOptions = view.getDistanceOptions();
        JCheckBox showLighting = view.getShowLighting();
        JTextField viewAngleInput = view.getViewInputAngle();
        JSlider earthSlider = view.getEarthSlider();
        JSlider sunSlider = view.getSunSlider();
        JSlider spacecraftSlider = view.getSpacecraftSlider();
        JLabel earthText = view.getEarthText();
        JLabel sunText = view.getSunText();
        JLabel spacecraftText = view.getSpacecraftText();
        //
        // handles changes in options to show/hide different parts of the model. Ex. pointers, lighting, trajectory
        //
        boolean selected = (e.getStateChange() == ItemEvent.SELECTED);

        if(source == showEarthMarker){
        	runs.setEarthDirectionMarkerVisibility(selected);
            earthSlider.setEnabled(selected);
            earthText.setEnabled(selected);
        } else if(source == showSunMarker){
        	runs.setSunDirectionMarkerVisibility(selected);
            sunSlider.setEnabled(selected);
            sunText.setEnabled(selected);
        } else if(source == showSpacecraftMarker){
        	runs.setSpacecraftDirectionMarkerVisibility(selected);
            spacecraftSlider.setEnabled(selected);
            spacecraftText.setEnabled(selected);
        } else if(source == showSpacecraft){
            distanceOptions.setEnabled(selected);
            if (selected)
            	historyModel.setDistanceText(distanceOptions.getSelectedItem().toString());
            runs.setSpacecraftLabelVisibility(selected);
            runs.setSpacecraftVisibility(selected);
        } else if(source == showLighting){
        	if (selected)
        	{
        		renderer.setFixedLightDirection(currentRun.getSunPosition());
        		renderer.setLighting(LightingType.FIXEDLIGHT);
        	}
        	else
        	{
        		renderer.setLighting(LightingType.LIGHT_KIT);
        	}
        }
    }

	public StateHistoryViewControlsPanel getView()
	{
		return view;
	}

}
