package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;

public class StateHistoryViewControlsController
{

	public StateHistoryViewControlsController()
	{
		// TODO Auto-generated constructor stub
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
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null) { // can't do any view things if we don't have a trajectory / time history
                    String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
                    view.getShowSpacecraft().setEnabled(true);
                    /*if(selectedItem.equals(viewChoices.FREE.toString())){

                    } else*/ if(selectedItem.equals(viewChoices.EARTH.toString())){
                        currentRun.setSpacecraftMovement(false);
                        currentRun.setEarthView(true, view.getShowSpacecraft().isSelected());
                        view.getViewInputAngle().setText(Double.toString(renderer.getCameraViewAngle()));
                    } else if(selectedItem.equals(viewChoices.SUN.toString())){
                        currentRun.setSpacecraftMovement(false);
                        currentRun.setEarthView(false, view.getShowSpacecraft().isSelected());
                        currentRun.setSunView(true, view.getShowSpacecraft().isSelected());
                        view.getViewInputAngle().setText(Double.toString(renderer.getCameraViewAngle()));
                    } else if(selectedItem.equals(viewChoices.SPACECRAFT.toString())){
                       setSpacecraftView(currentRun);
                    }
                }
            }
        });

        view.getViewOptions().setSelectedIndex(0);

        view.getBtnResetCameraTo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if( currentRun != null){
                    currentRun.getRenderer().setCameraFocalPoint(new double[] {0,0,0});
                }
            }
        });


        view.getDistanceOptions().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                String selectedItem = (String)((JComboBox<String>)e.getSource()).getSelectedItem();
                if(selectedItem.equals("Distance to Center")){
                    currentRun.setDistanceText("Distance to Center");
                }else if(selectedItem.equals("Distance to Surface")){
                    currentRun.setDistanceText("Distance to Surface");
                }
            }
        });

        view.getEarthSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider) e.getSource();
                    currentRun.setEarthPointerSize(slider.getValue());
                }

            }
        });

        view.getSunSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider) e.getSource();
                    currentRun.setSunPointerSize(slider.getValue());
                }

            }
        });

        view.getSpacecraftSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    JSlider slider = (JSlider)e.getSource();
                    currentRun.setSpacecraftPointerSize(slider.getValue());
                }
            }
        });

        view.getSetViewAngle().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(e.getSource() == view.getSetViewAngle()){
                    StateHistoryModel currentRun = runs.getCurrentRun();
                    if(!(Double.parseDouble(view.getViewInputAngle().getText())>120.0 || Double.parseDouble(view.getViewInputAngle().getText())<1.0)){
                        renderer.setCameraViewAngle(Double.parseDouble(view.getViewInputAngle().getText()));
                    }else if(Double.parseDouble(view.getViewInputAngle().getText())>120){
                        view.getViewInputAngle().setText("120.0");
                        currentRun.setViewAngle(120.0);
                    }else{
                        view.getViewInputAngle().setText("1.0");
                        currentRun.setViewAngle(1.0);
                    }
                }

            }
        });

        view.getSaveAnimationButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = runs.getCurrentRun();
                if (currentRun != null)
                {
                    view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    DateTime startTime = currentRun.getStartTime();
                    DateTime endTime = currentRun.getEndTime();
                    currentRun.saveAnimation(StateHistoryController.this,
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

    private void setSpacecraftView(StateHistoryModel currentRun)
    {
        currentRun.setEarthView(false, view.getShowSpacecraft().isSelected());
        currentRun.setSunView(false, view.getShowSpacecraft().isSelected());
        currentRun.setSpacecraftMovement(true);
        currentRun.setActorVisibility("Spacecraft", false);
        view.getShowSpacecraft().setEnabled(false);
        view.getDistanceOptions().setEnabled(false);
        view.getViewInputAngle().setText(Double.toString(currentRun.getRenderer().getCameraViewAngle()));
        renderer.setCameraViewAngle(currentRun.getRenderer().getCameraViewAngle());
        view.getViewOptions().setSelectedIndex(0);
    }

}
