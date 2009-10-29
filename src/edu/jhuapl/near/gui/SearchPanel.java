package edu.jhuapl.near.gui;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.event.*;

import org.joda.time.*;

import edu.jhuapl.near.database.MSIDatabase;
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

    public SearchPanel(ModelManager modelManager) 
    {
        //super(new GridBagLayout());
    	
    	this.modelManager = modelManager;
    	
    	JPanel pane = new JPanel();
    	pane.setLayout(new BoxLayout(pane,
        		BoxLayout.PAGE_AXIS));

    	//GridBagConstraints c = new GridBagConstraints();
        //c.gridwidth = GridBagConstraints.REMAINDER;
    	//c.fill = GridBagConstraints.BOTH;
    	//c.weightx = 1.0;
    	//c.weighty = 1.0;

        pane.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
                                   new TitledBorder("Query Editor")));

        String [] options = {"MSI", "NIS", "NLR"};
        queryTypeComboBox = new JComboBox(options);                                             
        queryTypeComboBox.setEditable(false);                                                   
        pane.add(queryTypeComboBox);//, c);                     

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
        pane.add(panel);//, c);

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
        pane.add(panel);//, c);


        //panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        //JLabel label = new JLabel("Select Region:");
        //panel.add(label);

        //this.shapePanel = new ShapeBuilderWidget(wwd, this);
        //panel.add(this.shapePanel);
        //pane.add(panel);//, c);

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Update");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        panel.add(submitButton);
        pane.add(panel);//, c);
        
        this.add(pane);

        resultsPanel = new SearchResultsPanel();
        
        add(resultsPanel);

    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
        	ArrayList<String> results = MSIDatabase.getInstance().runQuery(new LocalDateTime(startDate), new LocalDateTime(endDate));
        	
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
