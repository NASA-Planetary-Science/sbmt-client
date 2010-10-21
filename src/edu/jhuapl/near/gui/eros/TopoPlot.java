package edu.jhuapl.near.gui.eros;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Line;
import edu.jhuapl.near.model.eros.DEMModel;
import edu.jhuapl.near.util.Properties;


public class TopoPlot implements ChartMouseListener, PropertyChangeListener
{
    private XYDataset heightDistanceDataset;
    private LineModel lineModel;
    private DEMModel demModel;
    private ChartPanel chartPanel;
    
    private int numberOfProfilesCreated = 0;
    
    public TopoPlot(LineModel lineModel, DEMModel demModel)
    {
    	this.lineModel = lineModel;
    	this.demModel = demModel;
    	
    	lineModel.addPropertyChangeListener(this);

        heightDistanceDataset = new XYSeriesCollection();

        JFreeChart chart1 = ChartFactory.createXYLineChart
        ("Height vs. Distance", "Distance (km)", "Height (km)",
                heightDistanceDataset, PlotOrientation.VERTICAL, false, true, false);

        // add the jfreechart graph
        chartPanel = new ChartPanel(chart1);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.addChartMouseListener(this);

        XYPlot plot = (XYPlot) chart1.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        
        XYItemRenderer r = plot.getRenderer();                                                                                                 
        if (r instanceof XYLineAndShapeRenderer) {                                                                                             
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;                                                                      
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(true);
        }
    }
    
    public JPanel getChartPanel()
    {
    	return chartPanel;
    }
    
    private void setSeriesColor(int lineId)
    {
    	Line line = (Line)lineModel.getStructure(lineId);
    	int[] c = line.color;
    	((XYPlot)chartPanel.getChart().getPlot()).getRenderer().setSeriesPaint(
    			lineId, new Color(c[0], c[1], c[2], c[3]));
    }
    
    private void addProfile()
    {
    	int lineId = lineModel.getNumberOfStructures()-1;
    	XYSeries series = new XYSeries("Profile " + numberOfProfilesCreated++);
    	((XYSeriesCollection)heightDistanceDataset).addSeries(series);
    	setSeriesColor(lineId);
    	((XYPlot)chartPanel.getChart().getPlot()).getRenderer().setSeriesStroke(
    			lineId, new BasicStroke(2.0f)); // set line thickness
    	updateProfile(lineId);
    }
    
    private void updateProfile(int lineId)
    {
    	Line line = (Line)lineModel.getStructure(lineId);
    	ArrayList<Double> height = new ArrayList<Double>(); 
    	ArrayList<Double> distance = new ArrayList<Double>(); 
    	demModel.generateProfile(line.xyzPointList, height, distance);
    	
    	XYSeries series = ((XYSeriesCollection)heightDistanceDataset).getSeries(lineId);
    	series.clear();
    	int N = height.size();
        for (int i=0; i<N; ++i)
        	series.add(distance.get(i), height.get(i), false);
        series.fireSeriesChanged();
    }
    
    private void removeProfile(int lineId)
    {
        ((XYSeriesCollection)heightDistanceDataset).removeSeries(lineId);
    }

    public String getProfileAsString(int lineId)
    {
    	StringBuilder buffer = new StringBuilder();
    	
		XYSeries series = ((XYSeriesCollection)heightDistanceDataset).getSeries(lineId);
		
        String eol = System.getProperty("line.separator");

		int N = series.getItemCount();

		buffer.append("Distance = ");
		for (int i=0; i<N; ++i)
			buffer.append(series.getX(i) + " ");

		buffer.append(eol);

		buffer.append("Height = ");
		for (int i=0; i<N; ++i)
			buffer.append(series.getY(i) + " ");

		buffer.append(eol);

		return buffer.toString();
    }
    
    public void chartMouseClicked(ChartMouseEvent arg0)
    {
    	ChartEntity entity = arg0.getEntity();
    	
    	if (entity instanceof XYItemEntity)
    	{
    		//int id = ((XYItemEntity)entity).getItem();
    	}
    }

    public void chartMouseMoved(ChartMouseEvent arg0)
    {
    }

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.VERTEX_INSERTED_INTO_LINE.equals(evt.getPropertyName()))
		{
			int lineId = (Integer)evt.getNewValue();
			Line line = (Line)lineModel.getStructure(lineId);
			
			if (line.controlPointIds.size() == 2)
				addProfile();
		}
		else if (Properties.VERTEX_POSITION_CHANGED.equals(evt.getPropertyName()))
		{
			int lineId = (Integer)evt.getNewValue();
			updateProfile(lineId);
		}
		else if (Properties.STRUCTURE_REMOVED.equals(evt.getPropertyName()))
		{
			int lineId = (Integer)evt.getNewValue();
			removeProfile(lineId);
		}
		else if (Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
		{
			int lineId = (Integer)evt.getNewValue();
			setSeriesColor(lineId);
		}
	}
}
