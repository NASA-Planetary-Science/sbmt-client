/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SettingsDialog.java
 *
 * Created on Mar 27, 2012, 9:37:47 PM
 */
package edu.jhuapl.near.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import edu.jhuapl.near.gui.Renderer.InteractorStyleType;
import edu.jhuapl.near.gui.Renderer.LightingType;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Preferences;

/**
 *
 * @author eli
 */
public class PreferencesDialog extends javax.swing.JDialog {

    private ViewerManager viewerManager;
    private static final double MAX_TOLERANCE = 0.01;

    /** Creates new form SettingsDialog */
    public PreferencesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("Preferences");
    }

    public void setViewerManager(ViewerManager viewerManager)
    {
        this.viewerManager = viewerManager;
    }

    public void setVisible(boolean b) {
        if (b)
        {
            Renderer renderer = viewerManager.getCurrentViewer().getRenderer();
            if (renderer.getLighting() == Renderer.LightingType.LIGHT_KIT)
                lightKitRadioButton.setSelected(true);
            else if (renderer.getLighting() == Renderer.LightingType.HEADLIGHT)
                headlightRadioButton.setSelected(true);
            else
                fixedLightRadioButton.setSelected(true);
            intensitySpinner.setValue(renderer.getLightIntensity());
            LatLon position = renderer.getFixedLightPosition();
            latitudeTextField.setValue(position.lat);
            longitudeTextField.setValue(position.lon);
            distanceTextField.setValue(position.rad);
            showAxesCheckBox.setSelected(renderer.getShowOrientationAxes());
            interactiveCheckBox.setSelected(renderer.getOrientationAxesInteractive());

            if (renderer.getDefaultInteractorStyleType() == Renderer.InteractorStyleType.JOYSTICK_CAMERA)
                joystickRadioButton.setSelected(true);
            else
                trackballRadioButton.setSelected(true);

            SmallBodyModel smallBodyModel =
                    viewerManager.getCurrentViewer().getModelManager().getSmallBodyModel();
            showScaleBarCheckBox.setSelected(smallBodyModel.getShowScaleBar());

            PickManager pickManager = viewerManager.getCurrentViewer().getPickManager();
            int value = getSliderValueFromTolerance(pickManager.getPickTolerance());
            pickToleranceSlider.setValue(value);

            updateEnabledItems();
        }

        super.setVisible(b);
    }

    private void updateEnabledItems()
    {
        boolean enabled = headlightRadioButton.isSelected() || fixedLightRadioButton.isSelected();
        intensityLabel.setEnabled(enabled);
        intensitySpinner.setEnabled(enabled);
        enabled = fixedLightRadioButton.isSelected();
        latitudeLabel.setEnabled(enabled);
        latitudeTextField.setEnabled(enabled);
        longitudeLabel.setEnabled(enabled);
        longitudeTextField.setEnabled(enabled);
        distanceLabel.setEnabled(enabled);
        distanceTextField.setEnabled(enabled);
    }

    private void applyToViewer(Viewer v)
    {
        Renderer renderer = v.getRenderer();
        if (renderer != null)
        {
            if (lightKitRadioButton.isSelected())
            {
                renderer.setLighting(LightingType.LIGHT_KIT);
            }
            else if (headlightRadioButton.isSelected())
            {
                renderer.setLighting(LightingType.HEADLIGHT);
            }
            else
            {
                renderer.setLighting(LightingType.FIXEDLIGHT);
            }

            renderer.setLightIntensity((Double)intensitySpinner.getValue());

            LatLon position = new LatLon(
                    Double.parseDouble(latitudeTextField.getText()),
                    Double.parseDouble(longitudeTextField.getText()),
                    Double.parseDouble(distanceTextField.getText()));

            renderer.setFixedLightPosition(position);
            renderer.setShowOrientationAxes(showAxesCheckBox.isSelected());
            renderer.setOrientationAxesInteractive(interactiveCheckBox.isSelected());

            if (joystickRadioButton.isSelected())
                renderer.setDefaultInteractorStyleType(InteractorStyleType.JOYSTICK_CAMERA);
            else
                renderer.setDefaultInteractorStyleType(InteractorStyleType.TRACKBALL_CAMERA);

            SmallBodyModel smallBodyModel =
                    viewerManager.getCurrentViewer().getModelManager().getSmallBodyModel();
            smallBodyModel.setShowScaleBar(showScaleBarCheckBox.isSelected());

            PickManager pickManager = viewerManager.getCurrentViewer().getPickManager();
            double tolerance = getToleranceFromSliderValue(pickToleranceSlider.getValue());
            pickManager.setPickTolerance(tolerance);
        }
    }

    private double getToleranceFromSliderValue(int value)
    {
        return MAX_TOLERANCE * (double)value / (double)pickToleranceSlider.getMaximum();
    }

    private int getSliderValueFromTolerance(double tolerance)
    {
        return (int)((double)pickToleranceSlider.getMaximum()
                * tolerance / MAX_TOLERANCE);
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

        lightingButtonGroup = new javax.swing.ButtonGroup();
        interactorStyleButtonGroup = new javax.swing.ButtonGroup();
        headlightRadioButton = new javax.swing.JRadioButton();
        intensityLabel = new javax.swing.JLabel();
        showAxesCheckBox = new javax.swing.JCheckBox();
        interactiveCheckBox = new javax.swing.JCheckBox();
        lightKitRadioButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        applyToCurrentButton = new javax.swing.JButton();
        applyToAllButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        intensitySpinner = new javax.swing.JSpinner();
        fixedLightRadioButton = new javax.swing.JRadioButton();
        latitudeLabel = new javax.swing.JLabel();
        latitudeTextField = new javax.swing.JFormattedTextField();
        longitudeLabel = new javax.swing.JLabel();
        longitudeTextField = new javax.swing.JFormattedTextField();
        distanceLabel = new javax.swing.JLabel();
        distanceTextField = new javax.swing.JFormattedTextField();
        jPanel4 = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        showScaleBarCheckBox = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        trackballRadioButton = new javax.swing.JRadioButton();
        joystickRadioButton = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        pickToleranceSlider = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lightingButtonGroup.add(headlightRadioButton);
        headlightRadioButton.setText("Headlight");
        headlightRadioButton.setToolTipText("A Headlight is a single light always positioned at the virtual camera. It's intensity can be changed below.");
        headlightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headlightRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(headlightRadioButton, gridBagConstraints);

        intensityLabel.setText("Intensity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        getContentPane().add(intensityLabel, gridBagConstraints);

        showAxesCheckBox.setText("Show Axes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(showAxesCheckBox, gridBagConstraints);

        interactiveCheckBox.setText("Interactive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(interactiveCheckBox, gridBagConstraints);

        lightingButtonGroup.add(lightKitRadioButton);
        lightKitRadioButton.setSelected(true);
        lightKitRadioButton.setText("Light Kit");
        lightKitRadioButton.setToolTipText("A Light Kit is a set of several lights of various strengths positioned to provide suitable illumination for most situations.");
        lightKitRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightKitRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(lightKitRadioButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        applyToCurrentButton.setText("Apply to Current View");
        applyToCurrentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToCurrentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(applyToCurrentButton, gridBagConstraints);

        applyToAllButton.setText("Apply to All Views");
        applyToAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(applyToAllButton, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Lighting");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel2.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 5, 0);
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Orientation Axes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel3.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        getContentPane().add(jPanel3, gridBagConstraints);

        intensitySpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.1d));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(intensitySpinner, gridBagConstraints);

        lightingButtonGroup.add(fixedLightRadioButton);
        fixedLightRadioButton.setText("Fixed Light");
        fixedLightRadioButton.setToolTipText("A Fixed Light is a light fixed in space that does not move with the virtual camera. Its intensity and positon can be changed below.");
        fixedLightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixedLightRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(fixedLightRadioButton, gridBagConstraints);

        latitudeLabel.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        getContentPane().add(latitudeLabel, gridBagConstraints);

        latitudeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        latitudeTextField.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(latitudeTextField, gridBagConstraints);

        longitudeLabel.setText("Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        getContentPane().add(longitudeLabel, gridBagConstraints);

        longitudeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        longitudeTextField.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(longitudeTextField, gridBagConstraints);

        distanceLabel.setText("Distance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        getContentPane().add(distanceLabel, gridBagConstraints);

        distanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        distanceTextField.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(distanceTextField, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(jSeparator3, gridBagConstraints);

        jLabel2.setText("Scale Bar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel4.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        getContentPane().add(jPanel4, gridBagConstraints);

        showScaleBarCheckBox.setText("Show Scale Bar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(showScaleBarCheckBox, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel4.setText("Interactor Style");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel5.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(jSeparator4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        getContentPane().add(jPanel5, gridBagConstraints);

        interactorStyleButtonGroup.add(trackballRadioButton);
        trackballRadioButton.setText("Trackball");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(trackballRadioButton, gridBagConstraints);

        interactorStyleButtonGroup.add(joystickRadioButton);
        joystickRadioButton.setText("Joystick");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(joystickRadioButton, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel6.add(jSeparator5, gridBagConstraints);

        jLabel5.setText("Pick Tolerance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel6.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        getContentPane().add(jPanel6, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        pickToleranceSlider.setMaximum(1000);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel7.add(pickToleranceSlider, gridBagConstraints);

        jLabel6.setText("Most Sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel7.add(jLabel6, gridBagConstraints);

        jLabel7.setText("Least Sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel7.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        getContentPane().add(jPanel7, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void applyToCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToCurrentButtonActionPerformed
        applyToViewer(viewerManager.getCurrentViewer());
    }//GEN-LAST:event_applyToCurrentButtonActionPerformed

    private void applyToAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToAllButtonActionPerformed
        ArrayList<Viewer> viewers = viewerManager.getAllViewers();
        for (Viewer v : viewers)
        {
            applyToViewer(v);
        }

        // In addition, save in preferences file for future use
        LinkedHashMap<String, Object> preferencesMap = new LinkedHashMap<String, Object>();
        if (lightKitRadioButton.isSelected())
        {
            preferencesMap.put(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString());
        }
        else if (headlightRadioButton.isSelected())
        {
            preferencesMap.put(Preferences.LIGHTING_TYPE, LightingType.HEADLIGHT.toString());
        }
        else
        {
            preferencesMap.put(Preferences.LIGHTING_TYPE, LightingType.FIXEDLIGHT.toString());
        }

        if (joystickRadioButton.isSelected())
            preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.JOYSTICK_CAMERA.toString());
        else
            preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.TRACKBALL_CAMERA.toString());

        preferencesMap.put(Preferences.LIGHT_INTENSITY, (Double)intensitySpinner.getValue());
        preferencesMap.put(Preferences.FIXEDLIGHT_LATITUDE, Double.parseDouble(latitudeTextField.getText()));
        preferencesMap.put(Preferences.FIXEDLIGHT_LONGITUDE, Double.parseDouble(longitudeTextField.getText()));
        preferencesMap.put(Preferences.FIXEDLIGHT_DISTANCE, Double.parseDouble(distanceTextField.getText()));
        preferencesMap.put(Preferences.SHOW_AXES, (Boolean)showAxesCheckBox.isSelected());
        preferencesMap.put(Preferences.INTERACTIVE_AXES, (Boolean)interactiveCheckBox.isSelected());
        preferencesMap.put(Preferences.SHOW_SCALE_BAR, (Boolean)showScaleBarCheckBox.isSelected());
        preferencesMap.put(Preferences.LIGHT_INTENSITY, (Double)intensitySpinner.getValue());
        preferencesMap.put(Preferences.PICK_TOLERANCE, getToleranceFromSliderValue(pickToleranceSlider.getValue()));
        Preferences.getInstance().put(preferencesMap);
    }//GEN-LAST:event_applyToAllButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void lightKitRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightKitRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_lightKitRadioButtonActionPerformed

    private void headlightRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headlightRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_headlightRadioButtonActionPerformed

    private void fixedLightRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixedLightRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_fixedLightRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyToAllButton;
    private javax.swing.JButton applyToCurrentButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel distanceLabel;
    private javax.swing.JFormattedTextField distanceTextField;
    private javax.swing.JRadioButton fixedLightRadioButton;
    private javax.swing.JRadioButton headlightRadioButton;
    private javax.swing.JLabel intensityLabel;
    private javax.swing.JSpinner intensitySpinner;
    private javax.swing.JCheckBox interactiveCheckBox;
    private javax.swing.ButtonGroup interactorStyleButtonGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JRadioButton joystickRadioButton;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JFormattedTextField latitudeTextField;
    private javax.swing.JRadioButton lightKitRadioButton;
    private javax.swing.ButtonGroup lightingButtonGroup;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JFormattedTextField longitudeTextField;
    private javax.swing.JSlider pickToleranceSlider;
    private javax.swing.JCheckBox showAxesCheckBox;
    private javax.swing.JCheckBox showScaleBarCheckBox;
    private javax.swing.JRadioButton trackballRadioButton;
    // End of variables declaration//GEN-END:variables
}
