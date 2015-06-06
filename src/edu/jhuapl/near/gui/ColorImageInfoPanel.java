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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import vtk.vtkImageActor;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkInteractorStyleImage;
import vtk.vtkPropCollection;
import vtk.vtkPropPicker;
import vtk.vtkTransform;

import edu.jhuapl.near.model.ColorImage;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.util.IntensityRange;


public class ColorImageInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private ColorImage image;
    private ColorImageCollection imageCollection;

    private vtkEnhancedRenderWindowPanel renWin;
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
    public ColorImageInfoPanel(
            final ColorImage image,
            ColorImageCollection imageCollection)
    {
        initComponents();

        this.image = image;
        this.imageCollection = imageCollection;

        renWin = new vtkEnhancedRenderWindowPanel();

        vtkInteractorStyleImage style = new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = (vtkImageData)image.getTexture().GetInput();
//        vtkImageData displayedImage = (vtkImageData)image.getImage();

        // Only allow contrast changing for images with exactly 1 channel
        if (image.getNumberOfComponentsOfOriginalImage() > 1)
        {
            redSlider.setEnabled(false);
            greenSlider.setEnabled(false);
            blueSlider.setEnabled(false);
            jLabel4.setEnabled(false);
            jLabel5.setEnabled(false);
            jLabel9.setEnabled(false);
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

        renWin.GetRenderer().AddActor(actor);

        renWin.setSize(550, 550);

        imagePicker = new vtkPropPicker();
        imagePicker.PickFromListOn();
        imagePicker.InitializePickList();
        vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        imagePicker.AddPickList(actor);
        renWin.addMouseListener(new ColorImageInfoPanel.MouseListener());

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


        pack();
        setVisible(true);

        initialized = true;
    }


    public Model getModel()
    {
        return image;
    }

    public Model getCollectionModel()
    {
        return imageCollection;
    }

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

        jPanel1 = new javax.swing.JPanel();
        blueSlider = new com.jidesoft.swing.RangeSlider();
        redSlider = new com.jidesoft.swing.RangeSlider();
        jLabel6 = new javax.swing.JLabel();
        jSlider6 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jSlider5 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        greenSlider = new com.jidesoft.swing.RangeSlider();
        jCheckBox2 = new javax.swing.JCheckBox();
        jSlider4 = new javax.swing.JSlider();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        interpolateCheckBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        table1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        leftSpinner = new javax.swing.JSpinner();
        bottomSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        rightSpinner = new javax.swing.JSpinner();
        topSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        blueSlider.setMajorTickSpacing(10);
        blueSlider.setMaximum(255);
        blueSlider.setPaintTicks(true);
        blueSlider.setHighValue(255);
        blueSlider.setLowValue(0);
        blueSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blueSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(blueSlider, gridBagConstraints);

        redSlider.setMajorTickSpacing(10);
        redSlider.setMaximum(255);
        redSlider.setPaintTicks(true);
        redSlider.setHighValue(255);
        redSlider.setLowValue(0);
        redSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(redSlider, gridBagConstraints);

        jLabel6.setText("Red:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(jLabel6, gridBagConstraints);

        jSlider6.setMajorTickSpacing(50);
        jSlider6.setMinorTickSpacing(10);
        jSlider6.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jSlider6, gridBagConstraints);

        jLabel3.setText("Blue");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel1.add(jLabel3, gridBagConstraints);

        jSlider5.setMajorTickSpacing(50);
        jSlider5.setMinorTickSpacing(10);
        jSlider5.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jSlider5, gridBagConstraints);

        jLabel4.setText("Mono");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel4, gridBagConstraints);

        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jCheckBox1, gridBagConstraints);

        jLabel2.setText("Green:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jLabel2, gridBagConstraints);

        greenSlider.setMajorTickSpacing(10);
        greenSlider.setMaximum(255);
        greenSlider.setPaintTicks(true);
        greenSlider.setHighValue(255);
        greenSlider.setLowValue(0);
        greenSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                greenSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(greenSlider, gridBagConstraints);

        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        jPanel1.add(jCheckBox2, gridBagConstraints);

        jSlider4.setMajorTickSpacing(50);
        jSlider4.setMinorTickSpacing(10);
        jSlider4.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        jPanel1.add(jSlider4, gridBagConstraints);

        jLabel9.setText("Contrast");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel9, gridBagConstraints);

        jLabel5.setText("Intensity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel5, gridBagConstraints);

        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jCheckBox3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(jPanel1, gridBagConstraints);

        interpolateCheckBox.setSelected(true);
        interpolateCheckBox.setText("Interpolate Image");
        interpolateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                interpolateCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        getContentPane().add(interpolateCheckBox, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(452, 200));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(452, 200));

        jScrollPane2.setViewportView(table1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

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
        jPanel2.add(leftSpinner, gridBagConstraints);

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
        jPanel2.add(bottomSpinner, gridBagConstraints);

        jLabel7.setText("Left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel7, gridBagConstraints);

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
        jPanel2.add(rightSpinner, gridBagConstraints);

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
        jPanel2.add(topSpinner, gridBagConstraints);

        jLabel8.setText("Bottom");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel8, gridBagConstraints);

        jLabel10.setText("Top");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel10, gridBagConstraints);

        jLabel11.setText("Right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void redSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {//GEN-FIRST:event_redSliderStateChanged
        if (redSlider.getValueIsAdjusting())
            return;

        adjustContrast();
    }//GEN-LAST:event_redSliderStateChanged

    private void greenSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {//GEN-FIRST:event_greenSliderStateChanged
        if (greenSlider.getValueIsAdjusting())
            return;

        adjustContrast();
    }//GEN-LAST:event_greenSliderStateChanged

    private void blueSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {//GEN-FIRST:event_blueSliderStateChanged
        if (blueSlider.getValueIsAdjusting())
            return;

        adjustContrast();
    }//GEN-LAST:event_blueSliderStateChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void interpolateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpolateCheckBoxActionPerformed
        image.setInterpolate(interpolateCheckBox.isSelected());
        actor.SetInterpolate(interpolateCheckBox.isSelected() ? 1 : 0);
        renWin.Render();
    }//GEN-LAST:event_interpolateCheckBoxActionPerformed

    private void leftSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void bottomSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bottomSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_bottomSpinnerStateChanged

    private void rightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void topSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_topSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_topSpinnerStateChanged

    private void adjustContrast()
    {
        int redLowVal = redSlider.getLowValue();
        int redHighVal = redSlider.getHighValue();
        int greenLowVal = greenSlider.getLowValue();
        int greenHighVal = greenSlider.getHighValue();
        int blueLowVal = blueSlider.getLowValue();
        int blueHighVal = blueSlider.getHighValue();

        image.setDisplayedImageRange(
                new IntensityRange(redLowVal, redHighVal),
                new IntensityRange(greenLowVal, greenHighVal),
                new IntensityRange(blueLowVal, blueHighVal));

        image.updateImageMask();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jidesoft.swing.RangeSlider blueSlider;
    private javax.swing.JSpinner bottomSpinner;
    private com.jidesoft.swing.RangeSlider greenSlider;
    private javax.swing.JCheckBox interpolateCheckBox;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider jSlider4;
    private javax.swing.JSlider jSlider5;
    private javax.swing.JSlider jSlider6;
    private javax.swing.JSpinner leftSpinner;
    private com.jidesoft.swing.RangeSlider redSlider;
    private javax.swing.JSpinner rightSpinner;
    private javax.swing.JTable table1;
    private javax.swing.JSpinner topSpinner;
    // End of variables declaration//GEN-END:variables
}
