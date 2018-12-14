package edu.jhuapl.sbmt.gui.dtm.ui.browse;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DtmBrowseControlPanel extends JPanel
{
	private JButton unmapAllDEMsButton;
	private JComboBox<String> datasetComboBox;
	private JButton toggleAllBoundariesButton;
	private JButton removeAllBoundariesButton;

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

		unmapAllDEMsButton = new JButton("Unmap All");
		unmapAllDEMsButton.setEnabled(false);
		panel.add(unmapAllDEMsButton);

		toggleAllBoundariesButton = new JButton("Toggle Boundaries for Mapped DTMs");
		toggleAllBoundariesButton.setEnabled(false);
		panel.add(toggleAllBoundariesButton);

		removeAllBoundariesButton = new JButton("Remove All");
		removeAllBoundariesButton.setEnabled(false);
		panel.add(removeAllBoundariesButton);
	}

	public JButton getUnmapAllDEMsButton()
	{
		return unmapAllDEMsButton;
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

	public JButton getRemoveAllBoundariesButton()
	{
		return removeAllBoundariesButton;
	}

}
