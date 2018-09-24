package edu.jhuapl.sbmt.gui.image.controllers.custom;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.image.controllers.images.ImageResultsTableController;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomImagesModel;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ImageInfo;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class CustomImageResultsTableController extends ImageResultsTableController
{
    private List<ImageInfo> results;
    private CustomImagesModel model;

    public CustomImageResultsTableController(ImagingInstrument instrument, ImageCollection imageCollection, CustomImagesModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager, SbmtSpectrumWindowManager spectrumPanelManager)
    {
        super(instrument, imageCollection, model, renderer, infoPanelManager, spectrumPanelManager);
        this.model = model;
        this.results = model.getCustomImages();
    }

    @Override
    public void setImageResultsPanel()
    {
        // TODO Auto-generated method stub
        super.setImageResultsPanel();
        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
        tableModelListener = new CustomImageResultsTableModeListener();
        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);

        this.imageCollection.removePropertyChangeListener(propertyChangeListener);
        boundaries.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = new CustomImageResultsPropertyChangeListener();
        this.imageCollection.addPropertyChangeListener(propertyChangeListener);
        boundaries.addPropertyChangeListener(propertyChangeListener);

        tableModel = new CustomImagesTableModel(new Object[0][7], columnNames);
        imageResultsTableView.getResultList().setModel(tableModel);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setPreferredWidth(35);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setResizable(true);

        imageResultsTableView.getResultList().addMouseListener(new MouseAdapter()
        {
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


        imageResultsTableView.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    model.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
                    imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
                }
            }
        });

        try
        {
            model.initializeImageList();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    public void setImageResults(List<ImageInfo> results)
//    {
//        JTable resultTable = imageResultsTableView.getResultList();
////        System.out
////                .println("ImageResultsTableController: setImageResults: setting to " + results.size() + " images matched");
////        imageResultsTableView.getResultsLabel().setText(results.size() + " images matched");
//        imageRawResults = results;
//        stringRenderer.setImageRawResults(imageRawResults);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
//        imageCollection.removePropertyChangeListener(propertyChangeListener);
//        boundaries.removePropertyChangeListener(propertyChangeListener);
//
//        try
//        {
//            int mapColumnIndex = imageResultsTableView.getMapColumnIndex();
//            int showFootprintColumnIndex = imageResultsTableView.getShowFootprintColumnIndex();
//            int frusColumnIndex = imageResultsTableView.getFrusColumnIndex();
//            int idColumnIndex = imageResultsTableView.getIdColumnIndex();
//            int filenameColumnIndex = imageResultsTableView.getFilenameColumnIndex();
//            int dateColumnIndex = imageResultsTableView.getDateColumnIndex();
//            System.out.println(
//                    "ImageResultsTableController: setImageResults: date column index " + dateColumnIndex);
//            int bndrColumnIndex = imageResultsTableView.getBndrColumnIndex();
//            int[] widths = new int[resultTable.getColumnCount()];
//            int[] columnsNeedingARenderer=new int[]{idColumnIndex,filenameColumnIndex,dateColumnIndex};
//
//            // add the results to the list
//            ((DefaultTableModel)resultTable.getModel()).setRowCount(results.size());
//            int i=0;
//            for (List<String> str : results)
//            {
//                Date dt = new Date(Long.parseLong(str.get(1)));
//
//                String name = imageRawResults.get(i).get(0);
//                ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
//                if (imageCollection.containsImage(key))
//                {
//                    resultTable.setValueAt(true, i, mapColumnIndex);
//                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
//                    resultTable.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
//                    resultTable.setValueAt(!image.isFrustumShowing(), i, frusColumnIndex);
//                }
//                else
//                {
//                    resultTable.setValueAt(false, i, mapColumnIndex);
//                    resultTable.setValueAt(false, i, showFootprintColumnIndex);
//                    resultTable.setValueAt(false, i, frusColumnIndex);
//                }
//
//
//                if (boundaries.containsBoundary(key))
//                    resultTable.setValueAt(true, i, bndrColumnIndex);
//                else
//                    resultTable.setValueAt(false, i, bndrColumnIndex);
//
//                resultTable.setValueAt(i+1, i, idColumnIndex);
//                resultTable.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
//                resultTable.setValueAt(sdf.format(dt), i, dateColumnIndex);
//
//                for (int j : columnsNeedingARenderer)
//                {
//                    TableCellRenderer renderer = resultTable.getCellRenderer(i, j);
//                    Component comp = resultTable.prepareRenderer(renderer, i, j);
//                    widths[j] = Math.max (comp.getPreferredSize().width, widths[j]);
//                }
//
//                ++i;
//            }
//
//            for (int j : columnsNeedingARenderer)
//                imageResultsTableView.getResultList().getColumnModel().getColumn(j).setPreferredWidth(widths[j] + 5);
//
//            boolean enablePostSearchButtons = resultTable.getModel().getRowCount() > 0;
//            imageResultsTableView.getSaveImageListButton().setEnabled(enablePostSearchButtons);
//            imageResultsTableView.getSaveSelectedImageListButton().setEnabled(resultTable.getSelectedRowCount() > 0);
//            imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && enablePostSearchButtons);
//        }
//        finally
//        {
//            imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
//            imageCollection.addPropertyChangeListener(propertyChangeListener);
//            boundaries.addPropertyChangeListener(propertyChangeListener);
//        }
//
//
//        // Show the first set of boundaries
//        imageSearchModel.setResultIntervalCurrentlyShown( new IdPair(0, Integer.parseInt((String)imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem())));
//        this.showImageBoundaries(imageSearchModel.getResultIntervalCurrentlyShown());
//
//        // Enable or disable the image gallery button
//        imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && !results.isEmpty());
//    }

    @Override
    protected void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            List<ImageInfo> imageList = ((CustomImagesModel)imageSearchModel).getCustomImages();
//            int index = imageList.locationToIndex(e.getPoint());

            JTable resultList = imageResultsTableView.getResultList();
            int index = resultList.rowAtPoint(e.getPoint());

            if (index >= 0 /*&& imageList.getCellBounds(index, index).contains(e.getPoint())*/)
            {
//                // If the item right-clicked on is not selected, then deselect all the
//                // other items and select the item right-clicked on.
//                if (!imageList.isSelectedIndex(index))
//                {
//                    imageList.clearSelection();
//                    imageList.setSelectedIndex(index);
//                }

                int[] selectedIndices = resultList.getSelectedRows();
                List<ImageKey> imageKeys = new ArrayList<ImageKey>();
                for (int selectedIndex : selectedIndices)
                {
                    ImageInfo imageInfo = ((CustomImagesModel)imageSearchModel).getCustomImages().get(selectedIndex);
                    String name = ((CustomImagesModel)imageSearchModel).getCustomDataFolder() + File.separator + imageInfo.imagefilename;
                    ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
                    FileType fileType = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
                    ImageType imageType = imageInfo.imageType;
                    ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
                    ImageKey imageKey = new ImageKey(name, source, fileType, imageType, instrument, null, 0);
                    imageKeys.add(imageKey);
                }
                imagePopupMenu.setCurrentImages(imageKeys);
                imagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class CustomImageResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = imageResultsTableView.getResultList();
                imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
                int size = imageRawResults.size();
                for (int i=0; i<size; ++i)
                {
                    String name = imageRawResults.get(i).get(0);
//                    ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//                    ImageKey key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
                    ImageKey key = model.getImageKeyForIndex(i);
                    if (imageCollection.containsImage(key))
                    {
                        resultList.setValueAt(true, i, imageResultsTableView.getMapColumnIndex());
                        resultList.setValueAt(imageCollection.getImage(key).isVisible(), i, imageResultsTableView.getShowFootprintColumnIndex());
                        if (imageCollection.getImage(key).getClass() == PerspectiveImage.class)
                        {
                            PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(model.getImageKeyForIndex(i));
                            resultList.setValueAt(image.isFrustumShowing(), i, imageResultsTableView.getFrusColumnIndex());
                        }
                    }
                    else
                    {
                        resultList.setValueAt(false, i, imageResultsTableView.getMapColumnIndex());
                        resultList.setValueAt(false, i, imageResultsTableView.getShowFootprintColumnIndex());
                        resultList.setValueAt(false, i, imageResultsTableView.getFrusColumnIndex());
                    }
                    if (boundaries.containsBoundary(key))
                        resultList.setValueAt(true, i, imageResultsTableView.getBndrColumnIndex());
                    else
                        resultList.setValueAt(false, i, imageResultsTableView.getBndrColumnIndex());
                }
                imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }

    public class CustomImagesTableModel extends DefaultTableModel
    {
        public CustomImagesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == imageResultsTableView.getShowFootprintColumnIndex() || column == imageResultsTableView.getFrusColumnIndex())
            {
                String name = imageRawResults.get(row).get(0);
                ImageKey key = model.getImageKeyForIndex(row);
//                ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());
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
            else
                return String.class;
        }
    }


    class CustomImageResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            ImageSource sourceOfLastQuery = model.getImageSourceOfLastQuery();
            List<List<String>> imageRawResults = model.getImageResults();
            ModelManager modelManager = model.getModelManager();
            if (e.getColumn() == imageResultsTableView.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
                if ((Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getMapColumnIndex()))
                    model.loadImages(name, results.get(row));
                else
                {
                    model.unloadImages(name);
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
                ImageKey key = model.getImageKeyForIndex(row);
                model.setImageVisibility(key, visible);
            }
            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
            {
                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, model.getInstrument());
                ImageKey key = model.getImageKeyForIndex(row);
//                System.out.println(
//                        "CustomImageResultsTableController.CustomImageResultsTableModeListener: tableChanged: key is " + key);
//                System.out.println(
//                        "CustomImageResultsTableController.CustomImageResultsTableModeListener: tableChanged: results " + results);
//                key.imageType = results.get(row).imageType;
//                ImageCollection images = (ImageCollection)modelManager.getModel(model.getImageCollectionModelName());
//                System.out.println(
//                        "CustomImageResultsTableController.CustomImageResultsTableModeListener: tableChanged: " + imageCollection.getImage(key).getClass());
                if (imageCollection.containsImage(key) && (imageCollection.getImage(key) instanceof PerspectiveImage))
                {
                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                    image.setShowFrustum(!image.isFrustumShowing());
                }
            }
            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
            {
                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, model.getInstrument());
                ImageKey key = model.getImageKeyForIndex(row);
                key.imageType = results.get(row).imageType;
                try
                {
                    if (!boundaries.containsBoundary(key))
                        boundaries.addBoundary(key);
                    else
                        boundaries.removeBoundary(key);
                }
                catch (Exception e1) {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                            "There was an error mapping the boundary.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                }
            }

        }
    }

}
