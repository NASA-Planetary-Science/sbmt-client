package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.StateHistoryRenderModel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;

public class StateHistoryViewControlsController implements ItemListener
{

	public boolean earthEnabled = true;
	private StateHistoryViewControlsPanel view;
	private StateHistoryModel historyModel;
	private StateHistoryRenderModel renderModel;
	private StateHistoryCollection runs;

	public StateHistoryViewControlsController(StateHistoryModel historyModel, StateHistoryRenderModel renderModel)
	{
		this.historyModel = historyModel;
		this.renderModel = renderModel;
		this.runs = historyModel.getRuns();
		this.view = new StateHistoryViewControlsPanel();
		initializeViewControlPanel();
	}

	private void initializeViewControlPanel()
    {
        String[] distanceChoices = {"Distance to Center", "Distance to Surface"};
        DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);
        DefaultComboBoxModel<String> comboModelView = new DefaultComboBoxModel<String>(viewChoices.valuesAsStrings(earthEnabled));
        view.getDistanceOptions().setModel(comboModelDistance);
        view.getViewOptions().setModel(comboModelView);
        view.getShowEarthPointer().addItemListener(this);
        view.getShowSunPointer().addItemListener(this);
        view.getShowSpacecraftMarker().addItemListener(this);
        view.getShowSpacecraft().addItemListener(this);
        view.getShowLighting().addItemListener(this);


        view.getViewOptions().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistory currentRun = runs.getCurrentRun();
                if (currentRun != null) { // can't do any view things if we don't have a trajectory / time history
                    String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
                    view.getShowSpacecraft().setEnabled(true);
                    /*if(selectedItem.equals(viewChoices.FREE.toString())){

                    } else*/ if(selectedItem.equals(viewChoices.EARTH.toString())){
                        renderModel.setMove(false);
                        renderModel.setShowEarthView(true); //, view.getShowSpacecraft().isSelected());
                        view.getViewInputAngle().setText(Double.toString(renderModel.getCameraViewAngle()));
                    } else if(selectedItem.equals(viewChoices.SUN.toString())){
                        renderModel.setMove(false);
                        renderModel.setShowEarthView(false); //, view.getShowSpacecraft().isSelected());
                        renderModel.setShowSunView(true); //, view.getShowSpacecraft().isSelected());
                        view.getViewInputAngle().setText(Double.toString(renderModel.getCameraViewAngle()));
                    } else if(selectedItem.equals(viewChoices.SPACECRAFT.toString())){
                       setSpacecraftView(currentRun);
                    }
                }
            }
        });

        view.getViewOptions().setSelectedIndex(0);

        view.getBtnResetCameraTo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StateHistory currentRun = runs.getCurrentRun();
                if( currentRun != null){
                    renderModel.setCameraFocalPoint(new double[] {0,0,0});
                }
            }
        });


        view.getDistanceOptions().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistory currentRun = runs.getCurrentRun();
                String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
                if(selectedItem.equals("Distance to Center")){
                    historyModel.setDistanceText("Distance to Center");
                }else if(selectedItem.equals("Distance to Surface")){
                    historyModel.setDistanceText("Distance to Surface");
                }
            }
        });

        view.getEarthSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistory currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider) e.getSource();
                    renderModel.setEarthPointerSize(slider.getValue());
                }

            }
        });

        view.getSunSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistory currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider) e.getSource();
                    renderModel.setSunPointerSize(slider.getValue());
                }

            }
        });

        view.getSpacecraftSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistory currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider)e.getSource();
                    renderModel.setSpacecraftPointerSize(slider.getValue());
                }
            }
        });

        view.getSetViewAngle().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(e.getSource() == view.getSetViewAngle()){
                    StateHistory currentRun = runs.getCurrentRun();
                    if(!(Double.parseDouble(view.getViewInputAngle().getText())>120.0 || Double.parseDouble(view.getViewInputAngle().getText())<1.0)){
                        renderModel.setCameraViewAngle(Double.parseDouble(view.getViewInputAngle().getText()));
                    }else if(Double.parseDouble(view.getViewInputAngle().getText())>120){
                        view.getViewInputAngle().setText("120.0");
                        renderModel.setViewAngle(120.0);
                    }else{
                        view.getViewInputAngle().setText("1.0");
                        renderModel.setViewAngle(1.0);
                    }
                }

            }
        });

        view.getSaveAnimationButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistory currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    DateTime startTime = historyModel.getStartTime();
                    DateTime endTime = historyModel.getEndTime();
                    renderModel.saveAnimation(StateHistoryViewControlsController.this.getView(),
                            "" + startTime, "" + endTime);
                    view.setCursor(Cursor.getDefaultCursor());
//                    currentRun.saveAnimation(StateHistoryController.this, view.getStartTimeSpinner().getModel().getValue().toString(), view.getStopTimeSpinner().getModel().getValue().toString());
                }else
                {
                    JOptionPane.showMessageDialog(null, "No History Interval selected.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

            }
        });

        historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
			@Override
			public void historySegmentCreated(StateHistory historySegment)
			{
				view.setEnabled(true);

				String currentView = (String)view.getViewOptions().getSelectedItem();
                if (currentView.equals(viewChoices.SPACECRAFT.toString()))
                    view.getShowSpacecraft().setEnabled(false);
			}
		});

    }

	private enum viewChoices {
//      FREE("Free View"),
      SPACECRAFT("Spacecraft View"),
      EARTH("Earth View"),
      SUN("Sun View");

        private final String text;

        private viewChoices(final String text)
        {
            this.text = text;
        }

        public String toString()
        {
            return text;
        }

        public static String[] valuesAsStrings(boolean earthIncluded)
        {
            viewChoices[] values = values();
            if (earthIncluded == false)
            {
                values = new viewChoices[]{viewChoices.SPACECRAFT, viewChoices.SUN};
            }
            String[] asStrings = new String[values.length];
            for (int ix = 0; ix < values.length; ix++) {
                asStrings[ix] = values[ix].toString();
            }
            return asStrings;
        }
    }

    private void setSpacecraftView(StateHistory currentRun)
    {
        renderModel.setShowEarthView(false); //, view.getShowSpacecraft().isSelected());
        renderModel.setShowSunView(false); //, view.getShowSpacecraft().isSelected());
        renderModel.setMove(true);
        renderModel.setActorVisibility("Spacecraft", false);
        view.getShowSpacecraft().setEnabled(false);
        view.getDistanceOptions().setEnabled(false);
        view.getViewInputAngle().setText(Double.toString(renderModel.getCameraViewAngle()));
//        renderModel.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
        view.getViewOptions().setSelectedIndex(0);
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

        try
        {
            if(e.getStateChange() == ItemEvent.SELECTED){
                if(source == showEarthMarker){
                    renderModel.setActorVisibility("Earth", true);
                    earthSlider.setEnabled(true);
                    earthText.setEnabled(true);
                } else if(source == showSunMarker){
                	renderModel.setActorVisibility("Sun", true);
                    sunSlider.setEnabled(true);
                    sunText.setEnabled(true);
                } else if(source == showSpacecraftMarker){
                	renderModel.setActorVisibility("SpacecraftMarker", true);
                    spacecraftSlider.setEnabled(true);
                    spacecraftText.setEnabled(true);
                } else if(source == showSpacecraft){
                    distanceOptions.setEnabled(true);
                    historyModel.setDistanceText(distanceOptions.getSelectedItem().toString());
                    renderModel.setActorVisibility("Spacecraft", true);
                } else if(source == showLighting){
                    renderModel.setActorVisibility("Lighting", true);
                    renderModel.setFixedLightDirection(currentRun.getSunPosition());
                    renderModel.setLightingType(LightingType.FIXEDLIGHT);
                }

            }
            if(e.getStateChange() == ItemEvent.DESELECTED){
                if(source == showEarthMarker){
                    renderModel.setActorVisibility("Earth", false);
                    earthSlider.setEnabled(false);
                    earthText.setEnabled(false);
                } else if(source == showSunMarker){
                    renderModel.setActorVisibility("Sun", false);
                    sunSlider.setEnabled(false);
                    sunText.setEnabled(false);
                } else if(source == showSpacecraftMarker){
                    renderModel.setActorVisibility("SpacecraftMarker", false);
                    spacecraftSlider.setEnabled(false);
                    spacecraftText.setEnabled(false);
                } else if(source == showSpacecraft){
                    distanceOptions.setEnabled(false);
                    renderModel.setActorVisibility("Spacecraft", false);
                } else if(source == showLighting){
                    renderModel.setActorVisibility("Lighting", false);
                    renderModel.setLightingType(LightingType.LIGHT_KIT);
                }
            }
            //TODO fire actor visiblity update here
//            currentRun.updateActorVisibility();
        }catch(Exception ex){

        }


//        try
//        {
//            if(e.getStateChange() == ItemEvent.SELECTED){
//                if(source == showEarthMarker){
//                    currentRun.setActorVisibility("Earth", true);
//                } else if(source == showSunMarker){
//                    currentRun.setActorVisibility("Sun", true);
//                } else if(source == showSpacecraftMarkerHead){
//                    currentRun.setActorVisibility("SpacecraftMarkerHead", true);
//                } else if(source == showSpacecraftMarker){
//                    distanceOptions.setEnabled(true);
//                    currentRun.setDistanceText(distanceOptions.getSelectedItem().toString());
//                    currentRun.setActorVisibility("SpacecraftMarker", true);
//                } else if(source == showLighting){
//                    currentRun.setActorVisibility("Lighting", true);
//                    renderer.setFixedLightDirection(currentRun.getSunPosition());
//                    renderer.setLighting(LightingType.FIXEDLIGHT);
////                } else if(source == showSpacecraftView){
////                    currentRun.setSpacecraftMovement(true);
////                    currentRun.setActorVisibility("SpacecraftMarker", false);
////                    showSpacecraftMarker.setSelected(false);
////                    showSpacecraftMarker.setEnabled(false);
////                    distanceOptions.setEnabled(false);
////                    viewAngleInput.setText(Double.toString(currentRun.getRenderer().getCameraViewAngle()));
////                    renderer.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
////                }else if(source == showEarthView){
////                    if(!showSpacecraftMarker.isEnabled()){
////                        showSpacecraftMarker.setSelected(false);
////                        showSpacecraftMarker.setEnabled(true);
////                        currentRun.setSpacecraftMovement(false);
////                    }
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(true);
////                    viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
////                }else if(source == showSunView){
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(false);
////                    currentRun.setSunView(true);
////                    viewAngleInput.setText(Double.toString(renderer.getCameraViewAngle()));
////                }else if(source == showFreeView){
////
////                }else if(source == mapTrajectory){
////                    currentRun.setActorVisibility("Trajectory", true);
////                    showTrajectory.setEnabled(true);
////                    showTrajectory.setSelected(true);
////                }else if(source == showTrajectory){
////                    currentRun.showTrajectory(true);
//                }
//
//            }
//            if(e.getStateChange() == ItemEvent.DESELECTED){
//                if(source == showEarthMarker){
//                    currentRun.setActorVisibility("Earth", false);
//                } else if(source == showSunMarker){
//                    currentRun.setActorVisibility("Sun", false);
//                } else if(source == showSpacecraftMarkerHead){
//                    currentRun.setActorVisibility("SpacecraftMarkerHead", false);
//                } else if(source == showSpacecraftMarker){
//                    distanceOptions.setEnabled(false);
//                    currentRun.setActorVisibility("SpacecraftMarker", false);
//                } else if(source == showLighting){
//                    currentRun.setActorVisibility("Lighting", false);
//                    renderer.setLighting(LightingType.LIGHT_KIT);
////                } else if(source == showSpacecraftView){
////                    currentRun.setSpacecraftMovement(false);
////                    showSpacecraftMarker.setSelected(false);
////                    showSpacecraftMarker.setEnabled(true);
////                    distanceOptions.setEnabled(false);
////                } else if(source == showEarthView){
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(false);
////                } else if(source == showSunView){
////                    currentRun.setSpacecraftMovement(false);
////                    currentRun.setEarthView(false);
////                    currentRun.setSunView(false);
////                } else if(source == mapTrajectory){
////                    currentRun.setActorVisibility("Trajectory", false);
////                    showTrajectory.setEnabled(false);
////                    showTrajectory.setSelected(false);
////                } else if(source == showTrajectory){
////                    currentRun.showTrajectory(false);
//                }
//            }
//            currentRun.updateActorVisibility();
//        }catch(Exception ex){
//
//        }

    }

	public StateHistoryViewControlsPanel getView()
	{
		return view;
	}

}
