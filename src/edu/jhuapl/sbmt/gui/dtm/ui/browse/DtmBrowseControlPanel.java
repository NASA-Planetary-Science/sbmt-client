package edu.jhuapl.sbmt.gui.dtm.ui.browse;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DtmBrowseControlPanel extends JPanel
{

	public DtmBrowseControlPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel_1 = new JPanel();
		add(panel_1);

		JLabel lblDataset = new JLabel("Dataset:");
		panel_1.add(lblDataset);

		JComboBox datasetComboBox = new JComboBox();
		panel_1.add(datasetComboBox);

		JPanel panel = new JPanel();
		add(panel);

		JButton btnMapAll = new JButton("Toggle All DEMs");
		panel.add(btnMapAll);

		JButton btnNewButton = new JButton("Toggle All Boundaries");
		panel.add(btnNewButton);
		// TODO Auto-generated constructor stub
	}

}
