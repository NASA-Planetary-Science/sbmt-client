/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.gui.image.ui.custom;

import java.awt.Dialog;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.util.VtkENVIReader;


public class CustomImageImporterDialog extends javax.swing.JDialog
{
    private boolean okayPressed = false;
    private boolean isEllipsoid;
    private boolean isEditMode;
    private ImagingInstrument instrument;
    private static final String LEAVE_UNMODIFIED = "<cannot be changed>";

    public enum ProjectionType
    {
        CYLINDRICAL,
        PERSPECTIVE
    }

    public static class ImageInfo implements MetadataManager
    {
        public String name = ""; // name to call this image for display purposes
        public String imagefilename = ""; // filename of image on disk
        public ProjectionType projectionType = ProjectionType.CYLINDRICAL;
        public ImageType imageType = ImageType.GENERIC_IMAGE;
        public double rotation = 0.0;
        public String flip = "None";
        public double lllat = -90.0;
        public double lllon = 0.0;
        public double urlat = 90.0;
        public double urlon = 360.0;
        public String sumfilename = "null"; // filename of sumfile on disk
        public String infofilename = "null"; // filename of infofile on disk

        final Key<String> nameKey = Key.of("name");
        final Key<String> imageFileNameKey = Key.of("imagefilename");
        final Key<String> projectionKey = Key.of("projectionType");
        final Key<String> imageTypeKey = Key.of("imageType");
        final Key<Double> rotationKey = Key.of("rotation");
        final Key<String> flipKey = Key.of("flip");
        final Key<Double> lllatKey = Key.of("lllat");
        final Key<Double> lllonKey = Key.of("lllon");
        final Key<Double> urlatKey = Key.of("urlat");
        final Key<Double> urlonKey = Key.of("urlon");
        final Key<String> sumfilenameKey = Key.of("sumfilename");
        final Key<String> infofileKey = Key.of("infofilename");


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
                if (imageType == ImageType.GENERIC_IMAGE)
                    return name + ", Perspective" + ", " + imageType + ", Rotate " + rotation + ", Flip " + flip;
                else
                    return name + ", Perspective" + ", " + imageType;
            }
        }

        public List<String> toList()
        {
            List<String> string = new Vector<String>();
            string.add(name);
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//            string.add(formatter.format(new Date()));
            string.add("" + new Date().getTime());
            return string;

        }

        @Override
        public Metadata store()
        {
            SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
            result.put(nameKey, name);
            result.put(imageFileNameKey, imagefilename);
            result.put(projectionKey, projectionType.toString());
            result.put(imageTypeKey, imageType.toString());
            result.put(rotationKey, rotation);
            result.put(flipKey, flip);
            result.put(lllatKey, lllat);
            result.put(lllonKey, lllon);
            result.put(urlatKey, urlat);
            result.put(urlonKey, urlon);
            result.put(sumfilenameKey, sumfilename);
            result.put(infofileKey, infofilename);
            return result;
        }

        @Override
        public void retrieve(Metadata source)
        {
            name = source.get(nameKey);
            imagefilename = source.get(imageFileNameKey);
            projectionType = ProjectionType.valueOf(source.get(projectionKey));
            imageType = ImageType.valueOf(source.get(imageTypeKey));
            rotation = source.get(rotationKey);
            flip = source.get(flipKey);
            lllat = source.get(lllatKey);
            lllon = source.get(lllonKey);
            urlat = source.get(urlatKey);
            urlon = source.get(urlonKey);
            sumfilename = source.get(sumfilenameKey);
            infofilename = source.get(infofileKey);
        }
    }

    /** Creates new form ShapeModelImporterDialog */
    public CustomImageImporterDialog(java.awt.Window parent, boolean isEditMode, ImagingInstrument instrument)
    {
        super(parent, isEditMode ? "Edit Image" : "Import New Image", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        this.isEditMode = isEditMode;
        this.instrument = instrument;

        if (isEditMode)
        {
            browseImageButton.setEnabled(false);
            browseSumfileButton.setEnabled(false);
            browseInfofileButton.setEnabled(false);
            imagePathTextField.setEnabled(false);
            infofilePathTextField.setEnabled(false);
            sumfilePathTextField.setEnabled(false);
        }
    }

    public void setImageInfo(ImageInfo info, boolean isEllipsoid)
    {
        this.isEllipsoid = isEllipsoid;

        if (isEditMode)
            imagePathTextField.setText(LEAVE_UNMODIFIED);
        else
            imagePathTextField.setText(info.imagefilename);

        imageNameTextField.setText(info.name);

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
            {
                sumfilePathTextField.setText(LEAVE_UNMODIFIED);
                infofilePathTextField.setText(LEAVE_UNMODIFIED);
            }
        }

        ImageType currentImageType = info.imageType;
        if (info.imagefilename.toUpperCase().endsWith(".FITS") || info.imagefilename.toUpperCase().endsWith(".FIT"))
        {
            imageTypeComboBox.setModel(new DefaultComboBoxModel(ImageType.values()));
            imageTypeComboBox.setSelectedItem(currentImageType);
        }
        else
            imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));


        imageTypeComboBox.setSelectedItem(info.imageType);
        imageFlipComboBox.setSelectedItem(info.flip);
        imageRotateComboBox.setSelectedItem(Integer.toString((int)info.rotation));

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
            info.sumfilename = null;
            if (sumfilePathRB.isSelected() == true)
                info.sumfilename = sumfilePathTextField.getText();
            info.infofilename = null;
            if (infofilePathRB.isSelected() == true)
                info.infofilename = infofilePathTextField.getText();
            if (LEAVE_UNMODIFIED.equals(info.sumfilename) || info.sumfilename == null || info.sumfilename.isEmpty())
                info.sumfilename = null;
            if (LEAVE_UNMODIFIED.equals(info.infofilename) || info.infofilename == null || info.infofilename.isEmpty())
                info.infofilename = null;
        }

        // If name is not provided, set name to filename
        info.imageType = (ImageType)imageTypeComboBox.getSelectedItem();
        info.rotation = imageRotateComboBox.getSelectedIndex() * 90.0;
        info.flip = imageFlipComboBox.getSelectedItem().toString();
        info.name = imageNameTextField.getText();
        if ((info.name == null || info.name.isEmpty()) && info.imagefilename != null)
            info.name = new File(info.imagefilename).getName();

        return info;
    }

    private String validateInput()
    {
        String imagePath = imagePathTextField.getText();
        if (imagePath == null)
            imagePath = "";

        if (!isEditMode) // || (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED)))
        {
            if (imagePath.isEmpty())
                return "Please enter the path to an image.";

            File file = new File(imagePath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return imagePath + " does not exist or is not readable.";

            if (imagePath.contains(","))
                return "Image path may not contain commas.";
        }

        String imageName = imageNameTextField.getText();
        if (imageName == null)
            imageName = "";
        if (imageName.trim().isEmpty())
            return "Please enter a name for the image. The name can be any text that describes the image.";
        if (imageName.contains(","))
            return "Name may not contain commas.";

        if (cylindricalProjectionRadioButton.isSelected())
        {
            if (!isEditMode) // (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED))
            {
                // Check first to see if it is a natively supported image
                boolean supportedCustomFormat = false;

                // Check if this image is any of the custom supported formats
                if(VtkENVIReader.isENVIFilename(imagePath))
                {
                    // Both header and binary files must exist
                    if(VtkENVIReader.checkFilesExist(imagePath))
                    {
                        // SBMT supports ENVI
                        supportedCustomFormat = true;
                    }
                    else
                    {
                        // Error message
                        return "Was not able to locate a corresponding .hdr file for ENVI image binary";
                    }
                }

                // Otherwise, try to see if VTK natively supports
                if(!supportedCustomFormat)
                {
                    vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
                    vtkImageReader2 imageReader = imageFactory.CreateImageReader2(imagePath);
                    if (imageReader == null)
                        return "The format of the specified image is not supported.";
                }
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
            if (sumfilePathRB.isSelected() == false || sumfilePath == null)
                sumfilePath = "";

            String infofilePath = infofilePathTextField.getText();
            if (infofilePathRB.isSelected() == false || infofilePath == null)
                infofilePath = "";

            if (!isEditMode || (!sumfilePath.isEmpty() && !sumfilePath.equals(LEAVE_UNMODIFIED) || (!infofilePath.isEmpty() && !infofilePath.equals(LEAVE_UNMODIFIED))))
            {
                if (infofilePathRB.isSelected() == true && infofilePath.isEmpty() == true)
                    return "Please enter the path to an infofile.";

                if (sumfilePathRB.isSelected() == true && sumfilePath.isEmpty() == true)
                    return "Please enter the path to a sumfile.";

                if (!sumfilePath.isEmpty())
                {
                    File file = new File(sumfilePath);
                    if (!file.exists() || !file.canRead() || !file.isFile())
                        return sumfilePath + " does not exist or is not readable.";

                    if (sumfilePath.contains(","))
                        return "Path may not contain commas.";
                }
                else if (!infofilePath.isEmpty())
                {
                    File file = new File(infofilePath);
                    if (!file.exists() || !file.canRead() || !file.isFile())
                        return infofilePath + " does not exist or is not readable.";

                    if (infofilePath.contains(","))
                        return "Path may not contain commas.";
                }
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
        boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
        lllatLabel.setEnabled(cylindrical);
        lllatFormattedTextField.setEnabled(cylindrical);
        lllonLabel.setEnabled(cylindrical);
        lllonFormattedTextField.setEnabled(cylindrical);
        urlatLabel.setEnabled(cylindrical);
        urlatFormattedTextField.setEnabled(cylindrical);
        urlonLabel.setEnabled(cylindrical);
        urlonFormattedTextField.setEnabled(cylindrical);
        infofilePathRB.setEnabled(!cylindrical);
        infofilePathTextField.setEnabled(!cylindrical && !isEditMode && infofilePathRB.isSelected());
        sumfilePathRB.setEnabled(!cylindrical);
        sumfilePathTextField.setEnabled(!cylindrical && !isEditMode && sumfilePathRB.isSelected());

        boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
        imageFlipComboBox.setEnabled(generic && !cylindrical);
        imageRotateComboBox.setEnabled(generic && !cylindrical);
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
        infofilePathRB = new javax.swing.JRadioButton("Infofile Path", true);
        browseInfofileButton = new javax.swing.JButton();
        imageLabel = new javax.swing.JLabel();
        imageNameTextField = new javax.swing.JTextField();
        sumfilePathRB = new javax.swing.JRadioButton();
        infofilePathTextField = new javax.swing.JTextField();
        sumfilePathTextField = new javax.swing.JTextField();
        browseSumfileButton = new javax.swing.JButton();
        imageTypeLabel = new javax.swing.JLabel();
        imageTypeComboBox = new javax.swing.JComboBox();
        imageRotateLabel = new javax.swing.JLabel();
        imageFlipLabel = new javax.swing.JLabel();
        imageRotateComboBox = new javax.swing.JComboBox();
        imageFlipComboBox = new javax.swing.JComboBox();
        ButtonGroup tmpBG = new ButtonGroup();
        tmpBG.add(infofilePathRB);
        tmpBG.add(sumfilePathRB);

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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllatLabel, gridBagConstraints);

        lllonLabel.setText("Lower Left Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllonLabel, gridBagConstraints);

        urlatLabel.setText("Upper Right Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(urlatLabel, gridBagConstraints);

        urlonLabel.setText("Upper Right Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
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
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        lllatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        lllatFormattedTextField.setText("-90");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllatFormattedTextField, gridBagConstraints);

        lllonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        lllonFormattedTextField.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllonFormattedTextField, gridBagConstraints);

        urlatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        urlatFormattedTextField.setText("90");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlatFormattedTextField, gridBagConstraints);

        urlonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        urlonFormattedTextField.setText("360");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlonFormattedTextField, gridBagConstraints);

        projectionButtonGroup.add(cylindricalProjectionRadioButton);
        cylindricalProjectionRadioButton.setSelected(true);
        cylindricalProjectionRadioButton.setText("Simple Cylindrical Projection");
        cylindricalProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cylindricalProjectionRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
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
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
        getContentPane().add(perspectiveProjectionRadioButton, gridBagConstraints);

        infofilePathRB.setText("Infofile Path");
        infofilePathRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEnabledItems();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(infofilePathRB, gridBagConstraints);

        browseInfofileButton.setText("Browse...");
        browseInfofileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseInfofileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseInfofileButton, gridBagConstraints);

        imageLabel.setText("Name");
        imageLabel.setToolTipText("A name describing the image that will be displayed in the image list.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageLabel, gridBagConstraints);

        imageNameTextField.setToolTipText("A name describing the image that will be displayed in the image list.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
        getContentPane().add(imageNameTextField, gridBagConstraints);

        sumfilePathRB.setText("Sumfile Path");
        sumfilePathRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEnabledItems();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(sumfilePathRB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(infofilePathTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
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
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseSumfileButton, gridBagConstraints);

        imageTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        imageTypeLabel.setText("Image Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageTypeLabel, gridBagConstraints);

        imageTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
        imageTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageTypeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageTypeComboBox, gridBagConstraints);

        imageRotateLabel.setText("Image Rotate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageRotateLabel, gridBagConstraints);

        imageFlipLabel.setText("Image Flip");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageFlipLabel, gridBagConstraints);

        imageRotateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "90", "180", "270" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageRotateComboBox, gridBagConstraints);

        imageFlipComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "X", "Y" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageFlipComboBox, gridBagConstraints);

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

        String imageFileName = file.getName();
        if (imageFileName.toUpperCase().endsWith(".FITS") || imageFileName.toUpperCase().endsWith(".FIT"))
        {
            ImageType[] allImageTypes = ImageType.values();
            ImageType currentImageType = instrument != null ? instrument.type : ImageType.GENERIC_IMAGE;
            imageTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(allImageTypes));
            imageTypeComboBox.setSelectedItem(currentImageType);

            boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
            boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
            imageFlipComboBox.setEnabled(generic && !cylindrical);
            imageRotateComboBox.setEnabled(generic && !cylindrical);
        }
        else
        {
            imageTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
        }

        imageNameTextField.setText(imageFileName);

        // set default info file name
        String tokens[] = imageFileName.split("\\.");
        int ntokens = tokens.length;
        String suffix = tokens[ntokens-1];
        int suffixLength = suffix.length();
        String imageFileNamePrefix = imageFileName.substring(0, imageFileName.length() - suffixLength);
        String defaultInfoFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + "INFO";
        String defaultSumFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + "SUM";
        infofilePathTextField.setText(defaultInfoFileName);
        sumfilePathTextField.setText(defaultSumFileName);

        updateEnabledItems();
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

    private void browseInfofileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseInfofileButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Infofile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        infofilePathTextField.setText(filename);
    }//GEN-LAST:event_browseInfofileButtonActionPerformed

    private void browseSumfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSumfileButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Sumfile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        sumfilePathTextField.setText(filename);
    }//GEN-LAST:event_browseSumfileButtonActionPerformed

    private void imageTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageTypeComboBoxActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_imageTypeComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseImageButton;
    private javax.swing.JButton browseInfofileButton;
    private javax.swing.JButton browseSumfileButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton cylindricalProjectionRadioButton;
    private javax.swing.JComboBox imageFlipComboBox;
    private javax.swing.JLabel imageFlipLabel;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JTextField imageNameTextField;
    private javax.swing.JLabel imagePathLabel;
    private javax.swing.JTextField imagePathTextField;
    private javax.swing.JComboBox imageRotateComboBox;
    private javax.swing.JLabel imageRotateLabel;
    private javax.swing.JComboBox imageTypeComboBox;
    private javax.swing.JLabel imageTypeLabel;
    private javax.swing.JRadioButton infofilePathRB;
    private javax.swing.JTextField infofilePathTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JFormattedTextField lllatFormattedTextField;
    private javax.swing.JLabel lllatLabel;
    private javax.swing.JFormattedTextField lllonFormattedTextField;
    private javax.swing.JLabel lllonLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton perspectiveProjectionRadioButton;
    private javax.swing.ButtonGroup projectionButtonGroup;
    private javax.swing.JRadioButton sumfilePathRB;
    private javax.swing.JTextField sumfilePathTextField;
    private javax.swing.JFormattedTextField urlatFormattedTextField;
    private javax.swing.JLabel urlatLabel;
    private javax.swing.JFormattedTextField urlonFormattedTextField;
    private javax.swing.JLabel urlonLabel;
    // End of variables declaration//GEN-END:variables
}
