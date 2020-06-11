package edu.jhuapl.sbmt.spectrum.ui.search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Panel to display and change spectrum search parameters
 * @author steelrj1
 *
 */
public class SpectrumSearchParametersPanel  extends JPanel
{
    protected CheckBoxTree checkBoxTree;
    private JFormattedTextField searchByNumberTextField;
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
    private JLabel dataTypesLabel;
    protected JScrollPane hierarchicalSearchScrollPane;
    private JButton clearRegionButton;
    private JTextField textField;
    private JPanel auxPanel;
    private JRadioButton parametersRadioButton;
    private JRadioButton filenameRadioButton;
    private JCheckBox fullCheckBox;
    private JCheckBox partialCheckBox;
    private JCheckBox degenerateCheckBox;
    private boolean isHierarchical;
    private ButtonGroup searchByGroup;
    private JPanel parametersPanel;
    private JPanel filenamePanel;

    private String[] dataTypes;
    private JRadioButton[] dataTypeRadioButtons;
    private ButtonGroup dataTypeGroup;

    private boolean isFixedListSearch = false;

    public SpectrumSearchParametersPanel(boolean isHierarchical, String[] dataTypes)
    {
    	this.dataTypes = dataTypes;
        setBorder(new TitledBorder(null, "Search Parameters",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.isHierarchical = isHierarchical;
        if (isHierarchical) initHierarchicalSearch();
        else initParameterSearch();

        JPanel panel_10 = new JPanel();
        add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        selectRegionButton = new JToggleButton("Select Region");
        panel_10.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        panel_10.add(clearRegionButton);

        submitButton = new JButton("Search");
        panel_10.add(submitButton);

        Component verticalStrut_1 = Box.createVerticalStrut(20);
        add(verticalStrut_1);
    }

    private void initHierarchicalSearch()
    {
        // TODO: Override and setup in subclass
        hierarchicalSearchScrollPane = new JScrollPane();
        hierarchicalSearchScrollPane.setPreferredSize(new Dimension(150, 150));
        add(hierarchicalSearchScrollPane);
    }


    private void initParameterSearch()
    {
        JPanel choicePanel = new JPanel();
        add(choicePanel);

        parametersRadioButton = new JRadioButton(
                "Search by Parameters");
        if (isFixedListSearch == false)
        	choicePanel.add(parametersRadioButton);


        filenameRadioButton = new JRadioButton(
                "Search by Filename");
        choicePanel.add(filenameRadioButton);


        searchByGroup = new ButtonGroup();
        searchByGroup.add(filenameRadioButton);
        searchByGroup.add(parametersRadioButton);
        parametersRadioButton.setSelected(true);

        filenamePanel = new JPanel();
        filenamePanel.setVisible(false);
        add(filenamePanel);
        filenamePanel.setLayout(new BoxLayout(filenamePanel, BoxLayout.X_AXIS));

        JLabel lblFilename = new JLabel("Filename:");
        filenamePanel.add(lblFilename);

        searchByNumberTextField = new JFormattedTextField();
        filenamePanel.add(searchByNumberTextField);
        searchByNumberTextField.setColumns(30);
        searchByNumberTextField.setPreferredSize(
                new Dimension(200, 20));
        searchByNumberTextField.setMaximumSize(
                new Dimension(200, 20));

        Component horizontalGlue = Box.createHorizontalGlue();
        filenamePanel.add(horizontalGlue);

        parametersPanel = new JPanel();
        add(parametersPanel);
        parametersPanel
                .setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));

        parametersRadioButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                parametersPanel.setVisible(true);
                filenamePanel.setVisible(false);
            }
        });

        filenameRadioButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                parametersPanel.setVisible(false);
                filenamePanel.setVisible(true);
            }
        });

        JPanel panel_1 = new JPanel();
        parametersPanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        startDateLabel = new JLabel("Start Date:");
        panel_1.add(startDateLabel);

        startSpinner = new JSpinner();
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setModel(new javax.swing.SpinnerDateModel(
                new java.util.Date(1126411200000L), null, null,
                java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner,
                "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMaximumSize(
                new java.awt.Dimension(startSpinner.getWidth(), 22));
        panel_1.add(startSpinner);

        Component horizontalGlue_8 = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue_8);

        Component verticalStrut_8 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_8);

        JPanel panel_2 = new JPanel();
        parametersPanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        endDateLabel = new JLabel("  End Date:");
        panel_2.add(endDateLabel);

        endSpinner = new JSpinner();
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setModel(new javax.swing.SpinnerDateModel(
                new java.util.Date(1126411200000L), null, null,
                java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner,
                "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMaximumSize(
                new java.awt.Dimension(endSpinner.getWidth(), 22));
        panel_2.add(endSpinner);

        Component horizontalGlue_9 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_9);

        Component verticalStrut_7 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_7);

        JPanel panel_3 = new JPanel();
        parametersPanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        dataTypesLabel = new JLabel("Data Types:");
        panel_3.add(dataTypesLabel);

        dataTypeRadioButtons = new JRadioButton[dataTypes.length];
        dataTypeGroup = new ButtonGroup();
        int i=0;
        for (String dataType : dataTypes)
        {
        	dataTypeRadioButtons[i] = new JRadioButton(dataType);
        	panel_3.add(dataTypeRadioButtons[i]);
        	dataTypeGroup.add(dataTypeRadioButtons[i]);
        	i++;
        }
        dataTypeRadioButtons[0].setSelected(true);
        dataTypeGroup.setSelected(dataTypeRadioButtons[0].getModel(), true);

//        hasLimbLabel = new JLabel("Field of View Polygon Type:");
//        panel_3.add(hasLimbLabel);
//
//        fullCheckBox = new JCheckBox("Full");
//        panel_3.add(fullCheckBox);
//
//        partialCheckBox = new JCheckBox("Partial");
//        panel_3.add(partialCheckBox);

//        degenerateCheckBox = new JCheckBox("Degenerate");
//        panel_3.add(degenerateCheckBox);

        Component horizontalGlue_6 = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue_6);

        Component verticalStrut_6 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_6);

        JPanel panel_4 = new JPanel();
        parametersPanel.add(panel_4);

        JLabel lblScDistanceFrom = new JLabel("S/C Distance from");
        panel_4.add(lblScDistanceFrom);

        fromDistanceTextField = new JFormattedTextField();
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setMaximumSize(
                new Dimension(fromDistanceTextField.getWidth(), 20));
        fromDistanceTextField.setColumns(5);
        panel_4.add(fromDistanceTextField);

        panel_4.add(new JLabel("to"));

        toDistanceTextField = new JFormattedTextField();
        toDistanceTextField.setText("1000");
        toDistanceTextField.setMaximumSize(
                new Dimension(toDistanceTextField.getWidth(), 20));
        toDistanceTextField.setColumns(5);
        panel_4.add(toDistanceTextField);

        JLabel lblKm = new JLabel("km");
        panel_4.add(lblKm);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue_1);

        Component verticalStrut_5 = Box.createVerticalStrut(5);
        parametersPanel.add(verticalStrut_5);

        JPanel panel_6 = new JPanel();
        parametersPanel.add(panel_6);

        fromIncidenceLabel = new JLabel("      Incidence from");
        panel_6.add(fromIncidenceLabel);

        fromIncidenceTextField = new JFormattedTextField();
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setMaximumSize(
                new Dimension(fromIncidenceTextField.getWidth(), 20));
        fromIncidenceTextField.setColumns(5);
        panel_6.add(fromIncidenceTextField);

        toIncidenceLabel = new JLabel("to");
        panel_6.add(toIncidenceLabel);

        toIncidenceTextField = new JFormattedTextField();
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setMaximumSize(
                new Dimension(toIncidenceTextField.getWidth(), 20));
        toIncidenceTextField.setColumns(5);
        panel_6.add(toIncidenceTextField);

        panel_6.add(new JLabel("deg"));
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        panel_6.add(horizontalGlue_3);

        Component verticalStrut_3 = Box.createVerticalStrut(5);
        parametersPanel.add(verticalStrut_3);

        JPanel panel_7 = new JPanel();
        parametersPanel.add(panel_7);

        fromEmissionLabel = new JLabel("      Emission from");
        panel_7.add(fromEmissionLabel);

        fromEmissionTextField = new JFormattedTextField();
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setMaximumSize(
                new Dimension(fromEmissionTextField.getWidth(), 20));
        fromEmissionTextField.setColumns(5);
        panel_7.add(fromEmissionTextField);

        toEmissionLabel = new JLabel("to");
        panel_7.add(toEmissionLabel);

        toEmissionTextField = new JFormattedTextField();
        toEmissionTextField.setText("180");
        toEmissionTextField.setMaximumSize(
                new Dimension(toEmissionTextField.getWidth(), 20));
        toEmissionTextField.setColumns(5);
        panel_7.add(toEmissionTextField);

        panel_7.add(new JLabel("deg"));
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        Component horizontalGlue_4 = Box.createHorizontalGlue();
        panel_7.add(horizontalGlue_4);

        Component verticalStrut_2 = Box.createVerticalStrut(5);
        parametersPanel.add(verticalStrut_2);

        JPanel panel_8 = new JPanel();
        parametersPanel.add(panel_8);

        fromPhaseLabel = new JLabel("           Phase from");
        panel_8.add(fromPhaseLabel);

        fromPhaseTextField = new JFormattedTextField();
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setMaximumSize(
                new Dimension(fromPhaseTextField.getWidth(), 20));
        fromPhaseTextField.setColumns(5);
        panel_8.add(fromPhaseTextField);

        toPhaseLabel = new JLabel("to");
        panel_8.add(toPhaseLabel);

        toPhaseTextField = new JFormattedTextField();
        toPhaseTextField.setText("180");
        toPhaseTextField
                .setMaximumSize(new Dimension(toPhaseTextField.getWidth(), 20));
        toPhaseTextField.setColumns(5);
        panel_8.add(toPhaseTextField);

        panel_8.add(new JLabel("deg"));
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        Component horizontalGlue_5 = Box.createHorizontalGlue();
        panel_8.add(horizontalGlue_5);

        auxPanel = new JPanel();
        parametersPanel.add(auxPanel);
        auxPanel.setLayout(new BoxLayout(auxPanel, BoxLayout.Y_AXIS));
    }

    protected List<BasicSpectrum> processResults(List<BasicSpectrum> input)
    {
        return input;
    }

    public CheckBoxTree getCheckBoxTree()
    {
        return checkBoxTree;
    }

    public void setCheckBoxTree(CheckBoxTree checkBoxTree)
    {
        this.checkBoxTree = checkBoxTree;
    }

    public JFormattedTextField getSearchByNumberTextField()
    {
        return searchByNumberTextField;
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

    public JLabel getHasLimbLabel()
    {
        return dataTypesLabel;
    }

    public JScrollPane getHierarchicalSearchScrollPane()
    {
        return hierarchicalSearchScrollPane;
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

    public JRadioButton getFilenameRadioButton()
    {
        return filenameRadioButton;
    }

    public JCheckBox getFullCheckBox()
    {
        return fullCheckBox;
    }

    public JCheckBox getPartialCheckBox()
    {
        return partialCheckBox;
    }

    public JCheckBox getDegenerateCheckBox()
    {
        return degenerateCheckBox;
    }

    @Override
	public Dimension getMinimumSize()
	{
    	if (isHierarchical)
    		return new Dimension(650, 175);
    	else if (parametersRadioButton.isSelected() == false)
    		return new Dimension(650, 175);
    	return new Dimension(650, 325);
	}

    @Override
	public Dimension getPreferredSize()
	{
    	if (isHierarchical)
    		return new Dimension(650, 175);
    	else if (parametersRadioButton.isSelected() == false)
    		return new Dimension(650, 175);
    	return new Dimension(650, 325);
	}

    @Override
	public Dimension getMaximumSize()
	{
    	if (isHierarchical)
    		return new Dimension(650, 175);
    	else if (parametersRadioButton.isSelected() == false)
    		return new Dimension(650, 175);
    	else
    		return new Dimension(650, 325);
	}

    public void setFixedListSearch(boolean isFixedListSearch)
	{
		this.isFixedListSearch = isFixedListSearch;
		if (isFixedListSearch)
		{
			 parametersPanel.setVisible(false);
             filenamePanel.setVisible(true);
             selectRegionButton.setVisible(false);
             clearRegionButton.setVisible(false);
             parametersRadioButton.setVisible(false);
             filenameRadioButton.setVisible(false);
		}
		else
		{
			searchByNumberTextField.setText("");
            parametersPanel.setVisible(true);
            filenamePanel.setVisible(false);
            selectRegionButton.setVisible(true);
            clearRegionButton.setVisible(true);
            parametersRadioButton.setVisible(true);
            filenameRadioButton.setVisible(true);
		}
	}

	public String getSelectedDataTypes()
	{
		for (JRadioButton button : dataTypeRadioButtons)
		{
			if (button.isSelected()) return button.getText().toLowerCase();
		}
		return "";
	}

}
