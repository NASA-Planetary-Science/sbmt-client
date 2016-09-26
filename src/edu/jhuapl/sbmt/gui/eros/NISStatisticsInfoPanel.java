package edu.jhuapl.sbmt.gui.eros;

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
import org.jfree.ui.RectangleEdge;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.eros.NISSpectraCollection;
import edu.jhuapl.sbmt.model.eros.NISSpectrum;
import edu.jhuapl.sbmt.model.eros.NISStatistics;
import edu.jhuapl.sbmt.model.eros.NISStatistics.Sample;

public class NISStatisticsInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{

    ModelManager modelManager;
    NISStatistics stats;

    static final String statsChangedEventName="xYzzY";
    JTabbedPane tabbedPane=new JTabbedPane();


    public NISStatisticsInfoPanel(NISStatistics stats, ModelManager modelManager)
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.modelManager=modelManager;
        this.stats=stats;

        tabbedPane.add("Incidence Angle (deg)",setupHistogramPanel(stats.sampleEmergenceAngle(), "Angle (deg)", "# faces"));

        this.add(tabbedPane);
        pack();
        setVisible(true);

    }

    private JPanel setupHistogramPanel(List<Sample> samples, String xlabel, String ylabel)
    {
        HistogramDataset dataset=new HistogramDataset();

        double[] ange=NISStatistics.getValuesAsArray(samples);
        double mine=NISStatistics.getMin(samples);
        double maxe=NISStatistics.getMax(samples);

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
        data[0][1]=NISStatistics.getWeightedMean(samples);
        data[1][1]=Math.sqrt(NISStatistics.getWeightedVariance(samples));
        data[2][1]=NISStatistics.getWeightedSkewness(samples);
        data[3][1]=NISStatistics.getWeightedKurtosis(samples);
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
        controlPanel.add(restackButton);

        restackButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Map<NISSpectrum,Integer> stackingOrder=stats.orderSpectraByMeanEmergenceAngle();
                NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.SPECTRA);
                model.clearOrdinals();
                for (NISSpectrum spectrum: stackingOrder.keySet())  // only stack the ones that stats knows about
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