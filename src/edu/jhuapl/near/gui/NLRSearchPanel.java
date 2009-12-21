package edu.jhuapl.near.gui;

import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;

import com.jidesoft.swing.RangeSlider;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.NLRDataCollection;
import edu.jhuapl.near.model.NLRDataPerDay;
import edu.jhuapl.near.pair.DoublePair;


public class NLRSearchPanel extends JPanel implements ListSelectionListener
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

    public NLRSearchPanel(
    		final ModelManager modelManager, 
    		ModelInfoWindowManager infoPanelManager,
    		vtkRenderWindowPanel renWin) 
    {
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));
    	
    	//this.modelManager = modelManager;
    	this.nlrModel = (NLRDataCollection)modelManager.getModel(ModelManager.NLR_DATA);
        
        
        JPanel resultsPanel = new JPanel(new BorderLayout());
		
		//nlrPopupMenu = new NISPopupMenu(this.modelManager, infoPanelManager, renWin);

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
				NLRDataCollection model = (NLRDataCollection)modelManager.getModel(ModelManager.NLR_DATA);
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
        
        resultsPanel.add(resultControlsPanel, BorderLayout.SOUTH);

        timeIntervalChanger = new NlrTimeIntervalChanger();
        
        radialOffsetChanger = new RadialOffsetChanger(nlrModel, "Radial Offset");
        
        add(resultsPanel);
        add(timeIntervalChanger);
        add(radialOffsetChanger);
    }

	
	public void valueChanged(ListSelectionEvent arg0) 
	{
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
	}

}
