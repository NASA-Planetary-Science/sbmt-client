package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import com.jidesoft.swing.CheckBoxTree;

public class ImageSearchParametersPanel extends JPanel
{
    private JCheckBox[] filterCheckBoxes;
    private JCheckBox[] userDefinedCheckBoxes;
    protected CheckBoxTree checkBoxTree;
    private JCheckBox searchByFilenameCheckBox;
    private JFormattedTextField searchByNumberTextField;
    private JToggleButton selectRegionButton;
    private JComboBox sourceComboBox;
    private JLabel sourceLabel;
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
    private JLabel toResolutionLabel;
    private JFormattedTextField toResolutionTextField;
    private JCheckBox userDefined1CheckBox;
    private JCheckBox userDefined2CheckBox;
    private JCheckBox userDefined3CheckBox;
    private JCheckBox userDefined4CheckBox;
    private JCheckBox userDefined5CheckBox;
    private JCheckBox userDefined6CheckBox;
    private JCheckBox userDefined7CheckBox;
    private JCheckBox userDefined8CheckBox;
    private JPanel userDefinedCheckBoxPanel;
    private JLabel endDateLabel;
    private JLabel endDistanceLabel;
    private JLabel endEmissionLabel;
    private JLabel endIncidenceLabel;
    private JLabel endPhaseLabel;
    private JLabel endResolutionLabel;
    private JSpinner endSpinner;
    private JCheckBox excludeGaskellCheckBox;
    private JCheckBox filter10CheckBox;
    private JCheckBox filter11CheckBox;
    private JCheckBox filter12CheckBox;
    private JCheckBox filter13CheckBox;
    private JCheckBox filter14CheckBox;
    private JCheckBox filter15CheckBox;
    private JCheckBox filter16CheckBox;
    private JCheckBox filter17CheckBox;
    private JCheckBox filter18CheckBox;
    private JCheckBox filter19CheckBox;
    private JCheckBox filter1CheckBox;
    private JCheckBox filter20CheckBox;
    private JCheckBox filter21CheckBox;
    private JCheckBox filter22CheckBox;
    private JCheckBox filter2CheckBox;
    private JCheckBox filter3CheckBox;
    private JCheckBox filter4CheckBox;
    private JCheckBox filter5CheckBox;
    private JCheckBox filter6CheckBox;
    private JCheckBox filter7CheckBox;
    private JCheckBox filter8CheckBox;
    private JCheckBox filter9CheckBox;
    private JPanel filterCheckBoxPanel;
    private JLabel fromDistanceLabel;
    private JFormattedTextField fromDistanceTextField;
    private JLabel fromEmissionLabel;
    private JFormattedTextField fromEmissionTextField;
    private JLabel fromIncidenceLabel;
    private JFormattedTextField fromIncidenceTextField;
    private JLabel fromPhaseLabel;
    private JFormattedTextField fromPhaseTextField;
    private JLabel fromResolutionLabel;
    private JFormattedTextField fromResolutionTextField;
    private JComboBox hasLimbComboBox;
    private JLabel hasLimbLabel;
    private JScrollPane hierarchicalSearchScrollPane;
    private JButton clearRegionButton;
    private JTextField textField;

    public ImageSearchParametersPanel()
    {
        setBorder(new TitledBorder(null, "Search Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //TODO: Override and setup in subclass
        hierarchicalSearchScrollPane = new javax.swing.JScrollPane();

        Component verticalStrut_10 = Box.createVerticalStrut(5);
        add(verticalStrut_10);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        sourceLabel = new JLabel("Pointing:");
        panel.add(sourceLabel);

        sourceComboBox = new JComboBox();
        sourceComboBox.setMaximumSize(new java.awt.Dimension(sourceComboBox.getWidth(), 22));
        panel.add(sourceComboBox);

        Component horizontalGlue_7 = Box.createHorizontalGlue();
        panel.add(horizontalGlue_7);

        excludeGaskellCheckBox = new JCheckBox("Exclude SPC Derived");
        panel.add(excludeGaskellCheckBox);

        Component verticalStrut_9 = Box.createVerticalStrut(20);
        add(verticalStrut_9);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        startDateLabel = new JLabel("Start Date:");
        panel_1.add(startDateLabel);

        startSpinner = new JSpinner();
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMaximumSize(new java.awt.Dimension(startSpinner.getWidth(), 22));
        panel_1.add(startSpinner);

        Component horizontalGlue_8 = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue_8);

        Component verticalStrut_8 = Box.createVerticalStrut(10);
        add(verticalStrut_8);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        endDateLabel = new JLabel("  End Date:");
        panel_2.add(endDateLabel);

        endSpinner = new JSpinner();
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMaximumSize(new java.awt.Dimension(endSpinner.getWidth(), 22));
        panel_2.add(endSpinner);

        Component horizontalGlue_9 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_9);

        Component verticalStrut_7 = Box.createVerticalStrut(20);
        add(verticalStrut_7);

        JPanel panel_3 = new JPanel();
        add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        hasLimbLabel = new JLabel("Limb:");
        panel_3.add(hasLimbLabel);

        hasLimbComboBox = new JComboBox();
        hasLimbComboBox.setMaximumSize(new java.awt.Dimension(hasLimbComboBox.getWidth(), 22));
        panel_3.add(hasLimbComboBox);

        Component horizontalGlue_6 = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue_6);

        Component verticalStrut_6 = Box.createVerticalStrut(20);
        add(verticalStrut_6);

        JPanel panel_4 = new JPanel();
        add(panel_4);

        JLabel lblScDistanceFrom = new JLabel("S/C Distance from");
        panel_4.add(lblScDistanceFrom);

        fromDistanceTextField = new JFormattedTextField();
        fromDistanceTextField.setMaximumSize(new Dimension(fromDistanceTextField.getWidth(), 20));
        fromDistanceTextField.setColumns(5);
        panel_4.add(fromDistanceTextField);

        panel_4.add(new JLabel("to"));

        toDistanceTextField = new JFormattedTextField();
        toDistanceTextField.setMaximumSize(new Dimension(toDistanceTextField.getWidth(), 20));
        toDistanceTextField.setColumns(5);
        panel_4.add(toDistanceTextField);

        JLabel lblKm = new JLabel("km");
        panel_4.add(lblKm);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue_1);

        Component verticalStrut_5 = Box.createVerticalStrut(10);
        add(verticalStrut_5);

        JPanel panel_5 = new JPanel();
        add(panel_5);

        fromResolutionLabel = new JLabel("    Resolution from");
        panel_5.add(fromResolutionLabel);

        fromResolutionTextField = new JFormattedTextField();
        fromResolutionTextField.setMaximumSize(new Dimension(fromResolutionTextField.getWidth(), 20));
        fromResolutionTextField.setColumns(5);
        panel_5.add(fromResolutionTextField);

        toResolutionLabel = new JLabel("to");
        panel_5.add(toResolutionLabel);

        toResolutionTextField = new JFormattedTextField();
        toResolutionTextField.setMaximumSize(new Dimension(toResolutionTextField.getWidth(), 20));
        toResolutionTextField.setColumns(5);
        panel_5.add(toResolutionTextField);

        endResolutionLabel = new JLabel("mpp");
        panel_5.add(endResolutionLabel);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_5.add(horizontalGlue_2);

        Component verticalStrut_4 = Box.createVerticalStrut(10);
        add(verticalStrut_4);

        JPanel panel_6 = new JPanel();
        add(panel_6);

        fromIncidenceLabel = new JLabel("      Incidence from");
        panel_6.add(fromIncidenceLabel);

        fromIncidenceTextField = new JFormattedTextField();
        fromIncidenceTextField.setMaximumSize(new Dimension(fromIncidenceTextField.getWidth(), 20));
        fromIncidenceTextField.setColumns(5);
        panel_6.add(fromIncidenceTextField);

        toIncidenceLabel = new JLabel("to");
        panel_6.add(toIncidenceLabel);

        toIncidenceTextField = new JFormattedTextField();
        toIncidenceTextField.setMaximumSize(new Dimension(toIncidenceTextField.getWidth(), 20));
        toIncidenceTextField.setColumns(5);
        panel_6.add(toIncidenceTextField);

        panel_6.add(new JLabel("deg"));
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        panel_6.add(horizontalGlue_3);

        Component verticalStrut_3 = Box.createVerticalStrut(10);
        add(verticalStrut_3);

        JPanel panel_7 = new JPanel();
        add(panel_7);

        fromEmissionLabel = new JLabel("      Emission from");
        panel_7.add(fromEmissionLabel);

        fromEmissionTextField = new JFormattedTextField();
        fromEmissionTextField.setMaximumSize(new Dimension(fromEmissionTextField.getWidth(), 20));
        fromEmissionTextField.setColumns(5);
        panel_7.add(fromEmissionTextField);

        toEmissionLabel = new JLabel("to");
        panel_7.add(toEmissionLabel);

        toEmissionTextField = new JFormattedTextField();
        toEmissionTextField.setMaximumSize(new Dimension(toEmissionTextField.getWidth(), 20));
        toEmissionTextField.setColumns(5);
        panel_7.add(toEmissionTextField);

        panel_7.add(new JLabel("deg"));
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        Component horizontalGlue_4 = Box.createHorizontalGlue();
        panel_7.add(horizontalGlue_4);

        Component verticalStrut_2 = Box.createVerticalStrut(10);
        add(verticalStrut_2);

        JPanel panel_8 = new JPanel();
        add(panel_8);

        fromPhaseLabel = new JLabel("           Phase from");
        panel_8.add(fromPhaseLabel);

        fromPhaseTextField = new JFormattedTextField();
        fromPhaseTextField.setMaximumSize(new Dimension(fromPhaseTextField.getWidth(), 20));
        fromPhaseTextField.setColumns(5);
        panel_8.add(fromPhaseTextField);

        toPhaseLabel = new JLabel("to");
        panel_8.add(toPhaseLabel);

        toPhaseTextField = new JFormattedTextField();
        toPhaseTextField.setMaximumSize(new Dimension(toPhaseTextField.getWidth(), 20));
        toPhaseTextField.setColumns(5);
        panel_8.add(toPhaseTextField);

        panel_8.add(new JLabel("deg"));
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        Component horizontalGlue_5 = Box.createHorizontalGlue();
        panel_8.add(horizontalGlue_5);

        Component verticalStrut_1 = Box.createVerticalStrut(20);
        add(verticalStrut_1);

        JPanel panel_9 = new JPanel();
        add(panel_9);
        panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.X_AXIS));

        searchByFilenameCheckBox = new JCheckBox("Search by filename:");
        panel_9.add(searchByFilenameCheckBox);

        searchByNumberTextField = new JFormattedTextField();
        panel_9.add(searchByNumberTextField);
        searchByNumberTextField.setColumns(10);
        searchByNumberTextField.setMaximumSize(new Dimension(searchByNumberTextField.getWidth(), 20));

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_9.add(horizontalGlue);

        Component verticalStrut = Box.createVerticalStrut(20);
        add(verticalStrut);

        JPanel panel_10 = new JPanel();
        add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        selectRegionButton = new JToggleButton("Select Region");
        panel_10.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        panel_10.add(clearRegionButton);

        submitButton = new JButton("Search");
        panel_10.add(submitButton);
        // TODO Auto-generated constructor stub
        excludeGaskellCheckBox.setVisible(false);

        //TODO: initialize them from the controller

        filter1CheckBox = new JCheckBox();
        filter2CheckBox = new JCheckBox();
        filter3CheckBox = new JCheckBox();
        filter4CheckBox = new JCheckBox();
        filter5CheckBox = new JCheckBox();
        filter6CheckBox = new JCheckBox();
        filter7CheckBox = new JCheckBox();
        filter8CheckBox = new JCheckBox();
        filter9CheckBox = new JCheckBox();
        filter10CheckBox = new JCheckBox();
        filter11CheckBox = new JCheckBox();
        filter12CheckBox = new JCheckBox();
        filter13CheckBox = new JCheckBox();
        filter14CheckBox = new JCheckBox();
        filter15CheckBox = new JCheckBox();
        filter16CheckBox = new JCheckBox();
        filter17CheckBox = new JCheckBox();
        filter18CheckBox = new JCheckBox();
        filter19CheckBox = new JCheckBox();
        filter20CheckBox = new JCheckBox();
        filter21CheckBox = new JCheckBox();
        filter22CheckBox = new JCheckBox();

        filterCheckBoxes = new JCheckBox[]{
                filter1CheckBox,
                filter2CheckBox,
                filter3CheckBox,
                filter4CheckBox,
                filter5CheckBox,
                filter6CheckBox,
                filter7CheckBox,
                filter8CheckBox,
                filter9CheckBox,
                filter10CheckBox,
                filter11CheckBox,
                filter12CheckBox,
                filter13CheckBox,
                filter14CheckBox,
                filter15CheckBox,
                filter16CheckBox,
                filter17CheckBox,
                filter18CheckBox,
                filter19CheckBox,
                filter20CheckBox,
                filter21CheckBox,
                filter22CheckBox
        };

        userDefined1CheckBox = new JCheckBox();
        userDefined2CheckBox = new JCheckBox();
        userDefined3CheckBox = new JCheckBox();
        userDefined4CheckBox = new JCheckBox();
        userDefined5CheckBox = new JCheckBox();
        userDefined6CheckBox = new JCheckBox();
        userDefined7CheckBox = new JCheckBox();
        userDefined8CheckBox = new JCheckBox();

        userDefinedCheckBoxes = new JCheckBox[]{
                userDefined1CheckBox,
                userDefined2CheckBox,
                userDefined3CheckBox,
                userDefined4CheckBox,
                userDefined5CheckBox,
                userDefined6CheckBox,
                userDefined7CheckBox,
                userDefined8CheckBox
        };

        //maybe these need to overridden later?
//        redComboBox.setVisible(false);
//        greenComboBox.setVisible(false);
//        blueComboBox.setVisible(false);
//
//        ComboBoxModel redModel = getRedComboBoxModel();
//        ComboBoxModel greenModel = getGreenComboBoxModel();
//        ComboBoxModel blueModel = getBlueComboBoxModel();
//        if (redModel != null && greenModel != null && blueModel != null)
//        {
//            redComboBox.setModel(redModel);
//            greenComboBox.setModel(greenModel);
//            blueComboBox.setModel(blueModel);
//
//            redComboBox.setVisible(true);
//            greenComboBox.setVisible(true);
//            blueComboBox.setVisible(true);
//
//            redButton.setVisible(false);
//            greenButton.setVisible(false);
//            blueButton.setVisible(false);
//        }


    }

    public ImageSearchParametersPanel(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public ImageSearchParametersPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public ImageSearchParametersPanel(LayoutManager layout,
            boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    protected List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }



    public JCheckBox[] getFilterCheckBoxes()
    {
        return filterCheckBoxes;
    }

    public JCheckBox[] getUserDefinedCheckBoxes()
    {
        return userDefinedCheckBoxes;
    }

    public CheckBoxTree getCheckBoxTree()
    {
        return checkBoxTree;
    }

    public void setCheckBoxTree(CheckBoxTree checkBoxTree)
    {
        this.checkBoxTree = checkBoxTree;
    }

    public JCheckBox getSearchByFilenameCheckBox()
    {
        return searchByFilenameCheckBox;
    }

    public JFormattedTextField getSearchByNumberTextField()
    {
        return searchByNumberTextField;
    }

    public JToggleButton getSelectRegionButton()
    {
        return selectRegionButton;
    }

    public JComboBox getSourceComboBox()
    {
        return sourceComboBox;
    }

    public JLabel getSourceLabel()
    {
        return sourceLabel;
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

    public JLabel getToResolutionLabel()
    {
        return toResolutionLabel;
    }

    public JFormattedTextField getToResolutionTextField()
    {
        return toResolutionTextField;
    }

    public JCheckBox getUserDefined1CheckBox()
    {
        return userDefined1CheckBox;
    }

    public JCheckBox getUserDefined2CheckBox()
    {
        return userDefined2CheckBox;
    }

    public JCheckBox getUserDefined3CheckBox()
    {
        return userDefined3CheckBox;
    }

    public JCheckBox getUserDefined4CheckBox()
    {
        return userDefined4CheckBox;
    }

    public JCheckBox getUserDefined5CheckBox()
    {
        return userDefined5CheckBox;
    }

    public JCheckBox getUserDefined6CheckBox()
    {
        return userDefined6CheckBox;
    }

    public JCheckBox getUserDefined7CheckBox()
    {
        return userDefined7CheckBox;
    }

    public JCheckBox getUserDefined8CheckBox()
    {
        return userDefined8CheckBox;
    }

    public JPanel getUserDefinedCheckBoxPanel()
    {
        return userDefinedCheckBoxPanel;
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

    public JLabel getEndResolutionLabel()
    {
        return endResolutionLabel;
    }

    public JSpinner getEndSpinner()
    {
        return endSpinner;
    }

    public JCheckBox getExcludeGaskellCheckBox()
    {
        return excludeGaskellCheckBox;
    }

    public JCheckBox getFilter10CheckBox()
    {
        return filter10CheckBox;
    }

    public JCheckBox getFilter11CheckBox()
    {
        return filter11CheckBox;
    }

    public JCheckBox getFilter12CheckBox()
    {
        return filter12CheckBox;
    }

    public JCheckBox getFilter13CheckBox()
    {
        return filter13CheckBox;
    }

    public JCheckBox getFilter14CheckBox()
    {
        return filter14CheckBox;
    }

    public JCheckBox getFilter15CheckBox()
    {
        return filter15CheckBox;
    }

    public JCheckBox getFilter16CheckBox()
    {
        return filter16CheckBox;
    }

    public JCheckBox getFilter17CheckBox()
    {
        return filter17CheckBox;
    }

    public JCheckBox getFilter18CheckBox()
    {
        return filter18CheckBox;
    }

    public JCheckBox getFilter19CheckBox()
    {
        return filter19CheckBox;
    }

    public JCheckBox getFilter1CheckBox()
    {
        return filter1CheckBox;
    }

    public JCheckBox getFilter20CheckBox()
    {
        return filter20CheckBox;
    }

    public JCheckBox getFilter21CheckBox()
    {
        return filter21CheckBox;
    }

    public JCheckBox getFilter22CheckBox()
    {
        return filter22CheckBox;
    }

    public JCheckBox getFilter2CheckBox()
    {
        return filter2CheckBox;
    }

    public JCheckBox getFilter3CheckBox()
    {
        return filter3CheckBox;
    }

    public JCheckBox getFilter4CheckBox()
    {
        return filter4CheckBox;
    }

    public JCheckBox getFilter5CheckBox()
    {
        return filter5CheckBox;
    }

    public JCheckBox getFilter6CheckBox()
    {
        return filter6CheckBox;
    }

    public JCheckBox getFilter7CheckBox()
    {
        return filter7CheckBox;
    }

    public JCheckBox getFilter8CheckBox()
    {
        return filter8CheckBox;
    }

    public JCheckBox getFilter9CheckBox()
    {
        return filter9CheckBox;
    }

    public JPanel getFilterCheckBoxPanel()
    {
        return filterCheckBoxPanel;
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

    public JLabel getFromResolutionLabel()
    {
        return fromResolutionLabel;
    }

    public JFormattedTextField getFromResolutionTextField()
    {
        return fromResolutionTextField;
    }

    public JComboBox getHasLimbComboBox()
    {
        return hasLimbComboBox;
    }

    public JLabel getHasLimbLabel()
    {
        return hasLimbLabel;
    }

    public JScrollPane getHierarchicalSearchScrollPane()
    {
        return hierarchicalSearchScrollPane;
    }

    public JButton getClearRegionButton()
    {
        return clearRegionButton;
    }

    public void enableFilenameSearch(boolean enable)
    {
//        boolean enable = evt.getStateChange() == ItemEvent.SELECTED;
        searchByNumberTextField.setEnabled(enable);
        startDateLabel.setEnabled(!enable);
        startSpinner.setEnabled(!enable);
        endDateLabel.setEnabled(!enable);
        endSpinner.setEnabled(!enable);
        filter1CheckBox.setEnabled(!enable);
        filter2CheckBox.setEnabled(!enable);
        filter3CheckBox.setEnabled(!enable);
        filter4CheckBox.setEnabled(!enable);
        filter5CheckBox.setEnabled(!enable);
        filter6CheckBox.setEnabled(!enable);
        filter7CheckBox.setEnabled(!enable);
        filter8CheckBox.setEnabled(!enable);
        filter9CheckBox.setEnabled(!enable);
        filter10CheckBox.setEnabled(!enable);
        filter11CheckBox.setEnabled(!enable);
        filter12CheckBox.setEnabled(!enable);
        filter13CheckBox.setEnabled(!enable);
        filter14CheckBox.setEnabled(!enable);
        filter15CheckBox.setEnabled(!enable);
        filter16CheckBox.setEnabled(!enable);
        filter17CheckBox.setEnabled(!enable);
        filter18CheckBox.setEnabled(!enable);
        filter19CheckBox.setEnabled(!enable);
        filter20CheckBox.setEnabled(!enable);
        filter21CheckBox.setEnabled(!enable);
        filter22CheckBox.setEnabled(!enable);
        userDefined1CheckBox.setEnabled(!enable);
        userDefined2CheckBox.setEnabled(!enable);
        userDefined3CheckBox.setEnabled(!enable);
        userDefined4CheckBox.setEnabled(!enable);
        userDefined5CheckBox.setEnabled(!enable);
        userDefined6CheckBox.setEnabled(!enable);
        userDefined7CheckBox.setEnabled(!enable);
        userDefined8CheckBox.setEnabled(!enable);
        hasLimbLabel.setEnabled(!enable);
        hasLimbComboBox.setEnabled(!enable);
        fromDistanceLabel.setEnabled(!enable);
        fromDistanceTextField.setEnabled(!enable);
        toDistanceLabel.setEnabled(!enable);
        toDistanceTextField.setEnabled(!enable);
        endDistanceLabel.setEnabled(!enable);
        fromResolutionLabel.setEnabled(!enable);
        fromResolutionTextField.setEnabled(!enable);
        toResolutionLabel.setEnabled(!enable);
        toResolutionTextField.setEnabled(!enable);
        endResolutionLabel.setEnabled(!enable);
        fromIncidenceLabel.setEnabled(!enable);
        fromIncidenceTextField.setEnabled(!enable);
        toIncidenceLabel.setEnabled(!enable);
        toIncidenceTextField.setEnabled(!enable);
        endIncidenceLabel.setEnabled(!enable);
        fromEmissionLabel.setEnabled(!enable);
        fromEmissionTextField.setEnabled(!enable);
        toEmissionLabel.setEnabled(!enable);
        toEmissionTextField.setEnabled(!enable);
        endEmissionLabel.setEnabled(!enable);
        fromPhaseLabel.setEnabled(!enable);
        fromPhaseTextField.setEnabled(!enable);
        toPhaseLabel.setEnabled(!enable);
        toPhaseTextField.setEnabled(!enable);
        endPhaseLabel.setEnabled(!enable);
    }//GEN-LAST:event_searchByFilenameCheckBoxItemStateChanged



}
