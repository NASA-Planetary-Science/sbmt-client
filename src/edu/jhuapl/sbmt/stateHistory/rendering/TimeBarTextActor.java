package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkTextActor;

import edu.jhuapl.sbmt.util.TimeUtil;

public class TimeBarTextActor extends vtkTextActor
{

	public TimeBarTextActor()
	{
		GetTextProperty().SetColor(1.0, 1.0, 1.0);
        GetTextProperty().SetJustificationToCentered();
        GetTextProperty().BoldOn();
        VisibilityOn();
	}

	public TimeBarTextActor(long id)
	{
		super(id);
		GetTextProperty().SetColor(1.0, 1.0, 1.0);
        GetTextProperty().SetJustificationToCentered();
        GetTextProperty().BoldOn();
        VisibilityOn();
	}

	public void updateTimeBarValue(double time)
    {
        String utcValue =TimeUtil.et2str(time).substring(0, 23);
        SetInput(utcValue.trim());
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void updateTimeBarPosition(int windowWidth, int windowHeight)
    {
        int newTimeBarWidthInPixels = (int)Math.min(0.75*windowWidth, 200.0);

        int timeBarWidthInPixels = newTimeBarWidthInPixels;
        int timeBarHeight = timeBarWidthInPixels/9;
        int buffer = timeBarWidthInPixels/20;
        int x = buffer + 20; // lower left corner x
        //        int x = windowWidth - timeBarWidthInPixels - buffer; // lower right corner x
        int y = buffer; // lower left corner y

        SetPosition(x+timeBarWidthInPixels/2, y+2);
        GetTextProperty().SetFontSize(timeBarHeight-4);

//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
