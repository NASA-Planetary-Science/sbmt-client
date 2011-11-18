package edu.jhuapl.near.gui;

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

import edu.jhuapl.near.model.LidarSearchDataCollection;


public class LidarPlot extends JFrame implements ChartMouseListener
{
    private LidarSearchDataCollection lidarModel;
    private XYDataset potentialDistanceDataset;
    private XYDataset potentialTimeDataset;
    private XYSeries potentialDistanceDataSeries;
    private XYSeries potentialTimeDataSeries;
    private XYSeries potentialDistanceSelectionSeries;
    private XYSeries potentialTimeSelectionSeries;

    public LidarPlot(LidarSearchDataCollection lidarModel, int trackId)
    {
        this.lidarModel = lidarModel;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        {
            potentialDistanceSelectionSeries = new XYSeries("Lidar Selection");
            potentialDistanceDataSeries = new XYSeries("Lidar Potential");

            potentialDistanceDataset = new XYSeriesCollection();
            ((XYSeriesCollection)potentialDistanceDataset).addSeries(potentialDistanceSelectionSeries);
            ((XYSeriesCollection)potentialDistanceDataset).addSeries(potentialDistanceDataSeries);

            JFreeChart chart1 = ChartFactory.createXYLineChart
            ("Potential vs. Distance", "Distance (km)", "Potential (J/kg)",
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
            potentialTimeSelectionSeries = new XYSeries("Lidar Selection");
            potentialTimeDataSeries = new XYSeries("Lidar Potential");

            potentialTimeDataset = new XYSeriesCollection();
            ((XYSeriesCollection)potentialTimeDataset).addSeries(potentialTimeSelectionSeries);
            ((XYSeriesCollection)potentialTimeDataset).addSeries(potentialTimeDataSeries);

            JFreeChart chart2 = ChartFactory.createXYLineChart
                    ("Potential vs. Time", "Time (sec)", "Potential (J/kg)",
                            potentialTimeDataset, PlotOrientation.VERTICAL, false, true, false);
//            JFreeChart chart2 = ChartFactory.createTimeSeriesChart
//              ("Lidar Potential vs. Time", "Time (sec)", "Potential (J/kg)",
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

        updateData(trackId);

        add(panel, BorderLayout.CENTER);


        setTitle("Potential");
        pack();
        setVisible(true);
    }

    private void updateData(int trackId)
    {
        selectPoint(-1);

        ArrayList<Double> potential = new ArrayList<Double>();
        ArrayList<Double> distance = new ArrayList<Double>();
        lidarModel.getPotentialVsDistance(trackId, potential, distance);

        potentialDistanceDataSeries.clear();
        if (potential.size() > 0 && distance.size() > 0)
        {
            for (int i=0; i<potential.size(); ++i)
                potentialDistanceDataSeries.add(distance.get(i), potential.get(i), false);
        }
        potentialDistanceDataSeries.fireSeriesChanged();



        potential.clear();
        ArrayList<Long> time = new ArrayList<Long>();
        lidarModel.getPotentialVsTime(trackId, potential, time);

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
            lidarModel.selectPoint(id);
        }

    }

    public void chartMouseMoved(ChartMouseEvent arg0)
    {
    }
}
