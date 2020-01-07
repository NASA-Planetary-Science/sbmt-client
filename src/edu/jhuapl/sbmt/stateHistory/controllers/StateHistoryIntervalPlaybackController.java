package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.AnimatorFrameRunnable;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.animator.AnimationFrame;
import edu.jhuapl.sbmt.stateHistory.model.animator.MovieGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.animator.Animator;
import edu.jhuapl.sbmt.stateHistory.ui.AnimationFileDialog;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalPlaybackPanel;

public class StateHistoryIntervalPlaybackController
{

    private Timer timer;
    public static final int timerInterval = 100;
    private boolean playChecked = false;
    private boolean manualSetTime = false;
    public double currentOffsetTime = 0.0;
    private double offsetScale = 1.0; // 0.025;
//    private HasTime model;
    private StateHistoryCollection runs;
    private Renderer renderer;
    private StateHistoryIntervalPlaybackPanel view;
    private StateHistoryModel historyModel;

	public StateHistoryIntervalPlaybackController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		this.runs = historyModel.getRuns();
		this.renderer = renderer;

		view = new StateHistoryIntervalPlaybackPanel();
		createTimer();
		initializeIntervalPlaybackPanel();
	}

	private void initializeIntervalPlaybackPanel()
    {
        final JSlider slider = view.getSlider();
        slider.addChangeListener(new ChangeListener() {         //ADD TO CONTROLLER
            public void stateChanged(ChangeEvent evt) {
//                if(slider.getValueIsAdjusting()){
//                    int val = slider.getValue();
//                    int max = slider.getMaximum();
//                    int min = slider.getMinimum();
//                    currentOffsetTime = (double)(val - min) / (double)(max-min) * offsetScale;
//                    ((HasTime)model).setTimeFraction(currentOffsetTime);
//                }
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

                slider.setValue(historyModel.getDefaultSliderValue());
                currentOffsetTime = 0.0;
//                if (model != null)
                    runs.setTimeFraction(runs.getCurrentRun(), currentOffsetTime);
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

                slider.setValue(historyModel.getSliderFinalValue());
                currentOffsetTime = 1.0;
//                if (model != null)
                    runs.setTimeFraction(runs.getCurrentRun(), currentOffsetTime);

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

//                    ((RenderPanel)(renderer.getRenderWindowPanel())).constrainRotationAxis(Vector3D.ZERO, new Vector3D(runs.getCurrentLookFromDirection()));
                    timer.stop();
                    renderer.setMouseEnabled(true);
                    if (runs.getCurrentRun() != null)
                    {
                    	historyModel.setStatusBarString("");
//                        runs.getCurrentRun().updateStatusBarValue("");
                    }
                    playChecked = false;
                }
                else
                {
//                    if (view.getTable().getSelectedRowCount() == 0)
//                    {
//                        JOptionPane.showMessageDialog(null, "Please select a row from the table before playing", "Choose Interval",
//                                JOptionPane.OK_OPTION);
//                        return;
//                    }
                    try
                    {
                        Image pause = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PauseButton.png"));
                        Icon pauseIcon = new ImageIcon(pause);
                        view.getPlayButton().setIcon(pauseIcon);
                    }catch (Exception ex)
                    {
//                        System.out.println(ex);
                    }
//                    ((RenderPanel)(renderer.getRenderWindowPanel())).constrainRotationAxis(Vector3D.PLUS_J, new Vector3D(runs.getCurrentLookFromDirection()));

//                    ((RenderPanel)(renderer.getRenderWindowPanel())).constrainRotationAxis(new Vector3D(renderer.getCameraFocalPoint()), new Vector3D(runs.getCurrentLookFromDirection()));
                    timer.start();
                    renderer.setMouseEnabled(false);
                    if (runs.getCurrentRun() != null)
                    {
//                        runs.getCurrentRun().updateStatusBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
                        historyModel.setStatusBarString("Playing (mouse disabled)");
//                    	runs.getCurrentRun().updateStatusBarValue("Playing (mouse disabled)");
                    }
                    playChecked = true;
                }
            }
        });

        view.getTimeBox().setModel(new SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getTimeBox().setEditor(new JSpinner.DateEditor(view.getTimeBox(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getSetTimeButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                StateHistory currentRun = historyModel.getCurrentFlybyStateHistory();
                if (currentRun != null)
                {
                    Date enteredTime = (Date) view.getTimeBox().getModel().getValue();
                    DateTime dt = new DateTime(enteredTime);
                    DateTime dt1 = ISODateTimeFormat.dateTimeParser()
                            .parseDateTime(dt.toString());
                    boolean success = historyModel.setInputTime(dt1);
                    if (success) // only call again if the first call was a success
                        historyModel.setInputTime(dt1); //The method needs to run twice because running once gets it close to the input but not exact. Twice shows the exact time. I don't know why.
                }else{
                    JOptionPane.showMessageDialog(null, "No Time Interval selected.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

            }
        });

        view.getRecordButton().addActionListener(new ActionListener()
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
                    saveAnimation(StateHistoryIntervalPlaybackController.this.getView(),
                            "" + startTime, "" + endTime);
                    view.setCursor(Cursor.getDefaultCursor());
                }else
                {
                    JOptionPane.showMessageDialog(null, "No History Interval selected.", "Error",
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
            StateHistory currentRun = runs.getCurrentRun();
            if (currentRun != null)
            {
                try
                {
                    Double time = currentRun.getTime();
                    historyModel.setTimeBarValue(time);
                }catch(Exception ex){

                }
            }
        }
    }

//    public void updateTimeBarPosition()
//    {
//        if (runs != null)
//        {
//            StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
//            StateHistory currentRun = runs.getCurrentRun();
//            if (currentRun != null)
//                currentRun.updateTimeBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
//        }
//    }

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

    public void createTimer()
    {
        timer = new Timer(timerInterval, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                double period = runs.getPeriod();
                double deltaRealTime = timer.getDelay() / 1000.0;
                double playRate = 1.0;
                try {
                   playRate = Double.parseDouble(view.getRateTextField().getText());
                } catch (Exception ex) { ex.printStackTrace(); playRate = 1.0; }

                double deltaSimulationTime = deltaRealTime * playRate;
                double deltaOffsetTime = deltaSimulationTime / period;
//                System.out.println("Delta time: " + deltaSimulationTime + " Delta offset time: " + deltaOffsetTime);

                currentOffsetTime += deltaOffsetTime;
                // time looping
                if (currentOffsetTime > 1.0)
                    currentOffsetTime = 0.0;
                runs.getCurrentRun().setTimeFraction(runs.getCurrentRun(), currentOffsetTime);
                runs.setTimeFraction(runs.getCurrentRun(), currentOffsetTime);

                int max = view.getSlider().getMaximum();
                int min = view.getSlider().getMinimum();

                int val = (int)Math.round((currentOffsetTime / offsetScale) * ((double)(max - min)) + min);
                view.getSlider().setValue(val);

            }
        });
        timer.setDelay(timerInterval);
    }

	public StateHistoryIntervalPlaybackPanel getView()
	{
		return view;
	}



	// starts the process for creating the movie frames
	public void saveAnimation(Component panel, String start, String end)
	{
		AnimationFileDialog dialog = new AnimationFileDialog(start, end);
		int result = dialog.showSaveDialog(panel);

		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
		{
			return;
		}

		File file = dialog.getSelectedFile();

		int frameNum = (Integer) dialog.getNumFrames().getValue();
		double timeStep = 1.0 / (double) frameNum;

		Animator animator = new Animator(renderer, runs);
		animator.saveAnimation(frameNum, file, new AnimatorFrameRunnable()
		{
			@Override
			public void run(AnimationFrame frame)
			{
				// TODO Auto-generated method stub
				super.run(frame);
				run();
			}

			@Override
			public void run()
			{
				runs.getCurrentRun().setTimeFraction(runs.getCurrentRun(), getFrame().timeFraction);
				runs.setTimeFraction(runs.getCurrentRun(), currentOffsetTime);
				setTimeSlider(getFrame().timeFraction);
			}
		},
		new Runnable()
		{

			@Override
			public void run()
			{
				String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		        String base = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator));
		        String ext = ".png";
				List<String> filenames = new ArrayList<String>();
				for (int i=0; i<=frameNum; i++)
				{
					String index = String.format("%03d",  (int)i);
					filenames.add(path+base+"_Frame_"+index+ext);
				}
				try
				{
					MovieGenerator.create(filenames, new File(path+base + ".mp4"), renderer.getWidth(), renderer.getHeight());
				}
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

}
