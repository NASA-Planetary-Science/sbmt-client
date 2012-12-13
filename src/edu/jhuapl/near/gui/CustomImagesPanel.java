/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.near.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;
import vtk.vtkPNGWriter;

import edu.jhuapl.near.gui.CustomImageImporterDialog.ImageInfo;
import edu.jhuapl.near.gui.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.near.model.CustomPerspectiveImage;
import edu.jhuapl.near.model.CylindricalImage;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.MapUtil;
import edu.jhuapl.near.util.Properties;

/**
 *
 * @author kahneg1
 */
public class CustomImagesPanel extends javax.swing.JPanel implements PropertyChangeListener {

    private ModelManager modelManager;
    private ImagePopupMenu imagePopupMenu;
    private boolean initialized = false;


    /** Creates new form CustomImageLoaderPanel */
    public CustomImagesPanel(
            final ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            final PickManager pickManager,
            Renderer renderer)
    {
        this.modelManager = modelManager;

        initComponents();

        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        imageList.setModel(new DefaultListModel());

        ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        imagePopupMenu = new ImagePopupMenu(images, boundaries, infoPanelManager, renderer, this);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                try
                {
                    initializeImageList();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
    }

    private String getCustomDataFolder()
    {
        return modelManager.getSmallBodyModel().getCustomDataFolder();
    }

    private String getConfigFilename()
    {
        return modelManager.getSmallBodyModel().getConfigFilename();
    }

    private void initializeImageList() throws IOException
    {
        if (initialized)
            return;

        MapUtil configMap = new MapUtil(getConfigFilename());

        if (configMap.containsKey(CylindricalImage.LOWER_LEFT_LATITUDES) || configMap.containsKey(Image.PROJECTION_TYPES))
        {
            boolean needToUpgradeConfigFile = false;
            String[] imageNames = configMap.getAsArray(Image.IMAGE_NAMES);
            String[] imageFilenames = configMap.getAsArray(Image.IMAGE_FILENAMES);
            String[] projectionTypes = configMap.getAsArray(Image.PROJECTION_TYPES);
            if (imageFilenames == null)
            {
                // for backwards compatibility
                imageFilenames = configMap.getAsArray(Image.IMAGE_MAP_PATHS);
                imageNames = new String[imageFilenames.length];
                projectionTypes = new String[imageFilenames.length];
                for (int i=0; i<imageFilenames.length; ++i)
                {
                    imageNames[i] = new File(imageFilenames[i]).getName();
                    imageFilenames[i] = "image" + i + ".png";
                    projectionTypes[i] = ProjectionType.CYLINDRICAL.toString();
                }

                // Mark that we need to upgrade config file to latest version
                // which we'll do at end of function.
                needToUpgradeConfigFile = true;
            }
            double[] lllats = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LATITUDES);
            double[] lllons = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LONGITUDES);
            double[] urlats = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LATITUDES);
            double[] urlons = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LONGITUDES);
            String[] sumfileNames = configMap.getAsArray(CustomPerspectiveImage.SUMFILENAMES);
            double[] xfovs = configMap.getAsDoubleArray(CustomPerspectiveImage.X_FOV);
            double[] yfovs = configMap.getAsDoubleArray(CustomPerspectiveImage.Y_FOV);

            int numImages = lllats != null ? lllats.length : (projectionTypes != null ? projectionTypes.length : 0);
            for (int i=0; i<numImages; ++i)
            {
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.name = imageNames[i];
                imageInfo.imagefilename = imageFilenames[i];
                imageInfo.projectionType = ProjectionType.valueOf(projectionTypes[i]);

                if (projectionTypes == null || ProjectionType.CYLINDRICAL.toString().equals(projectionTypes[i]))
                {
                    imageInfo.lllat = lllats[i];
                    imageInfo.lllon = lllons[i];
                    imageInfo.urlat = urlats[i];
                    imageInfo.urlon = urlons[i];
                }
                else if (ProjectionType.PERSPECTIVE.toString().equals(projectionTypes[i]))
                {
                    imageInfo.sumfilename = sumfileNames[i];
                    imageInfo.xfov = xfovs[i];
                    imageInfo.yfov = yfovs[i];
                }

                ((DefaultListModel)imageList.getModel()).addElement(imageInfo);
            }

            if (needToUpgradeConfigFile)
                updateConfigFile();
        }

        initialized = true;
    }

    private void saveImage(int index, ImageInfo oldImageInfo, ImageInfo newImageInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        if (newImageInfo.projectionType == ProjectionType.CYLINDRICAL)
        {
            // If newImageInfo.imagefilename is null, that means we are in edit mode
            // and should continue to use the existing image
            if (newImageInfo.imagefilename == null)
            {
                newImageInfo.imagefilename = oldImageInfo.imagefilename;
                newImageInfo.name = oldImageInfo.name;
            }
            else
            {
                vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
                vtkImageReader2 imageReader = imageFactory.CreateImageReader2(newImageInfo.imagefilename);
                if (imageReader == null)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                            "The format of the specified file is not supported.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }
                imageReader.SetFileName(newImageInfo.imagefilename);
                imageReader.Update();

                vtkAlgorithmOutput imageReaderOutput = imageReader.GetOutputPort();
                vtkPNGWriter imageWriter = new vtkPNGWriter();
                imageWriter.SetInputConnection(imageReaderOutput);
                // We save out the image using a new name that makes use of a UUID
                newImageInfo.imagefilename = "image-" + uuid + ".png";
                imageWriter.SetFileName(getCustomDataFolder() + File.separator + newImageInfo.imagefilename);
                //imageWriter.SetFileTypeToBinary();
                imageWriter.Write();
            }
        }
        else if (newImageInfo.projectionType == ProjectionType.PERSPECTIVE)
        {
            // If newImageInfo.imagefilename is null, that means we are in edit mode
            // and should continue to use the existing image
            if (newImageInfo.imagefilename == null)
            {
                newImageInfo.imagefilename = oldImageInfo.imagefilename;
                newImageInfo.name = oldImageInfo.name;
            }
            else
            {
                // We save out the image using a new name that makes use of a UUID
                String newFilename = "image-" + uuid + ".fit";
                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                FileUtil.copyFile(newImageInfo.imagefilename, newFilepath);
                // Change newImageInfo.imagefilename to the new location of the file
                newImageInfo.imagefilename = newFilename;
            }

            // If newImageInfo.sumfilename is null, that means we are in edit mode
            // and should continue to use the existing sumfile
            if (newImageInfo.sumfilename == null)
            {
                newImageInfo.sumfilename = oldImageInfo.sumfilename;
            }
            else
            {
                // We save out the sumfile using a new name that makes use of a UUID
                String newFilename = "sumfile-" + uuid + ".SUM";
                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                FileUtil.copyFile(newImageInfo.sumfilename, newFilepath);
                // Change newImageInfo.sumfilename to the new location of the file
                newImageInfo.sumfilename = newFilename;
            }
        }

        DefaultListModel model = (DefaultListModel)imageList.getModel();
        if (index >= model.getSize())
        {
            model.addElement(newImageInfo);
        }
        else
        {
            model.set(index, newImageInfo);
        }

        updateConfigFile();
    }

    /**
     * This function unmaps the image from the renderer and maps it again,
     * if it is currently shown.
     * @throws IOException
     * @throws FitsException
     */
    private void remapImageToRenderer(int index) throws FitsException, IOException
    {
        ImageInfo imageInfo = (ImageInfo)((DefaultListModel)imageList.getModel()).get(index);
        String filename = getCustomDataFolder() + File.separator + imageInfo.imagefilename;

        // Remove the image from the renderer
        ImageKey imageKey = new ImageKey(filename,
                imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);

        if (imageCollection.containsImage(imageKey))
        {
            imageCollection.removeImage(imageKey);
            imageCollection.addImage(imageKey);
        }
    }

    private void removeAllImagesFromRenderer()
    {
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        imageCollection.removeImages(ImageSource.LOCAL_CYLINDRICAL);
        imageCollection.removeImages(ImageSource.LOCAL_PERSPECTIVE);
    }

    private void removeImage(int index)
    {
        ImageInfo imageInfo = (ImageInfo)((DefaultListModel)imageList.getModel()).get(index);

        String filename = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
        new File(filename).delete();

        // Remove the image from the renderer
        ImageKey imageKey = new ImageKey(filename,
                imageInfo.projectionType == ProjectionType.CYLINDRICAL ? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        imageCollection.removeImage(imageKey);

        if (imageInfo.projectionType == ProjectionType.PERSPECTIVE)
        {
            filename = getCustomDataFolder() + File.separator + imageInfo.sumfilename;
            new File(filename).delete();
        }

        ((DefaultListModel)imageList.getModel()).remove(index);
    }

    private void moveDown(int i)
    {
        DefaultListModel model = (DefaultListModel)imageList.getModel();

        if (i >= model.getSize())
            return;

        Object o = model.get(i);

        model.remove(i);
        model.add(i+1, o);
    }

    private void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getConfigFilename());

        String imageNames = "";
        String imageFilenames = "";
        String projectionTypes = "";
        String lllats = "";
        String lllons = "";
        String urlats = "";
        String urlons = "";
        String sumfilenames = "";
        String xfovs = "";
        String yfovs = "";

        DefaultListModel imageListModel = (DefaultListModel)imageList.getModel();
        for (int i=0; i<imageListModel.size(); ++i)
        {
            ImageInfo imageInfo = (ImageInfo)imageListModel.get(i);

            imageFilenames += imageInfo.imagefilename;
            imageNames += imageInfo.name;
            projectionTypes += imageInfo.projectionType;
            lllats += String.valueOf(imageInfo.lllat);
            lllons += String.valueOf(imageInfo.lllon);
            urlats += String.valueOf(imageInfo.urlat);
            urlons += String.valueOf(imageInfo.urlon);
            sumfilenames += imageInfo.sumfilename;
            xfovs += String.valueOf(imageInfo.xfov);
            yfovs += String.valueOf(imageInfo.yfov);

            if (i < imageListModel.size()-1)
            {
                imageNames += CustomShapeModel.LIST_SEPARATOR;
                imageFilenames += CustomShapeModel.LIST_SEPARATOR;
                projectionTypes += CustomShapeModel.LIST_SEPARATOR;
                lllats += CustomShapeModel.LIST_SEPARATOR;
                lllons += CustomShapeModel.LIST_SEPARATOR;
                urlats += CustomShapeModel.LIST_SEPARATOR;
                urlons += CustomShapeModel.LIST_SEPARATOR;
                sumfilenames += CustomShapeModel.LIST_SEPARATOR;
                xfovs += CustomShapeModel.LIST_SEPARATOR;
                yfovs += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(Image.IMAGE_NAMES, imageNames);
        newMap.put(Image.IMAGE_FILENAMES, imageFilenames);
        newMap.put(Image.PROJECTION_TYPES, projectionTypes);
        newMap.put(CylindricalImage.LOWER_LEFT_LATITUDES, lllats);
        newMap.put(CylindricalImage.LOWER_LEFT_LONGITUDES, lllons);
        newMap.put(CylindricalImage.UPPER_RIGHT_LATITUDES, urlats);
        newMap.put(CylindricalImage.UPPER_RIGHT_LONGITUDES, urlons);
        newMap.put(CustomPerspectiveImage.SUMFILENAMES, sumfilenames);
        newMap.put(CustomPerspectiveImage.X_FOV, xfovs);
        newMap.put(CustomPerspectiveImage.Y_FOV, yfovs);

        configMap.put(newMap);
    }

    private void imageListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = imageList.locationToIndex(e.getPoint());

            if (index >= 0 && imageList.getCellBounds(index, index).contains(e.getPoint()))
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!imageList.isSelectedIndex(index))
                {
                    imageList.clearSelection();
                    imageList.setSelectedIndex(index);
                }

                int[] selectedIndices = imageList.getSelectedIndices();
                ArrayList<ImageKey> imageKeys = new ArrayList<ImageKey>();
                for (int selectedIndex : selectedIndices)
                {
                    ImageInfo imageInfo = (ImageInfo)((DefaultListModel)imageList.getModel()).get(selectedIndex);
                    String name = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
                    ImageKey imageKey = new ImageKey(name,
                            imageInfo.projectionType == ProjectionType.CYLINDRICAL? ImageSource.LOCAL_CYLINDRICAL : ImageSource.LOCAL_PERSPECTIVE);
                    imageKeys.add(imageKey);
                }
                imagePopupMenu.setCurrentImages(imageKeys);
                imagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof ImageCollection)// || model instanceof PerspectiveImageBoundaryCollection)
            {
                String name = null;

                //if (model instanceof ImageCollection)
                    name = ((ImageCollection)model).getImage((vtkActor)e.getPickedProp()).getKey().name;
                //else if (model instanceof PerspectiveImageBoundaryCollection)
                //    name = ((PerspectiveImageBoundaryCollection)model).getBoundaryName((vtkActor)e.getPickedProp());

                int idx = -1;
                int size = imageList.getModel().getSize();
                for (int i=0; i<size; ++i)
                {
                    ImageInfo imageInfo = (ImageInfo)((DefaultListModel)imageList.getModel()).get(i);
                    String imageFilename = getCustomDataFolder() + File.separator + imageInfo.imagefilename;
                    if (name.equals(imageFilename))
                    {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0)
                {
                    imageList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageList.scrollRectToVisible(cellBounds);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        imageList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                imageListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                imageListMouseReleased(evt);
            }
        });
        imageList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                imageListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(imageList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 377;
        gridBagConstraints.ipady = 241;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        newButton.setText("New...");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        add(newButton, gridBagConstraints);

        editButton.setText("Edit...");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        add(editButton, gridBagConstraints);

        jLabel1.setText("Images");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        add(jLabel1, gridBagConstraints);

        moveUpButton.setText("Move Up");
        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        add(moveUpButton, gridBagConstraints);

        moveDownButton.setText("Move Down");
        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        add(moveDownButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        deleteButton.setText("Delete from List");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        jPanel1.add(deleteButton, gridBagConstraints);

        removeAllButton.setText("Remove All From View");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        jPanel1.add(removeAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        ImageInfo imageInfo = new ImageInfo();
        CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false);
        dialog.setImageInfo(imageInfo, modelManager.getSmallBodyModel().isEllipsoid());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            imageInfo = dialog.getImageInfo();
            try
            {
                saveImage(((DefaultListModel)imageList.getModel()).getSize(), null, imageInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int[] selectedIndices = imageList.getSelectedIndices();
        Arrays.sort(selectedIndices);
        for (int i=selectedIndices.length-1; i>=0; --i)
        {
            removeImage(selectedIndices[i]);
        }

        updateConfigFile();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int selectedItem = imageList.getSelectedIndex();
        if (selectedItem >= 0)
        {
            ImageInfo oldImageInfo = (ImageInfo)((DefaultListModel)imageList.getModel()).get(selectedItem);

            CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, true);
            dialog.setImageInfo(oldImageInfo, modelManager.getSmallBodyModel().isEllipsoid());
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // If user clicks okay replace item in list
            if (dialog.getOkayPressed())
            {
                ImageInfo newImageInfo = dialog.getImageInfo();
                try
                {
                    saveImage(selectedItem, oldImageInfo, newImageInfo);
                    remapImageToRenderer(selectedItem);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (FitsException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void imageListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMousePressed
        imageListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageListMousePressed

    private void imageListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageListMouseReleased
        imageListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageListMouseReleased

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        removeAllImagesFromRenderer();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int minSelectedItem = imageList.getMinSelectionIndex();
        if (minSelectedItem > 0)
        {
            int[] selectedIndices = imageList.getSelectedIndices();
            Arrays.sort(selectedIndices);
            for (int i=0; i<selectedIndices.length; ++i)
            {
                --selectedIndices[i];
                moveDown(selectedIndices[i]);
            }

            imageList.clearSelection();
            imageList.setSelectedIndices(selectedIndices);
            imageList.scrollRectToVisible(imageList.getCellBounds(minSelectedItem-1, minSelectedItem-1));

            updateConfigFile();
        }
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int maxSelectedItem = imageList.getMaxSelectionIndex();
        if (maxSelectedItem >= 0 && maxSelectedItem < imageList.getModel().getSize()-1)
        {
            int[] selectedIndices = imageList.getSelectedIndices();
            Arrays.sort(selectedIndices);
            for (int i=selectedIndices.length-1; i>=0; --i)
            {
                moveDown(selectedIndices[i]);
                ++selectedIndices[i];
            }

            imageList.clearSelection();
            imageList.setSelectedIndices(selectedIndices);
            imageList.scrollRectToVisible(imageList.getCellBounds(maxSelectedItem+1, maxSelectedItem+1));

            updateConfigFile();
        }
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void imageListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_imageListValueChanged
        int[] indices = imageList.getSelectedIndices();
        if (indices == null || indices.length == 0)
        {
            editButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        else
        {
            editButton.setEnabled(indices.length == 1);
            deleteButton.setEnabled(true);
            int minSelectedItem = imageList.getMinSelectionIndex();
            int maxSelectedItem = imageList.getMaxSelectionIndex();
            moveUpButton.setEnabled(minSelectedItem > 0);
            moveDownButton.setEnabled(maxSelectedItem < imageList.getModel().getSize()-1);
        }
    }//GEN-LAST:event_imageListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JList imageList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton removeAllButton;
    // End of variables declaration//GEN-END:variables
}
