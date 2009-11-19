package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import edu.jhuapl.near.gui.popupmenus.MSIPopupMenu;
import edu.jhuapl.near.model.*;

import vtk.*;

public class NISSpectrumInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
	private ModelManager modelManager;
	
	public NISSpectrumInfoPanel(ModelManager modelManager)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.modelManager = modelManager;
		
		//image.addPropertyChangeListener(this);
		
        //vtkImageData displayedImage = image.getDisplayedImage();
        
        JPanel panel = new JPanel(new BorderLayout());
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,
        		BoxLayout.PAGE_AXIS));

		// Add a text box for showing information about the image
		String[] columnNames = {"Property",
                "Value"};

		HashMap<String, String> properties = null;
		Object[][] data = {	{"", ""} };
		/*
		try 
		{
			
			properties = image.getProperties();
			TreeMap<String, String> sortedProperties = new TreeMap<String, String>(properties);
			int size = properties.size();
			data = new Object[size][2];
			
			int i=0;
			for (String key : sortedProperties.keySet())
			{
				data[i][0] = key;
				data[i][1] = sortedProperties.get(key);
				
				++i;
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			*/
		

		JTable table = new JTable(data, columnNames)
		{
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		table.setBorder(BorderFactory.createTitledBorder(""));
		table.setPreferredScrollableViewportSize(new Dimension(500, 130));

		JScrollPane scrollPane = new JScrollPane(table);
		
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(scrollPane);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(panel, BorderLayout.CENTER);
        
        createMenus();
        
        // Finally make the frame visible
        //setTitle("MSI Image " + image.getName() + " Properties");
        //this.set
        pack();
        setVisible(true);
	}

	public Model getModel()
	{
		return null;
	}
	

	/**
	 * The following function is a bit of a hack. We want to reuse the MSIPopupMenu
	 * class, but instead of having a right-click popup menu, we want instead to use
	 * it as an actual menu in a menu bar. Therefore we simply grab the menu items
	 * from that class and put these in our new JMenu.
	 */
    private void createMenus()
    {
    }

	public void propertyChange(PropertyChangeEvent arg0) 
	{
	}
}
