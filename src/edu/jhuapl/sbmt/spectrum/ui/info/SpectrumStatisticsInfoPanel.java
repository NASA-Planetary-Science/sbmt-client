package edu.jhuapl.sbmt.spectrum.ui.info;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics.Sample;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;

/**
 * Info panel for displayed spectrum statistics based on the spectrum's footprint on the surface.
 * @author steelrj1
 *
 */
public class SpectrumStatisticsInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{

    ModelManager modelManager;
    SpectrumStatistics stats;

    static final String statsChangedEventName="xYzzY";
    JTabbedPane tabbedPane=new JTabbedPane();


    public SpectrumStatisticsInfoPanel(SpectrumStatistics stats, ModelManager modelManager)
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.modelManager=modelManager;
        this.stats=stats;

        tabbedPane.add("Incidence Angle (deg)",setupHistogramPanel(stats.getIncidenceAngleSamples(), "Angle (deg)", "# faces"));
        tabbedPane.add("Emergence Angle (deg)",setupHistogramPanel(stats.getEmergenceAngleSamples(), "Angle (deg)", "# faces"));

        List<Sample> irradianceSamples=stats.getIrradianceSamples();
        List<Sample> phaseAngleSamples=stats.getPhaseAngleSamples();
        List<Sample> phaseAngleSamplesNoZeroIrrad=Lists.newArrayList();
        List<Sample> irradianceSamplesNoZeros=Lists.newArrayList();
        for (int i=0; i<irradianceSamples.size(); i++)
            if (irradianceSamples.get(i).value>0)
            {
                phaseAngleSamplesNoZeroIrrad.add(phaseAngleSamples.get(i));
                irradianceSamplesNoZeros.add(irradianceSamples.get(i));
            }

        tabbedPane.add("Relative Irradiance (No Zeros)",setupHistogramPanel(irradianceSamplesNoZeros, "Irradiance Level (dimensionless)", "# faces"));
        tabbedPane.add("Phase Angle (deg)",setupHistogramPanel(phaseAngleSamples, "Angle (deg)", "# faces"));
        tabbedPane.add("Reconstructed BRDF", setupBdrfPanel(phaseAngleSamplesNoZeroIrrad, irradianceSamplesNoZeros));


        this.add(tabbedPane);
        pack();
        setVisible(true);

    }

    /**
     * Sets up histogram panel to show statistics
     * @param samples
     * @param xlabel
     * @param ylabel
     * @return
     */
    private JPanel setupHistogramPanel(List<Sample> samples, String xlabel, String ylabel)
    {
        HistogramDataset dataset=new HistogramDataset();

        double[] ange=SpectrumStatistics.getValuesAsArray(samples);

        double mine=SpectrumStatistics.getMin(samples);
        double maxe=SpectrumStatistics.getMax(samples);

        int nBins=Math.max(10, (int)((double)Math.ceil(stats.getNumberOfFaces())/3.));
        dataset.addSeries(String.valueOf(samples.hashCode()), ange, nBins, mine, maxe);     // just pass in garbage value for key (first argument)
        //
        JFreeChart chart=ChartFactory.createHistogram(null, xlabel, ylabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel=new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        XYPlot plot=(XYPlot)chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        plot.getRenderer().setSeriesPaint(0, Color.BLACK);  // change default bar color to black
        StandardXYBarPainter painter=new StandardXYBarPainter() // disable gradient rendering (by instantiating a standard painter) and disable shadow rendering (by overriding the method below)
        {
            @Override
            public void paintBarShadow(Graphics2D arg0,
                    XYBarRenderer arg1, int arg2, int arg3,
                    RectangularShape arg4, RectangleEdge arg5,
                    boolean arg6)
            {
            }
        };
        ((XYBarRenderer)plot.getRenderer()).setBarPainter(painter);

        Object[][] data=new Object[4][2];
        data[0][0]="Mean";
        data[1][0]="Standard Deviation";
        data[2][0]="Skewness";
        data[3][0]="Kurtosis";
        data[0][1]=SpectrumStatistics.getWeightedMean(samples);
        data[1][1]=Math.sqrt(SpectrumStatistics.getWeightedVariance(samples));
        data[2][1]=SpectrumStatistics.getWeightedSkewness(samples);
        data[3][1]=SpectrumStatistics.getWeightedKurtosis(samples);
        String[] columns=new String[]{"Property","Value"};

        JTable table=new JTable(data, columns)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        table.setBorder(BorderFactory.createTitledBorder(""));
        table.setPreferredScrollableViewportSize(new Dimension(500,130));
        JScrollPane scrollPane=new JScrollPane(table);

        JPanel momentsPanel=new JPanel();
        momentsPanel.setLayout(new BoxLayout(momentsPanel, BoxLayout.PAGE_AXIS));
        momentsPanel.add(Box.createVerticalStrut(10));
        momentsPanel.add(scrollPane);

        JPanel controlPanel=new JPanel();
        JButton restackButton=new JButton("Stack by <e>");
       // controlPanel.add(restackButton);

        restackButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Map<IBasicSpectrumRenderer,Integer> stackingOrder = stats.orderSpectraByMeanEmergenceAngle();
                SpectraCollection model = (SpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.clearOrdinals();
                for (IBasicSpectrumRenderer spectrum: stackingOrder.keySet())  // only stack the ones that stats knows about
                {
                    model.setOrdinal(spectrum, stackingOrder.get(spectrum));
//                    System.out.println(stackingOrder.get(spectrum));
                }
                model.reshiftFootprints();
            }
        });

        JPanel panel=new JPanel(new BorderLayout());
        panel.add(chartPanel,BorderLayout.CENTER);
        panel.add(momentsPanel,BorderLayout.EAST);
        panel.add(controlPanel,BorderLayout.SOUTH);

        return panel;
    }

    private JPanel setupBdrfPanel(List<Sample> phaseAngles, List<Sample> irradiance)
    {
        XYSeriesCollection dataset=new XYSeriesCollection();
        XYSeries series=new XYSeries("Reconstructed BRDF");
        for (int i=0; i<phaseAngles.size(); i++)
        {
            double alpha=phaseAngles.get(i).value;
            double irrad=irradiance.get(i).value;
            series.add(alpha,irrad);
        }
        dataset.addSeries(series);

        JFreeChart chart=ChartFactory.createScatterPlot("BRDF", "Phase angle", "Irradiance", dataset, PlotOrientation.VERTICAL, false, false, false);
        ChartPanel chartPanel=new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        XYPlot plot=(XYPlot)chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);
        plot.getRenderer().setSeriesShape(0, ShapeUtilities.createDiagonalCross(0.5f, 0.5f));
        return chartPanel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(statsChangedEventName))
        {
        }
    }

    @Override
    public Model getModel()
    {
        return stats;
    }

    @Override
    public Model getCollectionModel()
    {
        return modelManager;
    }

}