package edu.jhuapl.sbmt.stateHistory.controllers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class PlannedDataController
{
	JPanel panel;

	public PlannedDataController()
	{
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	}

	public void addChildController(IPlannedDataController child)
	{
		panel.add(child.getView());
	}

	public JPanel getView()
	{
		return panel;
	}
}
