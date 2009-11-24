package edu.jhuapl.near.gui;

import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;

import org.joda.time.*;

import com.jidesoft.swing.RangeSlider;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.NLRDataCollection;
import edu.jhuapl.near.model.NLRDataPerDay;


public class NLRSearchPanel extends JPanel implements /*ActionListener,*/ MouseListener
{
	private final String NLR_REMOVE_ALL_BUTTON_TEXT = "Remove All NLR Data";
	
    private final ModelManager modelManager;
    private NLRDataCollection nlrModel;
    private java.util.Date startDate = new DateTime(2000, 3, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    private java.util.Date endDate = new DateTime(2000, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;

    private JFormattedTextField fromDistanceTextField;
    private JFormattedTextField toDistanceTextField;

    private JList resultList;
    private DefaultListModel nlrResultListModel;
    //private NLRPopupMenu nlrPopupMenu;
    private ArrayList<String> nlrRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private String nlrResultsLabelText = " ";
    private JButton showHideButton;
    //private JButton nextButton;
    //private JButton prevButton;
    private JButton removeAllButton;
    //private JComboBox numberOfBoundariesComboBox;
    //private IdPair resultIntervalCurrentlyShown = null;

    
    public class NlrTimeIntervalChanger extends JPanel implements ChangeListener
    {
    	private RangeSlider slider;
    	
    	private NLRDataCollection nlrImage;
    	
    	public NlrTimeIntervalChanger()
    	{
    		setBorder(BorderFactory.createTitledBorder("Time Interval"));

    		//this.setPreferredSize(new Dimension(300,300));
    		slider = new RangeSlider(0, 255, 0, 255);
    		slider.setPaintTicks(true);
    		slider.setMajorTickSpacing(10);
    		slider.setPaintTrack(true);
    		slider.addChangeListener(this);
    		slider.setEnabled(false);
    		add(slider);
    	}
    	
    	void setNearImage(NLRDataCollection image)
    	{
    		if (image != null)
    		{
    			nlrImage = image;
    			slider.setLowValue(0);
    			slider.setHighValue(24);
    			slider.setEnabled(true);
    		}
    		else
    		{
    			slider.setEnabled(false);
    		}
    	}

    	public void stateChanged(ChangeEvent e) 
    	{
    		int lowVal = slider.getLowValue();
    		int highVal = slider.getHighValue();
//    		if (nlrImage != null)
//    			nlrImage.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
    	}
    }

    public NLRSearchPanel(
    		final ModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
    		vtkRenderWindowPanel renWin) 
    {
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));
    	
    	this.modelManager = modelManager;
    	this.nlrModel = (NLRDataCollection)modelManager.getModel(ModelManager.NLR_DATA);
    	/*
    	JPanel pane = new JPanel();
    	pane.setLayout(new BoxLayout(pane,
        		BoxLayout.PAGE_AXIS));

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

        
        
        final JPanel submitPanel = new JPanel();
        //panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        JButton submitButton = new JButton("Update");
        submitButton.setEnabled(true);
        submitButton.addActionListener(this);

        submitPanel.add(submitButton);


        pane.add(distancePanel);
        pane.add(Box.createVerticalStrut(10));
        pane.add(Box.createVerticalStrut(10));
    	pane.add(submitPanel);
        
        this.add(pane);
    	 */
        
        
        
        
        
        
        JPanel resultsPanel = new JPanel(new BorderLayout());
		
		//nlrPopupMenu = new NISPopupMenu(this.modelManager, infoPanelManager, renWin);

		resultsLabel = new JLabel(" ");

        nlrResultListModel = new DefaultListModel();

        nlrRawResults = nlrModel.getAllNlrPaths();
    	for (String str : nlrRawResults)
    	{
    		//System.out.println(str);
    		nlrResultListModel.addElement( 
    				str.substring(5, 13) 
    				+ ", day: " + str.substring(8, 11) + "/20" + str.substring(6, 8)
    				);
    	}

        //Create the list and put it in a scroll pane.
        resultList = new JList(nlrResultListModel);
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
        		BoxLayout.PAGE_AXIS));

        
        showHideButton = new JButton("Show");
        showHideButton.setActionCommand("Show");
        showHideButton.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		int index = resultList.getSelectedIndex();
        		if (index >= 0)
        		{
            		try 
            		{
            			if (showHideButton.getText().startsWith("Show"))
            			{
            				nlrModel.addNlrData(nlrRawResults.get(index));

            				showHideButton.setText("Hide");
            			}
            			else
            			{
            				nlrModel.removeNlrData(nlrRawResults.get(index));

            				showHideButton.setText("Show");
            			}
            		}
            		catch (IOException e1) 
            		{
    					e1.printStackTrace();
    				}
				}
        	}
        });
        showHideButton.setEnabled(true);

        
//        final JLabel showLabel = new JLabel("Number Boundaries");
//        Object [] options2 = {
//        		10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
//        		110, 120, 130, 140, 150, 160, 170, 180, 190, 200,
//        		210, 220, 230, 240, 250
//        		};
//		numberOfBoundariesComboBox = new JComboBox(options2);
//		numberOfBoundariesComboBox.setMaximumSize(new Dimension(150, 23));
//		
//		nextButton = new JButton(">");
//        nextButton.setActionCommand(">");
//        nextButton.addActionListener(new ActionListener()
//        {
//			public void actionPerformed(ActionEvent e) 
//			{
//				if (resultIntervalCurrentlyShown != null)
//				{
//					// Only get the next block if there's something left to show.
//					if (resultIntervalCurrentlyShown.id2 < resultList.getModel().getSize())
//					{
//						resultIntervalCurrentlyShown.nextBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
//						showNISBoundaries(resultIntervalCurrentlyShown);
//					}
//				}
//				else
//				{
//					resultIntervalCurrentlyShown = new IdPair(0, (Integer)numberOfBoundariesComboBox.getSelectedItem());
//			    	showNISBoundaries(resultIntervalCurrentlyShown);
//				}
//			}
//        });
//        nextButton.setEnabled(true);
//
//        prevButton = new JButton("<");
//        prevButton.setActionCommand("<");
//        prevButton.addActionListener(new ActionListener()
//        {
//			public void actionPerformed(ActionEvent e) 
//			{
//				if (resultIntervalCurrentlyShown != null)
//				{
//					// Only get the prev block if there's something left to show.
//					if (resultIntervalCurrentlyShown.id1 > 0)
//					{
//						resultIntervalCurrentlyShown.prevBlock((Integer)numberOfBoundariesComboBox.getSelectedItem());
//						showNISBoundaries(resultIntervalCurrentlyShown);
//					}
//				}
//			}
//        });
//        prevButton.setEnabled(true);

        //resultSub1ControlsPanel.add(showLabel);
        //resultSub1ControlsPanel.add(numberOfBoundariesComboBox);
        //resultSub1ControlsPanel.add(Box.createHorizontalStrut(10));
        //resultSub1ControlsPanel.add(prevButton);
        //resultSub1ControlsPanel.add(nextButton);

        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
        		BoxLayout.LINE_AXIS));
        removeAllButton = new JButton(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NLRDataCollection model = (NLRDataCollection)modelManager.getModel(ModelManager.NLR_DATA);
				model.removeAllNlrData();
				//resultIntervalCurrentlyShown = null;
			}
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        resultSub2ControlsPanel.add(showHideButton);
        resultSub2ControlsPanel.add(removeAllButton);
        
        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        add(resultsPanel);
        
    }

//    public void actionPerformed(ActionEvent actionEvent)
//    {
//        try
//        {
//        	ArrayList<String> results = Database.getInstance().runQuery(
//        			Database.Datatype.NIS,
//        			new DateTime(startDate, DateTimeZone.UTC), 
//        			new DateTime(endDate, DateTimeZone.UTC),
//        			null,
//        			false,
//        			false,
//        			Double.parseDouble(fromDistanceTextField.getText()),
//        			Double.parseDouble(toDistanceTextField.getText()),
//        			0.0,
//        			0.0,
//        			null);
//
//        	setNISResults(results);
//        }
//        catch (Exception e) 
//        { 
//            e.printStackTrace();
//            System.out.println(e);
//            return;
//        }
//    }
    

//	private void setNISResults(ArrayList<String> results)
//	{
//		nlrResultsLabelText = results.size() + " spectra matched";
//    	resultsLabel.setText(nlrResultsLabelText);
//    	nlrResultListModel.clear();
//    	nlrRawResults = results;
//    	
//    	// add the results to the list
//    	for (String str : results)
//    	{
//    		//System.out.println(str);
//    		nlrResultListModel.addElement( 
//    				str.substring(16, 25) 
//    				+ ", day: " + str.substring(10, 13) + "/" + str.substring(5, 9)
//    				);
//    	}
//
//    	// Show the first set of boundaries
//    	this.resultIntervalCurrentlyShown = new IdPair(0, (Integer)this.numberOfBoundariesComboBox.getSelectedItem());
//    	this.showNISBoundaries(resultIntervalCurrentlyShown);
//	}
	
	public void mouseClicked(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public void mousePressed(MouseEvent e) 
	{
		//maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) 
	{
		//maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
        	int index = resultList.locationToIndex(e.getPoint());

        	if (index >= 0 && resultList.getCellBounds(index, index).contains(e.getPoint()))
        	{
        		//resultList.setSelectedIndex(index);
        		if (nlrModel.containsNlrData(nlrRawResults.get(index)))
        		{
        			showHideButton.setText("Hide");
				}
        		else
        		{
        			showHideButton.setText("Show");
        		}
        	}
        }
    }
	
//	private void showNLRData(String nlrPath)
//	{
//		NLRDataCollection model = (NLRDataCollection)modelManager.getModel(ModelManager.NLR_DATA);
//		model.removeAllNlrData();
//		
//		if (nlrPath != null)
//		{
//			try 
//			{
//				model.addNlrData(nlrPath);
//			} 
//			catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
//	}
}
