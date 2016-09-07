package edu.jhuapl.near.gui.lidar;

import java.awt.event.ActionEvent;
import java.util.TreeSet;

import com.google.common.base.Stopwatch;

import vtk.vtkCubeSource;
import vtk.vtkPolyData;

import edu.jhuapl.near.app.SmallBodyModel;
import edu.jhuapl.near.app.SmallBodyViewConfig;
import edu.jhuapl.near.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.near.model.lidar.OLALidarHyperTreeSearchDataCollection;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.LidarDatasourceInfo;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.BoundingBox;

public class OLALidarHyperTreeSearchPanel extends LidarSearchPanel  // currently implemented only for OLA lidar points, but could be revised to handle any points satisfying the LidarPoint interface.
{
    Renderer renderer;

    public OLALidarHyperTreeSearchPanel(SmallBodyViewConfig smallBodyConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer)
    {
        super(smallBodyConfig, modelManager, pickManager, renderer);
        this.renderer=renderer;
    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.LIDAR_HYPERTREE_SEARCH;
    }

    public void updateLidarDatasourceComboBox()
    {
        super.updateLidarDatasourceComboBox();

        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        OLALidarHyperTreeSearchDataCollection lidarHyperTreeSearchDataCollection = (OLALidarHyperTreeSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(getLidarModelName());

        // clear the skeletons instances (should we try to keep these around to avoid having to load them again? -turnerj1)
        lidarHyperTreeSearchDataCollection.clearDatasourceSkeletons();

        // add the server datasource
        String defaultDatasourceName = "Default";
        String defaultDatasourcePath = lidarModel.getLidarDataSourceMap().get("Default");
        lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(defaultDatasourceName, defaultDatasourcePath);

        // add the custom local datasources
        for (LidarDatasourceInfo info : smallBodyModel.getLidarDasourceInfoList())
        {
            String datasourceName = info.name;
            String datasourcePath = info.path;
            lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(datasourceName, datasourcePath);
        }

        // set the current datasource
        int index = smallBodyModel.getLidarDatasourceIndex();
        String datasourceName = smallBodyModel.getLidarDatasourceName(index);
        lidarHyperTreeSearchDataCollection.setCurrentDatasourceSkeleton(datasourceName);
    }

    @Override
    protected void submitButtonActionPerformed(ActionEvent evt)
    {
        lidarModel.removePropertyChangeListener(this);

        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);

        int lidarIndex = smallBodyModel.getLidarDatasourceIndex();
        String lidarDatasourceName = smallBodyModel.getLidarDatasourceName(lidarIndex);
        String lidarDatasourcePath = smallBodyModel.getLidarDatasourcePath(lidarIndex);
        System.out.println("Current Lidar Datasource Index : " + lidarIndex);
        System.out.println("Current Lidar Datasource Name: " + lidarDatasourceName);
        System.out.println("Current Lidar Datasource Path: " + lidarDatasourcePath);

        // read in the skeleton, if it hasn't been read in already
        ((OLALidarHyperTreeSearchDataCollection)lidarModel).readSkeleton();

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

        String selectedSourceName = null; // (String)sourceComboBox.getModel().getElementAt(sourceComboBox.getSelectedIndex());
        System.out.println("Selected lidar source name: "+selectedSourceName);
//        if (lidarDatasourceName.equals("Default"))
            lidarModel=(OLALidarHyperTreeSearchDataCollection)modelManager.getModel(getLidarModelName());
//        else
//            lidarModel=new LidarHyperTreeSearchDataCollection(smallBodyModel, Paths.get(lidarDatasourcePath));
        // lidarModel is by default equal to the source given in the super's constructor

        // look for custom data sources in small body model
/*        for (int i=0; i<smallBodyModel.getNumberOfLidarDatasources(); i++)
            if (smallBodyModel.getLidarDatasourceName(i).equals(selectedSourceName))
            {
                sourcePath=smallBodyModel.getLidarDatasourcePath(i);
                lidarModel=new LidarHyperTreeSearchDataCollection(smallBodyModel, Paths.get(sourcePath));
                break;
            }*/

        System.out.println("Found matching lidar data path: "+lidarDatasourcePath);
        lidarModel.addPropertyChangeListener(this);
        radialOffsetChanger.setModel(lidarModel);
        radialOffsetChanger.setOffsetScale(lidarModel.getOffsetScale());
        lidarPopupMenu = new LidarPopupMenu(lidarModel, renderer);

        Stopwatch sw=new Stopwatch();
        sw.start();
        TreeSet<Integer> cubeList=((OLALidarHyperTreeSearchDataCollection)lidarModel).getLeavesIntersectingBoundingBox(new BoundingBox(interiorPoly.GetBounds()), getSelectedTimeLimits());
        System.out.println("Search Time="+sw.elapsedMillis()+" ms");
        sw.stop();

        Picker.setPickingEnabled(false);

        ((OLALidarHyperTreeSearchDataCollection)lidarModel).setParentForProgressMonitor(this);
        showData(cubeList, selectionRegionCenter, selectionRegionRadius);
        radialOffsetChanger.reset();

/*        vtkPoints points=new vtkPoints();
        vtkCellArray cellArray=new vtkCellArray();
        vtkIntArray trackIds=new vtkIntArray();
        vtkIntArray pointTypes=new vtkIntArray();
        for (int i=0; i<lidarModel.getNumberOfTracks(); i++)
        {
            Track track=lidarModel.getTrack(i);
            for (int j=0; j<track.getNumberOfPoints(); j++)
            {
                LidarPoint p=track.getPoint(j);
                //
                int id=points.InsertNextPoint(p.getTargetPosition().toArray());
                vtkVertex vert=new vtkVertex();
                vert.GetPointIds().SetId(0, id);
                cellArray.InsertNextCell(vert);
                trackIds.InsertNextValue(i);
                pointTypes.InsertNextValue(0);  // for target positions
                //
                id=points.InsertNextPoint(p.getSourcePosition().toArray());
                vtkVertex vert2=new vtkVertex();
                vert2.GetPointIds().SetId(0, id);
                cellArray.InsertNextCell(vert2);
                trackIds.InsertNextValue(i);
                pointTypes.InsertNextValue(1);  // for target positions
            }
        }
        vtkPolyData polyData=new vtkPolyData();
        polyData.SetPoints(points);
        polyData.SetVerts(cellArray);
        polyData.GetCellData().AddArray(trackIds);
        //
        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
        writer.SetFileTypeToBinary();
        writer.SetInputData(polyData);
        writer.Write();*/


        Picker.setPickingEnabled(true);

    }


}
