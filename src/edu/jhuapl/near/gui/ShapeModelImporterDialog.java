/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.near.gui;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import vtk.vtkOBJReader;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;
import vtk.vtkTransform;
import vtk.vtkTransformPolyDataFilter;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.MapUtil;
import edu.jhuapl.near.util.PolyDataUtil;

/**
 *
 * @author eli
 */
public class ShapeModelImporterDialog extends javax.swing.JDialog
{
    // True if we're editing an existing model rather than creating a new one.
    private boolean editMode = false;

    private boolean okayPressed = false;

    /** Creates new form ShapeModelImporterDialog */
    public ShapeModelImporterDialog(java.awt.Window parent)
    {
        super(parent, "Import New Shape Model", Dialog.ModalityType.DOCUMENT_MODAL);
        initComponents();
    }

    public String getNameOfImportedShapeModel()
    {
        return nameTextField.getText();
    }

    public void setName(String name)
    {
        nameTextField.setText(name);
    }

    public void setEditMode(boolean b)
    {
        editMode = b;
        nameLabel.setEnabled(!b);
        nameTextField.setEnabled(!b);
    }

    public boolean getOkayPressed()
    {
        return okayPressed;
    }

    public void loadConfig(String configFilename) throws IOException
    {
        MapUtil configMap = new MapUtil(configFilename);
        nameTextField.setText(configMap.get(CustomShapeModel.NAME));
        boolean isEllipsoid = CustomShapeModel.ELLIPSOID.equals(configMap.get(CustomShapeModel.TYPE));
        ellipsoidRadioButton.setSelected(isEllipsoid);
        customShapeModelRadioButton.setSelected(!isEllipsoid);

        if (isEllipsoid)
        {
            equRadiusFormattedTextField.setText(configMap.get(CustomShapeModel.EQUATORIAL_RADIUS));
            polarRadiusFormattedTextField.setText(configMap.get(CustomShapeModel.POLAR_RADIUS));
            resolutionFormattedTextField.setText(configMap.get(CustomShapeModel.RESOLUTION));
        }
        else
        {
            shapeModelPathTextField.setText(configMap.get(CustomShapeModel.CUSTOM_SHAPE_MODEL_PATH));
            String format = configMap.get(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT);
            shapeModelFormatComboBox.setSelectedItem(format);
        }


        updateEnabledState();
    }

    private String validateInput()
    {
        String name = nameTextField.getText();

        if (name == null || name.trim().isEmpty())
            return "Please enter a name for the shape model.";

        // Make sure name is not empty and does not contain spaces or slashes
        if (name.contains("/") || name.contains("\\") || name.contains(" ") || name.contains("\t"))
            return "Name may not contain spaces or slashes.";

        // Check if name is already being used by another imported shape model.
        // Do not check in edit mode.
        if (!editMode)
        {
            File modelsDir = new File(Configuration.getImportedShapeModelsDir());
            File[] dirs = modelsDir.listFiles();
            if (dirs != null && dirs.length > 0)
            {
                for (File dir : dirs)
                {
                    if (dir.getName().equalsIgnoreCase(name))
                        return "Name already exists.";
                }
            }
        }

        if (ellipsoidRadioButton.isSelected())
        {
            double equRadius = Double.parseDouble(equRadiusFormattedTextField.getText());
            double polarRadius = Double.parseDouble(polarRadiusFormattedTextField.getText());

            if (equRadius <= 0.0)
                return "Equatorial radius must be positive.";
            if (polarRadius <= 0.0)
                return "Polar radius must be positive.";
            int resolution = Integer.parseInt(resolutionFormattedTextField.getText());
            if (resolution < 3 || resolution > 1024)
                return "Resolution may not be less than 3 or greater than 1024.";
        }
        else
        {
            String modelPath = shapeModelPathTextField.getText();

            if (modelPath == null || modelPath.trim().isEmpty())
                return "Please enter the path to a shape model.";

            File file = new File(modelPath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return modelPath + " does not exist or is not readable.";
        }

        return null;
    }

    private boolean importShapeModel()
    {
        LinkedHashMap<String, String> configMap = new LinkedHashMap<String, String>();

        String name = nameTextField.getText();
        configMap.put(CustomShapeModel.NAME, name);

        vtkPolyData shapePoly = null;

        // First either load a shape model from file or create ellipsoidal shape model
        if (ellipsoidRadioButton.isSelected())
        {
            double equRadius = Double.parseDouble(equRadiusFormattedTextField.getText());
            double polarRadius = Double.parseDouble(polarRadiusFormattedTextField.getText());
            int resolution = Integer.parseInt(resolutionFormattedTextField.getText());

            vtkSphereSource sphereSource = new vtkSphereSource();
            sphereSource.SetRadius(equRadius);
            sphereSource.SetCenter(0.0, 0.0, 0.0);
            sphereSource.SetLatLongTessellation(0);
            sphereSource.SetThetaResolution(resolution);
            sphereSource.SetPhiResolution(Math.max(3, resolution/2 + 1));
            sphereSource.Update();
            shapePoly = sphereSource.GetOutput();

            if (equRadius != polarRadius)
            {
                // Turn it into ellipsoid
                vtkTransformPolyDataFilter filter = new vtkTransformPolyDataFilter();
                filter.SetInput(shapePoly);

                vtkTransform transform = new vtkTransform();
                transform.Scale(1.0, 1.0, polarRadius/equRadius);

                filter.SetTransform(transform);
                filter.Update();

                shapePoly.Delete();
                shapePoly = filter.GetOutput();
            }

            configMap.put(CustomShapeModel.TYPE, CustomShapeModel.ELLIPSOID);
            configMap.put(CustomShapeModel.EQUATORIAL_RADIUS, String.valueOf(equRadius));
            configMap.put(CustomShapeModel.POLAR_RADIUS, String.valueOf(polarRadius));
            configMap.put(CustomShapeModel.RESOLUTION, String.valueOf(resolution));
        }
        else
        {
            String modelPath = shapeModelPathTextField.getText();
            String format = (String) shapeModelFormatComboBox.getSelectedItem();

            configMap.put(CustomShapeModel.TYPE, CustomShapeModel.CUSTOM);
            configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_PATH, modelPath);

            if (format.equals("PDS"))
            {
                try
                {
                    shapePoly = PolyDataUtil.loadPDSShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                            "The was an error loading " + modelPath + ".\nAre you sure you specified the right format?",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
                normalsFilter.SetInput(shapePoly);
                normalsFilter.SetComputeCellNormals(0);
                normalsFilter.SetComputePointNormals(1);
                normalsFilter.SplittingOff();
                normalsFilter.Update();

                shapePoly = normalsFilter.GetOutput();

                configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, CustomShapeModel.PDS_FORMAT);
            }
            else if (format.equals("OBJ"))
            {
                vtkOBJReader reader = new vtkOBJReader();
                reader.SetFileName(modelPath);
                reader.Update();

                shapePoly = reader.GetOutput();

                configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, CustomShapeModel.OBJ_FORMAT);
            }
            else if (format.equals("VTK"))
            {
                vtkPolyDataReader reader = new vtkPolyDataReader();
                reader.SetFileName(modelPath);
                reader.Update();

                shapePoly = reader.GetOutput();

                configMap.put(CustomShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, CustomShapeModel.VTK_FORMAT);
            }
        }

        // Now save the shape model to the users home folder within the
        // custom-shape-models folders
        File newModelDir = new File(Configuration.getImportedShapeModelsDir() + File.separator + name);
        FileUtils.deleteQuietly(newModelDir);
        newModelDir.mkdirs();


        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInput(shapePoly);
        writer.SetFileName(newModelDir.getAbsolutePath() + File.separator + "model.vtk");
        writer.SetFileTypeToBinary();
        writer.Write();


        // Generate a graticule
        Graticule grid = new Graticule(null, null);
        grid.generateGrid(shapePoly);

        writer = new vtkPolyDataWriter();
        writer.SetInput(grid.getGridAsPolyData());
        writer.SetFileName(newModelDir.getAbsolutePath() + File.separator + "grid.vtk");
        writer.SetFileTypeToBinary();
        writer.Write();





        // Save out all information about this shape model to the config.txt file
        MapUtil map = new MapUtil(newModelDir.getAbsolutePath() + File.separator + "config.txt");
        map.put(configMap);

        return true;
    }

    private void updateEnabledState()
    {
        boolean enabled = ellipsoidRadioButton.isSelected();
        shapeModelPathTextField.setEnabled(!enabled);
        browseShapeModelButton.setEnabled(!enabled);
        pathLabel.setEnabled(!enabled);
        shapeModelFormatLabel.setEnabled(!enabled);
        shapeModelFormatComboBox.setEnabled(!enabled);
        equRadiusLabel.setEnabled(enabled);
        equRadiusFormattedTextField.setEnabled(enabled);
        polarRadiusLabel.setEnabled(enabled);
        polarRadiusFormattedTextField.setEnabled(enabled);
        resolutionLabel.setEnabled(enabled);
        resolutionFormattedTextField.setEnabled(enabled);
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

        shapeModelSourceButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        customShapeModelRadioButton = new javax.swing.JRadioButton();
        ellipsoidRadioButton = new javax.swing.JRadioButton();
        shapeModelPathTextField = new javax.swing.JTextField();
        browseShapeModelButton = new javax.swing.JButton();
        equRadiusLabel = new javax.swing.JLabel();
        polarRadiusLabel = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        equRadiusFormattedTextField = new javax.swing.JFormattedTextField();
        polarRadiusFormattedTextField = new javax.swing.JFormattedTextField();
        resolutionFormattedTextField = new javax.swing.JFormattedTextField();
        shapeModelFormatLabel = new javax.swing.JLabel();
        shapeModelFormatComboBox = new javax.swing.JComboBox();
        pathLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        getContentPane().add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        getContentPane().add(nameTextField, gridBagConstraints);

        shapeModelSourceButtonGroup.add(customShapeModelRadioButton);
        customShapeModelRadioButton.setText("Custom Shape Model");
        customShapeModelRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customShapeModelRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(customShapeModelRadioButton, gridBagConstraints);

        shapeModelSourceButtonGroup.add(ellipsoidRadioButton);
        ellipsoidRadioButton.setSelected(true);
        ellipsoidRadioButton.setText("Ellipsoid Shape Model");
        ellipsoidRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipsoidRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(ellipsoidRadioButton, gridBagConstraints);

        shapeModelPathTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(shapeModelPathTextField, gridBagConstraints);

        browseShapeModelButton.setText("Browse...");
        browseShapeModelButton.setEnabled(false);
        browseShapeModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseShapeModelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseShapeModelButton, gridBagConstraints);

        equRadiusLabel.setText("Equatorial Radius");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(equRadiusLabel, gridBagConstraints);

        polarRadiusLabel.setText("Polar Radius");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(polarRadiusLabel, gridBagConstraints);

        resolutionLabel.setText("Resolution");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(resolutionLabel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        jPanel1.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        equRadiusFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        equRadiusFormattedTextField.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(equRadiusFormattedTextField, gridBagConstraints);

        polarRadiusFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        polarRadiusFormattedTextField.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(polarRadiusFormattedTextField, gridBagConstraints);

        resolutionFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        resolutionFormattedTextField.setText("360");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(resolutionFormattedTextField, gridBagConstraints);

        shapeModelFormatLabel.setText("Format");
        shapeModelFormatLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(shapeModelFormatLabel, gridBagConstraints);

        shapeModelFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PDS", "OBJ", "VTK" }));
        shapeModelFormatComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(shapeModelFormatComboBox, gridBagConstraints);

        pathLabel.setText("Path");
        pathLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(pathLabel, gridBagConstraints);

        jLabel1.setForeground(java.awt.Color.red);
        jLabel1.setText("<html>Note: Loading of images has been moved to the new Images tab.<br>Loading of plate data has been moved to the main tab of each view.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 20, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void customShapeModelRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_customShapeModelRadioButtonActionPerformed
    {//GEN-HEADEREND:event_customShapeModelRadioButtonActionPerformed
        updateEnabledState();
    }//GEN-LAST:event_customShapeModelRadioButtonActionPerformed

    private void ellipsoidRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ellipsoidRadioButtonActionPerformed
    {//GEN-HEADEREND:event_ellipsoidRadioButtonActionPerformed
        updateEnabledState();
    }//GEN-LAST:event_ellipsoidRadioButtonActionPerformed

    private void browseShapeModelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseShapeModelButtonActionPerformed
    {//GEN-HEADEREND:event_browseShapeModelButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Shape Model");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        shapeModelPathTextField.setText(filename);
    }//GEN-LAST:event_browseShapeModelButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        String errorString = validateInput();
        if (errorString != null)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    errorString,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = importShapeModel();

        if (success)
        {
            okayPressed = true;
            setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseShapeModelButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton customShapeModelRadioButton;
    private javax.swing.JRadioButton ellipsoidRadioButton;
    private javax.swing.JFormattedTextField equRadiusFormattedTextField;
    private javax.swing.JLabel equRadiusLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JFormattedTextField polarRadiusFormattedTextField;
    private javax.swing.JLabel polarRadiusLabel;
    private javax.swing.JFormattedTextField resolutionFormattedTextField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JComboBox shapeModelFormatComboBox;
    private javax.swing.JLabel shapeModelFormatLabel;
    private javax.swing.JTextField shapeModelPathTextField;
    private javax.swing.ButtonGroup shapeModelSourceButtonGroup;
    // End of variables declaration//GEN-END:variables
}
