package edu.jhuapl.sbmt.gui.image.controllers.images;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import vtk.vtkActor;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchResultsListener;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.images.ImagePopupMenu;
import edu.jhuapl.sbmt.gui.image.ui.images.ImageResultsTableView;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundary;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.util.ImageGalleryGenerator;
import edu.jhuapl.sbmt.util.ImageGalleryGenerator.ImageGalleryEntry;

public class ImageResultsTableController
{

    protected ImageResultsTableView imageResultsTableView;
    protected ImageSearchModel imageSearchModel;
    protected List<ImageKeyInterface> imageKeys;
    protected List<List<String>> imageRawResults;
    private ModelManager modelManager;
    protected ImagingInstrument instrument;
    protected Renderer renderer;
    protected StringRenderer stringRenderer;
    protected PropertyChangeListener propertyChangeListener;
    protected TableModelListener tableModelListener;
    protected ImageCollection imageCollection;
    protected PerspectiveImageBoundaryCollection boundaries;
    protected ImagePopupMenu imagePopupMenu;
    protected DefaultTableModel tableModel;
    protected String[] columnNames = new String[] {
            "Map",
            "Show",
            "Frus",
            "Bndr",
            "Id",
            "Filename",
            "Date"
    };

    int modifiedTableRow = -1;

    public ImageResultsTableController(ImagingInstrument instrument, ImageCollection imageCollection, ImageSearchModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager, SbmtSpectrumWindowManager spectrumPanelManager)
    {
        this.modelManager = model.getModelManager();
        this.imageKeys = new ArrayList<ImageKeyInterface>();
        boundaries = (PerspectiveImageBoundaryCollection) modelManager.getModel(model.getImageBoundaryCollectionModelName());
        imagePopupMenu = new ImagePopupMenu(modelManager, imageCollection, boundaries, infoPanelManager, spectrumPanelManager, renderer, imageResultsTableView);
        imageResultsTableView = new ImageResultsTableView(instrument, imageCollection, imagePopupMenu);
        imageResultsTableView.setup();
        //        imageResultsTableView.getResultList().setUI(new DragDropRowTableUI());
        imageRawResults = model.getImageResults();
        this.imageCollection = imageCollection;
        this.imageSearchModel = model;
        this.instrument = instrument;
        this.renderer = renderer;
        model.addResultsChangedListener(new ImageSearchResultsListener() {

            @Override
            public void resultsChanged(List<List<String>> results)
            {
                setImageResults(results);
            }

            @Override
            public void resultsCountChanged(int count)
            {
                imageResultsTableView.getResultsLabel().setText(count + " images found");
            }
        });

        propertyChangeListener = new ImageResultsPropertyChangeListener();
        tableModelListener = new ImageResultsTableModeListener();

        this.imageResultsTableView.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e)
            {
                imageCollection.addPropertyChangeListener(propertyChangeListener);
                boundaries.addPropertyChangeListener(propertyChangeListener);
            }

            @Override
            public void componentHidden(ComponentEvent e)
            {
                imageCollection.removePropertyChangeListener(propertyChangeListener);
                boundaries.removePropertyChangeListener(propertyChangeListener);
            }
        });

    }

    public void setImageResultsPanel()
    {
        setupWidgets();
        setupTable();
    }

    protected void setupWidgets()
    {
        if (instrument != null)
            imageResultsTableView.setEnableGallery(instrument.searchQuery.getGalleryPath() != null);

        // setup Image Results Table view components
        imageResultsTableView.getNumberOfBoundariesComboBox().setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200",
                "210", "220", "230", "240", "250", " " }));
        imageResultsTableView.getNumberOfBoundariesComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                numberOfBoundariesComboBoxActionPerformed(evt);
            }
        });

        imageResultsTableView.getPrevButton().setText("<");
        imageResultsTableView.getPrevButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                prevButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getNextButton().setText(">");
        imageResultsTableView.getNextButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                nextButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getRemoveAllButton().setText("Remove All Boundaries");
        imageResultsTableView.getRemoveAllButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeAllButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getRemoveAllImagesButton().setText("Remove All Images");
        imageResultsTableView.getRemoveAllImagesButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeAllImagesButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getSaveImageListButton().setText("Save List...");
        imageResultsTableView.getSaveImageListButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveImageListButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getSaveSelectedImageListButton().setText("Save Selected List...");
        imageResultsTableView.getSaveSelectedImageListButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveSelectedImageListButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getLoadImageListButton().setText("Load List...");
        imageResultsTableView.getLoadImageListButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                loadImageListButtonActionPerformed(evt);
            }
        });

        imageResultsTableView.getViewResultsGalleryButton().setText("View Search Results as Image Gallery");
        imageResultsTableView.getViewResultsGalleryButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                viewResultsGalleryButtonActionPerformed(evt);
            }
        });
    }

    protected void setupTable()
    {

        tableModel = new ImagesTableModel(new Object[0][7], columnNames);

        imageResultsTableView.getResultList().setModel(tableModel);
        imageResultsTableView.getResultList().getTableHeader().setReorderingAllowed(false);
        imageResultsTableView.getResultList().getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);

        imageResultsTableView.getResultList().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            {
                resultsListMaybeShowPopup(e);
                imageResultsTableView.getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }

            public void mouseReleased(MouseEvent e)
            {
                resultsListMaybeShowPopup(e);
                imageResultsTableView.getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }
        });

        imageResultsTableView.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    imageSearchModel.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
                    imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
                }
            }
        });

        stringRenderer = new StringRenderer(imageSearchModel, imageRawResults);
        imageResultsTableView.getResultList().setDefaultRenderer(String.class, stringRenderer);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setPreferredWidth(35);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setResizable(true);
        imageResultsTableView.getViewResultsGalleryButton().setVisible(true);

        imageResultsTableView.getResultList().getRowSorter().addRowSorterListener(new RowSorterListener() {

            @Override
            public void sorterChanged(RowSorterEvent e)
            {
                imageResultsTableView.repaint();
                imageResultsTableView.getResultList().repaint();
                stringRenderer.updateUI();
            }
        });
    }

    protected JTable getResultList()
    {
        return imageResultsTableView.getResultList();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent) evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof ImageCollection
                    || model instanceof PerspectiveImageBoundaryCollection)
            {
                String name = null;

                if (model instanceof ImageCollection)
                    name = ((ImageCollection) model).getImageName((vtkActor) e.getPickedProp());
                else if (model instanceof PerspectiveImageBoundaryCollection)
                    name = ((PerspectiveImageBoundaryCollection) model).getBoundaryName((vtkActor) e.getPickedProp());

                int idx = -1;
                int size = imageRawResults.size();
                for (int i = 0; i < size; ++i)
                {
                    // Ignore extension (The name returned from getImageName or
                    // getBoundary
                    // is the same as the first element of each list with the
                    // imageRawResults
                    // but without the extension).
                    String imagePath = imageRawResults.get(i).get(0);
                    imagePath = imagePath.substring(0, imagePath.lastIndexOf("."));
                    if (name.equals(imagePath))
                    {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0)
                {
                    imageResultsTableView.getResultList().setRowSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageResultsTableView.getResultList().getCellRect(idx, 0, true);
                    if (cellBounds != null)
                        imageResultsTableView.getResultList().scrollRectToVisible(cellBounds);
                }
            }
        }
    }

    private void viewResultsGalleryButtonActionPerformed(ActionEvent evt)
    {
        // Check if image search results are valid and nonempty
        if (imageRawResults != null)
        {
            String dataPath = instrument.searchQuery.getDataPath();
            String galleryPath = instrument.searchQuery.getGalleryPath();
            // Create list of gallery and preview image names based on results
            List<ImageGalleryEntry> galleryEntries = new LinkedList<ImageGalleryEntry>();
            for (List<String> res : imageRawResults)
            {
                String s = "/" + res.get(0).replace(dataPath, galleryPath);
                // Create entry for image gallery
                galleryEntries.add(new ImageGalleryEntry(res.get(0).substring(res.get(0).lastIndexOf("/") + 1), s + ".jpeg", s + "-small.jpeg"));
            }

            // Don't bother creating a gallery if empty
            if (galleryEntries.isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView), "Unable to generate gallery.  Gallery images corresponding to search results are not registered.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create preview gallery based on search results
            String galleryURL = ImageGalleryGenerator.generateGallery(galleryEntries);

            // Show gallery preview in browser
            try
            {
                java.awt.Desktop.getDesktop().browse(new File(galleryURL).toURI());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void numberOfBoundariesComboBoxActionPerformed(java.awt.event.ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = imageSearchModel.getResultIntervalCurrentlyShown();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only update if there's been a change in what is selected
            int newMaxId = resultIntervalCurrentlyShown.id1 + Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem());
            if (newMaxId != resultIntervalCurrentlyShown.id2)
            {
                resultIntervalCurrentlyShown.id2 = newMaxId;
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
            imageSearchModel.setNumBoundaries(Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
        }
    }

    protected void saveImageListButtonActionPerformed(ActionEvent evt)
    {
        File file = CustomFileChooser.showSaveDialog(imageResultsTableView, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing" + nl);
                int size = imageRawResults.size();
                for (int i = 0; i < size; ++i)
                {
                	int actualRow = imageResultsTableView.getResultList().getRowSorter().convertRowIndexToView(i);

                    String image = new File(imageRawResults.get(actualRow).get(0)).getName();
                    String dtStr = imageRawResults.get(actualRow).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + sdf.format(dt) + " " + imageSearchModel.getImageSourceOfLastQuery().toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView), "There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    protected void loadImageListButtonActionPerformed(ActionEvent evt)
    {
        File file = CustomFileChooser.showOpenDialog(imageResultsTableView, "Select File");

        if (file != null)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                List<List<String>> results = new ArrayList<List<String>>();
                List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
                for (int i = 0; i < lines.size(); ++i)
                {
                    if (lines.get(i).startsWith("#"))
                        continue;
                    String[] words = lines.get(i).trim().split("\\s+");
                    List<String> result = new ArrayList<String>();
                    String name = instrument.searchQuery.getDataPath() + "/" + words[0];
                    result.add(name);
                    Date dt = sdf.parse(words[1]);
                    result.add(String.valueOf(dt.getTime()));
                    results.add(result);
                }

                //TODO needed?
                //                imageSearchModel.setImageSourceOfLastQuery(ImageSource.valueOf(((Enum)sourceComboBox.getSelectedItem()).name()));
                imageSearchModel.setImageResults(new ArrayList<List<String>>());
                setImageResults(imageSearchModel.processResults(results));
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView), "There was an error reading the file.", "Error", JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }

    }

    protected void saveSelectedImageListButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        File file = CustomFileChooser.showSaveDialog(imageResultsTableView, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing" + nl);
                int[] selectedIndices = imageResultsTableView.getResultList().getSelectedRows();
                for (int selectedIndex : selectedIndices)
                {
                	int actualRow = imageResultsTableView.getResultList().getRowSorter().convertRowIndexToView(selectedIndex);
                    String image = new File(imageRawResults.get(actualRow).get(0)).getName();
                    String dtStr = imageRawResults.get(actualRow).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + sdf.format(dt) + " " + imageSearchModel.getImageSourceOfLastQuery().toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView), "There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    private void removeAllButtonActionPerformed(ActionEvent evt)
    {
        IdPair originalInterval = imageSearchModel.getResultIntervalCurrentlyShown();
        imageSearchModel.setResultIntervalCurrentlyShown(new IdPair(0, imageRawResults.size()));
        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection) modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName());
        for (ImageKeyInterface key : imageKeys)
        {
            boundaries.removeBoundary(key);
        }
        imageSearchModel.setResultIntervalCurrentlyShown(null);
    }

    private void removeAllImagesButtonActionPerformed(ActionEvent evt)
    {
        for (ImageKeyInterface key : imageKeys)
        {
            imageCollection.removeImage(key);
        }
        IdPair originalInterval = imageSearchModel.getResultIntervalCurrentlyShown();
        //    	imageSearchModel.setResultIntervalCurrentlyShown(new IdPair(0, imageRawResults.size()));
        //        imageCollection.removeImages(ImageSource.GASKELL);
        //        imageCollection.removeImages(ImageSource.GASKELL_UPDATED);
        //        imageCollection.removeImages(ImageSource.SPICE);
        //        imageCollection.removeImages(ImageSource.CORRECTED_SPICE);
        //        imageCollection.removeImages(ImageSource.CORRECTED);
        //        imageCollection.removeImages(ImageSource.LOCAL_CYLINDRICAL);
        //        imageCollection.removeImages(ImageSource.LOCAL_PERSPECTIVE);
        if (originalInterval != null)
            showImageBoundaries(originalInterval);

    }

    protected void prevButtonActionPerformed(ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = imageSearchModel.getResultIntervalCurrentlyShown();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }

    }

    protected void nextButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = imageSearchModel.getResultIntervalCurrentlyShown();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < imageResultsTableView.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
            showImageBoundaries(resultIntervalCurrentlyShown);
        }
    }

    protected void removeTableListeners()
    {
        TableModelListener[] tableModelListeners = ((DefaultTableModel) imageResultsTableView.getResultList().getModel()).getTableModelListeners();
        for (TableModelListener listener : tableModelListeners)
            ((DefaultTableModel) imageResultsTableView.getResultList().getModel()).removeTableModelListener(listener);
    }

    protected void addTableListeners()
    {
        ((DefaultTableModel) imageResultsTableView.getResultList().getModel()).addTableModelListener(tableModelListener);
    }

    public void setImageResults(List<List<String>> results)
    {
        removeTableListeners();
        imageCollection.removePropertyChangeListener(propertyChangeListener);
        boundaries.removePropertyChangeListener(propertyChangeListener);

        try
        {
            //clear out the old images and boundaries from the image and boundary collection
            for (ImageKeyInterface key : imageKeys)
            {
                imageCollection.removeImage(key);
                boundaries.removeBoundary(key);
            }
            imageKeys.clear();
            stringRenderer.setImageRawResults(results);
            JTable resultTable = imageResultsTableView.getResultList();
            DefaultTableModel tableModel = (DefaultTableModel) resultTable.getModel();
            tableModel.setRowCount(0);
            imageResultsTableView.getResultsLabel().setText(results.size() + " images matched");
            imageRawResults = results;
            stringRenderer.setImageRawResults(imageRawResults);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            //            imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);

            //            removeAllButtonActionPerformed(null);
            //            removeAllImagesButtonActionPerformed(null);

            int mapColumnIndex = imageResultsTableView.getMapColumnIndex();
            int showFootprintColumnIndex = imageResultsTableView.getShowFootprintColumnIndex();
            int frusColumnIndex = imageResultsTableView.getFrusColumnIndex();
            int idColumnIndex = imageResultsTableView.getIdColumnIndex();
            int filenameColumnIndex = imageResultsTableView.getFilenameColumnIndex();
            int dateColumnIndex = imageResultsTableView.getDateColumnIndex();
            int bndrColumnIndex = imageResultsTableView.getBndrColumnIndex();
            int[] widths = new int[resultTable.getColumnCount()];
            int[] columnsNeedingARenderer = new int[] { idColumnIndex, filenameColumnIndex, dateColumnIndex };

            // add the results to the list
            tableModel.setRowCount(results.size());
            resultTable.setAutoCreateRowSorter(true);
            int i = 0;
            for (List<String> str : results)
            {
                Date dt = new Date(Long.parseLong(str.get(1)));

                String name = imageRawResults.get(i).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), imageSearchModel.getImageSourceOfLastQuery(), instrument);
                imageKeys.add(key);
                if (imageCollection.containsImage(key))
                {
                    tableModel.setValueAt(true, i, mapColumnIndex);
                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                    image.setShowFrustum(false); //on initial load, don't show the frustum
                    tableModel.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
                    tableModel.setValueAt(image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    tableModel.setValueAt(false, i, mapColumnIndex);
                    tableModel.setValueAt(false, i, showFootprintColumnIndex);
                    tableModel.setValueAt(false, i, frusColumnIndex);
                }

                if (boundaries.containsBoundary(key))
                    tableModel.setValueAt(true, i, bndrColumnIndex);
                else
                    tableModel.setValueAt(false, i, bndrColumnIndex);

                tableModel.setValueAt(i + 1, i, idColumnIndex);
                tableModel.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
                tableModel.setValueAt(sdf.format(dt), i, dateColumnIndex);

                for (int j : columnsNeedingARenderer)
                {
                    TableCellRenderer renderer = resultTable.getCellRenderer(i, j);
                    Component comp = resultTable.prepareRenderer(renderer, i, j);
                    widths[j] = Math.max(comp.getPreferredSize().width, widths[j]);
                }

                ++i;
            }

            for (int j : columnsNeedingARenderer)
                imageResultsTableView.getResultList().getColumnModel().getColumn(j).setPreferredWidth(widths[j] + 5);

            boolean enablePostSearchButtons = resultTable.getModel().getRowCount() > 0;
            imageResultsTableView.getSaveImageListButton().setEnabled(enablePostSearchButtons);
            imageResultsTableView.getSaveSelectedImageListButton().setEnabled(resultTable.getSelectedRowCount() > 0);
            imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && enablePostSearchButtons);
        }
        finally
        {
            imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
            imageCollection.addPropertyChangeListener(propertyChangeListener);
            boundaries.addPropertyChangeListener(propertyChangeListener);
        }

        // Show the first set of boundaries
        imageSearchModel.setResultIntervalCurrentlyShown(new IdPair(0, Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem())));
        //        if (boundaries.getProps().size() > 0)
        this.showImageBoundaries(imageSearchModel.getResultIntervalCurrentlyShown());

        // Enable or disable the image gallery button
        imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && !results.isEmpty());
        modifiedTableRow = -1;
    }

    protected void showImageBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        //        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName());
        //        model.removeAllBoundaries();
        //        boundaries.removeAllBoundaries();

        for (int i = startId; i < endId; ++i)
        {
            if (i < 0)
                continue;
            else if (i >= imageRawResults.size())
                break;

            try
            {
                String currentImage = imageRawResults.get(i).get(0);
                String boundaryName = FileUtil.removeExtension(currentImage);
                ImageKeyInterface key = imageSearchModel.createImageKey(boundaryName, imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
                boundaries.addBoundary(key);
            }
            catch (Exception e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView), "There was an error mapping the boundary.", "Error", JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
                break;
            }
        }
        imageSearchModel.setResultIntervalCurrentlyShown(idPair);
    }

    public class ImagesTableModel extends DefaultTableModel
    {
        public ImagesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == imageResultsTableView.getShowFootprintColumnIndex() || column == imageResultsTableView.getFrusColumnIndex())
            {
                String name = imageRawResults.get(row).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
                ImageCollection imageCollection = (ImageCollection) modelManager.getModel(imageSearchModel.getImageCollectionModelName());
                return imageCollection.containsImage(key);
            }
            else
            {
                return column == imageResultsTableView.getMapColumnIndex() || column == imageResultsTableView.getBndrColumnIndex();
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= imageResultsTableView.getBndrColumnIndex())
                return Boolean.class;
            else if (columnIndex == imageResultsTableView.getIdColumnIndex())
                return Integer.class;
            else
                return String.class;
        }

    }

    protected void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable resultList = imageResultsTableView.getResultList();
            int tableRow = resultList.rowAtPoint(e.getPoint());
            int row =  imageResultsTableView.getResultList().getRowSorter().convertRowIndexToModel(tableRow);
            int index = (Integer) imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getIdColumnIndex()) - 1;

            if (index >= 0)
            {
                List<List<String>> imageRawResults = imageSearchModel.getImageResults();
                ImageSource sourceOfLastQuery = imageSearchModel.getImageSourceOfLastQuery();
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!resultList.isRowSelected(index))
                {
                    resultList.clearSelection();
                    resultList.setRowSelectionInterval(index, index);
                }

                int[] selectedIndices = resultList.getSelectedRows();
                List<ImageKeyInterface> imageKeys = new ArrayList<ImageKeyInterface>();
                for (int selectedIndex : selectedIndices)
                {
                    String name = imageRawResults.get(imageResultsTableView.getResultList().getRowSorter().convertRowIndexToModel(selectedIndex)).get(0);
                    ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), sourceOfLastQuery, imageSearchModel.getInstrument());
                    imageKeys.add(key);
                }
                imageResultsTableView.getImagePopupMenu().setCurrentImages(imageKeys);
                imageResultsTableView.getImagePopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public ImageResultsTableView getPanel()
    {
        return imageResultsTableView;
    }

    class ImageResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public final void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = imageResultsTableView.getResultList();
                DefaultTableModel tableModel = (DefaultTableModel) resultList.getModel();
                imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
//                int size = imageRawResults.size();
                if (evt.getNewValue() != null && evt.getNewValue() instanceof PerspectiveImage)
                {
	                PerspectiveImage image = ((PerspectiveImage)evt.getNewValue());
	                String name = image.getImageName();
	                ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
	                int i = imageKeys.indexOf(key);
	                if (i >= 0)
	                	updateTableRow(tableModel, i, key);
                }
                else if (evt.getNewValue() != null && evt.getNewValue() instanceof PerspectiveImageBoundary)
                {
                	PerspectiveImageBoundary boundary = ((PerspectiveImageBoundary)evt.getNewValue());
                	ImageKeyInterface key = boundary.getKey();
                	int i = imageKeys.indexOf(key);
                	if (i >= 0)
                		tableModel.setValueAt(boundaries.containsBoundary(key), i, imageResultsTableView.getBndrColumnIndex());
                }
//                int startIndex = 0;
//                int endIndex = Math.min(10, size);
//
//                if (imageSearchModel.getResultIntervalCurrentlyShown() != null)
//                {
//                    startIndex = imageSearchModel.getResultIntervalCurrentlyShown().id1;
//                    endIndex = Math.min(size, imageSearchModel.getResultIntervalCurrentlyShown().id2);
//                }
//
//                if (modifiedTableRow > size)
//                    modifiedTableRow = -1;
//                if (modifiedTableRow != -1)
//                {
//                    startIndex = modifiedTableRow;
//                    endIndex = startIndex + 1;
//                }
//                if (size > 0)
//                {
//                    for (int i = startIndex; i < endIndex; ++i)
//                    {
//                        String name = imageRawResults.get(i).get(0);
//                        ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
//                        updateTableRow(tableModel, i, key);
//                    }
//                }
                imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
                modifiedTableRow = -1;
            }
        }

        protected void updateTableRow(DefaultTableModel tableModel, int index, ImageKeyInterface key)
        {
            if (imageCollection.containsImage(key))
            {
                tableModel.setValueAt(true, index, imageResultsTableView.getMapColumnIndex());

                Image image = imageCollection.getImage(key);
                tableModel.setValueAt(image.isVisible(), index, imageResultsTableView.getShowFootprintColumnIndex());

                if (image instanceof PerspectiveImage)
                {
                    PerspectiveImage perspectiveImage = (PerspectiveImage) imageCollection.getImage(key);
                    tableModel.setValueAt(perspectiveImage.isFrustumShowing(), index, imageResultsTableView.getFrusColumnIndex());
                }
            }
            else
            {
                tableModel.setValueAt(false, index, imageResultsTableView.getMapColumnIndex());
                tableModel.setValueAt(false, index, imageResultsTableView.getShowFootprintColumnIndex());
                tableModel.setValueAt(false, index, imageResultsTableView.getFrusColumnIndex());
            }

            tableModel.setValueAt(boundaries.containsBoundary(key), index, imageResultsTableView.getBndrColumnIndex());
        }

    }

    class ImageResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            modifiedTableRow = e.getFirstRow();
            ImageSource sourceOfLastQuery = imageSearchModel.getImageSourceOfLastQuery();
            List<List<String>> imageRawResults = imageSearchModel.getImageResults();
            ModelManager modelManager = imageSearchModel.getModelManager();
            if (imageResultsTableView.getResultList().getModel().getRowCount() == 0)
                return;
            int actualRow = imageResultsTableView.getResultList().getRowSorter().convertRowIndexToView(e.getFirstRow());
            int row = (Integer) imageResultsTableView.getResultList().getValueAt(actualRow, imageResultsTableView.getIdColumnIndex()) - 1;

            if (e.getColumn() == imageResultsTableView.getMapColumnIndex())
            {
                //                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                String namePrefix = FileUtil.removeExtension(name);
                if ((Boolean) imageResultsTableView.getResultList().getValueAt(actualRow, imageResultsTableView.getMapColumnIndex()))
                    imageSearchModel.loadImages(namePrefix);
                else
                {
                    imageSearchModel.unloadImages(namePrefix);
                    //                    renderer.setLighting(LightingType.LIGHT_KIT);	//removed due to request in #1667
                }
            }
            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
            {
                //                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                String namePrefix = FileUtil.removeExtension(name);
                boolean visible = (Boolean) imageResultsTableView.getResultList().getValueAt(actualRow, imageResultsTableView.getShowFootprintColumnIndex());
                imageSearchModel.setImageVisibility(namePrefix, visible);
            }
            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
            {
                //                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), sourceOfLastQuery, imageSearchModel.getInstrument());
                ImageCollection images = (ImageCollection) modelManager.getModel(imageSearchModel.getImageCollectionModelName());
                if (images.containsImage(key))
                {
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    image.setShowFrustum(!image.isFrustumShowing());
                }
            }
            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
            {
                //                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(FileUtil.removeExtension(name), sourceOfLastQuery, imageSearchModel.getInstrument());
                try
                {
                    if (!boundaries.containsBoundary(key))
                        boundaries.addBoundary(key);
                    else
                        boundaries.removeBoundary(key);
                }
                catch (Exception e1)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView), "There was an error mapping the boundary.", "Error", JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                }
            }

        }
    }

    //    class DragDropRowTableUI extends BasicTableUI {
    //
    //        private boolean draggingRow = false;
    //        private int startDragPoint;
    //        private int dyOffset;
    //
    //       protected MouseInputListener createMouseInputListener() {
    //           return new DragDropRowMouseInputHandler();
    //       }
    //
    //       public void paint(Graphics g, JComponent c) {
    //            super.paint(g, c);
    //
    //            if (draggingRow) {
    //                 g.setColor(table.getParent().getBackground());
    //                  Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
    //                 g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);
    //
    //                 if (dyOffset < 0) {
    //                      g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
    //                 } else {
    //                      g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
    //                 }
    //            }
    //       }
    //
    //       class DragDropRowMouseInputHandler extends MouseInputHandler {
    //
    //    	   private int toRow;
    //
    //           public void mousePressed(MouseEvent e) {
    //                super.mousePressed(e);
    //                startDragPoint = (int)e.getPoint().getY();
    //                toRow = table.getSelectedRow();
    //           }
    //
    //           public void mouseDragged(MouseEvent e) {
    //                int fromRow = table.getSelectedRow();
    //
    //                if (fromRow >= 0) {
    //                     draggingRow = true;
    //
    //                     int rowHeight = table.getRowHeight();
    //                     int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);
    //
    //                     toRow = fromRow;
    //                     int yMousePoint = (int)e.getPoint().getY();
    //
    //                     if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
    //                          // Move row up
    //                          toRow = fromRow - 1;
    //                     } else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
    //                          // Move row down
    //                          toRow = fromRow + 1;
    //                     }
    //
    //                     DefaultTableModel model = (DefaultTableModel)table.getModel();
    //                     if (toRow >= 0 && toRow < table.getRowCount())
    //                     {
    //                    	 model.moveRow(table.getSelectedRow(), table.getSelectedRow(), toRow);
    //
    //                          List<String> fromList = imageRawResults.get(fromRow);
    //                          List<String> toList = imageRawResults.get(toRow);
    //
    //                          imageRawResults.set(toRow, fromList);
    //                          imageRawResults.set(fromRow, toList);
    ////                          stringRenderer.setImageRawResults(imageRawResults);
    //                           table.setRowSelectionInterval(toRow, toRow);
    //                           startDragPoint = yMousePoint;
    //                     }
    //
    //                     dyOffset = (startDragPoint - yMousePoint) * -1;
    //                     table.repaint();
    //                }
    //           }
    //
    //           public void mouseReleased(MouseEvent e){
    //                super.mouseReleased(e);
    //                draggingRow = false;
    //                table.repaint();
    //           }
    //       }
    //   }
}
