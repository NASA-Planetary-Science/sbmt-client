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
import java.util.logging.Level;
import java.util.logging.Logger;

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

import edu.jhuapl.saavtk.animator.AnimationFrame;
import edu.jhuapl.saavtk.animator.Animator;
import edu.jhuapl.saavtk.animator.AnimatorFrameRunnable;
import edu.jhuapl.saavtk.animator.MovieGenerator;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.BaseStateHistoryTimeModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.AnimationFileDialog;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.StateHistoryIntervalPlaybackPanel;

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
//    private TimerTask timerTask;

	/**
	 * Constructor
	 * @param historyModel  the state history model
	 * @param renderer		the renderer object
	 */
	public StateHistoryIntervalPlaybackController(StateHistoryRendererManager rendererManager, StateHistoryTimeModel timeModel)
	{
		this.runs = rendererManager.getRuns();
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
				if (rendererManager.getRuns().getCurrentRun() == null) return;
				Logger.getAnonymousLogger().log(Level.INFO, "Starting in playback");
				//if needed, update the time entry box and the slider
				Date date = timeModel.getDateForET(et);
				if (date == (Date)view.getTimeBox().getModel().getValue()) return;
				//put on EDT?
				view.getTimeBox().setValue(date);

				//Update the slider
				//put on EDT?
				int val = (int)Math.round((currentOffsetTime) * ((double)(view.getSlider().getMaximum() - view.getSlider().getMinimum())) + view.getSlider().getMinimum());
                view.getSlider().setValue(val);

                Logger.getAnonymousLogger().log(Level.INFO, "Setting history model times");
                rendererManager.setTimeFraction(currentOffsetTime, rendererManager.getRuns().getCurrentRun());
                Logger.getAnonymousLogger().log(Level.INFO, "Ending in playback");
			}

			@Override
			public void fractionDisplayedChanged(double minFractionDisplayed, double maxFractionDisplayed)
			{
				view.getSlider().setValue(0);
			}

			@Override
			public void timeFractionChanged(double fraction)
			{
				rendererManager.setTimeFraction(fraction, rendererManager.getRuns().getCurrentRun());
			}

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
//        System.out.println("StateHistoryIntervalPlaybackController: updatePlaypanelValues: current offset time " + currentOffsetTime);


//        System.out.println("StateHistoryIntervalPlaybackController: updatePlaypanelValues: time window " + timeModel.getDisplayedTimeWindow() + " and time " + TimeUtil.et2str(timeModel.getEt()));
        //TODO FIX ME - still needed?
//        try
//		{
//			runs.getCurrentRun().setTimeFraction(currentOffsetTime);
//		}
//		catch (StateHistoryInvalidTimeException e1)
//		{
//			JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//			// TODO Auto-generated catch block
////			e1.printStackTrace();
//		}
        timeModel.setTimeFraction(currentOffsetTime);
//        runs.setTimeFraction(currentOffsetTime);

        Date date = timeModel.getDateForET(timeModel.getEt());

        //TODO need this?
//		historyModel.setTime(timeModel.getEt());
        view.getTimeBox().setValue(date);
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
//            runs.setTimeFraction(currentOffsetTime);
        });

        view.getFastForwardButton().addActionListener(e -> {

            if (isPlaying) toggleToPlay();
            slider.setValue(StateHistoryModel.FINAL_SLIDER_VALUE);
            currentOffsetTime = 1.0;
            timeModel.setTimeFraction(currentOffsetTime);
//            runs.setTimeFraction(currentOffsetTime);
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

            //TODO still needed?
//            try
//			{
//				historyModel.getRuns().getCurrentRun().setCurrentTime(new Double(dt1.toDate().getTime()));
//			}
//			catch (StateHistoryInvalidTimeException e1)
//			{
//	            JOptionPane.showMessageDialog(null, e1.getMessage(), "Error",
//	                    JOptionPane.ERROR_MESSAGE);
////				e1.printStackTrace();
//			}
        });

        view.getRecordButton().addActionListener(e -> {
            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            DateTime startTime = timeModel.getDateTimeForET(runs.getCurrentRun().getStartTime());
            DateTime endTime = timeModel.getDateTimeForET(runs.getCurrentRun().getEndTime());
            saveAnimation(startTime, endTime, renderer, runs);
            view.setCursor(Cursor.getDefaultCursor());
        });

        view.getPlaybackRateTextField().addActionListener(e -> {

        	timer.setDelay(1000/Integer.parseInt(view.getPlaybackRateTextField().getText()));

        });

        rendererManager.addListener((aSource, aEventType) -> {
//        	System.out.println("StateHistoryIntervalPlaybackController: initializeIntervalPlaybackPanel: event type " + aEventType);
//			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getSelectedItems().size() > 0)
			{
				runs.setCurrentRun(rendererManager.getSelectedItems().asList().get(0));
				updateTimeBarValue();
			}
//			System.out.println("StateHistoryIntervalPlaybackController: initializeIntervalPlaybackPanel: updating play panel to listener response");
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
//        timer2.scheduleAtFixedRate(timerTask, 0, 1);
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
//        historyModel.setTime(historyModel.getRuns().getCurrentRun().getCurrentTime());
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
//        double timeStep = 1.0;
//        try {

//        } catch (Exception ex) { ex.printStackTrace(); timeStep = 1.0; }

//        timerTask = new TimerTask()
//		{
//
//			@Override
//			public void run()
//			{
//				double period = runs.getPeriod();
//                double timeStep = Double.parseDouble(view.getTimeStepTextField().getText());
//            	double deltaSimulationTime = deltaRealTime * timeStep;
//                double deltaOffsetTime = deltaSimulationTime / period;
//
//                currentOffsetTime += deltaOffsetTime;
//
//                int val = (int)Math.round((currentOffsetTime / deltaRealTime) * ((double)(max - min)) + min);
//                // time looping
//                if (val >= max)
//                {
//                	timer.stop();
//                    currentOffsetTime = 0.0;
//                }
//
//                timeModel.setTimeFraction(deltaRealTime*currentOffsetTime);
//
//				//Update the slider
//                SwingUtilities.invokeLater(new Runnable()
//				{
//
//					@Override
//					public void run()
//					{
//						  view.getSlider().setValue(val);
//
//			                //Update the time box with the current time
//			                Date date = getDateForET(timeModel.getEt());
//			                view.getTimeBox().setValue(date);
//			            	Logger.getAnonymousLogger().log(Level.INFO, "firing setting date " + date);
//
//			                historyModel.setTime(timeModel.getEt());
//					}
//				});
//
//			}
//		};


    	timer = new Timer(timerInterval, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
//            	Logger.getAnonymousLogger().log(Level.INFO, "start");
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
                Logger.getAnonymousLogger().log(Level.INFO, "setting time fraction");
                timeModel.setTimeFraction(deltaRealTime*currentOffsetTime);
                Logger.getAnonymousLogger().log(Level.INFO, "set time fraction");

                //Don't think I need these since they are handled elsewhere
//				//Update the slider
//                view.getSlider().setValue(val);
//
//                //Update the time box with the current time
//                Date date = getDateForET(timeModel.getEt());
//                view.getTimeBox().setValue(date);
////            	Logger.getAnonymousLogger().log(Level.INFO, "firing setting date " + date);
//
//                historyModel.setTime(timeModel.getEt());
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
			return;
		}

		File file = dialog.getSelectedFile();

		int frameNum = (Integer) dialog.getNumFrames().getValue();

		//Create an animator, which takes the number of frames, the file to save to, an
		//AnimatorFrameRunnable which handles the updating of the timestep, and a Runnable
		//that is kicked off in the background to handle the compilation of the frames into
		//a moving using MovieGenerator
		Animator animator = new Animator(renderer);
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
//                runs.setTimeFraction(getFrame().timeFraction);
				//TODO FIX ME
//				try
//				{
//					runs.getCurrentRun().setTimeFraction(getFrame().timeFraction);
//				}
//				catch (StateHistoryInvalidTimeException e)
//				{
//					JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//
//					// TODO Auto-generated catch block
////					e.printStackTrace();
//				}
//				runs.setTimeFraction(currentOffsetTime);
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
			}
		});
	}
}