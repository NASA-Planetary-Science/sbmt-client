package edu.jhuapl.sbmt.gui.lidar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.Track;
import edu.jhuapl.sbmt.util.TimeUtil;


public class LidarPlot extends JFrame implements ChartMouseListener
{
    private LidarSearchDataCollection lidarModel;
    private XYDataset distanceDataset;
    private XYDataset timeDataset;
    private XYSeries distanceDataSeries;
    private XYSeries timeDataSeries;
    private XYSeries distanceSelectionSeries;
    private XYSeries timeSelectionSeries;
    private List<Double> data;
    private List<Double> distance;
    private List<Double> time;
    private String name;

    public LidarPlot(LidarSearchDataCollection lidarModel,
            List<Double> data,
            List<Double> distance,
            List<Double> time,
            String name,
            String units)
    {
        this.lidarModel = lidarModel;
        this.data = data;
        this.distance = distance;
        this.time = time;
        this.name = name;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        {
            distanceSelectionSeries = new XYSeries("Lidar Selection");
            distanceDataSeries = new XYSeries("Lidar Data");

            distanceDataset = new XYSeriesCollection();
            ((XYSeriesCollection)distanceDataset).addSeries(distanceSelectionSeries);
            ((XYSeriesCollection)distanceDataset).addSeries(distanceDataSeries);

            final JFreeChart chart1 = ChartFactory.createXYLineChart
            (name + " vs. Distance", "Distance (km)", name + " (" + units + ")",
                    distanceDataset, PlotOrientation.VERTICAL, false, true, false);

            // add the jfreechart graph
            ChartPanel chartPanel = new ChartPanel(chart1){
                @Override
                public void restoreAutoRangeBounds()
                {
                    super.restoreAutoRangeBounds();
                    // This makes sure when the user auto-range's the plot, it will bracket the
                    // well with a small margin
                    ((XYPlot)chart1.getPlot()).getRangeAxis().setRangeWithMargins(
                            distanceDataSeries.getMinY(), distanceDataSeries.getMaxY());
                }
            };
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

            distanceDataSeries.clear();
            if (data.size() > 0 && distance.size() > 0)
            {
                for (int i=0; i<data.size(); ++i)
                    distanceDataSeries.add(distance.get(i), data.get(i), false);
            }
            distanceDataSeries.fireSeriesChanged();

            plot.getRangeAxis().setRangeWithMargins(distanceDataSeries.getMinY(), distanceDataSeries.getMaxY());
        }

        {
            timeSelectionSeries = new XYSeries("Lidar Selection");
            timeDataSeries = new XYSeries("Lidar Data");

            timeDataset = new XYSeriesCollection();
            ((XYSeriesCollection)timeDataset).addSeries(timeSelectionSeries);
            ((XYSeriesCollection)timeDataset).addSeries(timeDataSeries);

            final JFreeChart chart2 = ChartFactory.createXYLineChart
                    (name + " vs. Time", "Time (sec)", name + " (" + units + ")",
                            timeDataset, PlotOrientation.VERTICAL, false, true, false);

            // add the jfreechart graph
            ChartPanel chartPanel = new ChartPanel(chart2){
                @Override
                public void restoreAutoRangeBounds()
                {
                    super.restoreAutoRangeBounds();
                    // This makes sure when the user auto-range's the plot, it will bracket the
                    // well with a small margin
                    ((XYPlot)chart2.getPlot()).getRangeAxis().setRangeWithMargins(
                            timeDataSeries.getMinY(), timeDataSeries.getMaxY());
                }
            };
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

            timeDataSeries.clear();
            if (data.size() > 0 && time.size() > 0)
            {
                double t0 = time.get(0);
                for (int i=0; i<data.size(); ++i)
                    timeDataSeries.add(time.get(i)-t0, data.get(i), false);
            }
            timeDataSeries.fireSeriesChanged();

            plot.getRangeAxis().setRangeWithMargins(timeDataSeries.getMinY(), timeDataSeries.getMaxY());
        }

        add(panel, BorderLayout.CENTER);

        createMenus();

        setTitle(name);
        pack();
        setVisible(true);
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem mi = new JMenuItem(new ExportDataAction());
        fileMenu.add(mi);

        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }


    public void selectPoint(int ptId)
    {
        distanceSelectionSeries.clear();
        if (ptId >= 0)
        {
            distanceSelectionSeries.add(
                    distanceDataSeries.getX(ptId),
                    distanceDataSeries.getY(ptId), true);
        }



        timeSelectionSeries.clear();
        if (ptId >= 0)
        {
            timeSelectionSeries.add(
                    timeDataSeries.getX(ptId),
                    timeDataSeries.getY(ptId), true);
        }
    }

    public void chartMouseClicked(ChartMouseEvent arg0)
    {
        ChartEntity entity = arg0.getEntity();

        if (entity instanceof XYItemEntity)
        {
            int id = ((XYItemEntity)entity).getItem();
            selectPoint(id);

            // Only select the point in the renderer if there is only one track
            // shown and the number of points in the track is the same as the number
            // points in this plot. Without these conditions, then the selected
            // point will be incorrect.
            if (lidarModel.getNumberOfTracks() == 1 &&
                    lidarModel.getTrack(0).getNumberOfPoints() == data.size())
            {
                Track tmpTrack = lidarModel.getTrack(0);
                LidarPoint tmpPoint = lidarModel.getPoint(id);
                lidarModel.setSelectedPoint(tmpPoint, tmpTrack);
            }
        }
        else
        {
            distanceSelectionSeries.clear();
            timeSelectionSeries.clear();
        }
    }

    public void chartMouseMoved(ChartMouseEvent arg0)
    {
    }

    private class ExportDataAction extends AbstractAction
    {
        public ExportDataAction()
        {
            super("Export Data...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(LidarPlot.this, "Export Data", name + ".txt");

            try
            {
                if (file != null)
                {
                    FileWriter fstream = new FileWriter(file);
                    BufferedWriter out = new BufferedWriter(fstream);

                    String newline = System.getProperty("line.separator");

                    out.write(name + " Distance Time" + newline);

                    int size = data.size();
                    for (int i=0; i<size; ++i)
                    {
                        out.write(data.get(i) + " " +
                                distance.get(i) + " " +
                                TimeUtil.et2str(time.get(i)) + newline);
                    }
                    out.close();
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(LidarPlot.this),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }

        }
    }

}
