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

/**
 * @author steelrj1
 *
 */
public class StateHistoryIntervalGenerationPanel extends JPanel
{
    /**
     * JLabel showing the available time to choose from
     */
    private JTextPane availableTimeLabel;

    /**
     * DateTimeSpinner to select the start time of the interval being generated
     */
    private DateTimeSpinner startTimeSpinner;

    /**
     * DateTimeSpinner to select the stop time of the interval being generated
     */
    private DateTimeSpinner stopTimeSpinner;

    /**
     * JButton that causes the interval to be generated
     */
    private JButton getIntervalButton;

    /**
     * Internal value for spinner size
     */
    private Dimension spinnerSize = new Dimension(400, 28);

	/**
	 * Constructor.
	 */
	public StateHistoryIntervalGenerationPanel()
	{
		initUI();
	}

	/**
	 * Initializes the user interface elements
	 */
	private void initUI()
	{
        setBorder(new TitledBorder(null, "Interval Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(null);
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Available Time Range");
        panel_1.add(lblNewLabel);

        Component horizontalStrut_1 = Box.createHorizontalStrut(40);
        panel_1.add(horizontalStrut_1);

        availableTimeLabel = new JTextPane();
        availableTimeLabel.setMaximumSize( new Dimension(Integer.MAX_VALUE, availableTimeLabel.getPreferredSize().height*2) );
        availableTimeLabel.setEditable(false);
        availableTimeLabel.setBackground(null);
        availableTimeLabel.setBorder(null);

        panel_1.add(availableTimeLabel);

        JPanel panel_2 = new JPanel();
        panel_2.setBorder(null);
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("Start Time:");
        panel_2.add(lblNewLabel_2);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_1);

        startTimeSpinner = new DateTimeSpinner();
        startTimeSpinner.setMinimumSize(spinnerSize);
        startTimeSpinner.setMaximumSize(spinnerSize);
        startTimeSpinner.setPreferredSize(spinnerSize);
        panel_2.add(startTimeSpinner);

        JPanel panel_3 = new JPanel();
        panel_3.setBorder(null);
        add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        JLabel lblNewLabel_3 = new JLabel("Stop Time:");
        panel_3.add(lblNewLabel_3);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue_2);

        stopTimeSpinner = new DateTimeSpinner();
        stopTimeSpinner.setMinimumSize(spinnerSize);
        stopTimeSpinner.setMaximumSize(spinnerSize);
        stopTimeSpinner.setPreferredSize(spinnerSize);
        panel_3.add(stopTimeSpinner);

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(null);
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        getIntervalButton = new JButton("Get Interval");
        panel_4.add(getIntervalButton);
	}

    /**
     * Returns the available time label
     * @return the available time label
     */
    public JTextPane getAvailableTimeLabel()
    {
        return availableTimeLabel;
    }

    /**
     * Returns the start time spinner
     * @return the start time spinner
     */
    public DateTimeSpinner getStartTimeSpinner()
    {
        return startTimeSpinner;
    }

    /**
     * Returns the stop time spinner
     * @return the stop time spinner
     */
    public DateTimeSpinner getStopTimeSpinner()
    {
        return stopTimeSpinner;
    }

    /**
     * Returns the "Get Interval" button
     * @return the "Get Interval" button
     */
    public JButton getGetIntervalButton()
    {
        return getIntervalButton;
    }
}