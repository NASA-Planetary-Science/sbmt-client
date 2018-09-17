package edu.jhuapl.sbmt.gui.image.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.image.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.model.ImageCubeGenerationModel;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.panels.ImageCubeGenerationPanel;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCube;
import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import nom.tam.fits.FitsException;

public class ImageCubeGenerationController
{

    private ImageSearchModel model;
    private ImageCubeGenerationModel cubeModel;
    private ImageCubeGenerationPanel panel;
    private SbmtInfoWindowManager infoPanelManager;
    private ImageCubePopupMenu imageCubePopupMenu;
    private SbmtSpectrumWindowManager spectrumPanelManager;
    private Renderer renderer;
    ImageCubeCollection imageCubes;
    private int currentSlice = 0;

    public ImageCubeGenerationController(ImageSearchModel model,
            ImageCubeGenerationModel cubeModel,
            SbmtInfoWindowManager infoPanelManager,
            ImageCubePopupMenu imageCubePopupMenu,
            SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
    {
        super();
        this.model = model;
        this.cubeModel = cubeModel;
        this.infoPanelManager = infoPanelManager;
        this.imageCubePopupMenu = imageCubePopupMenu;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        this.panel = new ImageCubeGenerationPanel();
        setupPanel();
    }

    public void addPropertyChangeListner(PropertyChangeListener listener)
    {
        imageCubes.addPropertyChangeListener(listener);
    }

    private void setupPanel()
    {
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName());
        imageCubes = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        imageCubePopupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, panel);



//        imageCubesDisplayedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
//        imageCubesDisplayedList.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
//                imageCubesDisplayedListMousePressed(evt);
//            }
//            public void mouseReleased(java.awt.event.MouseEvent evt) {
//                imageCubesDisplayedListMouseReleased(evt);
//            }
//        });

        panel.getRemoveImageCubeButton().setText("Remove Image Cube");
        panel.getRemoveImageCubeButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeImageCubeButtonActionPerformed(evt);
            }
        });

        panel.getGenerateImageCubeButton().setText("Generate Image Cube");
        panel.getGenerateImageCubeButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateImageCubeButtonActionPerformed(evt);
            }
        });

        panel.getLayerSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                JTable imageList = panel.getImageCubeTable();

                int index = imageList.getSelectedRow();
                if (index == -1)
                {
                    setNumberOfBands(1);
                    return;
                }

                ImageCubeKey selectedValue = (ImageCubeKey)imageList.getValueAt(index, 5);
                String imagestring = selectedValue.fileNameString();
                String[]tokens = imagestring.split(",");
                String imagename = tokens[0].trim();

                JSlider source = (JSlider)e.getSource();
                currentSlice = (int)source.getValue();
                panel.getLayerValue().setText(Integer.toString(currentSlice));

                ImageCubeCollection images = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());

                Set<ImageCube> imageSet = images.getImages();
                for (ImageCube image : imageSet)
                {
                    ImageKey key = image.getKey();
                    String name = image.getImageName();

                    if(name.equals(imagename))
                    {
                       image.setCurrentSlice(currentSlice);
                       image.setDisplayedImageRange(null);
                       if (!source.getValueIsAdjusting())
                       {
                            image.loadFootprint();
                            image.firePropertyChange();
                       }
                       return; // twupy1: Only change band for a single image now even if multiple ones are highlighted since different cubical images can have different numbers of bands.
                    }
                }
            }
        });

//        jScrollPane5.setViewportView(imageCubesDisplayedList);
    }


    private void imageCubesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable imageCubesDisplayedList = panel.getImageCubeTable();
            int index = imageCubesDisplayedList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                if (!imageCubesDisplayedList.isRowSelected(index))
                {
                    imageCubesDisplayedList.clearSelection();
                    imageCubesDisplayedList.setRowSelectionInterval(index, index);
                }
//                ImageCubeKey colorKey = (ImageCubeKey)((TableModel)imageCubesDisplayedList.getModel()).get(index);
//                imageCubePopupMenu.setCurrentImage(colorKey);
//                imageCubePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected void generateImageCube(ActionEvent e)
    {
        ImageCollection images = (ImageCollection)model.getModelManager().getModel(model.getImageCollectionModelName());
        ImageCubeCollection collection = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        JTable imageCubesDisplayedList = panel.getImageCubeTable();
        ImageKey firstKey = null;
        boolean multipleFrustumVisible = false;

        List<ImageKey> selectedKeys = new ArrayList<ImageKey>();
        int[] selectedIndices = imageCubesDisplayedList.getSelectedRows();
        //System.out.println(Arrays.toString(selectedIndices));
        for (int selectedIndex : selectedIndices)
        {
            String name = model.getImageResults().get(selectedIndex).get(0);
            ImageKey selectedKey = model.createImageKey(name.substring(0, name.length()-4), model.getImageSourceOfLastQuery(), model.getInstrument());
//            System.out.println("Key: " + selectedKey.name);
            selectedKeys.add(selectedKey);
            PerspectiveImage selectedImage = (PerspectiveImage)images.getImage(selectedKey);
            if(selectedImage == null)
            {
                // We are in here because the image is not mapped, display an error message and exit
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
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
        }

        if(selectedKeys.size() == 0)
        {
            // We are in here because no images were selected by user
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "At least one image must be selected when generating an image cube.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if(firstKey == null)
        {
            // We are in here because no frustum was selected by user
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "At least one selected image must have its frustum showing when generating an image cube.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else
        {
            PerspectiveImage firstImage = (PerspectiveImage)images.getImage(firstKey);
            int mapColumnIndex = panel.getMapColumnIndex();
            int showFootprintColumnIndex = panel.getShowFootprintColumnIndex();
            int frusColumnIndex = panel.getFrusColumnIndex();
            int idColumnIndex = panel.getIdColumnIndex();
            int filenameColumnIndex = panel.getFilenameColumnIndex();
            int dateColumnIndex = panel.getDateColumnIndex();
            int bndrColumnIndex = panel.getBndrColumnIndex();

            ImageCubeKey imageCubeKey = new ImageCubeKey(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());
            try
            {
                TableModel listModel = imageCubesDisplayedList.getModel();
                if (!collection.containsImage(imageCubeKey))
                {
                    collection.addImage(imageCubeKey);
                    int i = imageCubesDisplayedList.getRowCount();
                    imageCubesDisplayedList.setValueAt(false, i, mapColumnIndex);
                    imageCubesDisplayedList.setValueAt(false, i, showFootprintColumnIndex);
                    imageCubesDisplayedList.setValueAt(false, i, frusColumnIndex);

//                    listModel.addElement(imageCubeKey);
//                    int idx = listModel.size()-1;
//                    imageCubesDisplayedList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = imageCubesDisplayedList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        imageCubesDisplayedList.scrollRectToVisible(cellBounds);

                    if(multipleFrustumVisible)
                    {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                                "More than one selected image has a visible frustum, image cube was generated using the first such frustum in order of appearance in the image list.",
                                "Notification",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else
                {
//                    int idx = listModel.indexOf(imageCubeKey);
//                    imageCubesDisplayedList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = imageCubesDisplayedList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        imageCubesDisplayedList.scrollRectToVisible(cellBounds);

                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                            "Image cube consisting of same images already exists, no new image cube was generated.",
                            "Notification",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (ImageCube.NoOverlapException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "The images you selected do not overlap.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void removeImageCube(ActionEvent e)
    {
        JTable imageCubesDisplayedList = panel.getImageCubeTable();
        int index = imageCubesDisplayedList.getSelectedRow();
        if (index >= 0)
        {
            ImageCubeKey imageCubeKey = (ImageCubeKey)((DefaultListModel)imageCubesDisplayedList.getModel()).remove(index);
            ImageCubeCollection collection = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
            collection.removeImage(imageCubeKey);

//            // Select the element in its place (unless it's the last one in which case
//            // select the previous one)
//            if (index >= imageCubesDisplayedList.getModel().getSize())
//                --index;
//            if (index >= 0)
//                imageCubesDisplayedList.setSelectionInterval(index, index);
        }
    }

//    public void propertyChange(PropertyChangeEvent evt)
//    {
//        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
//        {
//            PickEvent e = (PickEvent)evt.getNewValue();
//            Model imageModel = model.getModelManager().getModel(e.getPickedProp());
//            if (imageModel instanceof ImageCollection || imageModel instanceof PerspectiveImageBoundaryCollection)
//            {
//                String name = null;
//
//                if (imageModel instanceof ImageCollection)
//                    name = ((ImageCollection)imageModel).getImageName((vtkActor)e.getPickedProp());
//                else if (imageModel instanceof PerspectiveImageBoundaryCollection)
//                    name = ((PerspectiveImageBoundaryCollection)imageModel).getBoundaryName((vtkActor)e.getPickedProp());
//
//                int idx = -1;
//                int size = model.getImageResults().size();
//                for (int i=0; i<size; ++i)
//                {
//                    // Ignore extension (The name returned from getImageName or getBoundary
//                    // is the same as the first element of each list with the imageRawResults
//                    // but without the extension).
//                    String imagePath = model.getImageResults().get(i).get(0);
//                    imagePath = imagePath.substring(0, imagePath.lastIndexOf("."));
//                    if (name.equals(imagePath))
//                    {
//                        idx = i;
//                        break;
//                    }
//                }
//
////                if (idx >= 0)
////                {
////                    panel.getImageCubeTable().setRowSelectionInterval(idx, idx);
////                    Rectangle cellBounds = resultList.getCellRect(idx, 0, true);
////                    if (cellBounds != null)
////                        resultList.scrollRectToVisible(cellBounds);
////                }
//            }
//        }
//        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
//        {
//            JTable resultList = panel.getImageCubeTable();
//            resultList.getModel().removeTableModelListener(this);
//            int size = model.getImageResults().size();
//            ImageCollection images = (ImageCollection)model.getModelManager().getModel(model.getImageCollectionModelName());
//            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName());
//            for (int i=0; i<size; ++i)
//            {
//                String name = model.getImageResults().get(i).get(0);
////                ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), model.getImageSourceOfLastQuery(), model.getInstrument());
//                if (images.containsImage(key))
//                {
//                    resultList.setValueAt(true, i, mapColumnIndex);
//                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
//                    resultList.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
//                    resultList.setValueAt(image.isFrustumShowing(), i, frusColumnIndex);
//                }
//                else
//                {
//                    resultList.setValueAt(false, i, mapColumnIndex);
//                    resultList.setValueAt(false, i, showFootprintColumnIndex);
//                    resultList.setValueAt(false, i, frusColumnIndex);
//                }
//                if (boundaries.containsBoundary(key))
//                    resultList.setValueAt(true, i, bndrColumnIndex);
//                else
//                    resultList.setValueAt(false, i, bndrColumnIndex);
//            }
//            resultList.getModel().addTableModelListener(this);
//            // Repaint the list in case the boundary colors has changed
//            resultList.repaint();
//        }
//    }

    private void setNumberOfBands(int nbands)
    {
        // Select midband by default
        setNumberOfBands(nbands, (nbands-1)/2);
    }

    private void setNumberOfBands(int nbands, int activeBand)
    {
        cubeModel.setNbands(nbands);
        panel.setNBands(nbands);
        String activeBandString = Integer.toString(activeBand);
        panel.getLayerValue().setText(activeBandString);
        DefaultBoundedRangeModel monoBoundedRangeModel = new DefaultBoundedRangeModel(activeBand, 0, 0, nbands-1);
        panel.getLayerSlider().setModel(monoBoundedRangeModel);
    }

    private void removeImageCubeButtonActionPerformed(ActionEvent evt) {
        removeImageCube(evt);
    }

    private void generateImageCubeButtonActionPerformed(ActionEvent evt) {
        generateImageCube(evt);
    }

    public ImageCubeGenerationPanel getPanel()
    {
        return panel;
    }

//    private void imageCubesDisplayedListMousePressed(MouseEvent evt) {
//        imageCubesDisplayedListMaybeShowPopup(evt);
//    }
//
//    private void imageCubesDisplayedListMouseReleased(MouseEvent evt) {
//        imageCubesDisplayedListMaybeShowPopup(evt);
//    }

//    public JList getImageCubesDisplayedList()
//    {
//        return imageCubesDisplayedList;
//    }
//
//    @Override
//    public void valueChanged(ListSelectionEvent e)
//    {
//        if (!e.getValueIsAdjusting())
//        {
//            viewResultsGalleryButton.setEnabled(enableGallery && resultList.getSelectedRowCount() > 0);
//        }
//    }

}
