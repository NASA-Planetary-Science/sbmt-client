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

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.database.Database;
import edu.jhuapl.near.gui.popupmenus.MSIPopupMenu;
import edu.jhuapl.near.gui.popupmenus.NISPopupMenu;
import edu.jhuapl.near.model.MSIBoundaryCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.NearImageCollection;
import edu.jhuapl.near.pair.IdPair;


public class SearchPanel extends JPanel implements ActionListener, MouseListener
{
	private final String MSI_REMOVE_ALL_BUTTON_TEXT = "Remove All Boundaries";
	private final String NIS_REMOVE_ALL_BUTTON_TEXT = "Remove All Spectra";
	private final String NLR_REMOVE_ALL_BUTTON_TEXT = "Remove All NLR Data";
	
    private final ModelManager modelManager;
    private java.util.Date startDate = new DateTime(2000, 7, 7, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    private java.util.Date endDate = new DateTime(2000, 8, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
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
    private DefaultListModel msiResultListModel;
    private DefaultListModel nisResultListModel;
    private DefaultListModel nlrResultListModel;
    private MSIPopupMenu msiPopupMenu;
    private NISPopupMenu nisPopupMenu;
    private ArrayList<String> msiRawResults = new ArrayList<String>();
    private ArrayList<String> nisRawResults = new ArrayList<String>();
    private ArrayList<String> nlrRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private String msiResultsLabelText = " ";
    private String nisResultsLabelText = " ";
    private String nlrResultsLabelText = " ";
    private JButton nextButton;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllImagesButton;
    private JComboBox numberOfBoundariesComboBox;
    private IdPair resultIntervalCurrentlyShown = null;
    private IdPair msiBoundaryIntervalCurrentlyShown = null;
    private IdPair nisBoundaryIntervalCurrentlyShown = null;

    
    public SearchPanel(
    		final ModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
    		vtkRenderWindowPanel renWin) 
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

        String [] options = {"MSI", "NIS", "NLR"};
        //String [] options = {"MSI", "NIS"};
        //String [] options = {"MSI"};
        queryTypeComboBox = new JComboBox(options);                                             
        queryTypeComboBox.setEditable(false);
        pane.add(queryTypeComboBox);                     

        
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


        //panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        //JLabel label = new JLabel("Select Region:");
        //panel.add(label);

        //this.shapePanel = new ShapeBuilderWidget(wwd, this);
        //panel.add(this.shapePanel);
        //pane.add(panel);

        final JPanel filtersPanel = new JPanel();
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

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        
        final JPanel distancePanel = new JPanel();
        distancePanel.setLayout(new BoxLayout(distancePanel,
        		BoxLayout.LINE_AXIS));
        final JLabel fromDistanceLabel = new JLabel("S/C Distance from ");
        fromDistanceTextField = new JFormattedTextField(nf);
        fromDistanceTextField.setValue(30.0);
        fromDistanceTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel toDistanceLabel = new JLabel(" km to ");
        toDistanceTextField = new JFormattedTextField(nf);
        toDistanceTextField.setValue(40.0);
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
        final JLabel toResolutionLabel = new JLabel(" mpp to ");
        toResolutionLabel.setToolTipText("meters per pixel");
        toResolutionTextField = new JFormattedTextField(nf);
        toResolutionTextField.setValue(5000.0);
        toResolutionTextField.setMaximumSize(new Dimension(50, 23));
        final JLabel endResolutionLabel = new JLabel(" mpp");
        endResolutionLabel.setToolTipText("meters per pixel");
      
        resolutionPanel.add(fromResolutionLabel);
        resolutionPanel.add(fromResolutionTextField);
        resolutionPanel.add(toResolutionLabel);
        resolutionPanel.add(toResolutionTextField);
        resolutionPanel.add(endResolutionLabel);
        
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
        
        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Update");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);


        pane.add(filtersPanel);
        pane.add(iofcifPanel);
        pane.add(distancePanel);
        pane.add(Box.createVerticalStrut(10));
        pane.add(resolutionPanel);
        pane.add(Box.createVerticalStrut(10));
        pane.add(searchByNumberPanel);
    	pane.add(submitPanel);
        
        this.add(pane);

        
        
        
        
        
        
        JPanel resultsPanel = new JPanel(new BorderLayout());
		
		msiPopupMenu = new MSIPopupMenu(this.modelManager, infoPanelManager, renWin, this);
		nisPopupMenu = new NISPopupMenu(this.modelManager, infoPanelManager, renWin);

		resultsLabel = new JLabel(" ");

        msiResultListModel = new DefaultListModel();
        nisResultListModel = new DefaultListModel();
        nlrResultListModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        resultList = new JList(msiResultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);

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
		numberOfBoundariesComboBox.setMaximumSize(new Dimension(150, 23));
		
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
				MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
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
				NearImageCollection model = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
				model.removeAllImages();
			}
        });
        removeAllImagesButton.setEnabled(true);
        removeAllImagesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultSub2ControlsPanel.add(removeAllButton);
        resultSub2ControlsPanel.add(removeAllImagesButton);
        
        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        add(resultsPanel);
        
        queryTypeComboBox.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent arg0) 
			{
				if (queryTypeComboBox.getSelectedItem().equals("MSI"))
				{
					removeAllButton.setText(MSI_REMOVE_ALL_BUTTON_TEXT);
					removeAllImagesButton.setVisible(true);
					resultList.setModel(msiResultListModel);
					resultsLabel.setText(msiResultsLabelText);
					filtersPanel.setVisible(true);
					iofcifPanel.setVisible(true);
					searchByNumberPanel.setVisible(true);
					resolutionPanel.setVisible(true);
					distancePanel.setVisible(true);
					startDatePanel.setVisible(true);
					endDatePanel.setVisible(true);
					submitPanel.setVisible(true);
					resultSub1ControlsPanel.setVisible(true);
				}
				else if (queryTypeComboBox.getSelectedItem().equals("NIS"))
				{
					removeAllButton.setText(NIS_REMOVE_ALL_BUTTON_TEXT);
					removeAllImagesButton.setVisible(false);
					resultList.setModel(nisResultListModel);
					resultsLabel.setText(nisResultsLabelText);
					filtersPanel.setVisible(false);
					iofcifPanel.setVisible(false);
					searchByNumberPanel.setVisible(false);
					resolutionPanel.setVisible(false);
					distancePanel.setVisible(true);
					startDatePanel.setVisible(true);
					endDatePanel.setVisible(true);
					submitPanel.setVisible(true);
					resultSub1ControlsPanel.setVisible(true);
				}
				else if (queryTypeComboBox.getSelectedItem().equals("NLR"))
				{
					removeAllButton.setText(NLR_REMOVE_ALL_BUTTON_TEXT);
					removeAllImagesButton.setVisible(false);
					resultList.setModel(nlrResultListModel);
					resultsLabel.setText(nlrResultsLabelText);
					filtersPanel.setVisible(false);
					iofcifPanel.setVisible(false);
					searchByNumberPanel.setVisible(false);
					resolutionPanel.setVisible(false);
					distancePanel.setVisible(false);
					startDatePanel.setVisible(false);
					endDatePanel.setVisible(false);
					submitPanel.setVisible(false);
					resultSub1ControlsPanel.setVisible(false);
				}
			}
        	
        });
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
        	if (queryTypeComboBox.getSelectedItem().equals("MSI"))
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
        				Database.Datatype.MSI,
        				new DateTime(startDate), 
        				new DateTime(endDate),
        				filtersChecked,
        				iofdblCheckBox.isSelected(),
        				cifdblCheckBox.isSelected(),
        				Double.parseDouble(fromDistanceTextField.getText()),
        				Double.parseDouble(toDistanceTextField.getText()),
        				Double.parseDouble(fromResolutionTextField.getText()),
        				Double.parseDouble(toResolutionTextField.getText()),
        				searchField);

        		setMSIResults(results);
			}
			else if (queryTypeComboBox.getSelectedItem().equals("NIS"))
			{
        		ArrayList<String> results = Database.getInstance().runQuery(
        				Database.Datatype.NIS,
        				new DateTime(startDate), 
        				new DateTime(endDate),
        				null,
        				false,
        				false,
        				Double.parseDouble(fromDistanceTextField.getText()),
        				Double.parseDouble(toDistanceTextField.getText()),
        				0.0,
        				0.0,
        				null);

        		setNISResults(results);
			}
			else if (queryTypeComboBox.getSelectedItem().equals("NLR"))
			{
			}
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
    	msiResultListModel.clear();
    	msiRawResults = results;
    	
    	// add the results to the list
    	for (String str : results)
    	{
    		msiResultListModel.addElement( 
    				str.substring(19, 28) 
    				+ ", day: " + str.substring(6, 9) + "/" + str.substring(1, 5)
    				+ ", type: " + str.substring(10, 16)
    				+ ", filter: " + str.substring(29, 30)
    				);
    	}

    	// Show the first set of boundaries
    	this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
    	this.showMSIBoundaries(resultIntervalCurrentlyShown);
	}

	private void setNISResults(ArrayList<String> results)
	{
		nisResultsLabelText = results.size() + " spectra matched";
    	resultsLabel.setText(nisResultsLabelText);
    	nisResultListModel.clear();
    	nisRawResults = results;
    	
    	// add the results to the list
    	for (String str : results)
    	{
    		nisResultListModel.addElement( 
    				str.substring(19, 28) 
    				+ ", day: " + str.substring(6, 9) + "/" + str.substring(1, 5)
    				+ ", type: " + str.substring(10, 16)
    				+ ", filter: " + str.substring(29, 30)
    				);
    	}

    	// Show the first set of boundaries
    	this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
    	this.showMSIBoundaries(resultIntervalCurrentlyShown);
	}
	
	private void setNLRResults(ArrayList<String> results)
	{
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
        		msiPopupMenu.setCurrentImage(msiRawResults.get(index));
        		msiPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        	}
        }
    }
	
	private void showMSIBoundaries(IdPair idPair)
	{
		int startId = idPair.id1;
		int endId = idPair.id2;
		
		MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
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
