/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelManagerDialog.java
 *
 * Created on Jul 21, 2011, 1:28:04 PM
 */
package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Properties;


public class ShapeModelImporterManagerDialog extends javax.swing.JDialog
{
    /** Creates new form ShapeModelManagerDialog */
    public ShapeModelImporterManagerDialog(java.awt.Frame parent)
    {
        super(parent, "Import Shape Models", false);
        initComponents();

        modelList.setModel(new DefaultListModel());
        populateList();
    }

    private void populateList()
    {
        ((DefaultListModel)modelList.getModel()).clear();

        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            Arrays.sort(dirs);
            for (File dir : dirs)
            {
                if (dir.isDirectory())
                {
                    ((DefaultListModel)modelList.getModel()).addElement(dir.getName());
                }
            }
        }
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

        jScrollPane1 = new javax.swing.JScrollPane();
        modelList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        duplicateButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(400, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        modelList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(modelList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        newButton.setText("New...");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        getContentPane().add(newButton, gridBagConstraints);

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        getContentPane().add(removeButton, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        getContentPane().add(closeButton, gridBagConstraints);

        editButton.setText("Edit...");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        getContentPane().add(editButton, gridBagConstraints);

        duplicateButton.setText("Duplicate...");
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        getContentPane().add(duplicateButton, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        int numImportedModels = ((DefaultListModel)modelList.getModel()).getSize();

        ShapeModelImporterDialog dialog = new ShapeModelImporterDialog(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        populateList();

        if (numImportedModels < ((DefaultListModel)modelList.getModel()).getSize())
        {
            this.firePropertyChange(Properties.CUSTOM_MODEL_ADDED, "", dialog.getNameOfImportedShapeModel());
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
    {//GEN-HEADEREND:event_removeButtonActionPerformed
        int idx = modelList.getSelectedIndex();
        if (idx >= 0)
        {
            String dirname = (String)((DefaultListModel)modelList.getModel()).remove(idx);

            // Remove all the files also
            String dirpath = Configuration.getImportedShapeModelsDir() + File.separator + dirname;
            FileUtils.deleteQuietly(new File(dirpath));

            this.firePropertyChange(Properties.CUSTOM_MODEL_DELETED, "", dirname);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_editButtonActionPerformed
        int idx = modelList.getSelectedIndex();
        if (idx >= 0)
        {
            String dirname = (String)((DefaultListModel)modelList.getModel()).get(idx);
            String dirpath = Configuration.getImportedShapeModelsDir() + File.separator + dirname;

            ShapeModelImporterDialog dialog = new ShapeModelImporterDialog(this);

            try
            {
                dialog.loadConfig(dirpath + File.separator + "config.txt");
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this,
                        "The selected shape model may not be edited.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            dialog.setEditMode(true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            if (dialog.getOkayPressed())
                this.firePropertyChange(Properties.CUSTOM_MODEL_EDITED, "", dirname);
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_duplicateButtonActionPerformed
        int idx = modelList.getSelectedIndex();
        if (idx >= 0)
        {
            String dirname = (String)((DefaultListModel)modelList.getModel()).get(idx);
            String dirpath = Configuration.getImportedShapeModelsDir() + File.separator + dirname;

            int numImportedModels = ((DefaultListModel)modelList.getModel()).getSize();

            ShapeModelImporterDialog dialog = new ShapeModelImporterDialog(this);

            try
            {
                dialog.loadConfig(dirpath + File.separator + "config.txt");
            }
            catch (IOException ex)
            {
                // Silently ignore. The form will be empty. Not a big deal.
            }

            String newUniqueName = findUniqueName(dirname);
            dialog.setName(newUniqueName);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            populateList();

            if (numImportedModels < ((DefaultListModel)modelList.getModel()).getSize())
            {
                this.firePropertyChange(Properties.CUSTOM_MODEL_ADDED, "", dialog.getNameOfImportedShapeModel());
            }
        }
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private String findUniqueName(String dirname)
    {
        // Choose a new name with an integer appended to the end
        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        HashSet<String> dirNames = new HashSet<String>();
        if (dirs != null && dirs.length > 0)
        {
            for (File f : dirs)
                dirNames.add(f.getName());

            for (int i=1; i<Integer.MAX_VALUE; ++i)
            {
                String newName = dirname + "-" + i;
                if (!dirNames.contains(newName))
                    return newName;
            }
        }

        // Should never reach here.
        return dirname;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList modelList;
    private javax.swing.JButton newButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
