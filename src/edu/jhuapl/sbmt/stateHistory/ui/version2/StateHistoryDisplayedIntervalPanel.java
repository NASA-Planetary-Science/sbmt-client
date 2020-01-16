package edu.jhuapl.sbmt.stateHistory.ui.version2;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.gui.lidar.PercentIntervalChanger;

public class StateHistoryDisplayedIntervalPanel extends JPanel //implements ActionListener
{
	private PercentIntervalChanger timeIntervalChanger;
	private JLabel displayedStartTimeLabel, displayedStopTimeLabel;
	private JPanel labelPanel;
	private JLabel currentlyDisplayingLabel;

	public StateHistoryDisplayedIntervalPanel()
	{
		initGUI();
	}

	private void initGUI()
	{
		setBorder(new TitledBorder(null, "Displayed Track Portion",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		timeIntervalChanger = new PercentIntervalChanger("Displayed Trajectory Data");
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

	public PercentIntervalChanger getTimeIntervalChanger()
	{
		return timeIntervalChanger;
	}

	public JLabel getDisplayedStartTimeLabel()
	{
		return displayedStartTimeLabel;
	}

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
