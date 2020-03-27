package edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;

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

	public StateHistoryViewOptionsPanel()
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
		JPanel panel_14 = new JPanel();
		add(panel_14);
		panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.X_AXIS));

		lblVerticalFov = new JLabel("FOV (deg):");
		lblVerticalFov.setEnabled(false);
		panel_14.add(lblVerticalFov);

		viewInputAngle = new JTextField();
		viewInputAngle.setMaximumSize(new Dimension(150, viewInputAngle.getPreferredSize().height));
		viewInputAngle.setText("30.0");
		viewInputAngle.setEnabled(false);
		panel_14.add(viewInputAngle);
		viewInputAngle.setColumns(10);

		setViewAngle = new JButton("Set");
		setViewAngle.setEnabled(false);
		panel_14.add(setViewAngle);
		panel_14.add(Box.createHorizontalGlue());

		JPanel panel_15 = new JPanel();
		add(panel_15);
		panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));
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
}
