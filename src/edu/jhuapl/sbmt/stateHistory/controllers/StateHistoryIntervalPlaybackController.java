package edu.jhuapl.sbmt.stateHistory.controllers;

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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.animator.AnimationFrame;
import edu.jhuapl.saavtk.animator.Animator;
import edu.jhuapl.saavtk.animator.AnimatorFrameRunnable;
import edu.jhuapl.saavtk.animator.MovieGenerator;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.BaseStateHistoryTimeModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.model.time.TimeWindow;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.playback.AnimationFileDialog;
import edu.jhuapl.sbmt.stateHistory.ui.state.playback.StateHistoryIntervalPlaybackPanel;

import glum.item.ItemEventType;

/**
 * Controller that governs the "Interval Playback" panel in the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryIntervalPlaybackController
{
    private Timer timer;
    private java.util.Timer timer2;
    private boolean isPlaying = false;
    public double currentOffsetTime = 0.0;
    private StateHistoryIntervalPlaybackPanel view;
    private Icon playIcon;
    private Icon pauseIcon;
    private StateHistoryTimeModel timeModel;
    private StateHistoryCollection runs;
    private StateHistoryRendererManager rendererManager;
    private StatusNotifier statusNotifier;
    private boolean isRecording = false;
    private Animator animator;
//    private TimerTask timerTask;

	/**
	 * Constructor
	 * @param historyModel  the state history model
	 * @param renderer		the renderer object
	 */
	public StateHistoryIntervalPlaybackController(StateHistoryRendererManager rendererManager, StateHistoryTimeModel timeModel, StatusNotifier statusNotifier)
	{
		this.statusNotifier = statusNotifier;
		this.runs = rendererManager.getHistoryCollection();
		this.rendererManager = rendererManager;
		this.timeModel = timeModel;
		this.timer2 = new java.util.Timer();
		view = new StateHistoryIntervalPlaybackPanel();
		try
		{
			initializeButtonIcons();
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "There was an error loading the button icons; please see the console for a stack trace", "Loading Error",
                    JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		createTimer(runs);
		initializeIntervalPlaybackPanel(rendererManager.getRenderer(), runs);

		timeModel.addTimeModelChangeListener(new BaseStateHistoryTimeModelChangedListener() {

			@Override
			public void timeChanged(double et)
			{
				if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
				//if needed, update the time entry box and the slider
				Date date = StateHistoryTimeModel.getDateForET(et);
				if (date == (Date)view.getTimeBox().getModel().getValue()) return;
				//put on EDT?
				view.getTimeBox().setValue(date);

				try
				{
					runs.getCurrentRun().getMetadata().setCurrentTime(et);
				}
				catch (StateHistoryInvalidTimeException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				updateTimeBarValue();
				//Update the slider
				//put on EDT?
				int val = (int)Math.round((currentOffsetTime) * ((double)(view.getSlider().getMaximum() - view.getSlider().getMinimum())) + view.getSlider().getMinimum());
                view.getSlider().setValue(val);
                rendererManager.setTimeFraction(currentOffsetTime, rendererManager.getHistoryCollection().getCurrentRun());
			}

			@Override
			public void fractionDisplayedChanged(double minFractionDisplayed, double maxFractionDisplayed)
			{
				view.getSlider().setValue(0);
			}

			@Override
			public void timeFractionChanged(double fraction)
			{
				rendererManager.setTimeFraction(fraction, rendererManager.getHistoryCollection().getCurrentRun());
			}

		});

		rendererManager.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
			restorePlayPanelValues(rendererManager.getHistoryCollection());
		});
	}

	/**
	 * Returnst the panel associated with this controller
	 * @return
	 */
	public StateHistoryIntervalPlaybackPanel getView()
	{
		return view;
	}

	///////////////////////
	// Private Methods
	///////////////////////
	/**
	 * Attempts to initialize the artwork for the pause and play buttons
	 * @throws IOException
	 */
	private void initializeButtonIcons() throws IOException
	{
		Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
        play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
        playIcon = new ImageIcon(play);

        Image pause = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PauseButton.png"));
        pauseIcon = new ImageIcon(pause);
	}

	private void restorePlayPanelValues(StateHistoryCollection runs)
	{
		if (rendererManager.getSelectedItems().size() == 0) return;
		runs.setCurrentRun(rendererManager.getSelectedItems().asList().get(0));
		final JSlider slider = view.getSlider();
        int max = slider.getMaximum();

        double period = runs.getPeriod();
        double deltaRealTime = 1; //timer.getDelay() / 1000.0;
        double playRate = 1.0;
        try {
           playRate = Double.parseDouble(view.getTimeStepTextField().getText());
        } catch (Exception ex) { ex.printStackTrace(); playRate = 1.0; }

        double deltaSimulationTime = deltaRealTime * playRate;
        double deltaOffsetTime = deltaSimulationTime / period;
        TimeWindow twindow = new TimeWindow(runs.getCurrentRun().getMetadata().getStartTime(), runs.getCurrentRun().getMetadata().getEndTime());
        double et = runs.getCurrentRun().getMetadata().getCurrentTime();
        double currentOffsetTime = (et - twindow.getStartTime())/(twindow.getStopTime() - twindow.getStartTime());
        int val = (int)(currentOffsetTime/((period/playRate)/max*deltaOffsetTime));
        slider.setValue(val);
        timeModel.setTimeWindow(twindow);
        timeModel.setTimeFraction(currentOffsetTime);
	}

	/**
	 * Updates the time fraction and date values as the slider gets updated
	 */
	private void updatePlaypanelValues(StateHistoryCollection runs)
	{
		final JSlider slider = view.getSlider();
		int val = slider.getValue();
        int max = slider.getMaximum();

        double period = runs.getPeriod();
        double deltaRealTime = 1; //timer.getDelay() / 1000.0;
        double playRate = 1.0;
        try {
           playRate = Double.parseDouble(view.getTimeStepTextField().getText());
        } catch (Exception ex) { ex.printStackTrace(); playRate = 1.0; }

        double deltaSimulationTime = deltaRealTime * playRate;
        double deltaOffsetTime = deltaSimulationTime / period;
        currentOffsetTime = (val*(period/playRate)/max)*deltaOffsetTime;
        timeModel.setTimeWindow(new TimeWindow(runs.getCurrentRun().getMetadata().getStartTime(), runs.getCurrentRun().getMetadata().getEndTime()));
        timeModel.setTimeFraction(currentOffsetTime);
	}

	/**
	 * Sets up listeners for various UI components
	 */
	private void initializeIntervalPlaybackPanel(Renderer renderer, StateHistoryCollection runs)
    {
        final JSlider slider = view.getSlider();
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                if(slider.getValueIsAdjusting()){
                	updatePlaypanelValues(runs);
                }
            }
        });

        view.getRewindButton().addActionListener(e -> {

            if (isPlaying) toggleToPlay();
            slider.setValue(StateHistoryModel.INITIAL_SLIDER_VALUE);
            currentOffsetTime = 0.0;
            timeModel.setTimeFraction(currentOffsetTime);
        });

        view.getFastForwardButton().addActionListener(e -> {

            if (isPlaying) toggleToPlay();
            slider.setValue(StateHistoryModel.FINAL_SLIDER_VALUE);
            currentOffsetTime = 1.0;
            timeModel.setTimeFraction(currentOffsetTime);
        });

        view.getPlayButton().addActionListener(e -> {

            if(isPlaying)
            {
                toggleToPlay();

                renderer.setMouseEnabled(true);
                if (runs.getCurrentRun() != null)
                {
                	rendererManager.updateStatusBarValue("");
                	renderer.getRenderWindowPanel().resetCameraClippingRange();
                }
            }
            else
            {
            	toggleToPause();

                renderer.setMouseEnabled(false);
                if (runs.getCurrentRun() != null)
                {
                	rendererManager.updateStatusBarValue("Playing (mouse disabled)");
                    renderer.getRenderWindowPanel().resetCameraClippingRange();
                }
            }
        });

        view.getTimeBox().setModel(new SpinnerDateModel(new Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getTimeBox().setEditor(new JSpinner.DateEditor(view.getTimeBox(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getSetTimeButton().addActionListener(e -> {

        	Date enteredTime = (Date) view.getTimeBox().getModel().getValue();
            double et = timeModel.getETForDate(enteredTime);
            timeModel.setTime(et);
        });

        view.getRecordButton().addActionListener(e -> {
        	if (!isRecording)
        	{
	            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	            isRecording = true;
	            view.getRecordButton().setIcon(IconUtil.getStop());
	            view.getRecordButton().setToolTipText(ToolTipUtil.getStop());
	            DateTime startTime = StateHistoryTimeModel.getDateTimeForET(timeModel.getDisplayedTimeWindow().getStartTime());
	            DateTime endTime = StateHistoryTimeModel.getDateTimeForET(timeModel.getDisplayedTimeWindow().getStopTime());
	            saveAnimation(startTime, endTime, renderer, runs);
	            view.setCursor(Cursor.getDefaultCursor());

        	}
        	else
        	{
        		animator.setCancelled(true);
        		isRecording = false;
        		view.setRecordingInProgress(false);
        		view.getRecordButton().setIcon(IconUtil.getRecord());
        		view.getRecordButton().setToolTipText(ToolTipUtil.getRecord());
        	}
        });

        view.getPlaybackRateTextField().addActionListener(e -> {

        	timer.setDelay(1000/Integer.parseInt(view.getPlaybackRateTextField().getText()));

        });


        rendererManager.addListener((aSource, aEventType) -> {
			if (rendererManager.getSelectedItems().size() > 0)
			{
				if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
				runs.setCurrentRun(rendererManager.getSelectedItems().asList().get(0));
				updateTimeBarValue();
			}
			updatePlaypanelValues(runs);
        });

    }

	/**
	 * Toggles the Play/Pause button to show the play icon and stop
	 */
	private void toggleToPlay()
	{
		view.getPlayButton().setIcon(playIcon);
        timer.stop();
        timer2.cancel();
        isPlaying = false;
	}

	/**
	 * Toggles the Play/Pause button to show the pause button and start playback
	 */
	private void toggleToPause()
	{
        view.getPlayButton().setIcon(pauseIcon);
        timer.start();
        isPlaying = true;
	}

	private void updateTimeBarValue()
    {
		rendererManager.updateTimeBarValue();
    }

    /**
     * Sets the value of the JSlider that displays the time through this trajectory
     * @param tf
     */
    private void setTimeSlider(double tf){
        setSliderValue(tf);
        currentOffsetTime = tf;
    }

    /**
     * Helper method to set the slider value based on the time fraction passed in
     * @param tf
     */
    private void setSliderValue(double tf){
        int max = view.getSlider().getMaximum();
        int val = (int)Math.round(max * tf);
        view.getSlider().setValue(val);
    }

    /**
     * Creates a timer that handles the execution of time steps, calculating the proper
     * time and passing that to interested components in the rest of the system
     */
    private void createTimer(StateHistoryCollection runs)
    {
    	int timerInterval = 1000;	//how often the timer is fired, in ms

        double deltaRealTime = 1/1.0; //timer.getDelay() / 1000.0;
        int max = view.getSlider().getMaximum();
        int min = view.getSlider().getMinimum();

    	timer = new Timer(timerInterval, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
            	double period = runs.getPeriod();
                double timeStep = Double.parseDouble(view.getTimeStepTextField().getText());
            	double deltaSimulationTime = deltaRealTime * timeStep;
                double deltaOffsetTime = deltaSimulationTime / period;

                currentOffsetTime += deltaOffsetTime;

                int val = (int)Math.round((currentOffsetTime / deltaRealTime) * ((double)(max - min)) + min);
                // time looping
                if (val >= max)
                {
                	timer.stop();
                    currentOffsetTime = 0.0;
                }
                timeModel.setTimeFraction(deltaRealTime*currentOffsetTime);
            }
        });
        timer.setDelay(timerInterval);
    }



	/**
	 * Handles the animation and saving to file of the renderer frames when the user
	 * presses record
	 *
	 * @param panel
	 * @param start
	 * @param end
	 */
	private void saveAnimation(DateTime start, DateTime end, Renderer renderer, StateHistoryCollection runs)
	{
		//Create a dialog to grab the filename for the saved movie
		AnimationFileDialog dialog = new AnimationFileDialog(start.toString(), end.toString());
		int result = dialog.showSaveDialog(view);

		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
		{
			System.out.println("StateHistoryIntervalPlaybackController: saveAnimation: cancelling ");
			view.setRecordingInProgress(false);
	        view.getRecordButton().setIcon(IconUtil.getRecord());
      		view.getRecordButton().setToolTipText(ToolTipUtil.getRecord());
      		isRecording = false;
            SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					statusNotifier.setPriStatus("Ready", "Ready");
				}
			});
			return;
		}
		System.out.println("StateHistoryIntervalPlaybackController: saveAnimation: ok");
		File file = dialog.getSelectedFile();

		int frameNum = (Integer) dialog.getNumFrames().getValue();
        view.setRecordingInProgress(true);

		//Create an animator, which takes the number of frames, the file to save to, an
		//AnimatorFrameRunnable which handles the updating of the timestep, and a Runnable
		//that is kicked off in the background to handle the compilation of the frames into
		//a moving using MovieGenerator
		animator = new Animator(renderer);
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
				timeModel.setTimeFraction(getFrame().timeFraction);
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
					filenames.add(path+File.separator + ".movieCreate" + File.separator + base +"_Frame_"+index+ext);
				}
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						statusNotifier.setPriStatus("Creating movie from frames....", "Creating movie from frames....");
					}
				});

				try
				{
					MovieGenerator.create(filenames, new File(path+base + ".mp4"), renderer.getWidth(), renderer.getHeight());
				}
				catch (FileNotFoundException e)
				{
					JOptionPane.showMessageDialog(null, "The stated file could not be found; please see the console for a stack trace", "Saving Error",
		                    JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(null, "There was a problem writing the frames to file; please see the console for a stack trace", "Loading Error",
		                    JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				animator.cleanup();
	            view.setRecordingInProgress(false);
	            view.getRecordButton().setIcon(IconUtil.getRecord());
        		view.getRecordButton().setToolTipText(ToolTipUtil.getRecord());
	            SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						statusNotifier.setPriStatus("Ready", "Ready");
					}
				});

			}
		});
	}
}