package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.near.model.eros.NLRDataCollection2;


public class NLRPlot extends JFrame implements ChartMouseListener
{
    private NLRDataCollection2 nlrModel;
    private XYDataset potentialDistanceDataset;
    private XYDataset potentialTimeDataset;

    public NLRPlot(NLRDataCollection2 nlrModel)
    {
        this.nlrModel = nlrModel;
        
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        
        {
            XYSeries series = new XYSeries("NLR Selection");
            potentialDistanceDataset = new XYSeriesCollection(series);

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
            }

            panel.add(chartPanel, BorderLayout.CENTER);

        }
        
        {
            XYSeries series = new XYSeries("NLR Selection");
            potentialTimeDataset = new XYSeriesCollection(series);

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
            }

            panel.add(chartPanel, BorderLayout.SOUTH);

        }

        updateData();
        
        add(panel, BorderLayout.CENTER);


        setTitle("NLR Potential");
        pack();
        setVisible(true);
    }
    
    public void updateData()
    {
        ArrayList<Double> potential = new ArrayList<Double>();
        ArrayList<Double> distance = new ArrayList<Double>();
        nlrModel.getPotentialVsDistance(potential, distance);

        XYSeries series = new XYSeries("NLR Potential");
        for (int i=0; i<potential.size(); ++i)
            series.add(distance.get(i), potential.get(i));
        
        if (((XYSeriesCollection)potentialDistanceDataset).getSeriesCount() > 1)
            ((XYSeriesCollection)potentialDistanceDataset).removeSeries(1);
        
        ((XYSeriesCollection)potentialDistanceDataset).addSeries(series);

        
        potential.clear();
        ArrayList<Long> time = new ArrayList<Long>();
        nlrModel.getPotentialVsTime(potential, time);

        long t0 = time.get(0);
        series = new XYSeries("NLR Potential");
        for (int i=0; i<potential.size(); ++i)
            series.add((double)(time.get(i)-t0)/1000.0, potential.get(i));

        if (((XYSeriesCollection)potentialTimeDataset).getSeriesCount() > 1)
            ((XYSeriesCollection)potentialTimeDataset).removeSeries(1);

        ((XYSeriesCollection)potentialTimeDataset).addSeries(series);
    }


    public void selectPoint(int ptId)
    {
        
    }
    
    public void chartMouseClicked(ChartMouseEvent arg0)
    {
        System.out.println(arg0.getEntity());
    }

    public void chartMouseMoved(ChartMouseEvent arg0)
    {
        //System.out.println(arg0);
    }
}
