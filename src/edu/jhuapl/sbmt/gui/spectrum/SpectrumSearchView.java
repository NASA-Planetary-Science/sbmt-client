package edu.jhuapl.sbmt.gui.spectrum;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

public class SpectrumSearchView extends JPanel
{
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JFormattedTextField fromDistanceTextField;
    private JFormattedTextField toDistanceTextField;
    private JFormattedTextField fromIncidenceTextField;
    private JFormattedTextField toIncidenceTextField;
    private JFormattedTextField fromEmissionTextField;
    private JFormattedTextField toEmissionTextField;
    private JFormattedTextField fromPhaseTextField;
    private JFormattedTextField toPhaseTextField;
    private JScrollPane dataSourcesScrollPane;
    private JButton selectRegionButton;
    private JButton clearRegionButton;
    private JButton submitButton;
    private JScrollPane resultsScrollPanel;
    private JComboBox numberOfFootprintsComboBox;
    private JButton prevButton;
    private JButton nextButton;
    private JButton removeAllFootprintsButton;
    private JCheckBox grayscaleCheckBox;
    private JButton customFunctionsButton;
    private JComboBox redComboBox;
    private JSpinner redMinSpinner;
    private JSpinner redMaxSpinner;
    private JComboBox greenComboBox;
    private JSpinner greenMinSpinner;
    private JSpinner greenMaxSpinner;
    private JComboBox blueComboBox;
    private JSpinner blueMinSpinner;
    private JSpinner blueMaxSpinner;
    private JList resultList;
    protected SpectrumPopupMenu spectrumPopupMenu;
    private JLabel resultsLabel;
    private JPanel resultsLabelPanel;
    private JButton removeAllBoundariesButton;
    private JPanel dbSearchPanel;
    private JPanel coloringDetailPanel;
    private JPanel coloringPanel;
    private JComboBox coloringComboBox;
    private JPanel emissionAngleColoringPanel;
    private JPanel rgbColoringPanel;
    private JButton saveSpectraListButton;
    private JButton loadSpectraListButton;

    public SpectrumSearchView()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        resultList = new JList();
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        JPanel panel = new JPanel();
        scrollPane.setViewportView(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        dbSearchPanel = new JPanel();
        dbSearchPanel.setBorder(new TitledBorder(null, "Search Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(dbSearchPanel);
        dbSearchPanel.setLayout(new BoxLayout(dbSearchPanel, BoxLayout.Y_AXIS));

        JPanel panel_1 = new JPanel();
        dbSearchPanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("Start Date:");
        panel_1.add(lblNewLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue);

        startSpinner = new JSpinner();
        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setPreferredSize(new java.awt.Dimension(200, 22));
        startSpinner.setMaximumSize(new java.awt.Dimension(200, 22));
        panel_1.add(startSpinner);

        JPanel panel_2 = new JPanel();
        dbSearchPanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        JLabel lblNewLabel_1 = new JLabel("End Date: ");
        panel_2.add(lblNewLabel_1);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_1);

        endSpinner = new JSpinner();
        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1132462800000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(200, 22));
        endSpinner.setMaximumSize(new java.awt.Dimension(200, 22));
        panel_2.add(endSpinner);

        Component verticalStrut = Box.createVerticalStrut(10);
        dbSearchPanel.add(verticalStrut);

        JPanel panel_3 = new JPanel();
        dbSearchPanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        JLabel lblNewLabel_2 = new JLabel("S/C Distance from");
        panel_3.add(lblNewLabel_2);

        fromDistanceTextField = new JFormattedTextField();
        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setPreferredSize(new Dimension(0, 22));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromDistanceTextField.getPreferredSize().height) );

        panel_3.add(fromDistanceTextField);

        JLabel lblNewLabel_3 = new JLabel("to");
        panel_3.add(lblNewLabel_3);

        toDistanceTextField = new JFormattedTextField();
        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("100");
        toDistanceTextField.setPreferredSize(new Dimension(0, 22));
        toDistanceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toDistanceTextField.getPreferredSize().height) );

        panel_3.add(toDistanceTextField);

        JLabel lblNewLabel_10 = new JLabel("km");
        panel_3.add(lblNewLabel_10);

        JPanel panel_4 = new JPanel();
        dbSearchPanel.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalStrut = Box.createHorizontalStrut(22);
        panel_4.add(horizontalStrut);

        JLabel lblNewLabel_4 = new JLabel("Incidence from");
        panel_4.add(lblNewLabel_4);

        fromIncidenceTextField = new JFormattedTextField();
        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new Dimension(0, 22));
        fromIncidenceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromIncidenceTextField.getPreferredSize().height) );

        panel_4.add(fromIncidenceTextField);

        JLabel lblNewLabel_5 = new JLabel("to");
        panel_4.add(lblNewLabel_5);

        toIncidenceTextField = new JFormattedTextField();
        toIncidenceTextField.setPreferredSize(new Dimension(0, 22));
        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toIncidenceTextField.getPreferredSize().height) );

        panel_4.add(toIncidenceTextField);

        JLabel lblNewLabel_11 = new JLabel("deg");
        panel_4.add(lblNewLabel_11);

        JPanel panel_5 = new JPanel();
        dbSearchPanel.add(panel_5);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

        Component horizontalStrut2 = Box.createHorizontalStrut(25);
        panel_5.add(horizontalStrut2);

        JLabel lblNewLabel_6 = new JLabel("Emission from");
        panel_5.add(lblNewLabel_6);

        fromEmissionTextField = new JFormattedTextField();
        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new Dimension(0, 22));
        fromEmissionTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromEmissionTextField.getPreferredSize().height) );

        panel_5.add(fromEmissionTextField);

        JLabel lblNewLabel_7 = new JLabel("to");
        panel_5.add(lblNewLabel_7);

        toEmissionTextField = new JFormattedTextField();
        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new Dimension(0, 22));
        toEmissionTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toEmissionTextField.getPreferredSize().height) );

        panel_5.add(toEmissionTextField);

        JLabel lblNewLabel_12 = new JLabel("deg");
        panel_5.add(lblNewLabel_12);



        JPanel panel_6 = new JPanel();
        dbSearchPanel.add(panel_6);
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        Component horizontalStrut3 = Box.createHorizontalStrut(45);
        panel_6.add(horizontalStrut3);

        JLabel lblNewLabel_8 = new JLabel("Phase from");
        panel_6.add(lblNewLabel_8);

        fromPhaseTextField = new JFormattedTextField();
        fromPhaseTextField.setPreferredSize(new Dimension(0, 22));
        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromPhaseTextField.getPreferredSize().height) );

        panel_6.add(fromPhaseTextField);

        JLabel lblNewLabel_9 = new JLabel("to");
        panel_6.add(lblNewLabel_9);

        toPhaseTextField = new JFormattedTextField();
        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new Dimension(0, 22));
        toPhaseTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toPhaseTextField.getPreferredSize().height) );

        panel_6.add(toPhaseTextField);

        JLabel lblNewLabel_13 = new JLabel("deg");
        panel_6.add(lblNewLabel_13);

        JPanel dataSourcePanel = new JPanel();
        dataSourcePanel.setBorder(new TitledBorder(null, "Data Sources", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(dataSourcePanel);
        dataSourcePanel.setLayout(new BoxLayout(dataSourcePanel, BoxLayout.Y_AXIS));

        dataSourcesScrollPane = new JScrollPane();
        dataSourcesScrollPane.setPreferredSize(new java.awt.Dimension(150, 150));
        dataSourcePanel.add(dataSourcesScrollPane);

        JPanel panel_7 = new JPanel();
        dataSourcePanel.add(panel_7);
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        selectRegionButton = new JButton("Select Region");
        panel_7.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        panel_7.add(clearRegionButton);

        submitButton = new JButton("Search");
        panel_7.add(submitButton);

        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(new TitledBorder(null, "Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(resultsPanel);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        resultsLabelPanel = new JPanel();
        resultsLabelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        FlowLayout fl_resultsLabelPanel = (FlowLayout) resultsLabelPanel.getLayout();
        fl_resultsLabelPanel.setAlignment(FlowLayout.LEFT);
        resultsPanel.add(resultsLabelPanel);

        resultsLabel = new JLabel("");
        resultsLabelPanel.add(resultsLabel);

        resultsScrollPanel = new JScrollPane();
        resultsScrollPanel.setPreferredSize(new java.awt.Dimension(150, 150));
        resultsPanel.add(resultsScrollPanel);

        JPanel panel_8 = new JPanel();
        resultsPanel.add(panel_8);
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        JLabel lblNumberFootprints = new JLabel("Number Footprints:");
        panel_8.add(lblNumberFootprints);

        numberOfFootprintsComboBox = new JComboBox();
        panel_8.add(numberOfFootprintsComboBox);

        prevButton = new JButton("<");
        panel_8.add(prevButton);

        nextButton = new JButton(">");
        panel_8.add(nextButton);

        JPanel panel_9 = new JPanel();
        resultsPanel.add(panel_9);
        panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.Y_AXIS));

        JPanel panel_15 = new JPanel();
        panel_9.add(panel_15);
        panel_15.setLayout(new BoxLayout(panel_15, BoxLayout.X_AXIS));

        saveSpectraListButton = new JButton("Save Spectra");
        panel_15.add(saveSpectraListButton);

        loadSpectraListButton = new JButton("Load Spectra");
        panel_15.add(loadSpectraListButton);

        JPanel panel_14 = new JPanel();
        panel_9.add(panel_14);
        panel_14.setLayout(new BoxLayout(panel_14, BoxLayout.X_AXIS));

        removeAllFootprintsButton = new JButton("Remove All Footprints");
        panel_14.add(removeAllFootprintsButton);

        removeAllBoundariesButton = new JButton("Remove All Boundaries");
        panel_14.add(removeAllBoundariesButton);

        coloringPanel = new JPanel();
        coloringPanel.setBorder(new TitledBorder(null, "Coloring", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(coloringPanel);
        coloringPanel.setLayout(new BoxLayout(coloringPanel, BoxLayout.Y_AXIS));

        coloringComboBox = new JComboBox();
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

        redComboBox = new JComboBox();
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

        greenComboBox = new JComboBox();
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

        blueComboBox = new JComboBox();
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
        // TODO Auto-generated constructor stub
    }


    public JSpinner getStartSpinner() {
        return startSpinner;
    }
    public JSpinner getEndSpinner() {
        return endSpinner;
    }
    public JFormattedTextField getFromDistanceTextField() {
        return fromDistanceTextField;
    }
    public JFormattedTextField getToDistanceTextField() {
        return toDistanceTextField;
    }
    public JFormattedTextField getFromIncidenceTextField() {
        return fromIncidenceTextField;
    }
    public JFormattedTextField getToIncidenceTextField() {
        return toIncidenceTextField;
    }
    public JFormattedTextField getFromEmissionTextField() {
        return fromEmissionTextField;
    }
    public JFormattedTextField getToEmissionTextField() {
        return toEmissionTextField;
    }
    public JFormattedTextField getFromPhaseTextField() {
        return fromPhaseTextField;
    }
    public JFormattedTextField getToPhaseTextField() {
        return toPhaseTextField;
    }
    public JScrollPane getDataSourcesScrollPane() {
        return dataSourcesScrollPane;
    }
    public JButton getSelectRegionButton() {
        return selectRegionButton;
    }
    public JButton getClearRegionButton() {
        return clearRegionButton;
    }
    public JButton getSubmitButton() {
        return submitButton;
    }
    public JScrollPane getResultsScrollPanel() {
        return resultsScrollPanel;
    }
    public JComboBox getNumberOfFootprintsComboBox() {
        return numberOfFootprintsComboBox;
    }
    public JButton getPrevButton() {
        return prevButton;
    }
    public JButton getNextButton() {
        return nextButton;
    }
    public JButton getRemoveAllFootprintsButton() {
        return removeAllFootprintsButton;
    }
    public JCheckBox getGrayscaleCheckBox() {
        return grayscaleCheckBox;
    }
    public JButton getCustomFunctionsButton() {
        return customFunctionsButton;
    }
    public JComboBox getRedComboBox() {
        return redComboBox;
    }
    public JSpinner getRedMinSpinner() {
        return redMinSpinner;
    }
    public JSpinner getRedMaxSpinner() {
        return redMaxSpinner;
    }
    public JComboBox getGreenComboBox() {
        return greenComboBox;
    }
    public JSpinner getGreenMinSpinner() {
        return greenMinSpinner;
    }
    public JSpinner getGreenMaxSpinner() {
        return greenMaxSpinner;
    }
    public JComboBox getBlueComboBox() {
        return blueComboBox;
    }
    public JSpinner getBlueMinSpinner() {
        return blueMinSpinner;
    }
    public JSpinner getBlueMaxSpinner() {
        return blueMaxSpinner;
    }


    public JList getResultList()
    {
        return resultList;
    }


    public SpectrumPopupMenu getSpectrumPopupMenu()
    {
        return spectrumPopupMenu;
    }


    public void setSpectrumPopupMenu(SpectrumPopupMenu spectrumPopupMenu)
    {
        this.spectrumPopupMenu = spectrumPopupMenu;
    }
    public JLabel getResultsLabel() {
        return resultsLabel;
    }
    public JButton getRemoveAllBoundariesButton() {
        return removeAllBoundariesButton;
    }
    public JPanel getDbSearchPanel() {
        return dbSearchPanel;
    }
    public JPanel getColoringDetailPanel() {
        return coloringDetailPanel;
    }
    public JPanel getColoringPanel() {
        return coloringPanel;
    }
    public JComboBox getColoringComboBox() {
        return coloringComboBox;
    }
    public JPanel getEmissionAngleColoringPanel() {
        return emissionAngleColoringPanel;
    }
    public JPanel getRgbColoringPanel() {
        return rgbColoringPanel;
    }
    public JButton getSaveSpectraListButton() {
        return saveSpectraListButton;
    }
    public JButton getLoadSpectraListButton() {
        return loadSpectraListButton;
    }
}
