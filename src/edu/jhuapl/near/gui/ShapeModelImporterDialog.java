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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import vtk.vtkAlgorithmOutput;
import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;
import vtk.vtkOBJReader;
import vtk.vtkPNGWriter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;
import vtk.vtkTransform;
import vtk.vtkTransformPolyDataFilter;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.PolyDataUtil;

/**
 *
 * @author eli
 */
public class ShapeModelImporterDialog extends javax.swing.JDialog
{

    /** Creates new form ShapeModelImporterDialog */
    public ShapeModelImporterDialog(java.awt.Frame parent)
    {
        super(parent, "Import New Shape Model", true);
        initComponents();
    }

    public String getNameOfImportedShapeModel()
    {
        return nameTextField.getText();
    }

    private String validateInput()
    {
        String name = nameTextField.getText();

        if (name == null || name.isEmpty())
            return "Please enter a name for the shape model.";

        // Make sure name is not empty and does not contain spaces or slashes
        if (name.contains("/") || name.contains("\\") || name.contains(" ") || name.contains("\t"))
            return "Name may not contain spaces or slashes.";

        // Check if name is already being used by another imported shape model
        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            for (File dir : dirs)
            {
                if (dir.getName().equals(name))
                    return "Name already exists.";
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

            if (modelPath == null || modelPath.isEmpty())
                return "Please enter the path to a shape model.";

            File file = new File(modelPath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return modelPath + " does not exist or is not readable.";
        }

        if (mapImageCheckBox.isSelected())
        {
            String imagePath = imagePathTextField.getText();

            if (imagePath == null || imagePath.isEmpty())
                return "Please enter the path to an image.";

            File file = new File(imagePath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return imagePath + " does not exist or is not readable.";

            double lllat = Double.parseDouble(lllatFormattedTextField.getText());
            double lllon = Double.parseDouble(lllonFormattedTextField.getText());
            double urlat = Double.parseDouble(urlatFormattedTextField.getText());
            double urlon = Double.parseDouble(urlonFormattedTextField.getText());

            if (lllat < -90.0 || lllat > 90.0 || urlat < -90.0 || urlat > 90.0)
                return "Latitudes must be between -90 and +90.";
            if (lllon < 0.0 || lllon > 360.0 || urlon < 0.0 || urlon > 360.0)
                return "Longitudes must be between 0 and 360.";

            if (lllat >= urlat)
                return "Latitude of upper right corner must be greater than latitude of lower left corner.";
        }

        return null;
    }

    private boolean importShapeModel()
    {
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
        }
        else
        {
            String modelPath = shapeModelPathTextField.getText();
            String format = (String) shapeModelFormatComboBox.getSelectedItem();
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
            }
            else if (format.equals("OBJ"))
            {
                vtkOBJReader reader = new vtkOBJReader();
                reader.SetFileName(modelPath);
                reader.Update();

                shapePoly = reader.GetOutput();
            }
            else if (format.equals("VTK"))
            {
                vtkPolyDataReader reader = new vtkPolyDataReader();
                reader.SetFileName(modelPath);
                reader.Update();

                shapePoly = reader.GetOutput();
            }
        }

        // Now save the shape model to the users home folder within the
        // custom-shape-models folders
        String name = nameTextField.getText();
        File newModelDir = new File(Configuration.getImportedShapeModelsDir() + File.separator + name);
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


        // Load in the image
        if (mapImageCheckBox.isSelected())
        {
            String imageName = imagePathTextField.getText();
            vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
            vtkImageReader2 imageReader = imageFactory.CreateImageReader2(imageName);
            if (imageReader == null)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "The format of the specified file is not supported.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                String dirpath = Configuration.getImportedShapeModelsDir() + File.separator + name;
                FileUtils.deleteQuietly(new File(dirpath));

                return false;
            }
            imageReader.SetFileName(imageName);
            imageReader.Update();

            vtkAlgorithmOutput imageReaderOutput = imageReader.GetOutputPort();
            //vtkStructuredPointsWriter imageWriter = new vtkStructuredPointsWriter();
            vtkPNGWriter imageWriter = new vtkPNGWriter();
            imageWriter.SetInputConnection(imageReaderOutput);
            imageWriter.SetFileName(newModelDir.getAbsolutePath() + File.separator + "image.png");
            //imageWriter.SetFileTypeToBinary();
            imageWriter.Write();

            // Save out the corners of the texture to a file
            try
            {
                FileWriter fstream = new FileWriter(newModelDir.getAbsolutePath() + File.separator + "corners.txt");
                BufferedWriter out = new BufferedWriter(fstream);

                out.write(lllatFormattedTextField.getText() + " ");
                out.write(lllonFormattedTextField.getText() + " ");
                out.write(urlatFormattedTextField.getText() + " ");
                out.write(urlonFormattedTextField.getText() + "\n");

                out.close();
            }
            catch (IOException ex)
            {

            }
        }

        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        shapeModelSourceButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        customShapeModelRadioButton = new javax.swing.JRadioButton();
        ellipsoidRadioButton = new javax.swing.JRadioButton();
        shapeModelPathTextField = new javax.swing.JTextField();
        browseShapeModelButton = new javax.swing.JButton();
        equRadiusLabel = new javax.swing.JLabel();
        polarRadiusLabel = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        mapImageCheckBox = new javax.swing.JCheckBox();
        pathLabel2 = new javax.swing.JLabel();
        imagePathTextField = new javax.swing.JTextField();
        browseImageButton = new javax.swing.JButton();
        lllatLabel = new javax.swing.JLabel();
        lllonLabel = new javax.swing.JLabel();
        urlatLabel = new javax.swing.JLabel();
        urlonLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        equRadiusFormattedTextField = new javax.swing.JFormattedTextField();
        polarRadiusFormattedTextField = new javax.swing.JFormattedTextField();
        resolutionFormattedTextField = new javax.swing.JFormattedTextField();
        lllatFormattedTextField = new javax.swing.JFormattedTextField();
        lllonFormattedTextField = new javax.swing.JFormattedTextField();
        urlatFormattedTextField = new javax.swing.JFormattedTextField();
        urlonFormattedTextField = new javax.swing.JFormattedTextField();
        shapeModelFormatLabel = new javax.swing.JLabel();
        shapeModelFormatComboBox = new javax.swing.JComboBox();
        pathLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        getContentPane().add(jLabel1, gridBagConstraints);
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

        mapImageCheckBox.setSelected(true);
        mapImageCheckBox.setText("Image Map");
        mapImageCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapImageCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        getContentPane().add(mapImageCheckBox, gridBagConstraints);

        pathLabel2.setText("Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(pathLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(imagePathTextField, gridBagConstraints);

        browseImageButton.setText("Browse...");
        browseImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseImageButton, gridBagConstraints);

        lllatLabel.setText("Lower Left Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllatLabel, gridBagConstraints);

        lllonLabel.setText("Lower Left Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllonLabel, gridBagConstraints);

        urlatLabel.setText("Upper Right Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(urlatLabel, gridBagConstraints);

        urlonLabel.setText("Upper Right Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(urlonLabel, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.setPreferredSize(new java.awt.Dimension(54, 22));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        equRadiusFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00000"))));
        equRadiusFormattedTextField.setText("1000.0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(equRadiusFormattedTextField, gridBagConstraints);

        polarRadiusFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00000"))));
        polarRadiusFormattedTextField.setText("1000.0");
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

        lllatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        lllatFormattedTextField.setText("-90.00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllatFormattedTextField, gridBagConstraints);

        lllonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        lllonFormattedTextField.setText("0.00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllonFormattedTextField, gridBagConstraints);

        urlatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        urlatFormattedTextField.setText("90.00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlatFormattedTextField, gridBagConstraints);

        urlonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        urlonFormattedTextField.setText("360.00");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlonFormattedTextField, gridBagConstraints);

        shapeModelFormatLabel.setText("Format");
        shapeModelFormatLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(shapeModelFormatLabel, gridBagConstraints);

        shapeModelFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PDS", "OBJ", "" }));
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

        pack();
    }//GEN-END:initComponents

    private void customShapeModelRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_customShapeModelRadioButtonActionPerformed
    {//GEN-HEADEREND:event_customShapeModelRadioButtonActionPerformed
        boolean enabled = customShapeModelRadioButton.isSelected();
        shapeModelPathTextField.setEnabled(enabled);
        browseShapeModelButton.setEnabled(enabled);
        pathLabel.setEnabled(enabled);
        shapeModelFormatLabel.setEnabled(enabled);
        shapeModelFormatComboBox.setEnabled(enabled);
        equRadiusLabel.setEnabled(!enabled);
        equRadiusFormattedTextField.setEnabled(!enabled);
        polarRadiusLabel.setEnabled(!enabled);
        polarRadiusFormattedTextField.setEnabled(!enabled);
        resolutionLabel.setEnabled(!enabled);
        resolutionFormattedTextField.setEnabled(!enabled);
    }//GEN-LAST:event_customShapeModelRadioButtonActionPerformed

    private void ellipsoidRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ellipsoidRadioButtonActionPerformed
    {//GEN-HEADEREND:event_ellipsoidRadioButtonActionPerformed
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

    private void mapImageCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mapImageCheckBoxActionPerformed
    {//GEN-HEADEREND:event_mapImageCheckBoxActionPerformed
        boolean enabled = mapImageCheckBox.isSelected();
        pathLabel2.setEnabled(enabled);
        imagePathTextField.setEnabled(enabled);
        browseImageButton.setEnabled(enabled);
        lllatLabel.setEnabled(enabled);
        lllatFormattedTextField.setEnabled(enabled);
        lllonLabel.setEnabled(enabled);
        lllonFormattedTextField.setEnabled(enabled);
        urlatLabel.setEnabled(enabled);
        urlatFormattedTextField.setEnabled(enabled);
        urlonLabel.setEnabled(enabled);
        urlonFormattedTextField.setEnabled(enabled);
    }//GEN-LAST:event_mapImageCheckBoxActionPerformed

    private void browseImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseImageButtonActionPerformed
    {//GEN-HEADEREND:event_browseImageButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Image");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        imagePathTextField.setText(filename);
    }//GEN-LAST:event_browseImageButtonActionPerformed

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
            setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseImageButton;
    private javax.swing.JButton browseShapeModelButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton customShapeModelRadioButton;
    private javax.swing.JRadioButton ellipsoidRadioButton;
    private javax.swing.JFormattedTextField equRadiusFormattedTextField;
    private javax.swing.JLabel equRadiusLabel;
    private javax.swing.JTextField imagePathTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFormattedTextField lllatFormattedTextField;
    private javax.swing.JLabel lllatLabel;
    private javax.swing.JFormattedTextField lllonFormattedTextField;
    private javax.swing.JLabel lllonLabel;
    private javax.swing.JCheckBox mapImageCheckBox;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel pathLabel2;
    private javax.swing.JFormattedTextField polarRadiusFormattedTextField;
    private javax.swing.JLabel polarRadiusLabel;
    private javax.swing.JFormattedTextField resolutionFormattedTextField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JComboBox shapeModelFormatComboBox;
    private javax.swing.JLabel shapeModelFormatLabel;
    private javax.swing.JTextField shapeModelPathTextField;
    private javax.swing.ButtonGroup shapeModelSourceButtonGroup;
    private javax.swing.JFormattedTextField urlatFormattedTextField;
    private javax.swing.JLabel urlatLabel;
    private javax.swing.JFormattedTextField urlonFormattedTextField;
    private javax.swing.JLabel urlonLabel;
    // End of variables declaration//GEN-END:variables
}
