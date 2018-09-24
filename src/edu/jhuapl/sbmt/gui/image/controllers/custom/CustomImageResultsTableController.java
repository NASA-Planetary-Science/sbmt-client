package edu.jhuapl.sbmt.gui.image.controllers.custom;

import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.ModelManager;
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
                String namePrefix = name.substring(0, name.length()-4);
                if ((Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getMapColumnIndex()))
                    model.loadImages(namePrefix, results.get(row));
                else
                {
                    model.unloadImages(namePrefix);
                    renderer.setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                String namePrefix = name.substring(0, name.length()-4);
                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
                model.setImageVisibility(namePrefix, visible);
            }
            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, model.getInstrument());
                System.out.println(
                        "CustomImageResultsTableController.CustomImageResultsTableModeListener: tableChanged: key is " + key);
                System.out.println(
                        "CustomImageResultsTableController.CustomImageResultsTableModeListener: tableChanged: results " + results);
                key.imageType = results.get(row).imageType;
                ImageCollection images = (ImageCollection)modelManager.getModel(model.getImageCollectionModelName());
                if (images.containsImage(key))
                {
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    image.setShowFrustum(!image.isFrustumShowing());
                }
            }
            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, model.getInstrument());
                key.imageType = results.get(row).imageType;
                try
                {
//                    if (!boundaries.containsBoundary(key))
//                        boundaries.addBoundary(key);
//                    else
//                        boundaries.removeBoundary(key);
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
