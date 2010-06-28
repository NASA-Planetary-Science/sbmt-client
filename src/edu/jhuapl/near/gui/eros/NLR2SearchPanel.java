package edu.jhuapl.near.gui.eros;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import vtk.vtkPolyData;

import net.miginfocom.swing.MigLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import edu.jhuapl.near.gui.RadialOffsetChanger;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.model.eros.NLRDataCollection2;
import edu.jhuapl.near.model.eros.NLRDataCollection2.NLRMaskType;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;


public class NLR2SearchPanel extends JPanel implements ActionListener
{
	private final String NLR_REMOVE_ALL_BUTTON_TEXT = "Remove All NLR Data";
	
    private final ErosModelManager modelManager;
    private NLRDataCollection2 nlrModel;
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
    
    private enum DisplayedResultsOptions
    {
    	ALL              ("all",              NLRMaskType.NONE,        -1.0),
    	NEXT_POINT       ("next point",       NLRMaskType.BY_NUMBER,   1.0),
    	NEXT_10_POINTS   ("next 10 points",   NLRMaskType.BY_NUMBER,   10.0),
    	NEXT_100_POINTS  ("next 100 points",  NLRMaskType.BY_NUMBER,   100.0),
    	NEXT_1000_POINTS ("next 1000 points", NLRMaskType.BY_NUMBER,   1000.0),
    	NEXT_SECOND      ("next second",      NLRMaskType.BY_TIME,     1),
    	NEXT_MINUTE      ("next minute",      NLRMaskType.BY_TIME,     60),
    	NEXT_HOUR        ("next hour",        NLRMaskType.BY_TIME,     3600),
    	NEXT_DAY         ("next day",         NLRMaskType.BY_TIME,     86400),
    	NEXT_METER       ("next meter",       NLRMaskType.BY_DISTANCE, 0.001),
    	NEXT_10_METERS   ("next 10 meters",   NLRMaskType.BY_DISTANCE, 0.01),
    	NEXT_100_METERS  ("next 100 meters",  NLRMaskType.BY_DISTANCE, 0.1),
    	NEXT_1000_METERS ("next kilometer",   NLRMaskType.BY_DISTANCE, 1.0);
    
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
    

    public NLR2SearchPanel(
    		final ErosModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
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

		this.nlrModel = (NLRDataCollection2)modelManager.getModel(ErosModelManager.NLR_DATA);
    	
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
                RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ErosModelManager.CIRCLE_SELECTION);
                selectionModel.removeAllStructures();
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
		resultsLabel.setPreferredSize(new Dimension(250, 80));
		resultsLabel.setBorder(BorderFactory.createEtchedBorder());
		
        removeAllButton = new JButton(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NLRDataCollection2 model = (NLRDataCollection2)modelManager.getModel(ErosModelManager.NLR_DATA);
				model.removeAllNlrData();
			}
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        pane.add(resultsLabel);

        JLabel showLabel = new JLabel("Show ");
        
    	shownResultsShowComboBox = new JComboBox(DisplayedResultsOptions.values());
    	shownResultsShowComboBox.setSelectedIndex(4);
    	
    	pane.add(showLabel, "split");
    	pane.add(shownResultsShowComboBox);
    	
		nextButton = new JButton(">");
        nextButton.setActionCommand(">");
        nextButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				showData(1);
			}
        });
        nextButton.setEnabled(true);

        prevButton = new JButton("<");
        prevButton.setActionCommand("<");
        prevButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				showData(-1);
			}
        });
        prevButton.setEnabled(true);

        pane.add(prevButton);
        pane.add(nextButton, "wrap");
        
        pane.add(removeAllButton, "align center");
		
//        JButton plotPotentialPlotVsTimeButton = new JButton("Plot Potential vs. Time");
//        JButton plotPotentialPlotVsDistanceButton = new JButton("Plot Potential vs. Distance");
//
//        pane.add(plotPotentialPlotVsTimeButton, "align center");
//        pane.add(plotPotentialPlotVsDistanceButton, "align center");
        
        radialOffsetChanger = new RadialOffsetChanger(nlrModel, "Radial Offset");

        add(pane);
        add(radialOffsetChanger);
    }

	


    public void actionPerformed(ActionEvent e)
    {
    	selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        RegularPolygonModel selectionModel = (RegularPolygonModel)modelManager.getModel(ErosModelManager.CIRCLE_SELECTION);
		SmallBodyModel erosModel = (SmallBodyModel)modelManager.getModel(ErosModelManager.EROS);
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

		showData(1);
    }

    private void showData(int i)
	{
    	DisplayedResultsOptions option = (DisplayedResultsOptions)shownResultsShowComboBox.getSelectedItem();
    	
    	GregorianCalendar startCal = new GregorianCalendar();
    	startCal.setTimeInMillis(startDate.getTime());
    	GregorianCalendar stopCal = new GregorianCalendar();
    	stopCal.setTimeInMillis(endDate.getTime());

    	try
		{
			nlrModel.setNlrData(startCal, stopCal, cubeList, option.getType(), i*option.getValue());
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
		resultsLabel.setText("<html>" + nlrModel.getNumberOfPoints() + " points matched<br>" +
				"Showing points " + range[0] + " through " + range[1] + "<br></html>");
	}

}
