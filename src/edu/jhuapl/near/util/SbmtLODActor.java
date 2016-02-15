package edu.jhuapl.near.util;

import vtk.vtkActor;
import vtk.vtkDataObject;
import vtk.vtkDataSet;
import vtk.vtkMapper;
import vtk.vtkPolyDataMapper;
import vtk.vtkQuadricClustering;

/**
 * VTK actor which stores a normal (high-res) mapper and a LOD mapper
 * (low-res).  User can switch between the two dynamically.
 *
 * For Paraview-like LOD switching, add observers to the render window
 * interactor for StartInteractionEvent and EndInteractionEvent that
 * searches for SbmtLODActors in the scene and calls SetEnableLOD()
 *
 * @author twupy1
 *
 */

public class SbmtLODActor extends vtkActor{

    protected vtkMapper detailedMapper;
    protected vtkMapper lodMapper;
    protected boolean enableLOD;

    /**
     * Constructor
     */
    public SbmtLODActor(){
        super();

        // Set unused mappers to null
        detailedMapper = null;
        lodMapper = null;

        // Don't use LOD mapper by default, user should toggle on/off
        enableLOD = false;
    }

    /**
     * Whether to toggle on/off the LOD mapper
     * @param enableLOD
     */
    public void SetEnableLOD(boolean enableLOD){
        this.enableLOD = enableLOD;

        // Set the mapper accordingly
        if(enableLOD && lodMapper != null){
            // Set the LOD mapper if user asked for it and it has been specified
            super.SetMapper(lodMapper);
        }else{
            super.SetMapper(detailedMapper);
        }
        super.Modified();
    }

    /**
     * Gets the memory size in kilobytes
     * @param lodIfAvailable
     * @return
     */
    public int GetActualMemorySize(boolean lodIfAvailable){
        // Reference to input data sets from mappers
        vtkDataSet dataSet;

        // Try to get the LOD size
        if(lodIfAvailable && lodMapper != null)
        {
            lodMapper.Update();
            if((dataSet = lodMapper.GetInputAsDataSet()) != null)
            {
                // LOD mapper is set, get the LOD object data's memory size
                return dataSet.GetActualMemorySize();
            }
        }

        // Try to get the high-res size
        if(detailedMapper != null){
            detailedMapper.Update();
            if((dataSet = detailedMapper.GetInputAsDataSet()) != null)
            {
                // Detailed mapper is set, get the detailed object data's memory size
                return dataSet.GetActualMemorySize();
            }
        }

        // We got here because something went wrong, return 0
        return 0;
    }

    /**
     * Sets the LOD mapper
     * @param lodMapper
     */
    public void SetLODMapper(vtkMapper lodMapper){
        this.lodMapper = lodMapper;
    }

    /**
     * Assuming mapper is already set, takes its input data object and sets the LOD mapper as a
     * decimated version using quadric clustering where the number of divisions is auto selected
     * @return
     */
    public boolean SetQuadricDecimatedLODMapper(){
        vtkDataObject dataObject;
        if(detailedMapper != null && (dataObject = detailedMapper.GetInputDataObject(0,0)) != null){
            // Detailed mapper's input has been set
            vtkQuadricClustering decimator = new vtkQuadricClustering();

            // Decimate the input data
            decimator.SetInputDataObject(dataObject);
            decimator.CopyCellDataOn();
            decimator.AutoAdjustNumberOfDivisionsOn();
            decimator.Update();

            // Link to mapper and save
            lodMapper = new vtkPolyDataMapper();
            lodMapper.SetInputConnection(decimator.GetOutputPort());

            // Let user know we were successful
            return true;
        }else{
            // If we reached here then something went wrong, let user know
            return false;
        }
    }

    /**
     * Assuming mapper is already set, takes its input data object and sets the LOD mapper as a
     * decimated version using quadric clustering where the number of divisions in each x,y,z
     * dimensions is specified as input
     * @param numDivisions
     * @return
     */
    public boolean SetQuadricDecimatedLODMapper(int numDivisions){
        vtkDataObject dataObject;
        if(detailedMapper != null && numDivisions > 1 &&
                (dataObject = detailedMapper.GetInputDataObject(0,0)) != null){
            // Detailed mapper's input has been set
            vtkQuadricClustering decimator = new vtkQuadricClustering();

            // Decimate the input data
            decimator.SetInputDataObject(dataObject);
            decimator.CopyCellDataOn();
            decimator.SetNumberOfDivisions(numDivisions, numDivisions, numDivisions);
            decimator.Update();

            // Link to mapper and save
            lodMapper = new vtkPolyDataMapper();
            lodMapper.SetInputConnection(decimator.GetOutputPort());

            // Let user know we were successful
            return true;
        }else{
            // If we reached here then something went wrong, let user know
            return false;
        }
    }

    /**
     * Overwrites parent method, sets detailed mapper and saves a reference
     */
    public void SetMapper(vtkMapper mapper){
        // Set the mapper as usual
        super.SetMapper(mapper);

        // Maintain a link to parent class's mapper
        this.detailedMapper = super.GetMapper();
    }
}
