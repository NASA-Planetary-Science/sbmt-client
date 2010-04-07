package edu.jhuapl.near.model;

import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vtk.vtkProp;


/**
 * Model of structures drawn on Eros such as lineaments and circles.
 * 
 * @author 
 *
 */
public abstract class StructureModel extends Model
{
	public static abstract class Structure
	{
		public abstract Element toXmlDomElement(Document dom);
	    public abstract void fromXmlDomElement(Element element, ErosModel erosModel);
	    public abstract String getClickStatusBarText();
	    public abstract int getId();
	    public abstract String getName();
	    public abstract void setName(String name);
	    public abstract String getType();
	    public abstract String getInfo();
	}
	
	public abstract void addNewStructure();

	public abstract boolean supportsSelection();
	
	public abstract void selectStructure(int idx);
	
	public abstract int getSelectedStructureIndex();
	
	public void highlightStructure(int idx)
	{
	}
	
	public int getHighlightedStructure()
	{
		return -1;
	}
	
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
}
