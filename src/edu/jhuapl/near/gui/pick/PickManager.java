package edu.jhuapl.near.gui.pick;

import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;
import vtk.*;

public class PickManager extends Picker
{
	public enum PickMode
	{
		DEFAULT, 
		POINT_SELECTION, 
		RECTANGLE_SELECTION, 
		LINE_DRAW, 
		CIRCLE_DRAW,
		POINT_DRAW
	}
	
	private PickMode pickMode = PickMode.DEFAULT;
	private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private LinePicker linePicker;
    private CirclePicker circlePicker;
    private DefaultPicker defaultPicker;
    
	public PickManager(
			ErosRenderer erosRenderer, 
			StatusBar statusBar,
			ModelManager modelManager,
			ModelInfoWindowManager infoPanelManager)
	{
		this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();

		modelManager.addPropertyChangeListener(this);
		
		renWin.addMouseListener(this);
        renWin.addMouseMotionListener(this);
        renWin.addMouseWheelListener(this);

		linePicker = new LinePicker(erosRenderer, modelManager);
		circlePicker = new CirclePicker(erosRenderer, modelManager);
		
		defaultPicker = new DefaultPicker(erosRenderer, statusBar, modelManager, infoPanelManager);

		addPicker(defaultPicker);
	}

	public void setPickMode(PickMode mode)
	{
		if (this.pickMode == mode)
			return;
		
		this.pickMode = mode;
		switch(this.pickMode)
		{
		case DEFAULT:
			erosRenderer.setInteractorToDefault();
			defaultPicker.setSuppressPopups(false);
			removePicker(linePicker);
			removePicker(circlePicker);
	        break;
		case LINE_DRAW:
			erosRenderer.setInteractorToNone();
			defaultPicker.setSuppressPopups(true);
			removePicker(circlePicker);
			addPicker(linePicker);
			break;
		case CIRCLE_DRAW:
			erosRenderer.setInteractorToNone();
			defaultPicker.setSuppressPopups(true);
			removePicker(linePicker);
			addPicker(circlePicker);
			break;
		}
	}
	
	public LinePicker getLineamentPicker()
	{
		return this.linePicker;
	}
	
	private void addPicker(Picker picker)
	{
		renWin.addMouseListener(picker);
        renWin.addMouseMotionListener(picker);
        renWin.addMouseWheelListener(picker);
	}
	
	private void removePicker(Picker picker)
	{
		renWin.removeMouseListener(picker);
        renWin.removeMouseMotionListener(picker);
        renWin.removeMouseWheelListener(picker);
	}
	
}
