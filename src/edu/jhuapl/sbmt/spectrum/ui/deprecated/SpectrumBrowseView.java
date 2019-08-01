package edu.jhuapl.sbmt.spectrum.ui.deprecated;
//package edu.jhuapl.sbmt.spectrum.ui;
//
//import java.awt.Component;
//import java.awt.FlowLayout;
//
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JSpinner;
//import javax.swing.border.TitledBorder;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.pick.PickManager;
//import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
//
//public class SpectrumBrowseView extends SpectrumView
//{
//    private JScrollPane dataSourcesScrollPane;
//    private JScrollPane resultsScrollPanel;
//    private JComboBox numberOfFootprintsComboBox;
//    private JButton prevButton;
//    private JButton nextButton;
//    private JButton removeAllFootprintsButton;
//    private JCheckBox grayscaleCheckBox;
//    private JButton customFunctionsButton;
//    private JComboBox redComboBox;
//    private JSpinner redMinSpinner;
//    private JSpinner redMaxSpinner;
//    private JComboBox greenComboBox;
//    private JSpinner greenMinSpinner;
//    private JSpinner greenMaxSpinner;
//    private JComboBox blueComboBox;
//    private JSpinner blueMinSpinner;
//    private JSpinner blueMaxSpinner;
//    protected SpectrumPopupMenu spectrumPopupMenu;
//    private JLabel resultsLabel;
//    private JPanel resultsLabelPanel;
//    private JButton removeAllBoundariesButton;
//    private JPanel coloringDetailPanel;
//    private JPanel coloringPanel;
//    private JComboBox coloringComboBox;
//    private JPanel emissionAngleColoringPanel;
//    private JPanel rgbColoringPanel;
//    private JButton saveSpectraListButton;
//    private JButton loadSpectraListButton;
//    public SpectrumBrowseView(ModelManager modelManager, PickManager pickManager2, Renderer renderer, ISpectralInstrument instrument)
//    {
//
//        super(modelManager, pickManager2, renderer, instrument);
//
//        JPanel panel = getPanel();
//
//        JPanel dataSourcePanel = new JPanel();
//        dataSourcePanel.setBorder(new TitledBorder(null, "Data Sources", TitledBorder.LEADING, TitledBorder.TOP, null, null));
//        panel.add(dataSourcePanel);
//        dataSourcePanel.setLayout(new BoxLayout(dataSourcePanel, BoxLayout.Y_AXIS));
//
//        dataSourcesScrollPane = new JScrollPane();
//        dataSourcesScrollPane.setPreferredSize(new java.awt.Dimension(150, 150));
//        dataSourcePanel.add(dataSourcesScrollPane);
//        dataSourcePanel.add(getSearchButtonPanel());
//
//        JPanel resultsPanel = new JPanel();
//        resultsPanel.setBorder(new TitledBorder(null, "Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
//        panel.add(resultsPanel);
//        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
//
//        resultsLabelPanel = getResultsLabelPanel();
//        resultsLabelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
//        FlowLayout fl_resultsLabelPanel = (FlowLayout) resultsLabelPanel.getLayout();
//        fl_resultsLabelPanel.setAlignment(FlowLayout.LEFT);
//        resultsPanel.add(resultsLabelPanel);
//
//        resultsLabel = getResultsLabel();
//        resultsLabelPanel.add(resultsLabel);
//
//        resultsScrollPanel = getResultsScrollPanel();
//        resultsScrollPanel.setPreferredSize(new java.awt.Dimension(150, 150));
//        resultsPanel.add(resultsScrollPanel);
//
//        JPanel panel_8 = new JPanel();
//        resultsPanel.add(panel_8);
//        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));
//
//        JLabel lblNumberFootprints = new JLabel("Number Footprints:");
//        panel_8.add(lblNumberFootprints);
//        panel_8.add(getNumberOfFootprintsComboBox());
//        panel_8.add(getPrevButton());
//        panel_8.add(getNextButton());
//
//        JPanel panel_9 = new JPanel();
//        resultsPanel.add(panel_9);
//        panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.Y_AXIS));
//
//        JPanel panel_15 = new JPanel();
//        panel_9.add(panel_15);
//        panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));
//
//        panel_15.add(getSaveSpectraListButton());
//        panel_15.add(getLoadSpectraListButton());
//
//        JPanel panel_14 = new JPanel();
//        panel_9.add(panel_14);
//        panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.X_AXIS));
//
//        panel_14.add(getRemoveAllFootprintsButton());
//        panel_14.add(getRemoveAllBoundariesButton());
//
//        coloringPanel = getColoringPanel();
//        panel.add(coloringPanel);
//        coloringPanel.setLayout(new BoxLayout(coloringPanel, BoxLayout.Y_AXIS));
//
//
//        coloringPanel.add(getColoringComboBox());
//        coloringDetailPanel = getColoringDetailPanel();
//        coloringPanel.add(coloringDetailPanel);
//
//        emissionAngleColoringPanel = getEmissionAngleColoringPanel();
//        emissionAngleColoringPanel.setVisible(false);
//        coloringDetailPanel.setLayout(new BoxLayout(coloringDetailPanel, BoxLayout.Y_AXIS));
//        coloringDetailPanel.add(emissionAngleColoringPanel);
//        emissionAngleColoringPanel.setLayout(new BoxLayout(emissionAngleColoringPanel, BoxLayout.X_AXIS));
//
//        JLabel lblNewLabel_15 = new JLabel("Coloring by Avg Emission Angle (OREX Scalar Ramp, 0 to 90)");
//        emissionAngleColoringPanel.add(lblNewLabel_15);
//
//        rgbColoringPanel = getRgbColoringPanel();
//        coloringDetailPanel.add(rgbColoringPanel);
//        rgbColoringPanel.setLayout(new BoxLayout(rgbColoringPanel, BoxLayout.Y_AXIS));
//
//        JPanel panel_10 = new JPanel();
//        rgbColoringPanel.add(panel_10);
//        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));
//
//        grayscaleCheckBox = getGrayscaleCheckBox();
//        panel_10.add(grayscaleCheckBox);
//
//        Component horizontalGlue_2 = Box.createHorizontalGlue();
//        panel_10.add(horizontalGlue_2);
//
//        customFunctionsButton = getCustomFunctionsButton();
//        panel_10.add(customFunctionsButton);
//
//        JPanel panel_11 = new JPanel();
//        rgbColoringPanel.add(panel_11);
//        panel_11.setLayout(new BoxLayout(panel_11, BoxLayout.X_AXIS));
//
//        JLabel lblRed = new JLabel("Red");
//        panel_11.add(lblRed);
//
//        redComboBox = getRedComboBox();
//        panel_11.add(redComboBox);
//
//        JLabel lblMin = new JLabel("Min");
//        panel_11.add(lblMin);
//
//
//        redMinSpinner = getRedMinSpinner();
//        redMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
//        redMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
//        redMinSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        redMinSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
//        panel_11.add(redMinSpinner);
//
//        JLabel lblMax = new JLabel("Max");
//        panel_11.add(lblMax);
//
//        redMaxSpinner = getRedMaxSpinner();
//        redMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
//        redMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
//        redMaxSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        redMaxSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
//        panel_11.add(redMaxSpinner);
//
//        JPanel panel_12 = new JPanel();
//        rgbColoringPanel.add(panel_12);
//        panel_12.setLayout(new BoxLayout(panel_12, BoxLayout.X_AXIS));
//
//        JLabel lblGreen = new JLabel("Green");
//        panel_12.add(lblGreen);
//
//
//        greenComboBox = getGreenComboBox();
//        panel_12.add(greenComboBox);
//
//        JLabel lblMin_1 = new JLabel("Min");
//        panel_12.add(lblMin_1);
//
//        greenMinSpinner = getGreenMinSpinner();
//        greenMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
//        greenMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
//        greenMinSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        greenMinSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
//        panel_12.add(greenMinSpinner);
//
//        JLabel lblNewLabel_14 = new JLabel("Max");
//        panel_12.add(lblNewLabel_14);
//
//        greenMaxSpinner = getGreenMaxSpinner();
//        greenMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
//        greenMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
//        greenMaxSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        greenMaxSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
//        panel_12.add(greenMaxSpinner);
//
//        JPanel panel_13 = new JPanel();
//        rgbColoringPanel.add(panel_13);
//        panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));
//
//        JLabel lblBlue = new JLabel("Blue");
//        panel_13.add(lblBlue);
//
//        blueComboBox = getBlueComboBox();
//        panel_13.add(blueComboBox);
//
//        JLabel lblMin_2 = new JLabel("Min");
//        panel_13.add(lblMin_2);
//
//        blueMinSpinner = getBlueMinSpinner();
//        blueMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
//        blueMinSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
//        blueMinSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        blueMinSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
//        panel_13.add(blueMinSpinner);
//
//        JLabel lblMax_1 = new JLabel("Max");
//        panel_13.add(lblMax_1);
//
//        blueMaxSpinner = getBlueMaxSpinner();
//        blueMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.05d), null, null, Double.valueOf(0.01d)));
//        blueMaxSpinner.setPreferredSize(new java.awt.Dimension(100, 28));
//        blueMaxSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        blueMaxSpinner.setMaximumSize(new java.awt.Dimension(100, 22));
//        panel_13.add(blueMaxSpinner);
//    }
//    public JScrollPane getDataSourcesScrollPane()
//    {
//        return dataSourcesScrollPane;
//    }
//
//}
