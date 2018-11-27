package edu.jhuapl.sbmt.gui.dtm.ui.browse;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DtmBrowseControlPanel extends JPanel
{
	private JButton toggleAllDEMsButton;
	private JComboBox<String> datasetComboBox;
	private JButton toggleAllBoundariesButton;

	public DtmBrowseControlPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel_1 = new JPanel();
		add(panel_1);

		JLabel lblDataset = new JLabel("Dataset:");
		panel_1.add(lblDataset);

		datasetComboBox = new JComboBox<String>();
		panel_1.add(datasetComboBox);

		JPanel panel = new JPanel();
		add(panel);

		toggleAllDEMsButton = new JButton("Toggle All DEMs");
		panel.add(toggleAllDEMsButton);

		toggleAllBoundariesButton = new JButton("Toggle All Boundaries");
		panel.add(toggleAllBoundariesButton);
	}

	public JButton getToggleAllDEMsButton()
	{
		return toggleAllDEMsButton;
	}

	public JComboBox<String> getDatasetComboBox()
	{
		return datasetComboBox;
	}

	public void setDatasetComboBox(JComboBox<String> datasetComboBox)
	{
		this.datasetComboBox = datasetComboBox;
	}

	public JButton getToggleAllBoundariesButton()
	{
		return toggleAllBoundariesButton;
	}

}
