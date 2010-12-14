package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;
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

import edu.jhuapl.near.model.eros.NLRSearchDataCollection;


public class NLRPlot extends JFrame implements ChartMouseListener
{
    private NLRSearchDataCollection nlrModel;
    private XYDataset potentialDistanceDataset;
    private XYDataset potentialTimeDataset;
    private XYSeries potentialDistanceDataSeries;
    private XYSeries potentialTimeDataSeries;
    private XYSeries potentialDistanceSelectionSeries;
    private XYSeries potentialTimeSelectionSeries;

    public NLRPlot(NLRSearchDataCollection nlrModel)
    {
        this.nlrModel = nlrModel;
        
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

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
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.addChartMouseListener(this);

            XYPlot plot = (XYPlot) chart1.getPlot();
            plot.setDomainPannable(true);
            plot.setRangePannable(true);
            
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
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.addChartMouseListener(this);

            XYPlot plot = (XYPlot) chart2.getPlot();
            plot.setDomainPannable(true);
            plot.setRangePannable(true);
            
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


        setTitle("NLR Potential");
        pack();
        setVisible(true);
    }
    
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
    
    public void chartMouseClicked(ChartMouseEvent arg0)
    {
        ChartEntity entity = arg0.getEntity();
        
        if (entity instanceof XYItemEntity)
        {
            int id = ((XYItemEntity)entity).getItem();
            selectPoint(id);
            nlrModel.selectPoint(id);
        }
        
    }

    public void chartMouseMoved(ChartMouseEvent arg0)
    {
    }
}
