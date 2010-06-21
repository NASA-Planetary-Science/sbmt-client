package edu.jhuapl.near.gui.eros;

import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.near.gui.RadialOffsetChanger;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.model.eros.NLRDataCollection;
import edu.jhuapl.near.model.eros.NLRDataPerDay;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.util.DoublePair;


public class NLR2SearchPanel extends JPanel implements ListSelectionListener, ActionListener
{
	private final String NLR_REMOVE_ALL_BUTTON_TEXT = "Remove All NLR Data";
	
//    private final ModelManager modelManager;
    private NLRDataCollection nlrModel;
    private JList resultList;
    private DefaultListModel nlrResultListModel;
    //private NLRPopupMenu nlrPopupMenu;
    private ArrayList<String> nlrRawResults = new ArrayList<String>();
    private JLabel resultsLabel;
    private JButton showHideButton;
    private JButton removeAllButton;
    private NlrTimeIntervalChanger timeIntervalChanger;
    private RadialOffsetChanger radialOffsetChanger;
    
    private java.util.Date startDate = new GregorianCalendar(2000, 4, 1, 0, 0, 0).getTime();
    private java.util.Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private static final String START_DATE_LABEL_TEXT = "Start Date";
    private static final String END_DATE_LABEL_TEXT = "End Date:";
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JToggleButton selectRegionButton;

    public class NlrTimeIntervalChanger extends JPanel implements ChangeListener
    {
    	private RangeSlider slider;
    	
    	private NLRDataPerDay nlrData;
    	
    	public NlrTimeIntervalChanger()
    	{
    		setBorder(BorderFactory.createTitledBorder("Displayed NLR Data"));

    		slider = new RangeSlider(0, 255, 0, 255);
    		slider.setPaintTicks(true);
    		slider.setMajorTickSpacing(10);
    		slider.setPaintTrack(true);
    		slider.addChangeListener(this);
    		slider.setEnabled(false);
    		add(slider);
    	}
    	
    	void setNLRData(NLRDataPerDay data)
    	{
    		if (data != null)
    		{
    			nlrData = data;
    			DoublePair pair = data.getPercentageShown();
    			slider.setLowValue((int)(pair.d1*slider.getMaximum()));
    			slider.setHighValue((int)(pair.d2*slider.getMaximum()));
    			slider.setEnabled(true);
    		}
    		else
    		{
    			slider.setEnabled(false);
    		}
    	}

    	public void stateChanged(ChangeEvent e) 
    	{
    		double lowVal = (double)slider.getLowValue()/(double)slider.getMaximum();
    		double highVal = (double)slider.getHighValue()/(double)slider.getMaximum();
    		if (nlrData != null)
    			nlrData.setPercentageShown(lowVal, highVal);
    	}
    }

    public NLR2SearchPanel(
    		final ErosModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager) 
    {
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));
    	
    	//this.modelManager = modelManager;
    	this.nlrModel = (NLRDataCollection)modelManager.getModel(ErosModelManager.NLR_DATA);
    	
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

        /*
        JPanel browsePanel = new JPanel(new BorderLayout());
		
		resultsLabel = new JLabel("Available Files");

        nlrResultListModel = new DefaultListModel();

        nlrRawResults = nlrModel.getAllNlrPaths();
    	for (String str : nlrRawResults)
    	{
    		nlrResultListModel.addElement( 
    				str.substring(5, 13) 
    				+ ", day: " + str.substring(8, 11) + "/20" + str.substring(6, 8)
    				);
    	}

        //Create the list and put it in a scroll pane.
        resultList = new JList(nlrResultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(resultList);

        browsePanel.add(resultsLabel, BorderLayout.NORTH);
        browsePanel.add(listScrollPane, BorderLayout.CENTER);

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

            				showHideButton.setText("Remove");
        					timeIntervalChanger.setNLRData(nlrModel.getNlrData(nlrRawResults.get(index)));
            			}
            			else
            			{
            				nlrModel.removeNlrData(nlrRawResults.get(index));

            				showHideButton.setText("Show");
        					timeIntervalChanger.setNLRData(null);
            			}
            		}
            		catch (IOException e1) 
            		{
    					e1.printStackTrace();
    				}
				}
        	}
        });
        showHideButton.setEnabled(false);
        

        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
        		BoxLayout.LINE_AXIS));
        removeAllButton = new JButton(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.setActionCommand(NLR_REMOVE_ALL_BUTTON_TEXT);
        removeAllButton.addActionListener(new ActionListener()
        {
			public void actionPerformed(ActionEvent e) 
			{
				NLRDataCollection model = (NLRDataCollection)modelManager.getModel(ErosModelManager.NLR_DATA);
				model.removeAllNlrData();

				showHideButton.setText("Show");
				timeIntervalChanger.setNLRData(null);
			}
        });
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        resultSub2ControlsPanel.add(showHideButton);
        resultSub2ControlsPanel.add(removeAllButton);
        
        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        browsePanel.add(resultControlsPanel, BorderLayout.SOUTH);

        timeIntervalChanger = new NlrTimeIntervalChanger();
        
        
        add(browsePanel);
        add(timeIntervalChanger);
        */

        radialOffsetChanger = new RadialOffsetChanger(nlrModel, "Radial Offset");

        add(pane);
        add(radialOffsetChanger);
    }

	
	public void valueChanged(ListSelectionEvent arg0) 
	{
	    /*
		int[] idx = {arg0.getFirstIndex(), arg0.getLastIndex()};
		for (int index : idx)
		{
			if (index >= 0 && resultList.isSelectedIndex(index))
			{
				showHideButton.setEnabled(true);
				
				//resultList.setSelectedIndex(index);
				if (nlrModel.containsNlrData(nlrRawResults.get(index)))
				{
					showHideButton.setText("Remove");
					timeIntervalChanger.setNLRData(nlrModel.getNlrData(nlrRawResults.get(index)));
				}
				else
				{
					showHideButton.setText("Show");
					timeIntervalChanger.setNLRData(null);
				}
				break;
			}
			else
			{
				showHideButton.setEnabled(false);
			}
		}
		*/
	}


    public void actionPerformed(ActionEvent e)
    {
        // TODO Auto-generated method stub
        
    }

}
