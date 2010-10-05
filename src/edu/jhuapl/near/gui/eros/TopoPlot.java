package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
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
import edu.jhuapl.near.model.LineModel.Line;
import edu.jhuapl.near.util.Properties;


public class TopoPlot extends JPanel implements ChartMouseListener, PropertyChangeListener
{
//    private NLRSearchDataCollection nlrModel;
    private XYDataset heightDistanceDataset;
//    private XYDataset potentialTimeDataset;
//    private XYSeries potentialDistanceDataSeries;
//    private XYSeries potentialTimeDataSeries;
//    private XYSeries potentialDistanceSelectionSeries;
//    private XYSeries potentialTimeSelectionSeries;
    private ArrayList<XYSeries> allSeries;
    private LineModel lineModel;
    
    public TopoPlot(LineModel lineModel)
    {
    	this.lineModel = lineModel;
    	lineModel.addPropertyChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout());

        heightDistanceDataset = new XYSeriesCollection();

        JFreeChart chart1 = ChartFactory.createXYLineChart
        ("Height vs. Distance", "Distance (km)", "Height (km)",
                heightDistanceDataset, PlotOrientation.VERTICAL, false, true, false);

        // add the jfreechart graph
        ChartPanel chartPanel = new ChartPanel(chart1);

        chartPanel.addChartMouseListener(this);

        XYPlot plot = (XYPlot) chart1.getPlot();
        XYItemRenderer r = plot.getRenderer();                                                                                                 
        if (r instanceof XYLineAndShapeRenderer) {                                                                                             
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;                                                                      
            renderer.setBaseShapesVisible(true);                                                                                               
            renderer.setBaseShapesFilled(true);                                                                                                
            renderer.setDrawSeriesLineAsPath(true);
            renderer.setSeriesPaint(0, Color.BLACK);
            renderer.setSeriesPaint(1, Color.RED);
        }

        panel.add(chartPanel, BorderLayout.CENTER);

        add(panel, BorderLayout.CENTER);

    	/*    	
        this.nlrModel = nlrModel;
        
        JPanel panel = new JPanel(new BorderLayout());
        
        {
            potentialDistanceSelectionSeries = new XYSeries("NLR Selection");
            potentialDistanceDataSeries = new XYSeries("NLR Potential");

            potentialDistanceDataset = new XYSeriesCollection();
            ((XYSeriesCollection)potentialDistanceDataset).addSeries(potentialDistanceSelectionSeries);
            ((XYSeriesCollection)potentialDistanceDataset).addSeries(potentialDistanceDataSeries);

            JFreeChart chart1 = ChartFactory.createXYLineChart
            ("NLR Potential vs. Distance", "Distance (km)", "Potential (J/kg)",
                    potentialDistanceDataset, PlotOrientation.VERTICAL, false, true, false);

            // add the jfreechart graph
            ChartPanel chartPanel = new ChartPanel(chart1);

            chartPanel.addChartMouseListener(this);

            XYPlot plot = (XYPlot) chart1.getPlot();
            XYItemRenderer r = plot.getRenderer();                                                                                                 
            if (r instanceof XYLineAndShapeRenderer) {                                                                                             
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;                                                                      
                renderer.setBaseShapesVisible(true);                                                                                               
                renderer.setBaseShapesFilled(true);                                                                                                
                renderer.setDrawSeriesLineAsPath(true);
                renderer.setSeriesPaint(0, Color.BLACK);
                renderer.setSeriesPaint(1, Color.RED);
            }

            panel.add(chartPanel, BorderLayout.CENTER);

        }
        
        {
            potentialTimeSelectionSeries = new XYSeries("NLR Selection");
            potentialTimeDataSeries = new XYSeries("NLR Potential");

            potentialTimeDataset = new XYSeriesCollection();
            ((XYSeriesCollection)potentialTimeDataset).addSeries(potentialTimeSelectionSeries);
            ((XYSeriesCollection)potentialTimeDataset).addSeries(potentialTimeDataSeries);

            JFreeChart chart2 = ChartFactory.createXYLineChart
                    ("NLR Potential vs. Time", "Time (sec)", "Potential (J/kg)",
                            potentialTimeDataset, PlotOrientation.VERTICAL, false, true, false);
//            JFreeChart chart2 = ChartFactory.createTimeSeriesChart
//              ("NLR Potential vs. Time", "Time (sec)", "Potential (J/kg)",
//                  xyDataset, true, true, false);

            // add the jfreechart graph
            ChartPanel chartPanel = new ChartPanel(chart2);

            chartPanel.addChartMouseListener(this);

            XYPlot plot = (XYPlot) chart2.getPlot();
            XYItemRenderer r = plot.getRenderer();                                                                                                 
            if (r instanceof XYLineAndShapeRenderer) {                                                                                             
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;                                                                      
                renderer.setBaseShapesVisible(true);                                                                                               
                renderer.setBaseShapesFilled(true);                                                                                                
                renderer.setDrawSeriesLineAsPath(true);
                renderer.setSeriesPaint(0, Color.BLACK);
                renderer.setSeriesPaint(1, Color.RED);
            }

            panel.add(chartPanel, BorderLayout.SOUTH);

        }

        updateData();
        
        add(panel, BorderLayout.CENTER);


        setVisible(true);
*/
    }
    /*
    public void updateData()
    {
        selectPoint(-1);

        ArrayList<Double> potential = new ArrayList<Double>();
        ArrayList<Double> distance = new ArrayList<Double>();
        nlrModel.getPotentialVsDistance(potential, distance);

        potentialDistanceDataSeries.clear();
        if (potential.size() > 0 && distance.size() > 0)
        {
        	for (int i=0; i<potential.size(); ++i)
        		potentialDistanceDataSeries.add(distance.get(i), potential.get(i), false);
        }
        potentialDistanceDataSeries.fireSeriesChanged();

        

        potential.clear();
        ArrayList<Long> time = new ArrayList<Long>();
        nlrModel.getPotentialVsTime(potential, time);

        potentialTimeDataSeries.clear();
        if (potential.size() > 0 && time.size() > 0)
        {
        	long t0 = time.get(0);
        	for (int i=0; i<potential.size(); ++i)
        		potentialTimeDataSeries.add((double)(time.get(i)-t0)/1000.0, potential.get(i), false);
        }
        potentialTimeDataSeries.fireSeriesChanged();
    }


    public void selectPoint(int ptId)
    {
        potentialDistanceSelectionSeries.clear();
    	if (ptId >= 0)
    	{
    		potentialDistanceSelectionSeries.add(
    				potentialDistanceDataSeries.getX(ptId),
    				potentialDistanceDataSeries.getY(ptId), true);
    	}

        

        potentialTimeSelectionSeries.clear();
        if (ptId >= 0)
        {
    		potentialTimeSelectionSeries.add(
    				potentialTimeDataSeries.getX(ptId),
    				potentialTimeDataSeries.getY(ptId), true);
        }
    }
    */
    
    private void generateProfile(Line line, ArrayList<Double> height, ArrayList<Double> distance)
    {
    }
    
    private void addProfile(int lineId)
    {
    	XYSeries series = new XYSeries("Profile " + lineId);
    	allSeries.add(series);
        ((XYSeriesCollection)heightDistanceDataset).addSeries(series);
    }
    
    private void updateProfile(int lineId)
    {
    	Line line = (Line)lineModel.getStructure(lineId);
    	ArrayList<Double> height = new ArrayList<Double>(); 
    	ArrayList<Double> distance = new ArrayList<Double>(); 
    	generateProfile(line, height, distance);
    }
    
    private void removeProfile(int lineId)
    {
    	allSeries.remove(lineId);
        ((XYSeriesCollection)heightDistanceDataset).removeSeries(lineId);
    }
    
    public void chartMouseClicked(ChartMouseEvent arg0)
    {
    	ChartEntity entity = arg0.getEntity();
    	
    	if (entity instanceof XYItemEntity)
    	{
    		int id = ((XYItemEntity)entity).getItem();
    		//selectPoint(id);
    		//nlrModel.selectPoint(id);
    	}
    	
    }

    public void chartMouseMoved(ChartMouseEvent arg0)
    {
    }

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.VERTEX_INSERTED_INTO_LINE.equals(evt.getNewValue()) ||
				Properties.VERTEX_POSITION_CHANGED.equals(evt.getNewValue()) ||
				Properties.LINE_SELECTED.equals(evt.getNewValue()))
			System.out.println(evt.getPropertyName());
	}
}
