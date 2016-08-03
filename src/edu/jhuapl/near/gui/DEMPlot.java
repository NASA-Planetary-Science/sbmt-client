package edu.jhuapl.near.gui;

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

import edu.jhuapl.near.model.DEM;
import edu.jhuapl.near.model.Line;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.util.Properties;


public class DEMPlot implements ChartMouseListener, PropertyChangeListener
{
    private XYDataset valueDistanceDataset;
    private LineModel lineModel;
    private DEM demModel;
    private ChartPanel chartPanel;
    private int coloringIndex;

    private int numberOfProfilesCreated = 0;

    public DEMPlot(LineModel lineModel, DEM demModel, int coloringIndex)
    {
        this.lineModel = lineModel;
        this.demModel = demModel;
        this.coloringIndex = coloringIndex;

        lineModel.addPropertyChangeListener(this);

        valueDistanceDataset = new XYSeriesCollection();

        JFreeChart chart1 = ChartFactory.createXYLineChart("", "", "",
            valueDistanceDataset, PlotOrientation.VERTICAL, false, true, false);

        // add the jfreechart graph
        chartPanel = new ChartPanel(chart1);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.addChartMouseListener(this);
        updateChartLabels();

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
        ((XYSeriesCollection)valueDistanceDataset).addSeries(series);
        setSeriesColor(lineId);
        ((XYPlot)chartPanel.getChart().getPlot()).getRenderer().setSeriesStroke(
                lineId, new BasicStroke(2.0f)); // set line thickness
        updateProfile(lineId);
    }

    private void updateProfile(int lineId)
    {
        if (lineId >= ((XYSeriesCollection)valueDistanceDataset).getSeriesCount())
            return;

        Line line = (Line)lineModel.getStructure(lineId);
        ArrayList<Double> value= new ArrayList<Double>();
        ArrayList<Double> distance = new ArrayList<Double>();
        demModel.generateProfile(line.xyzPointList, value, distance, coloringIndex);

        XYSeries series = ((XYSeriesCollection)valueDistanceDataset).getSeries(lineId);
        series.clear();
        int N = value.size();
        for (int i=0; i<N; ++i)
            series.add(distance.get(i), value.get(i), false);
        series.fireSeriesChanged();
    }

    private void updateChartLabels(){
        // Figure out labels to use
        String title, domainLabel, rangeLabel;
        String[] coloringNames = demModel.getColoringNames();
        String[] coloringUnits = demModel.getColoringUnits();
        int numColors = coloringNames.length;

        if(coloringIndex < 0 || coloringIndex >= numColors)
        {
            title = "";
            domainLabel = "";
            rangeLabel = "";
        }
        else
        {
            title = coloringNames[coloringIndex] + " vs. Distance";
            domainLabel = "Distance (m)";
            rangeLabel = coloringNames[coloringIndex];

            // Try to add units for range label if possible
            if(coloringUnits[coloringIndex].length() > 0)
            {
                rangeLabel += " (" + coloringUnits[coloringIndex] + ")";
            }
        }

        // Apply the labels to the chart
        chartPanel.getChart().setTitle(title);;
        chartPanel.getChart().getXYPlot().getDomainAxis().setLabel(domainLabel);
        chartPanel.getChart().getXYPlot().getRangeAxis().setLabel(rangeLabel);
    }

    private void removeProfile(int lineId)
    {
        if (lineId < ((XYSeriesCollection)valueDistanceDataset).getSeriesCount())
            ((XYSeriesCollection)valueDistanceDataset).removeSeries(lineId);
    }

    public String getProfileAsString(int lineId)
    {
        // Figure out what to label the range
        String rangeLabel;
        String[] coloringNames = demModel.getColoringNames();
        int numColors = coloringNames.length;

        if(coloringIndex < 0 || coloringIndex >= numColors)
        {
            rangeLabel = "Value";
        }
        else
        {
            rangeLabel = coloringNames[coloringIndex];
        }

        StringBuilder buffer = new StringBuilder();

        XYSeries series = ((XYSeriesCollection)valueDistanceDataset).getSeries(lineId);

        String eol = System.getProperty("line.separator");

        int N = series.getItemCount();

        buffer.append("Distance=");
        for (int i=0; i<N; ++i)
            buffer.append(series.getX(i) + " ");

        buffer.append(eol);

        buffer.append(rangeLabel + "=");
        for (int i=0; i<N; ++i)
            buffer.append(series.getY(i) + " ");

        buffer.append(eol);

        return buffer.toString();
    }

    public void setColoringIndex(int index){
        // Save value of index
        coloringIndex = index;

        // Update chart labels
        updateChartLabels();

        // Update all profiles
        int numLines = ((XYSeriesCollection)valueDistanceDataset).getSeriesCount();
        for(int i=0; i<numLines; i++){
            updateProfile(i);
        }
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
        else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()))
        {
            ((XYSeriesCollection)valueDistanceDataset).removeAllSeries();
        }
        else if (Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
        {
            int lineId = (Integer)evt.getNewValue();
            setSeriesColor(lineId);
        }
    }
}
