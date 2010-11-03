package edu.jhuapl.near.gui.eros;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import vtk.vtkPolyData;

import net.miginfocom.swing.MigLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.near.gui.AnyFileChooser;
import edu.jhuapl.near.gui.RadialOffsetChanger;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection.NLRMaskType;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.util.Properties;


public class NLRSearchPanel extends JPanel implements ActionListener, PropertyChangeListener
{	
	private final String NLR_REMOVE_ALL_BUTTON_TEXT = "Clear Data";
	
    private final ModelManager modelManager;
    private NLRSearchDataCollection nlrModel;
    private JLabel resultsLabel;
    private JButton removeAllButton;
    private RadialOffsetChanger radialOffsetChanger;
    
    private java.util.Date startDate = new GregorianCalendar(2000, 4, 1, 0, 0, 0).getTime();
    private java.util.Date endDate = new GregorianCalendar(2000, 4, 2, 0, 0, 0).getTime();
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JToggleButton selectRegionButton;
    private JButton nextButton;
    private JButton prevButton;
    private TreeSet<Integer> cubeList = new TreeSet<Integer>();
    private JComboBox shownResultsShowComboBox;
    private PickManager pickManager;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-d HH:mm:ss.SSS", Locale.US);
    private NLRPlot nlrPlot;
    
    private enum DisplayedResultsOptions
    {
    	ALL                 ("all",            NLRMaskType.NONE,        -1.0),
    	NEXT_POINT          ("1 point",        NLRMaskType.BY_NUMBER,   1.0),
    	NEXT_10_POINTS      ("10 points",      NLRMaskType.BY_NUMBER,   10.0),
    	NEXT_100_POINTS     ("100 points",     NLRMaskType.BY_NUMBER,   100.0),
    	NEXT_1000_POINTS    ("1000 points",    NLRMaskType.BY_NUMBER,   1000.0),
    	NEXT_5000_POINTS    ("5000 points",    NLRMaskType.BY_NUMBER,   5000.0),
    	NEXT_10000_POINTS   ("10000 points",   NLRMaskType.BY_NUMBER,   10000.0),
    	NEXT_SECOND         ("1 second",       NLRMaskType.BY_TIME,     1),
    	NEXT_MINUTE         ("1 minute",       NLRMaskType.BY_TIME,     60),
    	NEXT_HOUR           ("1 hour",         NLRMaskType.BY_TIME,     3600),
        NEXT_12_HOURS       ("12 hours",       NLRMaskType.BY_TIME,     43200),
    	NEXT_DAY            ("1 day",          NLRMaskType.BY_TIME,     86400),
    	NEXT_METER          ("1 meter",        NLRMaskType.BY_DISTANCE, 0.001),
    	NEXT_10_METERS      ("10 meters",      NLRMaskType.BY_DISTANCE, 0.01),
    	NEXT_100_METERS     ("100 meters",     NLRMaskType.BY_DISTANCE, 0.1),
    	NEXT_KILOMETER      ("1 kilometer",    NLRMaskType.BY_DISTANCE, 1.0),
        NEXT_5_KILOMETERS   ("5 kilometers",   NLRMaskType.BY_DISTANCE, 5.0),
        NEXT_10_KILOMETERS  ("10 kilometers",  NLRMaskType.BY_DISTANCE, 10.0),
        NEXT_50_KILOMETERS  ("50 kilometers",  NLRMaskType.BY_DISTANCE, 50.0),
        NEXT_100_KILOMETERS ("100 kilometers", NLRMaskType.BY_DISTANCE, 100.0);
    
    	private final String name;
    	private final NLRMaskType type;
    	private final double value;

    	private DisplayedResultsOptions(String name, NLRMaskType type, double value)
    	{
    		this.name = name;
    		this.type = type;
    		this.value = value;
    	}
    	
    	public String toString()
    	{
    		return name;
    	}
    	
    	public NLRMaskType getType()
    	{
    		return type;
    	}
    	
    	public double getValue()
    	{
    		return value;
    	}
    }
    

    public NLRSearchPanel(
    		final ModelManager modelManager,
            final PickManager pickManager) 
    {
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));
    	
    	this.modelManager = modelManager;
    	this.pickManager = pickManager;
    	
		this.addComponentListener(new ComponentAdapter() 
		{
			public void componentHidden(ComponentEvent e)
			{
		    	selectRegionButton.setSelected(false);
				pickManager.setPickMode(PickMode.DEFAULT);
			}
		});
		
		pickManager.getDefaultPicker().addPropertyChangeListener(this);


		this.nlrModel = (NLRSearchDataCollection)modelManager.getModel(ModelNames.NLR_DATA_SEARCH);
    	
        JPanel pane = new JPanel();
        pane.setLayout(new MigLayout("wrap 1"));

        final JPanel startDatePanel = new JPanel();
        this.startDateLabel = new JLabel(START_DATE_LABEL_TEXT);
        startDatePanel.add(this.startDateLabel);
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
        startDatePanel.add(startSpinner);
        startSpinner.setEnabled(true);
        pane.add(startDatePanel);

        final JPanel endDatePanel = new JPanel();
        this.endDateLabel = new JLabel(END_DATE_LABEL_TEXT);
        endDatePanel.add(this.endDateLabel);
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
        endDatePanel.add(endSpinner);
        endSpinner.setEnabled(true);
        pane.add(endDatePanel);

        JPanel selectRegionPanel = new JPanel();
        //selectRegionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        selectRegionButton = new JToggleButton("Select Region");
        selectRegionButton.setEnabled(true);
        selectRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                if (selectRegionButton.isSelected())
                    pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
                else
                    pickManager.setPickMode(PickMode.DEFAULT);
            }
        });
        selectRegionPanel.add(selectRegionButton);

        final JButton clearRegionButton = new JButton("Clear Region");
        clearRegionButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
                selectionModel.removeAllStructures();
                cubeList.clear();
            }
        });
        selectRegionPanel.add(clearRegionButton);

        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Search");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);

        pane.add(selectRegionPanel, "align center");
        pane.add(submitPanel, "align center");

		resultsLabel = new JLabel("<html><br><br></html>");
		resultsLabel.setPreferredSize(new Dimension(300, 200));
		resultsLabel.setBorder(BorderFactory.createEtchedBorder());
		
        removeAllButton = new JButton(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NLRSearchDataCollection model = (NLRSearchDataCollection)modelManager.getModel(ModelNames.NLR_DATA_SEARCH);
				model.removeAllNlrData();
		        if (nlrPlot != null)
		            nlrPlot.updateData();
			}
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        pane.add(resultsLabel);

        JLabel showLabel = new JLabel("Show ");
        
    	shownResultsShowComboBox = new JComboBox(DisplayedResultsOptions.values());
    	shownResultsShowComboBox.setSelectedIndex(0);
    	
    	pane.add(showLabel, "split");
    	pane.add(shownResultsShowComboBox);
    	
		nextButton = new JButton(">");
        nextButton.setActionCommand(">");
        nextButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				showData(1, false);
			}
        });
        nextButton.setEnabled(true);

        prevButton = new JButton("<");
        prevButton.setActionCommand("<");
        prevButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				showData(-1, false);
			}
        });
        prevButton.setEnabled(true);

        pane.add(prevButton);
        pane.add(nextButton, "wrap");
        
        pane.add(removeAllButton, "align center");
		
        JButton plotPotentialButton = new JButton("Plot Potential");
        plotPotentialButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (nlrPlot == null)
				    nlrPlot = new NLRPlot(nlrModel);
				nlrPlot.setVisible(true);
			}
		});
        
        pane.add(plotPotentialButton, "align center");

        final JButton saveButton = new JButton("Save...");
        saveButton.setActionCommand("Save...");
        saveButton.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		int index = 1;
        		if (index >= 0)
        		{
        			File file = AnyFileChooser.showSaveDialog(
        					saveButton.getParent(),
        					"Save NLR data");
        			
        			try
        			{
        				if (file != null)
        				{
        					nlrModel.saveNlrDataSql(file);
        				}
        			}
        			catch(Exception ex)
        			{
        				JOptionPane.showMessageDialog(saveButton.getParent(),
        						"Unable to save file to " + file.getAbsolutePath(),
        						"Error Saving File",
        						JOptionPane.ERROR_MESSAGE);
        				ex.printStackTrace();
        			}

        		}
        	}
        });
        saveButton.setEnabled(true);

        //pane.add(saveButton, "align center");

        radialOffsetChanger = new RadialOffsetChanger(nlrModel, "Radial Offset");

        add(pane);
        add(radialOffsetChanger);
    }

	


    public void actionPerformed(ActionEvent e)
    {
    	selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
		SmallBodyModel erosModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
		if (selectionModel.getNumberOfStructures() > 0)
		{
			RegularPolygonModel.RegularPolygon region = (RegularPolygonModel.RegularPolygon)selectionModel.getStructure(0);
			
			// Always use the lowest resolution model for getting the intersection cubes list.
			// Therefore, if the selection region was created using a higher resolution model,
			// we need to recompute the selection region using the low res model.
			if (erosModel.getModelResolution() > 0)
			{
				vtkPolyData interiorPoly = new vtkPolyData();
				erosModel.drawPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
				cubeList = nlrModel.getIntersectingCubes(interiorPoly);
			}
			else
			{
				cubeList = nlrModel.getIntersectingCubes(region.interiorPolyData);
			}
		}

		showData(1, true);
    }

    private void showData(int direction, boolean reset)
	{
    	DisplayedResultsOptions option = (DisplayedResultsOptions)shownResultsShowComboBox.getSelectedItem();
    	
    	GregorianCalendar startCal = new GregorianCalendar();
    	startCal.setTimeInMillis(startDate.getTime());
    	GregorianCalendar stopCal = new GregorianCalendar();
    	stopCal.setTimeInMillis(endDate.getTime());

    	try
		{
			nlrModel.setNlrData(startCal, stopCal, cubeList, option.getType(), direction*option.getValue(), reset);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		int[] range = nlrModel.getMaskedPointRange();

        String resultsText = 
            "<html>" + nlrModel.getNumberOfPoints() + " points matched<br><br>";
        
        if (nlrModel.getNumberOfPoints() > 0)
        {
            long t0 = nlrModel.getTimeOfPoint(range[0]);
            long t1 = nlrModel.getTimeOfPoint(range[1]);

            resultsText += "Showing points " + (range[0]+1) + " through " + (range[1]+1) + ", " +
            " from " + sdf.format(new Date(t0)) + " until " + sdf.format(new Date(t1)) + "<br><br>" +
            "Number of points shown: " + (range[1] - range[0] + 1) + "<br>" +
            "Time range: " + ((double)(t1-t0)/1000.0) + " seconds<br>" +
            "Distance: " + (float)nlrModel.getLengthOfMaskedPoints() + " km";
        }
        
        resultsText += "</html>";
        
        resultsLabel.setText(resultsText);
        
        if (nlrPlot != null)
            nlrPlot.updateData();
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
		{
			PickEvent e = (PickEvent)evt.getNewValue();
			if (modelManager.getModel(e.getPickedProp()) == nlrModel &&
			        nlrModel.isDataPointsProp(e.getPickedProp()))
			{
				int id = e.getPickedCellId();
				nlrModel.selectPoint(id);
				if (nlrPlot != null)
					nlrPlot.selectPoint(id);
			}
		}
	}

}
