package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.StateHistoryDisplayItemShowLightingPanel;

public class StateHistoryViewOptionsPanel extends JPanel implements ActionListener
{
//	/**
//	 *
//	 */
//	private JTextField viewInputAngle;

//	/**
//	 *
//	 */
//	private JButton btnResetCameraTo;

//	/**
//	 *
//	 */
//	private JButton setViewAngle;

	/**
	 *
	 */
	private JComboBox<RendererLookDirection> viewOptions;

	/**
	 *
	 */
	private JLabel lblSelectView;

//	/**
//	 *
//	 */
//	private JLabel lblVerticalFov;

//	private ItemListener checkboxItemListener;

	StateHistoryDisplayItemShowLightingPanel showLightingPanel;

	private JPanel fovPanel = new JPanel();
//	private JPanel fovPanela = new JPanel();



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

		add(showLightingPanel = new StateHistoryDisplayItemShowLightingPanel());
		add(Box.createVerticalGlue());

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

//		btnResetCameraTo = new JButton("Reset Camera to Nadir");
//		btnResetCameraTo.setEnabled(false);
//		btnResetCameraTo.setVisible(false);
//		panel_2.add(btnResetCameraTo);

		// FOV panel
//		add(updateFOVsPanel());
		fovPanel.setLayout(new BoxLayout(fovPanel, BoxLayout.Y_AXIS));
	}

//	private JPanel updateFOVsPanel()
//	{
//		fovPanel.removeAll();
//
//		fovPanela.removeAll();
//		fovPanela.setLayout(new BoxLayout(fovPanela, BoxLayout.X_AXIS));
//		lblVerticalFov = new JLabel("FOV (deg):");
//		lblVerticalFov.setEnabled(false);
//		fovPanela.add(lblVerticalFov);
//
//		viewInputAngle = new JTextField();
//		viewInputAngle.setMaximumSize(new Dimension(150, viewInputAngle.getPreferredSize().height));
//		viewInputAngle.setText("30.0");
//		viewInputAngle.setEnabled(false);
//		fovPanela.add(viewInputAngle);
//		viewInputAngle.setColumns(10);
//
//		setViewAngle = new JButton("Set");
//		setViewAngle.setEnabled(false);
//		fovPanela.add(setViewAngle);
//		fovPanela.add(Box.createHorizontalGlue());
//		fovPanel.add(fovPanela);
//
//		return fovPanel;
//	}

	public StateHistoryDisplayItemShowLightingPanel getShowLightingPanel()
	{
		return showLightingPanel;
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

//	/**
//	 * @return
//	 */
//	public JLabel getLblVerticalFov()
//	{
//		return lblVerticalFov;
//	}

	/**
	 * @return
	 */
	public JComboBox<RendererLookDirection> getViewOptions()
	{
		return viewOptions;
	}

//	/**
//	 * @return
//	 */
//	public JButton getBtnResetCameraTo()
//	{
//		return btnResetCameraTo;
//	}
//
//	/**
//	 * @return
//	 */
//	public JButton getSetViewAngle()
//	{
//		return setViewAngle;
//	}
//
//	/**
//	 * @return
//	 */
//	public JTextField getViewInputAngle()
//	{
//		return viewInputAngle;
//	}

	@Override
	public void setEnabled(boolean enabled)
	{
		viewOptions.setEnabled(enabled);
//    	btnResetCameraTo.setEnabled(enabled);
//    	setViewAngle.setEnabled(enabled);
    	lblSelectView.setEnabled(enabled);
//    	lblVerticalFov.setEnabled(enabled);
//    	viewInputAngle.setEnabled(enabled);
		showLightingPanel.setEnabled(enabled);

		super.setEnabled(enabled);
	}

//	/**
//	 * @param checkboxItemListener the checkboxItemListener to set
//	 */
//	public void setCheckboxItemListener(ItemListener checkboxItemListener)
//	{
//		this.checkboxItemListener = checkboxItemListener;
//	}
}
