/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LidarSearchPanel.java
 *
 * Created on Apr 5, 2011, 5:15:12 PM
 */

package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
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
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.renderer.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PointInCylinderChecker;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection.Track;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection.TrackFileType;
import edu.jhuapl.sbmt.util.TimeUtil;


public class LidarSearchPanel extends javax.swing.JPanel implements PropertyChangeListener, ItemListener
{
    protected final ModelManager modelManager;
    protected PickManager pickManager;
    protected LidarSearchDataCollection lidarModel;
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    protected LidarPopupMenu lidarPopupMenu;
    private LidarTrackTranslationDialog translateDialog;
    protected edu.jhuapl.saavtk.gui.RadialOffsetChanger radialOffsetChanger;
    protected BodyViewConfig smallBodyConfig;

    /** Creates new form LidarSearchPanel */
    public LidarSearchPanel(BodyViewConfig smallBodyConfig,
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

        this.smallBodyConfig=smallBodyConfig;
        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(getLidarModelName());

        pickManager.getDefaultPicker().addPropertyChangeListener(this);
        lidarModel.addPropertyChangeListener(this);

        initComponents();

        radialOffsetChanger = new edu.jhuapl.saavtk.gui.RadialOffsetChanger();
        radialOffsetPanel.add(radialOffsetChanger);

        // make the load track panel invisible for now -turnerj1
        loadTrackButton.setVisible(false);
        fileTypeLabel.setVisible(false);
        fileTypeComboBox.setVisible(false);

        Map<String, String> sourceMap = lidarModel.getLidarDataSourceMap();
        boolean hasLidarData = smallBodyConfig.hasLidarData;
        if (hasLidarData)
        {
            DefaultComboBoxModel sourceComboBoxModel = new DefaultComboBoxModel(sourceMap.keySet().toArray());
            sourceComboBox.setModel(sourceComboBoxModel);
            /*if (sourceMap.size() == 1)
            {
                sourceLabel.setVisible(false);
                sourceComboBox.setVisible(false);
            }*/

            startDate = smallBodyConfig.lidarSearchDefaultStartDate;
            ((SpinnerDateModel)startSpinner.getModel()).setValue(startDate);
            endDate = smallBodyConfig.lidarSearchDefaultEndDate;
            ((SpinnerDateModel)endSpinner.getModel()).setValue(endDate);
        }

        radialOffsetChanger.setModel(lidarModel);
        radialOffsetChanger.setOffsetScale(lidarModel.getOffsetScale());

        lidarPopupMenu = new LidarPopupMenu(lidarModel, renderer);

        updateLidarDatasourceComboBox();


        jTable1.setModel(new LidarTableModel());
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //jTable1.setComponentPopupMenu(lidarPopupMenu);
        jTable1.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                maybeShowPopup(e);
            }
        });
        jTable1.removeColumn(jTable1.getColumnModel().getColumn(6));


        JPanel showSpacecraftPanel = new JPanel(new GridLayout());
        showSpacecraftPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        spacecraftPositionCheckbox.setVisible(false);
        lidarModel.setShowSpacecraftPosition(false);

/*        spacecraftPositionCheckbox.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                lidarModel.setShowSpacecraftPosition(spacecraftPositionCheckbox.isSelected());

            }
        });*/

    }

    double[] getSelectedTimeLimits()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        double start = TimeUtil.str2et(sdf.format(startDate).replace(' ', 'T'));
        double end = TimeUtil.str2et(sdf.format(endDate).replace(' ', 'T'));
        return new double[]{start,end};
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
        manageDatasourcesButton.setVisible(false);
    }

    protected void showData(
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            double start = TimeUtil.str2et(sdf.format(startDate).replace(' ', 'T'));
            double end = TimeUtil.str2et(sdf.format(endDate).replace(' ', 'T'));
            PointInCylinderChecker checker;
            if (selectionRegionCenter==null || selectionRegionRadius<=0)// do time-only search
                checker=null;
            else
                checker=new PointInCylinderChecker(modelManager.getPolyhedralModel(), selectionRegionCenter, selectionRegionRadius);
            //
            lidarModel.setLidarData(
                    sourceComboBox.getSelectedItem().toString(),
                    start,
                    end,
                    cubeList,
                    checker,
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

  //      populateTracksInfoLabel();
        //populateTracksList();
  //      populateTracksErrorLabel();

    }

/*    private void populateTracksInfoLabel()
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
*/

    boolean populatingTracks=false;
    protected void populateTracksList()
    {
        int numberOfTracks = lidarModel.getNumberOfTracks();

        boolean[] hidden=new boolean[jTable1.getModel().getRowCount()];
        for (int i=0; i<hidden.length; i++)
            hidden[i]=(boolean)jTable1.getModel().getValueAt(i, 0);

        ((DefaultTableModel)jTable1.getModel()).setRowCount(0);
        jTable1.revalidate();
        for (int i=0; i<numberOfTracks; i++)
        {
            if (i<hidden.length)
                ((LidarTableModel)jTable1.getModel()).addTrack(lidarModel.getTrack(i),i,hidden[i]);
            else
                ((LidarTableModel)jTable1.getModel()).addTrack(lidarModel.getTrack(i),i);
        }

        refreshTrackList();
    }

    private void refreshTrackList()
    {
        // establish nice spacing between columns
        for (int c=0; c<jTable1.getColumnCount(); c++)
        {
            int w=30;
            int spacing=10;
            for (int r=0; r<jTable1.getRowCount(); r++)
            {
                //Component comp=jTable1.prepareRenderer(jTable1.getCellRenderer(r, c), r, c);
                Component comp=jTable1.prepareRenderer(jTable1.getCellRenderer(r, c), r, c);
                w=Math.max(w, comp.getPreferredSize().width+1);
            }
            jTable1.getColumnModel().getColumn(c).setPreferredWidth(w+spacing);
        }

    }

    private void populateTracksErrorLabel()
    {
        //if (trackErrorButton.isSelected())
        //{
        //"<html>Hello World!<br>blahblahblah</html>"
            String errorText = "<html>"+ (float)lidarModel.getTrackError() + " RMS (units of DATA) <br> for " + lidarModel.getNumberOfVisibleTracks() + " visible tracks / " + lidarModel.getLastNumberOfPointsForTrackError() + " points </html>";
            trackErrorLabel.setText(errorText);
        //}
    }


    @Override
    public void itemStateChanged(ItemEvent e)
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        JComboBox lidarDatasourceComboBox = (JComboBox)e.getSource();
        int index = lidarDatasourceComboBox.getSelectedIndex() - 1;
        smallBodyModel.setLidarDatasourceIndex(index);
    }

    @Override
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
                    jTable1.setRowSelectionInterval(idx, idx);
                    Rectangle cellBounds=jTable1.getCellRect(idx, 0, true);
                    if (cellBounds!=null)
                        jTable1.scrollRectToVisible(cellBounds);
//                    tracksList.setSelectionInterval(idx, idx);
//                    Rectangle cellBounds = tracksList.getCellBounds(idx, idx);
//                    if (cellBounds != null)
//                        tracksList.scrollRectToVisible(cellBounds);
                }
            }
        }
        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()) && !populatingTracks)
        {
            populatingTracks=true;
            populateTracksList();
            populatingTracks=false;
        }
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            lidarPopupMenu.setVisible(false);
            int index=jTable1.rowAtPoint(e.getPoint());
            if (index >= 0)
            {
                jTable1.setRowSelectionInterval(index, index);
                lidarPopupMenu.setCurrentTrack(index);
                lidarPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected void updateLidarDatasourceComboBox()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
//        if (smallBodyModel.getLidarDatasourceIndex() < 0)
//            smallBodyModel.setLidarDatasourceIndex(0);

        sourceComboBox.removeItemListener(this);
        sourceComboBox.removeAllItems();


        if (lidarModel.getLidarDataSourceMap().containsKey("Default"))
            sourceComboBox.addItem("Default");

        for (String source : lidarModel.getLidarDataSourceMap().keySet())
            sourceComboBox.addItem(source);

        for (int i=0; i<smallBodyModel.getNumberOfLidarDatasources(); ++i)
        {
            sourceComboBox.addItem(smallBodyModel.getLidarDatasourceName(i));
        }

        if (smallBodyModel.getNumberOfLidarDatasources() > 0)
        {
            int index = smallBodyModel.getLidarDatasourceIndex();
            sourceComboBox.setSelectedIndex(Math.max(index, 0));
        }

        sourceComboBox.addItemListener(this);
    }

    class LidarTableModel extends DefaultTableModel
    {
        boolean hideOrShowAllInProgress=false;

        public LidarTableModel()
        {
            this.addTableModelListener(new TableModelListener()
            {

                @Override
                public void tableChanged(TableModelEvent e)
                {
                    if (hideOrShowAllInProgress)
                        return;
                    //
                    int r=e.getFirstRow();
                    int c=e.getColumn();
                    TableModel model=(TableModel)e.getSource();
                    if (c==0)   // hardcoded wiring into hide column
                    {
                        Boolean data=(Boolean)model.getValueAt(r,c);
                        lidarModel.hideTrack(r, data);
                    }
                }
            });

        }

        public Color getColor(int row)
        {
            int[] rgb=(int[])getValueAt(row, 6);
            return new Color(rgb[0],rgb[1],rgb[2]);
        }

        public void hideAllTracks()
        {
            hideOrShowAllInProgress=true;
            for (int r=0; r<getRowCount(); r++)
                setValueAt(true, r, 0);    // set checkbox value to true (0 is hardcoded as the hide column)
            lidarModel.hideAllTracks();
            hideOrShowAllInProgress=false;
        }

        public void showAllTracks()
        {
            hideOrShowAllInProgress=true;
            for (int r=0; r<getRowCount(); r++)
                setValueAt(false, r, 0);    // set checkbox value to false (0 is hardcoded as the hide column)
            lidarModel.showAllTracks();
            hideOrShowAllInProgress=false;
        }

        public void removeAllTracks()
        {
            hideOrShowAllInProgress=true;
            int cnt=getRowCount();
            for (int r=0; r<cnt; r++)
                removeRow(0);
            hideOrShowAllInProgress=false;
        }

        @Override
        public int getColumnCount()
        {
            return 7;
        }

        public void addTrack(Track track, int id)
        {
            addTrack(track, id, false);
        }

        public void addTrack(Track track, int id, boolean hidden)
        {
            String sourceFiles="";
            for (int i=0; i<track.getNumberOfSourceFiles(); i++)
            {
                sourceFiles+=track.getSourceFileName(i);
                if (i<track.getNumberOfSourceFiles()-1)
                    sourceFiles+=" | ";
            }
            addRow(new Object[]{
                    hidden,
                    "Trk "+id,
                    track.getNumberOfPoints(),
                    track.timeRange[0],
                    track.timeRange[1],
                    sourceFiles,
                    track.color
                    });
        }

        @Override
        public String getColumnName(int column)
        {
            switch (column)
            {
            case 0:
                return "Hide";
            case 1:
                return "Track";
            case 2:
                return "# pts";
            case 3:
                return "Start Time";
            case 4:
                return "End Time";
            case 5:
                return "Data Source";
            default:
                return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
            case 0:
                return Boolean.class;
            case 6:
                return int[].class;
            default:
                return String.class;
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
        trackErrorButton = new javax.swing.JButton();
        trackErrorLabel = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        sourceComboBox = new javax.swing.JComboBox();
        fileTypeComboBox = new javax.swing.JComboBox();
        fileTypeLabel = new javax.swing.JLabel();
        radialOffsetPanel = new javax.swing.JPanel();
        manageDatasourcesButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable(){
            @Override
            public TableCellRenderer getCellRenderer(int row, int column)
            {
                Color c=((LidarTableModel)getModel()).getColor(row);
                TableCellRenderer renderer=super.getCellRenderer(row, column);
                //                    System.out.println(renderer.getClass().equals(DefaultTableCellRenderer.UIResource.class));
                //                    System.out.println(renderer.getClass()+" "+DefaultTableCellRenderer.UIResource.class);
                if (renderer.getClass().equals(DefaultTableCellRenderer.UIResource.class))
                    ((DefaultTableCellRenderer)renderer).setForeground(c);
                return renderer;
            }
        };
        spacecraftPositionCheckbox = new javax.swing.JCheckBox();

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

        trackErrorButton.setText("Show Track Error");
        trackErrorButton.setToolTipText("<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");
        trackErrorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackErrorButtonActionPerformed(evt);
            }
        });

        trackErrorLabel.setText(" ");

        sourceLabel.setText("Source:");

        sourceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));

        fileTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Text", "Binary", "OLA Level 2" }));
        fileTypeComboBox.setToolTipText("<html>\nTrack file can be in either text or binary format.<br><br>\nIf text, file may contain 3 or more space-delimited columns.<br>\nDepending on the number of columns, the file is interpreted the following way:<br>\n - 3 columns: X, Y, and Z target position. Time and spacecraft position set to zero.<br> \n - 4 columns: time, X, Y, and Z target position. Spacecraft position set to zero.<br>\n - 5 columns: time, X, Y, and Z target position. Spacecraft position set to zero. 5th column ignored.<br>\n - 6 columns: X, Y, Z target position, X, Y, Z spacecraft position. Time set to zero.<br>\n - 7 or more columns: time, X, Y, and Z target position, X, Y, Z spacecraft position. Additional columns ignored.<br>\nNote that time is expressed either as a UTC string such as 2000-04-06T13:19:12.153<br>\nor as a floating point ephemeris time such as 9565219.901.<br>\n<br>\nIf binary, each record must consist of 7 double precision values:<br>\n1. ET<br>\n2. X target<br>\n3. Y target<br>\n4. Z target<br>\n5. X spacecraft position<br>\n6. Y spacecraft position<br>\n7. Z spacecraft position<br>\n");

        fileTypeLabel.setText("File Type:");

        radialOffsetPanel.setLayout(new java.awt.BorderLayout());

        manageDatasourcesButton.setText("Manage Data Sources...");
        manageDatasourcesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageDatasourcesButtonActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable1);

        spacecraftPositionCheckbox.setText("Show Spacecraft Position");
        spacecraftPositionCheckbox.setToolTipText("<html>\nIf checked, the track error will be calculated and shown to the right of this checkbox.<br>\nWhenever a change is made to the tracks, the track error will be updated. This can be<br>\na slow operation which is why this checkbox is provided to disable it.<br>\n<br>\nThe track error is computed as the mean distance between each lidar point and its<br>\nclosest point on the asteroid.");
        spacecraftPositionCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spacecraftPositionCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(radialOffsetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(resultsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(selectRegionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(submitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(loadTrackButton)
                                .addGap(18, 18, 18)
                                .addComponent(fileTypeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fileTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(sourceLabel)
                                    .addComponent(startLabel)
                                    .addComponent(endLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(endSpinner)
                                    .addComponent(manageDatasourcesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(sourceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(startSpinner)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minTrackSizeLabel)
                                    .addComponent(trackSeparationLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(trackSeparationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                    .addComponent(minTrackSizeTextField)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(translateTracksButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dragTracksToggleButton))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pointSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(trackErrorButton))
                                .addGap(32, 32, 32)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spacecraftPositionCheckbox)
                                    .addComponent(trackErrorLabel))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manageDatasourcesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startLabel)
                    .addComponent(startSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endLabel)
                    .addComponent(endSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(radialOffsetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(translateTracksButton)
                    .addComponent(dragTracksToggleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trackErrorButton)
                    .addComponent(spacecraftPositionCheckbox))
                .addGap(10, 10, 10)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trackErrorLabel)
                    .addComponent(jLabel5)
                    .addComponent(pointSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40))
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

    protected void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);

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

    private void hideAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideAllButtonActionPerformed
        lidarModel.hideAllTracks();
        ((LidarTableModel)jTable1.getModel()).hideAllTracks();
    }//GEN-LAST:event_hideAllButtonActionPerformed

    private void showAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllButtonActionPerformed
        lidarModel.showAllTracks();
        ((LidarTableModel)jTable1.getModel()).showAllTracks();
    }//GEN-LAST:event_showAllButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllButtonActionPerformed
        lidarModel.removeAllLidarData();
        ((LidarTableModel)jTable1.getModel()).removeAllTracks();
//        tracksList.setListData(new String[]{});
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
                else if (fileTypeComboBox.getSelectedItem().equals("OLA Level 2"))
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


  /*          populateTracksInfoLabel();
            populateTracksList();
            populateTracksErrorLabel();*/
        }
    }//GEN-LAST:event_loadTrackButtonActionPerformed

    private void trackErrorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackErrorCheckBoxActionPerformed
        lidarModel.setEnableTrackErrorComputation(true);
        trackErrorLabel.setEnabled(true);
        populateTracksErrorLabel();
    }//GEN-LAST:event_trackErrorCheckBoxActionPerformed

    private void manageDatasourcesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageDatasourcesButtonActionPerformed
        CustomLidarDataDialog dialog = new CustomLidarDataDialog(modelManager);
        dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this));
        dialog.setVisible(true);

        // update the panel to reflect changes to the lidar datasources
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        if (smallBodyModel.getNumberOfLidarDatasources()>0)
            smallBodyModel.loadCustomLidarDatasourceInfo();
        updateLidarDatasourceComboBox();
    }//GEN-LAST:event_manageDatasourcesButtonActionPerformed

    private void spacecraftPositionCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spacecraftPositionCheckboxActionPerformed
        System.out.println("Show Spacecraft Position Toggled");
    }//GEN-LAST:event_spacecraftPositionCheckboxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearRegionButton;
    private javax.swing.JToggleButton dragTracksToggleButton;
    private javax.swing.JLabel endLabel;
    private javax.swing.JSpinner endSpinner;
    protected javax.swing.JComboBox fileTypeComboBox;
    private javax.swing.JLabel fileTypeLabel;
    private javax.swing.JButton hideAllButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton loadTrackButton;
    private javax.swing.JButton manageDatasourcesButton;
    private javax.swing.JLabel minTrackSizeLabel;
    private javax.swing.JFormattedTextField minTrackSizeTextField;
    private javax.swing.JSpinner pointSizeSpinner;
    private javax.swing.JPanel radialOffsetPanel;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JLabel resultsLabel;
    protected javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JPanel selectRegionPanel;
    private javax.swing.JButton showAllButton;
    protected javax.swing.JComboBox sourceComboBox;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JCheckBox spacecraftPositionCheckbox;
    private javax.swing.JLabel startLabel;
    private javax.swing.JSpinner startSpinner;
    private javax.swing.JButton submitButton;
    private javax.swing.JPanel submitPanel;
    private javax.swing.JButton trackErrorButton;
    private javax.swing.JLabel trackErrorLabel;
    private javax.swing.JLabel trackSeparationLabel;
    private javax.swing.JFormattedTextField trackSeparationTextField;
    private javax.swing.JButton translateTracksButton;
    // End of variables declaration//GEN-END:variables

}
