///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
///*
// * ShapeModelImporterDialog.java
// *
// * Created on Jul 21, 2011, 9:00:24 PM
// */
//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//import java.awt.Dialog;
//import java.io.File;
//
//import javax.swing.JOptionPane;
//
//
//public class StateHistoryImporterDialog extends javax.swing.JDialog
//{
//    private boolean okayPressed = false;
//    private boolean isEditMode;
//    private static final String LEAVE_UNMODIFIED = "<leave unmodified or empty to use existing run>";
//
//    public static class RunInfo
//    {
//        public String name = "";        // name to call this run for display purposes
//        public String runfilename = ""; // filename of run on disk
//
//        @Override
//        public String toString()
//        {
//            return name;
//        }
//    }
//
//    /** Creates new form ShapeModelImporterDialog */
//    public StateHistoryImporterDialog(java.awt.Window parent, boolean isEditMode)
//    {
//        super(parent, "Add Time History", Dialog.ModalityType.APPLICATION_MODAL);
//        initComponents();
//        this.isEditMode = isEditMode;
//    }
//
//    public void setRunInfo(RunInfo info, boolean isEllipsoid)
//    {
//        if (isEditMode)
//            simRunsPathTextField.setText(LEAVE_UNMODIFIED);
//
//        simRunsNameTextField.setText(info.name);
//
//        updateEnabledItems();
//    }
//
//    public RunInfo getStateHistoryInfo()
//    {
//        RunInfo info = new RunInfo();
//
//        info.runfilename = simRunsPathTextField.getText();
//        if (LEAVE_UNMODIFIED.equals(info.runfilename) || info.runfilename == null || info.runfilename.isEmpty())
//            info.runfilename = null;
//
//        info.name = simRunsNameTextField.getText();
//
//        return info;
//    }
//
//    private String validateInput()
//    {
//        String runPath = simRunsPathTextField.getText();
//        if (runPath == null)
//            runPath = "";
//
//        if (!isEditMode || (!runPath.isEmpty() && !runPath.equals(LEAVE_UNMODIFIED)))
//        {
//            if (runPath.isEmpty())
//                return "Please enter the path to a history (.history) file.";
//
//            File file = new File(runPath);
//            if (!file.exists() || !file.canRead() || !file.isFile())
//                return runPath + " does not exist or is not readable.";
//
//            if (runPath.contains(","))
//                return "History name may not contain commas.";
//        }
//
//        String runName = simRunsNameTextField.getText();
//        if (runName == null)
//            runName = "";
//        if (runName.trim().isEmpty())
//            return "Please enter a name for the time history.";
//        if (runName.contains(","))
//            return "Name may not contain commas.";
//
//        return null;
//    }
//
//    public boolean getOkayPressed()
//    {
//        return okayPressed;
//    }
//
//    private void updateEnabledItems()
//    {
//    }
//
//    private void initComponents() {
//        java.awt.GridBagConstraints gridBagConstraints;
//
//        runPathLabel = new javax.swing.JLabel();
//        simRunsPathTextField = new javax.swing.JTextField();
//        browseRunButton = new javax.swing.JButton();
//        jPanel1 = new javax.swing.JPanel();
//        cancelButton = new javax.swing.JButton();
//        okButton = new javax.swing.JButton();
//        simRunsLabel = new javax.swing.JLabel();
//        simRunsNameTextField = new javax.swing.JTextField();
//
//        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
//        setMinimumSize(new java.awt.Dimension(600, 167));
//        getContentPane().setLayout(new java.awt.GridBagLayout());
//
//        runPathLabel.setText("History (.history) File");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
//        getContentPane().add(runPathLabel, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 400;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
//        getContentPane().add(simRunsPathTextField, gridBagConstraints);
//
//        browseRunButton.setText("Browse...");
//        browseRunButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                browseRunButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 5);
//        getContentPane().add(browseRunButton, gridBagConstraints);
//
//        jPanel1.setLayout(new java.awt.GridBagLayout());
//
//        cancelButton.setText("Cancel");
//        cancelButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                cancelButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//        jPanel1.add(cancelButton, gridBagConstraints);
//
//        okButton.setText("OK");
//        okButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                okButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        jPanel1.add(okButton, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 9;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
//        gridBagConstraints.weighty = 1.0;
//        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
//        getContentPane().add(jPanel1, gridBagConstraints);
//
//        simRunsLabel.setText("Name");
//        simRunsLabel.setToolTipText("A name describing the time history.");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(simRunsLabel, gridBagConstraints);
//
//        simRunsNameTextField.setToolTipText("A name describing the time history that will be displayed in the time interval list.");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 400;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
//        getContentPane().add(simRunsNameTextField, gridBagConstraints);
//
//        pack();
//    }
//
//    private void browseRunButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseRunButtonActionPerformed
//    {
//        File file = StateHistoryFileChooser.showOpenDialog(this, "Select Time History (.history) File", "history");
//        if (file == null)
//        {
//            return;
//        }
//
//        String filename = file.getAbsolutePath();
//        simRunsPathTextField.setText(filename);
//
//        // If no name has yet been provided, set it to the filename
//        if (simRunsNameTextField.getText() == null || simRunsNameTextField.getText().isEmpty())
//        {
//            simRunsNameTextField.setText(file.getParentFile().getName());
//        }
//    }
//
//    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
//    {
//        setVisible(false);
//    }
//
//    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)
//    {
//        String errorString = validateInput();
//        if (errorString != null)
//        {
//            JOptionPane.showMessageDialog(this,
//                    errorString,
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        okayPressed = true;
//        setVisible(false);
//    }
//
//
//    private javax.swing.JButton browseRunButton;
//    private javax.swing.JButton cancelButton;
//    private javax.swing.JLabel simRunsLabel;
//    private javax.swing.JTextField simRunsNameTextField;
//    private javax.swing.JLabel runPathLabel;
//    private javax.swing.JTextField simRunsPathTextField;
//    private javax.swing.JPanel jPanel1;
//    private javax.swing.JButton okButton;
//}
