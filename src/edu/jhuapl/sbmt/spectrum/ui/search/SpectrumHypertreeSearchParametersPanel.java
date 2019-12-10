package edu.jhuapl.sbmt.spectrum.ui.search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

public class SpectrumHypertreeSearchParametersPanel  extends JPanel
{
    private JToggleButton selectRegionButton;
    private JLabel startDateLabel;
    private JSpinner startSpinner;
    private JButton submitButton;
    private JLabel toDistanceLabel;
    private JFormattedTextField toDistanceTextField;
    private JLabel toEmissionLabel;
    private JFormattedTextField toEmissionTextField;
    private JLabel toIncidenceLabel;
    private JFormattedTextField toIncidenceTextField;
    private JLabel toPhaseLabel;
    private JFormattedTextField toPhaseTextField;
    private JLabel endDateLabel;
    private JLabel endDistanceLabel;
    private JLabel endEmissionLabel;
    private JLabel endIncidenceLabel;
    private JLabel endPhaseLabel;
    private JSpinner endSpinner;

    private JLabel fromDistanceLabel;
    private JFormattedTextField fromDistanceTextField;
    private JLabel fromEmissionLabel;
    private JFormattedTextField fromEmissionTextField;
    private JLabel fromIncidenceLabel;
    private JFormattedTextField fromIncidenceTextField;
    private JLabel fromPhaseLabel;
    private JFormattedTextField fromPhaseTextField;
    private JButton clearRegionButton;
    private JPanel auxPanel;
    private JRadioButton L3btn;
    private JRadioButton L2btn;
    private JRadioButton ifbtn;
    private JRadioButton refbtn;
    private ButtonGroup group;

    public SpectrumHypertreeSearchParametersPanel(boolean isHierarchical)
    {
        setBorder(new TitledBorder(null, "Search Parameters",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initParameterSearch();
//
//        JPanel panel_10 = new JPanel();
//        add(panel_10);
//        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));
//
//        selectRegionButton = new JToggleButton("Select Region");
//        panel_10.add(selectRegionButton);
//
//        clearRegionButton = new JButton("Clear Region");
//        panel_10.add(clearRegionButton);
//
//        submitButton = new JButton("Search");
//        panel_10.add(submitButton);
//
//        Component verticalStrut_1 = Box.createVerticalStrut(20);
//        add(verticalStrut_1);
    }

    private void initParameterSearch()
    {
        setBorder(new TitledBorder(null, "Search Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        startDateLabel = new JLabel("Start Date:");
        panel_1.add(startDateLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue);

        startSpinner = new JSpinner();
        DateTime start = new DateTime(2017, 1, 1, 0, 0, 0, 0);
//        model.setStartDate(start.toDate());
        startSpinner.setModel(new javax.swing.SpinnerDateModel(start.toDate(), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setPreferredSize(new java.awt.Dimension(200, 22));
        startSpinner.setMaximumSize(new java.awt.Dimension(200, 22));
        panel_1.add(startSpinner);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        endDateLabel = new JLabel("End Date: ");
        panel_2.add(endDateLabel);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_1);

        endSpinner = new JSpinner();
        DateTime end = new DateTime(2017, 12, 31, 0, 0, 0, 0);
//        model.setEndDate(end.toDate());
        endSpinner.setModel(new javax.swing.SpinnerDateModel(end.toDate(), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(200, 22));
        endSpinner.setMaximumSize(new java.awt.Dimension(200, 22));
        panel_2.add(endSpinner);

        Component verticalStrut = Box.createVerticalStrut(10);
        add(verticalStrut);

        JPanel panel_3 = new JPanel();
        add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        fromDistanceLabel = new JLabel("S/C Distance from");
        panel_3.add(fromDistanceLabel);

        fromDistanceTextField = new JFormattedTextField();
        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setPreferredSize(new Dimension(0, 22));
        fromDistanceTextField.setText("0");
//        fromDistanceTextField.setEnabled(false);
        fromDistanceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromDistanceTextField.getPreferredSize().height) );

        panel_3.add(fromDistanceTextField);

        toDistanceLabel = new JLabel("to");
        panel_3.add(toDistanceLabel);

        toDistanceTextField = new JFormattedTextField();
        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("5000000");
//        toDistanceTextField.setEnabled(false);
        toDistanceTextField.setPreferredSize(new Dimension(0, 22));
        toDistanceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toDistanceTextField.getPreferredSize().height) );

        panel_3.add(toDistanceTextField);

        JLabel lblNewLabel_10 = new JLabel("km");
        panel_3.add(lblNewLabel_10);

        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalStrut = Box.createHorizontalStrut(22);
        panel_4.add(horizontalStrut);

        fromIncidenceLabel = new JLabel("Incidence from");
        panel_4.add(fromIncidenceLabel);

        fromIncidenceTextField = new JFormattedTextField();
        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
//        fromIncidenceTextField.setEnabled(false);
        fromIncidenceTextField.setPreferredSize(new Dimension(0, 22));
        fromIncidenceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromIncidenceTextField.getPreferredSize().height) );

        panel_4.add(fromIncidenceTextField);

        toIncidenceLabel = new JLabel("to");
        panel_4.add(toIncidenceLabel);

        toIncidenceTextField = new JFormattedTextField();
        toIncidenceTextField.setPreferredSize(new Dimension(0, 22));
        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
//        toIncidenceTextField.setEnabled(false);
        toIncidenceTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toIncidenceTextField.getPreferredSize().height) );

        panel_4.add(toIncidenceTextField);

        JLabel lblNewLabel_11 = new JLabel("deg");
        panel_4.add(lblNewLabel_11);

        JPanel panel_5 = new JPanel();
        add(panel_5);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

        Component horizontalStrut2 = Box.createHorizontalStrut(25);
        panel_5.add(horizontalStrut2);

        fromEmissionLabel = new JLabel("Emission from");
        panel_5.add(fromEmissionLabel);

        fromEmissionTextField = new JFormattedTextField();
        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
//        fromEmissionTextField.setEnabled(false);
        fromEmissionTextField.setPreferredSize(new Dimension(0, 22));
        fromEmissionTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromEmissionTextField.getPreferredSize().height) );

        panel_5.add(fromEmissionTextField);

        toEmissionLabel = new JLabel("to");
        panel_5.add(toEmissionLabel);

        toEmissionTextField = new JFormattedTextField();
        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
//        toEmissionTextField.setEnabled(false);
        toEmissionTextField.setPreferredSize(new Dimension(0, 22));
        toEmissionTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toEmissionTextField.getPreferredSize().height) );

        panel_5.add(toEmissionTextField);

        JLabel lblNewLabel_12 = new JLabel("deg");
        panel_5.add(lblNewLabel_12);



        JPanel panel_6 = new JPanel();
        add(panel_6);
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        Component horizontalStrut3 = Box.createHorizontalStrut(45);
        panel_6.add(horizontalStrut3);

        fromPhaseLabel = new JLabel("Phase from");
        panel_6.add(fromPhaseLabel);

        fromPhaseTextField = new JFormattedTextField();
        fromPhaseTextField.setPreferredSize(new Dimension(0, 22));
        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
//        fromPhaseTextField.setEnabled(false);
        fromPhaseTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, fromPhaseTextField.getPreferredSize().height) );

        panel_6.add(fromPhaseTextField);

        toPhaseLabel = new JLabel("to");
        panel_6.add(toPhaseLabel);

        toPhaseTextField = new JFormattedTextField();
        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
//        toPhaseTextField.setEnabled(false);
        toPhaseTextField.setPreferredSize(new Dimension(0, 22));
        toPhaseTextField.setMaximumSize( new Dimension(Integer.MAX_VALUE, toPhaseTextField.getPreferredSize().height) );

        panel_6.add(toPhaseTextField);

        JLabel lblNewLabel_13 = new JLabel("deg");
        panel_6.add(lblNewLabel_13);


//        if (instrument.getDisplayName().equals(SpectraType.OTES_SPECTRA.getDisplayName())) {
//            // add L2 vs L3 checkbox
//            JPanel panel_16 = new JPanel();
//            add(panel_16);
//            panel_16.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
//
//            JLabel lblOtesLevel = new JLabel("OTES TYPE");
//            panel_16.add(lblOtesLevel);
//
//            L2btn = new JRadioButton("L2");
//            L2btn.setSelected(true);
//            panel_16.add(L2btn);
//
//            L3btn = new JRadioButton("L3");
//            L3btn.setSelected(false);
//            panel_16.add(L3btn);
//
//            ButtonGroup group = new ButtonGroup();
//            group.add(L2btn);
//            group.add(L3btn);
//        }
//        if (instrument.getDisplayName().equals(SpectraType.OVIRS_SPECTRA.getDisplayName())) {
//            // add L2 vs L3 checkbox
//            JPanel panel_16 = new JPanel();
//            add(panel_16);
//            panel_16.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
//
//            JLabel lblOvirs = new JLabel("OVIRS TYPE");
//            panel_16.add(lblOvirs);
//
//            ifbtn = new JRadioButton("I/F");
//            ifbtn.setSelected(true);
//            panel_16.add(ifbtn);
//
//            refbtn = new JRadioButton("REFF");
//            refbtn.setSelected(false);
//            panel_16.add(refbtn);
//
//            ButtonGroup group = new ButtonGroup();
//            group.add(ifbtn);
//            group.add(refbtn);
//        }


        // search / region buttons
        JPanel searchButtonPanel = new JPanel();

        selectRegionButton = new JToggleButton("Select Region");
//        selectRegionButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (!selectRegionButton.isSelected()) {
//                    selectRegionButton.setSelected(true);
//                    getPickManager().setPickMode(PickMode.CIRCLE_SELECTION);
//                } else {
//                    selectRegionButton.setSelected(false);
//                    getPickManager().setPickMode(PickMode.DEFAULT);
//                }
//            }
//        });
        searchButtonPanel.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        searchButtonPanel.add(clearRegionButton);

        submitButton = new JButton("Search");
        searchButtonPanel.add(submitButton);

        add(searchButtonPanel);
    }

    public void addRadioButtons(String type, String[] buttonText)
    {
        JPanel panel_16 = new JPanel();
        add(panel_16);
        panel_16.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JLabel lblOtesLevel = new JLabel(type);
        panel_16.add(lblOtesLevel);

        group = new ButtonGroup();
        for (String str : buttonText)
        {
            JRadioButton btn = new JRadioButton(str);
            panel_16.add(btn);
            group.add(btn);
            if (group.getButtonCount() == 1) btn.setSelected(true);
        }


//        L2btn = new JRadioButton("L2");
//        L2btn.setSelected(true);
//        panel_16.add(L2btn);
//
//        L3btn = new JRadioButton("L3");
//        L3btn.setSelected(false);
//        panel_16.add(L3btn);

//        ButtonGroup group = new ButtonGroup();
//        group.add(L2btn);
//        group.add(L3btn);
    }

//    private void initParameterSearch()
//    {
//        Component verticalStrut_10 = Box.createVerticalStrut(5);
//        add(verticalStrut_10);
//
//        JPanel choicePanel = new JPanel();
//        add(choicePanel);
//
//
//        Component verticalStrut_9 = Box.createVerticalStrut(20);
//        add(verticalStrut_9);
//
//        final JPanel filenamePanel = new JPanel();
//        filenamePanel.setVisible(false);
//        add(filenamePanel);
//        filenamePanel.setLayout(new BoxLayout(filenamePanel, BoxLayout.X_AXIS));
//
//        JLabel lblFilename = new JLabel("Filename:");
//        filenamePanel.add(lblFilename);
//
//        Component horizontalGlue = Box.createHorizontalGlue();
//        filenamePanel.add(horizontalGlue);
//
//        final JPanel parametersPanel = new JPanel();
//        add(parametersPanel);
//        parametersPanel
//                .setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
//
//
//        JPanel panel_1 = new JPanel();
//        parametersPanel.add(panel_1);
//        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
//
//        startDateLabel = new JLabel("Start Date:");
//        panel_1.add(startDateLabel);
//
//        startSpinner = new JSpinner();
//        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        startSpinner.setModel(new javax.swing.SpinnerDateModel(
//                new java.util.Date(1126411200000L), null, null,
//                java.util.Calendar.DAY_OF_MONTH));
//        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner,
//                "yyyy-MMM-dd HH:mm:ss"));
//        startSpinner.setMaximumSize(
//                new java.awt.Dimension(startSpinner.getWidth(), 22));
//        panel_1.add(startSpinner);
//
//        Component horizontalGlue_8 = Box.createHorizontalGlue();
//        panel_1.add(horizontalGlue_8);
//
//        Component verticalStrut_8 = Box.createVerticalStrut(10);
//        parametersPanel.add(verticalStrut_8);
//
//        JPanel panel_2 = new JPanel();
//        parametersPanel.add(panel_2);
//        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
//
//        endDateLabel = new JLabel("  End Date:");
//        panel_2.add(endDateLabel);
//
//        endSpinner = new JSpinner();
//        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
//        endSpinner.setModel(new javax.swing.SpinnerDateModel(
//                new java.util.Date(1126411200000L), null, null,
//                java.util.Calendar.DAY_OF_MONTH));
//        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner,
//                "yyyy-MMM-dd HH:mm:ss"));
//        endSpinner.setMaximumSize(
//                new java.awt.Dimension(endSpinner.getWidth(), 22));
//        panel_2.add(endSpinner);
//
//        Component horizontalGlue_9 = Box.createHorizontalGlue();
//        panel_2.add(horizontalGlue_9);
//
//        Component verticalStrut_7 = Box.createVerticalStrut(20);
//        parametersPanel.add(verticalStrut_7);
//
//        JPanel panel_3 = new JPanel();
//        parametersPanel.add(panel_3);
//        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
//
//
//        Component horizontalGlue_6 = Box.createHorizontalGlue();
//        panel_3.add(horizontalGlue_6);
//
//        Component verticalStrut_6 = Box.createVerticalStrut(20);
//        parametersPanel.add(verticalStrut_6);
//
//        JPanel panel_4 = new JPanel();
//        parametersPanel.add(panel_4);
//
//        JLabel lblScDistanceFrom = new JLabel("S/C Distance from");
//        panel_4.add(lblScDistanceFrom);
//
//        fromDistanceTextField = new JFormattedTextField();
//        fromDistanceTextField.setText("0");
//        fromDistanceTextField.setMaximumSize(
//                new Dimension(fromDistanceTextField.getWidth(), 20));
//        fromDistanceTextField.setColumns(5);
//        panel_4.add(fromDistanceTextField);
//
//        panel_4.add(new JLabel("to"));
//
//        toDistanceTextField = new JFormattedTextField();
//        toDistanceTextField.setText("1000");
//        toDistanceTextField.setMaximumSize(
//                new Dimension(toDistanceTextField.getWidth(), 20));
//        toDistanceTextField.setColumns(5);
//        panel_4.add(toDistanceTextField);
//
//        JLabel lblKm = new JLabel("km");
//        panel_4.add(lblKm);
//        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
//
//        Component horizontalGlue_1 = Box.createHorizontalGlue();
//        panel_4.add(horizontalGlue_1);
//
//        Component verticalStrut_5 = Box.createVerticalStrut(10);
//        parametersPanel.add(verticalStrut_5);
//
//        JPanel panel_6 = new JPanel();
//        parametersPanel.add(panel_6);
//
//        fromIncidenceLabel = new JLabel("      Incidence from");
//        panel_6.add(fromIncidenceLabel);
//
//        fromIncidenceTextField = new JFormattedTextField();
//        fromIncidenceTextField.setText("0");
//        fromIncidenceTextField.setMaximumSize(
//                new Dimension(fromIncidenceTextField.getWidth(), 20));
//        fromIncidenceTextField.setColumns(5);
//        panel_6.add(fromIncidenceTextField);
//
//        toIncidenceLabel = new JLabel("to");
//        panel_6.add(toIncidenceLabel);
//
//        toIncidenceTextField = new JFormattedTextField();
//        toIncidenceTextField.setText("180");
//        toIncidenceTextField.setMaximumSize(
//                new Dimension(toIncidenceTextField.getWidth(), 20));
//        toIncidenceTextField.setColumns(5);
//        panel_6.add(toIncidenceTextField);
//
//        panel_6.add(new JLabel("deg"));
//        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
//
//        Component horizontalGlue_3 = Box.createHorizontalGlue();
//        panel_6.add(horizontalGlue_3);
//
//        Component verticalStrut_3 = Box.createVerticalStrut(10);
//        parametersPanel.add(verticalStrut_3);
//
//        JPanel panel_7 = new JPanel();
//        parametersPanel.add(panel_7);
//
//        fromEmissionLabel = new JLabel("      Emission from");
//        panel_7.add(fromEmissionLabel);
//
//        fromEmissionTextField = new JFormattedTextField();
//        fromEmissionTextField.setText("0");
//        fromEmissionTextField.setMaximumSize(
//                new Dimension(fromEmissionTextField.getWidth(), 20));
//        fromEmissionTextField.setColumns(5);
//        panel_7.add(fromEmissionTextField);
//
//        toEmissionLabel = new JLabel("to");
//        panel_7.add(toEmissionLabel);
//
//        toEmissionTextField = new JFormattedTextField();
//        toEmissionTextField.setText("180");
//        toEmissionTextField.setMaximumSize(
//                new Dimension(toEmissionTextField.getWidth(), 20));
//        toEmissionTextField.setColumns(5);
//        panel_7.add(toEmissionTextField);
//
//        panel_7.add(new JLabel("deg"));
//        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));
//
//        Component horizontalGlue_4 = Box.createHorizontalGlue();
//        panel_7.add(horizontalGlue_4);
//
//        Component verticalStrut_2 = Box.createVerticalStrut(10);
//        parametersPanel.add(verticalStrut_2);
//
//        JPanel panel_8 = new JPanel();
//        parametersPanel.add(panel_8);
//
//        fromPhaseLabel = new JLabel("           Phase from");
//        panel_8.add(fromPhaseLabel);
//
//        fromPhaseTextField = new JFormattedTextField();
//        fromPhaseTextField.setText("0");
//        fromPhaseTextField.setMaximumSize(
//                new Dimension(fromPhaseTextField.getWidth(), 20));
//        fromPhaseTextField.setColumns(5);
//        panel_8.add(fromPhaseTextField);
//
//        toPhaseLabel = new JLabel("to");
//        panel_8.add(toPhaseLabel);
//
//        toPhaseTextField = new JFormattedTextField();
//        toPhaseTextField.setText("180");
//        toPhaseTextField
//                .setMaximumSize(new Dimension(toPhaseTextField.getWidth(), 20));
//        toPhaseTextField.setColumns(5);
//        panel_8.add(toPhaseTextField);
//
//        panel_8.add(new JLabel("deg"));
//        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));
//
//        Component horizontalGlue_5 = Box.createHorizontalGlue();
//        panel_8.add(horizontalGlue_5);
//
//        auxPanel = new JPanel();
//        parametersPanel.add(auxPanel);
//        auxPanel.setLayout(new BoxLayout(auxPanel, BoxLayout.Y_AXIS));
//
//        Component verticalStrut = Box.createVerticalStrut(20);
//        add(verticalStrut);
//
//
//    }

    protected List<BasicSpectrum> processResults(List<BasicSpectrum> input)
    {
        return input;
    }

    public JToggleButton getSelectRegionButton()
    {
        return selectRegionButton;
    }

    public JLabel getStartDateLabel()
    {
        return startDateLabel;
    }

    public JSpinner getStartSpinner()
    {
        return startSpinner;
    }

    public JButton getSubmitButton()
    {
        return submitButton;
    }

    public JLabel getToDistanceLabel()
    {
        return toDistanceLabel;
    }

    public JFormattedTextField getToDistanceTextField()
    {
        return toDistanceTextField;
    }

    public JLabel getToEmissionLabel()
    {
        return toEmissionLabel;
    }

    public JFormattedTextField getToEmissionTextField()
    {
        return toEmissionTextField;
    }

    public JLabel getToIncidenceLabel()
    {
        return toIncidenceLabel;
    }

    public JFormattedTextField getToIncidenceTextField()
    {
        return toIncidenceTextField;
    }

    public JLabel getToPhaseLabel()
    {
        return toPhaseLabel;
    }

    public JFormattedTextField getToPhaseTextField()
    {
        return toPhaseTextField;
    }

    public JLabel getEndDateLabel()
    {
        return endDateLabel;
    }

    public JLabel getEndDistanceLabel()
    {
        return endDistanceLabel;
    }

    public JLabel getEndEmissionLabel()
    {
        return endEmissionLabel;
    }

    public JLabel getEndIncidenceLabel()
    {
        return endIncidenceLabel;
    }

    public JLabel getEndPhaseLabel()
    {
        return endPhaseLabel;
    }


    public JSpinner getEndSpinner()
    {
        return endSpinner;
    }


    public JLabel getFromDistanceLabel()
    {
        return fromDistanceLabel;
    }

    public JFormattedTextField getFromDistanceTextField()
    {
        return fromDistanceTextField;
    }

    public JLabel getFromEmissionLabel()
    {
        return fromEmissionLabel;
    }

    public JFormattedTextField getFromEmissionTextField()
    {
        return fromEmissionTextField;
    }

    public JLabel getFromIncidenceLabel()
    {
        return fromIncidenceLabel;
    }

    public JFormattedTextField getFromIncidenceTextField()
    {
        return fromIncidenceTextField;
    }

    public JLabel getFromPhaseLabel()
    {
        return fromPhaseLabel;
    }

    public JFormattedTextField getFromPhaseTextField()
    {
        return fromPhaseTextField;
    }

    public JButton getClearRegionButton()
    {
        return clearRegionButton;
    }

    public JPanel getAuxPanel()
    {
        return auxPanel;
    }

    public void setAuxPanel(JPanel auxPanel)
    {
        this.auxPanel = auxPanel;
    }

    public ButtonGroup getGroup()
    {
        return group;
    }

    public JRadioButton getL2Button()
    {
        return L2btn;
    }
}
