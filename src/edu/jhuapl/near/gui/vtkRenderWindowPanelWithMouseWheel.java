package edu.jhuapl.near.gui;

import java.awt.event.*;

import vtk.vtkRenderWindowPanel;

/**
 * The purpose of this class is to add mouse wheel support to vtkRenderWindowPanel
 * which for some reason appears to be missing.
 * @author kahneg1
 *
 */
public class vtkRenderWindowPanelWithMouseWheel extends vtkRenderWindowPanel 
												implements 
												MouseWheelListener
{
	public vtkRenderWindowPanelWithMouseWheel() 
	{
		addMouseWheelListener(this);
	}

	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1
				: 0;
		shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1
				: 0;

		iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed,
				shiftPressed, '0', 0, "0");

		Lock();
		if (e.getWheelRotation() > 0)
			iren.MouseWheelBackwardEvent();
		else
			iren.MouseWheelForwardEvent();
		UnLock();
	}

	public void mouseEntered(MouseEvent e) 
	{
		// do nothing
	}
}
