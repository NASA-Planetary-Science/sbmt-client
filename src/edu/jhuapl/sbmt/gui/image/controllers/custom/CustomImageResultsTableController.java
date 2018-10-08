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
import edu.jhuapl.saavtk.util.IdPair;
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

    protected void showImageBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;
        boundaries.removeAllBoundaries();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= imageRawResults.size())
                break;

            try
            {
                String currentImage = imageRawResults.get(i).get(0);
//                String boundaryName = currentImage.substring(0,currentImage.length()-4);
                ImageKey key = model.getImageKeyForIndex(i);
                //TODO Can't handle cylindrical and perspective in the same area - should we bother with boundaries for cylindrical?
//                boundaries.addBoundary(key);
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error mapping the boundary.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
                break;
            }
        }
    }

    @Override
    protected void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable resultList = imageResultsTableView.getResultList();
            int index = resultList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
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
                ImageKey key = model.getImageKeyForIndex(row);
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
            List<List<String>> imageRawResults = model.getImageResults();
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
                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
                ImageKey key = model.getImageKeyForIndex(row);
                model.setImageVisibility(key, visible);
            }
            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
            {
                int row = e.getFirstRow();
                ImageKey key = model.getImageKeyForIndex(row);
                if (imageCollection.containsImage(key) && (imageCollection.getImage(key) instanceof PerspectiveImage))
                {
                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                    image.setShowFrustum(!image.isFrustumShowing());
                }
            }
            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
            {
                int row = e.getFirstRow();
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
