package edu.jhuapl.near.gui;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;

import org.joda.time.*;

import edu.jhuapl.near.database.Database;
import edu.jhuapl.near.model.ModelManager;


public class SearchPanel extends JPanel implements ActionListener 
{
    private ModelManager modelManager;
    private java.util.Date startDate = new DateTime(2000, 1, 11, 0, 0, 0, 0).toDate();
    private java.util.Date endDate = new DateTime(2001, 2, 13, 0, 0, 0, 0).toDate();
//    private ShapeBuilderWidget shapePanel;
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JComboBox queryTypeComboBox;
    private SearchResultsPanel resultsPanel;
    private JCheckBox filter1CheckBox;
    private JCheckBox filter2CheckBox;
    private JCheckBox filter3CheckBox;
    private JCheckBox filter4CheckBox;
    private JCheckBox filter5CheckBox;
    private JCheckBox filter6CheckBox;
    private JCheckBox filter7CheckBox;

    private JCheckBox iofdblCheckBox;
    private JCheckBox cifdblCheckBox;

    
    public SearchPanel(ModelManager modelManager) 
    {
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));
    	
    	this.modelManager = modelManager;
    	
    	JPanel pane = new JPanel();
    	pane.setLayout(new BoxLayout(pane,
        		BoxLayout.PAGE_AXIS));

    	//pane.setBorder(
        //        new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Editor")));

        //String [] options = {"MSI", "NIS", "NLR"};
        String [] options = {"MSI"};
        queryTypeComboBox = new JComboBox(options);                                             
        queryTypeComboBox.setEditable(false);                                                   
        pane.add(queryTypeComboBox);                     

        JPanel panel = new JPanel();
        this.startDateLabel = new JLabel(START_DATE_LABEL_TEXT);
        panel.add(this.startDateLabel);
        startSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    java.util.Date date = 
                        ((SpinnerDateModel)startSpinner.getModel()).getDate();
                    if (date != null)
                        startDate = date;
                }
            });
        panel.add(startSpinner);
        startSpinner.setEnabled(true);
        pane.add(panel);

        panel = new JPanel();
        this.endDateLabel = new JLabel(END_DATE_LABEL_TEXT);
        panel.add(this.endDateLabel);
        endSpinner = new JSpinner(new SpinnerDateModel(endDate, null, null, Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    java.util.Date date = 
                        ((SpinnerDateModel)endSpinner.getModel()).getDate();
                    if (date != null)
                        endDate = date;
                }
            });
        panel.add(endSpinner);
        endSpinner.setEnabled(true);
        pane.add(panel);


        //panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        //JLabel label = new JLabel("Select Region:");
        //panel.add(label);

        //this.shapePanel = new ShapeBuilderWidget(wwd, this);
        //panel.add(this.shapePanel);
        //pane.add(panel);

        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel,
        		BoxLayout.LINE_AXIS));
        filter1CheckBox = new JCheckBox();
    	filter1CheckBox.setText("Filter 1");
    	filter1CheckBox.setSelected(true);
        filter2CheckBox = new JCheckBox();
    	filter2CheckBox.setText("Filter 2");
    	filter2CheckBox.setSelected(true);
        filter3CheckBox = new JCheckBox();
    	filter3CheckBox.setText("Filter 3");
    	filter3CheckBox.setSelected(true);
        filter4CheckBox = new JCheckBox();
    	filter4CheckBox.setText("Filter 4");
    	filter4CheckBox.setSelected(true);
        filter5CheckBox = new JCheckBox();
    	filter5CheckBox.setText("Filter 5");
    	filter5CheckBox.setSelected(true);
        filter6CheckBox = new JCheckBox();
    	filter6CheckBox.setText("Filter 6");
    	filter6CheckBox.setSelected(true);
        filter7CheckBox = new JCheckBox();
    	filter7CheckBox.setText("Filter 7");
    	filter7CheckBox.setSelected(true);
    	
    	JPanel filtersSub1Panel = new JPanel();
        filtersSub1Panel.setLayout(new BoxLayout(filtersSub1Panel,
        		BoxLayout.PAGE_AXIS));
    	filtersSub1Panel.add(filter1CheckBox);
    	filtersSub1Panel.add(filter2CheckBox);
        filtersSub1Panel.add(filter3CheckBox);
    	filtersSub1Panel.add(filter4CheckBox);
    	JPanel filtersSub2Panel = new JPanel();
        filtersSub2Panel.setLayout(new BoxLayout(filtersSub2Panel,
        		BoxLayout.PAGE_AXIS));
        
    	filtersSub2Panel.add(filter5CheckBox);
    	filtersSub2Panel.add(filter6CheckBox);
    	filtersSub2Panel.add(filter7CheckBox);

    	filtersPanel.add(filtersSub1Panel);
        filtersPanel.add(Box.createHorizontalStrut(15));
    	filtersPanel.add(filtersSub2Panel);
    	    	
    	filtersPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

    	JPanel iofcifPanel = new JPanel();
        iofcifPanel.setLayout(new BoxLayout(iofcifPanel,
        		BoxLayout.LINE_AXIS));
        iofcifPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

        iofdblCheckBox = new JCheckBox();
        iofdblCheckBox.setText("iofdbl");
        iofdblCheckBox.setSelected(true);
        cifdblCheckBox = new JCheckBox();
        cifdblCheckBox.setText("cifdbl");
        cifdblCheckBox.setSelected(true);

        iofcifPanel.add(iofdblCheckBox);
        iofcifPanel.add(Box.createHorizontalStrut(15));
        iofcifPanel.add(cifdblCheckBox);

        panel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Update");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        panel.add(submitButton);


        pane.add(filtersPanel);
        pane.add(iofcifPanel);
        
    	pane.add(panel);
        
        this.add(pane);

        resultsPanel = new SearchResultsPanel(this.modelManager);
        
        add(resultsPanel);

    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
        	ArrayList<Integer> filtersChecked = new ArrayList<Integer>();
    		
    		if (filter1CheckBox.isSelected())
    			filtersChecked.add(1);
    		if (filter2CheckBox.isSelected())
    			filtersChecked.add(2);
    		if (filter3CheckBox.isSelected())
    			filtersChecked.add(3);
    		if (filter4CheckBox.isSelected())
    			filtersChecked.add(4);
    		if (filter5CheckBox.isSelected())
    			filtersChecked.add(5);
    		if (filter6CheckBox.isSelected())
    			filtersChecked.add(6);
    		if (filter7CheckBox.isSelected())
    			filtersChecked.add(7);

    		ArrayList<String> results = Database.getInstance().runQuery(
        			new LocalDateTime(startDate), 
        			new LocalDateTime(endDate),
        			filtersChecked,
        			iofdblCheckBox.isSelected(),
        			cifdblCheckBox.isSelected());
        	
        	resultsPanel.setResults(results);
        }
        catch (Exception e) 
        { 
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }
}
