package edu.jhuapl.near.model.eros;

import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkUnsignedCharArray;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.SmallBodyModel;

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
	private vtkIdList idList;
	private int[] greenColor = {0, 255, 0, 255}; // RGBA green
	private int[] redColor = {255, 0, 0, 255}; // RGBA red

	public ProfileLineModel(SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel);
		
		this.smallBodyModel = smallBodyModel;
		setMaximumVerticesPerLine(2);
		
		idList = new vtkIdList();
        idList.SetNumberOfIds(1);
	}

    protected void updateLineSelection()
    {
        vtkPolyData selectionPolyData = getSelectionPolyData();
        selectionPolyData.DeepCopy(getEmptyPolyData());
		vtkPoints points = selectionPolyData.GetPoints();
		vtkCellArray vert = selectionPolyData.GetVerts();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray)selectionPolyData.GetCellData().GetScalars();

        int count = 0;
        int numLines = getNumberOfStructures();
        for (int j=0; j<numLines; ++j)
        {
        	Line lin = (Line)getStructure(j);

        	for (int i=0; i<lin.controlPointIds.size(); ++i)
        	{
        		int idx = lin.controlPointIds.get(i);

        		points.InsertNextPoint(lin.xyzPointList.get(idx).xyz);
        		idList.SetId(0, count++);
        		vert.InsertNextCell(idList);
        		if (i == 0)
        			colors.InsertNextTuple4(greenColor[0],greenColor[1],greenColor[2],greenColor[3]);
        		else
        			colors.InsertNextTuple4(redColor[0],redColor[1],redColor[2],redColor[3]);
        	}
        }

		smallBodyModel.shiftPolyLineInNormalDirection(selectionPolyData, 0.001);
    }

	public int getVertexIdFromSelectionCellId(int idx)
	{
		int count = 0;
        int numLines = getNumberOfStructures();
        for (int j=0; j<numLines; ++j)
        {
        	if (idx < count)
        		return j;
            Line lin = (Line)getStructure(j);
            int size = lin.controlPointIds.size();
            count += size;
        }
        
        return -1;
	}
	
	@Override
	public boolean supportsSelection()
	{
		return false;
	}
}
