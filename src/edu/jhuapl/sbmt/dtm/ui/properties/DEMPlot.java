package edu.jhuapl.sbmt.dtm.ui.properties;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.dtm.model.DEM;


public class DEMPlot implements ChartMouseListener, PropertyChangeListener
{
	// Ref vars
   private LineModel<Line> refLineModel;
   private DEM refDemModel;

   // State vars
    private XYSeriesCollection valueDistanceDataset;
    private ChartPanel chartPanel;
    private int coloringIndex;

    private int numberOfProfilesCreated = 0;

    public DEMPlot(LineModel<Line> aLineModel, DEM aDemModel, int aColoringIndex)
    {
        refLineModel = aLineModel;
        refDemModel = aDemModel;

        coloringIndex = aColoringIndex;

        aLineModel.addPropertyChangeListener(this);

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
   	 if (lineId == -1 || lineId >= valueDistanceDataset.getSeriesCount())
   		 return;

        Line line = refLineModel.getStructure(lineId);
        Color color = line.getColor();
        ((XYPlot)chartPanel.getChart().getPlot()).getRenderer().setSeriesPaint(
                lineId, color);
    }

    private void addProfile()
    {
        int lineId = refLineModel.getNumItems()-1;
        XYSeries series = new XYSeries("Profile " + numberOfProfilesCreated++);
        valueDistanceDataset.addSeries(series);
        setSeriesColor(lineId);
        ((XYPlot)chartPanel.getChart().getPlot()).getRenderer().setSeriesStroke(
                lineId, new BasicStroke(2.0f)); // set line thickness
        updateProfile(lineId);
    }

    private void updateProfile(int lineId)
    {
        if (lineId == -1 || lineId >= valueDistanceDataset.getSeriesCount())
            return;

        Line line = refLineModel.getStructure(lineId);
        List<Double> value= new ArrayList<Double>();
        List<Double> distance = new ArrayList<Double>();
        refDemModel.generateProfile(line.xyzPointList, value, distance, coloringIndex);

        XYSeries series = valueDistanceDataset.getSeries(lineId);
        series.clear();
        int N = value.size();
        for (int i=0; i<N; ++i)
            series.add(distance.get(i), value.get(i), false);
        series.fireSeriesChanged();
    }

    private void updateChartLabels(){
        // Figure out labels to use
        String title, domainLabel, rangeLabel;
        String[] coloringNames = refDemModel.getColoringNames();
        String[] coloringUnits = refDemModel.getColoringUnits();
        int numColors = coloringNames.length;

        if(coloringIndex < 0 || coloringIndex >= numColors)
        {
            // By default show radius
            title = "Radius vs. Distance";
            domainLabel = "Distance (m)";
            rangeLabel = "Radius (m)";
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
        if (lineId == -1 || lineId >= valueDistanceDataset.getSeriesCount())
      	  return;

        valueDistanceDataset.removeSeries(lineId);
    }

    public String getProfileAsString(int lineId)
    {
        // Figure out what to label the range
        String rangeLabel;
        String[] coloringNames = refDemModel.getColoringNames();
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

        XYSeries series = valueDistanceDataset.getSeries(lineId);

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
        int numLines = valueDistanceDataset.getSeriesCount();
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
   	 Line line = null;
   	 if (evt.getNewValue() instanceof Line)
   		 line = (Line)evt.getNewValue();
		 int lineId = refLineModel.getAllItems().indexOf(evt.getNewValue());

        if (Properties.VERTEX_INSERTED_INTO_LINE.equals(evt.getPropertyName()))
        {
            if (line != null && line.controlPointIds.size() == 2)
                addProfile();
        }
        else if (Properties.VERTEX_POSITION_CHANGED.equals(evt.getPropertyName()))
        {
            updateProfile(lineId);
        }
        else if (Properties.STRUCTURE_REMOVED.equals(evt.getPropertyName()))
        {
            removeProfile(lineId);
        }
        else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()))
        {
            valueDistanceDataset.removeAllSeries();
        }
        else if (Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
        {
            setSeriesColor(lineId);
        }
    }
}
