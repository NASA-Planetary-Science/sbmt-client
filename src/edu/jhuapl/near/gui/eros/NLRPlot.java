package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.near.model.eros.NLRDataCollection2.NLRPoint;

public class NLRPlot extends JFrame
{
	public NLRPlot(int type, ArrayList<NLRPoint> data)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		
        // add the jfreechart graph
        XYSeries series = new XYSeries("NIS Spectrum");
        double[] wavelengths = {1};//this.nisSpectrum.getBandCenters();
        double[] spectrum = {1};//this.nisSpectrum.getSpectrum();
        for (int i=0; i<wavelengths.length; ++i)
        	series.add(wavelengths[i], spectrum[i]);
        XYDataset xyDataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart
                ("NIS Calibrated Spectrum", "Wavelength (nm)", "Reflectance",
                		xyDataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer r = plot.getRenderer();                                                                                                 
        if (r instanceof XYLineAndShapeRenderer) {                                                                                             
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;                                                                      
            renderer.setBaseShapesVisible(true);                                                                                               
            renderer.setBaseShapesFilled(true);                                                                                                
            renderer.setDrawSeriesLineAsPath(true);
        }
        
        panel.add(chartPanel, BorderLayout.CENTER);

	}
}
