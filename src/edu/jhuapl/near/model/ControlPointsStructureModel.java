package edu.jhuapl.near.model;

import java.io.File;

import vtk.vtkActor;


/**
 * A type of structure which uses a set of control points to describe it. This
 * currently includes only paths (LineModel) and polygons (PolygonModel).
 *
 * @author kahneg1
 *
 */
public abstract class ControlPointsStructureModel extends StructureModel
{
    public ControlPointsStructureModel(String name)
    {
        super(name);
    }

    abstract public vtkActor getSelectionActor();

    abstract public void selectCurrentStructureVertex(int idx);

    abstract public void insertVertexIntoSelectedStructure(double[] newPoint);

    abstract public void updateSelectedStructureVertex(int vertexId, double[] newPoint);

    abstract public void moveSelectionVertex(int vertexId, double[] newPoint);

    abstract public void removeCurrentStructureVertex();

    //optional for subclasses
    public int getStructureIdFromSelectionCellId(int idx)
    {
        return -1;
    }

    //optional for subclasses
    public int getVertexIdFromSelectionCellId(int idx)
    {
        return -1;
    }

    public boolean hasProfileMode()
    {
        return false;
    }

    /**
     * Subclasses should only redefine this if they support profiles (i.e. they redefine
     * hasProfileMode to return true).
     *
     * @param cellId
     * @param file
     * @param useElevationData
     * @throws Exception
     */
    public void saveProfile(int cellId, File file, boolean useElevationData) throws Exception
    {
        // do nothing
    }
}
