/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.near.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.SmallBodyModel.OlaDatasourceInfo;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.util.MapUtil;


public class CustomLidarDataDialog extends javax.swing.JDialog {

    private ModelManager modelManager;

    /** Creates new form CustomImageLoaderPanel */
    public CustomLidarDataDialog(
            final ModelManager modelManager)
    {
        this.modelManager = modelManager;

        initComponents();

        lidarDatasourceList.setModel(new DefaultListModel());

        initializeList();

        pack();
    }

    private void initializeList()
    {
        ((DefaultListModel)lidarDatasourceList.getModel()).clear();
        ArrayList<OlaDatasourceInfo> list = modelManager.getSmallBodyModel().getOlaDasourceInfoList();
        for (OlaDatasourceInfo info : list)
            ((DefaultListModel)lidarDatasourceList.getModel()).addElement(info);
    }

    private String getCustomDataFolder()
    {
        return modelManager.getSmallBodyModel().getCustomDataFolder();
    }

    private String getConfigFilename()
    {
        return modelManager.getSmallBodyModel().getConfigFilename();
    }

    private void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getConfigFilename());

        // Load in the plate data
        String olaDatasourcePath = "";
        String olaDatasourceName = "";

//        // We need to make sure to save out data from other resolutions without modification.
//        if (configMap.containsKey(SmallBodyModel.OLA_DATASOURCE_PATHS) &&
//                configMap.containsKey(SmallBodyModel.OLA_DATASOURCE_NAMES))
//        {
//            String[] cellDataFilenamesArr = configMap.get(SmallBodyModel.OLA_DATASOURCE_PATHS).split(",", -1);
//            String[] cellDataNamesArr = configMap.get(SmallBodyModel.OLA_DATASOURCE_NAMES).split(",", -1);
//
//            int resolution = modelManager.getSmallBodyModel().getModelResolution();
//            for (int i=0; i<cellDataFilenamesArr.length; ++i)
//            {
//                cellDataFilenames += cellDataFilenamesArr[i];
//                cellDataNames += cellDataNamesArr[i];
//
//                if (i < cellDataFilenamesArr.length-1)
//                {
//                    cellDataFilenames += CustomShapeModel.LIST_SEPARATOR;
//                    cellDataNames += CustomShapeModel.LIST_SEPARATOR;
//                }
//            }
//        }


        DefaultListModel cellDataListModel = (DefaultListModel)lidarDatasourceList.getModel();
        for (int i=0; i<cellDataListModel.size(); ++i)
        {
            OlaDatasourceInfo olaDatasourceInfo = (OlaDatasourceInfo)cellDataListModel.get(i);

            olaDatasourcePath += olaDatasourceInfo.path;
            olaDatasourceName += olaDatasourceInfo.name;

            if (i < cellDataListModel.size()-1)
            {
                olaDatasourcePath += CustomShapeModel.LIST_SEPARATOR;
                olaDatasourceName += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(SmallBodyModel.OLA_DATASOURCE_PATHS, olaDatasourcePath);
        newMap.put(SmallBodyModel.OLA_DATASOURCE_NAMES, olaDatasourceName);

        configMap.put(newMap);
    }

    private void saveOlaDatasourceData(int index, OlaDatasourceInfo oldOlaDatasourceInfo, OlaDatasourceInfo newOlaDatasourceInfo)
    {
        String uuid = UUID.randomUUID().toString();

        // If File is the same as the old File,
        // that means we are in edit mode and and the user did not change to a new file.
        if (oldOlaDatasourceInfo == null || !newOlaDatasourceInfo.path.equals(oldOlaDatasourceInfo.path))
        {
        }

        DefaultListModel model = (DefaultListModel)lidarDatasourceList.getModel();
        if (index >= model.getSize())
        {
            model.addElement(newOlaDatasourceInfo);
        }
        else
        {
            model.set(index, newOlaDatasourceInfo);
        }

        updateConfigFile();
    }

    private void removeOlaDatasource(int index)
    {
        try
        {
            OlaDatasourceInfo olaDatasourceInfo = (OlaDatasourceInfo)((DefaultListModel)lidarDatasourceList.getModel()).get(index);
            String filename = getCustomDataFolder() + File.separator + olaDatasourceInfo.path;
            new File(filename).delete();

            modelManager.getSmallBodyModel().removeCustomPlateData(index);
            ((DefaultListModel)lidarDatasourceList.getModel()).remove(index);
            updateConfigFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
        lidarDatasourceList = new javax.swing.JList();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();

        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lidarDatasourceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lidarDatasourceListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lidarDatasourceList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 377;
        gridBagConstraints.ipady = 241;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        newButton.setText("New...");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        getContentPane().add(newButton, gridBagConstraints);

        deleteButton.setText("Remove");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        getContentPane().add(deleteButton, gridBagConstraints);

        editButton.setText("Edit...");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        getContentPane().add(editButton, gridBagConstraints);

        jLabel1.setText("Lidar Data Sources");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 6, 0, 0);
        getContentPane().add(closeButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        try
        {
            OlaDatasourceInfo olaDatasourceInfo = new OlaDatasourceInfo();
            CustomLidarDataImporterDialog dialog = new CustomLidarDataImporterDialog(JOptionPane.getFrameForComponent(this), false);
            dialog.setOlaDatasourceInfo(olaDatasourceInfo, modelManager.getSmallBodyModel().getSmallBodyPolyData().GetNumberOfCells());
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // If user clicks okay add to list
            if (dialog.getOkayPressed())
            {
                olaDatasourceInfo = dialog.getOlaDatasourceInfo();
//                if (olaDatasourceInfo.coloringFile.toLowerCase().endsWith(".fit") || olaDatasourceInfo.coloringFile.toLowerCase().endsWith(".fits"))
//                    olaDatasourceInfo.format = Format.FIT;
                saveOlaDatasourceData(((DefaultListModel)lidarDatasourceList.getModel()).getSize(), null, olaDatasourceInfo);
                modelManager.getSmallBodyModel().addCustomOlaDatasource(olaDatasourceInfo);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int selectedItem = lidarDatasourceList.getSelectedIndex();
        if (selectedItem >= 0)
        {
            removeOlaDatasource(selectedItem);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        try
        {
            int selectedItem = lidarDatasourceList.getSelectedIndex();
            if (selectedItem >= 0)
            {
                DefaultListModel cellDataListModel = (DefaultListModel)lidarDatasourceList.getModel();
                OlaDatasourceInfo oldOlaDatasourceInfo = (OlaDatasourceInfo)cellDataListModel.get(selectedItem);

                CustomLidarDataImporterDialog dialog = new CustomLidarDataImporterDialog(JOptionPane.getFrameForComponent(this), true);
                dialog.setOlaDatasourceInfo(oldOlaDatasourceInfo, modelManager.getSmallBodyModel().getSmallBodyPolyData().GetNumberOfCells());
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);

                // If user clicks okay replace item in list
                if (dialog.getOkayPressed())
                {
                    OlaDatasourceInfo cellDataInfo = dialog.getOlaDatasourceInfo();
                    saveOlaDatasourceData(selectedItem, oldOlaDatasourceInfo, cellDataInfo);
                    modelManager.getSmallBodyModel().setCustomOlaDatasource(selectedItem, cellDataInfo);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void lidarDatasourceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lidarDatasourceListValueChanged
        int selectedItem = lidarDatasourceList.getSelectedIndex();
        if (selectedItem >= 0)
        {
            DefaultListModel cellDataListModel = (DefaultListModel)lidarDatasourceList.getModel();
            OlaDatasourceInfo cellDataInfo = (OlaDatasourceInfo)cellDataListModel.get(selectedItem);
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
        else
        {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }//GEN-LAST:event_lidarDatasourceListValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lidarDatasourceList;
    private javax.swing.JButton newButton;
    // End of variables declaration//GEN-END:variables
}
