package edu.jhuapl.near.model;

import java.io.*;

import vtk.vtkProp;


/**
 * Model of structures drawn on a body such as lines and circles.
 *
 * @author
 *
 */
public abstract class StructureModel extends Model
{
	public static abstract class Structure
	{
	    public abstract String getClickStatusBarText();
	    public abstract int getId();
	    public abstract String getName();
	    public abstract void setName(String name);
	    public abstract String getType();
	    public abstract String getInfo();
	    public abstract int[] getColor();
	    public abstract void setColor(int[] color);
	}

	public StructureModel(String name)
	{
		super(name);
	}

	public abstract void addNewStructure();

	public abstract boolean supportsSelection();

	public abstract void selectStructure(int idx);

	public abstract int getSelectedStructureIndex();

	public abstract void highlightStructure(int idx);

	public abstract int getHighlightedStructure();

	public abstract int getNumberOfStructures();

	public abstract void removeStructure(int idx);

	public void removeAllStructures()
	{
    	for (int i=getNumberOfStructures()-1; i>=0; --i)
    		removeStructure(i);
	}

	public abstract Structure getStructure(int idx);

	public abstract int getStructureIndexFromCellId(int cellId, vtkProp prop);

	public abstract void loadModel(File file) throws Exception;

	public abstract void saveModel(File file) throws Exception;

	public abstract void setStructureColor(int idx, int[] color);
}
