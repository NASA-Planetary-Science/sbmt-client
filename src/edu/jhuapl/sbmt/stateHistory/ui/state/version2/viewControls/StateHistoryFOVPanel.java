package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.viewOptions.table.ViewOptionsTableView;

public class StateHistoryFOVPanel extends JPanel
{
	private JCheckBox[] fovCheckboxes;

	private JPanel fovPanel = new JPanel();

	private ViewOptionsTableView tableView = null;

	public StateHistoryFOVPanel()
	{
		initUI();
	}

	private void initUI()
	{
		fovPanel.setBorder(new TitledBorder(null, "Available FOVs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		configureViewOptionsPanel();
	}

	public void clearTable()
	{
		this.tableView = null;
		updateFOVsPanel();
	}

	/**
	 *
	 */
	private void configureViewOptionsPanel()
	{
		// *********************
		// View Options Panel
		// *********************
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// FOV panel
		add(updateFOVsPanel());
		fovPanel.setLayout(new BoxLayout(fovPanel, BoxLayout.Y_AXIS));
	}

	private JPanel updateFOVsPanel()
	{
		fovPanel.removeAll();
		System.out.println("StateHistoryFOVPanel: updateFOVsPanel: table view is " + tableView);
		if (tableView != null)
		{
			fovPanel.add(tableView);
		}
		else
		{
			fovPanel.setLayout(new BoxLayout(fovPanel, BoxLayout.X_AXIS));
			fovPanel.setPreferredSize(new Dimension(fovPanel.getWidth(), 200));
			fovPanel.add(Box.createGlue());
			fovPanel.add(new JLabel("Please select a trajectory to show Fields of View"));
			fovPanel.add(Box.createGlue());
		}

		return fovPanel;
	}

	public void setTableView(ViewOptionsTableView tableView)
	{
		this.tableView = tableView;
	}

	public void setAvailableFOVs(Vector<String> fovs)
	{
		updateFOVsPanel();
	}

	public JCheckBox[] getFovCheckBoxes()
	{
		return fovCheckboxes;
	}
}
