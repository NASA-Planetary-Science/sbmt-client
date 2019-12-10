package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
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

public class StateHistoryIntervalPlaybackPanel extends JPanel
{
    private JTextField rateTextField;
    private JButton rewindButton;
    private JButton playButton;
    private JButton fastForwardButton;
    private JSlider slider;
    private int sliderMin = 0;
    private int sliderMax = 900;
    private int sliderMinorTick = 30;
    private int sliderMajorTick = 150;
    private int defaultValue = 0; // 15;
    private JSpinner timeBox;
    private JButton setTimeButton;


	public StateHistoryIntervalPlaybackPanel()
	{
		// TODO Auto-generated constructor stub
	}

	public StateHistoryIntervalPlaybackPanel(LayoutManager layout)
	{
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryIntervalPlaybackPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryIntervalPlaybackPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private void initUI()
	{
		JPanel intervalPlaybackPanel = new JPanel();
        intervalPlaybackPanel
                .setBorder(new TitledBorder(null, "Interval Playback",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
//        timeControlPanel.add(intervalPlaybackPanel);
        intervalPlaybackPanel.setLayout(
                new BoxLayout(intervalPlaybackPanel, BoxLayout.Y_AXIS));

        JPanel panel_17 = new JPanel();
        intervalPlaybackPanel.add(panel_17);
        panel_17.setLayout(new BoxLayout(panel_17, BoxLayout.X_AXIS));

        slider = new JSlider();
        panel_17.add(slider);
        slider.setMinimum(sliderMin);
        slider.setMaximum(sliderMax);
        slider.setMinorTickSpacing(sliderMinorTick);
        slider.setMajorTickSpacing(sliderMajorTick);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setValue(defaultValue);

        JPanel panel = new JPanel();
        intervalPlaybackPanel.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel lblNewLabel_1 = new JLabel("Play Speed:");
        panel.add(lblNewLabel_1);

        Image questionMark;
        try
        {
            questionMark = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/questionMark.png"));
            Icon question = new ImageIcon(questionMark);
            JLabel questionRate = new JLabel(question);
            questionRate.setToolTipText("<html>The speed of the animation is X times <br>faster than 1 second of real time. Ex. <br>60 means 1 minute of the interval is <br>traveled per second</html>");

            panel.add(questionRate);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        rateTextField = new JTextField("60.0    ");
        rateTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, rateTextField.getPreferredSize().height) );
        panel.add(rateTextField);
        rateTextField.setColumns(10);

        rewindButton = new JButton("");
        try
        {
            Image rewind = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/RewindButton.png"));
            Icon rewindIcon = new ImageIcon(rewind);
            rewindButton.setIcon(rewindIcon);
        }
        catch (Exception e)
        {
            rewindButton.setText("Rewind");
        }
        panel.add(rewindButton);

        playButton = new JButton("");
        try
        {
            Image play = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/PlayButton.png"));
            play.getScaledInstance(10, 10, Image.SCALE_DEFAULT);
            Icon playIcon = new ImageIcon(play);
            playButton.setIcon(playIcon);
        }catch (Exception e)
        {
            playButton.setText("Play");
        }
        panel.add(playButton);

        fastForwardButton = new JButton("");
        try
        {
            Image fast = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/FastforwardButton.png"));
            Icon fastforwardIcon = new ImageIcon(fast);
            fastForwardButton.setIcon(fastforwardIcon);
        }
        catch (Exception e)
        {
              fastForwardButton.setText("Fast Forward");
        }
        panel.add(fastForwardButton);

        JPanel panel_1 = new JPanel();
        intervalPlaybackPanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JLabel lblEnterUtcTime = new JLabel("Enter UTC Time:");
        panel_1.add(lblEnterUtcTime);

        timeBox = new JSpinner();
        // timeBox.setEditor(new DateEditor(timeBox, "yyyy-MMM-dd
        // HH:mm:ss.SSS"));
        timeBox.setMinimumSize(new Dimension(36, 22));
        timeBox.setPreferredSize(new Dimension(200, 28));
        panel_1.add(timeBox);

        setTimeButton = new JButton("Set Time");
        panel_1.add(setTimeButton);

	}


    public JButton getRewindButton()
    {
        return rewindButton;
    }

    public JButton getPlayButton()
    {
        return playButton;
    }

    public JButton getFastForwardButton()
    {
        return fastForwardButton;
    }

    public JSlider getSlider()
    {
        return slider;
    }

    public JTextField getRateTextField()
    {
        return rateTextField;
    }

    public JSpinner getTimeBox()
    {
        return timeBox;
    }

    public JButton getSetTimeButton()
    {
        return setTimeButton;
    }

}
