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
import vtk.vtkTransform;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.util.IntensityRange;

/**
 *
 * @author eli
 */
public class ImageInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private vtkEnhancedRenderWindowPanel renWin;
    private Image image;
    private ImageCollection imageCollection;
    private ImageBoundaryCollection imageBoundaryCollection;
    private vtkImageActor actor;
    private vtkImageReslice reslice;

    /** Creates new form ImageInfoPanel2 */
    public ImageInfoPanel(
            final Image image,
            ImageCollection imageCollection,
            ImageBoundaryCollection imageBoundaryCollection)
    {
        initComponents();

        this.image = image;
        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;

        renWin = new vtkEnhancedRenderWindowPanel();

        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = image.getDisplayedImage();

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
        setTitle("Image " + name + " Properties");

        pack();
        setVisible(true);
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
        {
            public void actionPerformed(ActionEvent e)
            {
                renWin.saveToFile();
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
            new ImagePopupMenu(imageCollection, imageBoundaryCollection, null, null, this);

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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        slider = new com.jidesoft.swing.RangeSlider();
        jLabel1 = new javax.swing.JLabel();
        interpolateCheckBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

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
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        getContentPane().add(interpolateCheckBox, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(452, 200));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 200));

        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        pack();
    }//GEN-END:initComponents

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox interpolateCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.jidesoft.swing.RangeSlider slider;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
