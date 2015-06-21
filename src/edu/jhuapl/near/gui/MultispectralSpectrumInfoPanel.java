package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.popupmenus.eros.NISPopupMenu;

public class MultispectralSpectrumInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private ModelManager modelManager;
    private PerspectiveImage perspectiveImage;

    public MultispectralSpectrumInfoPanel(PerspectiveImage perspectiveImage, ModelManager modelManager)
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.modelManager = modelManager;
        this.perspectiveImage = perspectiveImage;

        JPanel panel = new JPanel(new BorderLayout());



        int nspectra = perspectiveImage.getNumberSpectra();

        XYSeriesCollection xyDataset = new XYSeriesCollection();

        for (int spectrum = 0; spectrum<nspectra; spectrum++)
        {
            double[] wavelengths = this.perspectiveImage.getSpectrumWavelengths(spectrum);
            double[] values = this.perspectiveImage.getSpectrumValues(spectrum);

            // add the jfreechart graph
            XYSeries series = new XYSeries("Spectrum " + spectrum);

            for (int i=0; i<wavelengths.length; ++i)
                series.add(wavelengths[i], values[i]);

            xyDataset.addSeries(series);
        }

        String units = perspectiveImage.getSpectrumUnits();
        JFreeChart chart = ChartFactory.createXYLineChart("Image Spectrum", "Wavelength (" + units + ")", "Intensity", xyDataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }

        panel.add(chartPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel,
                BoxLayout.PAGE_AXIS));

        // Add a text box for showing information about the image
        String[] columnNames = {"Property",
                "Value"};

        HashMap<String, String> properties = null;
        Object[][] data = {    {"", ""} };

        try
        {

            properties = this.perspectiveImage.getProperties();
            int size = properties.size();
            data = new Object[size][2];

            int i=0;
            for (String key : properties.keySet())
            {
                data[i][0] = key;
                data[i][1] = properties.get(key);

                ++i;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }



        JTable table = new JTable(data, columnNames)
        {
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        table.setBorder(BorderFactory.createTitledBorder(""));
        table.setPreferredScrollableViewportSize(new Dimension(500, 130));

        JScrollPane scrollPane = new JScrollPane(table);

        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(scrollPane);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        createMenus();

        // Finally make the frame visible
        setTitle("Spectrum Properties");

        pack();
        setVisible(true);
    }


    public Model getModel()
    {
        return perspectiveImage;
    }

    public Model getCollectionModel()
    {
        return modelManager.getModel(ModelNames.IMAGES);
    }


    /**
     * The following function is a bit of a hack. We want to reuse the MSIPopupMenu
     * class, but instead of having a right-click popup menu, we want instead to use
     * it as an actual menu in a menu bar. Therefore we simply grab the menu items
     * from that class and put these in our new JMenu.
     */
    private void createMenus()
    {
        NISPopupMenu msiImagesPopupMenu =
        new NISPopupMenu(modelManager, null);

//        msiImagesPopupMenu.setCurrentSpectrum(perspectiveImage.getServerPath());
//        msiImagesPopupMenu.setCurrentSpectrum("<file path>");

        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = msiImagesPopupMenu.getComponents();
        for (Component item : components)
        {
            if (item instanceof JMenuItem)
                menu.add(item);
        }

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
    }
}
