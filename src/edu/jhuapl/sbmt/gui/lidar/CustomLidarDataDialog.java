/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomImageLoaderPanel.java
 *
 * Created on Jun 5, 2012, 3:56:56 PM
 */
package edu.jhuapl.sbmt.gui.lidar;

import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.model.LidarDatasourceInfo;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.app.SmallBodyModel;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;


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
        List<LidarDatasourceInfo> list = modelManager.getPolyhedralModel().getLidarDasourceInfoList();
        for (LidarDatasourceInfo info : list)
            ((DefaultListModel)lidarDatasourceList.getModel()).addElement(info);
    }

    private String getCustomDataFolder()
    {
        return modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    private String getConfigFilename()
    {
        return modelManager.getPolyhedralModel().getConfigFilename();
    }

    private void updateConfigFile()
    {
        MapUtil configMap = new MapUtil(getConfigFilename());

        // Load in the plate data
        String lidarDatasourcePath = "";
        String lidarDatasourceName = "";

        DefaultListModel cellDataListModel = (DefaultListModel)lidarDatasourceList.getModel();
        for (int i=0; i<cellDataListModel.size(); ++i)
        {
            LidarDatasourceInfo lidarDatasourceInfo = (LidarDatasourceInfo)cellDataListModel.get(i);

            lidarDatasourcePath += lidarDatasourceInfo.path;
            lidarDatasourceName += lidarDatasourceInfo.name;

            if (i < cellDataListModel.size()-1)
            {
                lidarDatasourcePath += CustomShapeModel.LIST_SEPARATOR;
                lidarDatasourceName += CustomShapeModel.LIST_SEPARATOR;
            }
        }

        Map<String, String> newMap = new LinkedHashMap<String, String>();

        newMap.put(SmallBodyModel.LIDAR_DATASOURCE_PATHS, lidarDatasourcePath);
        newMap.put(SmallBodyModel.LIDAR_DATASOURCE_NAMES, lidarDatasourceName);

        configMap.put(newMap);
    }

    private void saveLidarDatasourceData(int index, LidarDatasourceInfo oldLidarDatasourceInfo, LidarDatasourceInfo newLidarDatasourceInfo)
    {
        String uuid = UUID.randomUUID().toString();

        // If File is the same as the old File,
        // that means we are in edit mode and and the user did not change to a new file.
        if (oldLidarDatasourceInfo == null || !newLidarDatasourceInfo.path.equals(oldLidarDatasourceInfo.path))
        {
        }

        DefaultListModel model = (DefaultListModel)lidarDatasourceList.getModel();
        if (index >= model.getSize())
        {
            model.addElement(newLidarDatasourceInfo);
        }
        else
        {
            model.set(index, newLidarDatasourceInfo);
        }

        updateConfigFile();
    }

    private void removeLidarDatasource(int index)
    {
        try
        {
//            LidarDatasourceInfo lidarDatasourceInfo = (LidarDatasourceInfo)((DefaultListModel)lidarDatasourceList.getModel()).get(index);
//            String filename = getCustomDataFolder() + File.separator + lidarDatasourceInfo.path;
//            new File(filename).delete();
            modelManager.getPolyhedralModel().removeCustomLidarDatasource(index);
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
            LidarDatasourceInfo lidarDatasourceInfo = new LidarDatasourceInfo();
            CustomLidarDataImporterDialog dialog = new CustomLidarDataImporterDialog(JOptionPane.getFrameForComponent(this), false);
            dialog.setLidarDatasourceInfo(lidarDatasourceInfo, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // If user clicks okay add to list
            if (dialog.getOkayPressed())
            {
                lidarDatasourceInfo = dialog.getLidarDatasourceInfo();
                saveLidarDatasourceData(((DefaultListModel)lidarDatasourceList.getModel()).getSize(), null, lidarDatasourceInfo);
                modelManager.getPolyhedralModel().addCustomLidarDatasource(lidarDatasourceInfo);
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
            removeLidarDatasource(selectedItem);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        try
        {
            int selectedItem = lidarDatasourceList.getSelectedIndex();
            if (selectedItem >= 0)
            {
                DefaultListModel cellDataListModel = (DefaultListModel)lidarDatasourceList.getModel();
                LidarDatasourceInfo oldLidarDatasourceInfo = (LidarDatasourceInfo)cellDataListModel.get(selectedItem);

                CustomLidarDataImporterDialog dialog = new CustomLidarDataImporterDialog(JOptionPane.getFrameForComponent(this), true);
                dialog.setLidarDatasourceInfo(oldLidarDatasourceInfo, modelManager.getPolyhedralModel().getSmallBodyPolyData().GetNumberOfCells());
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);

                // If user clicks okay replace item in list
                if (dialog.getOkayPressed())
                {
                    LidarDatasourceInfo cellDataInfo = dialog.getLidarDatasourceInfo();
                    saveLidarDatasourceData(selectedItem, oldLidarDatasourceInfo, cellDataInfo);
                    modelManager.getPolyhedralModel().setCustomLidarDatasource(selectedItem, cellDataInfo);
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
            LidarDatasourceInfo cellDataInfo = (LidarDatasourceInfo)cellDataListModel.get(selectedItem);
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
