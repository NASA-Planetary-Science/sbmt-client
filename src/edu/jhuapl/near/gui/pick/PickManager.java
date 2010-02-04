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
		LINEAMENT_MAPPER, 
		CIRCLE_MAPPER
	}
	
	private PickMode pickMode = PickMode.DEFAULT;
	private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private LineamentMapper lineamentMapper;
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

		lineamentMapper = new LineamentMapper(erosRenderer, modelManager);
		
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
			removePicker(lineamentMapper);
	        break;
		case LINEAMENT_MAPPER:
			defaultPicker.setSuppressPopups(true);
			addPicker(lineamentMapper);
			break;
		//case CIRCLE_MAPPER:
		//	break;
		}
	}
	
	public Picker getLineamentPicker()
	{
		return this.lineamentMapper;
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
