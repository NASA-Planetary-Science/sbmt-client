package edu.jhuapl.sbmt.gui.spectrum.ui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.model.spectrum.coloring.SpectrumColoringStyle;

public class SpectrumColoringPanel extends JPanel
{
    private JCheckBox grayscaleCheckBox;
    private JButton customFunctionsButton;
    private JComboBox<String> redComboBox;
    private JSpinner redMinSpinner;
    private JSpinner redMaxSpinner;
    private JComboBox<String> greenComboBox;
    private JSpinner greenMinSpinner;
    private JSpinner greenMaxSpinner;
    private JComboBox<String> blueComboBox;
    private JSpinner blueMinSpinner;
    private JSpinner blueMaxSpinner;
    private JPanel coloringDetailPanel;
    private JPanel coloringPanel;
    private JComboBox<SpectrumColoringStyle> coloringComboBox;
    private JPanel emissionAngleColoringPanel;
    private JPanel rgbColoringPanel;

    public SpectrumColoringPanel()
    {
        init();
    }

    private void init()
    {
        coloringPanel.setBorder(new TitledBorder(null, "Coloring", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        coloringPanel.setLayout(new BoxLayout(coloringPanel, BoxLayout.Y_AXIS));

        coloringComboBox = new JComboBox<SpectrumColoringStyle>();
        coloringPanel.add(coloringComboBox);

        coloringDetailPanel = new JPanel();
        coloringPanel.add(coloringDetailPanel);

        emissionAngleColoringPanel = new JPanel();
        emissionAngleColoringPanel.setVisible(false);
        coloringDetailPanel.setLayout(new BoxLayout(coloringDetailPanel, BoxLayout.Y_AXIS));
        coloringDetailPanel.add(emissionAngleColoringPanel);
        emissionAngleColoringPanel.setLayout(new BoxLayout(emissionAngleColoringPanel, BoxLayout.X_AXIS));

        JLabel lblNewLabel_15 = new JLabel("Coloring by Avg Emission Angle (OREX Scalar Ramp, 0 to 90)");
        emissionAngleColoringPanel.add(lblNewLabel_15);

        rgbColoringPanel = new JPanel();
        coloringDetailPanel.add(rgbColoringPanel);
        rgbColoringPanel.setLayout(new BoxLayout(rgbColoringPanel, BoxLayout.Y_AXIS));

        JPanel panel_10 = new JPanel();
        rgbColoringPanel.add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        grayscaleCheckBox = new JCheckBox("Grayscale");
        panel_10.add(grayscaleCheckBox);

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_10.add(horizontalGlue_2);

        customFunctionsButton = new JButton("Custom Formulas");
        panel_10.add(customFunctionsButton);

        JPanel panel_11 = new JPanel();
        rgbColoringPanel.add(panel_11);
        panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));

        JLabel lblRed = new JLabel("Red");
        panel_11.add(lblRed);

        redComboBox = new JComboBox<String>();
        panel_11.add(redComboBox);

        JLabel lblMin = new JLabel("Min");
        panel_11.add(lblMin);

        redMinSpinner = new JSpinner();
        redMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        redMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        redMinSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        redMinSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
        panel_11.add(redMinSpinner);

        JLabel lblMax = new JLabel("Max");
        panel_11.add(lblMax);

        redMaxSpinner = new JSpinner();
        redMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        redMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        redMaxSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        redMaxSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
        panel_11.add(redMaxSpinner);

        JPanel panel_12 = new JPanel();
        rgbColoringPanel.add(panel_12);
        panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));

        JLabel lblGreen = new JLabel("Green");
        panel_12.add(lblGreen);

        greenComboBox = new JComboBox<String>();
        panel_12.add(greenComboBox);

        JLabel lblMin_1 = new JLabel("Min");
        panel_12.add(lblMin_1);

        greenMinSpinner = new JSpinner();
        greenMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        greenMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        greenMinSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        greenMinSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
        panel_12.add(greenMinSpinner);

        JLabel lblNewLabel_14 = new JLabel("Max");
        panel_12.add(lblNewLabel_14);

        greenMaxSpinner = new JSpinner();
        greenMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        greenMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        greenMaxSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        greenMaxSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
        panel_12.add(greenMaxSpinner);

        JPanel panel_13 = new JPanel();
        rgbColoringPanel.add(panel_13);
        panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));

        JLabel lblBlue = new JLabel("Blue");
        panel_13.add(lblBlue);

        blueComboBox = new JComboBox<String>();
        panel_13.add(blueComboBox);

        JLabel lblMin_2 = new JLabel("Min");
        panel_13.add(lblMin_2);

        blueMinSpinner = new JSpinner();
        blueMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        blueMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        blueMinSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        blueMinSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
        panel_13.add(blueMinSpinner);

        JLabel lblMax_1 = new JLabel("Max");
        panel_13.add(lblMax_1);

        blueMaxSpinner = new JSpinner();
        blueMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
        blueMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
        blueMaxSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        blueMaxSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
        panel_13.add(blueMaxSpinner);
    }

    public JCheckBox getGrayscaleCheckBox()
    {
        return grayscaleCheckBox;
    }

    public JButton getCustomFunctionsButton()
    {
        return customFunctionsButton;
    }

    public JComboBox<String> getRedComboBox()
    {
        return redComboBox;
    }

    public JSpinner getRedMinSpinner()
    {
        return redMinSpinner;
    }

    public JSpinner getRedMaxSpinner()
    {
        return redMaxSpinner;
    }

    public JComboBox<String> getGreenComboBox()
    {
        return greenComboBox;
    }

    public JSpinner getGreenMinSpinner()
    {
        return greenMinSpinner;
    }

    public JSpinner getGreenMaxSpinner()
    {
        return greenMaxSpinner;
    }

    public JComboBox<String> getBlueComboBox()
    {
        return blueComboBox;
    }

    public JSpinner getBlueMinSpinner()
    {
        return blueMinSpinner;
    }

    public JSpinner getBlueMaxSpinner()
    {
        return blueMaxSpinner;
    }

    public JPanel getColoringDetailPanel()
    {
        return coloringDetailPanel;
    }

    public JPanel getColoringPanel()
    {
        return coloringPanel;
    }

    public JComboBox<SpectrumColoringStyle> getColoringComboBox()
    {
        return coloringComboBox;
    }

    public JPanel getEmissionAngleColoringPanel()
    {
        return emissionAngleColoringPanel;
    }

    public JPanel getRgbColoringPanel()
    {
        return rgbColoringPanel;
    }
}
