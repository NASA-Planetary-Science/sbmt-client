package edu.jhuapl.sbmt.stateHistory.ui.state.version2;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.ui.state.StateHistoryPercentIntervalChanger;

/**
 * The panel that displays the controls for limiting what portion of the selected state history is shown
 *
 * @author steelrj1
 *
 */
public class StateHistoryDisplayedIntervalPanel extends JPanel //implements ActionListener
{
	/**
	 * Control that allows the user to restrict the portion of the state history that is shown
	 */
	private StateHistoryPercentIntervalChanger timeIntervalChanger;

	/**
	 * JLabels that display the current start and stop times
	 */
	private JLabel displayedStartTimeLabel, displayedStopTimeLabel;

	/**
	 * Internal panel that holds the displayedStartTimeLabel and displayedStopTimeLabel
	 */
	private JPanel labelPanel;

	/**
	 * Internal label that displays "Currently Displaying:" in the UI
	 */
	private JLabel currentlyDisplayingLabel;

	/**
	 *
	 */
	public StateHistoryDisplayedIntervalPanel()
	{
		initGUI();
	}

	/**
	 * Initializes the user interface elements
	 */
	private void initGUI()
	{
		//set a border and a vertical box layout
		setBorder(new TitledBorder(null, "Displayed Track Portion",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//initialize and setup the PercentIntervalChanger
		timeIntervalChanger = new StateHistoryPercentIntervalChanger("Displayed Trajectory Data");
		add(timeIntervalChanger.getSlider());
		timeIntervalChanger.setEnabled(false);

		//also display the displayed time window segment in labels below
		labelPanel = new JPanel();
		displayedStartTimeLabel = new JLabel("");
		displayedStopTimeLabel = new JLabel("");
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		currentlyDisplayingLabel = new JLabel("Currently displaying: ");
		labelPanel.add(currentlyDisplayingLabel);
		labelPanel.add(displayedStartTimeLabel);
		labelPanel.add(new JLabel(" - "));
		labelPanel.add(displayedStopTimeLabel);

		add(labelPanel);
	}

	/**
	 * Returns the percent interval changer
	 * @return the percent interval changer
	 */
	public StateHistoryPercentIntervalChanger getTimeIntervalChanger()
	{
		return timeIntervalChanger;
	}

	/**
	 * Returns the start time label
	 * @return the start time label
	 */
	public JLabel getDisplayedStartTimeLabel()
	{
		return displayedStartTimeLabel;
	}

	/**
	 * Returns the stop time label
	 * @return the stop time label
	 */
	public JLabel getDisplayedStopTimeLabel()
	{
		return displayedStopTimeLabel;
	}

	@Override
    public void setEnabled(boolean enabled)
    {
		timeIntervalChanger.setEnabled(enabled);
		displayedStartTimeLabel.setEnabled(enabled);
		displayedStopTimeLabel.setEnabled(enabled);
		currentlyDisplayingLabel.setEnabled(enabled);
		labelPanel.setEnabled(enabled);
		super.setEnabled(enabled);
    }

}
