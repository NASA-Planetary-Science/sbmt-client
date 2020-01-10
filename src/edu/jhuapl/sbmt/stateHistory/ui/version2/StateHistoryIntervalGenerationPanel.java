package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

public class StateHistoryIntervalGenerationPanel extends JPanel
{
    private JTextPane availableTimeLabel;
    private DateTimeSpinner startTimeSpinner;
    private DateTimeSpinner stopTimeSpinner;
    private JButton getIntervalButton;

	public StateHistoryIntervalGenerationPanel()
	{
		initUI();
	}

	private void initUI()
	{
//        JPanel panel_18 = new JPanel();
//        scrollPane.setViewportView(panel_18);
//        panel_18.setLayout(new BoxLayout(panel_18, BoxLayout.Y_AXIS));
//        JPanel timeControlPanel = new JPanel();
//		panel_18.add(timeControlPanel);


//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

//        JPanel intervalGenerationPanel = new JPanel();
        setBorder(new TitledBorder(null, "Interval Generation",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
//        add(intervalGenerationPanel);
        setLayout(
                new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(null);
        add(panel_4);
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
        add(panel_5);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("Start Time:");
        panel_5.add(lblNewLabel_2);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_5.add(horizontalGlue_1);

        startTimeSpinner = new DateTimeSpinner();
        // startTimeSpinner.setEditor(new
        // javax.swing.JSpinner.DateEditor(startTimeSpinner, "yyyy-MMM-dd
        // HH:mm:ss.SSS"));
        startTimeSpinner.setMinimumSize(new java.awt.Dimension(400, 28));
        startTimeSpinner.setMaximumSize(new java.awt.Dimension(400, 28));
        startTimeSpinner.setPreferredSize(new java.awt.Dimension(400, 28));
        panel_5.add(startTimeSpinner);

        JPanel panel_6 = new JPanel();
        panel_6.setBorder(null);
        add(panel_6);
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        JLabel lblNewLabel_3 = new JLabel("Stop Time:");
        panel_6.add(lblNewLabel_3);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_6.add(horizontalGlue_2);

        stopTimeSpinner = new DateTimeSpinner();
        // stopTimeSpinner.setEditor(new DateEditor(stopTimeSpinner,
        // "yyyy-MMM-dd HH:mm:ss.SSS"));
        stopTimeSpinner.setMinimumSize(new java.awt.Dimension(400, 28));
        stopTimeSpinner.setMaximumSize(new java.awt.Dimension(400, 28));
        stopTimeSpinner.setPreferredSize(new java.awt.Dimension(400, 28));
        panel_6.add(stopTimeSpinner);

        JPanel panel_7 = new JPanel();
        panel_7.setBorder(null);
        add(panel_7);
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        getIntervalButton = new JButton("Get Interval");
        panel_7.add(getIntervalButton);
	}

    public JTextPane getAvailableTimeLabel()
    {
        return availableTimeLabel;
    }

    public DateTimeSpinner getStartTimeSpinner()
    {
        return startTimeSpinner;
    }

    public DateTimeSpinner getStopTimeSpinner()
    {
        return stopTimeSpinner;
    }

    public JButton getGetIntervalButton()
    {
        return getIntervalButton;
    }

}
