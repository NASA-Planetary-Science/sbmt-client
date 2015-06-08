/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageInfoPanel2.java
 *
 * Created on May 30, 2011, 12:24:26 PM
 */
package edu.jhuapl.near.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.table.DefaultTableModel;

import vtk.vtkImageActor;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkInteractorStyleImage;
import vtk.vtkPropCollection;
import vtk.vtkPropPicker;
import vtk.vtkTransform;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.util.IntensityRange;


public class ImageInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private vtkEnhancedRenderWindowPanel renWin;
    private Image image;
    private ImageCollection imageCollection;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private vtkImageActor actor;
    private vtkImageReslice reslice;
    private vtkPropPicker imagePicker;
    private boolean initialized = false;

    private class MouseListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            renWin.lock();
            int pickSucceeded = imagePicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
            renWin.unlock();
            if (pickSucceeded == 1)
            {
                double[] p = imagePicker.GetPickPosition();
                // Note we reverse x and y so that the pixel is in the form the camera
                // position/orientation program expects.
                System.out.println(p[1] + " " + p[0]);
            }
        }
    }

    /** Creates new form ImageInfoPanel2 */
    public ImageInfoPanel(
            final Image image,
            ImageCollection imageCollection,
            PerspectiveImageBoundaryCollection imageBoundaryCollection)
    {
        initComponents();

        this.image = image;
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;

        renWin = new vtkEnhancedRenderWindowPanel();

        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = (vtkImageData)image.getTexture().GetInput();

        // Only allow contrast changing for images with exactly 1 channel
        if (image.getNumberOfComponentsOfOriginalImage() > 1)
        {
            slider.setEnabled(false);
            jLabel1.setEnabled(false);
        }

        int[] masking = image.getCurrentMask();
        leftSpinner.setValue(masking[0]);
        topSpinner.setValue(masking[1]);
        rightSpinner.setValue(masking[2]);
        bottomSpinner.setValue(masking[3]);

        double[] center = displayedImage.GetCenter();
        int[] dims = displayedImage.GetDimensions();

        // Rotate image by 90 degrees so it appears the same way as when you
        // use the Center in Image option.
        vtkTransform imageTransform = new vtkTransform();
        imageTransform.Translate(center[0], center[1], 0.0);
        imageTransform.RotateZ(-90.0);
        imageTransform.Translate(-center[1], -center[0], 0.0);

        reslice = new vtkImageReslice();
        reslice.SetInput(displayedImage);
        reslice.SetResliceTransform(imageTransform);
        reslice.SetInterpolationModeToNearestNeighbor();
        reslice.SetOutputSpacing(1.0, 1.0, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, dims[1]-1, 0, dims[0]-1, 0, 0);
        reslice.Update();

        actor = new vtkImageActor();
        actor.SetInput(reslice.GetOutput());
        actor.InterpolateOn();

// for testing backplane generation
//        {
//            int band = 10;
//            vtkImageData plane = new vtkImageData();
//            plane.DeepCopy(displayedImage);
//            float[] bp = image.generateBackplanes();
//            double minValue = Double.MAX_VALUE;
//            double maxValue = -Double.MAX_VALUE;
//            int pixelStart = image.index(0, 0, band);
//            int pixelEnd = image.index(0, 0, band+1);
//            for (int i=pixelStart; i<pixelEnd; ++i)
//            {
//                if (bp[i] == Image.PDS_NA) continue;
//                if (bp[i] < minValue) minValue = bp[i];
//                if (bp[i] > maxValue) maxValue = bp[i];
//            }
//            System.out.println("min band " + band + " : " + minValue);
//            System.out.println("max band " + band + " : " + maxValue);
//            for (int i=0; i<image.getImageHeight(); ++i)
//                for (int j=0; j<image.getImageWidth(); ++j)
//                {
//                    double v = bp[image.index(j, i, band)];
//                    if (v == Image.PDS_NA)
//                        v = minValue;
//                    else
//                        v = (v-minValue) * 255.0 / (maxValue - minValue);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 0, v);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 1, v);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 2, v);
//                }
//            actor.SetInput(plane);
//        }

        renWin.GetRenderer().AddActor(actor);

        renWin.setSize(550, 550);

        imagePicker = new vtkPropPicker();
        imagePicker.PickFromListOn();
        imagePicker.InitializePickList();
        vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        imagePicker.AddPickList(actor);
        renWin.addMouseListener(new MouseListener());

        // Trying to add a vtkEnhancedRenderWindowPanel in the netbeans gui
        // does not seem to work so instead add it here.
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(renWin, gridBagConstraints);


        // Add a text box for showing information about the image
        String[] columnNames = {"Property", "Value"};

        LinkedHashMap<String, String> properties = null;
        Object[][] data = { {"", ""} };
        try
        {
            properties = image.getProperties();
            int size = properties.size();
            data = new Object[size][2];

            int i=0;
            for (String key : properties.keySet())
            {
                data[i][0] = key;
                data[i][1] = properties.get(key);

                ++i;
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }


        DefaultTableModel model = new DefaultTableModel(data, columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        table.setModel(model);

        createMenus();

        // Finally make the frame visible
        String name = new File(image.getImageName()).getName();
        if (image instanceof PerspectiveImage)
        {
            PerspectiveImage pimage = (PerspectiveImage)image;
            int depth = pimage.getImageDepth();
            if (depth > 1)
            {
                String band = pimage.getCurrentBand();
                name = band + ":" + name;
            }
        }
        setTitle("Image " + name + " Properties");

        pack();
        setVisible(true);

        initialized = true;
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
        {
            public void actionPerformed(ActionEvent e)
            {
                File file = ImageFileChooser.showSaveDialog(renWin, "Export to Image...");
                renWin.saveToFile(file);
            }
        });
        fileMenu.add(mi);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        /**
         * The following is a bit of a hack. We want to reuse the PopupMenu
         * class, but instead of having a right-click popup menu, we want instead to use
         * it as an actual menu in a menu bar. Therefore we simply grab the menu items
         * from that class and put these in our new JMenu.
         */
        ImagePopupMenu imagesPopupMenu =
            new ImagePopupMenu(imageCollection, imageBoundaryCollection, null, null, null, this);

        imagesPopupMenu.setCurrentImage(image.getKey());

        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = imagesPopupMenu.getComponents();
        for (Component item : components)
        {
            if (item instanceof JMenuItem)
            {
                // Do not show the "Show Image" option since that creates problems
                // since it's supposed to close this window also.
                if (!(((JMenuItem)item).getAction() instanceof ImagePopupMenu.MapImageAction))
                    menu.add(item);
            }
        }

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    public Model getModel()
    {
        return image;
    }

    public Model getCollectionModel()
    {
        return imageCollection;
    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;
        renWin.Render();
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

        slider = new com.jidesoft.swing.RangeSlider();
        jLabel1 = new javax.swing.JLabel();
        interpolateCheckBox = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        leftSpinner = new javax.swing.JSpinner();
        bottomSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        rightSpinner = new javax.swing.JSpinner();
        topSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        slider.setMajorTickSpacing(10);
        slider.setMaximum(255);
        slider.setPaintTicks(true);
        slider.setHighValue(255);
        slider.setLowValue(0);
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        getContentPane().add(slider, gridBagConstraints);

        jLabel1.setText("Contrast:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        interpolateCheckBox.setSelected(true);
        interpolateCheckBox.setText("Interpolate Image");
        interpolateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpolateCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        getContentPane().add(interpolateCheckBox, gridBagConstraints);

        jLabel7.setText("Crop Image:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 0);
        getContentPane().add(jLabel7, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(452, 200));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 200));

        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        leftSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        leftSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        leftSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                leftSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(leftSpinner, gridBagConstraints);

        bottomSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        bottomSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        bottomSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bottomSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(bottomSpinner, gridBagConstraints);

        jLabel3.setText("Left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel3, gridBagConstraints);

        rightSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        rightSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        rightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rightSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(rightSpinner, gridBagConstraints);

        topSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        topSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        topSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                topSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel1.add(topSpinner, gridBagConstraints);

        jLabel6.setText("Bottom");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel4.setText("Top");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_sliderStateChanged
    {//GEN-HEADEREND:event_sliderStateChanged
        if (slider.getValueIsAdjusting())
            return;

        int lowVal = slider.getLowValue();
        int highVal = slider.getHighValue();
        if (image != null)
            image.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
    }//GEN-LAST:event_sliderStateChanged

    private void interpolateCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_interpolateCheckBoxActionPerformed
    {//GEN-HEADEREND:event_interpolateCheckBoxActionPerformed
        image.setInterpolate(interpolateCheckBox.isSelected());
        actor.SetInterpolate(interpolateCheckBox.isSelected() ? 1 : 0);
        renWin.Render();
    }//GEN-LAST:event_interpolateCheckBoxActionPerformed

    private void leftSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void topSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_topSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_topSpinnerStateChanged

    private void rightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void bottomSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bottomSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_bottomSpinnerStateChanged

    private void croppingChanged()
    {
        if (!initialized)
            return;

        Integer top = (Integer) leftSpinner.getValue();
        Integer right = (Integer) topSpinner.getValue();
        Integer bottom = (Integer) rightSpinner.getValue();
        Integer left = (Integer) bottomSpinner.getValue();

        int[] masking = {top, right, bottom, left};

        image.setCurrentMask(masking);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner bottomSpinner;
    private javax.swing.JCheckBox interpolateCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner leftSpinner;
    private javax.swing.JSpinner rightSpinner;
    private com.jidesoft.swing.RangeSlider slider;
    private javax.swing.JTable table;
    private javax.swing.JSpinner topSpinner;
    // End of variables declaration//GEN-END:variables
}
