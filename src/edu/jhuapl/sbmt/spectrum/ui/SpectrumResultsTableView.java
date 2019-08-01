package edu.jhuapl.sbmt.spectrum.ui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.spectrum.model.rendering.SpectraCollection;

public class SpectrumResultsTableView extends JPanel
{
    private JButton loadSpectraListButton;
    private JPanel monochromePanel;
    private JButton nextButton;
    private JComboBox<Integer> numberOfBoundariesComboBox;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllSpectraButton;
    private JButton saveSpectraListButton;
    private JButton saveSelectedSpectraListButton;
    private SpectrumPopupMenu spectrumPopupMenu;
    protected SpectrumResultsTable resultList;
    private JLabel resultsLabel;
    private JLabel lblNumberBoundaries;

    /**
     * @wbp.parser.constructor
     */
    public SpectrumResultsTableView(SpectraCollection spectrumCollection, SpectrumPopupMenu spectrumPopupMenu)
    {
        this.spectrumPopupMenu = spectrumPopupMenu;
        init();
    }

    protected void init()
    {
        resultsLabel = new JLabel("0 Results");
        resultList = new SpectrumResultsTable();
        lblNumberBoundaries = new JLabel("Number Boundaries:");
        numberOfBoundariesComboBox = new JComboBox<Integer>();
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        removeAllSpectraButton = new JButton("Remove All Images");
        removeAllButton = new JButton("Remove All Boundaries");
        loadSpectraListButton = new JButton("Load...");
        saveSpectraListButton = new JButton("Save...");
        saveSelectedSpectraListButton = new JButton("Save Selected...");
    }

    public void setup()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(null, "Available Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        panel_4.add(resultsLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(150, 150));
        add(scrollPane);

        scrollPane.setViewportView(resultList);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(lblNumberBoundaries);

        panel.add(numberOfBoundariesComboBox);

        panel.add(prevButton);

        panel.add(nextButton);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        panel_1.add(removeAllSpectraButton);

        panel_1.add(removeAllButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        panel_2.add(loadSpectraListButton);

        panel_2.add(saveSpectraListButton);

        panel_2.add(saveSelectedSpectraListButton);
    }

    public JTable getResultList()
    {
        return resultList;
    }

    public JLabel getResultsLabel()
    {
        return resultsLabel;
    }

    public JComboBox<Integer> getNumberOfBoundariesComboBox()
    {
        return numberOfBoundariesComboBox;
    }

    public JButton getLoadSpectraListButton()
    {
        return loadSpectraListButton;
    }

    public JPanel getMonochromePanel()
    {
        return monochromePanel;
    }

    public JButton getNextButton()
    {
        return nextButton;
    }

    public JButton getPrevButton()
    {
        return prevButton;
    }

    public JButton getRemoveAllButton()
    {
        return removeAllButton;
    }

    public JButton getRemoveAllSpectraButton()
    {
        return removeAllSpectraButton;
    }

    public JButton getSaveSpectraListButton()
    {
        return saveSpectraListButton;
    }

    public JButton getSaveSelectedSpectraListButton()
    {
        return saveSelectedSpectraListButton;
    }

    public void setNumberOfBoundariesComboBox(JComboBox<Integer> numberOfBoundariesComboBox)
    {
        this.numberOfBoundariesComboBox = numberOfBoundariesComboBox;
    }

    public void setResultsLabel(JLabel resultsLabel)
    {
        this.resultsLabel = resultsLabel;
    }

    public int getMapColumnIndex()
    {
        return resultList.mapColumnIndex;
    }

    public int getShowFootprintColumnIndex()
    {
        return resultList.showFootprintColumnIndex;
    }

    public int getFrusColumnIndex()
    {
        return resultList.frusColumnIndex;
    }

    public int getBndrColumnIndex()
    {
        return resultList.bndrColumnIndex;
    }

    public int getDateColumnIndex()
    {
        return resultList.dateColumnIndex;
    }

    public int getIdColumnIndex()
    {
        return resultList.idColumnIndex;
    }

    public int getFilenameColumnIndex()
    {
        return resultList.filenameColumnIndex;
    }

    public SpectrumPopupMenu getSpectrumPopupMenu()
    {
        return spectrumPopupMenu;
    }

    public void setSpectrumPopupMenu(SpectrumPopupMenu spectrumPopupMenu)
    {
        this.spectrumPopupMenu = spectrumPopupMenu;
    }

}
