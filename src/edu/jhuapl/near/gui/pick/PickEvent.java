package edu.jhuapl.near.gui.pick;

//import java.util.ArrayList;

import java.awt.event.MouseEvent;

import vtk.*;

public class PickEvent 
{
	public static class Point
	{
		public double x;
		public double y;
		public double z;
	}
	
	//public ArrayList<vtkActor> pickedActors = new ArrayList<vtkActor>();
	//public ArrayList<Integer> pickedCells = new ArrayList<Integer>();
	//public ArrayList<Point> pickedPoints = new ArrayList<Point>();
	
	private vtkCellPicker cellPicker;
	private MouseEvent mouseEvent;
	
	public PickEvent(vtkCellPicker cellPicker,
				MouseEvent mouseEvent)
	{
		this.cellPicker = cellPicker;
		this.mouseEvent = mouseEvent;
	}

	public vtkCellPicker getCellPicker()
	{
		return this.cellPicker;
	}
	
	public MouseEvent getMouseEvent()
	{
		return this.mouseEvent;
	}
	
	public boolean isRightMouseButton()
	{
		return this.mouseEvent.getButton() == MouseEvent.BUTTON3;
	}
}
