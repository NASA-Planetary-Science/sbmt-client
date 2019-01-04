package edu.jhuapl.sbmt.gui.lidar;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Date;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import vtk.vtkCubeSource;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.LidarDatasourceInfo;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.FileCache.NonexistentRemoteFile;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.lidar.v2.LidarSearchController;
import edu.jhuapl.sbmt.gui.lidar.v2.OlaLidarBrowsePanel;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.OlaLidarHyperTreeSearchDataCollection;

public class OlaLidarHyperTreeSearchPanel extends LidarSearchController //LidarSearchPanel  // currently implemented only for OLA lidar points, but could be revised to handle any points satisfying the LidarPoint interface.
{
    Renderer renderer;
    BiMap<Integer, String> sourceComboBoxEnumeration;
    OlaLidarBrowsePanel browsePanel;

    public OlaLidarHyperTreeSearchPanel(SmallBodyViewConfig smallBodyConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer, OlaLidarBrowsePanel browsePanel)
    {
        super(smallBodyConfig, modelManager, pickManager, renderer);
        this.renderer=renderer;
        this.browsePanel=browsePanel;
    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.LIDAR_HYPERTREE_SEARCH;
    }

    public void updateLidarDatasourceComboBox()
    {
        //super.updateLidarDatasourceComboBox();

        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        OlaLidarHyperTreeSearchDataCollection lidarHyperTreeSearchDataCollection = (OlaLidarHyperTreeSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(getLidarModelName());

        // clear the skeletons instances (should we try to keep these around to avoid having to load them again? -turnerj1)
        lidarHyperTreeSearchDataCollection.clearDatasourceSkeletons();
        view.getSourceComboBox().removeItemListener(this);
        view.getSourceComboBox().removeAllItems();
        view.getSourceComboBox().addItemListener(this);

        // add the server datasource
        String defaultDatasourceName = "Default";
        String defaultDatasourcePath = lidarModel.getLidarDataSourceMap().get("Default");

        lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(defaultDatasourceName, defaultDatasourcePath);
        view.getSourceComboBox().addItem(defaultDatasourceName);

        // add other pre-existing server datasources
        for (String preExistingDatasourceName : lidarModel.getLidarDataSourceMap().keySet())
            if (!preExistingDatasourceName.equals(defaultDatasourceName))
            {
                lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(preExistingDatasourceName, lidarModel.getLidarDataSourceMap().get(preExistingDatasourceName));
                view.getSourceComboBox().addItem(preExistingDatasourceName);
            }

        // add the custom local datasources
        for (LidarDatasourceInfo info : smallBodyModel.getLidarDasourceInfoList())
        {
            String datasourceName = info.name;
            String datasourcePath = info.path;
            lidarHyperTreeSearchDataCollection.addDatasourceSkeleton(datasourceName, datasourcePath);
        }

        sourceComboBoxEnumeration=HashBiMap.create();
        for (int i=0; i<view.getSourceComboBox().getItemCount(); i++)
            sourceComboBoxEnumeration.put(i, (String)view.getSourceComboBox().getItemAt(i));

        // set the current datasource
        //int index = smallBodyModel.getLidarDatasourceIndex();
        //String datasourceName = smallBodyModel.getLidarDatasourceName(index);
        //lidarHyperTreeSearchDataCollection.setCurrentDatasourceSkeleton(datasourceName);
        int index=sourceComboBoxEnumeration.inverse().get(defaultDatasourceName);
        String dataSourceName=defaultDatasourceName;
        lidarHyperTreeSearchDataCollection.setCurrentDatasourceSkeleton(defaultDatasourceName);

        if (browsePanel!=null)
            browsePanel.repopulate(defaultDatasourcePath, defaultDatasourceName);
    }

    @Override
    protected void submitButtonActionPerformed(ActionEvent evt)
    {
        lidarModel.removePropertyChangeListener(propertyChangeListener);

        view.getSelectRegionButton().setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);

        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);

        // get current lidar source
        int lidarIndex=view.getSourceComboBox().getSelectedIndex();
        String lidarDatasourceName=sourceComboBoxEnumeration.get(lidarIndex);


        // read in the skeleton, if it hasn't been read in already
        ((OlaLidarHyperTreeSearchDataCollection)lidarModel).setCurrentDatasourceSkeleton(lidarDatasourceName);
        try {
            ((OlaLidarHyperTreeSearchDataCollection)lidarModel).readSkeleton();
        } catch (NonexistentRemoteFile e) {
            JOptionPane.showMessageDialog(this.view,
                    "There is no existing tree for this survey",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

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

        //String selectedSourceName = null; // (String)sourceComboBox.getModel().getElementAt(sourceComboBox.getSelectedIndex());
        //System.out.println("Selected lidar source name: "+selectedSourceName);
//        if (lidarDatasourceName.equals("Default"))
//            lidarModel=(OLALidarHyperTreeSearchDataCollection)modelManager.getModel(getLidarModelName());
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

//        System.out.println("Found matching lidar data path: "+lidarDatasourcePath);
        lidarModel.addPropertyChangeListener(propertyChangeListener);
        view.getRadialOffsetSlider().setModel(lidarModel);
        view.getRadialOffsetSlider().setOffsetScale(lidarModel.getOffsetScale());
        lidarPopupMenu = new LidarPopupMenu(lidarModel, renderer);

        Stopwatch sw=new Stopwatch();
        sw.start();
        TreeSet<Integer> cubeList=((OlaLidarHyperTreeSearchDataCollection)lidarModel).getLeavesIntersectingBoundingBox(new BoundingBox(interiorPoly.GetBounds()), getSelectedTimeLimits());
//        System.out.println("Search Time="+sw.elapsedMillis()+" ms");
        sw.stop();

        Picker.setPickingEnabled(false);

        ((OlaLidarHyperTreeSearchDataCollection)lidarModel).setParentForProgressMonitor(view);
        showData(cubeList, selectionRegionCenter, selectionRegionRadius);
        view.getRadialOffsetSlider().reset();

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

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        int lidarIndex=view.getSourceComboBox().getSelectedIndex();
        System.out.println("Lidar Datasource Changed: " + lidarIndex);
        if (sourceComboBoxEnumeration!=null)
        {
            String dataSourceName=sourceComboBoxEnumeration.get(lidarIndex);
//            browsePanel.repopulate(model.getSmallBodyConfig().lidarBrowseDataSourceMap.get(dataSourceName), dataSourceName);

            /*
             *  change the min/max times for the search based on the datasource
             */
            // TODO get start and end times for the datasource from config?
            Date start = model.getSmallBodyConfig().lidarSearchDataSourceTimeMap.get(dataSourceName).get(0);
            Date end = model.getSmallBodyConfig().lidarSearchDataSourceTimeMap.get(dataSourceName).get(1);
            ((SpinnerDateModel)view.getStartDateSpinner().getModel()).setValue(start);
            ((SpinnerDateModel)view.getStartDateSpinner().getModel()).setStart(start);
            ((SpinnerDateModel)view.getStartDateSpinner().getModel()).setEnd(start);
            ((SpinnerDateModel)view.getEndDateSpinner().getModel()).setValue(end);
            ((SpinnerDateModel)view.getEndDateSpinner().getModel()).setStart(start);
            ((SpinnerDateModel)view.getEndDateSpinner().getModel()).setEnd(start);
        }
    }

}
