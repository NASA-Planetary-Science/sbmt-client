package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

import edu.jhuapl.near.model.eros.NISStatistics;
import edu.jhuapl.near.util.MomentCalculator;
import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;

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
        double[] th=stats.getTheta();
        double minth=stats.getMinTheta();
        double maxth=stats.getMaxTheta();
        int nBins=Math.max(10, (int)((double)Math.ceil(stats.getNumberOfFaces())/3.));
        dataset.addSeries("Frustum-face incidence angle",th, nBins, minth, maxth);
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
        MomentCalculator calculator=new MomentCalculator(th);
        data[0][1]=calculator.getMean();
        data[1][1]=Math.sqrt(calculator.getVariance());
        data[2][1]=calculator.getSkewness();
        data[3][1]=calculator.getKurtosis();
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
