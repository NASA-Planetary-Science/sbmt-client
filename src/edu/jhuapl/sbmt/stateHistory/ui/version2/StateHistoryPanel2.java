package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.awt.BorderLayout;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable;

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
    private JTextField viewInputAngle;
    private JScrollPane tableScrollPane;
    private JPanel panel_9;
    private JComboBox viewOptions;
    private JButton btnResetCameraTo;
    private JButton saveAnimationButton;
    private JButton setViewAngle;
    private JSlider spacecraftSlider;
    private JCheckBox showSpacecraftMarker;
    private JSlider sunSlider;
    private JCheckBox showSunPointer;
    private JSlider earthSlider;
    private JCheckBox showEarthPointer;
    private JCheckBox showLighting;
    private JComboBox distanceOptions;
    private JCheckBox showSpacecraft;
    private JLabel earthText;
    private JLabel sunText;
    private JLabel spacecraftText;
    private JLabel lblSelectView;
    private JLabel lblVerticalFov;
    private JPanel intervalSelectionPanel;


    /**
     * Create the panel.
     */
    public StateHistoryPanel2()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        JPanel panel_18 = new JPanel();
        scrollPane.setViewportView(panel_18);
        panel_18.setLayout(new BoxLayout(panel_18, BoxLayout.Y_AXIS));

        JPanel timeControlPanel = new JPanel();
        panel_18.add(timeControlPanel);
        timeControlPanel.setBorder(new TitledBorder(
                new EtchedBorder(EtchedBorder.LOWERED, null, null),
                "Time Controls", TitledBorder.LEADING, TitledBorder.TOP, null,
                new Color(0, 0, 0)));
        timeControlPanel
                .setLayout(new BoxLayout(timeControlPanel, BoxLayout.Y_AXIS));

        JPanel intervalGenerationPanel = new JPanel();
        intervalGenerationPanel
                .setBorder(new TitledBorder(null, "Interval Generation",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
        timeControlPanel.add(intervalGenerationPanel);
        intervalGenerationPanel.setLayout(
                new BoxLayout(intervalGenerationPanel, BoxLayout.Y_AXIS));

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(null);
        intervalGenerationPanel.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Available Time Range");
        panel_4.add(lblNewLabel);

        Component horizontalStrut_1 = Box.createHorizontalStrut(40);
        panel_4.add(horizontalStrut_1);

        availableTimeLabel = new JTextPane();
        availableTimeLabel.setMaximumSize( new Dimension(Integer.MAX_VALUE, availableTimeLabel.getPreferredSize().height*2) );

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
        // startTimeSpinner.setEditor(new
        // javax.swing.JSpinner.DateEditor(startTimeSpinner, "yyyy-MMM-dd
        // HH:mm:ss.SSS"));
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
        // stopTimeSpinner.setEditor(new DateEditor(stopTimeSpinner,
        // "yyyy-MMM-dd HH:mm:ss.SSS"));
        stopTimeSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        stopTimeSpinner.setPreferredSize(new java.awt.Dimension(200, 28));
        panel_6.add(stopTimeSpinner);

        JPanel panel_7 = new JPanel();
        panel_7.setBorder(null);
        intervalGenerationPanel.add(panel_7);
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        getIntervalButton = new JButton("Get Interval");
        panel_7.add(getIntervalButton);

        Component verticalGlue_1 = Box.createVerticalGlue();
        timeControlPanel.add(verticalGlue_1);

        intervalSelectionPanel = new JPanel();
        intervalSelectionPanel
                .setBorder(new TitledBorder(null, "Interval Selection",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
        timeControlPanel.add(intervalSelectionPanel);
        intervalSelectionPanel.setLayout(
                new BoxLayout(intervalSelectionPanel, BoxLayout.Y_AXIS));

        panel_9 = new JPanel();
        intervalSelectionPanel.add(panel_9);
        panel_9.setLayout(new BorderLayout(0, 0));

        //// tableScrollPane = new JScrollPane(table);
        //// tableScrollPane.setPreferredSize(new Dimension(10000, 10000));
        // panel_9.add(tableScrollPane);
        //
        //// table = new TimeInterval();
        // tableScrollPane.setViewportView(table);

        JPanel panel_8 = new JPanel();
        intervalSelectionPanel.add(panel_8);
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        loadButton = new JButton("Load...");
        panel_8.add(loadButton);

        saveButton = new JButton("Save...");
        panel_8.add(saveButton);

        removeButton = new JButton("Remove Selected");
        panel_8.add(removeButton);

        Component verticalGlue = Box.createVerticalGlue();
        timeControlPanel.add(verticalGlue);

        JPanel intervalPlaybackPanel = new JPanel();
        intervalPlaybackPanel
                .setBorder(new TitledBorder(null, "Interval Playback",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
        timeControlPanel.add(intervalPlaybackPanel);
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

        Component verticalStrut = Box.createVerticalStrut(10);
        panel_18.add(verticalStrut);

        viewControlPanel = new JPanel();
        panel_18.add(viewControlPanel);
        viewControlPanel.setBorder(new TitledBorder(null, "View Controls",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        viewControlPanel
                .setLayout(new BoxLayout(viewControlPanel, BoxLayout.Y_AXIS));

        JPanel panel_2 = new JPanel();
        viewControlPanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        lblSelectView = new JLabel("Select View:");
        lblSelectView.setEnabled(false);
        panel_2.add(lblSelectView);

        viewOptions = new JComboBox();
        viewOptions.setEnabled(false);
        panel_2.add(viewOptions);

        Component horizontalStrut_4 = Box.createHorizontalStrut(50);
        panel_2.add(horizontalStrut_4);

        btnResetCameraTo = new JButton("Reset Camera to Nadir");
        btnResetCameraTo.setEnabled(false);
        btnResetCameraTo.setVisible(false);
        panel_2.add(btnResetCameraTo);

        JPanel panel_3 = new JPanel();
        viewControlPanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        showSpacecraft = new JCheckBox("Show Spacecraft");
        showSpacecraft.setEnabled(false);
        panel_3.add(showSpacecraft);

        Component horizontalStrut = Box.createHorizontalStrut(100);
        panel_3.add(horizontalStrut);

        distanceOptions = new JComboBox();
        distanceOptions.setEnabled(false);
        panel_3.add(distanceOptions);

        JPanel panel_10 = new JPanel();
        viewControlPanel.add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        showLighting = new JCheckBox("Show Lighting");
        showLighting.setEnabled(false);
        panel_10.add(showLighting);

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        panel_10.add(horizontalGlue_3);

        JPanel panel_11 = new JPanel();
        viewControlPanel.add(panel_11);
        panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));

        showEarthPointer = new JCheckBox("Show Earth Pointer");
        showEarthPointer.setEnabled(false);
        panel_11.add(showEarthPointer);

        Component horizontalStrut_5 = Box.createHorizontalStrut(40);
        panel_11.add(horizontalStrut_5);

        earthText = new JLabel("Resize:");
        earthText.setEnabled(false);
        panel_11.add(earthText);

        earthSlider = new JSlider();
        earthSlider.setEnabled(false);
        panel_11.add(earthSlider);

        JPanel panel_12 = new JPanel();
        viewControlPanel.add(panel_12);
        panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));

        showSunPointer = new JCheckBox("Show Sun Pointer");
        showSunPointer.setEnabled(false);
        panel_12.add(showSunPointer);

        Component horizontalStrut_2 = Box.createHorizontalStrut(50);
        panel_12.add(horizontalStrut_2);

        sunText = new JLabel("Resize:");
        sunText.setEnabled(false);
        panel_12.add(sunText);

        sunSlider = new JSlider();
        sunSlider.setEnabled(false);
        panel_12.add(sunSlider);

        JPanel panel_13 = new JPanel();
        viewControlPanel.add(panel_13);
        panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));

        showSpacecraftMarker = new JCheckBox("Show S/C Pointer");
        showSpacecraftMarker.setEnabled(false);
        panel_13.add(showSpacecraftMarker);

        Component horizontalStrut_3 = Box.createHorizontalStrut(50);
        panel_13.add(horizontalStrut_3);

        spacecraftText = new JLabel("Resize:");
        spacecraftText.setEnabled(false);
        panel_13.add(spacecraftText);

        spacecraftSlider = new JSlider();
        spacecraftSlider.setEnabled(false);
        panel_13.add(spacecraftSlider);

        JPanel panel_14 = new JPanel();
        viewControlPanel.add(panel_14);
        panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.X_AXIS));

        lblVerticalFov = new JLabel("Vertical Field of View (deg):");
        lblVerticalFov.setEnabled(false);
        panel_14.add(lblVerticalFov);

        viewInputAngle = new JTextField();
        viewInputAngle.setMaximumSize( new Dimension(Integer.MAX_VALUE, viewInputAngle.getPreferredSize().height) );

        viewInputAngle.setEnabled(false);
        panel_14.add(viewInputAngle);
        viewInputAngle.setColumns(10);

        setViewAngle = new JButton("Set");
        setViewAngle.setEnabled(false);
        panel_14.add(setViewAngle);

        JPanel panel_15 = new JPanel();
        viewControlPanel.add(panel_15);
        panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));

        JPanel panel_16 = new JPanel();
        panel_15.add(panel_16);

        saveAnimationButton = new JButton("Save Movie Frames");
        saveAnimationButton.setEnabled(false);
        panel_16.add(saveAnimationButton);

    }

    public JTextPane getAvailableTimeLabel()
    {
        return availableTimeLabel;
    }

    public JSpinner getStartTimeSpinner()
    {
        return startTimeSpinner;
    }

    public JSpinner getStopTimeSpinner()
    {
        return stopTimeSpinner;
    }

    public JButton getGetIntervalButton()
    {
        return getIntervalButton;
    }

    public JButton getLoadButton()
    {
        return loadButton;
    }

    public JButton getSaveButton()
    {
        return saveButton;
    }

    public JButton getRemoveButton()
    {
        return removeButton;
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

    public JPanel getViewControlPanel()
    {
        return viewControlPanel;
    }

    public TimeIntervalTable getTable()
    {
        return table;
    }

    public void setTable(TimeIntervalTable table)
    {
        this.table = table;
//        this.table.setPreferredSize(new Dimension(intervalSelectionPanel.getWidth(), table.getHeight()));
//        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        this.table.setPreferredScrollableViewportSize(table.getPreferredSize());
        this.table.setFillsViewportHeight(true);
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(150, 150));
        tableScrollPane.setMaximumSize(new Dimension(150, 150));
//        tableScrollPane.setPreferredSize(new Dimension(intervalSelectionPanel.getWidth(), tableScrollPane.getHeight()));
        panel_9.add(tableScrollPane, BorderLayout.CENTER);

//        tableScrollPane.setViewportView(table);
    }

    public JScrollPane getTableScrollPane()
    {
        return tableScrollPane;
    }

    public JPanel getPanel_9()
    {
        return panel_9;
    }

    public JComboBox getViewOptions()
    {
        return viewOptions;
    }

    public JButton getBtnResetCameraTo()
    {
        return btnResetCameraTo;
    }

    public JButton getSaveAnimationButton()
    {
        return saveAnimationButton;
    }

    public JButton getSetViewAngle()
    {
        return setViewAngle;
    }

    public JTextField getViewInputAngle()
    {
        return viewInputAngle;
    }

    public JSlider getSpacecraftSlider()
    {
        return spacecraftSlider;
    }

    public JCheckBox getShowSpacecraftMarker()
    {
        return showSpacecraftMarker;
    }

    public JSlider getSunSlider()
    {
        return sunSlider;
    }

    public JCheckBox getShowSunPointer()
    {
        return showSunPointer;
    }

    public JSlider getEarthSlider()
    {
        return earthSlider;
    }

    public JCheckBox getShowEarthPointer()
    {
        return showEarthPointer;
    }

    public JCheckBox getShowLighting()
    {
        return showLighting;
    }

    public JComboBox getDistanceOptions()
    {
        return distanceOptions;
    }

    public JCheckBox getShowSpacecraft()
    {
        return showSpacecraft;
    }

    public JLabel getEarthText()
    {
        return earthText;
    }

    public JLabel getSunText()
    {
        return sunText;
    }

    public JLabel getSpacecraftText()
    {
        return spacecraftText;
    }

    public JLabel getLblSelectView()
    {
        return lblSelectView;
    }

    public JLabel getLblVerticalFov()
    {
        return lblVerticalFov;
    }
    public JPanel getIntervalSelectionPanel() {
        return intervalSelectionPanel;
    }
}
