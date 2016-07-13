package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.nio.file.Paths;
import java.util.TreeSet;

import com.google.common.base.Stopwatch;

import vtk.vtkCubeSource;
import vtk.vtkPolyData;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.LidarHyperTreeSearchDataCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.PickManager.PickMode;
import edu.jhuapl.near.pick.Picker;
import edu.jhuapl.near.util.BoundingBox;

public class LidarHyperTreeSearchPanel extends LidarSearchPanel
{
    final LidarHyperTreeSearchDataCollection defaultDataSource;
    LidarHyperTreeSearchDataCollection coll;

    public LidarHyperTreeSearchPanel(SmallBodyConfig smallBodyConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer)
    {
        super(smallBodyConfig, modelManager, pickManager, renderer);
        defaultDataSource=(LidarHyperTreeSearchDataCollection) modelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.LIDAR_HYPERTREE_SEARCH;
    }

    @Override
    protected void submitButtonActionPerformed(ActionEvent evt)
    {
        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);

        /*int lidarIndex = smallBodyModel.getLidarDatasourceIndex();
        String lidarDatasourceName = smallBodyModel.getLidarDatasourceName(lidarIndex);
        String lidarDatasourcePath = smallBodyModel.getLidarDatasourcePath(lidarIndex);*/
        //System.out.println("Current Lidar Datasource Index : " + lidarIndex);
        //System.out.println("Current Lidar Datasource Name: " + lidarDatasourceName);
        //System.out.println("Current Lidar Datasource Path: " + lidarDatasourcePath);


        double[] selectionRegionCenter = null;
        double selectionRegionRadius = 0.0;

        AbstractEllipsePolygonModel.EllipsePolygon region=null;
        vtkPolyData interiorPoly=new vtkPolyData();
        if (selectionModel.getNumberOfStructures() > 0)
        {
            region=(AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);
            selectionRegionCenter = region.center;
            selectionRegionRadius = region.radius;


            // Always use the lowest resolution model for getting the intersection cubes list.
            // Therefore, if the selection region was created using a higher resolution model,
            // we need to recompute the selection region using the low res model.
            if (smallBodyModel.getModelResolution() > 0)
                smallBodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);    // this sets interiorPoly
            else
                interiorPoly=region.interiorPolyData;

        }
        else
        {
            vtkCubeSource box=new vtkCubeSource();
            double[] bboxBounds=smallBodyModel.getBoundingBox().getBounds();
            BoundingBox bbox=new BoundingBox(bboxBounds);
            bbox.increaseSize(0.01);
            box.SetBounds(bbox.getBounds());
            box.Update();
            interiorPoly.DeepCopy(box.GetOutput());
        }

        String selectedSourceName=(String)sourceComboBox.getModel().getElementAt(sourceComboBox.getSelectedIndex());
        System.out.println("Selected lidar source name: "+selectedSourceName);
        coll=defaultDataSource;
        String sourcePath="default";

        // look for custom data sources in small body model
        for (int i=0; i<smallBodyModel.getNumberOfLidarDatasources(); i++)
            if (smallBodyModel.getLidarDatasourceName(i).equals(selectedSourceName))
            {
                sourcePath=smallBodyModel.getLidarDatasourcePath(i);
                coll=new LidarHyperTreeSearchDataCollection(smallBodyModel, Paths.get(sourcePath));
                break;
            }

        System.out.println("Found matching lidar data path: "+sourcePath);
        setLidarModel();

        Stopwatch sw=new Stopwatch();
        sw.start();
        TreeSet<Integer> cubeList=coll.getLeavesIntersectingBoundingBox(new BoundingBox(interiorPoly.GetBounds()), getSelectedTimeLimits());
        System.out.println("Search Time="+sw.elapsedMillis()+" ms");
        sw.stop();

        Picker.setPickingEnabled(false);

        coll.setParentForProgressMonitor(this);
        showData(cubeList, selectionRegionCenter, selectionRegionRadius);
        radialOffsetChanger.reset();

        Picker.setPickingEnabled(true);

    }

    @Override
    protected void setLidarModel()
    {
        if (coll==null)
            super.setLidarModel();
        else
            lidarModel=coll;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        //super.propertyChange(evt);
        System.out.println("*");
    }

}
