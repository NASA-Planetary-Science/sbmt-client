package edu.jhuapl.near.gui;

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

import edu.jhuapl.near.database.Database;
import edu.jhuapl.near.gui.popupmenus.MSIPopupMenu;
import edu.jhuapl.near.model.MSIBoundaryCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.NearImageCollection;
import edu.jhuapl.near.pair.IdPair;


public class SearchPanel extends JPanel implements ActionListener, MouseListener
{
    private final ModelManager modelManager;
    private java.util.Date startDate = new DateTime(2000, 1, 12, 0, 0, 0, 0).toDate();
    private java.util.Date endDate = new DateTime(2001, 2, 13, 0, 0, 0, 0).toDate();
//    private ShapeBuilderWidget shapePanel;
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JComboBox queryTypeComboBox;
    private JCheckBox filter1CheckBox;
    private JCheckBox filter2CheckBox;
    private JCheckBox filter3CheckBox;
    private JCheckBox filter4CheckBox;
    private JCheckBox filter5CheckBox;
    private JCheckBox filter6CheckBox;
    private JCheckBox filter7CheckBox;

    private JCheckBox iofdblCheckBox;
    private JCheckBox cifdblCheckBox;

    private JFormattedTextField fromDistanceTextField;
    private JFormattedTextField toDistanceTextField;

    private JFormattedTextField fromResolutionTextField;
    private JFormattedTextField toResolutionTextField;

    private JFormattedTextField searchByNumberTextField;
    private JCheckBox searchByNumberCheckBox;
    
    private JList resultList;
    private DefaultListModel resultListModel;
    private MSIPopupMenu popupMenu;
    private ArrayList<String> rawResults = new ArrayList<String>();
    private JLabel label;
    private JButton nextButton;
    private JButton prevButton;
    private JButton removeAllBoundariesButton;
    private JButton removeAllImagesButton;
    private JComboBox numberOfBoundariesComboBox;
    private IdPair boundaryIntervalCurrentlyShown = null;

    

    public SearchPanel(final ModelManager modelManager) 
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

        JPanel startDatePanel = new JPanel();
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

        JPanel endDatePanel = new JPanel();
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
    	
    	JPanel filtersSub2Panel = new JPanel();
        filtersSub2Panel.setLayout(new BoxLayout(filtersSub2Panel,
        		BoxLayout.PAGE_AXIS));
        filtersSub2Panel.add(filter4CheckBox);
    	filtersSub2Panel.add(filter5CheckBox);
    	filtersSub2Panel.add(filter6CheckBox);

    	JPanel filtersSub3Panel = new JPanel();
        filtersSub3Panel.setLayout(new BoxLayout(filtersSub3Panel,
        		BoxLayout.PAGE_AXIS));
    	filtersSub3Panel.add(filter7CheckBox);

    	filtersPanel.add(filtersSub1Panel);
        filtersPanel.add(Box.createHorizontalStrut(15));
    	filtersPanel.add(filtersSub2Panel);
        filtersPanel.add(Box.createHorizontalStrut(15));
    	filtersPanel.add(filtersSub3Panel);
    	    	
    	//filtersPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

    	JPanel iofcifPanel = new JPanel();
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

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        
        JPanel distancePanel = new JPanel();
        distancePanel.setLayout(new BoxLayout(distancePanel,
        		BoxLayout.LINE_AXIS));
        final JLabel fromDistanceLabel = new JLabel("S/C Distance from");
        fromDistanceTextField = new JFormattedTextField(nf);
        fromDistanceTextField.setValue(0.0);
        fromDistanceTextField.setMaximumSize(new Dimension(50, 25));
        final JLabel toDistanceLabel = new JLabel("km to");
        toDistanceTextField = new JFormattedTextField(nf);
        toDistanceTextField.setValue(1000.0);
        toDistanceTextField.setMaximumSize(new Dimension(50, 25));
        final JLabel endDistanceLabel = new JLabel("km");
                
        distancePanel.add(fromDistanceLabel);
        distancePanel.add(fromDistanceTextField);
        distancePanel.add(toDistanceLabel);
        distancePanel.add(toDistanceTextField);
        distancePanel.add(endDistanceLabel);

        
        JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel,
        		BoxLayout.LINE_AXIS));
        final JLabel fromResolutionLabel = new JLabel("Resolution from");
        fromResolutionTextField = new JFormattedTextField(nf);
        fromResolutionTextField.setValue(0.0);
        fromResolutionTextField.setMaximumSize(new Dimension(50, 25));
        final JLabel toResolutionLabel = new JLabel("mpp to");
        toResolutionLabel.setToolTipText("meters per pixel");
        toResolutionTextField = new JFormattedTextField(nf);
        toResolutionTextField.setValue(5000.0);
        toResolutionTextField.setMaximumSize(new Dimension(50, 25));
        final JLabel endResolutionLabel = new JLabel("mpp");
        endResolutionLabel.setToolTipText("meters per pixel");
      
        resolutionPanel.add(fromResolutionLabel);
        resolutionPanel.add(fromResolutionTextField);
        resolutionPanel.add(toResolutionLabel);
        resolutionPanel.add(toResolutionTextField);
        resolutionPanel.add(endResolutionLabel);
        
        JPanel searchByNumberPanel = new JPanel();
        searchByNumberPanel.setLayout(new BoxLayout(searchByNumberPanel,
        		BoxLayout.LINE_AXIS));
        searchByNumberCheckBox = new JCheckBox();
        searchByNumberCheckBox.setText("Search by number");
        searchByNumberCheckBox.setSelected(false);
        nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        searchByNumberTextField = new JFormattedTextField(nf);
        searchByNumberTextField.setMaximumSize(new Dimension(100, 25));
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
            }
        });
        
        searchByNumberPanel.add(searchByNumberCheckBox);
        searchByNumberPanel.add(searchByNumberTextField);
        
        JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Update");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);


        pane.add(filtersPanel);
        pane.add(iofcifPanel);
        pane.add(distancePanel);
        pane.add(resolutionPanel);
        pane.add(searchByNumberPanel);
    	pane.add(submitPanel);
        
        this.add(pane);

        
        
        
        
        
        
        JPanel resultsPanel = new JPanel(new BorderLayout());
		
		popupMenu = new MSIPopupMenu(this.modelManager, 0, this);

		label = new JLabel(" ");

        resultListModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        resultList = new JList(resultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);

        //listScrollPane.setBorder(
        //       new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Results")));

        resultsPanel.add(label, BorderLayout.NORTH);
        resultsPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel resultControlsPanel = new JPanel(new BorderLayout());
        
        JPanel resultSub1ControlsPanel = new JPanel();
        
        resultSub1ControlsPanel.setLayout(new BoxLayout(resultSub1ControlsPanel,
        		BoxLayout.LINE_AXIS));

        final JLabel showLabel = new JLabel("Number Boundaries");
        Object [] options2 = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200, 250};
		numberOfBoundariesComboBox = new JComboBox(options2);
		
		nextButton = new JButton(">");
        nextButton.setActionCommand(">");
        nextButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				if (boundaryIntervalCurrentlyShown != null)
				{
					// Only get the next block if there's something left to show.
					if (boundaryIntervalCurrentlyShown.id2 < rawResults.size())
					{
						boundaryIntervalCurrentlyShown.nextBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
						showBoundaries(boundaryIntervalCurrentlyShown);
					}
				}
				else
				{
					boundaryIntervalCurrentlyShown = new IdPair(0, (Integer)numberOfBoundariesComboBox.getSelectedItem());
			    	showBoundaries(boundaryIntervalCurrentlyShown);
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
				if (boundaryIntervalCurrentlyShown != null)
				{
					// Only get the prev block if there's something left to show.
					if (boundaryIntervalCurrentlyShown.id1 > 0)
					{
						boundaryIntervalCurrentlyShown.prevBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
						showBoundaries(boundaryIntervalCurrentlyShown);
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
        removeAllBoundariesButton = new JButton("Remove All Boundaries");
        removeAllBoundariesButton.setActionCommand("Remove All Boundaries");
        removeAllBoundariesButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
				model.removeAllBoundaries();
				boundaryIntervalCurrentlyShown = null;
			}
        });
        removeAllBoundariesButton.setEnabled(true);
        removeAllBoundariesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        removeAllImagesButton = new JButton("Remove All Images");
        removeAllImagesButton.setActionCommand("Remove All Images");
        removeAllImagesButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NearImageCollection model = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
				model.removeAllImages();
			}
        });
        removeAllImagesButton.setEnabled(true);
        removeAllImagesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultSub2ControlsPanel.add(removeAllBoundariesButton);
        resultSub2ControlsPanel.add(removeAllImagesButton);
        
        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

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

    		String searchField = null;
    		if (searchByNumberCheckBox.isSelected())
    			searchField = searchByNumberTextField.getText();
    		
    		ArrayList<String> results = Database.getInstance().runQuery(
        			new LocalDateTime(startDate), 
        			new LocalDateTime(endDate),
        			filtersChecked,
        			iofdblCheckBox.isSelected(),
        			cifdblCheckBox.isSelected(),
        			Double.parseDouble(fromDistanceTextField.getText()),
        			Double.parseDouble(toDistanceTextField.getText()),
        			Double.parseDouble(fromResolutionTextField.getText()),
        			Double.parseDouble(toResolutionTextField.getText()),
        			searchField);
        	
    		setResults(results);
        }
        catch (Exception e) 
        { 
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }
    
	public void setResults(ArrayList<String> results)
	{
    	resultListModel.clear();
    	rawResults = results;
    	label.setText(results.size() + " images matched");
    	
    	// add the results to the list
    	for (String str : results)
    	{
    		resultListModel.addElement( 
    				str.substring(19, 28) 
    				+ ", day: " + str.substring(6, 9) + "/" + str.substring(1, 5)
    				+ ", type: " + str.substring(10, 16)
    				+ ", filter: " + str.substring(29, 30)
    				);
    	}

    	// Show the first set of boundaries
    	this.boundaryIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
    	this.showBoundaries(boundaryIntervalCurrentlyShown);
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
        		popupMenu.show(e.getComponent(), e.getX(), e.getY(), rawResults.get(index));
        	}
        }
    }
	
	private void showBoundaries(IdPair idPair)
	{
		int startId = idPair.id1;
		int endId = idPair.id2;
		
		MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
		model.removeAllBoundaries();
		
		for (int i=startId; i<endId; ++i)
		{
			if (i < 0)
				continue;
			else if(i >= rawResults.size())
				break;
			
			try 
			{
				String currentImage = rawResults.get(i);
				String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
				model.addBoundary(boundaryName);
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
