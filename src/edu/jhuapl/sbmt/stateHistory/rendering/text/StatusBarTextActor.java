package edu.jhuapl.sbmt.stateHistory.rendering.text;

import vtk.vtkTextActor;

/**
 * vtkTextActor that displays information at the bottom of the renderer
 * @author steelrj1
 *
 */
public class StatusBarTextActor extends vtkTextActor
{

	public StatusBarTextActor() {

	}
    /**
     * Updates the position of the status bar based on the current window width and height
     * @param windowWidth	the current window width
     * @param windowHeight	the current window height
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
    }

    /**
     * Updates the status bar with the provided <pre>text</pre>
     * @param text
     */
    public void updateStatusBarValue(String text)
    {
        SetInput(text);
    }
}