package edu.jhuapl.sbmt.gui.image.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.image.ColorImagePopupMenu;
import edu.jhuapl.sbmt.gui.image.model.ColorImageModel;
import edu.jhuapl.sbmt.gui.image.model.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.panels.ColorImageGenerationPanel;
import edu.jhuapl.sbmt.model.image.ColorImage;
import edu.jhuapl.sbmt.model.image.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;

import nom.tam.fits.FitsException;

public class ColorImageController
{
    private ImageSearchModel model;
    private ColorImageModel colorModel;
    private ColorImageGenerationPanel panel;
    private SbmtInfoWindowManager infoPanelManager;

    public ColorImageController(ImageSearchModel model, ColorImageModel colorModel, SbmtInfoWindowManager infoPanelManager)
    {
        this.model = model;
        this.colorModel = colorModel;
        panel = new ColorImageGenerationPanel();
        this.infoPanelManager = infoPanelManager;
        setupPanel();
    }

    private void setupPanel()
    {
        ColorImageCollection colorImages = (ColorImageCollection)model.getModelManager().getModel(colorModel.getColorImageCollectionModelName());
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
        ImageKey selectedKey = model.getSelectedImageKey();
        if (selectedKey != null)
        {
            String name = selectedKey.name;
            if (!selectedKey.band.equals("0"))
                name = selectedKey.band + ":" + name;
            panel.getRedLabel().setText(name);
            colorModel.setSelectedRedKey(selectedKey);
        }
//        int index = resultList.getSelectedRow();
//        if (index >= 0)
//        {
//            String image = imageRawResults.get(index).get(0);
//            String name = new File(image).getName();
//            image = image.substring(0,image.length()-4);
//            selectedRedKey = imageResultsTableView.createImageKey(image, sourceOfLastQuery, instrument);
//            if (!selectedRedKey.band.equals("0"))
//                name = selectedRedKey.band + ":" + name;
//            redLabel.setText(name);
//        }
    }

    private void greenButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageKey selectedKey = model.getSelectedImageKey();
        if (selectedKey != null)
        {
            String name = selectedKey.name;
            if (!selectedKey.band.equals("0"))
                name = selectedKey.band + ":" + name;
            panel.getGreenLabel().setText(name);
            colorModel.setSelectedGreenKey(selectedKey);
        }
//        int index = resultList.getSelectedRow();
//        if (index >= 0)
//        {
//            String image = imageRawResults.get(index).get(0);
//            String name = new File(image).getName();
//            image = image.substring(0,image.length()-4);
//            greenLabel.setText(name);
//            selectedGreenKey = imageResultsTableView.createImageKey(image, sourceOfLastQuery, instrument);
//            if (!selectedGreenKey.band.equals("0"))
//                name = selectedGreenKey.band + ":" + name;
//            greenLabel.setText(name);
//        }
    }

    private void blueButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageKey selectedKey = model.getSelectedImageKey();
        if (selectedKey != null)
        {
            String name = selectedKey.name;
            if (!selectedKey.band.equals("0"))
                name = selectedKey.band + ":" + name;
            panel.getBlueLabel().setText(name);
            colorModel.setSelectedBlueKey(selectedKey);
        }

//        int index = resultList.getSelectedRow();
//        if (index >= 0)
//        {
//            String image = imageRawResults.get(index).get(0);
//            String name = new File(image).getName();
//            image = image.substring(0,image.length()-4);
//            blueLabel.setText(name);
//            selectedBlueKey = imageResultsTableView.createImageKey(image, sourceOfLastQuery, instrument);
//            if (!selectedBlueKey.band.equals("0"))
//                name = selectedBlueKey.band + ":" + name;
//            blueLabel.setText(name);
//        }
    }

    protected void generateColorImage(ActionEvent e)
    {
        JTable colorImagesDisplayedList = panel.getDisplayedImageList();
        int mapColumnIndex = panel.getMapColumnIndex();
        int showFootprintColumnIndex = panel.getShowFootprintColumnIndex();
        int frusColumnIndex = panel.getFrusColumnIndex();
        int idColumnIndex = panel.getIdColumnIndex();
        int filenameColumnIndex = panel.getFilenameColumnIndex();
        int dateColumnIndex = panel.getDateColumnIndex();
        int bndrColumnIndex = panel.getBndrColumnIndex();
        int[] widths = new int[colorImagesDisplayedList.getColumnCount()];
        int[] columnsNeedingARenderer=new int[]{idColumnIndex,filenameColumnIndex,dateColumnIndex};

        ColorImageCollection collection = (ColorImageCollection)model.getModelManager().getModel(colorModel.getColorImageCollectionModelName());
        ImageKey selectedRedKey = colorModel.getSelectedRedKey();
        ImageKey selectedGreenKey = colorModel.getSelectedGreenKey();
        ImageKey selectedBlueKey = colorModel.getSelectedBlueKey();
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
                    colorImagesDisplayedList.setValueAt(false, i, mapColumnIndex);
                    colorImagesDisplayedList.setValueAt(false, i, showFootprintColumnIndex);
                    colorImagesDisplayedList.setValueAt(false, i, frusColumnIndex);
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
                ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).get(index);
                panel.getColorImagePopupMenu().setCurrentImage(colorKey);
                panel.getColorImagePopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public ColorImageGenerationPanel getPanel()
    {
        return panel;
    }

    public void setPanel(ColorImageGenerationPanel panel)
    {
        this.panel = panel;
    }

}
