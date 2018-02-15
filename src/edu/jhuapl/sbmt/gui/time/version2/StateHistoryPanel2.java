package edu.jhuapl.sbmt.gui.time.version2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.gui.time.TimeIntervalTable;

public class StateHistoryPanel2 extends JPanel
{
    private TimeIntervalTable table;
    private JTextPane availableTimeLabel;
    private JSpinner startTimeSpinner;
    private JSpinner stopTimeSpinner;
    private JButton getIntervalButton;
    private JButton loadButton;
    private JButton saveButton;
    private JButton removeButton;
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
    private JPanel viewControlPanel;


    /**
     * Create the panel.
     */
    public StateHistoryPanel2()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel timeControlPanel = new JPanel();
        timeControlPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Time Controls", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        add(timeControlPanel);
        timeControlPanel.setLayout(new BoxLayout(timeControlPanel, BoxLayout.Y_AXIS));

        JPanel intervalGenerationPanel = new JPanel();
        intervalGenerationPanel.setBorder(new TitledBorder(null, "Interval Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        timeControlPanel.add(intervalGenerationPanel);
        intervalGenerationPanel.setLayout(new BoxLayout(intervalGenerationPanel, BoxLayout.Y_AXIS));

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(null);
        intervalGenerationPanel.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Available Time Range");
        panel_4.add(lblNewLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        availableTimeLabel = new JTextPane();
        panel_4.add(availableTimeLabel);

        JPanel panel_5 = new JPanel();
        panel_5.setBorder(null);
        intervalGenerationPanel.add(panel_5);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("Start Time:");
        panel_5.add(lblNewLabel_2);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_5.add(horizontalGlue_1);

        startTimeSpinner = new JSpinner();
//        startTimeSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startTimeSpinner, "yyyy-MMM-dd HH:mm:ss.SSS"));
        startTimeSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startTimeSpinner.setPreferredSize(new java.awt.Dimension(200, 28));
        panel_5.add(startTimeSpinner);

        JPanel panel_6 = new JPanel();
        panel_6.setBorder(null);
        intervalGenerationPanel.add(panel_6);
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        JLabel lblNewLabel_3 = new JLabel("Stop Time:");
        panel_6.add(lblNewLabel_3);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_6.add(horizontalGlue_2);

        stopTimeSpinner = new JSpinner();
//        stopTimeSpinner.setEditor(new DateEditor(stopTimeSpinner, "yyyy-MMM-dd HH:mm:ss.SSS"));
        stopTimeSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        stopTimeSpinner.setPreferredSize(new java.awt.Dimension(200, 28));
        panel_6.add(stopTimeSpinner);

        JPanel panel_7 = new JPanel();
        panel_7.setBorder(null);
        intervalGenerationPanel.add(panel_7);
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        getIntervalButton = new JButton("Get Interval");
        panel_7.add(getIntervalButton);

        JPanel intervalSelectionPanel = new JPanel();
        intervalSelectionPanel.setBorder(new TitledBorder(null, "Interval Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        timeControlPanel.add(intervalSelectionPanel);
        intervalSelectionPanel.setLayout(new BoxLayout(intervalSelectionPanel, BoxLayout.Y_AXIS));

        JPanel panel_9 = new JPanel();
        intervalSelectionPanel.add(panel_9);

        JScrollPane tableScrollPane = new JScrollPane(table);
//        tableScrollPane.setPreferredSize(new Dimension(10000, 10000));
        panel_9.add(tableScrollPane);

//        table = new TimeInterval();
        tableScrollPane.setViewportView(table);

        JPanel panel_8 = new JPanel();
        intervalSelectionPanel.add(panel_8);
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        loadButton = new JButton("Load...");
        panel_8.add(loadButton);

        saveButton = new JButton("Save...");
        panel_8.add(saveButton);

        removeButton = new JButton("Remove Selected");
        panel_8.add(removeButton);

        JPanel intervalPlaybackPanel = new JPanel();
        intervalPlaybackPanel.setBorder(new TitledBorder(null, "Interval Playback", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        timeControlPanel.add(intervalPlaybackPanel);
        intervalPlaybackPanel.setLayout(new BoxLayout(intervalPlaybackPanel, BoxLayout.Y_AXIS));

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

        slider = new JSlider();
        slider.setMinimum(sliderMin);
        slider.setMaximum(sliderMax);
        slider.setMinorTickSpacing(sliderMinorTick);
        slider.setMajorTickSpacing(sliderMajorTick);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setValue(defaultValue);

        panel.add(slider);

        JPanel panel_1 = new JPanel();
        intervalPlaybackPanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JLabel lblEnterUtcTime = new JLabel("Enter UTC Time:");
        panel_1.add(lblEnterUtcTime);

        timeBox = new JSpinner();
//        timeBox.setEditor(new DateEditor(timeBox, "yyyy-MMM-dd HH:mm:ss.SSS"));
        timeBox.setMinimumSize(new Dimension(36, 22));
        timeBox.setPreferredSize(new Dimension(200, 28));
        panel_1.add(timeBox);

        setTimeButton = new JButton("Set Time");
        panel_1.add(setTimeButton);

        viewControlPanel = new JPanel();
        viewControlPanel.setBorder(new TitledBorder(null, "View Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(viewControlPanel);

    }

    public JTextPane getAvailableTimeLabel() {
        return availableTimeLabel;
    }
    public JSpinner getStartTimeSpinner() {
        return startTimeSpinner;
    }
    public JSpinner getStopTimeSpinner() {
        return stopTimeSpinner;
    }
    public JButton getGetIntervalButton() {
        return getIntervalButton;
    }
    public JButton getLoadButton() {
        return loadButton;
    }
    public JButton getSaveButton() {
        return saveButton;
    }
    public JButton getRemoveButton() {
        return removeButton;
    }
    public JButton getRewindButton() {
        return rewindButton;
    }
    public JButton getPlayButton() {
        return playButton;
    }
    public JButton getFastForwardButton() {
        return fastForwardButton;
    }
    public JSlider getSlider() {
        return slider;
    }
    public JTextField getRateTextField() {
        return rateTextField;
    }
    public JSpinner getTimeBox() {
        return timeBox;
    }
    public JButton getSetTimeButton() {
        return setTimeButton;
    }
    public JPanel getViewControlPanel() {
        return viewControlPanel;
    }

    public TimeIntervalTable getTable()
    {
        return table;
    }

    public void setTable(TimeIntervalTable table)
    {
        this.table = table;
    }
}
