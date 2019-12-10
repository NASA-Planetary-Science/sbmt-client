package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class StateHistoryViewControlsPanel extends JPanel
{

    private JComboBox viewOptions;
    private JButton btnResetCameraTo;
    private JButton saveAnimationButton;
    private JButton setViewAngle;
    private JSlider spacecraftSlider;
    private JCheckBox showSpacecraftMarker;
    private JSlider sunSlider;
    private JCheckBox showSunPointer;
    private JSlider earthSlider;
    private JCheckBox showEarthPointer;
    private JCheckBox showLighting;
    private JComboBox distanceOptions;
    private JCheckBox showSpacecraft;
    private JLabel earthText;
    private JLabel sunText;
    private JLabel spacecraftText;
    private JLabel lblSelectView;
    private JLabel lblVerticalFov;
    private JTextField viewInputAngle;

	public StateHistoryViewControlsPanel()
	{
		// TODO Auto-generated constructor stub
	}

	public StateHistoryViewControlsPanel(LayoutManager layout)
	{
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryViewControlsPanel(boolean isDoubleBuffered)
	{
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryViewControlsPanel(LayoutManager layout, boolean isDoubleBuffered)
	{
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private void initUI()
	{
//		viewControlPanel = new JPanel();
//        panel_18.add(viewControlPanel);
        setBorder(new TitledBorder(null, "View Controls",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        lblSelectView = new JLabel("Select View:");
        lblSelectView.setEnabled(false);
        panel_2.add(lblSelectView);

        viewOptions = new JComboBox();
        viewOptions.setEnabled(false);
        panel_2.add(viewOptions);

        Component horizontalStrut_4 = Box.createHorizontalStrut(50);
        panel_2.add(horizontalStrut_4);

        btnResetCameraTo = new JButton("Reset Camera to Nadir");
        btnResetCameraTo.setEnabled(false);
        btnResetCameraTo.setVisible(false);
        panel_2.add(btnResetCameraTo);

        JPanel panel_3 = new JPanel();
        add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        showSpacecraft = new JCheckBox("Show Spacecraft");
        showSpacecraft.setEnabled(false);
        panel_3.add(showSpacecraft);

        Component horizontalStrut = Box.createHorizontalStrut(100);
        panel_3.add(horizontalStrut);

        distanceOptions = new JComboBox();
        distanceOptions.setEnabled(false);
        panel_3.add(distanceOptions);

        JPanel panel_10 = new JPanel();
        add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        showLighting = new JCheckBox("Show Lighting");
        showLighting.setEnabled(false);
        panel_10.add(showLighting);

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        panel_10.add(horizontalGlue_3);

        JPanel panel_11 = new JPanel();
        add(panel_11);
        panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));

        showEarthPointer = new JCheckBox("Show Earth Pointer");
        showEarthPointer.setEnabled(false);
        panel_11.add(showEarthPointer);

        Component horizontalStrut_5 = Box.createHorizontalStrut(40);
        panel_11.add(horizontalStrut_5);

        earthText = new JLabel("Resize:");
        earthText.setEnabled(false);
        panel_11.add(earthText);

        earthSlider = new JSlider();
        earthSlider.setEnabled(false);
        panel_11.add(earthSlider);

        JPanel panel_12 = new JPanel();
        add(panel_12);
        panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));

        showSunPointer = new JCheckBox("Show Sun Pointer");
        showSunPointer.setEnabled(false);
        panel_12.add(showSunPointer);

        Component horizontalStrut_2 = Box.createHorizontalStrut(50);
        panel_12.add(horizontalStrut_2);

        sunText = new JLabel("Resize:");
        sunText.setEnabled(false);
        panel_12.add(sunText);

        sunSlider = new JSlider();
        sunSlider.setEnabled(false);
        panel_12.add(sunSlider);

        JPanel panel_13 = new JPanel();
        add(panel_13);
        panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));

        showSpacecraftMarker = new JCheckBox("Show S/C Pointer");
        showSpacecraftMarker.setEnabled(false);
        panel_13.add(showSpacecraftMarker);

        Component horizontalStrut_3 = Box.createHorizontalStrut(50);
        panel_13.add(horizontalStrut_3);

        spacecraftText = new JLabel("Resize:");
        spacecraftText.setEnabled(false);
        panel_13.add(spacecraftText);

        spacecraftSlider = new JSlider();
        spacecraftSlider.setEnabled(false);
        panel_13.add(spacecraftSlider);

        JPanel panel_14 = new JPanel();
        add(panel_14);
        panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.X_AXIS));

        lblVerticalFov = new JLabel("Vertical Field of View (deg):");
        lblVerticalFov.setEnabled(false);
        panel_14.add(lblVerticalFov);

        viewInputAngle = new JTextField();
        viewInputAngle.setMaximumSize( new Dimension(Integer.MAX_VALUE, viewInputAngle.getPreferredSize().height) );

        viewInputAngle.setEnabled(false);
        panel_14.add(viewInputAngle);
        viewInputAngle.setColumns(10);

        setViewAngle = new JButton("Set");
        setViewAngle.setEnabled(false);
        panel_14.add(setViewAngle);

        JPanel panel_15 = new JPanel();
        add(panel_15);
        panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));

        JPanel panel_16 = new JPanel();
        panel_15.add(panel_16);

        saveAnimationButton = new JButton("Save Movie Frames");
        saveAnimationButton.setEnabled(false);
        panel_16.add(saveAnimationButton);
	}


    public JComboBox getViewOptions()
    {
        return viewOptions;
    }

    public JButton getBtnResetCameraTo()
    {
        return btnResetCameraTo;
    }

    public JButton getSaveAnimationButton()
    {
        return saveAnimationButton;
    }

    public JButton getSetViewAngle()
    {
        return setViewAngle;
    }

    public JTextField getViewInputAngle()
    {
        return viewInputAngle;
    }

    public JSlider getSpacecraftSlider()
    {
        return spacecraftSlider;
    }

    public JCheckBox getShowSpacecraftMarker()
    {
        return showSpacecraftMarker;
    }

    public JSlider getSunSlider()
    {
        return sunSlider;
    }

    public JCheckBox getShowSunPointer()
    {
        return showSunPointer;
    }

    public JSlider getEarthSlider()
    {
        return earthSlider;
    }

    public JCheckBox getShowEarthPointer()
    {
        return showEarthPointer;
    }

    public JCheckBox getShowLighting()
    {
        return showLighting;
    }

    public JComboBox getDistanceOptions()
    {
        return distanceOptions;
    }

    public JCheckBox getShowSpacecraft()
    {
        return showSpacecraft;
    }

    public JLabel getEarthText()
    {
        return earthText;
    }

    public JLabel getSunText()
    {
        return sunText;
    }

    public JLabel getSpacecraftText()
    {
        return spacecraftText;
    }

    public JLabel getLblSelectView()
    {
        return lblSelectView;
    }

    public JLabel getLblVerticalFov()
    {
        return lblVerticalFov;
    }
}
