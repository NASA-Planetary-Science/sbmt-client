package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.model.ModelManager;

public class LineamentMapper 
{
    private ModelManager modelManager;
	private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;

    public LineamentMapper(
			ErosRenderer erosRenderer, 
			ModelManager modelManager
			) 
	{
		this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;

	}
	
	public void mousePressed(MouseEvent e) 
	{
	}
	
	public void mouseReleased(MouseEvent e) 
	{
	}
	
	public void mouseDragged(MouseEvent e) 
	{
	}

	public void mouseMoved(MouseEvent e) 
	{
	}
}
