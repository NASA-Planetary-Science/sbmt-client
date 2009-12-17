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

import net.miginfocom.swing.MigLayout;
import nom.tam.fits.FitsException;

import org.joda.time.*;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.database.Database;
import edu.jhuapl.near.gui.popupmenus.NISPopupMenu;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.NISSpectraCollection;
import edu.jhuapl.near.pair.IdPair;


public class NISSearchPanel extends JPanel implements ActionListener, MouseListener
{
	private final String NIS_REMOVE_ALL_BUTTON_TEXT = "Remove All Footprints";
	
    private final ModelManager modelManager;
    private java.util.Date startDate = new GregorianCalendar(2000, 4, 1, 0, 0, 0).getTime();
    private java.util.Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;

    private JFormattedTextField fromDistanceTextField;
    private JFormattedTextField toDistanceTextField;
    private JFormattedTextField fromIncidenceTextField;
    private JFormattedTextField toIncidenceTextField;
    private JFormattedTextField fromEmissionTextField;
    private JFormattedTextField toEmissionTextField;
    private JFormattedTextField fromPhaseTextField;
    private JFormattedTextField toPhaseTextField;

    private JComboBox channelComboBox;

    private JList resultList;
    private NISPopupMenu nisPopupMenu;
    private ArrayList<String> nisRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private String nisResultsLabelText = " ";
    private JButton nextButton;
    private JButton prevButton;
    private JButton removeAllButton;
    private JComboBox numberOfBoundariesComboBox;
    private IdPair resultIntervalCurrentlyShown = null;
    private JCheckBox polygonType0CheckBox;
    private JCheckBox polygonType1CheckBox;
    private JCheckBox polygonType2CheckBox;
    private JCheckBox polygonType3CheckBox;

    
    public NISSearchPanel(
    		final ModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
    		vtkRenderWindowPanel renWin) 
    {
    	//setLayout(new BoxLayout(this,
        //		BoxLayout.PAGE_AXIS));
    	setLayout(new MigLayout("wrap 1, insets 0"));
    	
    	this.modelManager = modelManager;
    	
    	JPanel pane = new JPanel();
//    	pane.setLayout(new BoxLayout(pane,
//        		BoxLayout.PAGE_AXIS));
    	pane.setLayout(new MigLayout("wrap 1"));
    	
    	//pane.setBorder(
        //        new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Editor")));

        
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

        
    	JPanel polygonTypePanel = new JPanel();
    	//polygonTypePanel.setLayout(new BoxLayout(polygonTypePanel,
        //		BoxLayout.PAGE_AXIS));
    	polygonTypePanel.setLayout(new MigLayout("wrap 2"));
        JLabel polygonTypeLabel = new JLabel("Field-Of-View Polygon Type:");
        //polygonTypeLabel.setAlignmentX(1.0f);
        
        polygonType0CheckBox = new JCheckBox();
        polygonType0CheckBox.setText("Full");
        polygonType0CheckBox.setSelected(true);
        polygonType0CheckBox.setToolTipText("All vertices on shape.");
        //polygonType0CheckBox.setAlignmentX(1.0f);
        polygonType1CheckBox = new JCheckBox();
        polygonType1CheckBox.setText("Partial");
        polygonType1CheckBox.setSelected(false);
        polygonType1CheckBox.setToolTipText("Single contiguous set of vertices on shape.");
        //polygonType1CheckBox.setAlignmentX(1.0f);
        polygonType2CheckBox = new JCheckBox();
        polygonType2CheckBox.setText("Degenerate");
        polygonType2CheckBox.setSelected(false);
        polygonType2CheckBox.setToolTipText("Multiple contiguous sets of vertices on shape.");
        //polygonType2CheckBox.setAlignmentX(1.0f);
        polygonType3CheckBox = new JCheckBox();
        polygonType3CheckBox.setText("Empty");
        polygonType3CheckBox.setSelected(false);
        polygonType3CheckBox.setToolTipText("No vertices on shape.");
        //polygonType3CheckBox.setAlignmentX(1.0f);

        polygonTypePanel.add(polygonTypeLabel, "span");
        polygonTypePanel.add(polygonType0CheckBox);
        //polygonTypePanel.add(Box.createHorizontalStrut(15));
        polygonTypePanel.add(polygonType1CheckBox, "wrap");
        //polygonTypePanel.add(Box.createHorizontalStrut(15));
        polygonTypePanel.add(polygonType2CheckBox);
        //polygonTypePanel.add(Box.createHorizontalStrut(15));
        polygonTypePanel.add(polygonType3CheckBox);


        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        
        final JPanel distancePanel = new JPanel();
        distancePanel.setLayout(new BoxLayout(distancePanel,
        		BoxLayout.LINE_AXIS));
        final JLabel fromDistanceLabel = new JLabel("S/C Distance from ");
        fromDistanceTextField = new JFormattedTextField(nf);
        fromDistanceTextField.setValue(0.0);
        fromDistanceTextField.setMaximumSize(new Dimension(50, 23));
		fromDistanceTextField.setColumns(5);
        final JLabel toDistanceLabel = new JLabel(" to ");
        toDistanceTextField = new JFormattedTextField(nf);
        toDistanceTextField.setValue(500.0);
        toDistanceTextField.setMaximumSize(new Dimension(50, 23));
        toDistanceTextField.setColumns(5);
        final JLabel endDistanceLabel = new JLabel(" km");
                
        distancePanel.add(fromDistanceLabel);
        distancePanel.add(fromDistanceTextField);
        distancePanel.add(toDistanceLabel);
        distancePanel.add(toDistanceTextField);
        distancePanel.add(endDistanceLabel);

        fromIncidenceTextField = new JFormattedTextField(nf);
        toIncidenceTextField = new JFormattedTextField(nf);
        JPanel incidencePanel = SearchPanelUtil.createFromToPanel(
        		fromIncidenceTextField, 
        		toIncidenceTextField, 
        		0.0, 
        		180.0, 
        		"Incidence from", 
        		"to", 
        		"degrees");

        fromEmissionTextField = new JFormattedTextField(nf);
        toEmissionTextField = new JFormattedTextField(nf);
        JPanel emissionPanel = SearchPanelUtil.createFromToPanel(
        		fromEmissionTextField, 
        		toEmissionTextField, 
        		0.0, 
        		180.0, 
        		"Emissiom from", 
        		"to", 
        		"degrees");

        fromPhaseTextField = new JFormattedTextField(nf);
        toPhaseTextField = new JFormattedTextField(nf);
        JPanel phasePanel = SearchPanelUtil.createFromToPanel(
        		fromPhaseTextField, 
        		toPhaseTextField, 
        		0.0, 
        		180.0, 
        		"Phase from", 
        		"to", 
        		"degrees");


		
        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Search");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);


        pane.add(distancePanel);
        pane.add(incidencePanel);
        pane.add(emissionPanel);
        pane.add(phasePanel);
        //pane.add(Box.createVerticalStrut(10));
        pane.add(polygonTypePanel);
        //pane.add(Box.createVerticalStrut(10));
    	pane.add(submitPanel, "align center");
        
        this.add(pane);

        
        
        
        
        
        
        JPanel resultsPanel = new JPanel(new MigLayout("insets 0"));
		
		nisPopupMenu = new NISPopupMenu(this.modelManager, infoPanelManager, renWin);

		resultsLabel = new JLabel(" ");

        //Create the list and put it in a scroll pane.
        resultList = new JList();
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);
        listScrollPane.setPreferredSize(new Dimension(10000, 10000));
        
        
        //listScrollPane.setBorder(
        //       new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), 
        //                           new TitledBorder("Query Results")));

        resultsPanel.add(resultsLabel, "north");
        resultsPanel.add(listScrollPane, "center");

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
						showNISBoundaries(resultIntervalCurrentlyShown);
					}
				}
				else
				{
					resultIntervalCurrentlyShown = new IdPair(0, (Integer)numberOfBoundariesComboBox.getSelectedItem());
			    	showNISBoundaries(resultIntervalCurrentlyShown);
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
						showNISBoundaries(resultIntervalCurrentlyShown);
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
        removeAllButton = new JButton(NIS_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(NIS_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);
				model.removeAllImages();
				resultIntervalCurrentlyShown = null;
			}
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        resultSub2ControlsPanel.add(removeAllButton);

        JPanel channelPanel = new JPanel();
		channelPanel.setLayout(new BoxLayout(channelPanel,
				BoxLayout.LINE_AXIS));

        JLabel channelLabel = new JLabel("Color by Channel");
        Object[] channels = new Object[64];
        for (int i=1; i<=64; ++i)
        	channels[i-1] = new Integer(i);
		channelComboBox = new JComboBox(channels);
		channelComboBox.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);
				model.setChannelToColorBy(channelComboBox.getSelectedIndex());
			}
        });
		
		channelPanel.add(channelLabel);
		channelPanel.add(channelComboBox);

		//resultSub2ControlsPanel.add(channelPanel);

        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        resultsPanel.add(resultControlsPanel, "south");

        add(resultsPanel, "growy");
        
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
        {
        	ArrayList<Integer> polygonTypesChecked = new ArrayList<Integer>();

        	if (polygonType0CheckBox.isSelected())
        		polygonTypesChecked.add(0);
        	if (polygonType1CheckBox.isSelected())
        		polygonTypesChecked.add(1);
        	if (polygonType2CheckBox.isSelected())
        		polygonTypesChecked.add(2);
        	if (polygonType3CheckBox.isSelected())
        		polygonTypesChecked.add(3);

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

        	
        	ArrayList<String> results = Database.getInstance().runQuery(
        			Database.Datatype.NIS,
        			startDateJoda,
        			endDateJoda,
        			null,
        			false,
        			false,
        			Double.parseDouble(fromDistanceTextField.getText()),
        			Double.parseDouble(toDistanceTextField.getText()),
        			0.0,
        			0.0,
        			null,
        			polygonTypesChecked,
        			Double.parseDouble(fromIncidenceTextField.getText()),
        			Double.parseDouble(toIncidenceTextField.getText()),
        			Double.parseDouble(fromEmissionTextField.getText()),
        			Double.parseDouble(toEmissionTextField.getText()),
        			Double.parseDouble(fromPhaseTextField.getText()),
        			Double.parseDouble(toPhaseTextField.getText()));

        	setNISResults(results);
        }
        catch (Exception e) 
        { 
            e.printStackTrace();
            System.out.println(e);
            return;
        }
    }
    

	private void setNISResults(ArrayList<String> results)
	{
		nisResultsLabelText = results.size() + " spectra matched";
    	resultsLabel.setText(nisResultsLabelText);
    	nisRawResults = results;
    	
    	String[] formattedResults = new String[results.size()];

    	// add the results to the list
    	int i=0;
    	for (String str : results)
    	{
    		formattedResults[i] = new String(
    				str.substring(16, 25) 
    				+ ", day: " + str.substring(10, 13) + "/" + str.substring(5, 9)
    				);
    		
    		++i;
    	}
    	
    	resultList.setListData(formattedResults);

    	// Show the first set of boundaries
    	this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
    	this.showNISBoundaries(resultIntervalCurrentlyShown);
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
        		nisPopupMenu.setCurrentSpectrum(nisRawResults.get(index));
        		nisPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        	}
        }
    }
	
	private void showNISBoundaries(IdPair idPair)
	{
		int startId = idPair.id1;
		int endId = idPair.id2;
		
		NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);
		model.removeAllImages();
		
		for (int i=startId; i<endId; ++i)
		{
			if (i < 0)
				continue;
			else if(i >= nisRawResults.size())
				break;
			
			try 
			{
				String currentImage = nisRawResults.get(i);
				String boundaryName = currentImage.substring(0,currentImage.length()-4) + ".NIS";
				model.addSpectrum(boundaryName);
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
