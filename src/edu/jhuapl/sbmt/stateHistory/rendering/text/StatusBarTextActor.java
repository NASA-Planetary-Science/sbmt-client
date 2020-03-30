package edu.jhuapl.sbmt.stateHistory.rendering.text;

import vtk.vtkTextActor;

/**
 * @author steelrj1
 *
 */
public class StatusBarTextActor extends vtkTextActor
{

	/**
	 *
	 */
	public StatusBarTextActor()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 */
	public StatusBarTextActor(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}



    /**
     * @param windowWidth
     * @param windowHeight
     */
    public void updateStatusBarPosition(int windowWidth, int windowHeight)
    {
        int newStatusBarWidthInPixels = (int)Math.min(0.75*windowWidth, 200.0);

        int statusBarWidthInPixels = newStatusBarWidthInPixels;
        int statusBarHeight = statusBarWidthInPixels/9;
        int buffer = statusBarWidthInPixels/20;
        int y = buffer; // lower left corner y

        int leftside = windowWidth - statusBarWidthInPixels;

        SetPosition(leftside, y+2);
        GetTextProperty().SetFontSize(statusBarHeight-4);
        GetTextProperty().SetJustificationToCentered();

//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    /**
     * @param text
     */
    public void updateStatusBarValue(String text)
    {
        SetInput(text);
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

}
