package edu.jhuapl.sbmt.dtm.ui.properties;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
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

import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.plot.BaseLinePlot;
import edu.jhuapl.sbmt.dtm.model.DEM;

public class DEMPlot extends BaseLinePlot implements ChartMouseListener
{
	private LineModel<PolyLine> refManager;
	private DEM refDemModel;

	// State vars
	private ChartPanel chartPanel;
	private int coloringIndex;

	public DEMPlot(LineModel<PolyLine> aManager, DEM aDemModel, int aColoringIndex)
	{
		super(aManager);

		refManager = aManager;
		refDemModel = aDemModel;

		coloringIndex = aColoringIndex;

		JFreeChart chart1 = ChartFactory.createXYLineChart("", "", "", getXYDataSet(), PlotOrientation.VERTICAL, false,
				true, false);

		// add the jfreechart graph
		chartPanel = new ChartPanel(chart1);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.addChartMouseListener(this);
		updateChartLabels();

		XYPlot plot = (XYPlot) chart1.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer)
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(false);
			renderer.setBaseShapesFilled(true);
		}

	}

	@Override
	public ChartPanel getChartPanel()
	{
		return chartPanel;
	}

	@Override
	protected void getPlotPoints(PolyLine aItem, List<Double> xValueL, List<Double> yValueL)
	{
		// Bail if the line is not visible or valid
		if (aItem.getVisible() == false || aItem.getControlPoints().size() < 2)
			return;

		// Bail if there are no points for this plot
		List<Vector3D> xyzPointL = refManager.getXyzPointsFor(aItem);
		if (xyzPointL.size() == 0)
			return;

		refDemModel.generateProfile(xyzPointL, yValueL, xValueL, coloringIndex);
	}

	private void updateChartLabels()
	{
		// Figure out labels to use
		String title, domainLabel, rangeLabel;
		String[] coloringNames = refDemModel.getColoringNames();
		String[] coloringUnits = refDemModel.getColoringUnits();
		int numColors = coloringNames.length;

		if (coloringIndex < 0 || coloringIndex >= numColors)
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
			if (coloringUnits[coloringIndex].length() > 0)
			{
				rangeLabel += " (" + coloringUnits[coloringIndex] + ")";
			}
		}

		// Apply the labels to the chart
		chartPanel.getChart().setTitle(title);
		chartPanel.getChart().getXYPlot().getDomainAxis().setLabel(domainLabel);
		chartPanel.getChart().getXYPlot().getRangeAxis().setLabel(rangeLabel);
	}

	public String getProfileAsString(PolyLine aItem)
	{
		// Figure out what to label the range
		String[] coloringNames = refDemModel.getColoringNames();

		String rangeLabel = "Value";
		if (coloringIndex >= 0 && coloringIndex < coloringNames.length)
			rangeLabel = coloringNames[coloringIndex];

		StringBuilder buffer = new StringBuilder();

		XYSeries series = getSeriesFor(aItem);

		String eol = System.getProperty("line.separator");

		int N = series.getItemCount();

		buffer.append("Distance=");
		for (int i = 0; i < N; ++i)
			buffer.append(series.getX(i) + " ");
		buffer.append(eol);

		buffer.append(rangeLabel + "=");
		for (int i = 0; i < N; ++i)
			buffer.append(series.getY(i) + " ");
		buffer.append(eol);

		return buffer.toString();
	}

	public void setColoringIndex(int index)
	{
		// Save value of index
		coloringIndex = index;

		// Update chart labels
		updateChartLabels();

		// Update all profiles
		notifyAllStale();
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent arg0)
	{
		ChartEntity entity = arg0.getEntity();
		if (entity instanceof XYItemEntity)
		{
			// int id = ((XYItemEntity)entity).getItem();
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0)
	{
	}

}
