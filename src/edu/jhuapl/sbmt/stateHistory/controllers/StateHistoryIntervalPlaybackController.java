package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.HasTime;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

public class StateHistoryIntervalPlaybackController
{

	public StateHistoryIntervalPlaybackController()
	{
		// TODO Auto-generated constructor stub
	}

	private void initializeIntervalPlaybackPanel()
    {
        final JSlider slider = view.getSlider();
        slider.addChangeListener(new ChangeListener() {         //ADD TO CONTROLLER
            public void stateChanged(ChangeEvent evt) {
                if(slider.getValueIsAdjusting()){
                    int val = slider.getValue();
                    int max = slider.getMaximum();
                    int min = slider.getMinimum();
                    currentOffsetTime = (double)(val - min) / (double)(max-min) * offsetScale;
                    ((HasTime)model).setTimeFraction(currentOffsetTime);
                }
            }
        });

        view.getRewindButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                if(playChecked){
                    try
                    {
                        Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
                        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
                        Icon playIcon = new ImageIcon(play);
                        view.getPlayButton().setIcon(playIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.stop();
                    playChecked = false;
                }

                slider.setValue(defaultValue);
                currentOffsetTime = 0.0;
                if (model != null)
                    model.setTimeFraction(currentOffsetTime);
            }
        });

        view.getFastForwardButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                if(playChecked){
                    try
                    {
                        Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
                        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
                        Icon playIcon = new ImageIcon(play);
                        view.getPlayButton().setIcon(playIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.stop();
                    playChecked = false;
                }

                slider.setValue(finalValue);
                currentOffsetTime = 1.0;
                if (model != null)
                    model.setTimeFraction(currentOffsetTime);

            }
        });

        view.getPlayButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                if(playChecked){
                    try
                    {
                        Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
                        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
                        Icon playIcon = new ImageIcon(play);
                        view.getPlayButton().setIcon(playIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.stop();
                    renderer.setMouseEnabled(true);
                    if (runs.getCurrentRun() != null)
                    {
                        runs.getCurrentRun().updateStatusBarValue("");
                    }
                    playChecked = false;
                }
                else
                {
                    if (view.getTable().getSelectedRowCount() == 0)
                    {
                        JOptionPane.showMessageDialog(null, "Please select a row from the table before playing", "Choose Interval",
                                JOptionPane.OK_OPTION);
                        return;
                    }
                    try
                    {
                        Image pause = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PauseButton.png"));
                        Icon pauseIcon = new ImageIcon(pause);
                        view.getPlayButton().setIcon(pauseIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
                    timer.start();
                    renderer.setMouseEnabled(false);
                    if (runs.getCurrentRun() != null)
                    {
                        runs.getCurrentRun().updateStatusBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
                        runs.getCurrentRun().updateStatusBarValue("Playing (mouse disabled)");
                    }
                    playChecked = true;
                }
            }
        });

        view.getTimeBox().setModel(new SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getTimeBox().setEditor(new javax.swing.JSpinner.DateEditor(view.getTimeBox(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getSetTimeButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistoryModel currentRun = stateHistoryCollection.getCurrentRun();
                if (currentRun != null)
                {
                    Date enteredTime = (Date) view.getTimeBox().getModel().getValue();
                    DateTime dt = new DateTime(enteredTime);
                    DateTime dt1 = ISODateTimeFormat.dateTimeParser()
                            .parseDateTime(dt.toString());
                    boolean success = currentRun.setInputTime(dt1, StateHistoryController.this);
                    if (success) // only call again if the first call was a success
                        currentRun.setInputTime(dt1, StateHistoryController.this); //The method needs to run twice because running once gets it close to the input but not exact. Twice shows the exact time. I don't know why.
                }else{
                    JOptionPane.showMessageDialog(null, "No Time Interval selected.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

            }
        });
    }

	private void updateTimeBarValue()
    {
        if (runs != null)
        {
            StateHistoryModel currentRun = runs.getCurrentRun();
            if (currentRun != null)
            {
                try
                {
                    Double time = currentRun.getTime();
                    currentRun.updateTimeBarValue(time);
                }catch(Exception ex){

                }
            }
        }
    }

    public void updateTimeBarPosition()
    {
        if (runs != null)
        {
            StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
            StateHistoryModel currentRun = runs.getCurrentRun();
            if (currentRun != null)
                currentRun.updateTimeBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
        }
    }

    //
    // used to set the time for the slider and its time fraction.
    //
    public void setTimeSlider(double tf){
//        StateHistoryModel currentRun = runs.getCurrentRun();
//        TimeChanger tc = timeControlPane.getTimeChanger();
//        tc.setSliderValue(tf);
        setSliderValue(tf);
        currentOffsetTime = tf;
    }

    public void setSliderValue(double tf){
        manualSetTime = true;
        int max = view.getSlider().getMaximum();

        int val = (int)Math.round(max * tf);

        view.getSlider().setValue(val);
    }

}
