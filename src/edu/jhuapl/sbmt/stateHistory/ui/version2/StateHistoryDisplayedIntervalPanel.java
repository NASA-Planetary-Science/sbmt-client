package edu.jhuapl.sbmt.stateHistory.ui.version2;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.gui.lidar.PercentIntervalChanger;

public class StateHistoryDisplayedIntervalPanel extends JPanel //implements ActionListener
{
	PercentIntervalChanger timeIntervalChanger;

	public StateHistoryDisplayedIntervalPanel()
	{
		initGUI();
	}

	private void initGUI()
	{
		setBorder(new TitledBorder(null, "Displayed Track Portion",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
		timeIntervalChanger = new PercentIntervalChanger("Displayed Trajectory Data");
//		timeIntervalChanger.addActionListener(this);
		add(timeIntervalChanger, "growx,spanx,wrap 0");
		timeIntervalChanger.setEnabled(false);
	}

//	@Override
//	public void actionPerformed(ActionEvent aEvent)
//	{
//		Object source = aEvent.getSource();
//
//		//use the interval changer low/high percentage values to cap the time window of the track
//
//	}

	public PercentIntervalChanger getTimeIntervalChanger()
	{
		return timeIntervalChanger;
	}

}
