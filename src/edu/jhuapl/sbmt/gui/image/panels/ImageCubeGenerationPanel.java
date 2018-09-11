package edu.jhuapl.sbmt.gui.image.panels;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import vtk.vtkActor;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.gui.image.ImageCubePopupMenu;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCube;
import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import nom.tam.fits.FitsException;

public class ImageCubeGenerationPanel extends JPanel implements PropertyChangeListener, TableModelListener, MouseListener, ListSelectionListener
{
    private JList imageCubesDisplayedList;
    private ImageCubePopupMenu imageCubePopupMenu;
    private javax.swing.JButton generateImageCubeButton;
    private javax.swing.JButton greenButton;
    private javax.swing.JComboBox greenComboBox;
    private javax.swing.JLabel greenLabel;
    private javax.swing.JButton blueButton;
    private javax.swing.JComboBox blueComboBox;
    private javax.swing.JLabel blueLabel;
    private javax.swing.JButton redButton;
    private javax.swing.JComboBox redComboBox;
    private javax.swing.JLabel redLabel;

    public ImageCubeGenerationPanel()
    {
        ImageCubeCollection imageCubes = (ImageCubeCollection)modelManager.getModel(getImageCubeCollectionModelName());
        imageCubePopupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, this);

        imageCubes.addPropertyChangeListener(this);
        imageCubesDisplayedList = new JList();
        imageCubesDisplayedList.addListSelectionListener(this);

        imageCubesDisplayedList.setModel(new DefaultListModel());


        imageCubesDisplayedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        imageCubesDisplayedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                imageCubesDisplayedListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                imageCubesDisplayedListMouseReleased(evt);
            }
        });

        removeImageCubeButton.setText("Remove Image Cube");
        removeImageCubeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeImageCubeButtonActionPerformed(evt);
            }
        });
        jPanel19.add(removeImageCubeButton);

        generateImageCubeButton.setText("Generate Image Cube");
        generateImageCubeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateImageCubeButtonActionPerformed(evt);
            }
        });
        jPanel19.add(generateImageCubeButton);

        jScrollPane5.setViewportView(imageCubesDisplayedList);
    }


    private void imageCubesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = imageCubesDisplayedList.locationToIndex(e.getPoint());

            if (index >= 0 && imageCubesDisplayedList.getCellBounds(index, index).contains(e.getPoint()))
            {
                imageCubesDisplayedList.setSelectedIndex(index);
                ImageCubeKey colorKey = (ImageCubeKey)((DefaultListModel)imageCubesDisplayedList.getModel()).get(index);
                imageCubePopupMenu.setCurrentImage(colorKey);
                imageCubePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected void generateImageCube(ActionEvent e)
    {
        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        ImageCubeCollection model = (ImageCubeCollection)modelManager.getModel(getImageCubeCollectionModelName());

        ImageKey firstKey = null;
        boolean multipleFrustumVisible = false;

        List<ImageKey> selectedKeys = new ArrayList<ImageKey>();
        int[] selectedIndices = resultList.getSelectedRows();
        //System.out.println(Arrays.toString(selectedIndices));
        for (int selectedIndex : selectedIndices)
        {
            String name = imageRawResults.get(selectedIndex).get(0);
            ImageKey selectedKey = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//            System.out.println("Key: " + selectedKey.name);
            selectedKeys.add(selectedKey);
            PerspectiveImage selectedImage = (PerspectiveImage)images.getImage(selectedKey);
            if(selectedImage == null)
            {
                // We are in here because the image is not mapped, display an error message and exit
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "All selected images must be mapped when generating an image cube.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // "first key" is indicated by the first image with a visible frustum
            if (selectedImage.isFrustumShowing())
             {
                if(firstKey == null)
                {
                    firstKey = selectedKey;
                }
                else
                {
                    multipleFrustumVisible = true;
                }
            }

//            if (!selectedRedKey.band.equals("0"))
//                imageName = selectedKey.band + ":" + imageName;
        }

        if(selectedKeys.size() == 0)
        {
            // We are in here because no images were selected by user
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "At least one image must be selected when generating an image cube.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if(firstKey == null)
        {
            // We are in here because no frustum was selected by user
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "At least one selected image must have its frustum showing when generating an image cube.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else
        {
            PerspectiveImage firstImage = (PerspectiveImage)images.getImage(firstKey);

            ImageCubeKey imageCubeKey = new ImageCubeKey(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());
            try
            {
                DefaultListModel listModel = (DefaultListModel)imageCubesDisplayedList.getModel();
                if (!model.containsImage(imageCubeKey))
                {
                    model.addImage(imageCubeKey);

                    listModel.addElement(imageCubeKey);
                    int idx = listModel.size()-1;
                    imageCubesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageCubesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageCubesDisplayedList.scrollRectToVisible(cellBounds);

                    if(multipleFrustumVisible)
                    {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                                "More than one selected image has a visible frustum, image cube was generated using the first such frustum in order of appearance in the image list.",
                                "Notification",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else
                {
                    int idx = listModel.indexOf(imageCubeKey);
                    imageCubesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageCubesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageCubesDisplayedList.scrollRectToVisible(cellBounds);

                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                            "Image cube consisting of same images already exists, no new image cube was generated.",
                            "Notification",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (ImageCube.NoOverlapException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "The images you selected do not overlap.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void removeImageCube(ActionEvent e)
    {
        int index = imageCubesDisplayedList.getSelectedIndex();
        if (index >= 0)
        {
            ImageCubeKey imageCubeKey = (ImageCubeKey)((DefaultListModel)imageCubesDisplayedList.getModel()).remove(index);
            ImageCubeCollection model = (ImageCubeCollection)modelManager.getModel(getImageCubeCollectionModelName());
            model.removeImage(imageCubeKey);

            // Select the element in its place (unless it's the last one in which case
            // select the previous one)
            if (index >= imageCubesDisplayedList.getModel().getSize())
                --index;
            if (index >= 0)
                imageCubesDisplayedList.setSelectionInterval(index, index);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof ImageCollection || model instanceof PerspectiveImageBoundaryCollection)
            {
                String name = null;

                if (model instanceof ImageCollection)
                    name = ((ImageCollection)model).getImageName((vtkActor)e.getPickedProp());
                else if (model instanceof PerspectiveImageBoundaryCollection)
                    name = ((PerspectiveImageBoundaryCollection)model).getBoundaryName((vtkActor)e.getPickedProp());

                int idx = -1;
                int size = imageRawResults.size();
                for (int i=0; i<size; ++i)
                {
                    // Ignore extension (The name returned from getImageName or getBoundary
                    // is the same as the first element of each list with the imageRawResults
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
                    resultList.setRowSelectionInterval(idx, idx);
                    Rectangle cellBounds = resultList.getCellRect(idx, 0, true);
                    if (cellBounds != null)
                        resultList.scrollRectToVisible(cellBounds);
                }
            }
        }
        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            resultList.getModel().removeTableModelListener(this);
            int size = imageRawResults.size();
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
            for (int i=0; i<size; ++i)
            {
                String name = imageRawResults.get(i).get(0);
//                ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                if (images.containsImage(key))
                {
                    resultList.setValueAt(true, i, mapColumnIndex);
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    resultList.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
                    resultList.setValueAt(image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    resultList.setValueAt(false, i, mapColumnIndex);
                    resultList.setValueAt(false, i, showFootprintColumnIndex);
                    resultList.setValueAt(false, i, frusColumnIndex);
                }
                if (boundaries.containsBoundary(key))
                    resultList.setValueAt(true, i, bndrColumnIndex);
                else
                    resultList.setValueAt(false, i, bndrColumnIndex);
            }
            resultList.getModel().addTableModelListener(this);
            // Repaint the list in case the boundary colors has changed
            resultList.repaint();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {
            viewResultsGalleryButton.setEnabled(enableGallery && resultList.getSelectedRowCount() > 0);
        }
    }

    private void removeImageCubeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeImageCubeButtonActionPerformed
        removeImageCube(evt);
    }//GEN-LAST:event_removeImageCubeButtonActionPerformed

    private void generateImageCubeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateImageCubeButtonActionPerformed
        generateImageCube(evt);
    }//GEN-LAST:event_generateImageCubeButtonActionPerformed

    private void imageCubesDisplayedListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageCubesDisplayedListMousePressed
        imageCubesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageCubesDisplayedListMousePressed

    private void imageCubesDisplayedListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageCubesDisplayedListMouseReleased
        imageCubesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageCubesDis

    public javax.swing.JList getImageCubesDisplayedList()
    {
        return imageCubesDisplayedList;
    }
}
