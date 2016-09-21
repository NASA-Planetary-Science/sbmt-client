package edu.jhuapl.sbmt.gui.eros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.model.eros.NISStatistics;
import edu.jhuapl.sbmt.model.eros.NISStatistics.Sample;

public class NISStatisticsInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{

    ModelManager modelManager;
    NISStatistics stats;

    static final String statsChangedEventName="xYzzY";

    public NISStatisticsInfoPanel(NISStatistics stats, ModelManager modelManager)
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.modelManager=modelManager;
        this.stats=stats;

//        SimpleHistogramDataset dataset=new SimpleHistogramDataset("cos(theta)");
        HistogramDataset dataset=new HistogramDataset();

        List<Sample> emergenceAngle=stats.sampleEmergenceAngle();
        double[] ange=NISStatistics.getValuesAsArray(emergenceAngle);
        double mine=NISStatistics.getMin(emergenceAngle);
        double maxe=NISStatistics.getMax(emergenceAngle);

        int nBins=Math.max(10, (int)((double)Math.ceil(stats.getNumberOfFaces())/3.));
        dataset.addSeries("Frustum-face incidence angle", ange, nBins, mine, maxe);
        //
        JFreeChart chart=ChartFactory.createHistogram("Incidence histogram", "Theta", "# Faces", dataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel=new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        XYPlot plot=(XYPlot)chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);


        Object[][] data=new Object[4][2];
        data[0][0]="Mean";
        data[1][0]="Standard Deviation";
        data[2][0]="Skewness";
        data[3][0]="Kurtosis";
        data[0][1]=NISStatistics.getWeightedMean(emergenceAngle);
        data[1][1]=Math.sqrt(NISStatistics.getWeightedVariance(emergenceAngle));
        data[2][1]=NISStatistics.getWeightedSkewness(emergenceAngle);
        data[3][1]=NISStatistics.getWeightedKurtosis(emergenceAngle);
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

        JPanel bottomPanel=new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(scrollPane);

        JPanel panel=new JPanel(new BorderLayout());
        panel.add(chartPanel,BorderLayout.CENTER);
        panel.add(bottomPanel,BorderLayout.SOUTH);
        this.add(panel);

        pack();
        setVisible(true);

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
