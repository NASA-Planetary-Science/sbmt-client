/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.spectrum.ui.custom;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraType;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectraType;


public class CustomSpectrumImporterDialog extends JDialog
{
    private boolean okayPressed = false;
    private boolean isEditMode;
    private BasicSpectrumInstrument instrument;
    private static final String LEAVE_UNMODIFIED = "<cannot be changed>";
    private static final String MAKE_SELECTION = "<Choose Spectrum Type>";
    private String customDir;
    private SpectraHierarchicalSearchSpecification spectraSpec;

    /** Creates new form ShapeModelImporterDialog */
    public CustomSpectrumImporterDialog(Window parent, boolean isEditMode, BasicSpectrumInstrument instrument, String customDir, SpectraHierarchicalSearchSpecification spectraSpec)
    {
        super(parent, isEditMode ? "Edit Spectrum" : "Import New Spectrum", Dialog.ModalityType.APPLICATION_MODAL);
        this.instrument = instrument;
        this.spectraSpec = spectraSpec;
        initComponents();
        this.isEditMode = isEditMode;
        this.customDir = customDir;

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

    public void setSpectrumInfo(CustomSpectrumKeyInterface info)
    {
    	String keyName = info == null ? "" : info.getName();
    	String keySpectrumFilename = info == null ? "" : info.getSpectrumFilename();
    	ISpectraType currentSpectrumType = info == null ? SpectraTypeFactory.findSpectraTypeForDisplayName("NIS") : info.getSpectrumType();

        if (isEditMode)
        {
            spectrumPathTextField.setText(LEAVE_UNMODIFIED);
            if (info.getFileType() == FileType.SUM)
            	sumfilePathTextField.setText(info.getPointingFilename());
            else
            	infofilePathTextField.setText(info.getPointingFilename());
        }
        else
            spectrumPathTextField.setText(keySpectrumFilename);

        spectrumNameTextField.setText(keyName);

        updateEnabledItems();
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
        if (!sumfilePathTextField.getText().equals(""))
        {
        	fileType = FileType.SUM;
        	pointingFilename = sumfilePathTextField.getText();
        }
        else
        {
        	pointingFilename = infofilePathTextField.getText();
        	fileType = FileType.INFO;
        }
        if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
        	pointingFilename = null;
        if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
        	pointingFilename = null;


        SpectraType spectrumType = (SpectraType)spectrumTypeComboBox.getSelectedItem();
        SpectrumSearchSpec searchSpec = (SpectrumSearchSpec)spectrumSubTypeComboBox.getSelectedItem();
        CustomSpectrumKey info = new CustomSpectrumKey(name, fileType, instrument, spectrumType, filename, pointingFilename, searchSpec);

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


        String sumfilePath = sumfilePathTextField.getText();
        if (sumfilePath == null)
            sumfilePath = "";

        String infofilePath = infofilePathTextField.getText();
        if (infofilePath == null)
            infofilePath = "";


        if (!isEditMode || (!sumfilePath.isEmpty() && !sumfilePath.equals(LEAVE_UNMODIFIED) || (!infofilePath.isEmpty() && !infofilePath.equals(LEAVE_UNMODIFIED))))
        {
            if (sumfilePath.isEmpty() && infofilePath.isEmpty())
                return "Please enter the path to a sumfile or infofile.";

            if (!sumfilePath.isEmpty())
            {
                File file = new File(customDir, sumfilePath);
                if (!file.exists() || !file.canRead() || !file.isFile())
                    return sumfilePath + " does not exist or is not readable.";

                if (sumfilePath.contains(","))
                    return "Path may not contain commas.";
            }
            else if (!infofilePath.isEmpty())
            {
            	File file = null;
            	if (isEditMode)
            		file = new File(customDir, infofilePath);
            	else
                    file = new File(infofilePath);
                if (!file.exists() || !file.canRead() || !file.isFile())
                    return infofilePath + " does not exist or is not readable.";

                if (infofilePath.contains(","))
                    return "Path may not contain commas.";
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
        boolean cylindrical = false;
        infofilePathLabel.setEnabled(!cylindrical);
        infofilePathTextField.setEnabled(!cylindrical && !isEditMode);
        sumfilePathLabel.setEnabled(!cylindrical);
        sumfilePathTextField.setEnabled(!cylindrical && !isEditMode);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        spectrumPathLabel = new JLabel();
        spectrumPathTextField = new JTextField();
        browseSpectrumButton = new JButton();

        jPanel1 = new JPanel();
        cancelButton = new JButton();
        okButton = new JButton();

        infofilePathLabel = new JLabel();
        browseInfofileButton = new JButton();
        spectrumLabel = new JLabel();
        spectrumNameTextField = new JTextField();
        sumfilePathLabel = new JLabel();
        infofilePathTextField = new JTextField();
        sumfilePathTextField = new JTextField();
        browseSumfileButton = new JButton();
        spectrumTypeLabel = new JLabel();
        spectrumTypeComboBox = new JComboBox();
        spectrumSubTypeComboBox = new JComboBox<SpectrumSearchSpec>();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 167));
        getContentPane().setLayout(new GridBagLayout());

        spectrumPathLabel.setText("Spectrum Path");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        getContentPane().add(spectrumPathLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(6, 5, 4, 0);
        getContentPane().add(spectrumPathTextField, gridBagConstraints);

        browseSpectrumButton.setText("Browse...");
        browseSpectrumButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(6, 5, 4, 5);
        getContentPane().add(browseSpectrumButton, gridBagConstraints);

        jPanel1.setLayout(new GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        jPanel1.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        jPanel1.add(okButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        infofilePathLabel.setText("Infofile Path");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 25, 0, 0);
        getContentPane().add(infofilePathLabel, gridBagConstraints);

        browseInfofileButton.setText("Browse...");
        browseInfofileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseInfofileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 4, 5);
        getContentPane().add(browseInfofileButton, gridBagConstraints);

        spectrumLabel.setText("Name");
        spectrumLabel.setToolTipText("A name describing the spectrum that will be displayed in the spectrum list.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        getContentPane().add(spectrumLabel, gridBagConstraints);

        spectrumNameTextField.setToolTipText("A name describing the spectrum that will be displayed in the spectrum list.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(6, 5, 4, 0);
        getContentPane().add(spectrumNameTextField, gridBagConstraints);

        sumfilePathLabel.setText("Sumfile Path");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 25, 0, 0);
        getContentPane().add(sumfilePathLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 4, 0);
        getContentPane().add(infofilePathTextField, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 4, 0);
        getContentPane().add(sumfilePathTextField, gridBagConstraints);

        browseSumfileButton.setText("Browse...");
        browseSumfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseSumfileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 4, 5);
        getContentPane().add(browseSumfileButton, gridBagConstraints);

        spectrumTypeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        spectrumTypeLabel.setText("Spectrum Type");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        getContentPane().add(spectrumTypeLabel, gridBagConstraints);

        SpectraType[] spectraTypes = SpectraTypeFactory.values();
        DefaultComboBoxModel<SpectraType> comboBoxModel = new DefaultComboBoxModel<SpectraType>();
        for (SpectraType type : spectraTypes)
        {
        	String typeName = type.getDisplayName();
        	if (typeName.contains("_")) typeName = typeName.substring(0, typeName.indexOf("_"));
        	if (instrument.getDisplayName().equals(typeName))
        		comboBoxModel.addElement(type);
        }

        spectrumTypeComboBox.setModel(comboBoxModel);
        spectrumTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                spectrumTypeComboBoxActionPerformed(evt);
            }
        });
        spectrumTypeComboBox.setSelectedIndex(0);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        getContentPane().add(spectrumTypeComboBox, gridBagConstraints);


        InstrumentMetadata instrumentMetadata = spectraSpec.getInstrumentMetadata(instrument.getDisplayName());
        List<SpectrumSearchSpec> specs = instrumentMetadata.getSpecs();
        DefaultComboBoxModel<SpectrumSearchSpec> comboBoxModel2 = new DefaultComboBoxModel<SpectrumSearchSpec>();
        for (SpectrumSearchSpec subSpec : specs)
        {
        	comboBoxModel2.addElement(subSpec);
        }
        spectrumSubTypeComboBox.setModel(comboBoxModel2);
        spectrumSubTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                spectrumSubTypeComboBoxActionPerformed(evt);
            }
        });
        spectrumSubTypeComboBox.setSelectedIndex(0);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 110, 0, 0);
        getContentPane().add(spectrumSubTypeComboBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browseImageButtonActionPerformed(ActionEvent evt)
    {
        File file = CustomFileChooser.showOpenDialog(this, "Select Spectrum");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        spectrumPathTextField.setText(filename);

        String imageFileName = file.getName();

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
    }

    private void cancelButtonActionPerformed(ActionEvent evt)
    {
        setVisible(false);
    }

    private void okButtonActionPerformed(ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {
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
    }

    private void cylindricalProjectionRadioButtonActionPerformed(ActionEvent evt) {
        updateEnabledItems();
    }

    private void perspectiveProjectionRadioButtonActionPerformed(ActionEvent evt) {
        updateEnabledItems();
    }

    private void browseInfofileButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseInfofileButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Infofile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        infofilePathTextField.setText(filename);
    }

    private void browseSumfileButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showOpenDialog(this, "Select Sumfile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        sumfilePathTextField.setText(filename);
    }

    private void spectrumTypeComboBoxActionPerformed(ActionEvent evt) {

        updateEnabledItems();
    }

    private void spectrumSubTypeComboBoxActionPerformed(ActionEvent evt)
    {
        updateEnabledItems();
    }

    private JButton browseSpectrumButton;
    private JButton browseInfofileButton;
    private JButton browseSumfileButton;
    private JButton cancelButton;
    private JLabel spectrumLabel;
    private JTextField spectrumNameTextField;
    private JLabel spectrumPathLabel;
    private JTextField spectrumPathTextField;
    private JComboBox spectrumTypeComboBox;
    private JComboBox<SpectrumSearchSpec> spectrumSubTypeComboBox;
    private JLabel spectrumTypeLabel;
    private JLabel infofilePathLabel;
    private JTextField infofilePathTextField;
    private JPanel jPanel1;
    private JButton okButton;
    private JLabel sumfilePathLabel;
    private JTextField sumfilePathTextField;
}
