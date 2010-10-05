package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import edu.jhuapl.near.model.*;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.popupmenus.eros.NISPopupMenu;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

public class NISSpectrumInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
	private ModelManager modelManager;
	private NISSpectrum nisSpectrum;
	
	public NISSpectrumInfoPanel(NISSpectrum nisSpectrum, ModelManager modelManager)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.modelManager = modelManager;
		this.nisSpectrum = nisSpectrum;
		
        JPanel panel = new JPanel(new BorderLayout());
		
        
        // add the jfreechart graph
        XYSeries series = new XYSeries("NIS Spectrum");
        double[] wavelengths = this.nisSpectrum.getBandCenters();
        double[] spectrum = this.nisSpectrum.getSpectrum();
        for (int i=0; i<wavelengths.length; ++i)
        	series.add(wavelengths[i], spectrum[i]);
        XYDataset xyDataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart
                ("NIS Calibrated Spectrum", "Wavelength (nm)", "Reflectance",
                		xyDataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        
        XYItemRenderer r = plot.getRenderer();                                                                                                 
        if (r instanceof XYLineAndShapeRenderer) {                                                                                             
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;                                                                      
            renderer.setBaseShapesVisible(true);                                                                                               
            renderer.setBaseShapesFilled(true);                                                                                                
            renderer.setDrawSeriesLineAsPath(true);
        }
        
        panel.add(chartPanel, BorderLayout.CENTER);

        
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,
        		BoxLayout.PAGE_AXIS));

		// Add a text box for showing information about the image
		String[] columnNames = {"Property",
                "Value"};

		HashMap<String, String> properties = null;
		Object[][] data = {	{"", ""} };
		
		try 
		{
			
			properties = this.nisSpectrum.getProperties();
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
        setTitle("NIS Spectrum Properties");

        pack();
        setVisible(true);
	}

	
	public Model getModel()
	{
		return nisSpectrum;
	}
	

	/**
	 * The following function is a bit of a hack. We want to reuse the MSIPopupMenu
	 * class, but instead of having a right-click popup menu, we want instead to use
	 * it as an actual menu in a menu bar. Therefore we simply grab the menu items
	 * from that class and put these in our new JMenu.
	 */
    private void createMenus()
    {
    	NISPopupMenu msiImagesPopupMenu = 
			new NISPopupMenu(modelManager, null);
    	
    	msiImagesPopupMenu.setCurrentSpectrum(nisSpectrum.getServerPath());
    	
    	JMenuBar menuBar = new JMenuBar();

    	JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = msiImagesPopupMenu.getComponents();
        for (Component item : components)
        {
        	if (item instanceof JMenuItem)
        		menu.add(item);
        }
        
        menuBar.add(menu);
        
        setJMenuBar(menuBar);
    }

	public void propertyChange(PropertyChangeEvent arg0) 
	{
	}
}
