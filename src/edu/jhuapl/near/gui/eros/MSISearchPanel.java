package edu.jhuapl.near.gui.eros;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import nom.tam.fits.FitsException;

import org.joda.time.*;

import vtk.vtkPolyData;

import edu.jhuapl.near.query.Query;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImageCollection;
import edu.jhuapl.near.model.eros.MSIImage.MSIKey;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.popupmenus.eros.MSIPopupMenu;
import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SearchPanelUtil;


public class MSISearchPanel extends JPanel implements ActionListener, MouseListener
{
	private final String MSI_REMOVE_ALL_BUTTON_TEXT = "Remove All Boundaries";
	
    private final ModelManager modelManager;
    private PickManager pickManager;
    private JComboBox msiSourceComboBox;
    private java.util.Date startDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
    private java.util.Date endDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date:";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JCheckBox filter1CheckBox;
    private JCheckBox filter2CheckBox;
    private JCheckBox filter3CheckBox;
    private JCheckBox filter4CheckBox;
    private JCheckBox filter5CheckBox;
    private JCheckBox filter6CheckBox;
    private JCheckBox filter7CheckBox;

    private JCheckBox iofdblCheckBox;
    private JCheckBox cifdblCheckBox;

    private JComboBox hasLimbComboBox;

    private JFormattedTextField fromDistanceTextField;
    private JFormattedTextField toDistanceTextField;
    private JFormattedTextField fromResolutionTextField;
    private JFormattedTextField toResolutionTextField;
    private JFormattedTextField fromIncidenceTextField;
    private JFormattedTextField toIncidenceTextField;
    private JFormattedTextField fromEmissionTextField;
    private JFormattedTextField toEmissionTextField;
    private JFormattedTextField fromPhaseTextField;
    private JFormattedTextField toPhaseTextField;

    private JToggleButton selectRegionButton;
    
    private JFormattedTextField searchByNumberTextField;
    private JCheckBox searchByNumberCheckBox;
    
    private JList resultList;
    private MSIPopupMenu msiPopupMenu;
    private ArrayList<String> msiRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private String msiResultsLabelText = " ";
    private JButton nextButton;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllImagesButton;
    private JComboBox numberOfBoundariesComboBox;
    private IdPair resultIntervalCurrentlyShown = null;
    
    /**
     * The source of the msi images of the most recently executed query
     */
    private MSIImage.MSISource msiSourceOfLastQuery = MSIImage.MSISource.PDS;
    
    public MSISearchPanel(
    		final ModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
    		final PickManager pickManager,
    		Renderer renderer) 
    {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel,
        		BoxLayout.PAGE_AXIS));
		
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

		JPanel pane = new JPanel();
    	pane.setLayout(new BoxLayout(pane,
        		BoxLayout.PAGE_AXIS));

    	//pane.setBorder(
        //        new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Editor")));

        final JPanel msiSourcePanel = new JPanel();
        JLabel msiSourceLabel = new JLabel("MSI Source:");
    	Object[] msiSourceOptions = {MSIImage.MSISource.PDS, MSIImage.MSISource.GASKELL};
    	msiSourceComboBox = new JComboBox(msiSourceOptions);
    	//msiSourceComboBox.setMaximumSize(new Dimension(1000, 23));
    	msiSourcePanel.add(msiSourceLabel);
    	msiSourcePanel.add(msiSourceComboBox);
    	pane.add(msiSourcePanel);
    	
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



        final JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel,
        		BoxLayout.LINE_AXIS));
        filter1CheckBox = new JCheckBox();
    	filter1CheckBox.setText("Filter 1 (550 nm)");
    	filter1CheckBox.setSelected(true);
        filter2CheckBox = new JCheckBox();
    	filter2CheckBox.setText("Filter 2 (450 nm)");
    	filter2CheckBox.setSelected(true);
        filter3CheckBox = new JCheckBox();
    	filter3CheckBox.setText("Filter 3 (760 nm)");
    	filter3CheckBox.setSelected(true);
        filter4CheckBox = new JCheckBox();
    	filter4CheckBox.setText("Filter 4 (950 nm)");
    	filter4CheckBox.setSelected(true);
        filter5CheckBox = new JCheckBox();
    	filter5CheckBox.setText("Filter 5 (900 nm)");
    	filter5CheckBox.setSelected(true);
        filter6CheckBox = new JCheckBox();
    	filter6CheckBox.setText("Filter 6 (1000 nm)");
    	filter6CheckBox.setSelected(true);
        filter7CheckBox = new JCheckBox();
    	filter7CheckBox.setText("Filter 7 (1050 nm)");
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
    	    	
    	//filtersPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

    	final JPanel iofcifPanel = new JPanel();
        iofcifPanel.setLayout(new BoxLayout(iofcifPanel,
        		BoxLayout.LINE_AXIS));
        //iofcifPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

        iofdblCheckBox = new JCheckBox();
        iofdblCheckBox.setText("iofdbl");
        iofdblCheckBox.setSelected(true);
        cifdblCheckBox = new JCheckBox();
        cifdblCheckBox.setText("cifdbl");
        cifdblCheckBox.setSelected(true);

        iofcifPanel.add(iofdblCheckBox);
        iofcifPanel.add(Box.createHorizontalStrut(15));
        iofcifPanel.add(cifdblCheckBox);

        iofcifPanel.add(Box.createHorizontalStrut(25));

    	final JPanel hasLimbPanel = new JPanel();
        hasLimbPanel.setLayout(new BoxLayout(hasLimbPanel,
        		BoxLayout.LINE_AXIS));

        final JLabel hasLimbLabel = new JLabel("Limb: ");
    	Object[] hasLimbOptions = {"with or without", "with only", "without only"};
        hasLimbComboBox = new JComboBox(hasLimbOptions);
        hasLimbComboBox.setMaximumSize(new Dimension(150, 23));

        hasLimbPanel.add(hasLimbLabel);
        hasLimbPanel.add(hasLimbComboBox);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        
        final JPanel distancePanel = new JPanel();
        distancePanel.setLayout(new BoxLayout(distancePanel,
        		BoxLayout.LINE_AXIS));
        final JLabel fromDistanceLabel = new JLabel("S/C Distance from ");
        fromDistanceTextField = new JFormattedTextField(nf);
        fromDistanceTextField.setValue(0.0);
        fromDistanceTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel toDistanceLabel = new JLabel(" to ");
        toDistanceTextField = new JFormattedTextField(nf);
        toDistanceTextField.setValue(100.0);
        toDistanceTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel endDistanceLabel = new JLabel(" km");
                
        distancePanel.add(fromDistanceLabel);
        distancePanel.add(fromDistanceTextField);
        distancePanel.add(toDistanceLabel);
        distancePanel.add(toDistanceTextField);
        distancePanel.add(endDistanceLabel);

        
        final JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel,
        		BoxLayout.LINE_AXIS));
        final JLabel fromResolutionLabel = new JLabel("Resolution from ");
        fromResolutionTextField = new JFormattedTextField(nf);
        fromResolutionTextField.setValue(0.0);
        fromResolutionTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel toResolutionLabel = new JLabel(" to ");
        toResolutionTextField = new JFormattedTextField(nf);
        toResolutionTextField.setValue(50.0);
        toResolutionTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel endResolutionLabel = new JLabel(" mpp");
        endResolutionLabel.setToolTipText("meters per pixel");
      
        resolutionPanel.add(fromResolutionLabel);
        resolutionPanel.add(fromResolutionTextField);
        resolutionPanel.add(toResolutionLabel);
        resolutionPanel.add(toResolutionTextField);
        resolutionPanel.add(endResolutionLabel);
        
        
        fromIncidenceTextField = new JFormattedTextField(nf);
        toIncidenceTextField = new JFormattedTextField(nf);
        final JPanel incidencePanel = SearchPanelUtil.createFromToPanel(
        		fromIncidenceTextField, 
        		toIncidenceTextField, 
        		0.0, 
        		180.0, 
        		"Incidence from", 
        		"to", 
        		"degrees");

        fromEmissionTextField = new JFormattedTextField(nf);
        toEmissionTextField = new JFormattedTextField(nf);
        final JPanel emissionPanel = SearchPanelUtil.createFromToPanel(
        		fromEmissionTextField, 
        		toEmissionTextField, 
        		0.0, 
        		180.0, 
        		"Emissiom from", 
        		"to", 
        		"degrees");

        fromPhaseTextField = new JFormattedTextField(nf);
        toPhaseTextField = new JFormattedTextField(nf);
        final JPanel phasePanel = SearchPanelUtil.createFromToPanel(
        		fromPhaseTextField, 
        		toPhaseTextField, 
        		0.0, 
        		180.0, 
        		"Phase from", 
        		"to", 
        		"degrees");

        
        final JPanel searchByNumberPanel = new JPanel();
        searchByNumberPanel.setLayout(new BoxLayout(searchByNumberPanel,
        		BoxLayout.LINE_AXIS));
        searchByNumberCheckBox = new JCheckBox();
        searchByNumberCheckBox.setText("Search by number");
        searchByNumberCheckBox.setSelected(false);
        nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        searchByNumberTextField = new JFormattedTextField(nf);
        searchByNumberTextField.setMaximumSize(new Dimension(100, 23));
        searchByNumberTextField.setEnabled(false);
        searchByNumberCheckBox.addItemListener(new ItemListener()
        {
        	public void itemStateChanged(ItemEvent e) 
        	{
        		boolean enable = e.getStateChange() == ItemEvent.SELECTED;
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
                iofdblCheckBox.setEnabled(!enable);
                cifdblCheckBox.setEnabled(!enable);
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
                for (Component comp : incidencePanel.getComponents())
                	comp.setEnabled(!enable);
                for (Component comp : emissionPanel.getComponents())
                	comp.setEnabled(!enable);
                for (Component comp : phasePanel.getComponents())
                	comp.setEnabled(!enable);
            }
        });
        
        searchByNumberPanel.add(searchByNumberCheckBox);
        searchByNumberPanel.add(searchByNumberTextField);
        
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
			}
        });
        selectRegionPanel.add(clearRegionButton);

        
        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Search");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);


        pane.add(filtersPanel);
        pane.add(iofcifPanel);
        pane.add(hasLimbPanel);
        pane.add(distancePanel);
        pane.add(resolutionPanel);
        pane.add(incidencePanel);
        pane.add(emissionPanel);
        pane.add(phasePanel);
        pane.add(Box.createVerticalStrut(10));
        pane.add(searchByNumberPanel);
    	pane.add(selectRegionPanel);
    	pane.add(submitPanel);
        
        topPanel.add(pane);

        
        
        
        
        
        
        JPanel resultsPanel = new JPanel(new BorderLayout());
		
		msiPopupMenu = new MSIPopupMenu(this.modelManager, infoPanelManager, renderer, this);

		resultsLabel = new JLabel(" ");

        //Create the list and put it in a scroll pane.
        resultList = new JList();
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setPreferredSize(new Dimension(300, 200));
        
        //listScrollPane.setBorder(
        //       new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Results")));

        resultsPanel.add(resultsLabel, BorderLayout.NORTH);
        resultsPanel.add(listScrollPane, BorderLayout.CENTER);

        final JPanel resultControlsPanel = new JPanel(new BorderLayout());
        
        final JPanel resultSub1ControlsPanel = new JPanel();
        
        resultSub1ControlsPanel.setLayout(new BoxLayout(resultSub1ControlsPanel,
        		BoxLayout.LINE_AXIS));

        final JLabel showLabel = new JLabel("Number Boundaries");
        Object [] options2 = {
        		10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
        		110, 120, 130, 140, 150, 160, 170, 180, 190, 200,
        		210, 220, 230, 240, 250
        		};
		numberOfBoundariesComboBox = new JComboBox(options2);
		numberOfBoundariesComboBox.setMaximumSize(new Dimension(100, 23));
		
		nextButton = new JButton(">");
        nextButton.setActionCommand(">");
        nextButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				if (resultIntervalCurrentlyShown != null)
				{
					// Only get the next block if there's something left to show.
					if (resultIntervalCurrentlyShown.id2 < resultList.getModel().getSize())
					{
						resultIntervalCurrentlyShown.nextBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
						showMSIBoundaries(resultIntervalCurrentlyShown);
					}
				}
				else
				{
					resultIntervalCurrentlyShown = new IdPair(0, (Integer)numberOfBoundariesComboBox.getSelectedItem());
			    	showMSIBoundaries(resultIntervalCurrentlyShown);
				}
			}
        });
        nextButton.setEnabled(true);

        prevButton = new JButton("<");
        prevButton.setActionCommand("<");
        prevButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				if (resultIntervalCurrentlyShown != null)
				{
					// Only get the prev block if there's something left to show.
					if (resultIntervalCurrentlyShown.id1 > 0)
					{
						resultIntervalCurrentlyShown.prevBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
						showMSIBoundaries(resultIntervalCurrentlyShown);
					}
				}
			}
        });
        prevButton.setEnabled(true);

        resultSub1ControlsPanel.add(showLabel);
        resultSub1ControlsPanel.add(numberOfBoundariesComboBox);
        resultSub1ControlsPanel.add(Box.createHorizontalStrut(10));
        resultSub1ControlsPanel.add(prevButton);
        resultSub1ControlsPanel.add(nextButton);

        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
        		BoxLayout.PAGE_AXIS));
        
        removeAllButton = new JButton(MSI_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand("Remove All Boundaries");
        removeAllButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
				model.removeAllBoundaries();
				resultIntervalCurrentlyShown = null;
			}
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        removeAllImagesButton = new JButton("Remove All Images");
        removeAllImagesButton.setActionCommand("Remove All Images");
        removeAllImagesButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
				model.removeAllImages();
			}
        });
        removeAllImagesButton.setEnabled(true);
        removeAllImagesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

//        final JCheckBox showFrustumsCheckBox = new JCheckBox("Show Frustums");
//        showFrustumsCheckBox.addActionListener(new ActionListener()
//        {
//        	public void actionPerformed(ActionEvent e)
//        	{
//        		MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
//        		model.setShowFrustums(showFrustumsCheckBox.isSelected());
//        	}
//        });
//
//        resultSub2ControlsPanel.add(showFrustumsCheckBox); // for now don't show this
        resultSub2ControlsPanel.add(removeAllButton);
        resultSub2ControlsPanel.add(removeAllImagesButton);
        
        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        topPanel.add(resultsPanel);

        JScrollPane topScrollPane = new JScrollPane(topPanel);
        
        add(topScrollPane);

    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
        	selectRegionButton.setSelected(false);
			pickManager.setPickMode(PickMode.DEFAULT);

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

        	String searchField = null;
        	if (searchByNumberCheckBox.isSelected())
        		searchField = searchByNumberTextField.getText();

        	GregorianCalendar startDateGreg = new GregorianCalendar();
        	GregorianCalendar endDateGreg = new GregorianCalendar();
        	startDateGreg.setTime(startDate);
        	endDateGreg.setTime(endDate);
        	DateTime startDateJoda = new DateTime(
        			startDateGreg.get(GregorianCalendar.YEAR),
        			startDateGreg.get(GregorianCalendar.MONTH)+1,
        			startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
        			startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
        			startDateGreg.get(GregorianCalendar.MINUTE),
        			startDateGreg.get(GregorianCalendar.SECOND),
        			startDateGreg.get(GregorianCalendar.MILLISECOND),
        			DateTimeZone.UTC);
        	DateTime endDateJoda = new DateTime(
        			endDateGreg.get(GregorianCalendar.YEAR),
        			endDateGreg.get(GregorianCalendar.MONTH)+1,
        			endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
        			endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
        			endDateGreg.get(GregorianCalendar.MINUTE),
        			endDateGreg.get(GregorianCalendar.SECOND),
        			endDateGreg.get(GregorianCalendar.MILLISECOND),
        			DateTimeZone.UTC);
        	
			TreeSet<Integer> cubeList = null;
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
					cubeList = erosModel.getIntersectingCubes(interiorPoly);
				}
				else
				{
					cubeList = erosModel.getIntersectingCubes(region.interiorPolyData);
				}
			}
			
			MSIImage.MSISource msiSource = null;
			if (msiSourceComboBox.getSelectedItem().equals(MSIImage.MSISource.PDS))
			    msiSource = MSIImage.MSISource.PDS;
			else
			    msiSource = MSIImage.MSISource.GASKELL;
			System.out.println(msiSource.toString());
        	ArrayList<String> results = Query.getInstance().runQuery(
        			Query.Datatype.MSI,
        			startDateJoda, 
        			endDateJoda,
        			filtersChecked,
        			iofdblCheckBox.isSelected(),
        			cifdblCheckBox.isSelected(),
        			Double.parseDouble(fromDistanceTextField.getText()),
        			Double.parseDouble(toDistanceTextField.getText()),
        			Double.parseDouble(fromResolutionTextField.getText()),
        			Double.parseDouble(toResolutionTextField.getText()),
        			searchField,
        			null,
        			Double.parseDouble(fromIncidenceTextField.getText()),
        			Double.parseDouble(toIncidenceTextField.getText()),
        			Double.parseDouble(fromEmissionTextField.getText()),
        			Double.parseDouble(toEmissionTextField.getText()),
        			Double.parseDouble(fromPhaseTextField.getText()),
        			Double.parseDouble(toPhaseTextField.getText()),
        			cubeList,
        			msiSource,
        			hasLimbComboBox.getSelectedIndex());

            if (msiSourceComboBox.getSelectedItem().equals(MSIImage.MSISource.PDS))
                msiSourceOfLastQuery = MSIImage.MSISource.PDS;
            else
                msiSourceOfLastQuery = MSIImage.MSISource.GASKELL;

        	setMSIResults(results);
        }
        catch (Exception e) 
        { 
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }
    
	private void setMSIResults(ArrayList<String> results)
	{
		msiResultsLabelText = results.size() + " images matched";
    	resultsLabel.setText(msiResultsLabelText);
    	msiRawResults = results;
    	
    	String[] formattedResults = new String[results.size()];

    	// add the results to the list
    	int i=0;
    	for (String str : results)
    	{
    		formattedResults[i] = new String(
    				str.substring(23, 32) 
    				+ ", day: " + str.substring(10, 13) + "/" + str.substring(5, 9)
    				+ ", type: " + str.substring(14, 20)
                    + ", filter: " + str.substring(33, 34)
                    + ", source: " + msiSourceOfLastQuery
    				);
    		
    		++i;
    	}

    	resultList.setListData(formattedResults);

    	// Show the first set of boundaries
    	this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
    	this.showMSIBoundaries(resultIntervalCurrentlyShown);
	}

	public void mouseClicked(MouseEvent e) 
	{
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public void mousePressed(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
        	int index = resultList.locationToIndex(e.getPoint());

        	if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
        	{
        		resultList.setSelectedIndex(index);
        		String name = msiRawResults.get(index);
        		msiPopupMenu.setCurrentImage(new MSIKey(name.substring(0, name.length()-4), msiSourceOfLastQuery));
        		msiPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        	}
        }
    }
	
	private void showMSIBoundaries(IdPair idPair)
	{
		int startId = idPair.id1;
		int endId = idPair.id2;
		
		MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
		model.removeAllBoundaries();
		
		for (int i=startId; i<endId; ++i)
		{
			if (i < 0)
				continue;
			else if(i >= msiRawResults.size())
				break;
			
			try 
			{
				String currentImage = msiRawResults.get(i);
				//String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
				//String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_DDR.LBL";
				String boundaryName = currentImage.substring(0,currentImage.length()-4);
				model.addBoundary(new MSIKey(boundaryName, msiSourceOfLastQuery));
			} 
			catch (FitsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
