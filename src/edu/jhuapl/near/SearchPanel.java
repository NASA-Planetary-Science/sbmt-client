package edu.jhuapl.near;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import org.joda.time.*;

public class SearchPanel extends JPanel implements ActionListener 
{
    private ImageGLWidget viewer;

    private java.util.Date startDate = new DateTime(2005, 1, 1, 0, 0, 0, 0).toDate();
    private java.util.Date endDate = new DateTime(2010, 1, 1, 0, 0, 0, 0).toDate();
//    private ShapeBuilderWidget shapePanel;
    private String queryType;
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    //private double timeIntervalDays = 60.0;
    private int timeIntervalDays = 60;
    private double maxSearchRadius = 1.0; // used only for queries searching for ieds near caches and vice versa
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JSpinner timeIntervalSpinner;

    public SearchPanel(ImageGLWidget viewer) 
    {
        super(new GridBagLayout());
		this.viewer = viewer;


    	GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
    	c.fill = GridBagConstraints.BOTH;
    	c.weightx = 1.0;
    	c.weighty = 1.0;

        this.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
                                   new TitledBorder("Query Editor")));

        JPanel panel = new JPanel();
        this.startDateLabel = new JLabel(START_DATE_LABEL_TEXT);
        panel.add(this.startDateLabel);
        startSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "dd MMM yyyy"));
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
        this.add(panel, c);

        panel = new JPanel();
        this.endDateLabel = new JLabel(END_DATE_LABEL_TEXT);
        panel.add(this.endDateLabel);
        endSpinner = new JSpinner(new SpinnerDateModel(endDate, null, null, Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "dd MMM yyyy"));
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
        this.add(panel, c);

        panel = new JPanel();
        final JLabel timeIntervalLabel = new JLabel("Time Interval (days)");
        panel.add(timeIntervalLabel);
        //Spinner timeIntervalSpinner = new JSpinner(new SpinnerNumberModel(this.timeIntervalDays, 0.0, Double.MAX_VALUE, 1.0));
        timeIntervalSpinner = new JSpinner(new SpinnerNumberModel(this.timeIntervalDays, 0, Integer.MAX_VALUE, 1));
        timeIntervalSpinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                	
                	//Double d = 
                    //    (Double)((SpinnerNumberModel)timeIntervalSpinner.getModel()).getValue();
                    Integer d = 
                        (Integer)((SpinnerNumberModel)timeIntervalSpinner.getModel()).getValue();
                    if (d != null)
                    	timeIntervalDays = d;
                }
            });
        panel.add(timeIntervalSpinner);
        this.add(panel, c);
        //timeIntervalLabel.setVisible(false);
        //timeIntervalSpinner.setVisible(false);
        timeIntervalLabel.setEnabled(false);
        timeIntervalSpinner.setEnabled(false);

        /*panel = new JPanel();
        final JLabel radiusLabel = new JLabel("Radius (km)");
        panel.add(radiusLabel);
        final JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(this.maxSearchRadius, 0.0, Double.MAX_VALUE, 1.0));
    	radiusSpinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                Double d = 
                    (Double)((SpinnerNumberModel)timeIntervalSpinner.getModel()).getValue();
                if (d != null)
                	maxSearchRadius = d;
            }
        });
    	radiusSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
    	panel.add(radiusSpinner);
    	this.add(panel, c);
    	radiusSpinner.setEnabled(false);
    	radiusLabel.setEnabled(false);*/

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel label = new JLabel("Select Region:");
        panel.add(label);

        //this.shapePanel = new ShapeBuilderWidget(wwd, this);
        //panel.add(this.shapePanel);
        this.add(panel, c);

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Update");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        panel.add(submitButton);
        this.add(panel, c);
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
        }
        catch (Exception e) 
        { 
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }
}
