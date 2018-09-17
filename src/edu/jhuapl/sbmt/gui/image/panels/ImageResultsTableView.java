package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.sbmt.gui.image.ImagePopupMenu;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

public class ImageResultsTableView extends JPanel
{
    private JButton loadImageListButton;
    private JPanel monochromePanel;
    private JButton nextButton;
    private JComboBox<Integer> numberOfBoundariesComboBox;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllImagesButton;
    private JButton saveImageListButton;
    private JButton saveSelectedImageListButton;
    private JButton viewResultsGalleryButton;
    private ImagePopupMenu imagePopupMenu;
    private boolean enableGallery;
    String[] columnNames;


    private ImageResultsTable resultList;
    private JLabel resultsLabel;
    private JTable table;

    /**
     * @wbp.parser.constructor
     */
    public ImageResultsTableView(ImagingInstrument instrument, ImageCollection imageCollection, ImagePopupMenu imagePopupMenu)
    {
        this.imagePopupMenu = imagePopupMenu;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        JLabel lblNewLabel = new JLabel("0 Results");
        panel_4.add(lblNewLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        table = new JTable();
        scrollPane.setViewportView(table);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel lblNumberBoundaries = new JLabel("Number Boundaries:");
        panel.add(lblNumberBoundaries);

        numberOfBoundariesComboBox = new JComboBox();
        panel.add(numberOfBoundariesComboBox);

        prevButton = new JButton("Prev");
        panel.add(prevButton);

        nextButton = new JButton("Next");
        panel.add(nextButton);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        removeAllImagesButton = new JButton("Remove All Images");
        panel_1.add(removeAllImagesButton);

        removeAllButton = new JButton("Remove All Boundaries");
        panel_1.add(removeAllButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        loadImageListButton = new JButton("Load Image List...");
        panel_2.add(loadImageListButton);

        saveImageListButton = new JButton("Save Image List...");
        panel_2.add(saveImageListButton);

        saveSelectedImageListButton = new JButton("Save Selected Images List...");
        panel_2.add(saveSelectedImageListButton);

        JPanel panel_3 = new JPanel();
        add(panel_3);

        viewResultsGalleryButton = new JButton("View Search Results as Image Gallery");
        panel_3.add(viewResultsGalleryButton);
        resultList = new ImageResultsTable();
        resultsLabel = new JLabel("0 images matched");
    }

    public ImageResultsTableView(LayoutManager layout)
    {
        super(layout);
    }

    public ImageResultsTableView(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
    }

    public ImageResultsTableView(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
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

    public JButton getLoadImageListButton()
    {
        return loadImageListButton;
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

    public JButton getRemoveAllImagesButton()
    {
        return removeAllImagesButton;
    }

    public JButton getSaveImageListButton()
    {
        return saveImageListButton;
    }

    public JButton getSaveSelectedImageListButton()
    {
        return saveSelectedImageListButton;
    }

    public JButton getViewResultsGalleryButton()
    {
        return viewResultsGalleryButton;
    }

    public ImagePopupMenu getImagePopupMenu()
    {
        return imagePopupMenu;
    }

    public boolean isEnableGallery()
    {
        return enableGallery;
    }

    public void setEnableGallery(boolean enableGallery)
    {
        this.enableGallery = enableGallery;
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

}
