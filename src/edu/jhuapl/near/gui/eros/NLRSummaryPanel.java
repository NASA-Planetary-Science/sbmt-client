package edu.jhuapl.near.gui.eros;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;

import edu.jhuapl.near.gui.AnyFileChooser;
import edu.jhuapl.near.gui.RadialOffsetChanger;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.NLRBrowseDataCollection;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;


public class NLRSummaryPanel extends JPanel
{
	private final String NLR_REMOVE_ALL_BUTTON_TEXT = "Remove All NLR Data";
	
    private NLRBrowseDataCollection nlrModel;
    private JList resultList;
    private ArrayList<String> nlrRawResults = new ArrayList<String>();
    private JButton showHideButton;
    private JButton saveButton;
    private RadialOffsetChanger radialOffsetChanger;
    

    public NLRSummaryPanel(
    		final ModelManager modelManager) 
    {
    	setLayout(new BoxLayout(this,
        		BoxLayout.PAGE_AXIS));
    	
    	//this.modelManager = modelManager;
    	this.nlrModel = (NLRBrowseDataCollection)modelManager.getModel(ModelNames.NLR_DATA_BROWSE);
        
        
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
        	}
        });
        showHideButton.setEnabled(false);
        

        saveButton = new JButton("Save...");
        saveButton.setActionCommand("Save...");
        saveButton.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		int index = resultList.getSelectedIndex();
        		if (index >= 0)
        		{
        			File tmp = new File(nlrRawResults.get(index));
        			File file = AnyFileChooser.showSaveDialog(
        					saveButton.getParent(),
        					"Save NLR data",
        					tmp.getName().substring(0, tmp.getName().length()-3));
        			
        			try
        			{
        				if (file != null)
        				{
        					File nlrFile = FileCache.getFileFromServer(nlrRawResults.get(index));

        					FileUtil.copyFile(nlrFile, file);
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
        saveButton.setEnabled(false);

        
        JPanel resultSub2ControlsPanel = new JPanel();
        resultSub2ControlsPanel.setLayout(new BoxLayout(resultSub2ControlsPanel,
        		BoxLayout.LINE_AXIS));
        
        resultSub2ControlsPanel.add(showHideButton);
        resultSub2ControlsPanel.add(saveButton);
        
        resultControlsPanel.add(resultSub1ControlsPanel, BorderLayout.CENTER);
        resultControlsPanel.add(resultSub2ControlsPanel, BorderLayout.SOUTH);
        
        radialOffsetChanger = new RadialOffsetChanger(nlrModel, "Radial Offset");
        
        add(radialOffsetChanger);
    }

}
