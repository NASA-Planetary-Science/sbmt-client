package edu.jhuapl.sbmt.stateHistory.ui.state.playback;

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;

import glum.gui.GuiUtil;

/**
 * The panel that controls the VCR-like set of playback controls for the State History tab
 * @author steelrj1
 *
 */
public class StateHistoryIntervalPlaybackPanel extends JPanel
{
    /**
     * The time step text field.  Governs how many seconds is advanced every ticker of the slider
     */
    private JTextField timeStepTextField;

    /**
     * The playback rate text field.  Governs how much faster than realtime the playback is
     */
    private JTextField playbackRateTextField;

    /**
     * Rewind button for moving to the start of the slider
     */
    private JButton rewindButton;

    /**
     * Play button - starts to play at the time denoted in the slider; toggles to pause button during playback
     */
    private JButton playButton;

    /**
     * Record button - records the playback to file
     */
    private JButton recordButton;

    /**
     * Fast forward button for moving to the end of the slider
     */
    private JButton fastForwardButton;

    /**
     * The JSlider that shows the current timestep within the selected trajectory
     */
    private JSlider slider;

    /**
     * Minimum slider value
     */
    private int sliderMin = 0;

    /**
     * Maximum slider value
     */
    private int sliderMax = 900;

    /**
     *	Minor tick value for slider
     */
    private int sliderMinorTick = 30;

    /**
     * Major tick value for slider
     */
    private int sliderMajorTick = 150;

    /**
     * Default value for the slider
     */
    private int defaultValue = 0; // 15;

    /**
     * JSpinner that allows the user to dial up a specific time.  Also shows the corresponding time of the slider
     */
    private JSpinner timeBox;

    /**
     * JButton that sets the time as defined in the <pre>timeBox</pre>
     */
    private JButton setTimeButton;

    /**
     * JLabel for "Enter UTC Time"
     */
    private JLabel lblEnterUtcTime;

    /**
     * Parent panel that contains the UTC time components
     */
    private JPanel utcPanel;

    /**
     * Parent panel that containst the playback controls
     */
    private JPanel playPanel;

    /**
     * JLabel for the play speed
     */
    private JLabel playbackRateLabel;

    /**
     * JLabel for the time step
     */
    private JLabel timeStepLabel;


	/**
	 * Constructor.
	 */
	public StateHistoryIntervalPlaybackPanel()
	{
		initUI();
	}

	/**
	 * Builds the user interface elements
	 */
	private void initUI()
	{
        setBorder(new TitledBorder(null, "Interval Playback", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        slider = new JSlider();
        panel_1.add(slider);
        slider.setMinimum(sliderMin);
        slider.setMaximum(sliderMax);
        slider.setMinorTickSpacing(sliderMinorTick);
        slider.setMajorTickSpacing(sliderMajorTick);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setValue(defaultValue);

        playPanel = new JPanel();
        add(playPanel);
        playPanel.setLayout(new BoxLayout(playPanel, BoxLayout.X_AXIS));

        timeStepLabel = new JLabel("Time Step (s):");
        playPanel.add(timeStepLabel);

        Image questionMark;
        try
        {
            questionMark = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/questionMark.png"));
            Icon question = new ImageIcon(questionMark);
            JLabel questionRate = new JLabel(question);
            questionRate.setToolTipText("<html>The number of seconds in one step of the playback <br/>For example: '60.0' means 1 minute of the interval is <br/>traveled per tick</html>");

            playPanel.add(questionRate);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        timeStepTextField = new JTextField("60.0");
        timeStepTextField.setMaximumSize( new Dimension(100, timeStepTextField.getPreferredSize().height) );
        playPanel.add(timeStepTextField);
        timeStepTextField.setColumns(10);

        playbackRateLabel = new JLabel("Playback Rate (ticks/sec):");
        playPanel.add(playbackRateLabel);

        Image questionMark2;
        try
        {
            questionMark2 = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/questionMark.png"));
            Icon question = new ImageIcon(questionMark2);
            JLabel questionRate = new JLabel(question);
            questionRate.setToolTipText("<html>Rate of playback faster than real time. <br/> For example: 5 means 5 ticks of playback per second</html>");

            playPanel.add(questionRate);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        playbackRateTextField = new JTextField("1");
        playbackRateTextField.setMaximumSize( new Dimension(100, playbackRateTextField.getPreferredSize().height) );
        playPanel.add(playbackRateTextField);
        playbackRateTextField.setColumns(10);

        rewindButton = new JButton("");
        customizeButton(rewindButton, "/edu/jhuapl/sbmt/data/RewindButton.png", "Rewind");
        playPanel.add(rewindButton);

        playButton = new JButton("");
        customizeButton(playButton, "/edu/jhuapl/sbmt/data/PlayButton.png", "Play");
        playPanel.add(playButton);

        recordButton = GuiUtil.formButton(null, IconUtil.getRecord());
        recordButton.setToolTipText(ToolTipUtil.getRecord());
        recordButton.setEnabled(false);
        playPanel.add(recordButton);


        fastForwardButton = new JButton("");
        customizeButton(fastForwardButton, "/edu/jhuapl/sbmt/data/FastforwardButton.png", "Fast Forward");
        playPanel.add(fastForwardButton);

        utcPanel = new JPanel();
        add(utcPanel);
        utcPanel.setLayout(new BoxLayout(utcPanel, BoxLayout.X_AXIS));

        lblEnterUtcTime = new JLabel("Enter UTC Time:");
        utcPanel.add(lblEnterUtcTime);

        timeBox = new JSpinner();

        Dimension spinnerSize = new Dimension(300, 28);
        timeBox.setMinimumSize(spinnerSize);
        timeBox.setPreferredSize(spinnerSize);
        timeBox.setMaximumSize(spinnerSize);
        utcPanel.add(timeBox);

        setTimeButton = new JButton("Set Time");
        utcPanel.add(setTimeButton);
	}

	/**
	 * Helper method to add icons to the various button
	 * @param button	JButton to configure
	 * @param filename	The filename of the image for this button, relative to the getResource call (for our purposes "/edu/jhuapl/sbmt/data/..."
	 * @param altText	Alternative text to show if an exception is thrown loading the image
	 */
	private void customizeButton(JButton button, String filename, String altText)
	{
		try
        {
            Image image = ImageIO.read(getClass().getResource(filename));
            image.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
            Icon icon = new ImageIcon(image);
            button.setIcon(icon);
        }
        catch (Exception e)
        {
            button.setText(altText);
        }
	}

    /**
     * Returns the rewind button
     * @return the rewind button
     */
    public JButton getRewindButton()
    {
        return rewindButton;
    }

    /**
     * Returns the play button
     * @return the play button
     */
    public JButton getPlayButton()
    {
        return playButton;
    }

    /**
     * Returns the fast forward button
     * @return the fast forward button
     */
    public JButton getFastForwardButton()
    {
        return fastForwardButton;
    }

    /**
     * Returns the record button
     * @return the record button
     */
    public JButton getRecordButton()
	{
		return recordButton;
	}

	/**
	 * @return
	 */
	public JSlider getSlider()
    {
        return slider;
    }

    /**
     * Returns the time step text field JTextField
     * @return the time step text field JTextField
     */
    public JTextField getTimeStepTextField()
    {
        return timeStepTextField;
    }

    /**
     * Returns the playback rate text field JTextField
     * @return the playback rate text field JTextField
     */
    public JTextField getPlaybackRateTextField()
    {
        return playbackRateTextField;
    }

    /**
     * Returns the time box spinner
     * @return the time box spinner
     */
    public JSpinner getTimeBox()
    {
        return timeBox;
    }

    /**
     * Returns the set time button
     * @return the set time button
     */
    public JButton getSetTimeButton()
    {
        return setTimeButton;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
    	timeStepTextField.setEnabled(enabled);
    	rewindButton.setEnabled(enabled);
    	playButton.setEnabled(enabled);
    	recordButton.setEnabled(enabled);
    	fastForwardButton.setEnabled(enabled);
    	timeBox.setEnabled(enabled);
    	setTimeButton.setEnabled(enabled);
    	timeStepLabel.setEnabled(enabled);
    	lblEnterUtcTime.setEnabled(enabled);
    	playPanel.setEnabled(enabled);
    	super.setEnabled(enabled);
    }

    public void setRecordingInProgress(boolean inProgress)
    {
    	timeStepTextField.setEnabled(!inProgress);
    	rewindButton.setEnabled(!inProgress);
    	playButton.setEnabled(!inProgress);
    	fastForwardButton.setEnabled(!inProgress);
    	timeBox.setEnabled(!inProgress);
    	setTimeButton.setEnabled(!inProgress);
    	timeStepLabel.setEnabled(!inProgress);
    	lblEnterUtcTime.setEnabled(!inProgress);
    	playPanel.setEnabled(!inProgress);
    }

}
