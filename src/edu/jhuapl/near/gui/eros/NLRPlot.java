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


public class NLRPlot extends JFrame
{
	public NLRPlot(JFreeChart chart)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		
        // add the jfreechart graph
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
        add(panel, BorderLayout.CENTER);
        
        setTitle("NLR Potential");
		pack();
        setVisible(true);
	}
	
	static public void plotPotentialVsDistance(ArrayList<Double> potential, ArrayList<Double> distance)
	{
        XYSeries series = new XYSeries("NLR Potential");
        for (int i=0; i<potential.size(); ++i)
        	series.add(distance.get(i), potential.get(i));
        XYDataset xyDataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart
                ("NLR Potential vs. Distance", "Distance (km)", "Potential (J/kg)",
                		xyDataset, PlotOrientation.VERTICAL, true, true, false);
	
        new NLRPlot(chart);
	}

	static public void plotPotentialVsTime(ArrayList<Double> potential, ArrayList<Long> time)
	{
		long t0 = time.get(0);
        XYSeries series = new XYSeries("NLR Potential");
        for (int i=0; i<potential.size(); ++i)
        	series.add((double)(time.get(i)-t0)/1000.0, potential.get(i));
        XYDataset xyDataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart
        		("NLR Potential vs. Time", "Time (sec)", "Potential (J/kg)",
                		xyDataset, PlotOrientation.VERTICAL, true, true, false);
//        JFreeChart chart2 = ChartFactory.createTimeSeriesChart
//        	("NLR Potential vs. Time", "Time (sec)", "Potential (J/kg)",
//        		xyDataset, true, true, false);
        new NLRPlot(chart);
	}
}
