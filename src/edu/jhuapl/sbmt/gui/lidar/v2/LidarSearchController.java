package edu.jhuapl.sbmt.gui.lidar.v2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.RadialOffsetChanger;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PointInCylinderChecker;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.gui.lidar.CustomLidarDataDialog;
import edu.jhuapl.sbmt.gui.lidar.LidarPopupMenu;
import edu.jhuapl.sbmt.gui.lidar.LidarTrackTranslationDialog;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection.TrackFileType;
import edu.jhuapl.sbmt.util.TimeUtil;


public class LidarSearchController implements ItemListener
{
    protected LidarSearchModel model;
    protected LidarSearchView view;
    protected LidarSearchDataCollection lidarModel;
    boolean populatingTracks=false;

    //view components
    JComboBox fileTypeComboBox = null;
    RadialOffsetChanger radialOffsetChanger = null;
    JTable table = null;
    JToggleButton dragTracksButton = null;
    LidarTrackTranslationDialog translateDialog = null;
    protected LidarPopupMenu lidarPopupMenu;
    JComboBox sourceComboBox = null;

    protected PropertyChangeListener propertyChangeListener = null;

    //model components
    protected ModelManager modelManager = null;
    protected PickManager pickManager = null;

    public LidarSearchController(BodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            final PickManager pickManager,
            Renderer renderer)
    {
        this.model = new LidarSearchModel(smallBodyConfig, modelManager, pickManager, renderer, getLidarModelName());
        this.lidarModel = model.getLidarModel();
        this.modelManager = model.getModelManager();
        this.pickManager = model.getPickManager();


        this.view = new LidarSearchView();
        fileTypeComboBox = view.getFileTypeComboBox();
        fileTypeComboBox.setModel(new DefaultComboBoxModel<String>(TrackFileType.names()));
//        fileTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Text", "Binary", "OLA Level 2" }));

        sourceComboBox = view.getSourceComboBox();
        sourceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));


        radialOffsetChanger = view.getRadialOffsetSlider();
        dragTracksButton = view.getDragTracksButton();
        translateDialog = view.getTranslateDialog();

        view.setLidarPopupMenu(new LidarPopupMenu(lidarModel, renderer));
        lidarPopupMenu = view.getLidarPopupMenu();


        radialOffsetChanger.setModel(lidarModel);
        radialOffsetChanger.setOffsetScale(lidarModel.getOffsetScale());


        updateLidarDatasourceComboBox();

        view.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                view.getSelectRegionButton().setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
            }
        });

        propertyChangeListener = new PropertyChangeListener()
        {

            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
                {
                    PickEvent e = (PickEvent)evt.getNewValue();
                    if (modelManager.getModel(e.getPickedProp()) == model.getLidarModel() &&
                            model.getLidarModel().isDataPointsProp(e.getPickedProp()))
                    {
                        int id = e.getPickedCellId();
                        model.getLidarModel().selectPoint(id);

                        int idx = model.getLidarModel().getTrackIdFromPointId(id);


                        if (idx >= 0)
                        {
                            view.getTable().setRowSelectionInterval(idx, idx);
                            Rectangle cellBounds=view.getTable().getCellRect(idx, 0, true);
                            if (cellBounds!=null)
                                view.getTable().scrollRectToVisible(cellBounds);
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
        };
        model.getPickManager().getDefaultPicker().addPropertyChangeListener(propertyChangeListener);
        lidarModel.addPropertyChangeListener(propertyChangeListener);


        setupConnections();
    }

    public LidarSearchView getView()
    {
        return view;
    }

    protected void submitButtonActionPerformed(ActionEvent evt)
    {
        view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        view.getSelectRegionButton().setSelected(false);
        model.getPickManager().setPickMode(PickMode.DEFAULT);
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) modelManager
                .getModel(ModelNames.CIRCLE_SELECTION);
        TreeSet<Integer> cubeList = null;
        double[] selectionRegionCenter = null;
        double selectionRegionRadius = 0.0;
        if (selectionModel.getNumberOfStructures() > 0)
        {
            AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon) selectionModel
                    .getStructure(0);
            selectionRegionCenter = region.center;
            selectionRegionRadius = region.radius;

            // Always use the lowest resolution model for getting the
            // intersection cubes list.
            // Therefore, if the selection region was created using a
            // higher resolution model,
            // we need to recompute the selection region using the low
            // res model.
            if (smallBodyModel.getModelResolution() > 0)
            {
                vtkPolyData interiorPoly = new vtkPolyData();
                smallBodyModel.drawRegularPolygonLowRes(region.center,
                        region.radius, region.numberOfSides, interiorPoly,
                        null);
                cubeList = smallBodyModel.getIntersectingCubes(
                        new BoundingBox(interiorPoly.GetBounds()));
            }
            else
            {
                cubeList = smallBodyModel.getIntersectingCubes(
                        new BoundingBox(region.interiorPolyData.GetBounds()));
            }
        }
        else
        {
            JOptionPane.showMessageDialog(
                    JOptionPane.getFrameForComponent(view),
                    "Please select a region on the asteroid.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            view.setCursor(Cursor.getDefaultCursor());

            return;
        }
        PickUtil.setPickingEnabled(false);
        showData(cubeList, selectionRegionCenter, selectionRegionRadius);
        radialOffsetChanger.reset();
        PickUtil.setPickingEnabled(true);
        view.setCursor(Cursor.getDefaultCursor());
    }

    protected void setupConnections()
    {

        Map<String, String> sourceMap = lidarModel.getLidarDataSourceMap();
        boolean hasLidarData = model.getSmallBodyConfig().hasLidarData;
        if (hasLidarData)
        {
            DefaultComboBoxModel sourceComboBoxModel = new DefaultComboBoxModel(sourceMap.keySet().toArray());
            view.getSourceComboBox().setModel(sourceComboBoxModel);

            model.setStartDate(model.getSmallBodyConfig().lidarSearchDefaultStartDate);
            ((SpinnerDateModel)view.getStartDateSpinner().getModel()).setValue(model.getStartDate());
            model.setEndDate(model.getSmallBodyConfig().lidarSearchDefaultEndDate);
            ((SpinnerDateModel)view.getEndDateSpinner().getModel()).setValue(model.getEndDate());
        }


        view.setTable(new javax.swing.JTable()
        {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column)
            {
                Color c = ((LidarTableModel) getModel()).getColor(row);
                TableCellRenderer renderer = super.getCellRenderer(row, column);
                if (renderer.getClass()
                        .equals(DefaultTableCellRenderer.UIResource.class))
                    ((DefaultTableCellRenderer) renderer).setForeground(c);
                return renderer;
            }
        });
        table = view.getTable();

        table.setModel(new LidarTableModel(model.getLidarModel()));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addMouseListener(new MouseAdapter()
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
        table.removeColumn(table.getColumnModel().getColumn(6));

        // BUTTON ACTIONS
        view.getLoadTracksButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                File[] files = CustomFileChooser.showOpenDialog(view,
                        "Select Files", null, true);

                if (files != null)
                {
                    try
                    {

                        TrackFileType trackFileType = null;
                        trackFileType = trackFileType.find(fileTypeComboBox.getSelectedItem().toString());


//                        if (fileTypeComboBox.getSelectedItem().equals("Text"))
//                            trackFileType = TrackFileType.TEXT;
//                        else if (fileTypeComboBox.getSelectedItem()
//                                .equals("Binary"))
//                            trackFileType = TrackFileType.BINARY;
//                        else if (fileTypeComboBox.getSelectedItem()
//                                .equals("OLA Level 2"))
//                            trackFileType = TrackFileType.OLA_LEVEL_2;
                        lidarModel.loadTracksFromFiles(files, trackFileType);

                        radialOffsetChanger.reset();
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(
                                JOptionPane.getFrameForComponent(view),
                                "There was an error reading the file.", "Error",
                                JOptionPane.ERROR_MESSAGE);

                        ex.printStackTrace();
                    }

                }
            }
        });

        view.getHideAllButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                lidarModel.hideAllTracks();
                ((LidarTableModel) table.getModel()).hideAllTracks();
            }
        });

        view.getShowAllButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                lidarModel.showAllTracks();
                ((LidarTableModel) table.getModel()).showAllTracks();
            }
        });

        view.getRemoveAllButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                lidarModel.removeAllLidarData();
                ((LidarTableModel) table.getModel()).removeAllTracks();
            }
        });

        view.getTranslateAllButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (translateDialog == null)
                    translateDialog = new LidarTrackTranslationDialog(
                            JOptionPane.getFrameForComponent(view), true,
                            lidarModel);

                translateDialog.setLocationRelativeTo(view);
                translateDialog.setVisible(true);
            }
        });

        view.getDragTracksButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (dragTracksButton.isSelected())
                {
                    model.getPickManager().setPickMode(PickManager.PickMode.LIDAR_SHIFT);
                }
                else
                {
                    model.getPickManager().setPickMode(PickManager.PickMode.DEFAULT);
                    model.getLidarModel().deselectSelectedPoint();
                }
            }
        });

        view.getManageSourcesButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                CustomLidarDataDialog dialog = new CustomLidarDataDialog(
                        modelManager);
                dialog.setLocationRelativeTo(
                        JOptionPane.getFrameForComponent(view));
                dialog.setVisible(true);

                // update the panel to reflect changes to the lidar datasources
                PolyhedralModel smallBodyModel = modelManager
                        .getPolyhedralModel();
                if (smallBodyModel.getNumberOfLidarDatasources() > 0)
                    smallBodyModel.loadCustomLidarDatasourceInfo();
                updateLidarDatasourceComboBox();

            }
        });

        view.getSelectRegionButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (view.getSelectRegionButton().isSelected())
                    model.getPickManager().setPickMode(PickMode.CIRCLE_SELECTION);
                else
                    model.getPickManager().setPickMode(PickMode.DEFAULT);
            }
        });

        view.getClearRegionButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) model.getModelManager()
                        .getModel(ModelNames.CIRCLE_SELECTION);
                selectionModel.removeAllStructures();
            }
        });

        view.getSearchButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                submitButtonActionPerformed(e);
            }
        });

        // SPINNERS
        view.getStartDateSpinner().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                java.util.Date date = ((SpinnerDateModel) view.getStartDateSpinner()
                        .getModel()).getDate();
                if (date != null)
                    model.setStartDate(date);
            }
        });

        view.getEndDateSpinner().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                java.util.Date date = ((SpinnerDateModel) view.getEndDateSpinner().getModel())
                        .getDate();
                if (date != null)
                    model.setEndDate(date);
            }
        });

        view.getPointSizeSpinner().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                Number val = (Number) view.getPointSizeSpinner().getValue();
                lidarModel.setPointSize(val.intValue());
            }
        });

        // CHECKBOX
        view.getShowErrorCheckBox().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                lidarModel.setEnableTrackErrorComputation(true);
                view.getErrorLabel().setEnabled(view.getShowErrorCheckBox().isSelected());
                view.getErrorLabel().setVisible(view.getShowErrorCheckBox().isSelected());
                populateTracksErrorLabel();
            }
        });

    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            lidarPopupMenu.setVisible(false);
            int index = table.rowAtPoint(e.getPoint());
            if (index >= 0)
            {
                table.setRowSelectionInterval(index, index);
                lidarPopupMenu.setCurrentTrack(index);
                lidarPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected void updateLidarDatasourceComboBox()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        // if (smallBodyModel.getLidarDatasourceIndex() < 0)
        // smallBodyModel.setLidarDatasourceIndex(0);

        view.getSourceComboBox().removeItemListener(this);
         view.getSourceComboBox().removeAllItems();

        if (lidarModel.getLidarDataSourceMap().containsKey("Default"))
             view.getSourceComboBox().addItem("Default");

        for (String source : lidarModel.getLidarDataSourceMap().keySet())
             view.getSourceComboBox().addItem(source);

        for (int i = 0; i < smallBodyModel.getNumberOfLidarDatasources(); ++i)
        {
             view.getSourceComboBox().addItem(smallBodyModel.getLidarDatasourceName(i));
        }

        if (smallBodyModel.getNumberOfLidarDatasources() > 0)
        {
            int index = smallBodyModel.getLidarDatasourceIndex();
             view.getSourceComboBox().setSelectedIndex(Math.max(index, 0));
        }

         view.getSourceComboBox().addItemListener(this);
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        JComboBox lidarDatasourceComboBox = (JComboBox)e.getSource();
        int index = lidarDatasourceComboBox.getSelectedIndex() - 1;
        smallBodyModel.setLidarDatasourceIndex(index);
    }

    private void populateTracksErrorLabel()
    {
        String errorText = "<html>"+ (float)lidarModel.getTrackError() + " RMS (units of DATA) <br> for " + lidarModel.getNumberOfVisibleTracks() + " visible tracks / " + lidarModel.getLastNumberOfPointsForTrackError() + " points </html>";
        view.getErrorLabel().setText(errorText);

    }

    protected void populateTracksList()
    {
        int numberOfTracks = lidarModel.getNumberOfTracks();

        boolean[] hidden=new boolean[view.getTable().getModel().getRowCount()];
        for (int i=0; i<hidden.length; i++)
            hidden[i]=(boolean)view.getTable().getModel().getValueAt(i, 0);

        ((DefaultTableModel)view.getTable().getModel()).setRowCount(0);
        view.getTable().revalidate();
        for (int i=0; i<numberOfTracks; i++)
        {
            if (i<hidden.length)
                ((LidarTableModel)view.getTable().getModel()).addTrack(lidarModel.getTrack(i),i,hidden[i]);
            else
                ((LidarTableModel)view.getTable().getModel()).addTrack(lidarModel.getTrack(i),i);
        }

        refreshTrackList();
    }

    private void refreshTrackList()
    {
        // establish nice spacing between columns
        for (int c=0; c<view.getTable().getColumnCount(); c++)
        {
            int w=30;
            int spacing=10;
            for (int r=0; r<view.getTable().getRowCount(); r++)
            {
                //Component comp=jTable1.prepareRenderer(jTable1.getCellRenderer(r, c), r, c);
                Component comp=view.getTable().prepareRenderer(view.getTable().getCellRenderer(r, c), r, c);
                w=Math.max(w, comp.getPreferredSize().width+1);
                if (c == 0)
                {
                    view.getTable().getModel().setValueAt(lidarModel.getTrack(r).hidden, r, 0);
                }
            }
            view.getTable().getColumnModel().getColumn(c).setPreferredWidth(w+spacing);
        }

    }

    protected void showData(
            TreeSet<Integer> cubeList,
            double[] selectionRegionCenter,
            double selectionRegionRadius)
    {
        int minTrackLength = Integer.parseInt(view.getMinTrackSizeTextField().getText());
        if (minTrackLength < 1)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view),
                    "Minimum track length must be a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        double timeSeparationBetweenTracks = Double.parseDouble(view.getTrackSeparationTextField().getText());
        if (timeSeparationBetweenTracks < 0.0)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(view),
                    "Track separation must be nonnegative.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            double start = TimeUtil.str2et(sdf.format(model.getStartDate()).replace(' ', 'T'));
            double end = TimeUtil.str2et(sdf.format(model.getEndDate()).replace(' ', 'T'));
            PointInCylinderChecker checker;
            if (selectionRegionCenter==null || selectionRegionRadius<=0)// do time-only search
                checker=null;
            else
                checker=new PointInCylinderChecker(modelManager.getPolyhedralModel(), selectionRegionCenter, selectionRegionRadius);
            //
            lidarModel.setLidarData(
                    view.getSourceComboBox().getSelectedItem().toString(),
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
    }

    protected double[] getSelectedTimeLimits()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        double start = TimeUtil.str2et(sdf.format(model.getStartDate()).replace(' ', 'T'));
        double end = TimeUtil.str2et(sdf.format(model.getEndDate()).replace(' ', 'T'));
        return new double[]{start,end};
    }

    protected ModelNames getLidarModelName()
    {
        return ModelNames.LIDAR_SEARCH;
    }

    protected void hideSearchControls()
    {
        view.getSearchPanel().setVisible(false);
        view.getTrackInfoPanel().setVisible(true);
//        sourceLabel.setVisible(false);
//        sourceComboBox.setVisible(false);
//        startLabel.setVisible(false);
//        startSpinner.setVisible(false);
//        endLabel.setVisible(false);
//        endSpinner.setVisible(false);
//        minTrackSizeLabel.setVisible(false);
//        minTrackSizeTextField.setVisible(false);
//        trackSeparationLabel.setVisible(false);
//        trackSeparationTextField.setVisible(false);
//        selectRegionButton.setVisible(false);
//        clearRegionButton.setVisible(false);
//        selectRegionPanel.setVisible(false);
//        submitButton.setVisible(false);
//        submitPanel.setVisible(false);
//        loadTrackButton.setVisible(true);
//        fileTypeLabel.setVisible(true);
//        fileTypeComboBox.setVisible(true);
//        manageDatasourcesButton.setVisible(false);
    }
}
