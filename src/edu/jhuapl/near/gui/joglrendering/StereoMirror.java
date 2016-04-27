package edu.jhuapl.near.gui.joglrendering;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkDoubleArray;
import vtk.vtkGeometryFilter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkRectilinearGrid;
import vtk.vtkRenderer;

public class StereoMirror extends vtksbmtJoglCanvasComponent
{
    vtksbmtJoglCanvasComponent parent;
    vtkRenderer defaultRenderer;
//    vtkRenderer kludgeRenderer;
//    GridBackgroundRenderer backgroundRenderer;
    double eyeSeparation;
    vtkPolyData background;
    vtkPolyDataMapper backgroundMapper;
    vtkActor backgroundActor;
    double deepField;

    public StereoMirror(vtksbmtJoglCanvasComponent parent, double deepField)
    {
        this.parent=parent;
        parent.getRenderWindow().AddObserver("StartEvent", this, "Render");
        //
        defaultRenderer=getRenderer();
        eyeSeparation=getActiveCamera().GetEyeSeparation();
        //
 /*       backgroundRenderer=new GridBackgroundRenderer(parent.getRenderer(), 1e3, 20);
        kludgeRenderer=new vtkRenderer();
        getRenderWindow().SetNumberOfLayers(3);
        getRenderWindow().AddRenderer(backgroundRenderer);
        getRenderWindow().AddRenderer(kludgeRenderer);
        backgroundRenderer.SetLayer(0);
        defaultRenderer.SetLayer(1);
        kludgeRenderer.SetLayer(2);*/
        this.deepField=deepField;
        generateBackground(20, deepField);
    }

    void generateBackground(int ndiv, double scale)
    {
        vtkRectilinearGrid grid=new vtkRectilinearGrid();
        vtkDoubleArray div=new vtkDoubleArray();
        for (int i=0; i<ndiv; i++)
            div.InsertNextValue(-scale+(double)i*2.*scale/(double)(ndiv-1));
        grid.SetDimensions(ndiv, ndiv, ndiv);
        grid.SetXCoordinates(div);
        grid.SetYCoordinates(div);
        grid.SetZCoordinates(div);
        vtkGeometryFilter filter=new vtkGeometryFilter();
        filter.SetInputData(grid);
        filter.Update();
        background=new vtkPolyData();
        background.DeepCopy(filter.GetOutput());
        //
        backgroundMapper=new vtkPolyDataMapper();
        backgroundMapper.SetInputData(background);
        backgroundActor=new vtkActor();
        backgroundActor.SetMapper(backgroundMapper);
        backgroundActor.GetProperty().SetColor(1,1,1);
        backgroundActor.GetProperty().SetRepresentationToWireframe();
        backgroundActor.GetProperty().BackfaceCullingOff();
        backgroundActor.GetProperty().SetLineWidth(5);
        //
  //      getRenderer().AddActor(backgroundActor);
    }
/*    public vtkRenderer getKludgeRenderer()
    {
        return kludgeRenderer;
    }*/

    void syncCameraWithParent()
    {
        Vector3D pos=new Vector3D(parent.getActiveCamera().GetPosition());
        Vector3D fp=new Vector3D(parent.getActiveCamera().GetFocalPoint());
        double len=pos.subtract(fp).getNorm();
//        if (len>deepField)
//            Vector3D newlook=pos.subtract(fp).scalarMultiply(deepField/len);
        getActiveCamera().SetPosition(parent.getActiveCamera().GetPosition());
        getActiveCamera().SetFocalPoint(parent.getActiveCamera().GetFocalPoint());
        getActiveCamera().SetViewUp(parent.getActiveCamera().GetViewUp());
        getActiveCamera().SetClippingRange(parent.getActiveCamera().GetClippingRange());
        getActiveCamera().SetEyeSeparation(eyeSeparation);
    }

    public void decreaseEyeSeparation()
    {
        //System.out.println(getActiveCamera().GetEyeSeparation());
        eyeSeparation*=0.95;
        Render();
    }

    public void increaseEyeSeparation()
    {
        //System.out.println(getActiveCamera().GetEyeSeparation());
        eyeSeparation*=1.05;
        Render();
    }

    @Override
    public void Render()
    {
//        backgroundRenderer.syncCameraWithParent();
        syncCameraWithParent();
        super.Render();
    }

}

