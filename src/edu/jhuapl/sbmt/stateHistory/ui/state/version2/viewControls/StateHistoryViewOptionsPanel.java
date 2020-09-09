package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;

public class StateHistoryViewOptionsPanel extends JPanel implements ActionListener
{
	/**
	 *
	 */
	private JTextField viewInputAngle;

	/**
	 *
	 */
	private JButton btnResetCameraTo;

	/**
	 *
	 */
	private JButton setViewAngle;

	/**
	 *
	 */
	private JComboBox<RendererLookDirection> viewOptions;

	/**
	 *
	 */
	private JLabel lblSelectView;

	/**
	 *
	 */
	private JLabel lblVerticalFov;

	private JLabel availableFovsLabel;

	private Vector<String> fovs;

	private JCheckBox[] fovCheckboxes;

	private JPanel fovPanel = new JPanel();
	private JPanel fovPanela = new JPanel();
	private JPanel fovPanelb = new JPanel();

	private ItemListener checkboxItemListener;

	public StateHistoryViewOptionsPanel()
	{
		this.fovs = new Vector<String>();
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
		setBorder(new TitledBorder(null, "View Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Select view panel
		JPanel panel_2 = new JPanel();
		add(panel_2);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

		lblSelectView = new JLabel("Select View:");
		lblSelectView.setEnabled(false);
		panel_2.add(lblSelectView);

		viewOptions = new JComboBox<RendererLookDirection>();
		viewOptions.setEnabled(false);
		panel_2.add(viewOptions);
		panel_2.add(Box.createHorizontalGlue());

		Component horizontalStrut_4 = Box.createHorizontalStrut(50);
		panel_2.add(horizontalStrut_4);

		btnResetCameraTo = new JButton("Reset Camera to Nadir");
		btnResetCameraTo.setEnabled(false);
		btnResetCameraTo.setVisible(false);
		panel_2.add(btnResetCameraTo);

		// FOV panel
		add(updateFOVsPanel());
		fovPanel.setLayout(new BoxLayout(fovPanel, BoxLayout.Y_AXIS));
	}

	private JPanel updateFOVsPanel()
	{
		fovPanel.removeAll();

		if (fovs == null || fovs.size() == 0)
		{
			fovPanela.removeAll();
			fovPanela.setLayout(new BoxLayout(fovPanela, BoxLayout.X_AXIS));
			lblVerticalFov = new JLabel("FOV (deg):");
			lblVerticalFov.setEnabled(false);
			fovPanela.add(lblVerticalFov);

			viewInputAngle = new JTextField();
			viewInputAngle.setMaximumSize(new Dimension(150, viewInputAngle.getPreferredSize().height));
			viewInputAngle.setText("30.0");
			viewInputAngle.setEnabled(false);
			fovPanela.add(viewInputAngle);
			viewInputAngle.setColumns(10);

			setViewAngle = new JButton("Set");
			setViewAngle.setEnabled(false);
			fovPanela.add(setViewAngle);
			fovPanela.add(Box.createHorizontalGlue());
			fovPanel.add(fovPanela);
		}
		else
		{
			fovPanelb.removeAll();
			fovCheckboxes = new JCheckBox[fovs.size()];
			fovPanelb.setLayout(new BoxLayout(fovPanelb, BoxLayout.Y_AXIS));
			fovPanelb.setBackground(Color.red);
			availableFovsLabel = new JLabel("Available FOVs");
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
			labelPanel.add(availableFovsLabel);
			labelPanel.add(Box.createHorizontalGlue());
			fovPanelb.add(labelPanel);
			int i=0;
			for (String fov : fovs)
			{
				JCheckBox fovCheckBox = new JCheckBox(fov);
				fovCheckBox.addItemListener(checkboxItemListener);
				JPanel checkboxPanel = new JPanel();
				checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.X_AXIS));
				checkboxPanel.add(fovCheckBox);
				checkboxPanel.add(Box.createHorizontalGlue());
				fovPanelb.add(checkboxPanel);
				fovCheckboxes[i++] = fovCheckBox;
			}
			fovPanel.add(fovPanelb);
		}
		return fovPanel;
	}

	public void setAvailableFOVs(Vector<String> fovs)
	{
		this.fovs = fovs;
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

	/**
	 * @return
	 */
	public JLabel getLblSelectView()
	{
		return lblSelectView;
	}

	/**
	 * @return
	 */
	public JLabel getLblVerticalFov()
	{
		return lblVerticalFov;
	}

	/**
	 * @return
	 */
	public JComboBox<RendererLookDirection> getViewOptions()
	{
		return viewOptions;
	}

	/**
	 * @return
	 */
	public JButton getBtnResetCameraTo()
	{
		return btnResetCameraTo;
	}

	/**
	 * @return
	 */
	public JButton getSetViewAngle()
	{
		return setViewAngle;
	}

	/**
	 * @return
	 */
	public JTextField getViewInputAngle()
	{
		return viewInputAngle;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		viewOptions.setEnabled(enabled);
    	btnResetCameraTo.setEnabled(enabled);
    	setViewAngle.setEnabled(enabled);
    	lblSelectView.setEnabled(enabled);
    	lblVerticalFov.setEnabled(enabled);
    	viewInputAngle.setEnabled(enabled);

		super.setEnabled(enabled);
	}

	/**
	 * @param checkboxItemListener the checkboxItemListener to set
	 */
	public void setCheckboxItemListener(ItemListener checkboxItemListener)
	{
		this.checkboxItemListener = checkboxItemListener;
	}
}
