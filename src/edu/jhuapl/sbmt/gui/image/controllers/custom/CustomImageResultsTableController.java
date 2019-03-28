package edu.jhuapl.sbmt.gui.image.controllers.custom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.image.controllers.images.ImageResultsTableController;
import edu.jhuapl.sbmt.gui.image.model.CustomImageKeyInterface;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomImagesModel;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

public class CustomImageResultsTableController extends ImageResultsTableController
{
    private List<CustomImageKeyInterface> results;
    private CustomImagesModel model;
    int modifiedTableRow = -1;

    public CustomImageResultsTableController(ImagingInstrument instrument, ImageCollection imageCollection, CustomImagesModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager, SbmtSpectrumWindowManager spectrumPanelManager)
    {
        super(instrument, imageCollection, model, renderer, infoPanelManager, spectrumPanelManager);
        this.model = model;
        this.results = model.getCustomImages();
        this.boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName());
    }

    @Override
    public void setImageResultsPanel()
    {

        super.setImageResultsPanel();
        imageResultsTableView.getNextButton().setVisible(false);
        imageResultsTableView.getPrevButton().setVisible(false);
        imageResultsTableView.getNumberOfBoundariesComboBox().setVisible(false);
        imageResultsTableView.getLblNumberBoundaries().setVisible(false);

        imageResultsTableView.getViewResultsGalleryButton().setVisible(false);

        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
        tableModelListener = new CustomImageResultsTableModeListener();
        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
        this.imageCollection.removePropertyChangeListener(propertyChangeListener);
        boundaries.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = new CustomImageResultsPropertyChangeListener();


        this.imageResultsTableView.addComponentListener(new ComponentListener()
		{

			@Override
			public void componentShown(ComponentEvent e)
			{
				imageCollection.addPropertyChangeListener(propertyChangeListener);
		        boundaries.addPropertyChangeListener(propertyChangeListener);
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e)
			{
				imageCollection.removePropertyChangeListener(propertyChangeListener);
		        boundaries.removePropertyChangeListener(propertyChangeListener);
			}
		});


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

        imageResultsTableView.getRemoveAllImagesButton().removeActionListener(imageResultsTableView.getRemoveAllImagesButton().getActionListeners()[0]);
        imageResultsTableView.getRemoveAllButton().removeActionListener(imageResultsTableView.getRemoveAllButton().getActionListeners()[0]);

        imageResultsTableView.getRemoveAllImagesButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.removeAllImagesButtonActionPerformed(e);
            }
        });

        imageResultsTableView.getRemoveAllButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.removeAllButtonActionPerformed(e);
            }
        });

        try
        {
            model.initializeImageList();
            this.showImageBoundaries(null);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void nextButtonActionPerformed(java.awt.event.ActionEvent evt)
    {

        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < imageResultsTableView.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
            showImageBoundaries(resultIntervalCurrentlyShown);
        }
    }

    @Override
    protected void loadImageListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showOpenDialog(imageResultsTableView, "Select File");

        if (file != null)
        {
            try
            {
                model.loadImages(file.getAbsolutePath());
                model.setResultIntervalCurrentlyShown(new IdPair(0, model.getNumBoundaries()));
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error reading the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }

    }

    @Override
    protected void saveImageListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showSaveDialog(imageResultsTableView, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                model.saveImages(results, file.getAbsolutePath());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    @Override
    protected void saveSelectedImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {
        File file = CustomFileChooser.showSaveDialog(imageResultsTableView, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                ArrayList<CustomImageKeyInterface> infos = new ArrayList<CustomImageKeyInterface>();
                int[] selectedIndices = imageResultsTableView.getResultList().getSelectedRows();
                for (int selectedIndex : selectedIndices)
                {
                    infos.add(model.getCustomImages().get(selectedIndex));
                }
                model.saveImages(infos, file.getAbsolutePath());

            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    @Override
    public void setImageResults(List<List<String>> results)
    {
        super.setImageResults(results);
        model.setResultIntervalCurrentlyShown(new IdPair(0, results.size()));
    }

    @Override
    protected void showImageBoundaries(IdPair idPair)
    {
        if (idPair == null)
        {
            boundaries.removeAllBoundaries();
            return;
        }
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
//                String currentImage = imageRawResults.get(i).get(0);
//                String boundaryName = currentImage.substring(0,currentImage.length()-4);
                CustomImageKeyInterface key = model.getImageKeyForIndex(i);
                CustomImageKeyInterface info;
                if (key.getProjectionType() == ProjectionType.PERSPECTIVE)
        		{
        			info = new CustomPerspectiveImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), key.getImageFilename(), key.getSource(), key.getImageType(), ((CustomPerspectiveImageKey)key).getRotation(), ((CustomPerspectiveImageKey)key).getFlip(), key.getFileType(), key.getPointingFile(), key.getDate(), key.getName());
                    boundaries.addBoundary(info);

        		}
        		else
        		{
        			info = new CustomCylindricalImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), key.getImageFilename(), key.getImageType(), key.getSource(), key.getDate(), key.getName());
        		}
                //TODO For now, we don't handle cylindrical image boundaries, since it is a PerspectiveImageBoundary - need to make new classes for this.
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
                List<CustomImageKeyInterface> imageKeys = new ArrayList<CustomImageKeyInterface>();
                for (int selectedIndex : selectedIndices)
                {
                    CustomImageKeyInterface imageInfo = ((CustomImagesModel)imageSearchModel).getCustomImages().get(selectedIndex);
                    CustomImageKeyInterface revisedKey = ((CustomImagesModel)imageSearchModel).getRevisedKey(imageInfo);
                    imageKeys.add(revisedKey);
//                    String name = ((CustomImagesModel)imageSearchModel).getCustomDataFolder() + File.separator + imageInfo.imagefilename;
//                    ImageSource source = imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE;
//                    FileType fileType = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? FileType.SUM : FileType.INFO;
//                    String pointingFile = imageInfo.sumfilename != null && !imageInfo.sumfilename.equals("null") ? imageInfo.sumfilename : imageInfo.infofilename;
//                    pointingFile = ((CustomImagesModel)imageSearchModel).getCustomDataFolder() + File.separator + pointingFile;
//                    ImageType imageType = imageInfo.imageType;
//                    ImagingInstrument instrument = imageType == ImageType.GENERIC_IMAGE ? new ImagingInstrument(imageInfo.rotation, imageInfo.flip) : null;
//                    ImageKey imageKey = new ImageKey(name, source, fileType, imageType, instrument, null, 0, pointingFile);
//                    imageKeys.add(imageKey);
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
                int startIndex = imageSearchModel.getResultIntervalCurrentlyShown().id1;
                int endIndex = Math.min(size, imageSearchModel.getResultIntervalCurrentlyShown().id2);
                if (modifiedTableRow > size) modifiedTableRow = -1;
                if (modifiedTableRow != -1)
                {
                	startIndex = modifiedTableRow;
                	endIndex = startIndex + 1;
                }

                for (int i=startIndex; i<endIndex; ++i)
                {
                    CustomImageKeyInterface info = getConvertedKey(model.getImageKeyForIndex(i));
                    if (imageCollection.containsImage(info))
                    {
                        resultList.setValueAt(true, i, imageResultsTableView.getMapColumnIndex());
                        resultList.setValueAt(imageCollection.getImage(info).isVisible(), i, imageResultsTableView.getShowFootprintColumnIndex());
                        if (imageCollection.getImage(info) instanceof PerspectiveImage)
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


                    if (boundaries.containsBoundary(info))
                    {
                        resultList.setValueAt(true, i, imageResultsTableView.getBndrColumnIndex());
                    }
                    else
                    {
                        resultList.setValueAt(false, i, imageResultsTableView.getBndrColumnIndex());
                    }
                }
                imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
                modifiedTableRow = -1;
            }
        }

    }

    private CustomImageKeyInterface getConvertedKey(CustomImageKeyInterface key)
    {
//    	CustomImageKeyInterface key = model.getImageKeyForIndex(i);
        CustomImageKeyInterface info;
        if (key.getProjectionType() == ProjectionType.PERSPECTIVE)
		{
			info = new CustomPerspectiveImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), key.getImageFilename(), key.getSource(), key.getImageType(), ((CustomPerspectiveImageKey)key).getRotation(), ((CustomPerspectiveImageKey)key).getFlip(), key.getFileType(), key.getPointingFile(), key.getDate(), key.getName());
		}
		else
		{
			info = new CustomCylindricalImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), key.getImageFilename(), key.getImageType(), key.getSource(), key.getDate(), key.getName());
		}
        return info;
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
            	CustomImageKeyInterface info = getConvertedKey(model.getImageKeyForIndex(row));
                return imageCollection.containsImage(info);
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

    public String getCustomDataFolder()
    {
        return model.getModelManager().getPolyhedralModel().getCustomDataFolder();
    }


    class CustomImageResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
        	modifiedTableRow = e.getFirstRow();
            List<List<String>> imageRawResults = model.getImageResults();
            results = model.getCustomImages();
            if (e.getColumn() == imageResultsTableView.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
//                String namePrefix = name.substring(0, name.length()-4);
                if ((Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getMapColumnIndex()))
                {
                    model.loadImages(name, results.get(row));
                }
                else
                {
                    model.setImageVisibility(getConvertedKey(results.get(row)), false);
                    model.unloadImages(name, getConvertedKey(results.get(row)));
//                    renderer.setLighting(LightingType.LIGHT_KIT);	//removed due to request in #1667
                }
            }
            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
                CustomImageKeyInterface info = getConvertedKey(model.getImageKeyForIndex(row));
                model.setImageVisibility(info, visible);
            }
            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
            {
                int row = e.getFirstRow();
                ImageKeyInterface key = model.getImageKeyForIndex(row);
                if (imageCollection.containsImage(key) && (imageCollection.getImage(key) instanceof PerspectiveImage))
                {
                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                    image.setShowFrustum(!image.isFrustumShowing());
                }
            }
            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
            {
                int row = e.getFirstRow();
                CustomImageKeyInterface key;
//                ImageKeyInterface key = model.getImageKeyForIndex(row);
                CustomImageKeyInterface info = results.get(row);
                if (info.getProjectionType() == ProjectionType.PERSPECTIVE)
        		{
        			key = new CustomPerspectiveImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), info.getImageFilename(), info.getSource(), info.getImageType(), ((CustomPerspectiveImageKey)info).getRotation(), ((CustomPerspectiveImageKey)info).getFlip(), info.getFileType(), info.getPointingFile(), info.getDate(), info.getName());
        		}
        		else
        		{
        			key = new CustomCylindricalImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), info.getImageFilename(), info.getImageType(), info.getSource(), info.getDate(), info.getName());
        		}
                // There used to be an assignment here of the key.imageType, but that field is now immutable.
                // However, it appears that this assignment is not necessary -- the correct ImageType is
                // injected when the key is created. Replaced the assignment with a check for mismatch inside
                // the try just for testing, to uncover any runtime cases where this may actually be needed.
                // key.imageType = results.get(row).imageType;
                try
                {
                	// TODO remove this check if it never triggers the AssertionError.
                    if (key.getImageType() != results.get(row).getImageType())
                    {
                        throw new AssertionError("Image type mismatch");
                    }
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
