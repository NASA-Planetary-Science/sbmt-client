package edu.jhuapl.near.model.eros;

import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkUnsignedCharArray;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.LineModel.Line;

/**
 * This class modifies its parent class by making the control
 * points (i.e. the points you can drag and move around) visible on all
 * the lines, not just the selected line. The idea is to be able to drag
 * any line without having to first select, similar to how the points
 * and circles models work.
 * 
 * @author eli
 *
 */
public class ProfileLineModel extends LineModel
{
	private SmallBodyModel smallBodyModel;
	
	public ProfileLineModel(SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel);
		
		this.smallBodyModel = smallBodyModel;
		setMaximumVerticesPerLine(2);
	}
	/*
    private void updateLineSelection()
    {
    	if (selectedLine == -1)
    	{
            if (actors.contains(lineSelectionActor))
            	actors.remove(lineSelectionActor);
            
            return;
    	}

        Line lin = lines.get(selectedLine);
        
        vtkPolyData selectionPolyData = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray vert = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();

		selectionPolyData.SetPoints( points );
		selectionPolyData.SetVerts( vert );
		selectionPolyData.GetCellData().SetScalars(colors);

        colors.SetNumberOfComponents(4);

		int numPoints = lin.controlPointIds.size();

        points.SetNumberOfPoints(numPoints);

		vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        int numLines = getNumberOfStructures();
        for (int j=0; j<numLines; ++j)
        {
        	for (int i=0; i<numPoints; ++i)
        	{
        		int idx = lin.controlPointIds.get(i);
        		points.SetPoint(i, lin.xyzPointList.get(idx).xyz);
        		idList.SetId(0, i);
        		vert.InsertNextCell(idList);
        		if (i == this.currentLineVertex)
        			colors.InsertNextTuple4(blueColor[0],blueColor[1],blueColor[2],blueColor[3]);
        		else
        			colors.InsertNextTuple4(redColor[0],redColor[1],redColor[2],redColor[3]);
        	}
        }

		smallBodyModel.shiftPolyLineInNormalDirection(selectionPolyData, 0.001);
		
		if (lineSelectionMapper == null)
			lineSelectionMapper = new vtkPolyDataMapper();
        lineSelectionMapper.SetInput(selectionPolyData);
        lineSelectionMapper.Update();
        
        if (!actors.contains(lineSelectionActor))
        	actors.add(lineSelectionActor);

        lineSelectionActor.SetMapper(lineSelectionMapper);
        lineSelectionActor.Modified();
    }
*/
}
