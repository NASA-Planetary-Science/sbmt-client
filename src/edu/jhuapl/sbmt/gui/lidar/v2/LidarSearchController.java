package edu.jhuapl.sbmt.gui.lidar.v2;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PointInCylinderChecker;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.gui.lidar.CustomLidarDataDialog;
import edu.jhuapl.sbmt.gui.lidar.LidarSearchPanel;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.util.TimeUtil;


public class LidarSearchController implements ItemListener
{
    protected LidarSearchModel model;
    protected LidarSearchPanel view;
    protected LidarSearchDataCollection lidarModel;

    //model components
    protected ModelManager modelManager;
    protected PickManager pickManager;

    public LidarSearchController(BodyViewConfig smallBodyConfig, final ModelManager aModelManager,
        final PickManager aPickManager, Renderer aRenderer)
    {
        lidarModel = (LidarSearchDataCollection)aModelManager.getModel(getLidarModelName());
        model = new LidarSearchModel(smallBodyConfig, lidarModel);
        modelManager = aModelManager;
        pickManager = aPickManager;

        view = new LidarSearchPanel(aModelManager, model, aPickManager, aRenderer);

        updateLidarDatasourceComboBox();

        setupConnections();
    }

    public LidarSearchPanel getView()
    {
        return view;
    }

    protected void submitButtonActionPerformed(ActionEvent evt)
    {
        view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        pickManager.setActivePicker(null);
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel) modelManager
                .getModel(ModelNames.CIRCLE_SELECTION);
        TreeSet<Integer> cubeList = null;
        double[] selectionRegionCenter = null;
        double selectionRegionRadius = 0.0;
        if (selectionModel.getNumberOfStructures() > 0)
        {
            EllipsePolygon region = (EllipsePolygon) selectionModel
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
        }

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

        view.getSearchButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                submitButtonActionPerformed(e);
            }
        });

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

}
