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
import java.text.DecimalFormat;

import javax.swing.JOptionPane;

import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;


/**
 *
 * @author eli
 */
public class CustomImageImporterDialog extends javax.swing.JDialog
{
    private boolean okayPressed = false;
    private boolean isEllipsoid;
    private boolean isEditMode;
    private static final String LEAVE_UNMODIFIED = "<leave unmodified or empty to use existing image>";

    public enum ProjectionType
    {
        CYLINDRICAL,
        PERSPECTIVE
    }

    public static class ImageInfo
    {
        public String name = ""; // name to call this image for display purposes
        public String imagefilename = ""; // filename of image on disk
        public ProjectionType projectionType = ProjectionType.CYLINDRICAL;
        public double lllat = -90.0;
        public double lllon = 0.0;
        public double urlat = 90.0;
        public double urlon = 360.0;
        public String sumfilename = "null"; // filename of sumfile on disk
        public double xfov = 1.0;
        public double yfov = 1.0;

        @Override
        public String toString()
        {
            DecimalFormat df = new DecimalFormat("#.#####");
            if (projectionType == ProjectionType.CYLINDRICAL)
            {
                return name + ", Cylindrical  ["
                        + df.format(lllat) + ", "
                        + df.format(lllon) + ", "
                        + df.format(urlat) + ", "
                        + df.format(urlon)
                        + "]";
            }
            else
            {
                return name + ", Perspective";
            }
        }
    }

    /** Creates new form ShapeModelImporterDialog */
    public CustomImageImporterDialog(java.awt.Window parent, boolean isEditMode)
    {
        super(parent, "Import New Shape Model", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        this.isEditMode = isEditMode;
    }

    public void setImageInfo(ImageInfo info, boolean isEllipsoid)
    {
        this.isEllipsoid = isEllipsoid;

        if (isEditMode)
            imagePathTextField.setText(LEAVE_UNMODIFIED);

        if (info.projectionType == ProjectionType.CYLINDRICAL)
        {
            cylindricalProjectionRadioButton.setSelected(true);

            lllatFormattedTextField.setText(String.valueOf(info.lllat));
            lllonFormattedTextField.setText(String.valueOf(info.lllon));
            urlatFormattedTextField.setText(String.valueOf(info.urlat));
            urlonFormattedTextField.setText(String.valueOf(info.urlon));
        }
        else if (info.projectionType == ProjectionType.PERSPECTIVE)
        {
            perspectiveProjectionRadioButton.setSelected(true);

            if (isEditMode)
                sumfilePathTextField.setText(LEAVE_UNMODIFIED);
            xFovTextField.setValue(info.xfov);
            yFovTextField.setValue(info.yfov);
        }

        updateEnabledItems();
    }

    public ProjectionType getSelectedProjectionType()
    {
        if (cylindricalProjectionRadioButton.isSelected())
            return ProjectionType.CYLINDRICAL;
        else
            return ProjectionType.PERSPECTIVE;
    }

    public ImageInfo getImageInfo()
    {
        ImageInfo info = new ImageInfo();

        info.imagefilename = imagePathTextField.getText();
        if (LEAVE_UNMODIFIED.equals(info.imagefilename) || info.imagefilename == null || info.imagefilename.isEmpty())
            info.imagefilename = null;

        if (cylindricalProjectionRadioButton.isSelected())
        {
            info.projectionType = ProjectionType.CYLINDRICAL;
            info.lllat = Double.parseDouble(lllatFormattedTextField.getText());
            info.lllon = Double.parseDouble(lllonFormattedTextField.getText());
            info.urlat = Double.parseDouble(urlatFormattedTextField.getText());
            info.urlon = Double.parseDouble(urlonFormattedTextField.getText());
        }
        else if (perspectiveProjectionRadioButton.isSelected())
        {
            info.projectionType = ProjectionType.PERSPECTIVE;
            info.sumfilename = sumfilePathTextField.getText();
            if (LEAVE_UNMODIFIED.equals(info.sumfilename) || info.sumfilename == null || info.sumfilename.isEmpty())
                info.sumfilename = null;
            info.xfov = Double.parseDouble(xFovTextField.getText());
            info.yfov = Double.parseDouble(yFovTextField.getText());
        }

        if (info.imagefilename != null)
            info.name = new File(info.imagefilename).getName();

        return info;
    }

    private String validateInput()
    {
        String imagePath = imagePathTextField.getText();
        if (imagePath == null)
            imagePath = "";
        imagePath = imagePath.trim();

        if (!isEditMode || (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED)))
        {
            if (imagePath.isEmpty())
                return "Please enter the path to an image.";

            File file = new File(imagePath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return imagePath + " does not exist or is not readable.";

            if (imagePath.contains(","))
                return "Image path may not contain commas.";
        }

        if (cylindricalProjectionRadioButton.isSelected())
        {
            if (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED))
            {
                vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
                vtkImageReader2 imageReader = imageFactory.CreateImageReader2(imagePath);
                if (imageReader == null)
                    return "The format of the specified image is not supported.";
            }

            try
            {
                double lllat = Double.parseDouble(lllatFormattedTextField.getText());
                double urlat = Double.parseDouble(urlatFormattedTextField.getText());
                Double.parseDouble(lllonFormattedTextField.getText());
                Double.parseDouble(urlonFormattedTextField.getText());

                if (lllat < -90.0 || lllat > 90.0 || urlat < -90.0 || urlat > 90.0)
                    return "Latitudes must be between -90 and +90.";
                if (lllat >= urlat)
                    return "Upper right latitude must be greater than lower left latitude.";

                if (!isEllipsoid)
                {
                    if ( (lllat < 1.0 && lllat > 0.0) || (lllat > -1.0 && lllat < 0.0) ||
                            (urlat < 1.0 && urlat > 0.0) || (urlat > -1.0 && urlat < 0.0) )
                        return "For non-ellipsoidal shape models, latitudes must be (in degrees) either 0, greater than +1, or less then -1.";
                }
            }
            catch (NumberFormatException e)
            {
                return "An error occurred parsing one of the required fields.";
            }
        }
        else
        {
            String sumfilePath = sumfilePathTextField.getText();
            if (sumfilePath == null)
                sumfilePath = "";
            sumfilePath = sumfilePath.trim();

            if (!isEditMode || (!sumfilePath.isEmpty() && !sumfilePath.equals(LEAVE_UNMODIFIED)))
            {
                if (sumfilePath.isEmpty())
                    return "Please enter the path to a sumfile.";

                File file = new File(sumfilePath);
                if (!file.exists() || !file.canRead() || !file.isFile())
                    return sumfilePath + " does not exist or is not readable.";

                if (sumfilePath.contains(","))
                    return "Path may not contain commas.";
            }

            try
            {
                double xfov = Double.parseDouble(xFovTextField.getText());
                double yfov = Double.parseDouble(yFovTextField.getText());
                if (xfov < 0.00000001 || xfov > 179.0 || yfov < 0.00000001 || yfov > 179.0)
                    return "Field of view must be between 0.00000001 and 179 degrees.";
            }
            catch (NumberFormatException e)
            {
                return "An error occurred parsing one of the required fields.";
            }
        }

        return null;
    }

    public boolean getOkayPressed()
    {
        return okayPressed;
    }

    private void updateEnabledItems()
    {
        boolean enable = cylindricalProjectionRadioButton.isSelected();
        lllatLabel.setEnabled(enable);
        lllatFormattedTextField.setEnabled(enable);
        lllonLabel.setEnabled(enable);
        lllonFormattedTextField.setEnabled(enable);
        urlatLabel.setEnabled(enable);
        urlatFormattedTextField.setEnabled(enable);
        urlonLabel.setEnabled(enable);
        urlonFormattedTextField.setEnabled(enable);
        sumfilePathLabel.setEnabled(!enable);
        sumfilePathTextField.setEnabled(!enable);
        xFovLabel.setEnabled(!enable);
        xFovTextField.setEnabled(!enable);
        yFovLabel.setEnabled(!enable);
        yFovTextField.setEnabled(!enable);
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

        projectionButtonGroup = new javax.swing.ButtonGroup();
        imagePathLabel = new javax.swing.JLabel();
        imagePathTextField = new javax.swing.JTextField();
        browseImageButton = new javax.swing.JButton();
        lllatLabel = new javax.swing.JLabel();
        lllonLabel = new javax.swing.JLabel();
        urlatLabel = new javax.swing.JLabel();
        urlonLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        lllatFormattedTextField = new javax.swing.JFormattedTextField();
        lllonFormattedTextField = new javax.swing.JFormattedTextField();
        urlatFormattedTextField = new javax.swing.JFormattedTextField();
        urlonFormattedTextField = new javax.swing.JFormattedTextField();
        cylindricalProjectionRadioButton = new javax.swing.JRadioButton();
        perspectiveProjectionRadioButton = new javax.swing.JRadioButton();
        sumfilePathLabel = new javax.swing.JLabel();
        sumfilePathTextField = new javax.swing.JTextField();
        browseSumfileButton = new javax.swing.JButton();
        xFovLabel = new javax.swing.JLabel();
        yFovLabel = new javax.swing.JLabel();
        xFovTextField = new javax.swing.JFormattedTextField();
        yFovTextField = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 167));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        imagePathLabel.setText("Image Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        getContentPane().add(imagePathLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
        getContentPane().add(imagePathTextField, gridBagConstraints);

        browseImageButton.setText("Browse...");
        browseImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 5);
        getContentPane().add(browseImageButton, gridBagConstraints);

        lllatLabel.setText("Lower Left Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllatLabel, gridBagConstraints);

        lllonLabel.setText("Lower Left Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllonLabel, gridBagConstraints);

        urlatLabel.setText("Upper Right Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(urlatLabel, gridBagConstraints);

        urlonLabel.setText("Upper Right Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
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
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        lllatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        lllatFormattedTextField.setText("-90");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllatFormattedTextField, gridBagConstraints);

        lllonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        lllonFormattedTextField.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllonFormattedTextField, gridBagConstraints);

        urlatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        urlatFormattedTextField.setText("90");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlatFormattedTextField, gridBagConstraints);

        urlonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        urlonFormattedTextField.setText("360");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlonFormattedTextField, gridBagConstraints);

        projectionButtonGroup.add(cylindricalProjectionRadioButton);
        cylindricalProjectionRadioButton.setSelected(true);
        cylindricalProjectionRadioButton.setText("Cylindrical Projection");
        cylindricalProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cylindricalProjectionRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
        getContentPane().add(cylindricalProjectionRadioButton, gridBagConstraints);

        projectionButtonGroup.add(perspectiveProjectionRadioButton);
        perspectiveProjectionRadioButton.setText("Perspective Projection");
        perspectiveProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                perspectiveProjectionRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
        getContentPane().add(perspectiveProjectionRadioButton, gridBagConstraints);

        sumfilePathLabel.setText("Sumfile Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(sumfilePathLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(sumfilePathTextField, gridBagConstraints);

        browseSumfileButton.setText("Browse...");
        browseSumfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseSumfileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseSumfileButton, gridBagConstraints);

        xFovLabel.setText("X FOV (degrees)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(xFovLabel, gridBagConstraints);

        yFovLabel.setText("Y FOV (degrees)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(yFovLabel, gridBagConstraints);

        xFovTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        xFovTextField.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(xFovTextField, gridBagConstraints);

        yFovTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        yFovTextField.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(yFovTextField, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            JOptionPane.showMessageDialog(this,
                    errorString,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        okayPressed = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cylindricalProjectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cylindricalProjectionRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_cylindricalProjectionRadioButtonActionPerformed

    private void perspectiveProjectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_perspectiveProjectionRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_perspectiveProjectionRadioButtonActionPerformed

    private void browseSumfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSumfileButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Sumfile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        sumfilePathTextField.setText(filename);
    }//GEN-LAST:event_browseSumfileButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseImageButton;
    private javax.swing.JButton browseSumfileButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton cylindricalProjectionRadioButton;
    private javax.swing.JLabel imagePathLabel;
    private javax.swing.JTextField imagePathTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFormattedTextField lllatFormattedTextField;
    private javax.swing.JLabel lllatLabel;
    private javax.swing.JFormattedTextField lllonFormattedTextField;
    private javax.swing.JLabel lllonLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton perspectiveProjectionRadioButton;
    private javax.swing.ButtonGroup projectionButtonGroup;
    private javax.swing.JLabel sumfilePathLabel;
    private javax.swing.JTextField sumfilePathTextField;
    private javax.swing.JFormattedTextField urlatFormattedTextField;
    private javax.swing.JLabel urlatLabel;
    private javax.swing.JFormattedTextField urlonFormattedTextField;
    private javax.swing.JLabel urlonLabel;
    private javax.swing.JLabel xFovLabel;
    private javax.swing.JFormattedTextField xFovTextField;
    private javax.swing.JLabel yFovLabel;
    private javax.swing.JFormattedTextField yFovTextField;
    // End of variables declaration//GEN-END:variables
}
