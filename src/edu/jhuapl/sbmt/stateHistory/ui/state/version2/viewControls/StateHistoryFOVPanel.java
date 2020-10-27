package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.viewOptions.table.ViewOptionsTableView;

public class StateHistoryFOVPanel extends JPanel implements ActionListener
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
		configureViewOptionsPanel();
	}

	/**
	 *
	 */
	private void configureViewOptionsPanel()
	{
		// *********************
		// View Options Panel
		// *********************
//		setBorder(new TitledBorder(null, "View Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// FOV panel
		add(updateFOVsPanel());
		fovPanel.setLayout(new BoxLayout(fovPanel, BoxLayout.Y_AXIS));
	}

	private JPanel updateFOVsPanel()
	{
		fovPanel.removeAll();

		if (tableView != null)
		{
			fovPanel.add(tableView);
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

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(boolean enabled)
	{

		super.setEnabled(enabled);
	}
}
