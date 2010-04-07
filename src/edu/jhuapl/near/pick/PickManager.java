package edu.jhuapl.near.pick;

import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.popupmenus.PopupManager;
import vtk.*;

public class PickManager extends Picker
{
	public enum PickMode
	{
		DEFAULT, 
		CIRCLE_SELECTION, 
		LINE_DRAW, 
		CIRCLE_DRAW,
		POINT_DRAW
	}
	
	private PickMode pickMode = PickMode.DEFAULT;
	private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    
    private LinePicker linePicker;
    private CirclePicker circlePicker;
    private PointPicker pointPicker;
    private DefaultPicker defaultPicker;
    private CircleSelectionPicker circleSelectionPicker;
    
	public PickManager(
			ErosRenderer erosRenderer, 
			StatusBar statusBar,
			ModelManager modelManager,
			ModelInfoWindowManager infoPanelManager,
			PopupManager popupManager)
	{
		this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();

		modelManager.addPropertyChangeListener(this);
		
		renWin.addMouseListener(this);
        renWin.addMouseMotionListener(this);
        renWin.addMouseWheelListener(this);

		linePicker = new LinePicker(erosRenderer, modelManager);
		circlePicker = new CirclePicker(erosRenderer, modelManager);
		pointPicker = new PointPicker(erosRenderer, modelManager);

		circleSelectionPicker = new CircleSelectionPicker(erosRenderer, modelManager);
		
		defaultPicker = new DefaultPicker(erosRenderer, statusBar, modelManager, infoPanelManager, popupManager);

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
			removePicker(linePicker);
			removePicker(circlePicker);
			removePicker(pointPicker);
			removePicker(circleSelectionPicker);
	        break;
		case LINE_DRAW:
			erosRenderer.setInteractorToNone();
			removePicker(circlePicker);
			removePicker(pointPicker);
			removePicker(circleSelectionPicker);
			addPicker(linePicker);
			break;
		case CIRCLE_DRAW:
			erosRenderer.setInteractorToNone();
			removePicker(linePicker);
			removePicker(pointPicker);
			removePicker(circleSelectionPicker);
			addPicker(circlePicker);
			break;
		case POINT_DRAW:
			erosRenderer.setInteractorToNone();
			removePicker(linePicker);
			removePicker(circlePicker);
			removePicker(circleSelectionPicker);
			addPicker(pointPicker);
			break;
		case CIRCLE_SELECTION:
			erosRenderer.setInteractorToNone();
			removePicker(linePicker);
			removePicker(pointPicker);
			removePicker(circlePicker);
			addPicker(circleSelectionPicker);
			break;
		}
	}
	
	public DefaultPicker getDefaultPicker()
	{
		return defaultPicker;
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
