package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jfree.ui.FontChooserDialog;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;

import glum.item.ItemEventType;

/**
 * @author steelrj1
 *
 */
public class StateHistoryViewControlsController implements ItemListener
{
	public boolean earthEnabled = true;
	private StateHistoryViewControlsPanel view;
	private StateHistoryModel historyModel;
	private StateHistoryCollection runs;
	private Renderer renderer;

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryViewControlsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		this.renderer = renderer;
		this.runs = historyModel.getRuns();
		this.view = new StateHistoryViewControlsPanel();
		initializeViewControlPanel();
	}

	/**
	 *
	 */
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
			runs.setTimeFraction(0.0);
//			updateLookDirection();
		});

        view.getViewOptions().addActionListener(e -> { updateLookDirection(); });


        view.getBtnResetCameraTo().addActionListener(e -> {
            if( runs.getCurrentRun() == null) return;
            renderer.setCameraFocalPoint(new double[] {0,0,0});
        });

        view.getLabelCheckBox().addActionListener(e -> {
        	view.getDistanceOptions().setEnabled(view.getLabelCheckBox().isSelected());
        	view.getLabelFontButton().setEnabled(view.getLabelCheckBox().isSelected());
        	runs.setDistanceTextVisiblity(view.getLabelCheckBox().isSelected());
        });

        view.getDistanceOptions().addActionListener(e ->
        {
	        String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
	        runs.setDistanceText(selectedItem);
        });

        view.getLabelFontButton().addActionListener(e -> {
        	JFrame fontFrame = new JFrame();
        	FontChooserDialog fontDialog = new FontChooserDialog(fontFrame, "Choose Font", true, view.getLabelFont());
        	fontDialog.setSize(400, 300);
        	fontDialog.setVisible(true);
        	if (fontDialog.getSelectedFont() != null)
        	{
        		view.setLabelFont(fontDialog.getSelectedFont());
        		runs.setDistanceTextFont(fontDialog.getSelectedFont());
        	}
        });

        view.getSpacecraftColorButton().addActionListener(e ->
        {
        	Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog", view.getSpacecraftColor());
        	if (tmpColor == null)
    			return;
        	view.setSpacecraftColor(tmpColor);
        	Icon spacecraftIcon = new ColorIcon(view.getSpacecraftColor(), Color.BLACK, view.getIconW(), 10);
    		view.getSpacecraftColorButton().setIcon(spacecraftIcon);
    		runs.setSpacecraftColor(view.getSpacecraftColor());
        });

        view.getEarthPointerColorButton().addActionListener(e ->
        {
        	Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog", view.getEarthPointerColor());
        	if (tmpColor == null)
    			return;
        	view.setEarthPointerColor(tmpColor);
        	Icon earthIcon = new ColorIcon(view.getEarthPointerColor(), Color.BLACK, view.getIconW(), 10);
    		view.getEarthPointerColorButton().setIcon(earthIcon);
    		runs.setEarthDirectionMarkerColor(view.getEarthPointerColor());
        });

        view.getSunPointerColorButton().addActionListener(e ->
        {
        	Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog", view.getSunPointerColor());
        	if (tmpColor == null)
    			return;
        	view.setSunPointerColor(tmpColor);
        	Icon sunIcon = new ColorIcon(view.getSunPointerColor(), Color.BLACK, view.getIconW(), 10);
    		view.getSunPointerColorButton().setIcon(sunIcon);
    		runs.setSunDirectionMarkerColor(view.getSunPointerColor());
        });

        view.getScPointerColorButton().addActionListener(e ->
        {
        	Color tmpColor = JColorChooser.showDialog(this.getView(), "Color Chooser Dialog", view.getScPointerColor());
        	if (tmpColor == null)
    			return;
        	view.setScPointerColor(tmpColor);
        	Icon scIcon = new ColorIcon(view.getScPointerColor(), Color.BLACK, view.getIconW(), 10);
    		view.getScPointerColorButton().setIcon(scIcon);
    		runs.setScDirectionMarkerColor(view.getScPointerColor());
        });

        view.getScSizeSlider().addChangeListener(e ->
        {
        	JSlider slider = (JSlider) e.getSource();
        	runs.setSpacecraftSize(slider.getValue()*.0002);
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

        view.getColorFunctionComboBox().addActionListener(e -> {
        	TrajectoryActor trajectoryActor = runs.getTrajectoryActorForStateHistory(runs.getCurrentRun());
        	Colormap colormap = (Colormap)view.getColormapComboBox().getSelectedItem();
        	colormap.setRangeMax(12);
            colormap.setRangeMin(0);
            StateHistoryColoringFunctions coloringFunction = ((StateHistoryColoringFunctions)view.getColorFunctionComboBox().getSelectedItem());
            view.getColormapComboBox().setEnabled(!(coloringFunction == StateHistoryColoringFunctions.PER_TABLE));

        	trajectoryActor.setColoringFunction(coloringFunction.getColoringFunction(), colormap);
        	runs.refreshColoring(runs.getCurrentRun());
        });

        view.getColormapComboBox().addActionListener(e -> {
        	TrajectoryActor trajectoryActor = runs.getTrajectoryActorForStateHistory(runs.getCurrentRun());
        	Colormap colormap = (Colormap)view.getColormapComboBox().getSelectedItem();
        	colormap.setRangeMax(12);
            colormap.setRangeMin(0);

        	trajectoryActor.setColoringFunction(((StateHistoryColoringFunctions)view.getColormapComboBox().getSelectedItem()).getColoringFunction(), colormap);
        	runs.refreshColoring(runs.getCurrentRun());
        });

        view.getViewOptions().setSelectedIndex(0);
        view.updateUI();
    }

	/**
	 *
	 */
	private void updateLookDirection()
	{
		RendererLookDirection selectedItem = (RendererLookDirection)view.getViewOptions().getSelectedItem();
		double[] upVector = {0,0,1};
        StateHistory currentRun = runs.getCurrentRun();

        if (currentRun != null) 	// can't do any view things if we don't have a trajectory / time history
        {
			Vector3D targOrig = new Vector3D(renderer.getCameraFocalPoint());

			if (selectedItem == RendererLookDirection.FREE_VIEW)
			{
				Vector3D targAxis = new Vector3D(runs.updateLookDirection(selectedItem, historyModel.getScalingFactor()));

//	            double[] lookFromDirection = runs.updateLookDirection(selectedItem, historyModel.getScalingFactor());
	            double[] lookFromDirection = renderer.getCameraPosition();
	            renderer.setCameraFocalPoint(new double[] {0, 0, 0});
	            renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(), renderer.getCamera().getUpUnit().toArray(), renderer.getCameraViewAngle());

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
//		        view.getDistanceOptions().setEnabled(!scSelected);
			}
        }
	}

    /**
     *
     */
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
            System.out.println("StateHistoryViewControlsController: itemStateChanged: earth pointer color " + view.getEarthPointerColor());
            runs.setEarthDirectionMarkerColor(view.getEarthPointerColor());
        } else if(source == showSunMarker){
        	runs.setSunDirectionMarkerVisibility(selected);
            sunSlider.setEnabled(selected);
            sunText.setEnabled(selected);
            runs.setSunDirectionMarkerColor(view.getSunPointerColor());
        } else if(source == showSpacecraftMarker){
        	runs.setSpacecraftDirectionMarkerVisibility(selected);
            spacecraftSlider.setEnabled(selected);
            spacecraftText.setEnabled(selected);
            runs.setScDirectionMarkerColor(view.getScPointerColor());
        } else if(source == showSpacecraft){
//            distanceOptions.setEnabled(selected);
            if (selected)
            	historyModel.setDistanceText(distanceOptions.getSelectedItem().toString());
//            runs.setSpacecraftLabelVisibility(selected);
            runs.setSpacecraftVisibility(selected);
            view.getScSizeSlider().setEnabled(selected);
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

	/**
	 * @return
	 */
	public StateHistoryViewControlsPanel getView()
	{
		return view;
	}

}
