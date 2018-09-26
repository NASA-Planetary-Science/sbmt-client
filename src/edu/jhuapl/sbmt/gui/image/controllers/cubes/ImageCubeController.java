package edu.jhuapl.sbmt.gui.image.controllers.cubes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.gui.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchResultsListener;
import edu.jhuapl.sbmt.gui.image.model.cubes.ImageCubeModel;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubeGenerationPanel;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCube;
import edu.jhuapl.sbmt.model.image.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import nom.tam.fits.FitsException;

public class ImageCubeController
{
    protected ImageSearchModel model;
    protected ImageCubeModel cubeModel;
    protected ImageCubeGenerationPanel panel;
    private SbmtInfoWindowManager infoPanelManager;
    private ImageCubePopupMenu imageCubePopupMenu;
    private SbmtSpectrumWindowManager spectrumPanelManager;
    private Renderer renderer;
    ImageCubeCollection imageCubes;
    private StringRenderer stringRenderer;
    private ImageCubeResultsTableModeListener tableModelListener;
    private ImageCubeResultsPropertyChangeListener propertyChangeListener;
    private PerspectiveImageBoundaryCollection boundaries;

    public ImageCubeController(ImageSearchModel model,
            ImageCubeModel cubeModel,
            SbmtInfoWindowManager infoPanelManager,
            ImageCubePopupMenu imageCubePopupMenu,
            SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
    {
        super();
        this.model = model;
        model.addResultsChangedListener(new ImageSearchResultsListener()
        {
            @Override
            public void resultsChanged(List<List<String>> results)
            {
                stringRenderer.setImageRawResults(results);
            }
        });
        this.cubeModel = cubeModel;
        this.infoPanelManager = infoPanelManager;
        this.imageCubePopupMenu = imageCubePopupMenu;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        this.panel = new ImageCubeGenerationPanel();
        propertyChangeListener = new ImageCubeResultsPropertyChangeListener();
        tableModelListener = new ImageCubeResultsTableModeListener();
        setupPanel();
    }

    public void addPropertyChangeListner(PropertyChangeListener listener)
    {
        imageCubes.addPropertyChangeListener(listener);
    }

    protected void setupPanel()
    {
        boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName());
        imageCubes = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        imageCubePopupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, panel);

        panel.getRemoveImageCubeButton().setText("Remove Image Cube");
        panel.getRemoveImageCubeButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeImageCubeButtonActionPerformed(evt);
            }
        });

        panel.getGenerateImageCubeButton().setText("Generate Image Cube");
        panel.getGenerateImageCubeButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generateImageCubeButtonActionPerformed(evt);
            }
        });

        String[] columnNames = new String[]{
                "Map",
                "Show",
                "Filename",
        };
        panel.getImageCubeTable().setModel(new ImageCubesTableModel(new Object[0][3], columnNames));
        stringRenderer = new StringRenderer(model, model.getImageResults());
        panel.getImageCubeTable().setDefaultRenderer(String.class, stringRenderer);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getMapColumnIndex()).setPreferredWidth(31);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setPreferredWidth(35);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getMapColumnIndex()).setResizable(true);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setResizable(true);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getFilenameColumnIndex()).setPreferredWidth(250);
        panel.getImageCubeTable().getModel().addTableModelListener(tableModelListener);
        imageCubes.addPropertyChangeListener(propertyChangeListener);

        panel.getImageCubeTable().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                imageCubesDisplayedListMaybeShowPopup(e);
//                panel.getImageCubeTable().getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }

            public void mouseReleased(MouseEvent e)
            {
                imageCubesDisplayedListMaybeShowPopup(e);
//                panel.getImageCubeTable().getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }
        });


        panel.getImageCubeTable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
//                    imageSearchModel.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
//                    imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
                }
            }
        });
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
                ImageCube image = imageCubes.getLoadedImages().get(index);
                ImageCubeKey imageCubeKey = image.getImageCubeKey();
                imageCubePopupMenu.setCurrentImage(imageCubeKey);
                imageCubePopupMenu.show(e.getComponent(), e.getX(), e.getY());
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

        List<ImageKey> selectedKeys = new ArrayList<>();
        for (ImageKey key : model.getSelectedImageKeys()) { selectedKeys.add(key); }
        for (ImageKey selectedKey : selectedKeys)
        {
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
            int filenameColumnIndex = panel.getFilenameColumnIndex();
            ImageCubeKey imageCubeKey = new ImageCubeKey(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());
            try
            {
                DefaultTableModel tableModel = (DefaultTableModel)imageCubesDisplayedList.getModel();
                if (!collection.containsImage(imageCubeKey))
                {
                    collection.addImage(imageCubeKey);
                    tableModel.setRowCount(imageCubesDisplayedList.getRowCount()+1);
                    int i = imageCubesDisplayedList.getRowCount();
                    imageCubesDisplayedList.setValueAt(true, i-1, mapColumnIndex);
                    imageCubesDisplayedList.setValueAt(true, i-1, showFootprintColumnIndex);
                    imageCubesDisplayedList.setValueAt(imageCubeKey.toString(), i-1, filenameColumnIndex);

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
                        "Cube Generation: The images you selected do not overlap.",
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
        }
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

    public class ImageCubesTableModel extends DefaultTableModel
    {
        public ImageCubesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == panel.getShowFootprintColumnIndex() /*|| column == panel.getFrusColumnIndex()*/)
            {
                return (Boolean)getValueAt(row, 0);
            }
            else
            {
                return column == panel.getMapColumnIndex() /*|| column == panel.getBndrColumnIndex()*/;
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= panel.getShowFootprintColumnIndex())
                return Boolean.class;
            else
                return String.class;
        }
    }

    class ImageCubeResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            if (e.getColumn() == panel.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                ImageCube image = imageCubes.getLoadedImages().get(row);
                ImageCubeKey key = image.getImageCubeKey();

                if ((Boolean)panel.getImageCubeTable().getValueAt(row, panel.getMapColumnIndex()))
                {
                    try
                    {
                        cubeModel.loadImage(key);
                    }
                    catch (FitsException | IOException | NoOverlapException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                else
                {
                    cubeModel.unloadImage(key);
                    panel.getImageCubeTable().getModel().setValueAt(false, 0, panel.getShowFootprintColumnIndex());
//                    panel.getImageCubeTable().getModel().setValueAt(false, 0, panel.getBndrColumnIndex());
                    model.getRenderer().setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                boolean visible = (Boolean)panel.getImageCubeTable().getValueAt(row, panel.getShowFootprintColumnIndex());
                if (imageCubes.getLoadedImages().size() == 0) return;
                imageCubes.getLoadedImages().get(row).setVisible(visible);
            }
        }
    }

    class ImageCubeResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = panel.getImageCubeTable();
                if (resultList.getRowCount() == 0) return;
                resultList.getModel().removeTableModelListener(tableModelListener);
                Set<ImageCube> imageCubeSet = imageCubes.getImages();

                int i=0;
                for (ImageCube image : imageCubeSet)
                {
                    ImageCubeKey key = image.getImageCubeKey();
                    if (imageCubes.containsImage(key))
                    {
                        resultList.setValueAt(true, i, panel.getMapColumnIndex());
                        resultList.setValueAt(image.isVisible(), i, panel.getShowFootprintColumnIndex());
                    }
                    else
                    {
                        resultList.setValueAt(false, i, panel.getMapColumnIndex());
                        resultList.setValueAt(false, i, panel.getShowFootprintColumnIndex());
                    }
                    i++;
                }
                resultList.getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }

}
