package edu.jhuapl.sbmt.gui.image.controllers.color;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchResultsListener;
import edu.jhuapl.sbmt.gui.image.model.color.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.color.ColorImageGenerationPanel;
import edu.jhuapl.sbmt.gui.image.ui.color.ColorImagePopupMenu;
import edu.jhuapl.sbmt.model.image.ColorImage;
import edu.jhuapl.sbmt.model.image.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.model.image.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;

import nom.tam.fits.FitsException;

public class ColorImageController
{
    private ImageSearchModel model;
    private ColorImageModel colorModel;
    private ColorImageGenerationPanel panel;
    private SbmtInfoWindowManager infoPanelManager;
    private StringRenderer stringRenderer;
    private PerspectiveImageBoundaryCollection boundaries;
    private ColorImageResultsTableModeListener tableModelListener;
    private ColorImageResultsPropertyChangeListener propertyChangeListener;
    private ColorImageCollection colorImages;

    public ColorImageController(ImageSearchModel model, ColorImageModel colorModel, SbmtInfoWindowManager infoPanelManager)
    {
        this.model = model;
        model.addResultsChangedListener(new ImageSearchResultsListener()
        {

            @Override
            public void resultsChanged(List<List<String>> results)
            {
                stringRenderer.setImageRawResults(results);
            }
        });
        this.colorModel = colorModel;
        colorImages = (ColorImageCollection)model.getModelManager().getModel(colorModel.getColorImageCollectionModelName());
        colorModel.setImages(colorImages);
        panel = new ColorImageGenerationPanel();
        this.infoPanelManager = infoPanelManager;
        propertyChangeListener = new ColorImageResultsPropertyChangeListener();
        tableModelListener = new ColorImageResultsTableModeListener();
        setupPanel();



    }

    private void setupPanel()
    {
        boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName());

        ColorImagePopupMenu colorImagePopupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager, model.getModelManager(), panel);
        panel.setColorImagePopupMenu(colorImagePopupMenu);

//        panel.getColorImagesDisplayedList().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
//        panel.getColorImagesDisplayedList().addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
//                colorImagesDisplayedListMousePressed(evt);
//            }
//            public void mouseReleased(java.awt.event.MouseEvent evt) {
//                colorImagesDisplayedListMouseReleased(evt);
//            }
//        });

        panel.getRemoveColorImageButton().setText("Remove Color Image");
        panel.getRemoveColorImageButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColorImageButtonActionPerformed(evt);
            }
        });

        panel.getGenerateColorImageButton().setText("Generate Color Image");
        panel.getGenerateColorImageButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateColorImageButtonActionPerformed(evt);
            }
        });

        JButton redButton = panel.getRedButton();
        redButton.setBackground(new java.awt.Color(255, 0, 0));
        redButton.setText("Red");
        redButton.setToolTipText("Select an image from the list above and then press this button");
        redButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redButtonActionPerformed(evt);
            }
        });

        JButton greenButton = panel.getGreenButton();
        greenButton.setBackground(new java.awt.Color(0, 255, 0));
        greenButton.setText("Green");
        greenButton.setToolTipText("Select an image from the list above and then press this button");
        greenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenButtonActionPerformed(evt);
            }
        });

        JButton blueButton = panel.getBlueButton();
        blueButton.setBackground(new java.awt.Color(0, 0, 255));
        blueButton.setText("Blue");
        blueButton.setToolTipText("Select an image from the list above and then press this button");
        blueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueButtonActionPerformed(evt);
            }
        });

//        jScrollPane3.setViewportView(colorImagesDisplayedList);

        String[] columnNames = new String[]{
                "Map",
                "Show",
//                "Frus",
                "Bndr",
//                "Id",
                "Filename",
//                "Date"
        };

        panel.getDisplayedImageList().setModel(new ColorImageTableModel(new Object[0][4], columnNames));
        stringRenderer = new StringRenderer(model, model.getImageResults());
        panel.getDisplayedImageList().setDefaultRenderer(String.class, stringRenderer);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getMapColumnIndex()).setPreferredWidth(31);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setPreferredWidth(35);
//        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getFrusColumnIndex()).setPreferredWidth(31);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getBndrColumnIndex()).setPreferredWidth(31);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getMapColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setResizable(true);
//        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getFrusColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getBndrColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getFilenameColumnIndex()).setPreferredWidth(350);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getFilenameColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getModel().addTableModelListener(tableModelListener);
        colorImages.addPropertyChangeListener(propertyChangeListener);

        panel.getDisplayedImageList().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                colorImagesDisplayedListMaybeShowPopup(e);
//                panel.getImageCubeTable().getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }

            public void mouseReleased(MouseEvent e)
            {
                colorImagesDisplayedListMaybeShowPopup(e);
//                panel.getImageCubeTable().getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }
        });


        panel.getDisplayedImageList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
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

    private void generateColorImageButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        generateColorImage(evt);
    }

    private void removeColorImageButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        removeColorImage(evt);
    }

    private void colorImagesDisplayedListMousePressed(java.awt.event.MouseEvent evt)
    {
        colorImagesDisplayedListMaybeShowPopup(evt);
    }

    private void colorImagesDisplayedListMouseReleased(java.awt.event.MouseEvent evt)
    {
        colorImagesDisplayedListMaybeShowPopup(evt);
    }

    private void redButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageKey selectedKey = model.getSelectedImageKeys()[0];
        if (selectedKey != null)
        {
            String name = selectedKey.name;
            if (!selectedKey.band.equals("0"))
                name = selectedKey.band + ":" + name;
            panel.getRedLabel().setText(new File(name).getName());
            colorModel.setSelectedRedKey(selectedKey);
        }
    }

    private void greenButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageKey selectedKey = model.getSelectedImageKeys()[0];
        if (selectedKey != null)
        {
            String name = selectedKey.name;
            if (!selectedKey.band.equals("0"))
                name = selectedKey.band + ":" + name;
            panel.getGreenLabel().setText(new File(name).getName());
            colorModel.setSelectedGreenKey(selectedKey);
        }
    }

    private void blueButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageKey selectedKey = model.getSelectedImageKeys()[0];
        if (selectedKey != null)
        {
            String name = selectedKey.name;
            if (!selectedKey.band.equals("0"))
                name = selectedKey.band + ":" + name;
            panel.getBlueLabel().setText(new File(name).getName());
            colorModel.setSelectedBlueKey(selectedKey);
        }
    }

    protected void generateColorImage(ActionEvent e)
    {
        JTable colorImagesDisplayedList = panel.getDisplayedImageList();
        int mapColumnIndex = panel.getMapColumnIndex();
        int showFootprintColumnIndex = panel.getShowFootprintColumnIndex();
//        int frusColumnIndex = panel.getFrusColumnIndex();
//        int idColumnIndex = panel.getIdColumnIndex();
        int filenameColumnIndex = panel.getFilenameColumnIndex();
//        int dateColumnIndex = panel.getDateColumnIndex();
        int bndrColumnIndex = panel.getBndrColumnIndex();
        int[] widths = new int[colorImagesDisplayedList.getColumnCount()];
        int[] columnsNeedingARenderer=new int[]{/*idColumnIndex,*/filenameColumnIndex/*,dateColumnIndex*/};

        ColorImageCollection collection = (ColorImageCollection)model.getModelManager().getModel(colorModel.getColorImageCollectionModelName());
        ImageKey selectedRedKey = colorModel.getSelectedRedKey();
        ImageKey selectedGreenKey = colorModel.getSelectedGreenKey();
        ImageKey selectedBlueKey = colorModel.getSelectedBlueKey();
        System.out.println("ColorImageController: generateColorImage: setting row count to " + (colorImagesDisplayedList.getRowCount()+1));
        ((DefaultTableModel)colorImagesDisplayedList.getModel()).setRowCount(colorImagesDisplayedList.getRowCount()+1);

        if (selectedRedKey != null && selectedGreenKey != null && selectedBlueKey != null)
        {
            ColorImageKey colorKey = new ColorImageKey(selectedRedKey, selectedGreenKey, selectedBlueKey);
            try
            {
//                TableModel tableModel = colorImagesDisplayedList.getModel();
//                if (collection.containsImage(colorKey))
//                {
//                    colorImagesDisplayedList.setValueAt(true, i, mapColumnIndex);
//                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
//                    colorImagesDisplayedList.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
//                    colorImagesDisplayedList.setValueAt(!image.isFrustumShowing(), i, frusColumnIndex);
//                }
//                else
//                {
                    collection.addImage(colorKey);
                    int i = colorImagesDisplayedList.getRowCount();
                    System.out.println(
                            "ColorImageController: generateColorImage: row count " + i);
                    colorImagesDisplayedList.setValueAt(true, i-1, mapColumnIndex);
                    colorImagesDisplayedList.setValueAt(true, i-1, showFootprintColumnIndex);
                    colorImagesDisplayedList.setValueAt(true, i-1, bndrColumnIndex);
//                    colorImagesDisplayedList.setValueAt(aValue, i-1, idColumnIndex);
                    colorImagesDisplayedList.setValueAt(colorKey.toString(), i-1, filenameColumnIndex);
//                    colorImagesDisplayedList.setValueAt(aValue, i-1, dateColumnIndex);
//                }
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
            catch (ColorImage.NoOverlapException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        "The images you selected do not overlap.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    protected void removeColorImage(ActionEvent e)
    {
        JTable colorImagesDisplayedList = panel.getDisplayedImageList();
        int index = colorImagesDisplayedList.getSelectedRow();
        if (index >= 0)
        {
            ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).remove(index);
            ColorImageCollection imageCollection = (ColorImageCollection)model.getModelManager().getModel(colorModel.getColorImageCollectionModelName());
            imageCollection.removeImage(colorKey);

//            // Select the element in its place (unless it's the last one in which case
//            // select the previous one)
//            if (index >= colorImagesDisplayedList.getModel().getSize())
//                --index;
//            if (index >= 0)
//                colorImagesDisplayedList.setSelectionInterval(index, index);
        }
    }


    private void colorImagesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable colorImagesDisplayedList = panel.getDisplayedImageList();
            int index = colorImagesDisplayedList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                if (!colorImagesDisplayedList.isRowSelected(index))
                {
                    colorImagesDisplayedList.clearSelection();
                    colorImagesDisplayedList.setRowSelectionInterval(index, index);
                }
//                ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).get(index);
                ColorImage image = colorImages.getLoadedImages().get(index);
                ColorImageKey colorKey = image.getColorKey();
                panel.getColorImagePopupMenu().setCurrentImage(colorKey);
                panel.getColorImagePopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public ColorImageGenerationPanel getPanel()
    {
        return panel;
    }

//    public void setPanel(ColorImageGenerationPanel panel)
//    {
//        this.panel = panel;
//    }

    public class ColorImageTableModel extends DefaultTableModel
    {
        public ColorImageTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == panel.getShowFootprintColumnIndex() /*|| column == panel.getFrusColumnIndex()*/)
            {
//                String name = model.getImageResults().get(row).get(0);
//                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), model.getImageSourceOfLastQuery(), model.getInstrument());
//                ColorImageKey key = (ColorImageKey)((DefaultListModel)panel.getDisplayedImageList().getModel()).get(row);
//                return colorImages.containsImage(key);
                return (Boolean)getValueAt(row, 0);
            }
            else
            {
                return column == panel.getMapColumnIndex() || column == panel.getBndrColumnIndex();
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= panel.getBndrColumnIndex())
                return Boolean.class;
            else
                return String.class;
        }
    }

    class ColorImageResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            if (e.getColumn() == panel.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                ColorImage image = colorImages.getLoadedImages().get(row);
                ColorImageKey key = image.getColorKey();

                if ((Boolean)panel.getDisplayedImageList().getValueAt(row, panel.getMapColumnIndex()))
                    try
                    {
                        colorModel.loadImage(key);
                    }
                    catch (FitsException | IOException | NoOverlapException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                else
                {
                    colorModel.unloadImage(key);
                    panel.getDisplayedImageList().getModel().setValueAt(false, 0, panel.getShowFootprintColumnIndex());
                    panel.getDisplayedImageList().getModel().setValueAt(false, 0, panel.getBndrColumnIndex());
                    model.getRenderer().setLighting(LightingType.LIGHT_KIT);
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                boolean visible = (Boolean)panel.getDisplayedImageList().getValueAt(row, panel.getShowFootprintColumnIndex());
                colorImages.getLoadedImages().get(row).setVisible(visible);
            }
//            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
//            {
//                int row = e.getFirstRow();
//                String name = imageRawResults.get(row).get(0);
//                ImageKey key = model.createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, model.getInstrument());
//                ImageCollection images = (ImageCollection)modelManager.getModel(model.getImageCollectionModelName());
//                if (images.containsImage(key))
//                {
//                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
//                    image.setShowFrustum(!image.isFrustumShowing());
//                }
//            }
            else if (e.getColumn() == panel.getBndrColumnIndex())
            {
                int row = e.getFirstRow();
                ColorImage image = colorImages.getLoadedImages().get(row);
                ImageKey key = image.getColorKey().getImageKey();
                try
                {
                    if (!boundaries.containsBoundary(key))
                        boundaries.addBoundary(key);
                    else
                        boundaries.removeBoundary(key);
                }
                catch (Exception e1) {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                            "There was an error mapping the boundary.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                }
            }

        }
    }

    class ColorImageResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = panel.getDisplayedImageList();
                if (resultList.getRowCount() == 0) return;
                resultList.getModel().removeTableModelListener(tableModelListener);
                Set<ColorImage> colorImageSet = colorImages.getImages();

                int size = colorImageSet.size();
//                for (int i=0; i<size; ++i)
                int i=0;
                for (ColorImage image : colorImageSet)
                {
//                    String name = imageRawResults.get(i).get(0);
                    String name = image.getImageName();
                    ColorImageKey key = image.getColorKey();
//                    ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//                    ImageKey key = model.createImageKey(name.substring(0, name.length()-4), model.getImageSourceOfLastQuery(), model.getInstrument());
                    if (colorImages.containsImage(key))
                    {
                        resultList.setValueAt(true, i, panel.getMapColumnIndex());
//                        PerspectiveImage image = (PerspectiveImage) model.getImageCollection().getImage(key);
                        resultList.setValueAt(image.isVisible(), i, panel.getShowFootprintColumnIndex());
//                        resultList.setValueAt(image.isFrustumShowing(), i, panel.getFrusColumnIndex());
                    }
                    else
                    {
                        resultList.setValueAt(false, i, panel.getMapColumnIndex());
                        resultList.setValueAt(false, i, panel.getShowFootprintColumnIndex());
//                        resultList.setValueAt(false, i, panel.getFrusColumnIndex());
                    }
                    //TODO fix this - do we track the color image boundaries separately? - DON'T THINK SO
//                    if (boundaries.containsBoundary(key))
//                        resultList.setValueAt(true, i, panel.getBndrColumnIndex());
//                    else
//                        resultList.setValueAt(false, i, panel.getBndrColumnIndex());
                    i++;
                }
                resultList.getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }

}
