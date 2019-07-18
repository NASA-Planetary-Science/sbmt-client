/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.spectrum.ui;

import java.awt.Dialog;
import java.awt.Window;
import java.io.File;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer.ProjectionType;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectraType;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraType;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKeyInterface;


public class CustomSpectrumImporterDialog extends javax.swing.JDialog
{
    private boolean okayPressed = false;
    private boolean isEllipsoid;
    private boolean isEditMode;
    private ISpectralInstrument instrument;
    private static final String LEAVE_UNMODIFIED = "<cannot be changed>";

    /** Creates new form ShapeModelImporterDialog */
    public CustomSpectrumImporterDialog(Window parent, boolean isEditMode, ISpectralInstrument instrument)
    {
        super(parent, isEditMode ? "Edit Spectrum" : "Import New Spectrum", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        this.isEditMode = isEditMode;
        this.instrument = instrument;

        if (isEditMode)
        {
            browseSpectrumButton.setEnabled(false);
            browseSumfileButton.setEnabled(false);
            browseInfofileButton.setEnabled(false);
            spectrumPathTextField.setEnabled(false);
            infofilePathTextField.setEnabled(false);
            sumfilePathTextField.setEnabled(false);
        }
    }

    public void setSpectrumInfo(CustomSpectrumKeyInterface info, boolean isEllipsoid)
    {
    	String keyName = info == null ? "" : info.getName();
    	String keySpectrumFilename = info == null ? "" : info.getSpectrumFilename();
    	ProjectionType projection = ProjectionType.PERSPECTIVE;
    	ISpectraType currentSpectrumType = info == null ? SpectraTypeFactory.findSpectraTypeForDisplayName("NIS") : info.getSpectrumType();



        this.isEllipsoid = isEllipsoid;

        if (isEditMode)
            spectrumPathTextField.setText(LEAVE_UNMODIFIED);
        else
            spectrumPathTextField.setText(keySpectrumFilename);

        spectrumNameTextField.setText(keyName);

//        if (info.projectionType == ProjectionType.CYLINDRICAL)
//        {
//            cylindricalProjectionRadioButton.setSelected(true);
//
//            lllatFormattedTextField.setText(String.valueOf(info.lllat));
//            lllonFormattedTextField.setText(String.valueOf(info.lllon));
//            urlatFormattedTextField.setText(String.valueOf(info.urlat));
//            urlonFormattedTextField.setText(String.valueOf(info.urlon));
//        }
//        else if (info.projectionType == ProjectionType.PERSPECTIVE)
//        {
//            perspectiveProjectionRadioButton.setSelected(true);
//
//            if (isEditMode)
//            {
//                sumfilePathTextField.setText(LEAVE_UNMODIFIED);
//                infofilePathTextField.setText(LEAVE_UNMODIFIED);
//            }
//        }
//
//        ImageType currentImageType = info.imageType;
//        if (info.spectrumfilename.toUpperCase().endsWith(".FITS") || info.spectrumfilename.toUpperCase().endsWith(".FIT"))
//        {
//            imageTypeComboBox.setModel(new DefaultComboBoxModel(ImageType.values()));
//            imageTypeComboBox.setSelectedItem(currentImageType);
//        }
//        else
//            imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
//
//
//        imageTypeComboBox.setSelectedItem(info.imageType);
//        imageFlipComboBox.setSelectedItem(info.flip);
//        imageRotateComboBox.setSelectedItem(Integer.toString((int)info.rotation));

        updateEnabledItems();
    }

    public ProjectionType getSelectedProjectionType()
    {
            return ProjectionType.PERSPECTIVE;
    }

    public CustomSpectrumKeyInterface getSpectrumInfo()
    {
        String filename = spectrumPathTextField.getText();
        String name = spectrumNameTextField.getText();
        if (LEAVE_UNMODIFIED.equals(filename) || filename == null || filename.isEmpty())
            filename = null;

        if ((name == null || name.isEmpty()) && filename != null)
            name = new File(filename).getName();

        String pointingFilename = null;
        FileType fileType = null;
//        ImageSource source = null;
//        if (sumfilePathRB.isSelected() == true)
        if (!sumfilePathTextField.getText().equals(""))
        {
        	fileType = FileType.SUM;
        	pointingFilename = sumfilePathTextField.getText();
//        	source = ImageSource.LOCAL_PERSPECTIVE;
        }
        else
//        if (infofilePathRB.isSelected() == true)
        {
        	pointingFilename = infofilePathTextField.getText();
        	fileType = FileType.INFO;
//        	source = ImageSource.LOCAL_PERSPECTIVE;
        }
        if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
        	pointingFilename = null;
        if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
        	pointingFilename = null;


        SpectraType spectrumType = (SpectraType)spectrumTypeComboBox.getSelectedItem();

        CustomSpectrumKey info = new CustomSpectrumKey(name, fileType, instrument, spectrumType, filename, pointingFilename);

        return info;
    }

    private String validateInput()
    {
        String spectrumPath = spectrumPathTextField.getText();
        if (spectrumPath == null)
            spectrumPath = "";

        if (!isEditMode) // || (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED)))
        {
            if (spectrumPath.isEmpty())
                return "Please enter the path to a spectrum.";

            File file = new File(spectrumPath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return spectrumPath + " does not exist or is not readable.";

            if (spectrumPath.contains(","))
                return "Spectrum path may not contain commas.";
        }

        String imageName = spectrumNameTextField.getText();
        if (imageName == null)
            imageName = "";
        if (imageName.trim().isEmpty())
            return "Please enter a name for the image. The name can be any text that describes the spectrum.";
        if (imageName.contains(","))
            return "Name may not contain commas.";

//        if (cylindricalProjectionRadioButton.isSelected())
//        {
//            if (!isEditMode) // (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED))
//            {
//                // Check first to see if it is a natively supported image
//                boolean supportedCustomFormat = false;
//
//                // Check if this image is any of the custom supported formats
//                if(VtkENVIReader.isENVIFilename(spectrumPath))
//                {
//                    // Both header and binary files must exist
//                    if(VtkENVIReader.checkFilesExist(spectrumPath))
//                    {
//                        // SBMT supports ENVI
//                        supportedCustomFormat = true;
//                    }
//                    else
//                    {
//                        // Error message
//                        return "Was not able to locate a corresponding .hdr file for ENVI image binary";
//                    }
//                }
//
//                // Otherwise, try to see if VTK natively supports
//                if(!supportedCustomFormat)
//                {
//                    vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
//                    vtkImageReader2 imageReader = imageFactory.CreateImageReader2(spectrumPath);
//                    if (imageReader == null)
//                        return "The format of the specified image is not supported.";
//                }
//            }
//
//            try
//            {
//                double lllat = Double.parseDouble(lllatFormattedTextField.getText());
//                double urlat = Double.parseDouble(urlatFormattedTextField.getText());
//                Double.parseDouble(lllonFormattedTextField.getText());
//                Double.parseDouble(urlonFormattedTextField.getText());
//
//                if (lllat < -90.0 || lllat > 90.0 || urlat < -90.0 || urlat > 90.0)
//                    return "Latitudes must be between -90 and +90.";
//                if (lllat >= urlat)
//                    return "Upper right latitude must be greater than lower left latitude.";
//
//                if (!isEllipsoid)
//                {
//                    if ( (lllat < 1.0 && lllat > 0.0) || (lllat > -1.0 && lllat < 0.0) ||
//                            (urlat < 1.0 && urlat > 0.0) || (urlat > -1.0 && urlat < 0.0) )
//                        return "For non-ellipsoidal shape models, latitudes must be (in degrees) either 0, greater than +1, or less then -1.";
//                }
//            }
//            catch (NumberFormatException e)
//            {
//                return "An error occurred parsing one of the required fields.";
//            }
//        }
//        else
        {
            String sumfilePath = sumfilePathTextField.getText();
            if (sumfilePath == null)
                sumfilePath = "";

            String infofilePath = infofilePathTextField.getText();
            if (infofilePath == null)
                infofilePath = "";

            System.out.println("CustomSpectrumImporterDialog: validateInput: spectra is " + spectrumTypeComboBox.getSelectedItem());
            if (!spectrumTypeComboBox.getSelectedItem().toString().equals("NIS_SPECTRA"))
            {
	            if (!isEditMode || (!sumfilePath.isEmpty() && !sumfilePath.equals(LEAVE_UNMODIFIED) || (!infofilePath.isEmpty() && !infofilePath.equals(LEAVE_UNMODIFIED))))
	            {
	                if (sumfilePath.isEmpty() && infofilePath.isEmpty())
	                    return "Please enter the path to a sumfile or infofile.";

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
        }

        return null;
    }

    public boolean getOkayPressed()
    {
        return okayPressed;
    }

    private void updateEnabledItems()
    {
//        boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
//        lllatLabel.setEnabled(cylindrical);
//        lllatFormattedTextField.setEnabled(cylindrical);
//        lllonLabel.setEnabled(cylindrical);
//        lllonFormattedTextField.setEnabled(cylindrical);
//        urlatLabel.setEnabled(cylindrical);
//        urlatFormattedTextField.setEnabled(cylindrical);
//        urlonLabel.setEnabled(cylindrical);
//        urlonFormattedTextField.setEnabled(cylindrical);
        boolean cylindrical = false;
        infofilePathLabel.setEnabled(!cylindrical);
        infofilePathTextField.setEnabled(!cylindrical && !isEditMode);
        sumfilePathLabel.setEnabled(!cylindrical);
        sumfilePathTextField.setEnabled(!cylindrical && !isEditMode);

        boolean generic = spectrumTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
//        imageFlipComboBox.setEnabled(generic && !cylindrical);
//        imageRotateComboBox.setEnabled(generic && !cylindrical);
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
        spectrumPathLabel = new javax.swing.JLabel();
        spectrumPathTextField = new javax.swing.JTextField();
        browseSpectrumButton = new javax.swing.JButton();
//        lllatLabel = new javax.swing.JLabel();
//        lllonLabel = new javax.swing.JLabel();
//        urlatLabel = new javax.swing.JLabel();
//        urlonLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
//        lllatFormattedTextField = new javax.swing.JFormattedTextField();
//        lllonFormattedTextField = new javax.swing.JFormattedTextField();
//        urlatFormattedTextField = new javax.swing.JFormattedTextField();
//        urlonFormattedTextField = new javax.swing.JFormattedTextField();
//        cylindricalProjectionRadioButton = new javax.swing.JRadioButton();
        perspectiveProjectionRadioButton = new javax.swing.JRadioButton();
        infofilePathLabel = new javax.swing.JLabel();
        browseInfofileButton = new javax.swing.JButton();
        spectrumLabel = new javax.swing.JLabel();
        spectrumNameTextField = new javax.swing.JTextField();
        sumfilePathLabel = new javax.swing.JLabel();
        infofilePathTextField = new javax.swing.JTextField();
        sumfilePathTextField = new javax.swing.JTextField();
        browseSumfileButton = new javax.swing.JButton();
        spectrumTypeLabel = new javax.swing.JLabel();
        spectrumTypeComboBox = new javax.swing.JComboBox();
//        imageRotateLabel = new javax.swing.JLabel();
//        imageFlipLabel = new javax.swing.JLabel();
//        imageRotateComboBox = new javax.swing.JComboBox();
//        imageFlipComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 167));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        spectrumPathLabel.setText("Spectrum Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        getContentPane().add(spectrumPathLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
        getContentPane().add(spectrumPathTextField, gridBagConstraints);

        browseSpectrumButton.setText("Browse...");
        browseSpectrumButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 5);
        getContentPane().add(browseSpectrumButton, gridBagConstraints);

//        lllatLabel.setText("Lower Left Latitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(lllatLabel, gridBagConstraints);
//
//        lllonLabel.setText("Lower Left Longitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 7;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(lllonLabel, gridBagConstraints);
//
//        urlatLabel.setText("Upper Right Latitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(urlatLabel, gridBagConstraints);
//
//        urlonLabel.setText("Upper Right Longitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 9;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(urlonLabel, gridBagConstraints);

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

//        lllatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        lllatFormattedTextField.setText("-90");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(lllatFormattedTextField, gridBagConstraints);
//
//        lllonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        lllonFormattedTextField.setText("0");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 7;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(lllonFormattedTextField, gridBagConstraints);
//
//        urlatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        urlatFormattedTextField.setText("90");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(urlatFormattedTextField, gridBagConstraints);
//
//        urlonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        urlonFormattedTextField.setText("360");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 9;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(urlonFormattedTextField, gridBagConstraints);

//        projectionButtonGroup.add(cylindricalProjectionRadioButton);
//        cylindricalProjectionRadioButton.setSelected(true);
//        cylindricalProjectionRadioButton.setText("Simple Cylindrical Projection");
//        cylindricalProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                cylindricalProjectionRadioButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 5;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
//        getContentPane().add(cylindricalProjectionRadioButton, gridBagConstraints);

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

        infofilePathLabel.setText("Infofile Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(infofilePathLabel, gridBagConstraints);

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

        spectrumLabel.setText("Name");
        spectrumLabel.setToolTipText("A name describing the spectrum that will be displayed in the spectrum list.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(spectrumLabel, gridBagConstraints);

        spectrumNameTextField.setToolTipText("A name describing the spectrum that will be displayed in the spectrum list.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
        getContentPane().add(spectrumNameTextField, gridBagConstraints);

        sumfilePathLabel.setText("Sumfile Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(sumfilePathLabel, gridBagConstraints);
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

        spectrumTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        spectrumTypeLabel.setText("Spectrum Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(spectrumTypeLabel, gridBagConstraints);

        spectrumTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(SpectraTypeFactory.values()));
        spectrumTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageTypeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(spectrumTypeComboBox, gridBagConstraints);
//
//        imageRotateLabel.setText("Image Rotate");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageRotateLabel, gridBagConstraints);
//
//        imageFlipLabel.setText("Image Flip");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 4;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageFlipLabel, gridBagConstraints);
//
//        imageRotateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "90", "180", "270" }));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageRotateComboBox, gridBagConstraints);
//
//        imageFlipComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "X", "Y" }));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 4;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageFlipComboBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browseImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseImageButtonActionPerformed
    {//GEN-HEADEREND:event_browseImageButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Spectrum");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        spectrumPathTextField.setText(filename);

        String imageFileName = file.getName();
//        if (imageFileName.toUpperCase().endsWith(".FITS") || imageFileName.toUpperCase().endsWith(".FIT"))
//        {
////            ImageType[] allImageTypes = ImageType.values();
////            ImageType currentImageType = instrument != null ? instrument.type : ImageType.GENERIC_IMAGE;
////            imageTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(allImageTypes));
////            imageTypeComboBox.setSelectedItem(currentImageType);
////
////            boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
////            boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
////            imageFlipComboBox.setEnabled(generic && !cylindrical);
////            imageRotateComboBox.setEnabled(generic && !cylindrical);
//        }
//        else
//        {
//            spectrumTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
//        }

        spectrumNameTextField.setText(imageFileName);

        // set default info file name
        String tokens[] = imageFileName.split("\\.");
        int ntokens = tokens.length;
        String suffix = tokens[ntokens-1];
        int suffixLength = suffix.length();
        String imageFileNamePrefix = imageFileName.substring(0, imageFileName.length() - suffixLength);
        String defaultInfoFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + "INFO";
        infofilePathTextField.setText(defaultInfoFileName);

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
    private javax.swing.JButton browseSpectrumButton;
    private javax.swing.JButton browseInfofileButton;
    private javax.swing.JButton browseSumfileButton;
    private javax.swing.JButton cancelButton;
//    private javax.swing.JRadioButton cylindricalProjectionRadioButton;
//    private javax.swing.JComboBox imageFlipComboBox;
//    private javax.swing.JLabel imageFlipLabel;
    private javax.swing.JLabel spectrumLabel;
    private javax.swing.JTextField spectrumNameTextField;
    private javax.swing.JLabel spectrumPathLabel;
    private javax.swing.JTextField spectrumPathTextField;
//    private javax.swing.JComboBox imageRotateComboBox;
//    private javax.swing.JLabel imageRotateLabel;
    private javax.swing.JComboBox spectrumTypeComboBox;
    private javax.swing.JLabel spectrumTypeLabel;
    private javax.swing.JLabel infofilePathLabel;
    private javax.swing.JTextField infofilePathTextField;
    private javax.swing.JPanel jPanel1;
//    private javax.swing.JFormattedTextField lllatFormattedTextField;
//    private javax.swing.JLabel lllatLabel;
//    private javax.swing.JFormattedTextField lllonFormattedTextField;
//    private javax.swing.JLabel lllonLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton perspectiveProjectionRadioButton;
    private javax.swing.ButtonGroup projectionButtonGroup;
    private javax.swing.JLabel sumfilePathLabel;
    private javax.swing.JTextField sumfilePathTextField;
//    private javax.swing.JFormattedTextField urlatFormattedTextField;
//    private javax.swing.JLabel urlatLabel;
//    private javax.swing.JFormattedTextField urlonFormattedTextField;
//    private javax.swing.JLabel urlonLabel;
    // End of variables declaration//GEN-END:variables
}
