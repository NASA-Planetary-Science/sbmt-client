/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LidarSearchPanel.java
 *
 * Created on Apr 5, 2011, 5:15:12 PM
 */

package edu.jhuapl.near.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkPolyData;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.pick.Picker;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.Properties;

/**
 *
 * @author kahneg1
 */
abstract public class LidarSearchPanel extends javax.swing.JPanel implements PropertyChangeListener
{
    private final ModelManager modelManager;
    private PickManager pickManager;
    private LidarSearchDataCollection lidarModel;
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    private LidarPopupMenu lidarPopupMenu;

    /** Creates new form LidarSearchPanel */
    public LidarSearchPanel(final ModelManager modelManager,
            final PickManager pickManager)
    {
        this.modelManager = modelManager;
        this.pickManager = pickManager;

        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                selectRegionButton.setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
            }
        });

        pickManager.getDefaultPicker().addPropertyChangeListener(this);


        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(getLidarModelName());

        initComponents();

        radialOffsetChanger.setModel(lidarModel);
        radialOffsetChanger.setOffsetScale(lidarModel.getOffsetScale());

        lidarPopupMenu = new LidarPopupMenu(lidarModel);

        startDate = getDefaultStartDate();
        ((SpinnerDateModel)startSpinner.getModel()).setValue(startDate);
        endDate = getDefaultEndDate();
        ((SpinnerDateModel)endSpinner.getModel()).setValue(endDate);
    }

    abstract protected java.util.Date getDefaultStartDate();
    abstract protected java.util.Date getDefaultEndDate();
    abstract protected String getLidarModelName();

    private void showData(
            int direction,
            boolean reset,
            BoundingBox bb,
            double[] selectionRegionCenter,
            double selectionRegionRadius)
    {
        int minTrackLength = Integer.parseInt(minTrackSizeTextField.getText());
        if (minTrackLength < 1)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "Minimum track length must be a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        double timeSeparationBetweenTracks = Double.parseDouble(trackSeparationTextField.getText());
        if (timeSeparationBetweenTracks < 0.0)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "Track separation must be nonnegative.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
            lidarModel.setLidarData(new DateTime(startDate, DateTimeZone.UTC),
                    new DateTime(endDate, DateTimeZone.UTC),
                    bb,
                    selectionRegionCenter,
                    selectionRegionRadius,
                    direction* -1.0,
                    reset,
                    Math.round(1000.0*timeSeparationBetweenTracks), // convert to milliseconds
                    minTrackLength);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        String resultsText =
            "<html>" + lidarModel.getNumberOfPoints() + " points matched<br><br>";

        if (lidarModel.getNumberOfPoints() > 0)
        {
            resultsText += "Number of tracks: " + lidarModel.getNumberOfTrack();
        }

        resultsText += "</html>";

        resultsLabel.setText(resultsText);

        populateTracksList();
    }

    private void populateTracksList()
    {
        int numberOfTracks = lidarModel.getNumberOfTrack();
        String[] results = new String[numberOfTracks];

        for (int i=0; i<numberOfTracks; ++i)
        {
            LidarSearchDataCollection.Track track = lidarModel.getTrack(i);
            results[i] = "Track " + i + ", Number of points: " + track.getNumberOfPoints()
            + ", " + lidarModel.getTrackTimeRange(i);
        }

        tracksList.setListData(results);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            if (modelManager.getModel(e.getPickedProp()) == lidarModel &&
                    lidarModel.isDataPointsProp(e.getPickedProp()))
            {
                int id = e.getPickedCellId();
                lidarModel.selectPoint(id);
            }
        }
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = tracksList.locationToIndex(e.getPoint());

            if (index >= 0 && tracksList.getCellBounds(index, index).contains(e.getPoint()))
            {
                tracksList.setSelectedIndex(index);
                lidarPopupMenu.setCurrentTrack(index);
                lidarPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        startSpinner = new javax.swing.JSpinner();
        endSpinner = new javax.swing.JSpinner();
        resultsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tracksList = new javax.swing.JList();
        radialOffsetChanger = new edu.jhuapl.near.gui.RadialOffsetChanger();
        jPanel1 = new javax.swing.JPanel();
        hideAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        selectRegionButton = new javax.swing.JToggleButton();
        clearRegionButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        submitButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        minTrackSizeTextField = new javax.swing.JFormattedTextField();
        trackSeparationTextField = new javax.swing.JFormattedTextField();

        jLabel1.setText("Start Date:");

        jLabel2.setText("End Date:");

        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(951714000000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });

        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(982040400000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });

        resultsLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tracksList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tracksListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tracksListMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tracksList);

        hideAllButton.setText("Hide All");
        hideAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideAllButtonActionPerformed(evt);
            }
        });
        jPanel1.add(hideAllButton);

        removeAllButton.setText("Remove All");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        jPanel1.add(removeAllButton);

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        jPanel1.add(resetButton);

        selectRegionButton.setText("Select Region");
        selectRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });
        jPanel2.add(selectRegionButton);

        clearRegionButton.setText("Clear Region");
        clearRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });
        jPanel2.add(clearRegionButton);

        submitButton.setText("Search");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        jPanel3.add(submitButton);

        jLabel3.setText("Minumum Track Size:");

        jLabel4.setText("Track Separation (sec):");

        minTrackSizeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        minTrackSizeTextField.setText("1");

        trackSeparationTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
        trackSeparationTextField.setText("10");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addComponent(resultsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addComponent(radialOffsetChanger, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(endSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(minTrackSizeTextField)
                            .addComponent(trackSeparationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(startSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(endSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(minTrackSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(trackSeparationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radialOffsetChanger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_startSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)startSpinner.getModel()).getDate();
        if (date != null)
            startDate = date;
    }//GEN-LAST:event_startSpinnerStateChanged

    private void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_endSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)endSpinner.getModel()).getDate();
        if (date != null)
            endDate = date;
    }//GEN-LAST:event_endSpinnerStateChanged

    private void clearRegionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearRegionButtonActionPerformed
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }//GEN-LAST:event_clearRegionButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        BoundingBox bb = null;
        double[] selectionRegionCenter = null;
        double selectionRegionRadius = 0.0;
        if (selectionModel.getNumberOfStructures() > 0)
        {
            AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);

            vtkPolyData interiorPoly = new vtkPolyData();
            smallBodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
            bb = new BoundingBox(interiorPoly.GetBounds());
            selectionRegionCenter = region.center;
            selectionRegionRadius = region.radius;
        }
        else
        {
            int option = JOptionPane.showConfirmDialog(
                    JOptionPane.getFrameForComponent(this),
                    "You have not selected a region on the small body in order to limit the search.\n" +
                    "This may cause the program to crash if too much data is returned.\n" +
                    "Are you sure you want to continue?",
                    "No search region selected",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.NO_OPTION)
                return;

            bb = smallBodyModel.getBoundingBox();
        }

        Picker.setPickingEnabled(false);

        // Download file so progress bar shows up if necessary
        FileDownloadSwingWorker.downloadFile(
                this,
                "Downloading Lidar Database",
                lidarModel.getDatabasePath(),
                false);

        showData(1, true, bb, selectionRegionCenter, selectionRegionRadius);
        radialOffsetChanger.reset();
        Picker.setPickingEnabled(true);
    }//GEN-LAST:event_submitButtonActionPerformed

    private void selectRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectRegionButtonActionPerformed
    {//GEN-HEADEREND:event_selectRegionButtonActionPerformed
        if (selectRegionButton.isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_selectRegionButtonActionPerformed

    private void tracksListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tracksListMousePressed
        maybeShowPopup(evt);
    }//GEN-LAST:event_tracksListMousePressed

    private void tracksListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tracksListMouseReleased
        maybeShowPopup(evt);
    }//GEN-LAST:event_tracksListMouseReleased

    private void hideAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideAllButtonActionPerformed
        lidarModel.hideAllTracks();
    }//GEN-LAST:event_hideAllButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        lidarModel.resetTracks();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllButtonActionPerformed
        lidarModel.removeAllLidarData();
        tracksList.setListData(new String[]{});
    }//GEN-LAST:event_removeAllButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearRegionButton;
    private javax.swing.JSpinner endSpinner;
    private javax.swing.JButton hideAllButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JFormattedTextField minTrackSizeTextField;
    private edu.jhuapl.near.gui.RadialOffsetChanger radialOffsetChanger;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JSpinner startSpinner;
    private javax.swing.JButton submitButton;
    private javax.swing.JFormattedTextField trackSeparationTextField;
    private javax.swing.JList tracksList;
    // End of variables declaration//GEN-END:variables

}
