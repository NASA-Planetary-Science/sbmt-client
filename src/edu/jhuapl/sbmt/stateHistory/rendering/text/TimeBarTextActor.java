package edu.jhuapl.sbmt.stateHistory.rendering.text;

import vtk.vtkTextActor;

import edu.jhuapl.sbmt.util.TimeUtil;

/**
 * vtkTextActor that displays the current time in the renderer
 * @author steelrj1
 *
 */
public class TimeBarTextActor extends vtkTextActor
{

	/**
	 * Constructor.  Sets initial properties for vtkTextActor parent
	 */
	public TimeBarTextActor()
	{
		configure();
	}

	/**
	 * vtk constructor.  Sets initial properties for vtkTextActor parent
	 * @param id
	 */
	public TimeBarTextActor(long id)
	{
		super(id);
		configure();
	}

	/**
	 * Performs initial configuration of the actor
	 */
	private void configure()
	{
		GetTextProperty().SetColor(1.0, 1.0, 1.0);
        GetTextProperty().SetJustificationToCentered();
        GetTextProperty().BoldOn();
        VisibilityOn();
	}

	/**
	 * Updates the time bar with the given <pre>time</pre>
	 * @param time
	 */
	public void updateTimeBarValue(double time)
    {
        String utcValue =TimeUtil.et2str(time).substring(0, 23);
        SetInput(utcValue.trim());
    }

    /**
     * Updates the position of the time bar based on the current window width and height
     * @param windowWidth	the current window width
     * @param windowHeight	the current window height
     */
    public void updateTimeBarPosition(int windowWidth, int windowHeight)
    {
        int newTimeBarWidthInPixels = (int)Math.min(0.75*windowWidth, 200.0);

        int timeBarWidthInPixels = newTimeBarWidthInPixels;
        int timeBarHeight = timeBarWidthInPixels/9;
        int buffer = timeBarWidthInPixels/20;
        int x = buffer + 20; // lower left corner x
        int y = buffer; // lower left corner y

        SetPosition(x+timeBarWidthInPixels/2, y+2);
        GetTextProperty().SetFontSize(timeBarHeight-4);
    }
}
