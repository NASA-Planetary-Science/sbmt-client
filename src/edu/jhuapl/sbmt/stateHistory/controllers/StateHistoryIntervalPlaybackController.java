package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.animator.Animator;
import edu.jhuapl.sbmt.stateHistory.ui.AnimationFileDialog;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalPlaybackPanel;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.item.ItemEventType;

/**
 * Controller that governs the "Interval Playback" panel in the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryIntervalPlaybackController
{
    private Timer timer;
    public static final int timerInterval = 100;
    private boolean isPlaying = false;
    public double currentOffsetTime = 0.0;
    private double offsetScale = 0.1; // 0.025;
    private StateHistoryCollection runs;
    private Renderer renderer;
    private StateHistoryIntervalPlaybackPanel view;
    private StateHistoryModel historyModel;
    private Icon playIcon;
    private Icon pauseIcon;

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryIntervalPlaybackController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.historyModel = historyModel;
		this.runs = historyModel.getRuns();
		this.renderer = renderer;

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
		createTimer();
		initializeIntervalPlaybackPanel();

	}

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
	private void updatePlaypanelValues()
	{
		final JSlider slider = view.getSlider();
		int val = slider.getValue();
        int max = slider.getMaximum();
        int min = slider.getMinimum();

        double period = runs.getPeriod();
        double deltaRealTime = 1; //timer.getDelay() / 1000.0;
        double playRate = 1.0;
        try {
           playRate = Double.parseDouble(view.getRateTextField().getText());
        } catch (Exception ex) { ex.printStackTrace(); playRate = 1.0; }

        double deltaSimulationTime = deltaRealTime * playRate;
        double deltaOffsetTime = deltaSimulationTime / period;
        currentOffsetTime = (val*(period/playRate)/max)*deltaOffsetTime;

        runs.getCurrentRun().setTimeFraction(currentOffsetTime);
        runs.setTimeFraction(currentOffsetTime);

        Date date = null;
		try
		{
			date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(TimeUtil.et2str(runs.getCurrentRun().getTime()));
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        view.getTimeBox().setValue(date);
	}

	/**
	 * Sets up listeners for various UI components
	 */
	private void initializeIntervalPlaybackPanel()
    {
        final JSlider slider = view.getSlider();
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                if(slider.getValueIsAdjusting()){
                	updatePlaypanelValues();
                }
            }
        });

        view.getRewindButton().addActionListener(e -> {

            if (isPlaying) toggleToPlay();
            slider.setValue(historyModel.getDefaultSliderValue());
            currentOffsetTime = 0.0;
            runs.setTimeFraction(currentOffsetTime);
        });

        view.getFastForwardButton().addActionListener(e -> {

            if (isPlaying) toggleToPlay();
            slider.setValue(historyModel.getSliderFinalValue());
            currentOffsetTime = 1.0;
            runs.setTimeFraction(currentOffsetTime);
        });

        view.getPlayButton().addActionListener(e -> {

            if(isPlaying){
                toggleToPlay();

                renderer.setMouseEnabled(true);
                if (runs.getCurrentRun() != null)
                {
                	historyModel.setStatusBarString("");
//                  runs.getCurrentRun().updateStatusBarValue("");
                }

            }
            else
            {
            	toggleToPause();

                renderer.setMouseEnabled(false);
                if (runs.getCurrentRun() != null)
                {
//                  runs.getCurrentRun().updateStatusBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
                    historyModel.setStatusBarString("Playing (mouse disabled)");
//                  runs.getCurrentRun().updateStatusBarValue("Playing (mouse disabled)");
                }
            }
        });

        view.getTimeBox().setModel(new SpinnerDateModel(new Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getTimeBox().setEditor(new JSpinner.DateEditor(view.getTimeBox(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getSetTimeButton().addActionListener(e -> {
            Date enteredTime = (Date) view.getTimeBox().getModel().getValue();
            DateTime dt = new DateTime(enteredTime);
            DateTime dt1 = ISODateTimeFormat.dateTimeParser().parseDateTime(dt.toString());
            boolean success = historyModel.setInputTime(dt1);
            if (success) // only call again if the first call was a success
                historyModel.setInputTime(dt1); //The method needs to run twice because running once gets it close to the input but not exact. Twice shows the exact time. I don't know why.

        });

        view.getRecordButton().addActionListener(e -> {
            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            DateTime startTime = historyModel.getStartTime();
            DateTime endTime = historyModel.getEndTime();
            saveAnimation(startTime, endTime);
            view.setCursor(Cursor.getDefaultCursor());
        });

        runs.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (historyModel.getRuns().getSelectedItems().size() > 0)
			{
				historyModel.getRuns().setCurrentRun(historyModel.getRuns().getSelectedItems().asList().get(0));
//				historyModel.setStartTime(runs.getCurrentRun().);
			}
			updatePlaypanelValues();
        });

    }

	/**
	 * Toggles the Play/Pause button to show the play icon and stop
	 */
	private void toggleToPlay()
	{
		view.getPlayButton().setIcon(playIcon);
        timer.stop();
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

//	private void updateTimeBarValue()
//    {
//        if (runs != null)
//        {
//            StateHistory currentRun = runs.getCurrentRun();
//            if (currentRun != null)
//            {
//                try
//                {
//                    Double time = currentRun.getTime();
//                    historyModel.setTimeBarValue(time);
//
//                }catch(Exception ex){
//
//                }
//            }
//        }
//    }
//
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
    /**
     * Sets the value of the JSlider that displays the time through this trajectory
     * @param tf
     */
    public void setTimeSlider(double tf){
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
    public void createTimer()
    {
        timer = new Timer(timerInterval, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                double period = runs.getPeriod();
                double deltaRealTime = 1; //timer.getDelay() / 1000.0;
                double playRate = 1.0;
                try {
                   playRate = Double.parseDouble(view.getRateTextField().getText());
                } catch (Exception ex) { ex.printStackTrace(); playRate = 1.0; }
                double deltaSimulationTime = deltaRealTime * playRate;
                double deltaOffsetTime = deltaSimulationTime / period;

                currentOffsetTime += deltaOffsetTime;

                int max = view.getSlider().getMaximum();
                int min = view.getSlider().getMinimum();
                int val = (int)Math.round((currentOffsetTime / offsetScale) * ((double)(max - min)) + min);
                // time looping
                if (val >= max)
                {
                	timer.stop();
                    currentOffsetTime = 0.0;
                }

                runs.getCurrentRun().setTimeFraction(10*currentOffsetTime);
                runs.setTimeFraction(currentOffsetTime);

                //Update the slider
                view.getSlider().setValue(val);

                //Update the time box with the current time
                view.getTimeBox().setValue(new Date(historyModel.getStartTime().toDate().getTime() + new Double(1000*val/((double)(max - min)) * runs.getCurrentRun().getPeriod()).longValue()));
            }
        });
        timer.setDelay(timerInterval);
    }

	/**
	 * Returnst the panel associated with this controller
	 * @return
	 */
	public StateHistoryIntervalPlaybackPanel getView()
	{
		return view;
	}

	// starts the process for creating the movie frames
	/**
	 * Handles the animation and saving to file of the renderer frames when the user
	 * presses record
	 *
	 * @param panel
	 * @param start
	 * @param end
	 */
	public void saveAnimation(DateTime start, DateTime end)
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
				runs.getCurrentRun().setTimeFraction(getFrame().timeFraction);
				runs.setTimeFraction(currentOffsetTime);
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