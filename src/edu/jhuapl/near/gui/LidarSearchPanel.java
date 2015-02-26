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

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;

import vtk.vtkPolyData;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.LidarSearchDataCollection.TrackFileType;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointInCylinderChecker;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickEvent;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.pick.Picker;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.TimeUtil;


public class LidarSearchPanel extends javax.swing.JPanel implements PropertyChangeListener
{
    private final ModelManager modelManager;
    private PickManager pickManager;
    private LidarSearchDataCollection lidarModel;
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    private LidarPopupMenu lidarPopupMenu;
    private LidarTrackTranslationDialog translateDialog;

    /** Creates new form LidarSearchPanel */
    public LidarSearchPanel(SmallBodyConfig smallBodyConfig,
            final ModelManager modelManager,
            final PickManager pickManager,
            Renderer renderer)
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


        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(getLidarModelName());

        pickManager.getDefaultPicker().addPropertyChangeListener(this);
        lidarModel.addPropertyChangeListener(this);

        initComponents();

        loadTrackButton.setVisible(false);
        fileTypeLabel.setVisible(false);
        fileTypeComboBox.setVisible(false);

        Map<String, String> sourceMap = lidarModel.getLidarDataSourceMap();
        if (smallBodyConfig.hasLidarData)
        {
            DefaultComboBoxModel sourceComboBoxModel = new DefaultComboBoxModel(sourceMap.keySet().toArray());
            sourceComboBox.setModel(sourceComboBoxModel);
            if (sourceMap.size() == 1)
            {
                sourceLabel.setVisible(false);
                sourceComboBox.setVisible(false);
            }

            startDate = smallBodyConfig.lidarSearchDefaultStartDate;
            ((SpinnerDateModel)startSpinner.getModel()).setValue(startDate);
            endDate = smallBodyConfig.lidarSearchDefaultEndDate;
            ((SpinnerDateModel)endSpinner.getModel()).setValue(endDate);
        }

        radialOffsetChanger.setModel(lidarModel);
        radialOffsetChanger.setOffsetScale(lidarModel.getOffsetScale());

        lidarPopupMenu = new LidarPopupMenu(lidarModel, renderer);
    }

    protected ModelNames getLidarModelName()
    {
        return ModelNames.LIDAR_SEARCH;
    }

    protected void hideSearchControls()
    {
        sourceLabel.setVisible(false);
        sourceComboBox.setVisible(false);
        startLabel.setVisible(false);
        startSpinner.setVisible(false);
        endLabel.setVisible(false);
        endSpinner.setVisible(false);
        minTrackSizeLabel.setVisible(false);
        minTrackSizeTextField.setVisible(false);
        trackSeparationLabel.setVisible(false);
        trackSeparationTextField.setVisible(false);
        selectRegionButton.setVisible(false);
        clearRegionButton.setVisible(false);
        selectRegionPanel.setVisible(false);
        submitButton.setVisible(false);
        submitPanel.setVisible(false);
        loadTrackButton.setVisible(true);
        fileTypeLabel.setVisible(true);
        fileTypeComboBox.setVisible(true);
    }

    private void showData(
            TreeSet<Integer> cubeList,
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
            System.out.println(sourceComboBox.getSelectedItem().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            double start = TimeUtil.str2et(sdf.format(startDate).replace(' ', 'T'));
            double end = TimeUtil.str2et(sdf.format(endDate).replace(' ', 'T'));
            lidarModel.setLidarData(
                    sourceComboBox.getSelectedItem().toString(),
                    start,
                    end,
                    cubeList,
                    new PointInCylinderChecker(modelManager.getSmallBodyModel(), selectionRegionCenter, selectionRegionRadius),
                    timeSeparationBetweenTracks,
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

        populateTracksInfoLabel();
        populateTracksList();
        populateTracksErrorLabel();
    }

    private void populateTracksInfoLabel()
    {
        String resultsText =
            "<html>Number of points " + lidarModel.getNumberOfPoints() + "<br><br>";

        if (lidarModel.getNumberOfPoints() > 0)
        {
            resultsText += "Number of tracks: " + lidarModel.getNumberOfTrack();
        }

        resultsText += "</html>";

        resultsLabel.setText(resultsText);
    }

    private void populateTracksList()
    {
        int numberOfTracks = lidarModel.getNumberOfTrack();
        String[] results = new String[numberOfTracks];

        for (int i=0; i<numberOfTracks; ++i)
        {
            results[i] = "Track " + i + ", Number of points: " + lidarModel.getNumberOfPointsPerTrack(i)
            + ", " + lidarModel.getTrackTimeRange(i);
        }

        tracksList.setListData(results);
    }

    private void populateTracksErrorLabel()
    {
        if (trackErrorCheckBox.isSelected())
        {
            String errorText = "" + (float)lidarModel.getTrackError();
            trackErrorLabel.setText(errorText);
        }
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

                int idx = lidarModel.getTrackIdFromPointId(id);
                if (idx >= 0)
                {
                    tracksList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = tracksList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        tracksList.scrollRectToVisible(cellBounds);
                }
            }
        }
        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            populateTracksErrorLabel();
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

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        startLabel = new javax.swing.JLabel();
        endLabel = new javax.swing.JLabel();
        startSpinner = new javax.swing.JSpinner();
        endSpinner = new javax.swing.JSpinner();
        resultsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tracksList = new javax.swing.JList();
        radialOffsetChanger = new edu.jhuapl.near.gui.RadialOffsetChanger();
        jPanel1 = new javax.swing.JPanel();
        hideAllButton = new javax.swing.JButton();
        showAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        selectRegionPanel = new javax.swing.JPanel();
        selectRegionButton = new javax.swing.JToggleButton();
        clearRegionButton = new javax.swing.JButton();
        submitPanel = new javax.swing.JPanel();
        submitButton = new javax.swing.JButton();
        minTrackSizeLabel = new javax.swing.JLabel();
        trackSeparationLabel = new javax.swing.JLabel();
        minTrackSizeTextField = new javax.swing.JFormattedTextField();
        trackSeparationTextField = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        pointSizeSpinner = new javax.swing.JSpinner();
        translateTracksButton = new javax.swing.JButton();
        dragTracksToggleButton = new javax.swing.JToggleButton();
        loadTrackButton = new javax.swing.JButton();
        trackErrorCheckBox = new javax.swing.JCheckBox();
        trackErrorLabel = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        sourceComboBox = new javax.swing.JComboBox();
        fileTypeComboBox = new javax.swing.JComboBox();
        fileTypeLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel4.setPreferredSize(new java.awt.Dimension(300, 794));

        startLabel.setText("Start Date:");

        endLabel.setText("End Date:");

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

        showAllButton.setText("Show All");
        showAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllButtonActionPerformed(evt);
            }
        });
        jPanel1.add(showAllButton);

        removeAllButton.setText("Remove All");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        jPanel1.add(removeAllButton);

        selectRegionButton.setText("Select Region");
        selectRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });
        selectRegionPanel.add(selectRegionButton);

        clearRegionButton.setText("Clear Region");
        clearRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });
        selectRegionPanel.add(clearRegionButton);

        submitButton.setText("Search");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        submitPanel.add(submitButton);

        minTrackSizeLabel.setText("Minumum Track Size:");

        trackSeparationLabel.setText("Track Separation (sec):");

        minTrackSizeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        minTrackSizeTextField.setText("10");

        trackSeparationTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));
        trackSeparationTextField.setText("10");

        jLabel5.setText("Point Size");

        pointSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, 100, 1));
        pointSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pointSizeSpinnerStateChanged(evt);
            }
        });

        translateTracksButton.setText("Translate Tracks...");
        translateTracksButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateTracksButtonActionPerformed(evt);
            }
        });

        dragTracksToggleButton.setText("Drag Tracks");
        dragTracksToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dragTracksToggleButtonActionPerformed(evt);
            }
        });

        loadTrackButton.setText("Load Tracks...");
        loadTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTrackButtonActionPerformed(evt);
            }
        });

        trackErrorCheckBox.setText("Show Track Error:");
        trackErrorCheckBox.setToolTipText("<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");
        trackErrorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackErrorCheckBoxActionPerformed(evt);
            }
        });

        trackErrorLabel.setText(" ");

        sourceLabel.setText("Source:");

        sourceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));

        fileTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Text", "Binary", "OLA Level 2" }));
        fileTypeComboBox.setToolTipText("<html>\nTrack file can be in either text or binary format.<br><br>\nIf text, file may contain 3 or more space-delimited columns.<br>\nDepending on the number of columns, the file is interpreted the following way:<br>\n - 3 columns: X, Y, and Z target position. Time and spacecraft position set to zero.<br> \n - 4 columns: time, X, Y, and Z target position. Spacecraft position set to zero.<br>\n - 5 columns: time, X, Y, and Z target position. Spacecraft position set to zero. 5th column ignored.<br>\n - 6 columns: X, Y, Z target position, X, Y, Z spacecraft position. Time set to zero.<br>\n - 7 or more columns: time, X, Y, and Z target position, X, Y, Z spacecraft position. Additional columns ignored.<br>\nNote that time is expressed as a UTC string such as 2000-04-06T13:19:12.153.<br>\n<br>\nIf binary, each record must consist of 7 double precision values:<br>\n1. ET<br>\n2. X target<br>\n3. Y target<br>\n4. Z target<br>\n5. X spacecraft position<br>\n6. Y spacecraft position<br>\n7. Z spacecraft position<br>\n");

        fileTypeLabel.setText("File Type:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(radialOffsetChanger, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addComponent(resultsLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(selectRegionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(submitPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(trackErrorCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(trackErrorLabel))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(pointSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(loadTrackButton)
                                .addGap(18, 18, 18)
                                .addComponent(fileTypeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fileTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dragTracksToggleButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(translateTracksButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(sourceLabel)
                                    .addComponent(startLabel)
                                    .addComponent(endLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(sourceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(startSpinner)
                                    .addComponent(endSpinner)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minTrackSizeLabel)
                                    .addComponent(trackSeparationLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(minTrackSizeTextField)
                                    .addComponent(trackSeparationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {endSpinner, sourceComboBox, startSpinner});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startLabel)
                    .addComponent(startSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endLabel)
                    .addComponent(endSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minTrackSizeLabel)
                    .addComponent(minTrackSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trackSeparationLabel)
                    .addComponent(trackSeparationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectRegionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadTrackButton)
                    .addComponent(fileTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radialOffsetChanger, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(translateTracksButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dragTracksToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trackErrorCheckBox)
                    .addComponent(trackErrorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(pointSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {endSpinner, sourceComboBox, startSpinner});

        jScrollPane2.setViewportView(jPanel4);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
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
        TreeSet<Integer> cubeList = null;
        double[] selectionRegionCenter = null;
        double selectionRegionRadius = 0.0;
        if (selectionModel.getNumberOfStructures() > 0)
        {
            AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);
            selectionRegionCenter = region.center;
            selectionRegionRadius = region.radius;

            // Always use the lowest resolution model for getting the intersection cubes list.
            // Therefore, if the selection region was created using a higher resolution model,
            // we need to recompute the selection region using the low res model.
            if (smallBodyModel.getModelResolution() > 0)
            {
                vtkPolyData interiorPoly = new vtkPolyData();
                smallBodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
                cubeList = smallBodyModel.getIntersectingCubes(new BoundingBox(interiorPoly.GetBounds()));
            }
            else
            {
                cubeList = smallBodyModel.getIntersectingCubes(new BoundingBox(region.interiorPolyData.GetBounds()));
            }
        }
        else
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "Please select a region on the asteroid.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        Picker.setPickingEnabled(false);

        showData(cubeList, selectionRegionCenter, selectionRegionRadius);
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

    private void showAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllButtonActionPerformed
        lidarModel.showAllTracks();
    }//GEN-LAST:event_showAllButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllButtonActionPerformed
        lidarModel.removeAllLidarData();
        tracksList.setListData(new String[]{});
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void pointSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pointSizeSpinnerStateChanged
        Number val = (Number)pointSizeSpinner.getValue();
        lidarModel.setPointSize(val.intValue());
    }//GEN-LAST:event_pointSizeSpinnerStateChanged

    private void translateTracksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translateTracksButtonActionPerformed
        if (translateDialog == null)
            translateDialog = new LidarTrackTranslationDialog(JOptionPane.getFrameForComponent(this), true, lidarModel);

        translateDialog.setLocationRelativeTo(this);
        translateDialog.setVisible(true);
    }//GEN-LAST:event_translateTracksButtonActionPerformed

    private void dragTracksToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragTracksToggleButtonActionPerformed
        if (dragTracksToggleButton.isSelected())
        {
            pickManager.setPickMode(PickManager.PickMode.LIDAR_SHIFT);
        }
        else
        {
            pickManager.setPickMode(PickManager.PickMode.DEFAULT);
        }
    }//GEN-LAST:event_dragTracksToggleButtonActionPerformed

    private void loadTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadTrackButtonActionPerformed
        File[] files = CustomFileChooser.showOpenDialog(this, "Select Files", null, true);

        if (files != null)
        {
            try
            {
                TrackFileType trackFileType = null;
                if (fileTypeComboBox.getSelectedItem().equals("Text"))
                    trackFileType = TrackFileType.TEXT;
                else if (fileTypeComboBox.getSelectedItem().equals("Binary"))
                    trackFileType = TrackFileType.BINARY;
                else if (fileTypeComboBox.getSelectedItem().equals("Binary"))
                    trackFileType = TrackFileType.OLA_LEVEL_2;
                lidarModel.loadTracksFromFiles(files, trackFileType);

                radialOffsetChanger.reset();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error reading the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }

            populateTracksInfoLabel();
            populateTracksList();
            populateTracksErrorLabel();
        }
    }//GEN-LAST:event_loadTrackButtonActionPerformed

    private void trackErrorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackErrorCheckBoxActionPerformed
        lidarModel.setEnableTrackErrorComputation(trackErrorCheckBox.isSelected());
        trackErrorLabel.setEnabled(trackErrorCheckBox.isSelected());
        populateTracksErrorLabel();
    }//GEN-LAST:event_trackErrorCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearRegionButton;
    private javax.swing.JToggleButton dragTracksToggleButton;
    private javax.swing.JLabel endLabel;
    private javax.swing.JSpinner endSpinner;
    private javax.swing.JComboBox fileTypeComboBox;
    private javax.swing.JLabel fileTypeLabel;
    private javax.swing.JButton hideAllButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton loadTrackButton;
    private javax.swing.JLabel minTrackSizeLabel;
    private javax.swing.JFormattedTextField minTrackSizeTextField;
    private javax.swing.JSpinner pointSizeSpinner;
    private edu.jhuapl.near.gui.RadialOffsetChanger radialOffsetChanger;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JPanel selectRegionPanel;
    private javax.swing.JButton showAllButton;
    private javax.swing.JComboBox sourceComboBox;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JLabel startLabel;
    private javax.swing.JSpinner startSpinner;
    private javax.swing.JButton submitButton;
    private javax.swing.JPanel submitPanel;
    private javax.swing.JCheckBox trackErrorCheckBox;
    private javax.swing.JLabel trackErrorLabel;
    private javax.swing.JLabel trackSeparationLabel;
    private javax.swing.JFormattedTextField trackSeparationTextField;
    private javax.swing.JList tracksList;
    private javax.swing.JButton translateTracksButton;
    // End of variables declaration//GEN-END:variables

}
